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

    void addNewNode(NetworkNode.TYPE node);

    void addSniffedData(Data data);

    Map<Integer, Data> getSniffedDataMap();

}
