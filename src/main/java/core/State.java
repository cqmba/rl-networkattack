package core;


import environment.NetworkNode;
import knowledge.NetworkKnowledge;
import knowledge.NodeKnowledge;
import knowledge.impl.NetworkKnowledgeImpl;
import knowledge.impl.NodeKnowledgeImpl;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class State implements Serializable {
    private Map<NetworkNode.TYPE, NodeKnowledge> nodeKnowledgeMap;
    private NetworkKnowledge networkKnowledge;
    //used to determine which Node an Action is executed FROM in PostCondition Of Action
    private NetworkNode.TYPE currentActor;

    public State() {
        this.nodeKnowledgeMap = new LinkedHashMap<>();
        this.networkKnowledge = new NetworkKnowledgeImpl();
    }

    public static State getStartState(){
        State start = new State();
        start.addNodeKnowledge(NetworkNode.TYPE.ROUTER);
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
        nodeKnowledgeMap.replace(node, old.addNewRemoteSoftware(swName));
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
}
