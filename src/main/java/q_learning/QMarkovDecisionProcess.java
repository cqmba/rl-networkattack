package q_learning;

import aima.core.agent.Action;
import aima.core.probability.mdp.ActionsFunction;
import aima.core.probability.mdp.MarkovDecisionProcess;

public interface QMarkovDecisionProcess<S, A extends Action> extends MarkovDecisionProcess<S, A> {
    public double reward(S state, A action, S targetState);
    public S stateTransition(S state, A action);
    public ActionsFunction<S, A> getActionsFunction();
    public boolean isFinalState(S state);
}
