package knowledge.impl;

import com.google.gson.Gson;
import environment.Data;
import environment.NetworkNode;
import knowledge.NetworkKnowledge;

import java.io.Serializable;
import java.util.*;

/**
 * This class implements all knowledge of the adversary about the network,
 * which includes which nodes are included and what data is in the network.
 */
public class NetworkKnowledgeImpl implements NetworkKnowledge, Serializable {
    private final Map<Integer, Data> sniffedData;
    private final Set<NetworkNode.TYPE> nodes;

    public NetworkKnowledgeImpl(){
        this.nodes = new HashSet<>();
        this.sniffedData = new HashMap<>();
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

        return Objects.hash(nodes, sniffedData);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
