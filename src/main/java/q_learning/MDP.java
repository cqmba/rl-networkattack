package q_learning;

import aima.core.probability.mdp.ActionsFunction;
import aima.core.probability.mdp.MarkovDecisionProcess;

import java.util.Map;
import java.util.Set;

/**
 * A simple implementation of a Markov Decision Process
 */
public class MDP implements MarkovDecisionProcess<NetworkState, NetworkAction> {
    private Map<NetworkState, Double> states;
    private NetworkState initialState;
    private ActionsFunction<NetworkState, NetworkAction> actions;

    public MDP(Map<NetworkState, Double> states, NetworkState initialState,
               ActionsFunction<NetworkState, NetworkAction> actions) {
        this.states = states;
        this.initialState = initialState;
        this.actions = actions;
    }

    /**
     * This function returns an ActionsFunction, which is needed for Q-Learning Agent to work.
     * @return
     */
    public ActionsFunction<NetworkState, NetworkAction> getActionsFunction() {
        return actions;
    }

    @Override
    public Set<NetworkState> states() {
        return states.keySet();
    }

    @Override
    public NetworkState getInitialState() {
        return initialState;
    }

    @Override
    public Set<NetworkAction> actions(NetworkState networkState) {
        return actions.actions(networkState);
    }

    @Override
    public double transitionProbability(NetworkState sDelta, NetworkState networkState, NetworkAction networkAction) {
        return 1.0 / (double) actions.actions(networkState).size();
    }

    @Override
    public double reward(NetworkState networkState) {
        return states.get(networkState);
    }
}
