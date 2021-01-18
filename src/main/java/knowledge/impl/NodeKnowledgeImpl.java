package knowledge.impl;

import environment.Data;
import environment.NetworkNode;
import environment.Software;
import knowledge.NodeKnowledge;

import java.io.Serializable;
import java.util.*;

public class NodeKnowledgeImpl implements NodeKnowledge, Serializable {

    private String pub_ip;
    private String priv_ip;
    private String hostname;
    private String operatingSystem;
    private String osVersion;
    private Set<Software> remoteSoftware;
    private Set<Software> localSoftware;
    private Map<Integer, Data> dataSet;
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
        this.dataSet = new HashMap<>();
        this.sysAccess = NetworkNode.ACCESS_LEVEL.NONE;
        this.remotelyVisibleSWInNetwork = new EnumMap<>(NetworkNode.TYPE.class);
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
        return (sysAccess.equals(NetworkNode.ACCESS_LEVEL.USER));
    }

    @Override
    public boolean hasAccessLevelRoot(){
        return sysAccess.equals(NetworkNode.ACCESS_LEVEL.ROOT);
    }

    @Override
    public boolean hasFoundData(int ID) {
        return dataSet.containsKey(ID);
    }

    @Override
    public Map<Integer, Data> getKnownData() {
        return this.dataSet;
    }

    @Override
    public void addPubIp(String pubIp) {
        this.pub_ip = pubIp;
    }

    @Override
    public void addPrivIp(String privIp) {
        this.priv_ip = privIp;
    }

    @Override
    public void addHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public void addOperationSystem(String os) {
        this.operatingSystem = os;
    }

    @Override
    public void addOSVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    @Override
    public void addAccessLevel(NetworkNode.ACCESS_LEVEL accessLevel) {
        sysAccess = accessLevel;
    }

    @Override
    public void addData(int ID, Data data) {
        dataSet.put(ID, data);
    }

    @Override
    public int hashCode() {

        return Objects.hash(pub_ip, priv_ip, hostname, operatingSystem, osVersion, remoteSoftware, localSoftware, dataSet, sysAccess, remotelyVisibleSWInNetwork, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeKnowledgeImpl)) return false;
        NodeKnowledgeImpl that = (NodeKnowledgeImpl) o;
        return Objects.equals(pub_ip, that.pub_ip) &&
                Objects.equals(priv_ip, that.priv_ip) &&
                Objects.equals(hostname, that.hostname) &&
                Objects.equals(operatingSystem, that.operatingSystem) &&
                Objects.equals(osVersion, that.osVersion) &&
                Objects.equals(remoteSoftware, that.remoteSoftware) &&
                Objects.equals(localSoftware, that.localSoftware) &&
                Objects.equals(dataSet, that.dataSet) &&
                Objects.equals(sysAccess, that.sysAccess) &&
                Objects.equals(remotelyVisibleSWInNetwork, that.remotelyVisibleSWInNetwork) &&
                Objects.equals(type, that.type);
    }
}
