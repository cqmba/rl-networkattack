package knowledge;

import environment.Data;
import environment.NetworkNode;
import knowledge.impl.NodeKnowledgeImpl;

import java.util.Map;

public interface NodeKnowledge {

    //use this if the Adversary found a new yet unknown node
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
