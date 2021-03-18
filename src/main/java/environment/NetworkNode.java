package environment;

import com.google.gson.Gson;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * This class implements a single hosts configuration
 */
public class NetworkNode{
    private final String pub_ip;
    private final String priv_ip;
    private final String hostname;
    private final String operatingSystem;
    private final String osVersion;
    private final Set<Software> remoteSoftware;
    private Set<Software> localSoftware;
    private final Map<Integer, Data> dataSet;
    //this maps the remote visible software of other nodes, that can be accessed by this node
    private Map<NetworkNode.TYPE, Set<Software>> remotelyVisibleSWInNetwork = new EnumMap<>(TYPE.class);
    //could be hidden
    private final TYPE type;

    public enum TYPE {
        ROUTER,
        WEBSERVER,
        ADMINPC,
        DATABASE,
        ADVERSARY
    }

    public enum ACCESS_LEVEL{
        NONE,
        USER,
        ROOT
    }

    public NetworkNode(NetworkNode.TYPE type, String pub_ip, String priv_ip, String hostname, String operatingSystem, String osVersion, Set<Software> remoteSoftware, Set<Software> localSoftware, Map<Integer, Data> dataSet) {
        this.type = type;
        this.pub_ip = pub_ip;
        this.priv_ip = priv_ip;
        this.hostname = hostname;
        this.operatingSystem = operatingSystem;
        this.osVersion = osVersion;
        this.remoteSoftware = remoteSoftware;
        this.localSoftware = localSoftware;
        this.dataSet = dataSet;
    }

    public String getPub_ip() {
        return pub_ip;
    }

    public String getPriv_ip() {
        return priv_ip;
    }

    public String getHostname() {
        return hostname;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public Set<Software> getRemoteSoftware() {
        return remoteSoftware;
    }

    public Set<Software> getLocalSoftware() {
        return localSoftware;
    }

    public Map<Integer, Data> getDataSet() {
        return dataSet;
    }

    public TYPE getType() {
        return type;
    }

    public void setLocalSoftware(Set<Software> localSoftware) {
        this.localSoftware = localSoftware;
    }

    public Map<TYPE, Set<Software>> getRemotelyVisibleSWInNetwork() {
        return remotelyVisibleSWInNetwork;
    }

    public void computeRemoteSWMap(){
        this.remotelyVisibleSWInNetwork = NetworkTopology.getRemoteSWMapByScanningNode(type);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
