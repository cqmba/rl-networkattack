package q_learning;

import core.AdversaryAction;
import core.NodeAction;
import core.State;
import environment.NetworkNode;
import me.tongfei.progressbar.ProgressBar;
import q_learning.mdp.MDP;
import q_learning.mdp.QLearner;
import q_learning.utils.Pair;
import q_learning.utils.Parameter;
import run.Simulation;
import stats.StatisticsHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LearnRandomGreedy {
    private static final Logger LOGGER = Logger.getLogger(LearnRandomGreedy.class.getName());
    private static final double ERROR = 0.000000001;
    private static final double R_PLUS = 20.0;
    private static final int NE = 5;
    private static final int iterations = 500000;

    public static void main(String[] args) throws IOException {
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Setting up environment...");
        Simulation.setupWorld();
        MDP<State, NodeAction> mdp = getMDP();

        Parameter parameter = new Parameter(1, 0.1, 0.1, 1.0,
                0.3, 0.0, 1.0,
                1.0, 0, ERROR, NE,
                R_PLUS, 100000, 100000,
                "",
                true);
        //runWithParameters(mdp, params, "runData", 10000, null);
        List<Double> rewards = new ArrayList<>();
        List<Double> runRewards = new ArrayList<>();
        List<NodeAction> curPolicy = new ArrayList<>();
        List<NodeAction> shortestPolicy = new ArrayList<>();
        List<NodeAction> bestRewardPolicy = new ArrayList<>();
        double bestReward = 0.0;
        int[] actcount = new int[iterations];
        Random random = new Random(parameter.getSeed());
        try (ProgressBar pb = new ProgressBar("Random It.", iterations)) {
            for (int i=0; i<iterations;i++) {
                State curState = mdp.getInitialState();
                curPolicy.clear();
                runRewards.clear();
                NodeAction curAction;
                do {
                    if (!mdp.isFinalState(curState)) {
                        curAction = getRandomAction(mdp, curState, random);
                        curPolicy.add(curAction);
                        State nextState = null;
                        if (curAction != null){
                            nextState = mdp.stateTransition(curState, curAction);
                            runRewards.add(mdp.reward(curState, curAction, nextState));
                            curState = nextState;
                        }else {
                            break;
                        }
                    }
                } while (!mdp.isFinalState(curState));
                runRewards.add(mdp.reward(curState, null,null));
                actcount[i] = runRewards.size();
                double runReward = runRewards.stream().mapToDouble(f -> f).sum();
                rewards.add(runReward);
                if (bestReward<runReward){
                    bestReward = runReward;
                    bestRewardPolicy = new ArrayList<>(curPolicy);
                }
                if (curPolicy.size()<shortestPolicy.size() || shortestPolicy.isEmpty()){
                    shortestPolicy = new ArrayList<>(curPolicy);
                }
                pb.step();
            }
        }
        StatisticsHelper actionStats = new StatisticsHelper(actcount);
        LOGGER.info("Minimum transitions: "+actionStats.getMin());
        LOGGER.info("Maximum transitions: "+actionStats.getMax());
        LOGGER.info("Mean transitions: "+ String.format("%.2f", actionStats.getMean()));
        LOGGER.info("Median transitions: "+actionStats.getMedian());
        LOGGER.info("Mode transitions: "+actionStats.mode());
        LOGGER.info("Standard deviation transitions: "+ String.format("%.2f", actionStats.getSD()));
        double mean_reward = rewards.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
        LOGGER.info("Minimum reward: "+String.format("%.2f", rewards.stream().mapToDouble(Double::doubleValue).min().getAsDouble()));
        LOGGER.info("Maximum reward: "+String.format("%.2f", rewards.stream().mapToDouble(Double::doubleValue).max().getAsDouble()));
        LOGGER.info("Mean reward: "+String.format("%.2f", mean_reward));
        LOGGER.info("SD reward: "+String.format("%.2f", StatisticsHelper.getSD(mean_reward, rewards)));
        LOGGER.info("Accumulated rewards" + String.format("%.2f", rewards.stream().mapToDouble(f -> f).sum()));
        LOGGER.info("Most Reward Policy Size "+bestRewardPolicy.size() + ": Reward "+ String.format("%.2f", bestReward));
        Simulation.printPolicy(bestRewardPolicy);
        if (!bestRewardPolicy.equals(shortestPolicy)){
            LOGGER.info("Shortest Policy Size "+shortestPolicy.size()+ ": Reward "+String.format("%.2f", getRewardsForNodeActionList(shortestPolicy, mdp).stream().mapToDouble(f -> f).sum()));
            Simulation.printPolicy(shortestPolicy);
        }
    }

    public static MDP<State, NodeAction> getMDP(){
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Loading MDP...");
        try (FileInputStream streamIn = new FileInputStream("mdp.ser"); ObjectInputStream objectinputstream = new ObjectInputStream(streamIn)) {
            return (MDP<State, NodeAction>) objectinputstream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<Double> getListOfRewardsForRandom(MDP<State, NodeAction> mdp, Random random){
        State curState = mdp.getInitialState();
        NodeAction curAction;
        List<Double> rewards = new ArrayList<>();
        int action_count = 0;
        do {
            if (!mdp.isFinalState(curState)) {
                curAction = getRandomAction(mdp, curState, random);
                State nextState = null;
                if (curAction != null){
                    nextState = mdp.stateTransition(curState, curAction);
                    action_count++;
                    rewards.add(mdp.reward(curState, curAction, nextState));
                    curState = nextState;
                }else {
                    break;
                }
            }
        } while (!mdp.isFinalState(curState));
        rewards.add(mdp.reward(curState, null,null));
        LOGGER.info("Action count: "+action_count);
        return rewards;
    }

    private static List<Double> getListOfRewardsForMaxGreed(MDP<State, NodeAction> mdp, Random random){
        State curState = mdp.getInitialState();
        NodeAction curAction;
        List<Double> rewards = new ArrayList<>();
        int action_count = 0;
        do {
            if (!mdp.isFinalState(curState)) {
                curAction = argmaxAPrimeGreedy(mdp, curState,random);
                //LOGGER.info("\tActive Host: " + curAction.getCurrentActor() + " \tTarget: " + curAction.getTarget() + " \tAction: " + curAction.getAction());
                State nextState = null;

                if (curAction != null){
                    nextState = mdp.stateTransition(curState, curAction);
                    action_count++;

                    rewards.add(mdp.reward(curState, curAction, nextState));

                    curState = nextState;
                }else {
                    break;
                }
            }
        } while (!mdp.isFinalState(curState));
        rewards.add(mdp.reward(curState, null,null));
        LOGGER.info("Action count: "+action_count);
        return rewards;
    }

    private static NodeAction argmaxAPrimeGreedy(MDP<State, NodeAction> mdp, State sPrime, Random random){
        List<Pair<NodeAction, Double>> actionReward = new ArrayList<>();
        for (NodeAction aPrime : mdp.getActionsFunction().actions(sPrime)) {
            double reward = mdp.reward(sPrime, aPrime, mdp.stateTransition(sPrime, aPrime));
            actionReward.add(new Pair<>(aPrime, reward));
        }
        actionReward.sort(Comparator.comparing(Pair::getB, Collections.reverseOrder()));
        double max = actionReward.get(0).getB();
        List<NodeAction> maxActions = new ArrayList<>();
        for (Pair<NodeAction, Double> aReward : actionReward) {
            if (aReward.getB() < max - ERROR)
                break;
            maxActions.add(aReward.getA());
            //LOGGER.info("mehrere Aktionen hier möglich" + aReward.getA());
        }
        int item = random.nextInt(maxActions.size());
        return maxActions.get(item);
    }

    private static NodeAction getRandomAction(MDP<State, NodeAction> mdp, State sPrime,Random random){
        Set<NodeAction> actions = mdp.getActionsFunction().actions(sPrime);
        int item = random.nextInt(actions.size());
        int i = 0;
        for (NodeAction action : actions) {
            if (i == item) {
                return action;
            }
            i++;
        }
        return null;
    }

    public static List<Double> getRewardsForNodeActionList(List<NodeAction> actions, MDP<State, NodeAction> mdp){
        List<Double> rewards = new ArrayList<>();
        try {
            State curState = mdp.getInitialState();
            for (NodeAction action: actions){
                State nextState = mdp.stateTransition(curState, action);
                rewards.add(mdp.reward(curState, action, nextState));
                curState = nextState;
            }
        } catch (Exception e){
            LOGGER.info("Wrong action sequence was given");
        }
        return rewards;
    }

    private static NodeAction randomAction(MDP<State, NodeAction> mdp, State sPrime, Random random){
        return null;
    }

    private static void runWithParameters(MDP<State, NodeAction> mdp, List<Parameter> params, String filename,
                                          int loggingCount, String loadFilename) {
        // create learner. The parameters are changed at each run in the for loop.
        Parameter dummyParam = new Parameter(1, 0.1, 0.1, 1.0,
                0.1, 0.1, 1.0, 1.0, 0, ERROR, 1, 0.0,
                0, 0, "", false);
        QLearner<State, NodeAction> learner = new QLearner<>(mdp, dummyParam, loggingCount);

        if (loadFilename != null)
            learner.loadData(loadFilename);

        for (Parameter par : params) {
            learner.setParameter(par);

            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Learning...");
            learner.runIterations();

            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Printing best path from initial state...");
            try {
                List<Pair<State, NodeAction>> path = learner.getPreferredPath(0);
                NetworkNode.TYPE previousActor = null;
                for (Pair<State, NodeAction> pair : path) {
                    NodeAction nodeAction = pair.getB();
                    if (nodeAction == null) {
                        break;
                    }
                    if (!nodeAction.getCurrentActor().equals(previousActor)) {
                        previousActor = nodeAction.getCurrentActor();
                        LOGGER.info("\tActive Host: " + previousActor + " \tTarget: " + nodeAction.getTarget() + " \tAction: " + nodeAction.getAction());
                    } else {
                        LOGGER.info("\t\t\tTarget: " + nodeAction.getTarget() + " \tAction: " + nodeAction.getAction());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Saving learning values...");
        learner.saveData(filename);
    }

    private void runsomething(){
        /*

        try (ProgressBar pb = new ProgressBar("Random It.", iterations)) {
            for (int i=0; i<iterations;i++) {
                singleRun.clear();
                state = State.getStartState();
                int zdOncePerRun = 0;
                int failedOncePerRun = 0;
                while (!state.isFinalState()){
                    for (NetworkNode.TYPE actor: state.getNodesWithAnyNodeAccess()){
                        Map<AdversaryAction, Set<NetworkNode.TYPE>> actions = State.computePossibleActions(state, actor);
                        for (AdversaryAction action: actions.keySet()){
                            for (NetworkNode.TYPE target: actions.get(action)){
                                transitionList.add(new NodeAction(target, actor, action));
                            }
                        }
                    }
                    Collections.shuffle(transitionList);
                    //execute random Action
                    NodeAction nodeAction = transitionList.get(0);
                    singleRun.add(nodeAction);
                    state = State.performGivenAction(Simulation.state, nodeAction.getAction(), nodeAction.getTarget(), nodeAction.getCurrentActor());
                    if (failedNodeActions.contains(nodeAction)){
                        failedState++;
                        failedOncePerRun = 1;
                    } else if (zerodayTransitions.contains(nodeAction)){
                        zeroday++;
                        zdOncePerRun = 1;
                    }
                    transitionList.clear();
                    //System.out.println("........Actor: "+ randomTransition.actor+" Performing ACTION "+ randomTransition.action+" on "+ randomTransition.target+"..........");
                }
                act_count[i] = singleRun.size();
                zdOncePerRunAggr += zdOncePerRun;
                failedOncePerRunAggr += failedOncePerRun;
                pb.step(); // step by 1
            }
        }
         */
    }
}
