package q_learning;

import core.NodeAction;
import core.State;
import environment.NetworkNode;
import q_learning.interfaces.StateReward;

import java.util.Set;

public class KnowledgeStateReward implements StateReward<State, NodeAction> {

    private final State state;
    private final double reward;
    private Set<NodeAction> actionsIntoFailedState;


    public KnowledgeStateReward(State state, double reward, Set<NodeAction> actionsIntoFailedState) {
        this.state = state;
        this.reward = reward;
        this.actionsIntoFailedState = actionsIntoFailedState;
    }

    @Override
    public double reward(NodeAction action, State targetState) {
            if(targetState==null||action==null)
                return -0.1;
            double actionCost =0.1;
            double finalStateBonus = 2.0;
            double zeroDayPenality = 1.0;
            double failedStatePenality = 4.0;

            switch (action.getAction()){
                case ACTIVE_SCAN_VULNERABILITY :
                    actionCost =0.1;
                case ACTIVE_SCAN_IP_PORT :
                    actionCost =0.1;
                case EXPLOIT_FOR_PRIVILEGE_ESCALATION:
                    actionCost =0.3;
                case EXPLOIT_FOR_CLIENT_EXECUTION:
                    actionCost =0.3;
                case CREATE_ACCOUNT:
                    actionCost =0.1;
                case VALID_ACCOUNTS_CRED:
                    actionCost =0.1;
                case EXPLOIT_PUBLIC_FACING_APPLICATION:
                    actionCost =0.3;
                case SOFTWARE_DISCOVERY:
                    actionCost =0.1;
                case DATA_FROM_LOCAL_SYSTEM:
                    actionCost =0.1;
                case MAN_IN_THE_MIDDLE:
                    actionCost =0.2;
                case VALID_ACCOUNTS_VULN:
                    actionCost =0.3;
            }


            double stateValue =0;
            double targetStateValue=0;

            if(!state.isZerodayUsed()||targetState.isZerodayUsed()){
                actionCost+= zeroDayPenality;
            }

            if(state.isFinalState()){
                targetStateValue+= finalStateBonus;
            }
            /*
            if(state.isFailedState()){
                targetStateValue-= failedStatePenality;
            }
             */
            if (actionsIntoFailedState.contains(action)){
                targetStateValue-= failedStatePenality;
            }


        for(NetworkNode.TYPE node :state.getNodeKnowledgeMap().keySet()){
                if(state.getNodeKnowledgeMap().get(node).hasAccessLevelRoot()){
                    stateValue +=1.0;
                }
            }
            for(NetworkNode.TYPE node :targetState.getNodeKnowledgeMap().keySet()){
                if(targetState.getNodeKnowledgeMap().get(node).hasAccessLevelRoot()){
                    stateValue +=1.0;
                }
            }
            double reward = targetStateValue - stateValue -actionCost;
            return reward;
        }

    @Override
    public State state() {
            return state;
        }
}

