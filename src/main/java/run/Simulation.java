package run;

import environment.*;
import visualize.SimpleNetworkPrint;

import java.util.*;

public class Simulation {
    private static final String PUB_IP = "79.1.1.100";
    private static final String ROUTER_PRIV_IP = "10.1.1.1";
    private static final String WEBSERVER_PRIV_IP = "10.1.1.2";
    private static final String ADMINPC_PRIV_IP = "10.1.1.3";
    private static final String DB_PRIV_IP = "10.1.1.4";

    private static final String ROUTER_HOSTNAME = "router.simworld.com";
    private static final String WEBSERVER_HOSTNAME = "webserver.simworld.com";
    private static final String ADMINPC_HOSTNAME = "admin.simworld.com";
    private static final String DB_HOSTNAME = "database.simworld.com";

    private static final String ROUTER_OS = "OpenWRT";
    private static final String ROUTER_OS_VERSION = "18.06.0";
    private static final String NODE_OS = "Ubuntu";
    private static final String NODE_OS_VERSION = "LTS 16.04.07";

    public static final String SERVICE_SSH = "openssh-server";
    public static final String SERVICE_MYSQL = "Oracle MySQL Server";
    public static final String SERVICE_TELNET = "telnetd";
    public static final String SERVICE_HTTP = "HTTP";
    public static final String SERVICE_HTTPS = "HTTPS/TLS";
    public static final String SERVICE_PHP = "PHP";
    public static final String SERVICE_NGINX = "nginx";


