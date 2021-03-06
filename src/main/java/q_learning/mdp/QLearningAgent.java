package q_learning.mdp;

import java.io.Serializable;
import java.util.*;

import aima.core.agent.Action;
import aima.core.probability.mdp.ActionsFunction;
import aima.core.util.FrequencyCounter;
import q_learning.utils.Pair;
import q_learning.utils.Parameter;
import q_learning.abstracts.QReinforcementAgent;

/**
 * This class is based off the original AIMA libraries QLearningAgent, but was modified. Please ignore the horrible
 * naming conventions, as they mostly come from their original code.
 */

/**
 * Artificial Intelligence A Modern Approach (3rd Edition): page 844.<br>
 * <br>
 *
 * <pre>
 * function Q-LEARNING-AGENT(percept) returns an action
 *   inputs: percept, a percept indicating the current state s' and reward signal r'
 *   persistent: Q, a table of action values indexed by state and action, initially zero
 *               N<sub>sa</sub>, a table of frequencies for state-action pairs, initially zero
 *               s,a,r, the previous state, action, and reward, initially null
 *
 *   if TERMAINAL?(s) then Q[s,None] <- r'
 *   if s is not null then
 *       increment N<sub>sa</sub>[s,a]
 *       Q[s,a] <- Q[s,a] + &alpha;(N<sub>sa</sub>[s,a])(r + &gamma;max<sub>a'</sub>Q[s',a'] - Q[s,a])
 *   s,a,r <- s',argmax<sub>a'</sub>f(Q[s',a'],N<sub>sa</sub>[s',a']),r'
 *   return a
 * </pre>
 *
 * Figure 21.8 An exploratory Q-learning agent. It is an active learner that
 * learns the value Q(s,a) of each action in each situation. It uses the same
 * exploration function f as the exploratory ADP agent, but avoids having to
 * learn the transition model because the Q-value of a state can be related
 * directly to those of its neighbors.<br>
 * <br>
 * <b>Note:</b> There appears to be two minor defects in the algorithm outlined
 * in the book:<br>
 * if TERMAINAL?(s) then Q[s,None] <- r'<br>
 * should be:<br>
 * if TERMAINAL?(s') then Q[s',None] <- r'<br>
 * so that the correct value for Q[s',a'] is used in the Q[s,a] update rule when
 * a terminal state is reached.<br>
 * <br>
 * s,a,r <- s',argmax<sub>a'</sub>f(Q[s',a'],N<sub>sa</sub>[s',a']),r'<br>
 * should be:
 *
 * <pre>
 * if s'.TERMINAL? then s,a,r <- null else s,a,r <- s',argmax<sub>a'</sub>f(Q[s',a'],N<sub>sa</sub>[s',a']),r'
 * </pre>
 *
 * otherwise at the beginning of a consecutive trial, s will be the prior
 * terminal state and is what will be updated in Q[s,a], which appears not to be
 * correct as you did not perform an action in the terminal state and the
 * initial state is not reachable from the prior terminal state. Comments
 * welcome.
 *
 * @param <S>
 *            the state type.
 * @param <A>
 *            the action type.
 *
 * @author Ciaran O'Reilly
 * @author Ravi Mohan
 * @author Ruediger Lunde
 *
 */
public class QLearningAgent<S extends Serializable, A extends Action & Serializable> extends QReinforcementAgent<S, A> {
    // persistent: Q, a table of action values indexed by state and action,
    // initially zero
    private final Map<Pair<S, A>, Double> Q = new HashMap<>();
    // N<sub>sa</sub>, a table of frequencies for state-action pairs, initially
    // zero
    private final FrequencyCounter<Pair<S, A>> Nsa = new FrequencyCounter<>();
    // s,a,r, the previous state, action, and reward, initially null
    private S s = null;
    private A a = null;
    private Double r = null;
    // The actions per state, the parameters and a random generator
    private final ActionsFunction<S, A> actionsFunction;
    private Parameter parameter;
    private Random random;

    /**
     * Constructor.
     *
     * @param actionsFunction
     *            a function that lists the legal actions from a state.
     * @param parameter
     *            the parameters for the learning
     */
    public QLearningAgent(ActionsFunction<S, A> actionsFunction, Parameter parameter) {
        if (parameter == null)
            throw new IllegalArgumentException("Parameters are null");
        this.actionsFunction = actionsFunction;
        this.parameter = parameter;
        this.random = new Random(parameter.getSeed());
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
        this.random = new Random(parameter.getSeed());
    }

    public Parameter getParameters() { return parameter; }

    //##########################################################################
    //              FUNCTIONALITY
    //##########################################################################

