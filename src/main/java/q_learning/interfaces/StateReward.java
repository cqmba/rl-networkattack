package q_learning.interfaces;

import aima.core.agent.Action;

import java.io.Serializable;

/**
 * This interface provides methods to provide the reward of a single state. For this, a reward/utility function
 * has to be implemented. Since this interface needs a single state as foundation, it enables for different reward
 * functions for each state.
 *
 * @param <S> The state class
 * @param <A> The action class
 */
public interface StateReward<S extends Serializable, A extends Action & Serializable> extends Serializable {
    /**
     * Should return the state for which a reward is implemented.
     *
     * @return The state
     */
    S state();

    /**
     * Should return the reward for the state given an action and target state.
     *
     * @param action An action taken from the state this interface is based on
     * @param targetState The target state reached by doing the action on the state
     * @return The reward for the state, depending on action and target state
     */
    double reward(A action, S targetState);
}
