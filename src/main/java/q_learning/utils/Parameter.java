package q_learning.utils;

/**
 * This class stores all parameters for the Q-Learning and checks the validity.
 */
public class Parameter {
    private final int learningRateMaxCount;
    private final double learningRateEndValue;
    private final double learningRateStartValue;
    private final double learningRateSlope;

    private final double epsilonEndValue;
    private final double epsilonStartValue;
    private final double epsilonSlope;

    private final double discountFactor;
    private final int seed;
    private final double error;
    private final int ne;
    private final double rPlus;
    private final int iterations;
    private final int initialStateIterations;
    private final String additionalInformation;
    private final boolean saveQ;

    public Parameter(int learningRateMaxCount, double learningRateStartValue, double learningRateEndValue, double learningRateSlope,
                     double epsilonStartValue, double epsilonEndValue, double epsilonSlope,
                     double discountFactor, int seed, double error, int ne,
                   double rPlus, int iterations, int initialStateIterations, String additionalInformation, boolean saveQ) {
        if (learningRateMaxCount <= 0)
            throw new IllegalArgumentException("The Max Count for learning rate has to be at least 1");
        if (learningRateStartValue < 0 || learningRateStartValue > 1 || learningRateEndValue < 0 || learningRateEndValue > 1)
            throw new IllegalArgumentException("Learning Rate must be in [0, 1]");
        if (learningRateSlope <= 0)
            throw new IllegalArgumentException("The slope for the learning rate must be greater 0");
        if (epsilonStartValue < 0 || epsilonStartValue > 1 || epsilonEndValue < 0 || epsilonEndValue > 1)
            throw new IllegalArgumentException("Epsilon must be in [0, 1]");
        if (epsilonSlope <= 0)
            throw new IllegalArgumentException("The slope for the epsilon must be greater 0");
        if (discountFactor < 0 || discountFactor > 1)
            throw new IllegalArgumentException("Discount factor must be in [0, 1]");
        if (error <= 0)
            throw new IllegalArgumentException("Error must be greater 0");
        if (ne <= 0)
            throw new IllegalArgumentException("Ne must be greater 0");
        if (iterations < 0 || initialStateIterations < 0)
            throw new IllegalArgumentException("Iterations must be a natural number");

        this.learningRateMaxCount = learningRateMaxCount;
        this.learningRateStartValue = learningRateStartValue;
        this.learningRateEndValue = learningRateEndValue;
        this.learningRateSlope = learningRateSlope;

        this.epsilonStartValue = epsilonStartValue;
        this.epsilonEndValue = epsilonEndValue;
        this.epsilonSlope = epsilonSlope;

        this.discountFactor = discountFactor;
        this.seed = seed;
        this.error = error;
        this.ne = ne;
        this.rPlus = rPlus;
        this.iterations = iterations;
        this.initialStateIterations = initialStateIterations;
        this.additionalInformation = additionalInformation;
        this.saveQ = saveQ;
    }

    public double getDiscountFactor() {
        return discountFactor;
    }

    public int getSeed() {
        return seed;
    }

    public double getError() {
        return error;
    }

    public int getNe() {
        return ne;
    }

    public double getrPlus() {
        return rPlus;
    }

    public int getIterations() {
        return iterations;
    }

    public int getInitialStateIterations() {
        return initialStateIterations;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public boolean getSaveQ() {
        return saveQ;
    }

    public int getLearningRateMaxCount() {
        return learningRateMaxCount;
    }

    public double getLearningRateEndValue() {
        return learningRateEndValue;
    }

    public double getLearningRateStartValue() {
        return learningRateStartValue;
    }

    public double getLearningRateSlope() {
        return learningRateSlope;
    }

    public double getEpsilonEndValue() {
        return epsilonEndValue;
    }

    public double getEpsilonStartValue() {
        return epsilonStartValue;
    }

    public double getEpsilonSlope() {
        return epsilonSlope;
    }
}
