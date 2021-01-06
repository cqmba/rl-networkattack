package q_learning;

import aima.core.agent.Action;

import java.util.HashMap;

public class QStateTransition<S, A extends Action> {
    // Map from State and Action to target state
    private HashMap<Pair<S, A>, S> transitions;

    public QStateTransition() {
        this.transitions = new HashMap<>();
    }

    public void addTransition(S state, A action, S targetState) {
        transitions.put(new Pair<>(state, action), targetState);
    }

    public S stateTransition(S state, A action) {
        if (!transitions.containsKey(new Pair<>(state, action)))
            throw new IllegalArgumentException("No state action pair set.");

        return transitions.get(new Pair<>(state, action));
    }
}
