package q_learning;

import aima.core.probability.mdp.ActionsFunction;

import com.google.gson.Gson;
import core.AdversaryAction;
import core.NodeAction;
import core.State;
import environment.NetworkNode;
import q_learning.interfaces.StateReward;
import q_learning.mdp.*;
import run.Simulation;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final int ITERATIONS = 1000000;
    private static final int INITIAL_STATE_ITERATIONS = 0;

    //set these values to include a honeypot
    private static final Set<NetworkNode.TYPE> actorsFailedTransition = Set.of(NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.ADVERSARY, NetworkNode.TYPE.DATABASE);
    private static final NetworkNode.TYPE targetFailedTransition = NetworkNode.TYPE.ADMINPC;
    private static final AdversaryAction failedAction = AdversaryAction.VALID_ACCOUNTS_CRED;

    private static final NetworkNode.TYPE zerodayTarget = NetworkNode.TYPE.ADMINPC;
    private static final AdversaryAction zerodayAction = AdversaryAction.VALID_ACCOUNTS_VULN;


    public static void main(String[] args) {
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Setting up environment...");
        //use this boolean to toggle precondition filtering;
        // true = allow only actions as possible actions, which result in state change
        // false = allow transitions, that dont change the state
        Simulation.setupWorld(DISALLOW_SELF_TRANSITIONS);

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Generating states...");
        HashMap<State, StateReward<State, NodeAction>> states = null;
        try {
            states = generateStates();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Generating Actions...");
        ActionsFunction<State, NodeAction> actions = generateActions(states);

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Generating Transitions...");
        QStateTransition<State, NodeAction> transitions = generateTransitions(states, actions);

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Generating final states...");
        HashSet<State> finalStates = getFinalStates(states, actions);

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Creating Q Learning agent...");
        MDP<State, NodeAction> mdp = new MDP<>(states, State.getStartState(), actions, transitions, finalStates);

        //##############################################################################################
        //                                      RUN LEARNING
        //##############################################################################################
        List<Parameter> params = Arrays.asList(
                new Parameter(0.1, 1.0, 0.3, 0, ERROR, NE, R_PLUS, ITERATIONS,
                        INITIAL_STATE_ITERATIONS,
                        String.format("failedStateEnabled:%b,disallowSelfTransition:%b,states:36k,finalState:rootOnAll;accountOnAdminDatabase;DataRead;KnowNetwork", FAILED_STATE_ENABLED, DISALLOW_SELF_TRANSITIONS),
                        false),
                new Parameter(0.1, 1.0, 0.0, 0, ERROR, NE, R_PLUS, ITERATIONS,
                        INITIAL_STATE_ITERATIONS,
                        String.format("failedStateEnabled:%b,disallowSelfTransition:%b,states:36k,finalState:rootOnAll;accountOnAdminDatabase;DataRead;KnowNetwork", FAILED_STATE_ENABLED, DISALLOW_SELF_TRANSITIONS),
                        true)
        );
        runWithParameters(mdp, params, "runData", 10000, null);
    }

    /**
     * Runs the learner once for each given parameter setting.
     *
     * @param mdp The markov decision process
     * @param params The parameters. Each param is run in order and the results will be saved
     * @param filename The filename for the saved data
     * @param loggingCount Each loggingCount'th iteration will be logged to the console to show that the code is still running
     * @param loadFilename The filename of the Q to load from. Loading will be skipped, if set to null
     */
    private static void runWithParameters(MDP<State, NodeAction> mdp, List<Parameter> params, String filename,
                                          int loggingCount, String loadFilename) {

        // create learner. The parameters are changed at each run in the for loop.
        QLearner<State, NodeAction> learner = new QLearner<>(mdp, 0.5, 0.5, 0.5, 0.1,
                1, 0.0, 0, loggingCount);

        if (loadFilename != null)
            learner.loadData(loadFilename);

        for (Parameter par : params) {
            learner.setLearningRate(par.getLearningRate());
            learner.setDiscountFactor(par.getDiscountFactor());
            learner.setActionEpsilon(par.getEpsilon());
            learner.setSeed(par.getSeed());
            learner.setErrorEpsilon(par.getError());
            learner.setNe(par.getNe());
            learner.setRPlus(par.getrPlus());

            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Learning...");
            learner.runIterations(par.getIterations(), par.getInitialStateIterations(), par.getAdditionalInformation(),
                    par.getSaveQ());

            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Saving learning values...");
            learner.saveData(filename);

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
    }

    /**
     * Calculates the Root Mean Squared Error for each state and returns it.
     * @param runs Number of runs of the whole Q Learning to do
     * @param iterationsPerRun Number of iterations to run per run
     * @param initialIterationsPerRun Number of iterations on the initial state to run per run
     * @param expectedUtil A Map, which maps each state with an expected utility
     * @return The RMSE, the average error for each state
     */
    private Map<State, Double> RMSE(int runs, int iterationsPerRun, int initialIterationsPerRun,
                                    Map<State, Double> expectedUtil) {
        // This is a thing to show how to calculate the RMS Error.

        // First set up the environment as usual
        Simulation.setupWorld(DISALLOW_SELF_TRANSITIONS);
        HashMap<State, StateReward<State, NodeAction>> states = null;
        try {
            states = generateStates();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ActionsFunction<State, NodeAction> actions = generateActions(states);
        QStateTransition<State, NodeAction> transitions = generateTransitions(states, actions);
        HashSet<State> finalStates = getFinalStates(states, actions);
        MDP<State, NodeAction> mdp = new MDP<>(states, State.getStartState(), actions, transitions, finalStates);

        QLearner<State, NodeAction> learner = new QLearner<>(mdp, 0.1, 1.0, 0.0, ERROR, NE, R_PLUS, 0, 10000);

        // now calculate the rms error

        // run the learner and get the utilities
        List<Map<State, Double>> utilities = new ArrayList<>();
        for (int i = 0; i < runs; i++) {
            learner.runIterations(iterationsPerRun, initialIterationsPerRun, "", false);
            utilities.add(learner.getUtility());
            learner.reset();
        }

        // now calculate the rmse over all states and runs
        Map<State, Double> rmse = new HashMap<>();
        for (State curState : mdp.states()) {
            double meanSquared = 0;
            for (Map<State, Double> util : utilities) {
                Double utilOfState = util.get(curState);
                if (utilOfState == null)
                    throw new RuntimeException("Utility for state not found..");

                meanSquared += Math.pow(expectedUtil.get(curState) - utilOfState, 2);
            }
            rmse.put(curState, Math.sqrt(meanSquared / utilities.size()));
        }
        return rmse;
    }

    /**
     * Initializes the states
     * @return The states of the MDP
     */
    private static HashMap<State, StateReward<State, NodeAction>> generateStates() throws IOException {
        FileInputStream streamIn = null;
        ObjectInputStream objectinputstream = null;
        Set<State> stateSet = null;
        try {
            streamIn = new FileInputStream("states.ser");
            objectinputstream = new ObjectInputStream(streamIn);
            stateSet = (Set<State>) objectinputstream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (streamIn != null)
                streamIn.close();
            if(objectinputstream != null){
                objectinputstream .close();
            }
        }

        HashMap<State, StateReward<State, NodeAction>> states = new HashMap<>();

        for(State s : stateSet){
            double reward = 0.0;
            states.put(s, new KnowledgeStateReward(s, reward, getFailedNodeActions(), getZerodayTransitions()));
        }

        return states;
    }

    /**
     * Initializes the actions possible per state
     * @param states The states possible
     * @return An ActionFunction, which returns all possible actions per state
     */
    private static ActionsFunction<State, NodeAction> generateActions(Map<State, StateReward<State, NodeAction>> states) {
        QActionsFunction<State, NodeAction> actions = new QActionsFunction(states);
        for (State state : states.keySet()) {
            for (NodeAction nodeAction : NodeAction.getAllActionPossibleWithChangeState(state)) {
                actions.addAction(state, nodeAction);
            }
        }
        return actions;
    }

    private static QStateTransition<State, NodeAction> generateTransitions(Map<State,StateReward<State, NodeAction>> states, ActionsFunction<State, NodeAction> actions) {
            QStateTransition<State, NodeAction> transition = new QStateTransition<>();
            for (State state : states.keySet()) {
                for (NodeAction action : actions.actions(state)) {
                    transition.addTransition(state, action, NodeAction.performNodeAction(action,state));
                }
            }
            return transition;
        }

    private static Set<State> getFailedStates(Map<State,StateReward<State, NodeAction>> states, ActionsFunction<State, NodeAction> actions){
        Set<State> failedStates = new HashSet<>();
        Set<NodeAction> failedNodeActions = getFailedNodeActions();
        for (State state : states.keySet()) {
            for (NodeAction action : actions.actions(state)) {
                /*
                if (action.getAction().equals(failedAction) && action.getTarget().equals(targetFailedTransition)
                        && actorsFailedTransition.contains(action.getCurrentActor())){
                    //next State should be marked as failed
                    failedStates.add(NodeAction.performNodeAction(action, state));
                }

                 */
                if (failedNodeActions.contains(action)){
                    failedStates.add(NodeAction.performNodeAction(action, state));
                }
            }
        }
        return failedStates;
    }

    private static Set<NodeAction> getZerodayTransitions(){
        Set<NodeAction> zerodayTransitions = new HashSet<>();
        for (NetworkNode.TYPE actor : Set.of(NetworkNode.TYPE.ADVERSARY, NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.DATABASE)){
            zerodayTransitions.add(new NodeAction(zerodayTarget, actor, zerodayAction));
        }
        return zerodayTransitions;
    }

    private static Set<NodeAction> getFailedNodeActions(){
        Set<NodeAction> possibleFailedTransitions = new HashSet<>();
        for (NetworkNode.TYPE actor : actorsFailedTransition){
            possibleFailedTransitions.add(new NodeAction(targetFailedTransition, actor, failedAction));
        }
        return possibleFailedTransitions;
    }

    private static HashSet<State> getFinalStates(Map<State, StateReward<State, NodeAction>> states, ActionsFunction<State, NodeAction> actions){
        HashSet<State> finalStates = new HashSet<>();
        for (State state : states.keySet()) {
            if(state.isFinalState()){//||state.isFailedState()
                finalStates.add(state);
            }
        }
        if (FAILED_STATE_ENABLED){
            finalStates.addAll(getFailedStates(states, actions));
        }
        return finalStates;
    }

}
