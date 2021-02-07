package q_learning;

public class Parameter {
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
    private final boolean saveQ;

    public Parameter(double learningRate, double discountFactor, double epsilon, int seed, double error, int ne,
                   double rPlus, int iterations, int initialStateIterations, String additionalInformation, boolean saveQ) {
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
        this.saveQ = saveQ;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public double getDiscountFactor() {
        return discountFactor;
    }

    public double getEpsilon() {
        return epsilon;
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
}
