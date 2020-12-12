package core;


import environment.NetworkNode;
import environment.Software;
import knowledge.NetworkKnowledge;
import knowledge.NodeKnowledge;
import knowledge.SoftwareKnowledge;
import knowledge.impl.NetworkKnowledgeImpl;
import knowledge.impl.NodeKnowledgeImpl;

import java.io.Serializable;
import java.util.*;

public class State implements Serializable {
    private Map<NetworkNode.TYPE, NodeKnowledge> nodeKnowledgeMap;

    private NetworkKnowledge networkKnowledge;
    //used to determine which Node an Action is executed FROM in PostCondition Of Action
    private NetworkNode.TYPE currentActor;
    //Map for the SoftwareKnowledge of the adversary for each NetworkNode
    private Map<NetworkNode.TYPE,Set<SoftwareKnowledge>> softwareKnowledgeMap = new HashMap<>();

    public State() {
        this.nodeKnowledgeMap = new LinkedHashMap<>();
        this.networkKnowledge = new NetworkKnowledgeImpl();
    }

    public static State getStartState(){
        State start = new State();
        start.addNodeKnowledge(NetworkNode.TYPE.ROUTER);
        //todo I just added it for testing I do not know how you intend to use it
        start.currentActor = NetworkNode.TYPE.ADVERSARY;
        return start;
    }

    public static Set<AdversaryAction> computePossibleActions(){
        //TODO
        return null;
    }

    public void addNodeKnowledge(NetworkNode.TYPE node){
        this.networkKnowledge.addNewNode(node);
        this.nodeKnowledgeMap.put(node, new NodeKnowledgeImpl(node));
    }

    public void addNodeHostname(NetworkNode.TYPE node, String hostname){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        nodeKnowledgeMap.replace(node, old.addHostname(hostname));
    }

    public void addNodePubIp(NetworkNode.TYPE node, String pubIp){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        nodeKnowledgeMap.replace(node, old.addPubIp(pubIp));
    }

    public void addNodePrivIp(NetworkNode.TYPE node, String privIp){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        nodeKnowledgeMap.replace(node, old.addPrivIp(privIp));
    }

    public void addNodeRemoteSoftwareName(NetworkNode.TYPE node, String swName){
        NodeKnowledge old = nodeKnowledgeMap.get(node);
        //SoftwareKnowledge swKnowledge = SoftwareKnowledge.addNew(swName);
        if(softwareKnowledgeMap.containsKey(node)) {
            Set<SoftwareKnowledge> softwareKnowledgeSet = softwareKnowledgeMap.get(node);
            //check if the software is already known
            if (!softwareContainedInSet(swName, softwareKnowledgeSet)) {
                softwareKnowledgeSet.add(SoftwareKnowledge.addNew(swName));
                softwareKnowledgeMap.put(node,softwareKnowledgeSet);
                //todo: do we need this?
                nodeKnowledgeMap.replace(node, old.addNewRemoteSoftware(swName));
            }
        }else{
            //for the case that the node is not contained in the map, create entry in the map
            Set<SoftwareKnowledge> softwareKnowledgeSet = new HashSet<>();
            SoftwareKnowledge softwareKnowledge = SoftwareKnowledge.addNew(swName);
            softwareKnowledgeSet.add(softwareKnowledge);
            softwareKnowledgeMap.put(node,softwareKnowledgeSet);
            //todo: do we need this?
            nodeKnowledgeMap.replace(node, old.addNewRemoteSoftware(swName));
        }
    }





    public NetworkKnowledge getNetworkKnowledge() {
        return networkKnowledge;
    }

    public Map<NetworkNode.TYPE, NodeKnowledge> getNodeKnowledgeMap() {
        return nodeKnowledgeMap;
    }

    public NetworkNode.TYPE getCurrentActor() {
        return currentActor;
    }

    public Map<NetworkNode.TYPE, Set<SoftwareKnowledge>> getSoftwareKnowledgeMap() {
        return softwareKnowledgeMap;
    }

    public void setCurrentActor(NetworkNode.TYPE currentActor) {
        this.currentActor = currentActor;
    }

    Boolean softwareContainedInSet(String name , Set<SoftwareKnowledge> softwareKnowledgeSet){
        for(SoftwareKnowledge s : softwareKnowledgeSet){
            if(s.getName().equals(name))
                return true;
        }
        return false;
    }
}
