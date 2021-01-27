package core;

import aima.core.agent.Action;
import environment.NetworkNode;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NodeAction implements Action {
    NetworkNode.TYPE target;
    NetworkNode.TYPE currentActor;
    AdversaryAction action;

    public NodeAction (NetworkNode.TYPE target, NetworkNode.TYPE currentActor, AdversaryAction action){
        this.target = target;
        this.currentActor = currentActor;
        this.action = action;
    }

    @Override public int hashCode() {
        return Objects.hash(target, currentActor, action);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NodeAction) {
            NodeAction other = (NodeAction) o;
            return this.target.equals(other.target) && this.currentActor.equals(other.currentActor) && this.action.equals(other.action);
        }
        return false;
    }

    static Set<NodeAction> getAllActionPossible(State currentState){
        Set<NodeAction> allPossibleActions = new HashSet<>();
        Set<AdversaryAction> actions = AdversaryAction.allActions();
        for(AdversaryAction action : actions){
            allPossibleActions.addAll(action.getActionsWhichFulfillPrecondition(currentState));
        }
        return allPossibleActions;
    }

    public static Set<NodeAction> getAllActionPossibleWithChangeState(State currentState){
        Set<NodeAction> allPossibleActionsWithChangeState = new HashSet<>();
        Set<NodeAction> allPossibleActions = getAllActionPossible(currentState);
        for(NodeAction nodeAction : allPossibleActions){
            State newState = nodeAction.action.executePostConditionOnTarget(nodeAction.target ,currentState, nodeAction.currentActor);
            if(!newState.equals(currentState)){
                allPossibleActionsWithChangeState.add(nodeAction);
            }
        }
        return allPossibleActionsWithChangeState;
    }

    public static State performNodeAction(NodeAction action,State state){
        return action.action.executePostConditionOnTarget(action.target,state,action.currentActor);
    }

    public AdversaryAction getAction() {
        return action;
    }

    public NetworkNode.TYPE getTarget() {
        return target;
    }

    public NetworkNode.TYPE getCurrentActor() {
        return currentActor;
    }

    @Override
    public boolean isNoOp() {
        return false;
    }

    @Override
    public String toString() {
        return "NodeAction{" +
                "target=" + target +
                ", currentActor=" + currentActor +
                ", action=" + action +
                '}';
    }
}


