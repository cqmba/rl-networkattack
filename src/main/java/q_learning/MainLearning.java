package q_learning;

import aima.core.probability.mdp.ActionsFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
public class MainLearning {
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
     * Number of times each state is at least viseted
     */
    private static final int Ne = 5;

    // Should be the highest (or higher than that) reward possible
    private static final double Rplus = 2.0;


    /**
     * Main Function, which initializes the MDP and runs the Q Learning Agent. It prints each run, starting from a
     * random state to an end state, which actions where taken at which state. At the end it prints for each state
     * the learned expected utility it will return.
     * @param args
     */
    public static void main(String[] args) {
        // Check if Values are chosen in possible range
        if (LEARNING_RATE < 0 || LEARNING_RATE > 1)
            throw new IllegalArgumentException("Learning Rate must be in 0<=LearningRate<=1");
        if (DISCOUNT_FACTOR < 0 || DISCOUNT_FACTOR > 1)
            throw new IllegalArgumentException("Discount factor must be in 0<=DiscountFactor<=1");
        if (Ne <= 0)
            throw new IllegalArgumentException("Ne must be greater 0");

        // generate states, actions and MDP
        Map<NetworkState, Double> states = generateStates();
        MDP mdp = new MDP(states, new NetworkState(0, 0), generateActions(states));

        // Initialize QLearningAgent
        QLearningAgent<NetworkState, NetworkAction> agent =
                new QLearningAgent<>(mdp.getActionsFunction(), LEARNING_RATE, DISCOUNT_FACTOR, EPSILON, Ne, Rplus,
                        SEED, ERROR);

        // Used to get a random initial state
        Random random = new Random(SEED + 1);

        // run the q learning agent
        // Each run is started from a random state and run until a final state is reached.
        // This is done x times as defined in the "for" below
        for (int i = 0; i < 2000; i++) {
            // get random initial state and an action
            // NOTE: The initial action is not used. It is just set for the while loop. Will be later changed to repeat until TODO
            int item = random.nextInt(mdp.states().size());
            NetworkState curState = null;
            int counter = 0;
            for (NetworkState state : mdp.states()) {
                if (counter == item) {
                    curState = state;
                    break;
                }
                counter++;
            }
            NetworkAction curAction = new NetworkAction(3);

            // run the simulation from curState until we reach a final state
            System.out.printf("Running iteration %d...%n", i);
            while (curAction != null) {
                // get next action using q learning
                curAction = agent.execute(new NetworkPerceptStateReward(curState, mdp.reward(curState)));
                System.out.printf("\tState: x=%d, y=%d \tAction: %s%n", curState.getX(), curState.getY(), curAction == null ? "NULL" : curAction.toString());

                // Do the action and set the new state
                if (curAction != null)
                    curState = useAction(curState, curAction);
            }
        }

        // print the learned results.
        // Prints each states calculated utility
        // The actual actions taken in each state will be implemented later
        System.out.println("Expected utility per state:");
        Map<NetworkState, Double> util = agent.getUtility();
        for (NetworkState state : util.keySet()) {
            System.out.printf("\tState: x=%d, y=%d \tUtiliy: %.16f%n", state.getX(), state.getY(), util.get(state));
        }
    }

    // ###################################### The following is setting up the environment

    /**
     * Initializes the states
     * @return The states of the MDP
     */
    private static Map<NetworkState, Double> generateStates() {
        Map<NetworkState, Double> states = new HashMap<>();
        for (int x = 0; x <= 5; x++) {
            for (int y = 0; y <= 4; y++) {
                if (!(x == 3 && y == 0) &&
                !(x == 0 && y == 1) &&
                !(x == 1 && y ==1) &&
                !(x == 3 && y == 1) &&
                !(x == 1 && y == 3) &&
                !(x == 2 && y == 3) &&
                !(x == 3 && y == 3)) {
                    double reward = 0.0;
                    if (x == 5 && y == 2)
                        reward = 1.0;
                    if (x == 1 && y == 4)
                        reward = 2.0;

                    states.put(new NetworkState(x, y), reward);
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
    private static ActionsFunction<NetworkState, NetworkAction> generateActions(Map<NetworkState, Double> states) {
        NetworkActionsFunction actions = new NetworkActionsFunction(states);
        NetworkAction up = new NetworkAction(0);
        NetworkAction down = new NetworkAction(1);
        NetworkAction left = new NetworkAction(2);
        NetworkAction right = new NetworkAction(3);

        for (NetworkState state : states.keySet()) {
            if (states.get(state) > 0.5)
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
    private static NetworkState useAction(NetworkState state, NetworkAction action) {
        switch (action.getDirection()) {
            case 0: return new NetworkState(state.getX(), state.getY() - 1);
            case 1: return new NetworkState(state.getX(), state.getY() + 1);
            case 2: return new NetworkState(state.getX() - 1, state.getY());
            case 3: return new NetworkState(state.getX() + 1, state.getY());
            default: return null;
        }
    }
}
