package knowledge;

import environment.Data;
import environment.NetworkNode;
import environment.Software;

public interface NodeKnowledge {
    boolean hasPubIp();

    boolean hasPrivIp();

    boolean hasHostname();

    boolean hasOperatingSystem();

    boolean hasOSVersion();

    boolean hasAccessLevelUser();

    boolean hasAccessLevelRoot();

    boolean hasFoundData(Data data);

    boolean hasFoundRemoteNode(NetworkNode.TYPE node);

    boolean hasFoundSoftwareOnRemoteNode(NetworkNode.TYPE node, Software sw);

}
