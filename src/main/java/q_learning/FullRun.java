package q_learning;

import java.util.List;

public class FullRun {
    private final double learningRate;
    private final double discountFactor;
    private final double epsilon;
    private final int seed;
    private final double error;
    private final int ne;
    private final double rPlus;
    private final int iterations;
    private final int initialStateIterations;
    private final String additionalInformation;
    private final List<Pair<Integer, Double>> rewards;
    private final byte[] Q;

    public FullRun(double learningRate, double discountFactor, double epsilon, int seed, double error, int ne,
                   double rPlus, int iterations, int initialStateIterations, String additionalInformation,
                   List<Pair<Integer, Double>> rewards, byte[] Q) {
        this.learningRate = learningRate;
        this.discountFactor = discountFactor;
        this.epsilon = epsilon;
        this.seed = seed;
        this.error = error;
        this.ne = ne;
        this.rPlus = rPlus;
        this.iterations = iterations;
        this.initialStateIterations = initialStateIterations;
        this.additionalInformation = additionalInformation;
        this.rewards = rewards;
        this.Q = Q;
    }

    public byte[] getQ() { return Q; }
}
