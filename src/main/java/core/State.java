package core;


import com.google.gson.Gson;
import environment.Data;
import environment.NetworkNode;
import knowledge.NetworkKnowledge;
import knowledge.NodeKnowledge;
import knowledge.SoftwareKnowledge;
import run.Simulation;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Implements the state of the knowledge of the adversary, thus is used as state object in the MDP.
 */
public class State implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(State.class.getName());
    private final Map<NetworkNode.TYPE, NodeKnowledge> nodeKnowledgeMap;
    private boolean startState;
    private final NetworkKnowledge networkKnowledge;
    //Map for the SoftwareKnowledge of the adversary for each NetworkNode
    private final Map<NetworkNode.TYPE,Set<SoftwareKnowledge>> softwareKnowledgeMap = new EnumMap<>(NetworkNode.TYPE.class);

    public State(boolean startState) {
        this.nodeKnowledgeMap = new LinkedHashMap<>();
        this.networkKnowledge = NetworkKnowledge.create();
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
        if(softwareKnowledgeMap.containsKey(node)) {
            Set<SoftwareKnowledge> softwareKnowledgeSet = softwareKnowledgeMap.get(node);
            //check if the software is already known
            if (!isSoftwareContainedInSet(swName, softwareKnowledgeSet)) {
                Set<SoftwareKnowledge> newKnowledge = new HashSet<>(softwareKnowledgeSet);
                newKnowledge.add(SoftwareKnowledge.addNew(swName, remote));
                softwareKnowledgeMap.replace(node, newKnowledge);
            }
        }else{
            //for the case that the node is not contained in the map, create entry in the map
            Set<SoftwareKnowledge> swSet = new HashSet<>();
            swSet.add(SoftwareKnowledge.addNew(swName, remote));
            softwareKnowledgeMap.put(node, swSet);
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

    public NetworkKnowledge getNetworkKnowledge() {
        return networkKnowledge;
    }

    public Map<NetworkNode.TYPE, NodeKnowledge> getNodeKnowledgeMap() {
        return nodeKnowledgeMap;
    }

    public Map<NetworkNode.TYPE, Set<SoftwareKnowledge>> getSoftwareKnowledgeMap() {
        return softwareKnowledgeMap;
    }

    /**
     * This method computes all possible states.
     * @param startState - start state
     * @return - set of all possible states
     */
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
                    if (newSetofStates != null){
                        newSetofStates.add(a.action.executePostConditionOnTarget(a.target,s,a.currentActor));
                    }
                }

            }
            states = newSetofStates;
            LOGGER.info(String.valueOf(previousNumber_of_States));
        }
        return states;
    }

    boolean isSoftwareContainedInSet(String name , Set<SoftwareKnowledge> softwareKnowledgeSet){
        for(SoftwareKnowledge s : softwareKnowledgeSet){
            if(s.getName().equals(name))
                return true;
        }
        return false;
    }

    public Set<NetworkNode.TYPE> getNodesWithoutAnyAccess(){
        Set<NetworkNode.TYPE> needsAccess = new HashSet<>(nodeKnowledgeMap.keySet());
        needsAccess.removeAll(getNodesWithAnyNodeAccess());
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

    /**
     * Checks if the state is final according to the requirements explained in the report
     * @return - true if state is final
     */
    public boolean isFinalState(){
        Set<NetworkNode.TYPE> expectedRootNodes = new HashSet<>(Set.of(NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.ADMINPC, NetworkNode.TYPE.DATABASE));
        return hasRootOnRequiredNodes(expectedRootNodes)
                && hasReadDatabase()
                && knowsNetwork();
    }

    /**
     * Checks if the network was scanned sufficiently according to final state requirements.
     * @return - true if scanned
     */
    public boolean knowsNetwork(){
        Set<NetworkNode.TYPE> nodes = Set.of(NetworkNode.TYPE.ROUTER, NetworkNode.TYPE.ADMINPC, NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.DATABASE);
        for (NetworkNode.TYPE node: nodes){
            if (!nodeKnowledgeMap.containsKey(node) || !nodeKnowledgeMap.get(node).hasPrivIp()
                    || !nodeKnowledgeMap.get(node).hasHostname() || !nodeKnowledgeMap.get(node).hasPubIp()
                    || !nodeKnowledgeMap.get(node).hasOperatingSystem()){
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if root access was acquired on the necessary nodes to conform with final state requirements
     * @param required - the required logical nodes
     * @return - true if root access was acquired
     */
    public boolean hasRootOnRequiredNodes (Set<NetworkNode.TYPE> required){
        for (NetworkNode.TYPE node : required){
            if (!nodeKnowledgeMap.containsKey(node) || !nodeKnowledgeMap.get(node).hasAccessLevelRoot()){
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the database was read by the adversary conforming to final state requirements.
     * @return - true if db was read
     */
    public boolean hasReadDatabase(){
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

    @Override
    public String toString() {
        return new Gson().toJson(this);
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
