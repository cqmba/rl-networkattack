package q_learning;

public class NetworkStateReward implements StateReward<NetworkState, NetworkAction> {
    private final NetworkState state;
    private final double reward;

    public NetworkStateReward(NetworkState state, double reward) {
        this.state = state;
        this.reward = reward;
    }

    @Override
    public double reward(NetworkAction action, NetworkState targetState) {
        return reward;
    }

    @Override
    public NetworkState state() {
        return state;
    }
}