    /**
     * An exploratory Q-learning agent. It is an active learner that learns the
     * value Q(s,a) of each action in each situation. It uses the same
     * exploration function f as the exploratory ADP agent, but avoids having to
     * learn the transition model because the Q-value of a state can be related
     * directly to those of its neighbors.
     *
     * @param state
     *            the current state
     * @param mdp
     *            The markov decision process, which is used to get the reward for a state and an action
     * @return an action
     */
    @Override
    public A execute(S state, MDP<S, A> mdp, int curIteration) {
        S sPrime = state;

        // if TERMAINAL?(s') then Q[s',None] <- r'
        if (mdp.isFinalState(sPrime)) {
            Q.put(new Pair<>(sPrime, null), mdp.reward(state,null, null));
        }

        // if s is not null then
        if (null != s) {
            // increment N<sub>sa</sub>[s,a]
            Pair<S, A> sa = new Pair<>(s, a);
            Nsa.incrementFor(sa);
            // Q[s,a] <- Q[s,a] + &alpha;(N<sub>sa</sub>[s,a])(r +
            // &gamma;max<sub>a'</sub>Q[s',a'] - Q[s,a])
            Double Q_sa = Q.get(sa);
            if (null == Q_sa) {
                Q_sa = 0.0;
            }
            Q.put(sa, Q_sa + alpha(Nsa, s, a)
                    * (r + parameter.getDiscountFactor() * maxAPrime(sPrime, mdp) - Q_sa));
        }
        // if s'.TERMINAL? then s,a,r <- null else
        // s,a,r <- s',argmax<sub>a'</sub>f(Q[s',a'],N<sub>sa</sub>[s',a']),r'
        if (mdp.isFinalState(sPrime)) {
            s = null;
            a = null;
            r = null;
        } else {
            s = sPrime;
            a = argmaxAPrime(sPrime, curIteration);
            r = mdp.reward(s, a, mdp.stateTransition(s, a));
        }

        // return a
        return a;
    }

    /**
     * resets the Q-Learning
     */
    @Override
    public void reset() {
        Q.clear();
        Nsa.clear();
        s = null;
        a = null;
        r = null;
    }

    /**
     * Returns the estimated utility
     */
    @Override
    public Map<S, Double> getUtility() {
        // Q-values are directly related to utility values as follows
        // (AIMA3e pg. 843 - 21.6) :
        // U(s) = max<sub>a</sub>Q(s,a).
        Map<S, Double> U = new HashMap<>();
        for (Pair<S, A> sa : Q.keySet()) {
            Double q = Q.get(sa);
            Double u = U.get(sa.getA());
            if (null == u || u < q) {
                U.put(sa.getA(), q);
            }
        }

        return U;
    }

    /**
     * returns Q itself
     */
    public Map<Pair<S, A>, Double> getQ() {
        return Q;
    }

    /**
     * Loads a new Q
     */
    public void setQ(Map<Pair<S, A>, Double> Q) {
        this.Q.clear();
        for (Map.Entry<Pair<S, A>, Double> entry : Q.entrySet()) {
            this.Q.put(entry.getKey(), entry.getValue());
        }
    }

    //##########################################################################
    //              Private/Protected methods
    //##########################################################################

    /**
     * This function is a concession function. It is usually used in negotiations and expresses the concession of
     * an agent.
     * It is used here, as it is a very customizable function, where the slope can be adjusted.
     *
     * This function creates a curve from pMax to pMin. The variable of the graph is t, which expresses the time.
     * When t = 0, the method returns pMax. When t = 1, it returns pMin. For values between 0 and 1, the method
     * returns values in form of a curve, which is defined by e (also called concession factor).
     * For 0 < e < 1, the curve will be high first and fall down at the end (assuming pMax > pMin),
     * for e = 1 it will behave linear and for e>1 it will fall down fast and near pMin slowly at the end.
     * k describes a percentile on the first value (pMax). Increasing k will lower the first value of the function.
     * k = 0 will create a slope from pMax to pMin as described above.
     *
     * @param pMin The value of the function when t = 1
     * @param pMax The value of the function when t = 0
     * @param k 0 <= k <= 1: a higher value decreases the initial value of the function. For a slope from pMax to pMin,
     *          use k = 0
     * @param e 0 < e: the concession factor; describes the slope of the function. For 0 < e < 1 it will
     *          stay near pMax first and go fast against pMin at the end, for e = 1 it will be linear,
     *          for e > 1 it will go near pMin fast at the start and be flat at the end
     * @param t 0 <= t <= 1: The variable of the function. It describes the time, where 0 is the start and
     *          1 is the last value
     * @return A value between pMax and pMin
     */
    public static double conc(double pMin, double pMax, double k, double e, double t) {
        if (k < 0 || k > 1)
            throw new IllegalArgumentException("k must be in [0, 1]");
        if (e <= 0)
            throw new IllegalArgumentException("e must be greater 0");
        if (t < 0 || t > 1)
            throw new IllegalArgumentException("t must be in [0, 1]");
        return pMin + ((pMax - pMin) * (1 - (k + ((1 - k) * Math.pow(t, 1.0 / e)))));
    }

