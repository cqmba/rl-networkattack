package run;

import core.AdversaryAction;
import core.State;
import environment.*;
import visualize.SimpleActionsPrint;
import visualize.SimpleNetworkPrint;
import visualize.SimpleStatePrint;


import java.io.*;
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

    private static NetworkWorld simWorld = new NetworkWorld();
    private static State state = State.getStartState();

    private static boolean preconditionFilterEnabled;

    public static void main(String[] args) throws IOException {
        System.out.println("Starting simulation");
        setupWorld(true);
        computeStates();
        //chooseRandomStatesUntilEnd();
    }

    private static void chooseRandomStatesUntilEnd(){
        List<Choice> choiceList = new ArrayList<>();
        while (!state.isFinalState() && !state.isFailedState()){
            for (NetworkNode.TYPE actor: state.getNodesWithAnyNodeAccess()){
                Map<AdversaryAction, Set<NetworkNode.TYPE>> actions = State.computePossibleActions(state, actor);
                for (AdversaryAction action: actions.keySet()){
                    for (NetworkNode.TYPE target: actions.get(action)){
                        choiceList.add(new Choice(target, actor, action));
                    }
                }
            }
            Collections.shuffle(choiceList);
            //execute random Action
            Choice randomChoice = choiceList.get(0);
            state = State.performGivenAction(Simulation.state, randomChoice.action, randomChoice.target, randomChoice.actor);
            choiceList.clear();
            System.out.println("........Actor: "+randomChoice.actor+" Performing ACTION "+randomChoice.action+" on "+randomChoice.target+"..........");
        }
        System.out.println("Is failed state "+state.isFailedState());
        System.out.println("Is final state "+state.isFinalState());
    }

    static class Choice{
        public NetworkNode.TYPE target;
        public NetworkNode.TYPE actor;
        public AdversaryAction action;

        public Choice (NetworkNode.TYPE target, NetworkNode.TYPE actor, AdversaryAction action){
            this.target = target;
            this.actor = actor;
            this.action = action;
        }
    }

    private static void choseStatesManually(){
        //SimpleNetworkPrint.print(simWorld);
        //SimpleStatePrint.print(state);
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
        actions.add(7, AdversaryAction.ACTIVE_SCAN_IP_PORT);
        targets.add(7, NetworkNode.TYPE.DATABASE);
        actions.add(8,AdversaryAction.ACTIVE_SCAN_VULNERABILITY);
        targets.add(8, NetworkNode.TYPE.DATABASE);
        for (int i=0; i<actions.size();i++){
            AdversaryAction action = actions.get(i);
            //Assume we have Webserver root control
            if (i>=5){
                currentActor = NetworkNode.TYPE.WEBSERVER;
            }
            printPossibleActions(currentActor);
            printPerformAction(action, targets.get(i));
            state = State.performGivenAction(state, action, targets.get(i), currentActor);
            //SimpleStatePrint.print(state);
        }

        printPossibleActions(currentActor);
    }

    private static void computeStates() throws IOException {
        Set<State> states = State.computeListOfPossibleStates(state);
        int states_nr = states.size();
        int config_0 = 0;
        int failed = 0;
        int rootNodes= 0;
        int knownNetw = 0;
        int createdAdmin = 0;
        int createdDB = 0;
        int readDB = 0;
        int zerodayUsed = 0;
        for (State state: states){
            if (state.isFinalState()){
                config_0++;
            }
            if (state.isFailedState()){
                failed++;
            }
            if (state.knowsNetwork()){
                knownNetw++;
            }
            if (state.hasRootOnRequiredNodes(Set.of(NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.ADMINPC, NetworkNode.TYPE.DATABASE))){
                rootNodes++;
            }
            if (state.hasCreatedAccountOnNode(NetworkNode.TYPE.ADMINPC)){
                createdAdmin++;
            }
            if (state.hasCreatedAccountOnNode(NetworkNode.TYPE.DATABASE)){
                createdDB++;
            }
            if (state.hasReadDatabase()){
                readDB++;
            }
            if (state.isZerodayUsed()){
                zerodayUsed++;
            }
        }
        System.out.println("State count: "+states_nr+"\nAdmin Root only States: "
                +config_0+"\nFailed States: "+failed+"\nKnown Netw: "+knownNetw+"\nRoot Nodes: "+rootNodes
                +"\nCreated Admin: "+createdAdmin+"\nCreated DB: "+createdDB+"\nRead DB: "+readDB
                +"\nZeroday Used: "+zerodayUsed);


        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        try {
            fout = new FileOutputStream("states.ser");
            oos = new ObjectOutputStream(fout);
            oos.writeObject(states);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fout != null)
                fout.close();
            if (oos != null)
                oos.close();
        }
    }

    public static void setupWorld(boolean filterEnabled){
        preconditionFilterEnabled = filterEnabled;
        //add Router, currently no Software
        Map<Integer, Data> routerData = new HashMap<>();
        Set<Software> routerSW = new HashSet<>();
        simWorld.addNode(new NetworkNode(NetworkNode.TYPE.ROUTER, PUB_IP, ROUTER_PRIV_IP, ROUTER_HOSTNAME,ROUTER_OS, ROUTER_OS_VERSION, routerSW, routerSW, routerData));
        //add Webserver
        Set<Software> wsSWLocal = new HashSet<>();
        simWorld.addNode(new NetworkNode(NetworkNode.TYPE.WEBSERVER, PUB_IP, WEBSERVER_PRIV_IP, WEBSERVER_HOSTNAME, NODE_OS, NODE_OS_VERSION, setWebserverRemoteSW(), wsSWLocal, setWebserverData()));
        //add Admin PC
        Set<Software> adminSWLocal = new HashSet<>();
        simWorld.addNode(new NetworkNode(NetworkNode.TYPE.ADMINPC, PUB_IP, ADMINPC_PRIV_IP, ADMINPC_HOSTNAME, NODE_OS, NODE_OS_VERSION, setAdminPCRemoteSW(), adminSWLocal, setAdminPCData()));
        //add Database
        Set<Software> dbSWLocal = new HashSet<>();
        simWorld.addNode(new NetworkNode(NetworkNode.TYPE.DATABASE, PUB_IP, DB_PRIV_IP, DB_HOSTNAME, NODE_OS, NODE_OS_VERSION, setDBRemoteSW(), dbSWLocal, setDBData()));
        //should add data here that can be sniffed in the network
        simWorld.initializeNetworkTopology();
        simWorld.setSniffableData(getNetworkData());
    }

    static Set<Software> setWebserverRemoteSW(){
        Set<Software> remoteSW = new HashSet<>();
        //assume Webapp access with leaked credentials grants system access for now
        Software http = new Software(SERVICE_HTTP, "2");
        Software https = new Software(SERVICE_HTTPS, "1.3");
        //vulnerable
        Software ssh = new Software(SERVICE_SSH, "7.3");
        //https.addVulnerability(new Vulnerability("", Vulnerability.TYPE.CREDENTIAL_LEAK, false));
        //http.addVulnerability(new Vulnerability("", Vulnerability.TYPE.CREDENTIAL_LEAK, false));
        //ssh.addVulnerability(new Vulnerability("", Vulnerability.TYPE.CREDENTIAL_LEAK, false));
        ssh.addVulnerability(new Vulnerability("CVE-2016-10012", Vulnerability.TYPE.PRIVILEGE_ESCALATION, false));
        remoteSW.add(http);
        remoteSW.add(https);
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
        //create some data that can be used for priv escalation on the webserver locally (for now uses software Ubuntu)
        Map<Integer, Data> wsData = new HashMap<>();
        Credentials passwordfile = new Credentials(Credentials.TYPE.PASSWORD_FILE, Credentials.ACCESS_GRANT_LEVEL.ROOT, WEBSERVER_PRIV_IP, NODE_OS, NetworkNode.TYPE.WEBSERVER);
        wsData.put(0, new Data(0, passwordfile, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.USER));
        //create data for ssh access (key) to admin pc
        Credentials ssh_key = new Credentials(Credentials.TYPE.KEY, Credentials.ACCESS_GRANT_LEVEL.ROOT, ADMINPC_PRIV_IP, SERVICE_SSH, NetworkNode.TYPE.ADMINPC);
        wsData.put(1, new Data(1, ssh_key, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
        return wsData;
    }

    static Set<Software> setAdminPCRemoteSW(){
        Set<Software> remoteSW = new HashSet<>();
        Software telnetd = new Software(SERVICE_TELNET, "0.17");
        telnetd.addVulnerability(new Vulnerability("CVE-2020-10188", Vulnerability.TYPE.REMOTE_CODE_EXECUTION, false));
        //add zeroday because why not
        //telnetd.addVulnerability(new Vulnerability("", Vulnerability.TYPE.REMOTE_CODE_EXECUTION, true));
        remoteSW.add(telnetd);
        Software ssh = new Software(SERVICE_SSH, "7.3");
        ssh.addVulnerability(new Vulnerability("CVE-2016-10012", Vulnerability.TYPE.PRIVILEGE_ESCALATION, false));
        ssh.addVulnerability(new Vulnerability("", Vulnerability.TYPE.BYPASS_AUTHORIZATION, true));
        //ssh.addVulnerability(new Vulnerability("", Vulnerability.TYPE.CREDENTIAL_LEAK, false));
        remoteSW.add(ssh);
        return remoteSW;
    }

    static Map<Integer, Data> setAdminPCData(){
        int ssh_id = 0;
        //AdminPC contains mostly high value data
        Map<Integer, Data> adminData = new HashMap<>();
        //create data for ssh access (key) to webserver
        Credentials ssh_key = new Credentials(Credentials.TYPE.KEY, Credentials.ACCESS_GRANT_LEVEL.USER, WEBSERVER_PRIV_IP, SERVICE_SSH, NetworkNode.TYPE.WEBSERVER);
        adminData.put(ssh_id, new Data(ssh_id, ssh_key, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
        return adminData;
    }

    static Set<Software> setDBRemoteSW(){
        //TODO need to define behaviour of authorization bypass, since currently its mapped to VALID_ACCOUNT exploit
        Set<Software> remoteSW = new HashSet<>();
        Software mySQL = new Software(SERVICE_MYSQL, "8.0.13");
        mySQL.addVulnerability(new Vulnerability("CVE-2019-2534", Vulnerability.TYPE.BYPASS_AUTHORIZATION, false));
        remoteSW.add(mySQL);
        return remoteSW;
    }

    static Map<Integer, Data> setDBData(){
        //Database contains mostly high value data
        Map<Integer, Data> dbData = new HashMap<>();
        dbData.put(0, new Data(0, Data.GAINED_KNOWLEDGE.LOW, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.ROOT));
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
        Credentials db_cred = new Credentials(Credentials.TYPE.PASSWORD_FILE, Credentials.ACCESS_GRANT_LEVEL.ROOT, DB_PRIV_IP, SERVICE_MYSQL, NetworkNode.TYPE.DATABASE);
        Map<Integer, Data> networkData = new HashMap<>();
        int db_pw_id = 0;
        Data cred_data = new Data(db_pw_id, db_cred, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.SNIFFED, Data.ACCESS_REQUIRED.ALL);
        networkData.put(db_pw_id, cred_data);
        return networkData;
    }

    public static boolean isPreconditionFilterEnabled() {
        return preconditionFilterEnabled;
    }
}
