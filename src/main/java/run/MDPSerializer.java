package run;

import aima.core.probability.mdp.ActionsFunction;
import core.AdversaryAction;
import core.NodeAction;
import core.State;
import environment.NetworkNode;
import q_learning.env_network.KnowledgeStateReward;
import q_learning.interfaces.QActionsFunctionInterface;
import q_learning.interfaces.StateReward;
import q_learning.mdp.MDP;
import q_learning.mdp.QActionsFunction;
import q_learning.mdp.QStateTransition;

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

    //set these values to include a honeypot
    private static final Set<NetworkNode.TYPE> actorsFailedTransition = Set.of(NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.ADVERSARY, NetworkNode.TYPE.DATABASE);
    private static final NetworkNode.TYPE targetFailedTransition = NetworkNode.TYPE.ADMINPC;
    private static final AdversaryAction failedAction = AdversaryAction.VALID_ACCOUNTS_CRED;

    private static final NetworkNode.TYPE zerodayTarget = NetworkNode.TYPE.ADMINPC;
    private static final AdversaryAction zerodayAction = AdversaryAction.VALID_ACCOUNTS_VULN;

    public static void computeMDP(Set<State> stateSet){
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Generating states...");
        HashMap<State, StateReward<State, NodeAction>> states = generateStates(stateSet);

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
            LOGGER.info("Generating MDP...");
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
    private static HashMap<State, StateReward<State, NodeAction>> generateStates(Set<State> stateSet){
        HashMap<State, StateReward<State, NodeAction>> states = new HashMap<>();

        for(State s : stateSet){
            states.put(s, new KnowledgeStateReward(s, getFailedNodeActions(), getZerodayTransitions()));
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
            if(state.isFinalState()){
                finalStates.add(state);
            }
        }
        if (FAILED_STATE_ENABLED){
            finalStates.addAll(getFailedStates(states, actions));
        }
        return finalStates;
    }
}
