package q_learning;

import aima.core.probability.mdp.ActionsFunction;
import core.AdversaryAction;
import core.NodeAction;
import core.State;
import environment.NetworkNode;
import q_learning.interfaces.QActionsFunctionInterface;
import q_learning.interfaces.StateReward;
import q_learning.mdp.MDP;
import q_learning.mdp.QActionsFunction;
import q_learning.mdp.QStateTransition;
import run.Simulation;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MDPSerializer {
    private static final Logger LOGGER = Logger.getLogger(MDPSerializer.class.getName());

    private static final String FILENAME = "mdp.ser";
    public static final boolean FAILED_STATE_ENABLED = false;
    private static final boolean DISALLOW_SELF_TRANSITIONS = true;

    //set these values to include a honeypot
    private static final Set<NetworkNode.TYPE> actorsFailedTransition = Set.of(NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.ADVERSARY, NetworkNode.TYPE.DATABASE);
    private static final NetworkNode.TYPE targetFailedTransition = NetworkNode.TYPE.ADMINPC;
    private static final AdversaryAction failedAction = AdversaryAction.VALID_ACCOUNTS_CRED;

    private static final NetworkNode.TYPE zerodayTarget = NetworkNode.TYPE.ADMINPC;
    private static final AdversaryAction zerodayAction = AdversaryAction.VALID_ACCOUNTS_VULN;

    public static void main(String[] args) throws IOException {
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
        QActionsFunctionInterface<State, NodeAction> actions = generateActions(states);

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Generating Transitions...");
        QStateTransition<State, NodeAction> transitions = generateTransitions(states, actions);

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Generating final states...");
        HashSet<State> finalStates = getFinalStates(states, actions);

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Creating Q Learning agent...");
        MDP<State, NodeAction> mdp = new MDP<>(states, State.getStartState(), actions, transitions, finalStates);

        try (FileOutputStream fout = new FileOutputStream(FILENAME); ObjectOutputStream oos = new ObjectOutputStream(fout)) {
            oos.writeObject(mdp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Initializes the states
     * @return The states of the MDP
     */
    private static HashMap<State, StateReward<State, NodeAction>> generateStates() throws IOException {
        Set<State> stateSet = null;
        try (FileInputStream streamIn = new FileInputStream("states.ser"); ObjectInputStream objectinputstream = new ObjectInputStream(streamIn)){
            stateSet = (Set<State>) objectinputstream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
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
    private static QActionsFunctionInterface<State, NodeAction> generateActions(Map<State, StateReward<State, NodeAction>> states) {
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
