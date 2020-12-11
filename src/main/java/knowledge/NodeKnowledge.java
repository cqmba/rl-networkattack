package knowledge;

import environment.Data;
import environment.NetworkNode;
import environment.Software;

public interface NodeKnowledge {

    //use this if the Adversary found a new yet unknown node
    NodeKnowledgeImpl addNode(NetworkNode.TYPE node);

    boolean hasPubIp();

    boolean hasPrivIp();

    boolean hasHostname();

    boolean hasOperatingSystem();

    boolean hasOSVersion();

    boolean hasAccessLevelUser();

    boolean hasAccessLevelRoot();

    boolean hasFoundData(Data data);

    void addPubIp(String pubIp);

    void addPrivIp(String privIp);

    void addHostname(String hostname);

    void addOperationSystem(String os);

    void addOSVersion(String osVersion);

    void addAccessLevel(NetworkNode.ACCESS_LEVEL accessLevel);

    void addData(Data data);

}
