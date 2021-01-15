package knowledge.impl;

import environment.Data;
import environment.NetworkNode;
import knowledge.NetworkKnowledge;

import java.io.Serializable;
import java.util.*;

public class NetworkKnowledgeImpl implements NetworkKnowledge, Serializable {
    private Map<Integer, Data> sniffedData;
    private Set<NetworkNode.TYPE> nodes;

    public NetworkKnowledgeImpl(){
        this.nodes = new HashSet<>();
        this.sniffedData = new HashMap<>();
    }

    public NetworkKnowledgeImpl(Set<NetworkNode.TYPE> nodes){
        this.nodes = nodes;
    }

    @Override
    public Set<NetworkNode.TYPE> getKnownNodes() {
        return nodes;
    }

    @Override
    public void addNewNode(NetworkNode.TYPE node) {
        nodes.add(node);
    }

    @Override
    public void addSniffedData(Data data) {
        sniffedData.put(data.getID(), data);
    }

    @Override
    public Map<Integer, Data> getSniffedDataMap() {
        return sniffedData;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkKnowledgeImpl)) return false;
        NetworkKnowledgeImpl that = (NetworkKnowledgeImpl) o;
        return Objects.equals(sniffedData, that.sniffedData) &&
                Objects.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {

        return Objects.hash(nodes);
    }
}
