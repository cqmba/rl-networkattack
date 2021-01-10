package q_learning.interfaces;

import aima.core.agent.Action;
import aima.core.probability.mdp.ActionsFunction;
import aima.core.probability.mdp.MarkovDecisionProcess;

/**
 * This class is an extension of the MarkovDecisionProcess interface. It adds methods for getting the reward
 * depending on state, action and target state as well as a method to do transitions.
 * @param <S> The state class
 * @param <A> The action class
 */
public interface QMarkovDecisionProcess<S, A extends Action> extends MarkovDecisionProcess<S, A> {
    /**
     * This method should return the reward of a state given the next action taken and the target state.
     *
     * NOTE: The reward(state) method provided by the MarkovDecisionProcess interface should throw an
     * UnsupportedOperationException and is not used.
     * @param state The origin state
     * @param action The action taken
     * @param targetState the target state
     * @return The reward associated with the states and action
     */
    double reward(S state, A action, S targetState);

    /**
     * Does a transition from a state with the given action and returns the target state.
     *
     * @param state The origin state
     * @param action The action taken
     * @return The target state reached by doing the given action on the given state
     */
    S stateTransition(S state, A action);

    /**
     * Returns the ActionsFunction, providing information which actions can be taken at which state.
     *
     * @return The ActionsFunction
     */
    ActionsFunction<S, A> getActionsFunction();

    /**
     * Returns rather or not the given state is a final state or not
     *
     * @param state The state to be checked
     * @return Is the given state a final state of the MDP?
     */
    boolean isFinalState(S state);
}
