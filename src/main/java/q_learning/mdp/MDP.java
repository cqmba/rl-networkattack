package q_learning.mdp;

import aima.core.agent.Action;
import aima.core.probability.mdp.ActionsFunction;
import q_learning.interfaces.QActionsFunctionInterface;
import q_learning.interfaces.QMarkovDecisionProcess;
import q_learning.interfaces.StateReward;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple implementation of a Markov Decision Process
 */
public class MDP<S extends Serializable, A extends Action & Serializable> implements QMarkovDecisionProcess<S, A>, Serializable {
    // Save state and reward
    private final HashMap<S, StateReward<S, A>> states;

    private final S initialState;

    // Each state with all possible actions
    private final QActionsFunctionInterface<S, A> actions;

    // Every transition of a state and action
    private final QStateTransition<S, A> transitions;

    private final HashSet<S> finalStates;

    /**
     * The constructor for Markov decision process.
     *
     * @param states All states of the MDP together with a StateReward providing information about the reward for each
     *               state
     * @param initialState The initial state
     * @param actions An ActionsFunction providing all actions for each state
     * @param transitions A StateTransition providing all transitions from state to state with an action
     * @param finalStates All final states
     */
    public MDP(HashMap<S, StateReward<S, A>> states, S initialState,
               QActionsFunctionInterface<S, A> actions, QStateTransition<S, A> transitions,
               HashSet<S> finalStates) {
        this.states = states;
        this.initialState = initialState;
        this.actions = actions;
        this.transitions = transitions;
        this.finalStates = finalStates;
    }

    /**
     * This function returns an ActionsFunction, which is needed for Q-Learning Agent to work.
     *
     * @return the actionsfunction
     */
    @Override
    public ActionsFunction<S, A> getActionsFunction() {
        return actions;
    }

    /**
     * Does a transition from an origin state with an action to a target state.
     *
     * @param state The origin state
     * @param action The action taken
     * @return The target state reached by doing the given action on the given state
     */
    @Override
    public S stateTransition(S state, A action) {
        return transitions.stateTransition(state, action);
    }

    /**
     * Returns all states.
     */
    @Override
    public Set<S> states() {
        return states.keySet();
    }

    /**
     * Returns the initial state
     */
    @Override
    public S getInitialState() {
        return initialState;
    }

    /**
     * Returns all actions for a given state
     */
    @Override
    public Set<A> actions(S state) {
        return actions.actions(state);
    }

    /**
     * Returns a laplacian probability for the given state.
     * @param sDelta The target state
     * @param state The starting state
     * @param action The action leading from starting state to target state
     * @return The probability of the transition
     */
    @Override
    public double transitionProbability(S sDelta, S state, A action) {
        return 1.0 / (double) actions.actions(state).size();
    }

    /**
     * This method is unsupported and not used by the QLearningAgent. Instead use the reward(state, action, targetState)
     * method.
     *
     * @param state The state
     * @return An UnsupportedOperationException
     */
    @Override
    public double reward(S state) {
        throw new UnsupportedOperationException("State, action and target action are required. Use reward(state, action, targetState) instead.");
    }

    /**
     * Returns the reward of the given state, action and target state, where the target state should be reached
     * by doing the action on the origin state.
     *
     * @param state The origin state
     * @param action The action taken
     * @param targetState the target state
     * @return The reward of the state by doing the given action and reaching the target state
     */
    @Override
    public double reward(S state, A action, S targetState) {
        return states.get(state).reward(action, targetState);
    }

    /**
     * Checks whether the given state is a final state
     * @param state The state to be checked
     * @return true if the state is a final state
     */
    @Override
    public boolean isFinalState(S state) {
        return finalStates.contains(state);
    }
}
