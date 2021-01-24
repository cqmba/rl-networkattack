package q_learning.mdp;

import aima.core.agent.Action;
import aima.core.probability.mdp.ActionsFunction;
import q_learning.Pair;

import java.util.ArrayList;
import java.util.List;
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
     *      The discount factor, which has to be between 0 and 1. A high discount factor lets the agent be far sighted
     *      while a low factor makes it consider early rewards more. In some cases a discount factor of or near 1
     *      can result in divergence or instabilities.
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

    //##########################################################################
    //              SETTERS FOR PARAMETERS
    //##########################################################################

    public void setLearningRate(double learningRate) {
        if (learningRate < 0 || learningRate > 1)
            throw new IllegalArgumentException("Learning Rate must be in 0<=LearningRate<=1");
        agent.setLearningRate(learningRate);
    }

    public void setDiscountFactor(double discountFactor) {
        if (discountFactor < 0 || discountFactor > 1)
            throw new IllegalArgumentException("Discount factor must be in 0<=DiscountFactor<=1");
        agent.setDiscountFactor(discountFactor);
    }

    public void setActionEpsilon(double actionEpsilon) {
        if (actionEpsilon < 0 || actionEpsilon > 1)
            throw new IllegalArgumentException("Action epsilon must be in 0<=ActionEpsilon<=1");
        agent.setRandomness(actionEpsilon);
    }

    public void setNe(int ne) {
        if (ne <= 0)
            throw new IllegalArgumentException("Ne must be greater 0");
        agent.setNe(ne);
    }

    //##########################################################################
    //              FUNCTIONALITY
    //##########################################################################

    /**
     * Runs the QLearningAgent "iterations" times.
     * @param iterations The number of iterations to run
     * @param initialIterations The number of iterations run (in addition) starting from the initial state set by the mdp.
     */
    public void runIterations(int iterations, int initialIterations) {
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

            runSingleIteration(curState, i);
        }

        for (int i = 0; i < initialIterations; i++) {
            S curState = mdp.getInitialState();

            runSingleIteration(curState, i);
        }
    }

    private void runSingleIteration(S initialState, int iteration) {
        S curState = initialState;
        // run the simulation from curState until we reach a final state
        A curAction;
        if (iteration % 100 == 0 && LOGGER.isLoggable(Level.INFO))
            LOGGER.info(String.format("Running iteration %d...%n", iteration));
        do {
            // get next action using q learning
            curAction = agent.execute(curState, mdp);

            // Do the action and set the new state
            if (curAction != null)
                curState = mdp.stateTransition(curState, curAction);

        } while (curAction != null);
    }

    /**
     * Returns the estimated utility per state
     *
     * @return The utility for each state
     */
    public Map<S, Double> getUtility() {
        return agent.getUtility();
    }

    /**
     * Returns the best action path that was learned.
     * Might throw exceptions, if the learned values do not suffice to find a path (not every path was visited, since
     * not enough iterations were run) or a loop is detected. Also this method might run infinitely long, if the
     * loopSize is picked too small. A big loopSize might slow computation.
     *
     * @return The best path in a list of the current state and action. If an action is null, it was a finalState.
     */
    public List<Pair<S, A>> getPreferredPath(int loopSize) throws Exception {
        List<Pair<S, A>> actionStates = new ArrayList<>();
        S curState = mdp.getInitialState();
        A curAction;
        Map<Pair<S, A>, Double> Q = agent.getQ();

        do {
            curAction = maxAPrime(curState, mdp.getActionsFunction(), Q, mdp);
            actionStates.add(new Pair<>(curState, curAction));

            if (curAction != null) {
                curState = mdp.stateTransition(curState, curAction);
            } else {
                if (!mdp.isFinalState(curState))
                    throw new Exception("Not learned enough to generate full path");
            }

            for (int i = 0; i < loopSize; i++) {
                if (actionStates.size() - (i + 1) < 0)
                    break;

                Pair<S, A> past = actionStates.get(actionStates.size() - (i + 1));
                if (past.getA().equals(curState))
                    throw new Exception("Loop detected. Cancelling computation");
            }
        } while (curAction != null);

        return actionStates;
    }

    /**
     * This method is originally from the LearningAgent and was programmed by the aima-team. The only
     * change is that not the reward is returned, but the action itself.
     *
     * This method returns the action with the highest value, which would be chosen from the given state.
     * @param sPrime The state from which the best action should be returned
     * @param actionsFunction All possible actions
     * @param Q The utility Map from the QLearningAgent
     * @return The action which would be chosen
     */
    private A maxAPrime(S sPrime, ActionsFunction<S, A> actionsFunction, Map<Pair<S, A>, Double> Q, MDP<S, A> mdp) {
        double max = Double.NEGATIVE_INFINITY;
        A action = null;
        if (mdp.isFinalState(sPrime)) {
            // a terminal state
            max = Q.get(new Pair<>(sPrime, null));
        } else {
            for (A aPrime : actionsFunction.actions(sPrime)) {
                Double Q_sPrimeAPrime = Q.get(new Pair<>(sPrime, aPrime));
                if (null != Q_sPrimeAPrime && Q_sPrimeAPrime > max) {
                    max = Q_sPrimeAPrime;
                    action = aPrime;
                }
            }
        }
        if (max == Double.NEGATIVE_INFINITY) {
            // Assign 0 as the mimics Q being initialized to 0 up front.
            action = null;
        }
        return action;
    }
}
