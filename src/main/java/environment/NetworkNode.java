package environment;

import java.util.Set;

public class NetworkNode {
    private String pub_ip = "";
    private String priv_ip = "";
    private String hostname = "";
    private String operatingSystem = "";
    private String osVersion = "";
    private Set<Software> remoteSoftware;
    private Set<Software> localSoftware;
    private Set<Data> dataSet;

    public NetworkNode(String pub_ip, String priv_ip, String hostname, String operatingSystem, String osVersion, Set<Software> remoteSoftware, Set<Software> localSoftware, Set<Data> dataSet) {
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

    public Set<Data> getDataSet() {
        return dataSet;
    }
}
