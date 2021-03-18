package knowledge;

import environment.Data;
import environment.NetworkNode;
import knowledge.impl.NodeKnowledgeImpl;

import java.util.Map;

public interface NodeKnowledge {

    /**
     * This method creates a new NodeKnowledge instance for a particular logical node
     * @param node - logical node type which was found
     * @return - a new instance
     */
    static NodeKnowledge addNode(NetworkNode.TYPE node) {
        return new NodeKnowledgeImpl(node);
    }

    boolean hasPubIp();

    boolean hasPrivIp();

    boolean hasHostname();

    boolean hasOperatingSystem();

    boolean hasOSVersion();

    boolean hasAccessLevelUser();

    boolean hasAccessLevelRoot();

    Map<Integer, Data> getKnownData();

    void addPubIp(String pubIp);

    void addPrivIp(String privIp);

    void addHostname(String hostname);

    void addOperationSystem(String os);

    void addOSVersion(String osVersion);

    void addAccessLevel(NetworkNode.ACCESS_LEVEL accessLevel);

    void addData(int ID, Data data);
}
