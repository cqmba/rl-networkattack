package q_learning.interfaces;

import aima.core.agent.Action;
import aima.core.probability.mdp.ActionsFunction;
import aima.core.probability.mdp.MarkovDecisionProcess;

public interface QMarkovDecisionProcess<S, A extends Action> extends MarkovDecisionProcess<S, A> {
    double reward(S state, A action, S targetState);
    S stateTransition(S state, A action);
    ActionsFunction<S, A> getActionsFunction();
    boolean isFinalState(S state);
}
