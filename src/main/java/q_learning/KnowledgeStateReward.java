package q_learning;

import core.NodeAction;
import core.State;
import environment.NetworkNode;
import knowledge.NodeKnowledge;
import q_learning.interfaces.StateReward;

public class KnowledgeStateReward implements StateReward<State, NodeAction> {

    private final State state;
    private final double reward;

    public KnowledgeStateReward(State state, double reward) {
        this.state = state;
        this.reward = reward;
        }

    @Override
    public double reward(NodeAction action, State targetState) {
            double reward =0;
            double stateValue =0;
            double targetStateValue=0;
            double actionCost =0.1;
            if(targetState==null)
                return -0.1;
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
            reward = targetStateValue - stateValue -actionCost;
            return reward;
        }

    @Override
    public State state() {
            return state;
        }


}

