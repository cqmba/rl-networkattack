package q_learning.utils;

import java.util.List;

public class FullRun {
    private final Parameter parameter;
    private final List<Pair<Integer, Double>> rewards;
    private byte[] Q;

    public FullRun(Parameter parameter, List<Pair<Integer, Double>> rewards, byte[] Q) {
        this.parameter = parameter;
        this.rewards = rewards;
        this.Q = Q;
    }

    public byte[] getQ() { return Q; }

    public void removeQ(int counter) {
        Q = new byte[1];
        Q[0] = (byte)(counter % 256);
    }
}
