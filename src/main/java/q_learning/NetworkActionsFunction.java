package q_learning;

import aima.core.agent.Action;
import aima.core.probability.mdp.ActionsFunction;

import java.util.*;

/**
 * A simple implementation of ActionsFunction.
 * Actions can be added after initialization.
 */
public class NetworkActionsFunction<S, A extends Action> implements ActionsFunction<S, A> {
    private final Map<S, Set<A>> actions;

    public NetworkActionsFunction(Map<S, Double> states) {
        this.actions = new HashMap<>();
        for (S state : states.keySet()) {
            actions.put(state, new HashSet<>());
        }
    }

    public void addAction(S state, A action) {
        actions.get(state).add(action);
    }

    @Override
    public Set<A> actions(S state) {
        return actions.get(state);
    }
}
