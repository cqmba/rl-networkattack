package q_learning;

import aima.core.agent.Action;
import aima.core.probability.mdp.ActionsFunction;
import q_learning.interfaces.QMarkovDecisionProcess;
import q_learning.interfaces.StateReward;

import java.util.Map;
import java.util.Set;

/**
 * A simple implementation of a Markov Decision Process
 */
public class MDP<S, A extends Action> implements QMarkovDecisionProcess<S, A> {
    // Save state and reward
    private final Map<S, StateReward<S, A>> states;

    private final S initialState;

    // Each state with all possible actions
    private final ActionsFunction<S, A> actions;

    // Every transition of a given state and action to a state
    private final QStateTransition<S, A> transitions;

    private final Set<S> finalStates;

    public MDP(Map<S, StateReward<S, A>> states, S initialState,
               ActionsFunction<S, A> actions, QStateTransition<S, A> transitions,
               Set<S> finalStates) {
        this.states = states;
        this.initialState = initialState;
        this.actions = actions;
        this.transitions = transitions;
        this.finalStates = finalStates;
    }

    /**
     * This function returns an ActionsFunction, which is needed for Q-Learning Agent to work.
     * @return the actionsfunction
     */
    @Override
    public ActionsFunction<S, A> getActionsFunction() {
        return actions;
    }

    @Override
    public S stateTransition(S state, A action) {
        return transitions.stateTransition(state, action);
    }

    @Override
    public Set<S> states() {
        return states.keySet();
    }

    @Override
    public S getInitialState() {
        return initialState;
    }

    @Override
    public Set<A> actions(S state) {
        return actions.actions(state);
    }

    @Override
    public double transitionProbability(S sDelta, S state, A action) {
        return 1.0 / (double) actions.actions(state).size();
    }

    @Override
    public double reward(S state) {
        throw new UnsupportedOperationException("State, action and target action are required. Use reward(state, action, targetState) instead.");
    }

    @Override
    public double reward(S state, A action, S targetState) {
        return states.get(state).reward(action, targetState);
    }

    @Override
    public boolean isFinalState(S state) {
        return finalStates.contains(state);
    }
}
