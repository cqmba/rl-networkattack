package core;


import environment.NetworkNode;
import knowledge.NetworkKnowledge;
import knowledge.NodeKnowledge;
import knowledge.SoftwareKnowledge;
import knowledge.impl.NetworkKnowledgeImpl;

import java.io.Serializable;
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

    public NetworkKnowledge getNetworkKnowledge() {
        return networkKnowledge;
    }

    public Map<NetworkNode.TYPE, NodeKnowledge> getNodeKnowledgeMap() {
        return nodeKnowledgeMap;
    }



    public Map<NetworkNode.TYPE, Set<SoftwareKnowledge>> getSoftwareKnowledgeMap() {
        return softwareKnowledgeMap;
    }



    Boolean softwareContainedInSet(String name , Set<SoftwareKnowledge> softwareKnowledgeSet){
        for(SoftwareKnowledge s : softwareKnowledgeSet){
            if(s.getName().equals(name))
                return true;
        }
        return false;
    }

    public Set<NetworkNode.TYPE> getNodesWithoutSystemAccess(){
        Set<NetworkNode.TYPE> needsAccess = new HashSet<>();
        Set<NetworkNode.TYPE> nodes = nodeKnowledgeMap.keySet();
        for (NetworkNode.TYPE node: nodes){
            if (!nodeKnowledgeMap.get(node).hasAccessLevelUser()){
                needsAccess.add(node);
            }
        }
        return needsAccess;
    }
}
