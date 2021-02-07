package q_learning.mdp;

import aima.core.probability.mdp.ActionsFunction;
import org.junit.Test;
import q_learning.Pair;
import q_learning.env_cells.CellAction;
import q_learning.env_cells.CellState;
import q_learning.env_cells.CellStateReward;
import q_learning.interfaces.StateReward;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * This class runs and initializes the Q-Learning Agent.
 * The Scenario is a Cellworld as follows
 *
 * -------------------------------------
 * |  x  |     |     |||||||     |     |
 * -------------------------------------
 * |||||||||||||     |||||||     |     |
 * -------------------------------------
 * |     |     |     |     |     | 1.0 |
 * -------------------------------------
 * |     |||||||||||||||||||     |     |
 * -------------------------------------
 * |     | 2.0 |     |     |     |     |
 * -------------------------------------
 *
 * Each cell is a state with x and y coordinate, where 0, 0 is the top left corner and 5, 4 is the bottom right corner.
 * The x at the top left cell is the starting position, aka the initial state. The filled out cells are walls where
 * the agent cannot move to. The 1.0 and 2.0 are final states with reward 1 and 2. The task for the Q-Learning agent
 * is to find for each cell the best action path (policy) to get a high reward. Each cell except the final states have
 * no reward and each action is for free and does not reduce the reward.
 *
 * The output of the algorithm is each cell and the corresponding action taken at run i. Note that for each run the
 * initial state of the algorithm is selected at random and the actual initial state is ignored. At a later point the
 * actual initial state, which is marked with x, will be used to print the learned policy, but for learning purposes
 * it shows a lot better results to choose a random starting point at each run.
 *
 * At the end another output is printed. It shows each state with its expected utility. So if a state has utility 1.999
 * or similar, it means that it will choose an action path, which will lead to the finals state 2.0. If it has utility
 * 0.999 it will lead to the final state 1.0.
 *
 * Action NULL means that it is the final state and there is no action to be taken available.
 *
 * Note that the learning process is done a number of times. So with each iteration the agent goes from a starting state
 * until he reaches a final state. When the number of iterations is picked high enough, it should end up with having
 * an expected value of nearly 2.0 for every cell, since he can move from anywhere to the final state 2.0 (except the
 * other final state of course). When that point is reached however, the agent cannot distinguish in which direction
 * the best final state is. So he wanders randomly around until he reaches the final state 2.0. This is expected
 * behaviour and not a bug. It could be changed by giving each action done a penalty.
 */
public class QLearnerTest {
    private static final Logger LOGGER = Logger.getLogger(QLearnerTest.class.getName());

    private static final Level LEVEL = Level.ALL;

    /*
     * Learning Rate determines how much new information is used instead of old and is between 0 and 1. A learning rate
     * of 1 overrides all learned data, while a learning rate of 0 does not learn. Usually smaller values are used
     * (0.01 to 0.1), so that jumping over the perfect value (which should be learned) is prevented.
     */
    private static final double LEARNING_RATE = 0.2;

    /*
     * In q-learning an action is selected and the value of that action is calculated. For that the best action after
     * that is also considered. The discount factor manages the importance of the next actions and must be between
     * 0 and 1. A low value is considered short-sighted, while a high value looks further into the future.
     * Due to problems when the discount factor is near 1, it is advised to use a smaller value.
     */
    private static final double DISCOUNT_FACTOR = 1.0;

    /*
     * In order to select the next action, the q-learning agent will select either the best action by utility for
     * the next step or with probability of epsilon will select a random action.
     */
    private static final double EPSILON = 0.05;

    /*
     * A fixed seed for random path selection
     */
    private static final int SEED = 0;

    /*
     * To which extend two doubles may differ and be considered identical
     */
    private static final double ERROR = 0.000000001;

    /*
     * Number of times each state is at least visited
     */
    private static final int NE = 5;

    // Should be the highest (or higher than that) reward possible
    private static final double R_PLUS = 2.0;

    /**
     * The validity of the QLearner and QLearningAgent is tested using an example case.
     * If the test is successful the utilities calculated should be roughly 2.0, except for one state, which
     * has a fixed utility of 1.0.
     */
    @Test
    public void runIterations_happyPath() {
        LOGGER.setLevel(LEVEL);

        // generate states, actions and MDP
        HashMap<CellState, StateReward<CellState, CellAction>> states = generateStates();
        ActionsFunction<CellState, CellAction> actions = generateActions(states);
        QStateTransition<CellState, CellAction> transitions = generateTransitions(states, actions);
        HashSet<CellState> finalStates = new HashSet<>();
        finalStates.add(new CellState(5, 2));
        finalStates.add(new CellState(1, 4));
        MDP<CellState, CellAction> mdp = new MDP<>(states, new CellState(0, 0), actions, transitions, finalStates);

        QLearner<CellState, CellAction> learner = new QLearner<>(mdp, LEARNING_RATE, DISCOUNT_FACTOR, EPSILON, ERROR, NE, R_PLUS, SEED, 100);

        learner.runIterations(20000, 20, "", false);

        // print the learned results.
        // Prints each states calculated utility
        // The actual actions taken in each state will be implemented later
        Map<CellState, Double> util = learner.getUtility();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Expected utility per state:");
            for (Map.Entry<CellState, Double> entry : util.entrySet()) {
                LOGGER.fine(String.format("\tState: x=%d, y=%d \tUtiliy: %.16f", entry.getKey().getX(),
                        entry.getKey().getY(), util.get(entry.getKey())));
            }
        }

