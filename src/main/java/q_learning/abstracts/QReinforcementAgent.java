package q_learning.abstracts;

import aima.core.agent.Action;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;
import aima.core.learning.reinforcement.PerceptStateReward;
import q_learning.mdp.MDP;

import java.io.Serializable;
import java.util.Map;

/**
 * This abstract class is originally the ReinforcementAgent class, with the only change that the execute method
 * has a MDP as the input allowing for the reward to be dependent on state and action.
 * More formally the reward function r was changed from: r(state) to: r(state, action, targetState).
 *
 * Original Doc:
 *
 * An abstract base class for creating reinforcement based agents.
 * @param <S>
 *            the state type.
 * @param <A>
 *            the action type.
 *
 * @author Ciaran O'Reilly
 * @author Ravi Mohan
 */
public abstract class QReinforcementAgent<S extends Serializable, A extends Action & Serializable> extends AbstractAgent {
    protected QReinforcementAgent() {
    }

    public abstract A execute(S state, MDP<S, A> mdp, int curIteration);

    public abstract Map<S, Double> getUtility();

    public abstract void reset();

    @Override
    public Action execute(Percept p) {
        // needed to remove this, since the execute(PerceptStateReward) no longer exists and this code would
        // create an infinity loop.
        throw new IllegalArgumentException(
                "Not supported");
    }
}
