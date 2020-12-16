package knowledge.impl;

import environment.Data;
import environment.NetworkNode;
import environment.Software;
import knowledge.DataKnowledge;
import knowledge.NetworkKnowledge;

import java.io.Serializable;
import java.util.*;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkKnowledgeImpl that = (NetworkKnowledgeImpl) o;
        return Objects.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {

        return Objects.hash(nodes);
    }
}
