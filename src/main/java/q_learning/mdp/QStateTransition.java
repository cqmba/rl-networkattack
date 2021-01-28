package q_learning.mdp;

import aima.core.agent.Action;
import q_learning.Pair;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This class saves all transitions from state to state.
 *
 * @param <S> The state class
 * @param <A> The action class
 */
public class QStateTransition<S extends Serializable, A extends Action & Serializable> {
    // Map from State and Action to target state
    private final HashMap<Pair<S, A>, S> transitions;

    public QStateTransition() {
        this.transitions = new HashMap<>();
    }

    /**
     * Adds a transition from the given origin state, with the given action to the given target state.
     *
     * @param state The origin state
     * @param action The action
     * @param targetState The target state
     */
    public void addTransition(S state, A action, S targetState) {
        transitions.put(new Pair<>(state, action), targetState);
    }

    /**
     * Returns the state reached by doing the action on the given state. Throws an exception if the transition
     * was not set.
     *
     * @param state The origin state
     * @param action The action used
     * @return The target state reached by doing the action on the given state.
     */
    public S stateTransition(S state, A action) {
        if (!transitions.containsKey(new Pair<>(state, action)))
            throw new IllegalArgumentException("No state action pair set.");

        return transitions.get(new Pair<>(state, action));
    }
}
