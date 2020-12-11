package environment;

import java.util.LinkedHashSet;
import java.util.Set;

public class NetworkWorld {

    private Set<NetworkNode> nodes = new LinkedHashSet<>();
    private Set<Data> sniffableData = new LinkedHashSet<>();

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

    public Set<Data> getSniffableData() {
        return sniffableData;
    }

    public Set<NetworkNode> getNodes() {
        return nodes;
    }
}
