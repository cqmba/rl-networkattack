package q_learning.utils;

import java.io.Serializable;
import java.util.List;

/**
 * This class stores a complete run of Q-Learning with all parameters, rewards, the policy, its reward and the learned
 * Q.
 * NOTE: This class is saved as a JSON file and therefore does not require getters.
 */
public class FullRun<S extends Serializable, A extends Serializable> {
    private final Parameter parameter;
    private final List<Pair<Integer, Double>> rewards;
    private List<Pair<S, A>> policy;
    private double policyReward;
    private byte[] Q;

    public FullRun(Parameter parameter, List<Pair<Integer, Double>> rewards, List<Pair<S, A>> policy, double policyReward,
                   byte[] Q) {
        this.parameter = parameter;
        this.rewards = rewards;
        this.policy = policy;
        this.policyReward = policyReward;
        this.Q = Q;
    }

    public byte[] getQ() { return Q; }

    public void removeQ(int counter) {
        Q = new byte[1];
        Q[0] = (byte)(counter % 256);
    }
}
