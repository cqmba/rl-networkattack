package q_learning;

import core.AdversaryAction;
import core.NodeAction;
import core.State;
import environment.NetworkNode;
import environment.NetworkWorld;
import knowledge.NodeKnowledge;
import q_learning.interfaces.StateReward;
import run.Simulation;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class KnowledgeStateReward implements StateReward<State, NodeAction> {

    private final State state;
    private final double reward;
    private final Set<NodeAction> actionsIntoFailedState;
    private final Set<NodeAction> actionsIntoZerodayUsed;


    public KnowledgeStateReward(State state, double reward, Set<NodeAction> actionsIntoFailedState, Set<NodeAction> actionsIntoZerodayUsed) {
        this.state = state;
        this.reward = reward;
        this.actionsIntoFailedState = actionsIntoFailedState;
        this.actionsIntoZerodayUsed = actionsIntoZerodayUsed;
    }

    @Override
    public double reward(NodeAction action, State targetState) {
        double actionCost = 0.0;
        double finalStateBonus = 5.0;
        double zeroDayPenality = 3.0;
        double failedStatePenality = 5.0;
        double stateValue = 0.0;

        if (targetState == null || action == null){
            if (state.isFinalState()) {
                stateValue += finalStateBonus;
            } else if (QLearnerNetwork.failedStateEnabled && actionsIntoFailedState.contains(action)) {
                stateValue -= failedStatePenality;
            }
            return stateValue;
        }

        switch (action.getAction()) {
            case ACTIVE_SCAN_VULNERABILITY:
                actionCost = 0.1;
                break;
            case ACTIVE_SCAN_IP_PORT:
                actionCost = 0.1;
                break;
            case EXPLOIT_FOR_PRIVILEGE_ESCALATION:
                actionCost = 0.3;
                break;
            case EXPLOIT_FOR_CLIENT_EXECUTION:
                actionCost = 0.3;
                break;
            case VALID_ACCOUNTS_CRED:
                actionCost = 0.1;
                break;
            case EXPLOIT_PUBLIC_FACING_APPLICATION:
                actionCost = 0.3;
                break;
            case SOFTWARE_DISCOVERY:
                actionCost = 0.1;
                break;
            case DATA_FROM_LOCAL_SYSTEM:
                actionCost = 0.1;
                break;
            case MAN_IN_THE_MIDDLE:
                actionCost = 0.2;
                break;
            case VALID_ACCOUNTS_VULN:
                actionCost = 0.3;
                break;
        }

        if (actionsIntoZerodayUsed.contains(action)) {
            actionCost += zeroDayPenality;
        }

        Map<NetworkNode.TYPE, NodeKnowledge> map = state.getNodeKnowledgeMap();
        //experimental, try to motivate to do early IP scanning
        Predicate<NetworkNode.TYPE> canScan = n -> state.getNodesWithAnyNodeAccess().contains(n);
        Set<NetworkNode.TYPE> potentialScanningTargets = map.keySet();
        potentialScanningTargets.remove(NetworkNode.TYPE.ADVERSARY);
        for (NetworkNode.TYPE node : potentialScanningTargets) {
            if (!map.get(node).hasPrivIp() && action.getAction().equals(AdversaryAction.ACTIVE_SCAN_IP_PORT)
                    && Simulation.getSimWorld().getInternalNodes().stream().anyMatch(canScan)) {
                stateValue += 0.2;
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

