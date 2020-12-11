package knowledge;

import environment.Data;
import environment.NetworkNode;
import environment.Software;

import java.util.*;

//implement attacker knowledge here, could also be an interface maybe, we only need checking functions
public class NodeKnowledgeImpl implements NodeKnowledge {

    private String pub_ip;
    private String priv_ip;
    private String hostname;
    private String operatingSystem;
    private String osVersion;
    private Set<Software> remoteSoftware;
    private Set<Software> localSoftware;
    private Set<Data> dataSet;
    private NetworkNode.ACCESS_LEVEL sysAccess;
    //this maps the remote visible software of other nodes, that can be accessed by this node
    private Map<NetworkNode.TYPE, List<Software>> remotelyVisibleSWInNetwork;
    //could be hidden
    private NetworkNode.TYPE type;

    public NodeKnowledgeImpl(NetworkNode.TYPE type) {
        this.pub_ip = "";
        this.priv_ip = "";
        this.hostname = "";
        this.operatingSystem = "";
        this.osVersion = "";
        this.remoteSoftware = new HashSet<>();
        this.localSoftware = new HashSet<>();
        this.dataSet = new HashSet<>();
        this.sysAccess = NetworkNode.ACCESS_LEVEL.NONE;
        this.remotelyVisibleSWInNetwork = new HashMap<>();
        //could also add new unknown type
        this.type = type;
    }

    @Override
    public boolean hasPubIp(){
        return !pub_ip.isEmpty();
    }

    @Override
    public boolean hasPrivIp(){
        return !priv_ip.isEmpty();
    }

    @Override
    public boolean hasHostname(){
        return !hostname.isEmpty();
    }

    @Override
    public boolean hasOperatingSystem(){
        return !operatingSystem.isEmpty();
    }

    @Override
    public boolean hasOSVersion(){
        return !osVersion.isEmpty();
    }

    @Override
    public boolean hasAccessLevelUser(){
        return (sysAccess.equals(NetworkNode.ACCESS_LEVEL.USER) || sysAccess.equals(NetworkNode.ACCESS_LEVEL.ROOT));
    }

    @Override
    public boolean hasAccessLevelRoot(){
        return sysAccess.equals(NetworkNode.ACCESS_LEVEL.ROOT);
    }

    @Override
    public boolean hasFoundData(Data data) {
        return false;
    }

    @Override
    public boolean hasFoundRemoteNode(NetworkNode.TYPE node) {
        return false;
    }

    @Override
    public boolean hasFoundSoftwareOnRemoteNode(NetworkNode.TYPE node, Software sw) {
        return false;
    }
    //need checks for software, date etc, delegate to Software Knowledge
}
