package knowledge.impl;

import environment.Data;
import environment.NetworkNode;
import environment.Software;
import knowledge.DataKnowledge;
import knowledge.NetworkKnowledge;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class NetworkKnowledgeImpl implements NetworkKnowledge {

    @Override
    public Set<NetworkNode.TYPE> getKnownNodes() {
        return null;
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
    public void addNewNode(NetworkNode.TYPE node) {

    }

    @Override
    public void addSniffedData(Data data) {

    }
}
