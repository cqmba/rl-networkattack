package q_learning.interfaces;

import aima.core.agent.Action;
import aima.core.probability.mdp.ActionsFunction;

import java.io.Serializable;

public interface QActionsFunctionInterface<S extends Serializable, A extends Action & Serializable> extends ActionsFunction<S, A> {
}
