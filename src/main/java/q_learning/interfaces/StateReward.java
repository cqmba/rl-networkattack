package q_learning.interfaces;

import aima.core.agent.Action;

public interface StateReward<S, A extends Action> {
    S state();
    double reward(A action, S targetState);
}
