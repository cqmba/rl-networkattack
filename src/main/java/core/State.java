package core;


import environment.NetworkNode;
import knowledge.NetworkKnowledge;
import knowledge.NodeKnowledge;
import knowledge.SoftwareKnowledge;
import knowledge.impl.NetworkKnowledgeImpl;

import java.io.*;
import java.util.*;

public class State implements Serializable {
    private Map<NetworkNode.TYPE, NodeKnowledge> nodeKnowledgeMap;

    private NetworkKnowledge networkKnowledge;
    //used to determine which Node an Action is executed FROM in PostCondition Of Action
    //private NetworkNode.TYPE currentActor;
    //Map for the SoftwareKnowledge of the adversary for each NetworkNode
    private Map<NetworkNode.TYPE,Set<SoftwareKnowledge>> softwareKnowledgeMap = new HashMap<>();

    public State() {
        this.nodeKnowledgeMap = new LinkedHashMap<>();
        this.networkKnowledge = new NetworkKnowledgeImpl();
    }

    public static State getStartState(){
        State start = new State();
        start.addNodeKnowledge(NetworkNode.TYPE.ADVERSARY);
        start.nodeKnowledgeMap.get(NetworkNode.TYPE.ADVERSARY).addAccessLevel(NetworkNode.ACCESS_LEVEL.ROOT);
        start.addNodeKnowledge(NetworkNode.TYPE.ROUTER);

        return start;
    }

    //assumes next acting node was determined already, not sure when this actually happens
    public static Map<AdversaryAction, Set<NetworkNode.TYPE>> computePossibleActions(State current, NetworkNode.TYPE currentActor){
        //TODO
        Map<AdversaryAction, Set<NetworkNode.TYPE>> targetsByAction = new HashMap<>();
        for (AdversaryAction action: AdversaryAction.values()){
            Set<NetworkNode.TYPE> targets = action.getTargetsWhichFulfillPrecondition(current,currentActor);
            if (!targets.isEmpty()){
                targetsByAction.put(action, targets);
            }
        }
        return targetsByAction;
    }

    //perform a certain Action from an acting node towards a target node
    public static State performGivenAction(State s, AdversaryAction action, NetworkNode.TYPE target, NetworkNode.TYPE currentActor){
        return action.executePostConditionOnTarget(target, s, currentActor);
    }

    public void addNodeKnowledge(NetworkNode.TYPE node){
        networkKnowledge.addNewNode(node);
        nodeKnowledgeMap.put(node, NodeKnowledge.addNode(node));
    }

    public void addNodeHostname(NetworkNode.TYPE node, String hostname){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        nodeKnowledgeMap.replace(node, old.addHostname(hostname));
    }

    public void addNodePubIp(NetworkNode.TYPE node, String pubIp){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        NodeKnowledge newNode = old.addPubIp(pubIp);
        nodeKnowledgeMap.replace(node, newNode);
    }

    public void addNodePrivIp(NetworkNode.TYPE node, String privIp){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        nodeKnowledgeMap.replace(node, old.addPrivIp(privIp));
    }

    public void addNodeRemoteSoftwareName(NetworkNode.TYPE node, String swName, boolean remote){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        //node already entered
        if(softwareKnowledgeMap.containsKey(node)) {
            Set<SoftwareKnowledge> softwareKnowledgeSet = softwareKnowledgeMap.get(node);
            //check if the software is already known
            if (!softwareContainedInSet(swName, softwareKnowledgeSet)) {
                Set<SoftwareKnowledge> newKnowledge = new HashSet<>(softwareKnowledgeSet);
                newKnowledge.add(SoftwareKnowledge.addNew(swName, remote));
                softwareKnowledgeMap.replace(node, newKnowledge);
            }
        }else{
            //for the case that the node is not contained in the map, create entry in the map
            softwareKnowledgeMap.put(node, Set.of(SoftwareKnowledge.addNew(swName, remote)));
        }
    }

    public Set<NetworkNode.TYPE> getSetOfSystemWithAcess(){
        Set<NetworkNode.TYPE> nodesWithAcess = new HashSet<>();
        for(NetworkNode.TYPE node : this.getNodeKnowledgeMap().keySet()){
            if(this.getNodeKnowledgeMap().get(node).hasAccessLevelRoot()||this.getNodeKnowledgeMap().get(node).hasAccessLevelUser())
                nodesWithAcess.add(node);
        }
        return nodesWithAcess;
    }

    public NetworkKnowledge getNetworkKnowledge() {
        return networkKnowledge;
    }

    public Map<NetworkNode.TYPE, NodeKnowledge> getNodeKnowledgeMap() {
        return nodeKnowledgeMap;
    }



    public Map<NetworkNode.TYPE, Set<SoftwareKnowledge>> getSoftwareKnowledgeMap() {
        return softwareKnowledgeMap;
    }

    public static Set<State> computeListOfPossibleStates(State startState){
        Set<State> states = new HashSet<>();
        states.add(startState);
        int previousNumber_of_States =0;

        while(previousNumber_of_States<states.size()) {
            previousNumber_of_States = states.size();
            Set<State> newSetofStates = (Set<State>) deepCopy(states);
            for (State s : states) {
                Set<NodeAction> possibleActions = NodeAction.getAllActionPossibleWithChangeState(s);
                for (NodeAction a : possibleActions) {
                    newSetofStates.add(a.action.executePostConditionOnTarget(a.target,s,a.currentActor));
                }
            }
            states = newSetofStates;
        }
        return states;
    }


    Boolean softwareContainedInSet(String name , Set<SoftwareKnowledge> softwareKnowledgeSet){
        for(SoftwareKnowledge s : softwareKnowledgeSet){
            if(s.getName().equals(name))
                return true;
        }
        return false;
    }

    public void setNetworkKnowledge(NetworkKnowledge networkKnowledge) {
        this.networkKnowledge = networkKnowledge;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof State) {
            State other = (State) o;
            return this.softwareKnowledgeMap.equals(other.softwareKnowledgeMap) && this.networkKnowledge.equals(other.networkKnowledge) && this.nodeKnowledgeMap.equals(other.nodeKnowledgeMap);
        }
        return false;
    }

    @Override
    public int hashCode() {

        return Objects.hash(nodeKnowledgeMap, networkKnowledge, softwareKnowledgeMap);
    }

    /**
     * Makes a deep copy of any Java object that is passed.
     */
    private static Object deepCopy(Object object) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
            outputStrm.writeObject(object);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
            return objInputStream.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