    private static NetworkWorld simWorld = new NetworkWorld();
    private static Set<Software> swRepo = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Starting simulation");
        setupWorld();
        simWorld.initializeNetworkTopology();
        SimpleNetworkPrint.print(simWorld);
    }

    static void setupWorld(){
        //add Router, currently no Software
        Set<Data> routerData = new LinkedHashSet<>();
        Set<Software> routerSW = new LinkedHashSet<>();
        simWorld.addNode(NetworkWorld.ROUTER_ID, new NetworkNode(NetworkNode.TYPE.ROUTER, PUB_IP, ROUTER_PRIV_IP, ROUTER_HOSTNAME,ROUTER_OS, ROUTER_OS_VERSION, routerSW, routerSW, routerData));
        //add Webserver
        Set<Software> wsSWLocal = new LinkedHashSet<>();
        simWorld.addNode(NetworkWorld.WEBSERVER_ID, new NetworkNode(NetworkNode.TYPE.WEBSERVER, PUB_IP, WEBSERVER_PRIV_IP, WEBSERVER_HOSTNAME, NODE_OS, NODE_OS_VERSION, setWebserverRemoteSW(), wsSWLocal, setWebserverData()));
        //add Admin PC
        Set<Software> adminSWLocal = new LinkedHashSet<>();
        simWorld.addNode(NetworkWorld.ADMINPC_ID, new NetworkNode(NetworkNode.TYPE.ADMINPC, PUB_IP, ADMINPC_PRIV_IP, ADMINPC_HOSTNAME, NODE_OS, NODE_OS_VERSION, setAdminPCRemoteSW(), adminSWLocal, setAdminPCData()));
        //add Database
        Set<Software> dbSWLocal = new LinkedHashSet<>();
        simWorld.addNode(NetworkWorld.DB_ID, new NetworkNode(NetworkNode.TYPE.DATABASE, PUB_IP, DB_PRIV_IP, DB_HOSTNAME, NODE_OS, NODE_OS_VERSION, setDBRemoteSW(), dbSWLocal, setDBData()));
        //should add data here that can be sniffed in the network
    }

    static Set<Software> setWebserverRemoteSW(){
        Set<Software> remoteSW = new LinkedHashSet<>();
        //assume Webapp access with leaked credentials grants system access for now
        Software http = new Software(SERVICE_HTTP, "2");
        http.addVulnerability(new Vulnerability("", Vulnerability.TYPE.CREDENTIAL_LEAK, false));
        remoteSW.add(http);
        Software https = new Software(SERVICE_HTTPS, "1.3");
        https.addVulnerability(new Vulnerability("", Vulnerability.TYPE.CREDENTIAL_LEAK, false));
        remoteSW.add(https);

        //vulnerable
        Software ssh = new Software(SERVICE_SSH, "7.3");
        ssh.addVulnerability(new Vulnerability("", Vulnerability.TYPE.CREDENTIAL_LEAK, false));
        ssh.addVulnerability(new Vulnerability("CVE-2016-10012", Vulnerability.TYPE.PRIVILEGE_ESCALATION, false));
        remoteSW.add(ssh);

        Software php = new Software(SERVICE_PHP, "7.1.19");
        php.addVulnerability(new Vulnerability("CVE-2016-1247", Vulnerability.TYPE.REMOTE_CODE_EXECUTION, false));
        php.addVulnerability(new Vulnerability("", Vulnerability.TYPE.CODE_INJECTION, false));
        remoteSW.add(php);

        Software nginx = new Software(SERVICE_NGINX, "1.09");
        nginx.addVulnerability(new Vulnerability("CVE-2016-1247", Vulnerability.TYPE.PRIVILEGE_ESCALATION, false));
        remoteSW.add(nginx);
        swRepo.addAll(remoteSW);
        return remoteSW;
    }

    static Set<Data> setWebserverData(){
        //create some data that can be used for priv escalation on the webserver locally (for now uses software Ubuntu)
        Set<Data> wsData = new LinkedHashSet<>();
        Credentials passwordfile = new Credentials(Credentials.TYPE.PASSWORD_FILE, Credentials.ACCESS_GRANT_LEVEL.ROOT, WEBSERVER_PRIV_IP, NODE_OS);
        wsData.add(new Data(passwordfile, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.USER));
        //create data for ssh access (key) to admin pc
        Credentials ssh_key = new Credentials(Credentials.TYPE.KEY, Credentials.ACCESS_GRANT_LEVEL.ROOT, ADMINPC_PRIV_IP, SERVICE_SSH);
        wsData.add(new Data(ssh_key, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.USER));
        //fill some less usefull data
        for (int i=0;i<10; i++){
            if (i/2==0){
                wsData.add(new Data(Data.GAINED_KNOWLEDGE.LOW, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.USER));
            } else {
                wsData.add(new Data(Data.GAINED_KNOWLEDGE.NONE, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.USER));
            }
        }
        return wsData;
    }

    static Set<Software> setAdminPCRemoteSW(){
        Set<Software> remoteSW = new LinkedHashSet<>();
        Software telnetd = new Software(SERVICE_TELNET, "0.17");
        telnetd.addVulnerability(new Vulnerability("CVE-2020-10188", Vulnerability.TYPE.REMOTE_CODE_EXECUTION, false));
        //add zeroday because why not
        telnetd.addVulnerability(new Vulnerability("", Vulnerability.TYPE.REMOTE_CODE_EXECUTION, true));
        remoteSW.add(telnetd);
        Software ssh = new Software(SERVICE_SSH, "7.3");
        ssh.addVulnerability(new Vulnerability("CVE-2016-10012", Vulnerability.TYPE.PRIVILEGE_ESCALATION, false));
        ssh.addVulnerability(new Vulnerability("", Vulnerability.TYPE.CREDENTIAL_LEAK, false));
        remoteSW.add(ssh);
        swRepo.addAll(remoteSW);
        return remoteSW;
    }

    static Set<Data> setAdminPCData(){
        //AdminPC contains mostly high value data
        Set<Data> adminData = new LinkedHashSet<>();
        //create data for ssh access (key) to webserver
        Credentials ssh_key = new Credentials(Credentials.TYPE.KEY, Credentials.ACCESS_GRANT_LEVEL.USER, WEBSERVER_PRIV_IP, SERVICE_SSH);
        adminData.add(new Data(ssh_key, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
        //fill some additional data
        for (int i=0;i<10; i++){
            if (i/2==0){
                adminData.add(new Data(Data.GAINED_KNOWLEDGE.LOW, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
            } else {
               adminData.add(new Data(Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
            }
        }
        return adminData;
    }

    static Set<Software> setDBRemoteSW(){
        Set<Software> remoteSW = new LinkedHashSet<>();
        Software mySQL = new Software(SERVICE_MYSQL, "8.0.13");
        mySQL.addVulnerability(new Vulnerability("CVE-2019-2534", Vulnerability.TYPE.BYPASS_AUTHORIZATION, false));
        remoteSW.add(mySQL);
        swRepo.addAll(remoteSW);
        return remoteSW;
    }

    static Set<Data> setDBData(){
        //Database contains mostly high value data
        Set<Data> dbData = new LinkedHashSet<>();
        for (int i=0;i<20; i++){
            if (i/2==0){
                dbData.add(new Data(Data.GAINED_KNOWLEDGE.LOW, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
            } else {
                dbData.add(new Data(Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
            }
        }
        return dbData;
    }

    public static NetworkWorld getSimWorld() {
        return simWorld;
    }

    public static Set<Software> getSwRepo() {
        return swRepo;
    }
}
