package q_learning;

import aima.core.agent.Action;

import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QLearner<S, A extends Action> {
    private static final Logger LOGGER = Logger.getLogger(QLearner.class.getName());

    private final MDP<S, A> mdp;

    private final QLearningAgent<S, A> agent;

    private final Random random;

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

    public Map<S, Double> getUtility() {
        return agent.getUtility();
    }
}
