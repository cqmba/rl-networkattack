package q_learning.mdp;

import aima.core.agent.Action;
import aima.core.probability.mdp.ActionsFunction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import q_learning.utils.FullRun;
import q_learning.utils.Pair;
import q_learning.utils.Parameter;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes a QLearningAgent using a MDP.
 *
 * @param <S> The state class
 * @param <A> The action class
 */
public class QLearner<S extends Serializable, A extends Action & Serializable> {
    private static final Logger LOGGER = Logger.getLogger(QLearner.class.getName());

    private final MDP<S, A> mdp;

    private final QLearningAgent<S, A> agent;

    private Random random;
    private Parameter parameter;

    private final int loggingCount;

    private final List<FullRun> runData = new ArrayList<>();

    /**
     * This constructor initializes a QLearningAgent given the MDP it should base on and a number of parameters
     * used for the learning process.
     *
     * @param mdp
     *      The MDP that should be used
     * @param parameter
     *      The parameter used for learning
     * @param loggingCount
     *      Every loggingCount'th iteration the logger will print the current iteration to the console.
     */
    public QLearner(MDP<S, A> mdp, Parameter parameter, int loggingCount) {
        if (parameter == null)
            throw new IllegalArgumentException("Parameter are null");

        this.mdp = mdp;
        this.agent = new QLearningAgent<>(mdp.getActionsFunction(), parameter);

        this.random = new Random(parameter.getSeed());
        this.parameter = parameter;
        this.loggingCount = loggingCount;
    }

    //##########################################################################
    //              SETTERS FOR PARAMETERS
    //##########################################################################

    public void setParameter(Parameter parameter) {
        if (parameter == null)
            throw new IllegalArgumentException("Parameters are null");

        this.parameter = parameter;
        agent.setParameter(parameter);
        this.random = new Random(parameter.getSeed());
    }

    //##########################################################################
    //              FUNCTIONALITY
    //##########################################################################

    /**
     * Runs the QLearningAgent
     */
    public void runIterations() {
        List<Pair<Integer, Double>> accumulatedRewards = new ArrayList<>();

        // run the q learning agent
        // Each run is started from a random state and run until a final state is reached.
        // This is done x times as defined in the "for" below
        for (int i = 0; i < parameter.getIterations(); i++) {
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

            List<Double> curRewards = runSingleIteration(curState, i);
            double sum = 0;
            for (Double r : curRewards)
                sum += r != null ? r : 0;
            accumulatedRewards.add(new Pair<>(i, sum));
        }

        for (int i = parameter.getIterations(); i < parameter.getIterations() + parameter.getInitialStateIterations(); i++) {
            S curState = mdp.getInitialState();

            List<Double> curRewards = runSingleIteration(curState, i);
            double sum = 0;
            for (Double r : curRewards)
                sum += r != null ? r : 0;
            accumulatedRewards.add(new Pair<>(i, sum));
        }

        byte[] q = null;
        if (parameter.getSaveQ()) {
            try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bout)) {
                oos.writeObject((HashMap) agent.getQ());
                q = bout.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Parameter usedParams = agent.getParameters();
        runData.add(new FullRun(usedParams, accumulatedRewards, q));
    }

    private List<Double> runSingleIteration(S initialState, int iteration) {
        S curState = initialState;
        // run the simulation from curState until we reach a final state
        A curAction;

        List<Double> rewards = new ArrayList<>();
        if (iteration % loggingCount == 0 && LOGGER.isLoggable(Level.INFO))
            LOGGER.info(String.format("Running iteration %d...%n", iteration));

        do {
            // get next action using q learning
            curAction = agent.execute(curState, mdp, iteration);

            S nextState = null;
            // Do the action and set the new state
            if (curAction != null) {
                nextState = mdp.stateTransition(curState, curAction);
            }

            rewards.add(mdp.reward(curState, curAction, nextState));

            curState = nextState;

        } while (curAction != null);

        return rewards;
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
     * Resets the QLearningAgent to starting values.
     * The random generator is not reset!!!
     * This is due to this method being called for the RMS-Error. It would be meaningless to run the q learner
     * with the same values again, as it would result in the same output. So the random generator is not reset.
     */
    public void reset() {
        agent.reset();
    }

    public void saveData(String filename) {
        try {
            String json = ".json";

            String newFilename = filename;
            int counter = 0;
            while (new File(newFilename + json).exists()) {
                newFilename = filename + counter;
                counter++;
            }

            Gson gson = new Gson();
            counter = 0;
            for (FullRun run : runData) {
                byte[] q = run.getQ();
                if (q != null) {
                    Writer writer = new FileWriter(newFilename + "QData" + counter + json);
                    gson.toJson(q, writer);
                    writer.flush();
                    writer.close();
                    run.removeQ(counter);
                    counter++;
                }
            }

            Writer writer = new FileWriter(newFilename + json);
            gson.toJson(runData, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData(String filename) {
        byte[] qFileData = null;
        try {
            qFileData = new Gson().fromJson(new FileReader(filename), new TypeToken<byte[]>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (qFileData == null) {
            LOGGER.severe(String.format("Could not load Q File with filename %s.", filename));
        }

        Map<Pair<S, A>, Double> q = null;
        try (ByteArrayInputStream streamIn = new ByteArrayInputStream(qFileData); ObjectInputStream objectinputstream = new ObjectInputStream(streamIn)) {
            q = (HashMap<Pair<S, A>, Double>) objectinputstream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (q != null) {
            agent.setQ(q);
        } else {
            LOGGER.severe(String.format("Could not load latest Q with filename %s.", filename));
        }
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
