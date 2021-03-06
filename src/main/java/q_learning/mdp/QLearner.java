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

    // the markov decision process to use
    private final MDP<S, A> mdp;

    // the Q-Learning agent
    private final QLearningAgent<S, A> agent;

    // A random generator and the parameters to use
    private Random random;
    private Parameter parameter;

    // The interval at which messages will be logged to the console
    private final int loggingCount;

    // The data to print to file
    private final List<FullRun<S,A>> runData = new ArrayList<>();

    /**
     * This constructor initializes a QLearningAgent given the MDP it should base on and a number of parameters
     * used for the learning process.
     *
     * @param mdp
     *      The MDP that should be used
     * @param parameter
     *      The parameter used for learning
     * @param loggingCount
     *      Every loggingCount iteration the logger will print the current iteration to the console.
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
        List<Pair<S, A>> path = null;
        double reward = 0.0;
        try {
            path = getPreferredPath(0);
            for (Pair<S, A> pair : path) {
                if (pair.getB() != null)
                    reward += mdp.reward(pair.getA(), pair.getB(), mdp.stateTransition(pair.getA(), pair.getB()));
                else
                    reward += mdp.reward(pair.getA(), null, null);
            }
        } catch (Exception e) {
            LOGGER.warning("could not get preferred path..");
        }
        runData.add(new FullRun<>(usedParams, accumulatedRewards, path, reward, q));
    }

    /**
     * This method runs a single iteration on the given state. The iteration denotes the current iteration. It is
     * used for logging and is also passed to the Q-Learning-Agent for the calculation of epsilon.
     *
     * @param initialState The initial state of the sequence
     * @param iteration The current iteration
     * @return The rewards gained for each action in the sequence
     */
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
     * Saves the run data to a json file with the given file name. In case Q should be saved, it is saved to a
     * second file with the same filename + QData + number. In the original save file, the Q will be exchanged with
     * the number added at the end of the QData file, so it is clear which QData belongs to which run.
     * If the given filename already exists, a number will be added, so that old data is not overridden.
     *
     * @param filename The filename/path where to save the file to (.json is added automatically)
     */
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
            for (FullRun<S,A> run : runData) {
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

    /**
     * Loads data from the given filename (must include the .json).
     *
     * WARNING: This method needs a lot of memory as it is not written well. Expect to need more than 8 GB of ram to
     * run this.
     *
     * @param filename The filename (including .json)
     */
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