    /**
     * AIMA3e pg. 836 'if we change &alpha; from a fixed parameter to a function
     * that decreases as the number of times a state action has been observed
     * increases, then U<sup>&pi;</sup>(s) itself will converge to the correct
     * value.<br>
     * <br>
     * <b>Note:</b> override this method to obtain the desired behavior.
     *
     * @param Nsa
     *            a frequency counter of observed state action pairs.
     * @param s
     *            the current state.
     * @param a the current action.
     * @return the learning rate to use based on the frequency of the state
     *         passed in.
     */
    protected double alpha(FrequencyCounter<Pair<S, A>> Nsa, S s, A a) {
        int count = Nsa.getCount(new Pair<>(s, a));
        double t = Math.min((double)count / (double)parameter.getLearningRateMaxCount(), 1.0);
        return conc(parameter.getLearningRateEndValue(), parameter.getLearningRateStartValue(), 0.0, parameter.getLearningRateSlope(), t);
    }

    /**
     * Calculates a probability at which a random action is chosen using the current iteration.
     *
     * @param curIteration The current iteration of the run
     * @return The probability at which a random action should be chosen
     */
    protected double epsilon(int curIteration) {
        double t = Math.min((double)curIteration / ((double)(parameter.getIterations() + parameter.getInitialStateIterations())), 1.0);
        return conc(parameter.getEpsilonEndValue(), parameter.getEpsilonStartValue(), 0.0, parameter.getEpsilonSlope(), t);
    }

    /**
     * AIMA3e pg. 842 'f(u, n) is called the <b>exploration function</b>. It
     * determines how greed (preferences for high values of u) is traded off
     * against curiosity (preferences for actions that have not been tried often
     * and have low n). The function f(u, n) should be increasing in u and
     * decreasing in n.
     *
     *
     * <b>Note:</b> Override this method to obtain desired behavior.
     *
     * @param u
     *            the currently estimated utility.
     * @param n
     *            the number of times this situation has been encountered.
     * @return the exploration value.
     */
    protected double f(Double u, int n) {
        // A Simple definition of f(u, n):
        if (null == u || n < parameter.getNe()) {
            return parameter.getrPlus();
        }
        return u;
    }

    private double maxAPrime(S sPrime, MDP<S, A> mdp) {
        double max = Double.NEGATIVE_INFINITY;
        if (mdp.isFinalState(sPrime)) {
            // a terminal state
            max = Q.get(new Pair<>(sPrime, null));
        } else {
            for (A aPrime : actionsFunction.actions(sPrime)) {
                Double Q_sPrimeAPrime = Q.get(new Pair<>(sPrime, aPrime));
                if (null != Q_sPrimeAPrime && Q_sPrimeAPrime > max) {
                    max = Q_sPrimeAPrime;
                }
            }
        }
        if (max == Double.NEGATIVE_INFINITY) {
            // Assign 0 as the mimics Q being initialized to 0 up front.
            max = 0.0;
        }
        return max;
    }

    // argmax<sub>a'</sub>f(Q[s',a'],N<sub>sa</sub>[s',a'])
    /*
     * This method was improved by giving the possibility for a epsilon, where a random action is selected with
     * probability epsilon.
     * Also in case multiple actions share the same utility, a random action from the actions with maximum utility
     * is selected.
     */
    private A argmaxAPrime(S sPrime, int curIteration) {
        A a = null;
        if (random.nextDouble() < epsilon(curIteration)) {
            // choose random action
            int item = random.nextInt(actionsFunction.actions(sPrime).size());
            int i = 0;
            for (A action : actionsFunction.actions(sPrime)) {
                if (i == item) {
                    a = action;
                    break;
                }
                i++;
            }
        } else {
            // choose best action by estimated utility
            List<Pair<A, Double>> actionReward = new ArrayList<>();
            for (A aPrime : actionsFunction.actions(sPrime)) {
                Pair<S, A> sPrimeAPrime = new Pair<>(sPrime, aPrime);
                double explorationValue = f(Q.get(sPrimeAPrime), Nsa
                        .getCount(sPrimeAPrime));
                actionReward.add(new Pair<>(aPrime, explorationValue));
            }
            actionReward.sort(Comparator.comparing(Pair::getB, Collections.reverseOrder()));
            double max = actionReward.get(0).getB();
            List<A> maxActions = new ArrayList<>();
            for (Pair<A, Double> aReward : actionReward) {
                if (aReward.getB() < max - parameter.getError())
                    break;
                maxActions.add(aReward.getA());
            }
            int item = random.nextInt(maxActions.size());
            a = maxActions.get(item);
        }
        return a;
    }
}
