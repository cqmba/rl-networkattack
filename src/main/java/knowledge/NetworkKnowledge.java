package knowledge;

import environment.Data;
import environment.NetworkNode;
import knowledge.impl.NetworkKnowledgeImpl;

import java.util.Map;
import java.util.Set;

public interface NetworkKnowledge {
    /**
     * This method adds a new NetworkKnowledge instance
     * @return - a new instance
     */
    static NetworkKnowledge create() {
        return new NetworkKnowledgeImpl();
    }

    /**
     * This method returns a set of all known nodes
     * @return - set of known nodes
     */
    Set<NetworkNode.TYPE> getKnownNodes();

    /**
     * This method adds a new node to this network knowledge.
     * @param node - logical type of the found node
     */
    void addNewNode(NetworkNode.TYPE node);

    /**
     * This method adds new data, which the adversary found in the network.
     * @param data - the data object which was found
     */
    void addSniffedData(Data data);

    /**
     * This method returns a map of all previously found data objects in the network.
     * @return - map of all data
     */
    Map<Integer, Data> getSniffedDataMap();

}
