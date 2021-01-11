package knowledge;

import environment.Data;
import environment.NetworkNode;
import environment.Software;
import knowledge.impl.NetworkKnowledgeImpl;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NetworkKnowledge {

    static NetworkKnowledge addNew() {
        return new NetworkKnowledgeImpl();
    }

    Set<NetworkNode.TYPE> getKnownNodes();

    Map<NetworkNode.TYPE, List<Software>> getMapOfNodeRemoteSWForNodesWithoutEstablishedSystemAccess();

    void addNewNode(NetworkNode.TYPE node);

    void addSniffedData(Data data);



}
