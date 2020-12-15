package q_learning;

import aima.core.probability.mdp.ActionsFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * This class runs and initializes the Q-Learning Agent.
 */
public class MainLearning {
    /*
     * Learning Rate determines how much new information is used instead of old and is between 0 and 1. A learning rate
     * of 1 overrides all learned data, while a learning rate of 0 does not learn. Usually smaller values are used
     * (0.01 to 0.05), so that jumping over the perfect value (which should be learned) is prevented.
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
     * Main Function, which initializes the MDP and runs the Q Leaerning Agent. At the end it prints the policy that
     * was learned.
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

        Random random = new Random();
        // run the q learning agent
        // Each run is started from the initial state and run until a final state is reached.
        // This is done x times as defined below
        for (int i = 0; i < 2000; i++) {
            // get initial state and an action
            // NOTE: The initial action is not used. It is just set for the while loop
            //NetworkState curState = mdp.getInitialState();
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

            System.out.println(String.format("running iteration %d...", i));
            while (curAction != null) {
                // get next action using q learning
                curAction = agent.execute(new NetworkPerceptStateReward(curState, mdp.reward(curState)));
                System.out.println(String.format("State: %d, %d Action: %s", curState.getX(), curState.getY(), curAction == null ? "NULL" : curAction.toString()));

                // Do the action and set the new state
                if (curAction != null)
                    curState = useAction(curState, curAction);
            }
        }

        // print the learned results.
        // This part prints all actions taken in order to check that all where actually found
        /*Map<Pair<NetworkState, NetworkAction>, Double> Q = agent.getQ();
        for (Pair<NetworkState, NetworkAction> pair : Q.keySet()) {
            System.out.println(String.format("%d, %d, %s, %.2f", pair.getA().getX(), pair.getA().getY(), pair.getB() == null ? "NULL" : pair.getB().toString(), Q.get(pair)));
        }
        System.out.println(String.format("Numbers of Actions: %d", Q.size()));


        // This part prints the policy. First the best action per state is found, then printed.
        Set<NetworkState> stateSet = mdp.states();
        HashMap<NetworkState, Pair<NetworkAction, Double>> policy = new HashMap<>();
        for (NetworkState state : stateSet) {
            double max = Double.NEGATIVE_INFINITY;
            for (Pair<NetworkState, NetworkAction> pair : Q.keySet()) {
                if (pair.getA().equals(state)) {
                    if (Q.get(pair) > max) {
                        policy.put(state, new Pair<>(pair.getB(), Q.get(pair)));
                    }
                }
            }
        }
        for (NetworkState state : policy.keySet()) {
            System.out.println(state.toString() + ": " + policy.get(state).toString());
        }*/

        Map<NetworkState, Double> util = agent.getUtility();
        for (NetworkState state : util.keySet()) {
            System.out.println(state.toString() + ", " + util.get(state));
        }
    }

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
