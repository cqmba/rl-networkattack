package environment;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class contains the logical structure of the Simulation network, thus containing all its nodes,
 * setting its network topology and network data.
 */
public class NetworkWorld {

    private final Set<NetworkNode> nodes = new LinkedHashSet<>();
    private Map<Integer, Data> sniffableData = new HashMap<>();
    private final Set<NetworkNode.TYPE> internal = Set.of(NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.ADMINPC, NetworkNode.TYPE.DATABASE);

    public Set<NetworkNode.TYPE> getInternalNodes() {
        return internal;
    }

    public NetworkWorld() {
    }

    public void addNode(NetworkNode node){
        nodes.add(node);
    }

    public void initializeNetworkTopology(){
        for (NetworkNode node: nodes){
            node.computeRemoteSWMap();
        }
    }

    public void setSniffableData(Map<Integer, Data> sniffableData) {
        this.sniffableData = sniffableData;
    }

    public Map<Integer, Data> getSniffableData() {
        return sniffableData;
    }

    public Set<NetworkNode> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
