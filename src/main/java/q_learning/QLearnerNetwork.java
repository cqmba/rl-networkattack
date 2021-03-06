package q_learning;

import core.NodeAction;
import core.State;
import environment.NetworkNode;
import q_learning.mdp.*;
import q_learning.utils.Pair;
import q_learning.utils.Parameter;
import run.Simulation;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the main class to run for QLearning
 */
public class QLearnerNetwork {
    private static final Logger LOGGER = Logger.getLogger(QLearnerNetwork.class.getName());

    /*
     * To which extend two doubles may differ and be considered identical
     */
    private static final double ERROR = 0.000000001;

    /*
     * Number of times each state is at least visited
     */
    private static final int NE = 5;

    // Should be the highest (or higher than that) reward possible
    private static final double R_PLUS = 20.0;

    public static final boolean FAILED_STATE_ENABLED = false;
    private static final boolean DISALLOW_SELF_TRANSITIONS = true;

    public static void main(String[] args) {
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Setting up environment...");
        // use this boolean to toggle precondition filtering;
        // true = allow only actions as possible actions, which result in state change
        // false = allow transitions, that dont change the state
        Simulation.setupWorld(DISALLOW_SELF_TRANSITIONS);


        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Loading MDP...");
        MDP<State, NodeAction> mdp = null;
        try (FileInputStream streamIn = new FileInputStream("mdp.ser"); ObjectInputStream objectinputstream = new ObjectInputStream(streamIn)) {
            mdp = (MDP<State, NodeAction>) objectinputstream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //##############################################################################################
        //                                      RUN LEARNING
        //##############################################################################################

        List<Parameter> params = Arrays.asList(
                // Runs with static learning rate of 0.1, decreasing epsilon from 0.3 to 0.0 (linear decreasing)
                // and discount factor 1.0
                // Note that the learningRateMaxCount has to be at least 1. The value does not make a difference though
                // if the learningRateStartValue and EndValue are the same.
                new Parameter(10, 0.4, 0.05, 1.0,
                        0.25, 0.0, 4.0,
                        1.0, 50, ERROR, NE,
                        R_PLUS, 0, 500000,
                        "",
                        false)

                // Runs with deceasing learning rate (from 0.1 to 0.01) as the states are explored.
                // 0.01 is reached when a state is explored 20 times and it will be 0.1 when it is explored
                // 0 times. The decrease is linear.
                // If you want a different decrease, change the 1.0 in the learningRateSlope.
                // a value smaller 1 will result in little loss at the start and heavier loss at the end
                //      (looks a little like a root function mirrored on the y-axis)
                // a value greater 1 will result in heavy loss early on and little loss later (looks a little like 1/x)
                // The same principle applies to epsilon.
                // If you do not want a decrease or raise of the values, set the Start and End values to the same number.
        );
        runWithParameters(mdp, params, "runData", 10000, null);
    }

    /**
     * Runs the learner once for each given parameter setting.
     * NOTE: The seed is reset after each run, so you should change it for each run.
     *
     * @param mdp The markov decision process
     * @param params The parameters. Each param is run in order and the results will be saved
     * @param filename The filename for the saved data
     * @param loggingCount Each loggingCount iteration will be logged to the console to show that the code is still running
     * @param loadFilename The filename of the Q to load from. Loading will be skipped, if set to null
     */
    private static void runWithParameters(MDP<State, NodeAction> mdp, List<Parameter> params, String filename,
                                          int loggingCount, String loadFilename) {
        // create learner. The parameters are changed at each run in the for loop.
        Parameter dummyParam = new Parameter(1, 0.1, 0.1, 1.0,
                0.1, 0.1, 1.0, 1.0, 0, ERROR, 1, 0.0,
                0, 0, "", false);
        QLearner<State, NodeAction> learner = new QLearner<>(mdp, dummyParam, loggingCount);

        if (loadFilename != null)
            learner.loadData(loadFilename);

        for (Parameter par : params) {
            learner.setParameter(par);

            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Learning...");
            learner.runIterations();

            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Printing best path from initial state...");
            try {
                List<Pair<State, NodeAction>> path = learner.getPreferredPath(0);
                NetworkNode.TYPE previousActor = null;
                for (Pair<State, NodeAction> pair : path) {
                    NodeAction nodeAction = pair.getB();
                    if (nodeAction == null) {
                        break;
                    }
                    if (!nodeAction.getCurrentActor().equals(previousActor)) {
                        previousActor = nodeAction.getCurrentActor();
                        LOGGER.info("\tActive Host: " + previousActor + " \tTarget: " + nodeAction.getTarget() + " \tAction: " + nodeAction.getAction());
                    } else {
                        LOGGER.info("\t\t\tTarget: " + nodeAction.getTarget() + " \tAction: " + nodeAction.getAction());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Saving learning values...");
        learner.saveData(filename);
    }
}
