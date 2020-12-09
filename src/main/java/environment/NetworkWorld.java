package environment;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class NetworkWorld {
    private Set<NetworkNode> nodes = new LinkedHashSet<>();
    private Map<Integer, NetworkNode> networkLookup = new HashMap<>();
    private Set<Data> sniffableData = new LinkedHashSet<>();

    public NetworkWorld() {
    }

    public Integer addNode(NetworkNode node){
        Integer newSize = networkLookup.size()+1;
        nodes.add(node);
        networkLookup.put(newSize, node);
        return newSize;
    }

    public void addConfig(Integer nodeID){
        networkLookup.get(nodeID);
    }

    public Set<Data> getSniffableData() {
        return sniffableData;
    }

    public Set<NetworkNode> getNodes() {
        return nodes;
    }
}
