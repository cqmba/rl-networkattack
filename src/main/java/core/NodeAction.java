package core;

import aima.core.agent.Action;
import com.google.gson.Gson;
import environment.NetworkNode;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class wraps the AdversaryAction as an object that is used for the learning (extended by target and
 * acting node), so that it can be seen as a transition from one state to another.
 */
public class NodeAction implements Action, Serializable {
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

    /**
     * This calculates all possible actions given a particular state
     * @param currentState - current state
     * @return - set of possible actions
     */
    static Set<NodeAction> getAllActionPossible(State currentState){
        Set<NodeAction> allPossibleActions = new HashSet<>();
        Set<AdversaryAction> actions = AdversaryAction.allActions();
        for(AdversaryAction action : actions){
            allPossibleActions.addAll(action.getActionsWhichFulfillPrecondition(currentState));
        }
        return allPossibleActions;
    }

    /**
     * This calculates all possible actions with a state change, given a particular state.
     * @param currentState - current state
     * @return - set of actions
     */
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
        return new Gson().toJson(this);
    }
}


