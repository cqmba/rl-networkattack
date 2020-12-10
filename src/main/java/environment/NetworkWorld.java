package environment;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class NetworkWorld {
    //TODO do we need IDs map or is set sufficient?
    public static final Integer ROUTER_ID=1;
    public static final Integer WEBSERVER_ID=2;
    public static final Integer ADMINPC_ID=3;
    public static final Integer DB_ID=4;
    private Set<NetworkNode> nodes = new LinkedHashSet<>();
    private Map<Integer, NetworkNode> networkLookup = new HashMap<>();
    private Set<Data> sniffableData = new LinkedHashSet<>();

    public NetworkWorld() {
    }

    public void addNode(Integer id, NetworkNode node){
        nodes.add(node);
        networkLookup.put(id, node);
    }

    public void initializeNetworkTopology(){
        for (NetworkNode node: nodes){
            node.computeRemoteSWMap();
        }
    }

    public Set<Data> getSniffableData() {
        return sniffableData;
    }

    public Set<NetworkNode> getNodes() {
        return nodes;
    }
}
