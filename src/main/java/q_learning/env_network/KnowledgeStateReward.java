package q_learning.env_network;

import core.AdversaryAction;
import core.NodeAction;
import core.State;
import environment.NetworkNode;
import knowledge.NodeKnowledge;
import q_learning.QLearnerNetwork;
import q_learning.interfaces.StateReward;
import run.Simulation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This class implements the reward function.
 */
public class KnowledgeStateReward implements StateReward<State, NodeAction> {

    private final State state;
    private final Set<NodeAction> actionsIntoFailedState;
    private final Set<NodeAction> actionsIntoZerodayUsed;


    public KnowledgeStateReward(State state, Set<NodeAction> actionsIntoFailedState, Set<NodeAction> actionsIntoZerodayUsed) {
        this.state = state;
        this.actionsIntoFailedState = actionsIntoFailedState;
        this.actionsIntoZerodayUsed = actionsIntoZerodayUsed;
    }

    /**
     * The reward function.
     *
     * @param action An action on the state from this class
     * @param targetState The target state reached by doing the action on the state
     * @return The reward for the state with given action and target state
     */
    @Override
    public double reward(NodeAction action, State targetState) {
        double actionCost = 0.0;
        double finalStateBonus = 5.0;
        double zeroDayPenality = 3.0;
        double failedStatePenality = 5.0;
        double stateValue = 0.0;
        double lowCost = 0.1;
        double mediumCost = 0.2;
        double highCost = 0.3;

        if (targetState == null || action == null){
            if (state.isFinalState()) {
                stateValue += finalStateBonus;
            } else if (QLearnerNetwork.FAILED_STATE_ENABLED && actionsIntoFailedState.contains(action)) {
                stateValue -= failedStatePenality;
            }
            return stateValue;
        }

        switch (action.getAction()) {
            case ACTIVE_SCAN_VULNERABILITY:
            case SOFTWARE_DISCOVERY:
            case ACTIVE_SCAN_IP_PORT:
            case VALID_ACCOUNTS_CRED:
            case DATA_FROM_LOCAL_SYSTEM:
                actionCost = lowCost;
                break;
            case EXPLOIT_FOR_PRIVILEGE_ESCALATION:
            case EXPLOIT_FOR_CLIENT_EXECUTION:
            case EXPLOIT_PUBLIC_FACING_APPLICATION:
            case VALID_ACCOUNTS_VULN:
                actionCost = highCost;
                break;
            case MAN_IN_THE_MIDDLE:
                actionCost = mediumCost;
                break;
        }

        if (actionsIntoZerodayUsed.contains(action)) {
            actionCost += zeroDayPenality;
        }

        Map<NetworkNode.TYPE, NodeKnowledge> map = state.getNodeKnowledgeMap();
        //experimental, try to motivate to do early IP scanning
        Predicate<NetworkNode.TYPE> canScan = n -> state.getNodesWithAnyNodeAccess().contains(n);
        Set<NetworkNode.TYPE> potentialScanningTargets = new HashSet<>(map.keySet());
        potentialScanningTargets.remove(NetworkNode.TYPE.ADVERSARY);
        for (NetworkNode.TYPE node : potentialScanningTargets) {
            if (!map.get(node).hasPrivIp() && action.getAction().equals(AdversaryAction.ACTIVE_SCAN_IP_PORT)
                    && action.getTarget().equals(node)
                    && Simulation.getSimWorld().getInternalNodes().stream().anyMatch(canScan)) {
                stateValue += 0.25;
                //limit to single bonus
                break;
            }
        }

        Set<NetworkNode.TYPE> vulnscanTargets = new HashSet<>(potentialScanningTargets);
        vulnscanTargets.removeAll(state.getNodesWithAnyNodeAccess());
        for (NetworkNode.TYPE node: vulnscanTargets){
            if (!map.get(node).hasOperatingSystem()
                    && action.getTarget().equals(node)
                    && action.getAction().equals(AdversaryAction.ACTIVE_SCAN_VULNERABILITY)){
                stateValue += 0.15;
                //limit to single bonus
                break;
            }
        }

        //after initial access, using ADVERSARY node should be discouraged since everything can be done in internal network
        Predicate<NetworkNode.TYPE> hasRoot = n -> state.getNodeKnowledgeMap().containsKey(n) && state.getNodeKnowledgeMap().get(n).hasAccessLevelRoot();
        if(Simulation.getSimWorld().getInternalNodes().stream().anyMatch(hasRoot) && action.getCurrentActor().equals(NetworkNode.TYPE.ADVERSARY)){
            actionCost += 0.5;
        }

        for (NetworkNode.TYPE node : potentialScanningTargets) {
            if (!map.get(node).hasAccessLevelRoot() && targetState.getNodeKnowledgeMap().get(node).hasAccessLevelRoot()) {
                stateValue += 0.5;
            }
        }


        return stateValue - actionCost;
    }

    @Override
    public State state() {
        return state;
    }
}

