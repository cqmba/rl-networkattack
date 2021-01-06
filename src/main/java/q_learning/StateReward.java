package q_learning;

import aima.core.agent.Action;

public interface StateReward<S, A extends Action> {
    public S state();
    public double reward(A action, S targetState);
}
