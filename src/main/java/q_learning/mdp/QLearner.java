package q_learning.mdp;

import aima.core.agent.Action;

import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes a QLearningAgent using a MDP.
 *
 * @param <S> The state class
 * @param <A> The action class
 */
public class QLearner<S, A extends Action> {
    private static final Logger LOGGER = Logger.getLogger(QLearner.class.getName());

    private final MDP<S, A> mdp;

    private final QLearningAgent<S, A> agent;

    private final Random random;

    /**
     * This constructor initializes a QLearningAgent given the MDP it should base on and a number of parameters
     * used for the learning process.
     *
     * @param mdp
     *      The MDP that should be used
     * @param learningRate
     *      The learning rate, which has to be between 0 and 1. The learning rate determines how much of the new
     *      information is used. A learning rate of 1 overrides all previous data, while a learning rate of 0
     *      ignores new information. A learning rate of 0.01 to 0.1 is recommended.
     * @param discountFactor
     *      The discount factor, which has to be between 0 and 1.
     *      (numbers smaller than 1 currently generate false results)
     * @param actionEpsilon
     *      The epsilon value used for action determination, which must be between 0 and 1. Usually the action with
     *      the highest reward is used as the next action, however with a probability of epsilon a random action is
     *      selected, which can help to find better paths. A small value is recommended.
     * @param errorEpsilon
     *      The error epsilon tells how far apart two floating point numbers can be and still be considered identical.
     *      A value in the region of e-10 is recommended.
     * @param ne
     *      A state has a high reward if it was visited less than this number.
     * @param rPlus
     *      This should be higher than the maximum possible reward. A state has the reward of rPlus if it was visited
     *      less than Ne times.
     * @param seed
     *      A seed for a random number generator used to determine random next actions in case it cannot be decided by
     *      reward.
     */
    public QLearner(MDP<S, A> mdp, double learningRate, double discountFactor, double actionEpsilon,
                    double errorEpsilon, int ne, double rPlus, int seed) {
        // Check if Values are chosen in possible range
        if (learningRate < 0 || learningRate > 1)
            throw new IllegalArgumentException("Learning Rate must be in 0<=LearningRate<=1");
        if (discountFactor < 0 || discountFactor > 1)
            throw new IllegalArgumentException("Discount factor must be in 0<=DiscountFactor<=1");
        if (actionEpsilon < 0 || actionEpsilon > 1)
            throw new IllegalArgumentException("Action epsilon must be in 0<=ActionEpsilon<=1");
        if (errorEpsilon < 0)
            throw new IllegalArgumentException("Error epsilon must be greater 0");
        if (ne <= 0)
            throw new IllegalArgumentException("Ne must be greater 0");

        this.mdp = mdp;
        this.agent = new QLearningAgent<>(mdp.getActionsFunction(), learningRate, discountFactor, actionEpsilon,
                ne, rPlus, seed, errorEpsilon);

        this.random = new Random(seed);
    }

    /**
     * Runs the QLearningAgent "iterations" times.
     * @param iterations
     */
    public void runIterations(int iterations) {
        if (iterations <= 0)
            throw new IllegalArgumentException("Iterations must be greater 0");

        // run the q learning agent
        // Each run is started from a random state and run until a final state is reached.
        // This is done x times as defined in the "for" below
        for (int i = 0; i < iterations; i++) {
            // get random initial state and an action
            int item = random.nextInt(mdp.states().size());
            S curState = null;
            int counter = 0;
            for (S state : mdp.states()) {
                if (counter == item) {
                    curState = state;
                    break;
                }
                counter++;
            }

            // run the simulation from curState until we reach a final state
            A curAction;
            if (i % 100 == 0 && LOGGER.isLoggable(Level.INFO))
                LOGGER.info(String.format("Running iteration %d...%n", i));
            do {
                // get next action using q learning
                curAction = agent.execute(curState, mdp);

                // Do the action and set the new state
                if (curAction != null)
                    curState = mdp.stateTransition(curState, curAction);

            } while (curAction != null);
        }
    }

    /**
     * Returns the estimated utility per state
     *
     * @return The utility for each state
     */
    public Map<S, Double> getUtility() {
        return agent.getUtility();
    }
}
