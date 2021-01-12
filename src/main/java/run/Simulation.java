package run;

import core.AdversaryAction;
import core.State;
import environment.*;
import visualize.SimpleActionsPrint;
import visualize.SimpleNetworkPrint;
import visualize.SimpleStatePrint;

import java.util.*;
import java.util.function.Predicate;

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

    public static final int MAX_DATA_PER_HOST = 3;

    private static NetworkWorld simWorld = new NetworkWorld();
    private static State state = State.getStartState();

    public static void main(String[] args) {
        System.out.println("Starting simulation");
        setupWorld();
        SimpleNetworkPrint.print(simWorld);
        SimpleStatePrint.print(state);
        Set<State> states = State.computeListOfPossibleStates(state);
        NetworkNode.TYPE currentActor = NetworkNode.TYPE.ADVERSARY;

        //for now do this manually
        List<NetworkNode.TYPE> targets = new ArrayList<>();
        List<AdversaryAction> actions = new ArrayList<>();
        actions.add(0, AdversaryAction.ACTIVE_SCAN_IP_PORT);
        targets.add(0, NetworkNode.TYPE.ROUTER);
        actions.add(1, AdversaryAction.ACTIVE_SCAN_VULNERABILITY);
        targets.add(1, NetworkNode.TYPE.WEBSERVER);
        actions.add(2, AdversaryAction.ACTIVE_SCAN_VULNERABILITY);
        targets.add(2, NetworkNode.TYPE.ADMINPC);
        //actions.add(3, AdversaryAction.EXPLOIT_PUBLIC_FACING_APPLICATION);
        //targets.add(3, NetworkNode.TYPE.WEBSERVER);
        actions.add(3, AdversaryAction.EXPLOIT_FOR_CLIENT_EXECUTION);
        targets.add(3, NetworkNode.TYPE.WEBSERVER);
        actions.add(4, AdversaryAction.EXPLOIT_FOR_PRIVILEGE_ESCALATION);
        targets.add(4, NetworkNode.TYPE.WEBSERVER);
        //now from Webserver
        actions.add(5, AdversaryAction.ACTIVE_SCAN_IP_PORT);
        targets.add(5, NetworkNode.TYPE.ROUTER);
        actions.add(6, AdversaryAction.ACTIVE_SCAN_VULNERABILITY);
        targets.add(6, NetworkNode.TYPE.ROUTER);
        actions.add(7, AdversaryAction.DATA_FROM_LOCAL_SYSTEM);
        targets.add(7, NetworkNode.TYPE.WEBSERVER);
        actions.add(8,AdversaryAction.DATA_FROM_LOCAL_SYSTEM);
        targets.add(8, NetworkNode.TYPE.WEBSERVER);
        for (int i=0; i<actions.size();i++){
            AdversaryAction action = actions.get(i);
            //Assume we have Webserver root control
            if (i>=5){
                currentActor = NetworkNode.TYPE.WEBSERVER;
            }
            printPossibleActions(currentActor);
            printPerformAction(action, targets.get(i));
            state = State.performGivenAction(state, action, targets.get(i), currentActor);
            SimpleStatePrint.print(state);
        }

        printPossibleActions(currentActor);
    }

    public static void setupWorld(){
        //add Router, currently no Software
        Map<Integer, Data> routerData = new LinkedHashMap<>();
        Set<Software> routerSW = new LinkedHashSet<>();
        simWorld.addNode(new NetworkNode(NetworkNode.TYPE.ROUTER, PUB_IP, ROUTER_PRIV_IP, ROUTER_HOSTNAME,ROUTER_OS, ROUTER_OS_VERSION, routerSW, routerSW, routerData));
        //add Webserver
        Set<Software> wsSWLocal = new LinkedHashSet<>();
        simWorld.addNode(new NetworkNode(NetworkNode.TYPE.WEBSERVER, PUB_IP, WEBSERVER_PRIV_IP, WEBSERVER_HOSTNAME, NODE_OS, NODE_OS_VERSION, setWebserverRemoteSW(), wsSWLocal, setWebserverData()));
        //add Admin PC
        Set<Software> adminSWLocal = new LinkedHashSet<>();
        simWorld.addNode(new NetworkNode(NetworkNode.TYPE.ADMINPC, PUB_IP, ADMINPC_PRIV_IP, ADMINPC_HOSTNAME, NODE_OS, NODE_OS_VERSION, setAdminPCRemoteSW(), adminSWLocal, setAdminPCData()));
        //add Database
        Set<Software> dbSWLocal = new LinkedHashSet<>();
        simWorld.addNode(new NetworkNode(NetworkNode.TYPE.DATABASE, PUB_IP, DB_PRIV_IP, DB_HOSTNAME, NODE_OS, NODE_OS_VERSION, setDBRemoteSW(), dbSWLocal, setDBData()));
        //should add data here that can be sniffed in the network
        simWorld.initializeNetworkTopology();
        simWorld.setSniffableData(getNetworkData());
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
        return remoteSW;
    }

    static Map<Integer, Data> setWebserverData(){
        //get 2 unique random IDs
        Random r = new Random();
        int pass_id = r.nextInt(MAX_DATA_PER_HOST-1);
        int ssh_id;
        do {
            ssh_id = r.nextInt(MAX_DATA_PER_HOST-1);
        }
        while (pass_id == ssh_id);
        //create some data that can be used for priv escalation on the webserver locally (for now uses software Ubuntu)
        Map<Integer, Data> wsData = new LinkedHashMap<>();
        Credentials passwordfile = new Credentials(Credentials.TYPE.PASSWORD_FILE, Credentials.ACCESS_GRANT_LEVEL.ROOT, WEBSERVER_PRIV_IP, NODE_OS, NetworkNode.TYPE.WEBSERVER);
        wsData.put(pass_id, new Data(pass_id, passwordfile, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.USER));
        //create data for ssh access (key) to admin pc
        Credentials ssh_key = new Credentials(Credentials.TYPE.KEY, Credentials.ACCESS_GRANT_LEVEL.ROOT, ADMINPC_PRIV_IP, SERVICE_SSH, NetworkNode.TYPE.ADMINPC);
        wsData.put(ssh_id, new Data(ssh_id, ssh_key, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.USER));
        //fill some less usefull data
        for (int i=0;i<MAX_DATA_PER_HOST; i++){
            if (i == pass_id || i == ssh_id){
                //do nothing
            }else if (i/2==0){
                wsData.put(i, new Data(i, Data.GAINED_KNOWLEDGE.LOW, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.USER));
            } else {
                wsData.put(i, new Data(i, Data.GAINED_KNOWLEDGE.NONE, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.USER));
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
        return remoteSW;
    }

    static Map<Integer, Data> setAdminPCData(){
        //unique random IDs
        int ssh_id = new Random().nextInt(MAX_DATA_PER_HOST-1);
        //AdminPC contains mostly high value data
        Map<Integer, Data> adminData = new LinkedHashMap<>();
        //create data for ssh access (key) to webserver
        Credentials ssh_key = new Credentials(Credentials.TYPE.KEY, Credentials.ACCESS_GRANT_LEVEL.USER, WEBSERVER_PRIV_IP, SERVICE_SSH, NetworkNode.TYPE.WEBSERVER);
        adminData.put(ssh_id, new Data(ssh_id, ssh_key, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
        //fill some additional data
        for (int i=0;i<MAX_DATA_PER_HOST; i++){
            if (i == ssh_id){
                //do nothing
            }
            if (i/2==0){
                adminData.put(i, new Data(i, Data.GAINED_KNOWLEDGE.LOW, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
            } else {
               adminData.put(i, new Data(i, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
            }
        }
        return adminData;
    }

    static Set<Software> setDBRemoteSW(){
        //TODO need to define behaviour of authorization bypass, since currently its mapped to VALID_ACCOUNT exploit
        Set<Software> remoteSW = new LinkedHashSet<>();
        Software mySQL = new Software(SERVICE_MYSQL, "8.0.13");
        mySQL.addVulnerability(new Vulnerability("CVE-2019-2534", Vulnerability.TYPE.BYPASS_AUTHORIZATION, false));
        remoteSW.add(mySQL);
        return remoteSW;
    }

    static Map<Integer, Data> setDBData(){
        //Database contains mostly high value data
        Map<Integer, Data> dbData = new LinkedHashMap<>();
        for (int i=0;i<MAX_DATA_PER_HOST; i++){
            if (i/2==0){
                dbData.put(i, new Data(i, Data.GAINED_KNOWLEDGE.LOW, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
            } else {
                dbData.put(i, new Data(i, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
            }
        }
        return dbData;
    }

    public static NetworkWorld getSimWorld() {
        return simWorld;
    }

    public static NetworkNode getNodeByType(NetworkNode.TYPE type){
        Predicate<NetworkNode> isRealNode = node -> node.getType().equals(type);
        Optional<NetworkNode> first = simWorld.getNodes().stream().filter(isRealNode).findFirst();
        if (first.isPresent()) {
            return first.get();
        }else throw new RuntimeException("Node doesnt exist in Simulation");
    }

    private static void printPerformAction(AdversaryAction action, NetworkNode.TYPE target){
        System.out.println("........Performing ACTION "+action+" on "+target+"..........");
    }

    private static void printPossibleActions(NetworkNode.TYPE currentActor){
        Map<AdversaryAction, Set<NetworkNode.TYPE>> adversaryActionSetMap = State.computePossibleActions(state,currentActor);
        SimpleActionsPrint.print(adversaryActionSetMap, currentActor);
    }

    private static Map<Integer, Data> getNetworkData(){
        Map<Integer, Data> networkData = new HashMap<>();
        //currently only DB password is sniffable
        //get 2 unique random IDs
        int db_pw_id = new Random().nextInt(MAX_DATA_PER_HOST-1);
        for (int i=0;i<MAX_DATA_PER_HOST; i++){
            if (i == db_pw_id){
                //do nothing
            }
            if (i/2==0){
                networkData.put(i, new Data(i, Data.GAINED_KNOWLEDGE.LOW, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
            } else {
                networkData.put(i, new Data(i, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
            }
        }
        return networkData;
    }
}
