package q_learning.mdp;

import aima.core.agent.Action;
import aima.core.probability.mdp.ActionsFunction;

import java.util.*;

/**
 * A simple implementation of ActionsFunction.
 * Actions can be added after initialization.
 */
public class QActionsFunction<S, A extends Action> implements ActionsFunction<S, A> {
    private final Map<S, Set<A>> actions;

    /**
     * This constructor needs all states of the MDP. Afterwards the actions for each state can be added.
     *
     * @param states All states of the MDP
     */
    public QActionsFunction(Map<S, Double> states) {
        this.actions = new HashMap<>();
        for (S state : states.keySet()) {
            actions.put(state, new HashSet<>());
        }
    }

    /**
     * Adds the given action to the given state
     *
     * @param state The state an action should be added to
     * @param action The action doable on the given state
     */
    public void addAction(S state, A action) {
        actions.get(state).add(action);
    }

    /**
     * Returns all actions for the given state
     *
     * @param state A state
     * @return All actions doable on the given state
     */
    @Override
    public Set<A> actions(S state) {
        return actions.get(state);
    }
}
