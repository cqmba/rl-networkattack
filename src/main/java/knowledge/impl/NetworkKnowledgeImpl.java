package knowledge.impl;

import environment.Data;
import environment.NetworkNode;
import environment.Software;
import knowledge.DataKnowledge;
import knowledge.NetworkKnowledge;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NetworkKnowledgeImpl implements NetworkKnowledge, Serializable {
    private Set<NetworkNode.TYPE> nodes;

    public NetworkKnowledgeImpl(){
        this.nodes = new HashSet<>();
    }

    public NetworkKnowledgeImpl(Set<NetworkNode.TYPE> nodes){
        this.nodes = nodes;
    }

    @Override
    public Set<NetworkNode.TYPE> getKnownNodes() {
        return nodes;
    }

    @Override
    public Set<DataKnowledge> getSniffedData() {
        return null;
    }

    @Override
    public Map<NetworkNode.TYPE, List<Software>> getMapOfNodeRemoteSWForNodesWithoutEstablishedSystemAccess() {
        return null;
    }

    @Override
    public NetworkKnowledge addNewNode(NetworkNode.TYPE node) {
        Set<NetworkNode.TYPE> newNodes = new HashSet<>(nodes);
        newNodes.add(node);
        return new NetworkKnowledgeImpl(newNodes);
    }

    @Override
    public void addSniffedData(Data data) {

    }
}
