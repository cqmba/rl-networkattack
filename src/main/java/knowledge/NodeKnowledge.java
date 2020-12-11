package knowledge;

import environment.Data;
import environment.NetworkNode;
import knowledge.impl.NodeKnowledgeImpl;

import java.util.Set;

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

    boolean hasFoundData(Data data);

    Set<SoftwareKnowledge> getLocalSoftwareKnowledge();

    Set<SoftwareKnowledge> getRemoteSoftwareKnowledge();

    boolean isRemoteServiceKnown(String software);

    Set<DataKnowledge> getDataKnowledge();

    NodeKnowledge addPubIp(String pubIp);

    NodeKnowledge addPrivIp(String privIp);

    NodeKnowledge addHostname(String hostname);

    NodeKnowledge addOperationSystem(String os);

    NodeKnowledge addOSVersion(String osVersion);

    NodeKnowledge addAccessLevel(NetworkNode.ACCESS_LEVEL accessLevel);

    NodeKnowledge addData(Data data);

    NodeKnowledge addNewLocalSoftware(String sw);

    NodeKnowledge addNewRemoteSoftware(String sw);

}
