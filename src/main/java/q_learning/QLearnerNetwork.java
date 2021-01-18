package q_learning;

import aima.core.probability.mdp.ActionsFunction;

import core.NodeAction;
import core.State;
import q_learning.interfaces.StateReward;
import q_learning.mdp.*;
import run.Simulation;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class QLearnerNetwork {

    private static final Logger LOGGER = Logger.getLogger(QLearnerNetwork.class.getName());

    /*
     * Learning Rate determines how much new information is used instead of old and is between 0 and 1. A learning rate
     * of 1 overrides all learned data, while a learning rate of 0 does not learn. Usually smaller values are used
     * (0.01 to 0.1), so that jumping over the perfect value (which should be learned) is prevented.
     */
    private static final double LEARNING_RATE = 0.2;

    /*
     * In q-learning an action is selected and the value of that action is calculated. For that the best action after
     * that is also considered. The discount factor manages the importance of the next actions and must be between
     * 0 and 1. A low value is considered short-sighted, while a high value looks further into the future.
     * Due to problems when the discount factor is near 1, it is advised to use a smaller value.
     */
    private static final double DISCOUNT_FACTOR = 1.0;

    /*
     * In order to select the next action, the q-learning agent will select either the best action by utility for
     * the next step or with probability of epsilon will select a random action.
     */
    private static final double EPSILON = 0.05;

    /*
     * A fixed seed for random path selection
     */
    private static final int SEED = 0;

    /*
     * To which extend two doubles may differ and be considered identical
     */
    private static final double ERROR = 0.000000001;

    /*
     * Number of times each state is at least visited
     */
    private static final int NE = 5;

    // Should be the highest (or higher than that) reward possible
    private static final double R_PLUS = 4.0;


    public static void main(String[] args) {
        Simulation.setupWorld();
        Map<State, StateReward<State, NodeAction>> states = null;
        try {
            states = generateStates();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ActionsFunction<State, NodeAction> actions = generateActions(states);
        QStateTransition<State, NodeAction> transitions = generateTransitions(states, actions);
        Set<State> finalStates = getFinalStates(states);
        MDP<State, NodeAction> mdp = new MDP<>(states, State.getStartState(), actions, transitions, finalStates);

        QLearner<State, NodeAction> learner = new QLearner<>(mdp, LEARNING_RATE, DISCOUNT_FACTOR, EPSILON, ERROR, NE, R_PLUS, SEED);

        learner.runIterations(200000, 20);

        try {
            List<Pair<State, NodeAction>> path = learner.getPreferredPath(0);
            for (Pair<State, NodeAction> pair : path) {
                LOGGER.info(String.format("\tAction: %s", pair.getB()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the states
     * @return The states of the MDP
     */
    private static Map<State, StateReward<State, NodeAction>> generateStates() throws IOException {
        FileInputStream streamIn = null;
        ObjectInputStream objectinputstream = null;
        Set<State> stateSet = null;
        try {
            streamIn = new FileInputStream("states.ser");
            objectinputstream = new ObjectInputStream(streamIn);
            stateSet = (Set<State>) objectinputstream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (streamIn != null)
                streamIn.close();
            if(objectinputstream != null){
                objectinputstream .close();
            }
        }

        Map<State, StateReward<State, NodeAction>> states = new HashMap<>();

        for(State s : stateSet){
            double reward = 0.0;
            states.put(s, new KnowledgeStateReward(s, reward));
        }

        return states;
    }

    /**
     * Initializes the actions possible per state
     * @param states The states possible
     * @return An ActionFunction, which returns all possible actions per state
     */
    private static ActionsFunction<State, NodeAction> generateActions(
            Map<State, StateReward<State, NodeAction>> states) {
        QActionsFunction<State, NodeAction> actions = new QActionsFunction(states);


        for (State state : states.keySet()) {
            for (NodeAction nodeAction : NodeAction.getAllActionPossibleWithChangeState(state)) {
                actions.addAction(state, nodeAction);
            }
        }
        return actions;
    }



    private static QStateTransition<State, NodeAction> generateTransitions(Map<State,StateReward<State, NodeAction>> states, ActionsFunction<State, NodeAction> actions) {
            QStateTransition<State, NodeAction> transition = new QStateTransition<>();
            for (State state : states.keySet()) {
                for (NodeAction action : actions.actions(state)) {
                    transition.addTransition(state, action, NodeAction.performNodeAction(action,state));
                }
            }
            return transition;
        }

    private static Set<State> getFinalStates(Map<State, StateReward<State, NodeAction>> states){
        Set<State> finalStates = new HashSet<>();
        for (State state : states.keySet()) {
            if(state.isFinalState(0)){
                finalStates.add(state);
            }
        }
        return finalStates;
    }



}