        for (Map.Entry<CellState, Double> entry : util.entrySet()) {
            if (entry.getKey().getX() == 5 && entry.getKey().getY() == 2) {
                assertEquals(1.0, entry.getValue(), 10e-10);
            } else {
                assertEquals(2.0, entry.getValue(), 10e-10);
            }
        }

        try {
            List<Pair<CellState, CellAction>> path = learner.getPreferredPath(0);
            for (Pair<CellState, CellAction> pair : path) {
                LOGGER.info(String.format("State: x=%d, y=%d", pair.getA().getX(), pair.getA().getY()));
                LOGGER.info(String.format("\tAction: %s", pair.getB()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// ###################################### The following is setting up the environment

    /**
     * Initializes the states
     * @return The states of the MDP
     */
    private static HashMap<CellState, StateReward<CellState, CellAction>> generateStates() {
        HashMap<CellState, StateReward<CellState, CellAction>> states = new HashMap<>();
        for (int x = 0; x <= 5; x++) {
            for (int y = 0; y <= 4; y++) {
                if (!(x == 3 && y == 0) && !(x == 0 && y == 1) && !(x == 1 && y ==1) && !(x == 3 && y == 1) &&
                        !(x == 1 && y == 3) && !(x == 2 && y == 3) && !(x == 3 && y == 3)) {
                    double reward = 0.0;
                    if (x == 5 && y == 2)
                        reward = 1.0;
                    if (x == 1 && y == 4)
                        reward = 2.0;

                    states.put(new CellState(x, y), new CellStateReward(new CellState(x, y), reward));
                }
            }
        }
        return states;
    }

    /**
     * Initializes the actions possible per state
     * @param states The states possible
     * @return An ActionFunction, which returns all possible actions per state
     */
    private static ActionsFunction<CellState, CellAction> generateActions(
            Map<CellState, StateReward<CellState, CellAction>> states) {
        QActionsFunction<CellState, CellAction> actions = new QActionsFunction(states);
        CellAction up = new CellAction(0);
        CellAction down = new CellAction(1);
        CellAction left = new CellAction(2);
        CellAction right = new CellAction(3);

        for (CellState state : states.keySet()) {
            if (states.get(state).reward(null, null) > 0.5)
                continue;
            int x = state.getX();
            int y = state.getY();

            // up
            if ((x == 4 && y > 0) || (x == 5 && y > 0) ||
                    (x == 2 && y == 1) || (x == 2 && y == 2) ||
                    (x == 0 && y >= 3))
                actions.addAction(state, up);

            // down
            if ((x == 4 && y < 4) || (x == 5 && y < 4) ||
                    (x == 2 && y <= 1) ||
                    (x == 0 && y == 2) || (x == 0 && y == 3))
                actions.addAction(state, down);

            // left
            if ((x == 5) ||
                    (x > 0 && y == 2) ||
                    (x > 0 && y == 4) ||
                    (x == 1 && y == 0) ||
                    (x == 2 && y == 0))
                actions.addAction(state, left);

            // right
            if ((x == 4) ||
                    (x < 5 && y == 2) ||
                    (x < 5 && y == 4) ||
                    (x == 0 && y == 0) ||
                    (x == 1 && y == 0))
                actions.addAction(state, right);
        }
        return actions;
    }

    /**
     * Transitions from a given state using a given action to a new state.
     * @param state The original state
     * @param action The action used
     * @return The new state
     */
    private static CellState useAction(CellState state, CellAction action) {
        switch (action.getDirection()) {
            case 0: return new CellState(state.getX(), state.getY() - 1);
            case 1: return new CellState(state.getX(), state.getY() + 1);
            case 2: return new CellState(state.getX() - 1, state.getY());
            case 3: return new CellState(state.getX() + 1, state.getY());
            default: return null;
        }
    }

    private static QStateTransition<CellState, CellAction> generateTransitions(Map<CellState,
            StateReward<CellState, CellAction>> states, ActionsFunction<CellState, CellAction> actions) {
        QStateTransition<CellState, CellAction> transition = new QStateTransition<>();
        for (CellState state : states.keySet()) {
            for (CellAction action : actions.actions(state)) {
                transition.addTransition(state, action, useAction(state, action));
            }
        }

        return transition;
    }
}