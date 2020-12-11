package knowledge;

import environment.Data;
import environment.NetworkNode;
import environment.Software;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NetworkKnowledge {

    Set<NetworkNode.TYPE> getKnownNodes();

    Set<Data> getSniffedData();

    Map<NetworkNode.TYPE, List<Software>> getMapOfNodeRemoteSWForNodesWithoutEstablishedSystemAccess();

    void addNewNode(NetworkNode.TYPE node);

    void addSniffedData(Data data);
}
