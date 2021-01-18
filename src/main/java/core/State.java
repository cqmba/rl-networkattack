package core;


import environment.Data;
import environment.NetworkNode;
import knowledge.NetworkKnowledge;
import knowledge.NodeKnowledge;
import knowledge.SoftwareKnowledge;
import run.Simulation;

import java.io.*;
import java.util.*;

public class State implements Serializable {
    //for this goal, only root rights on the admin pc must be achieved
    public static final int CONFIG_ROOT_ONLY = 0;
    //for this goal, achieve everything of goal 1 and read all database entries
    public static final int CONFIG_ROOT_DB = 1;
    //for this goal, gain root access on all systems, read db and create a new acc on admin pc
    public static final int CONFIG_ROOT_DB_ACC = 2;
    private Map<NetworkNode.TYPE, NodeKnowledge> nodeKnowledgeMap;
    private boolean startState = false;

    private NetworkKnowledge networkKnowledge;
    //used to determine which Node an Action is executed FROM in PostCondition Of Action
    //private NetworkNode.TYPE currentActor;
    //Map for the SoftwareKnowledge of the adversary for each NetworkNode
    private Map<NetworkNode.TYPE,Set<SoftwareKnowledge>> softwareKnowledgeMap = new EnumMap<>(NetworkNode.TYPE.class);

    public State(boolean startState) {
        this.nodeKnowledgeMap = new LinkedHashMap<>();
        this.networkKnowledge = NetworkKnowledge.addNew();
        this.startState = startState;
    }

    public static State getStartState(){
        State start = new State(true);
        start.addNodeKnowledge(NetworkNode.TYPE.ADVERSARY);
        start.nodeKnowledgeMap.get(NetworkNode.TYPE.ADVERSARY).addAccessLevel(NetworkNode.ACCESS_LEVEL.ROOT);
        start.addNodeKnowledge(NetworkNode.TYPE.ROUTER);

        return start;
    }

    public void setStartState(boolean startState){
        this.startState = startState;
    }

    //assumes next acting node was determined already, not sure when this actually happens
    public static Map<AdversaryAction, Set<NetworkNode.TYPE>> computePossibleActions(State current, NetworkNode.TYPE currentActor){
        //TODO
        Map<AdversaryAction, Set<NetworkNode.TYPE>> targetsByAction = new EnumMap<>(AdversaryAction.class);
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
        old.addHostname(hostname);
        nodeKnowledgeMap.replace(node, old);
    }

    public void addNodePubIp(NetworkNode.TYPE node, String pubIp){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        old.addPubIp(pubIp);
        nodeKnowledgeMap.replace(node, old);
    }

    public void addNodePrivIp(NetworkNode.TYPE node, String privIp){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        old.addPrivIp(privIp);
        nodeKnowledgeMap.replace(node, old);
    }

    public void addNodeOS(NetworkNode.TYPE node, String os){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        old.addOperationSystem(os);
        nodeKnowledgeMap.replace(node, old);
    }

    public void addNodeOSVersion(NetworkNode.TYPE node, String osversion){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        old.addOSVersion(osversion);
        nodeKnowledgeMap.replace(node, old);
    }

    public void addNodeRemoteSoftwareName(NetworkNode.TYPE node, String swName, boolean remote){
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

    public void addNodeData(NetworkNode.TYPE node, int ID, Data data){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        old.addData(ID, data);
        nodeKnowledgeMap.replace(node, old);
    }

    public void addNetworkData(Data data){
        networkKnowledge.addSniffedData(data);
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

    boolean softwareContainedInSet(String name , Set<SoftwareKnowledge> softwareKnowledgeSet){
        for(SoftwareKnowledge s : softwareKnowledgeSet){
            if(s.getName().equals(name))
                return true;
        }
        return false;
    }

    public Set<NetworkNode.TYPE> getNodesWithoutAnyAccess(){
        Set<NetworkNode.TYPE> needsAccess = new HashSet<>();
        Set<NetworkNode.TYPE> nodes = nodeKnowledgeMap.keySet();
        for (NetworkNode.TYPE node: nodes){
            if (!nodeKnowledgeMap.get(node).hasAccessLevelUser() && !nodeKnowledgeMap.get(node).hasAccessLevelRoot()){
                needsAccess.add(node);
            }
        }
        return needsAccess;
    }

    public Set<NetworkNode.TYPE> getNodesWithAnyNodeAccess(){
        Set<NetworkNode.TYPE> needsAccess = new HashSet<>();
        Set<NetworkNode.TYPE> nodes = nodeKnowledgeMap.keySet();
        for (NetworkNode.TYPE node: nodes){
            if (nodeKnowledgeMap.get(node).hasAccessLevelUser() || nodeKnowledgeMap.get(node).hasAccessLevelRoot()){
                needsAccess.add(node);
            }
        }
        return needsAccess;
    }

    public boolean isFinalState(int config){
        Set<NetworkNode.TYPE> expectedRootNodes = new HashSet<>();
        switch (config){
            case CONFIG_ROOT_ONLY:
                expectedRootNodes.add(NetworkNode.TYPE.ADMINPC);
                return hasRootOnRequiredNodes(expectedRootNodes);
            case CONFIG_ROOT_DB:
                expectedRootNodes.add(NetworkNode.TYPE.ADMINPC);
                return hasRootOnRequiredNodes(expectedRootNodes) && hasReadDatabase();
            case CONFIG_ROOT_DB_ACC:
                expectedRootNodes.addAll(Set.of(NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.ADMINPC, NetworkNode.TYPE.DATABASE));
                return  hasRootOnRequiredNodes(expectedRootNodes) &&
                        hasReadDatabase() && hasCreatedAccountOnNode(NetworkNode.TYPE.ADMINPC);
            default: return false;
        }
    }

    private boolean hasFoundNetworkData(){
        Map<Integer, Data> sniffableData = Simulation.getSimWorld().getSniffableData();
        if (!networkKnowledge.getSniffedDataMap().isEmpty()){
            Set<Integer> knowledge = networkKnowledge.getSniffedDataMap().keySet();
            for (int ID : sniffableData.keySet()){
                if (!knowledge.contains(ID)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean hasCreatedAccountOnNode(NetworkNode.TYPE node){
        return (nodeKnowledgeMap.containsKey(node) &&
                nodeKnowledgeMap.get(node).getKnownData().containsKey(AdversaryAction.CREATE_ACC_ID));
    }

    private boolean hasRootOnRequiredNodes (Set<NetworkNode.TYPE> required){
        for (NetworkNode.TYPE node : required){
            if (!nodeKnowledgeMap.containsKey(node) || !nodeKnowledgeMap.get(node).hasAccessLevelRoot()){
                return false;
            }
        }
        return true;
    }

    private boolean hasReadDatabase(){
        Set<Integer> expectedIDs = Simulation.getNodeByType(NetworkNode.TYPE.DATABASE).getDataSet().keySet();
        if (nodeKnowledgeMap.containsKey(NetworkNode.TYPE.DATABASE)){
            Map<Integer, Data> knowledge = nodeKnowledgeMap.get(NetworkNode.TYPE.DATABASE).getKnownData();
            for (int ID: expectedIDs){
                if (!knowledge.containsKey(ID)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isStartState(){
        return startState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State state = (State) o;
        return Objects.equals(nodeKnowledgeMap, state.nodeKnowledgeMap) &&
                Objects.equals(networkKnowledge, state.networkKnowledge) &&
                Objects.equals(softwareKnowledgeMap, state.softwareKnowledgeMap);
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
