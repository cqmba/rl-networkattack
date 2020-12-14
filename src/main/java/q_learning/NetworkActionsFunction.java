package q_learning;

import aima.core.probability.mdp.ActionsFunction;

import java.util.*;

/**
 * A simple implementation of ActionsFunction.
 * Actions can be added after initialization.
 */
public class NetworkActionsFunction implements ActionsFunction<NetworkState, NetworkAction> {
    private final Map<NetworkState, Set<NetworkAction>> actions;

    public NetworkActionsFunction(Map<NetworkState, Double> states) {
        this.actions = new HashMap<>();
        for (NetworkState state : states.keySet()) {
            actions.put(state, new HashSet<>());
        }
    }

    public void addAction(NetworkState state, NetworkAction action) {
        actions.get(state).add(action);
    }

    @Override
    public Set<NetworkAction> actions(NetworkState networkState) {
        return actions.get(networkState);
    }
}
