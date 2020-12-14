package q_learning;

import aima.core.learning.reinforcement.PerceptStateReward;

/**
 * Simple implementation of PerceptStateReward, containing a state and its reward
 */
public class NetworkPerceptStateReward implements PerceptStateReward<NetworkState> {
    private final NetworkState state;
    private final double reward;

    public NetworkPerceptStateReward(NetworkState state, double reward) {
        this.state = state;
        this.reward = reward;
    }

    @Override
    public NetworkState state() {
        return state;
    }

    @Override
    public double reward() {
        return reward;
    }
}
