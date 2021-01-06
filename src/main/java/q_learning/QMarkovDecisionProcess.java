package q_learning;

import aima.core.agent.Action;
import aima.core.probability.mdp.MarkovDecisionProcess;

public interface QMarkovDecisionProcess<S, A extends Action> extends MarkovDecisionProcess<S, A> {
    public double reward(S state, A action, S targetState);
}
