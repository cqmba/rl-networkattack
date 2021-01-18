package core;

import environment.*;
import knowledge.NodeKnowledge;
import knowledge.SoftwareKnowledge;
import run.Simulation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum AdversaryAction {
    ACTIVE_SCAN_IP_PORT{
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> scannableNodes = new HashSet<>();
            if (currentState.isStartState()){
                scannableNodes.add(NetworkNode.TYPE.ROUTER);
            }else {
                scannableNodes.addAll(getViewableNodes(currentActor));
            }
            Map<NetworkNode.TYPE, NodeKnowledge> knowledgeMap = currentState.getNodeKnowledgeMap();
            Predicate<NetworkNode.TYPE> isNewScannable = node -> !knowledgeMap.containsKey(node)
                    || !knowledgeMap.get(node).hasPubIp()
                    || (knowledgeMap.containsKey(node) && Simulation.getSimWorld().getInternalNodes().contains(currentActor) && !knowledgeMap.get(node).hasPrivIp());
            return scannableNodes.stream().filter(isNewScannable).collect(Collectors.toSet());
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            NetworkNode node = Simulation.getNodeByType(target);
            State newState = (State) deepCopy(currentState);
            if (currentState.isStartState()){
                newState.setStartState(false);
            }
            Set<NetworkNode.TYPE> knownNodes = currentState.getNetworkKnowledge().getKnownNodes();
            if (!knownNodes.contains(target)){
                newState.addNodeKnowledge(target);
            }
            if (!newState.getNodeKnowledgeMap().get(target).hasPubIp()){
                newState.addNodePubIp(target, node.getPub_ip());
            }
            if (!newState.getNodeKnowledgeMap().get(target).hasHostname()){
                newState.addNodeHostname(target, node.getHostname());
            }
            //implement router port forwarding
            if (target.equals(NetworkNode.TYPE.ROUTER)){
                for (NetworkNode.TYPE relayTargets: Set.of(NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.ADMINPC)){
                    if (!knownNodes.contains(relayTargets)){
                        newState.addNodeKnowledge(relayTargets);
                        newState.addNodePubIp(relayTargets, node.getPub_ip());
                        //I dont think we see private IP on an IP Scan, only if we get system access
                        newState.addNodeHostname(relayTargets, node.getHostname());
                    }
                }
            }
            Set<NetworkNode.TYPE> internal = Simulation.getSimWorld().getInternalNodes();
            //Get all the visible remote software FROM the node, where the scan was executed from
            Map<NetworkNode.TYPE, Set<Software>> remotelyVisibleSWInNetwork = NetworkTopology.getRemoteSWMapByScanningNode(currentActor);
            //when scanning on router from outside, get port forwarded sw only
            if (target.equals(NetworkNode.TYPE.ROUTER) && newState.getNodeKnowledgeMap().get(NetworkNode.TYPE.ROUTER).hasPrivIp()){
                return newState;
            }else if (target.equals(NetworkNode.TYPE.ROUTER)){
                if (internal.contains(currentActor)){
                    if (!newState.getNodeKnowledgeMap().get(target).hasPrivIp()){
                        newState.addNodePrivIp(target, node.getPriv_ip());
                    }
                    return newState;
                }
                if (!internal.contains(currentActor)){
                    Set<NetworkNode.TYPE> targets = Set.of(NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.ADMINPC);
                    for (NetworkNode.TYPE scanned:targets){
                        for (Software software:remotelyVisibleSWInNetwork.get(scanned)){
                            //TODO no check if sw is new
                            newState.addNodeRemoteSoftwareName(scanned, software.getName(), true);
                        }
                    }
                    return newState;
                }
            }else if(internal.contains(currentActor)){
                if (!newState.getNodeKnowledgeMap().get(target).hasPrivIp()){
                    newState.addNodePrivIp(target, node.getPriv_ip());
                }
                addRemoteSw(remotelyVisibleSWInNetwork, target, newState);
            }else {
                addRemoteSw(remotelyVisibleSWInNetwork, target, newState);
            }

            return newState;
        }

        private void addRemoteSw(Map<NetworkNode.TYPE, Set<Software>> remotelyVisibleSWInNetwork, NetworkNode.TYPE target, State newState){
            for (Software sw: remotelyVisibleSWInNetwork.get(target)){
                if (!newState.getSoftwareKnowledgeMap().containsKey(target) || !newState.softwareContainedInSet(sw.getName(), newState.getSoftwareKnowledgeMap().get(target))){
                    newState.addNodeRemoteSoftwareName(target, sw.getName(), true);
                }
            }
        }
    },
    ACTIVE_SCAN_VULNERABILITY {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> targets = new HashSet<>();
            for (NetworkNode.TYPE host: currentState.getNetworkKnowledge().getKnownNodes()){
                //software has to beknown of the host and it has to be remote (currently ROUTER wont be a target, but WS & AdminPC
                if (currentState.getSoftwareKnowledgeMap().containsKey(host)
                        && currentState.getSoftwareKnowledgeMap().get(host).stream().anyMatch(SoftwareKnowledge::isRemote)){
                    targets.add(host);
                }
            }
            if (!currentState.isStartState()){
                targets.add(NetworkNode.TYPE.ROUTER);
            }
            return targets;
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            NetworkNode actualTarget = Simulation.getNodeByType(target);
            NodeKnowledge knownNode = newState.getNodeKnowledgeMap().get(target);
            //TODO Router OS info can be read, but actual software scanning is bypassed for now and doesnt work as relay
            if (target.equals(NetworkNode.TYPE.ROUTER)){
                if (!knownNode.hasOperatingSystem()){
                    newState.addNodeOS(target, actualTarget.getOperatingSystem());
                }
                if (!knownNode.hasOSVersion()){
                    newState.addNodeOSVersion(target, actualTarget.getOsVersion());
                }
                return newState;
            }
            Set<SoftwareKnowledge> knownSoftware = newState.getSoftwareKnowledgeMap().get(target);
            // add to every software we know the version and the vulnerabilities
            addVersionAndVulnerabilities(actualTarget.getLocalSoftware(), knownSoftware);
            addVersionAndVulnerabilities(actualTarget.getRemoteSoftware(), knownSoftware);
            if (!knownNode.hasOperatingSystem()){
                newState.addNodeOS(target, actualTarget.getOperatingSystem());
            }
            if (!knownNode.hasOSVersion()){
                newState.addNodeOSVersion(target, actualTarget.getOsVersion());
            }
            return newState;
        }

        private void addVersionAndVulnerabilities(Set<Software> actualSoftware, Set<SoftwareKnowledge> knownSoftware) {
            for(Software s : actualSoftware){
                SoftwareKnowledge foundSw = findSoftwareByName(knownSoftware,s.getName());
                if(foundSw!=null){
                    if (!foundSw.hasVersion()){
                        foundSw.addVersion(s.getVersion());
                    }
                    if (foundSw.getVulnerabilities().isEmpty()){
                        foundSw.addVulnerabilities(s.getVulnerabilities());
                    }
                }
            }
        }

    },
    EXPLOIT_PUBLIC_FACING_APPLICATION {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> viewableNodes = getViewableNodes(currentActor);
            Set<NetworkNode.TYPE> attackableNodes = new HashSet<>();
            Set<NetworkNode.TYPE> nodes = currentState.getSoftwareKnowledgeMap().keySet();
            for(NetworkNode.TYPE node: nodes){
                //check if we could attack the node from our current location
                if(viewableNodes.contains(node)) {
                    for (SoftwareKnowledge softwareKnowledge : currentState.getSoftwareKnowledgeMap().get(node)) {
                        for (Vulnerability v : softwareKnowledge.getVulnerabilities()) {
                            if (v.getExploitType().equals(Exploit.TYPE.EXPLOIT_PUBLIC_FACING_APPLICATION)) {
                                attackableNodes.add(node);
                            }
                        }
                    }
                }
            }
            //only keep those, which we actually need
            attackableNodes.retainAll(currentState.getNodesWithoutAnyAccess());
            return attackableNodes;
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            // check if we have not root access so we do not override it
            if(!newState.getNodeKnowledgeMap().get(target).hasAccessLevelRoot())
                newState.getNodeKnowledgeMap().get(target).addAccessLevel(NetworkNode.ACCESS_LEVEL.USER);
            //learn own IP if new
            if (!newState.getNodeKnowledgeMap().get(target).hasPrivIp()){
                newState.addNodePrivIp(target, Simulation.getNodeByType(target).getPriv_ip());
            }

            return newState;
        }
    },
    VALID_ACCOUNTS {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> viewableNodes = getViewableNodes(currentActor);
            Set<NetworkNode.TYPE> nodesWithCredentials = new HashSet<>();
            Map<NetworkNode.TYPE, NodeKnowledge> knownNodes = currentState.getNodeKnowledgeMap();
            //we use certain exploits
            Set<NetworkNode.TYPE> nodes = currentState.getSoftwareKnowledgeMap().keySet();
            for(NetworkNode.TYPE node: nodes){
                //check if we could attack the node from our current location
                if(viewableNodes.contains(node)) {
                    for (SoftwareKnowledge softwareKnowledge : currentState.getSoftwareKnowledgeMap().get(node)) {
                        for (Vulnerability v : softwareKnowledge.getVulnerabilities()) {
                            if (v.getExploitType().equals(Exploit.TYPE.VALID_ACCOUNTS)) {
                                //TODO no check if systemaccess achieved yet
                                nodesWithCredentials.add(node);
                            }
                        }
                    }
                }
            }
            //we can also use valid acc locally for priv esc (password file)
            viewableNodes.add(currentActor);
            //we can use found credentials
            Map<Integer, Data> sniffedDataMap = currentState.getNetworkKnowledge().getSniffedDataMap();
            Set<Data> knownData = new HashSet<>();
            if (!sniffedDataMap.isEmpty()){
                knownData.addAll(sniffedDataMap.values());
            }
            for(NetworkNode.TYPE node : knownNodes.keySet()) {
                knownData.addAll(knownNodes.get(node).getKnownData().values());
            }
            Set<Credentials> credentials = getAllCredentialsFromData(knownData);
            if (!credentials.isEmpty()){
                for (Credentials creds: credentials){
                    //debugging
                    if (creds.getNode().equals(NetworkNode.TYPE.DATABASE)){
                        System.out.println("");
                    }
                    NetworkNode.TYPE accessibleNode = creds.getNode();
                    //changed to make privilege escalation possible
                    //TODO will keep saying yes for nodes if root is already achieved, as long as creds give root
                    if (viewableNodes.contains(accessibleNode) && knownNodes.containsKey(accessibleNode)
                            && (currentState.getNodesWithoutAnyAccess().contains(accessibleNode)
                            || creds.getAccessGrantLevel().equals(Credentials.ACCESS_GRANT_LEVEL.ROOT))){
                        nodesWithCredentials.add(accessibleNode);
                    }
                }
            }
            return nodesWithCredentials;
        }

        private Set<Credentials> getAllCredentialsFromData(Set<Data> dataSet){
            Set<Credentials> credentialsSet = new HashSet<>();
            for (Data data: dataSet){
                if (data.containsCredentials()){
                    credentialsSet.add(data.getCredentials());
                }
            }
            return credentialsSet;
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            Map<NetworkNode.TYPE, NodeKnowledge> map = newState.getNodeKnowledgeMap();
            NodeKnowledge targetNodeKnowledge = map.get(target);
            //we can also use sniffed Data
            Map<Integer, Data> sniffedDataMap = currentState.getNetworkKnowledge().getSniffedDataMap();
            Set<Data> knownData = new HashSet<>();
            if (!sniffedDataMap.isEmpty()){
                knownData.addAll(sniffedDataMap.values());
            }
            for(NetworkNode.TYPE node : map.keySet()) {
                knownData.addAll(map.get(node).getKnownData().values());
            }
            Set<Credentials> credentials = getAllCredentialsFromData(knownData);
            for (Credentials creds : credentials) {
                Credentials.ACCESS_GRANT_LEVEL acLevel = creds.getAccessGrantLevel();
                if (!targetNodeKnowledge.hasAccessLevelRoot()) {
                    if (acLevel == Credentials.ACCESS_GRANT_LEVEL.ROOT) {
                        targetNodeKnowledge.addAccessLevel(NetworkNode.ACCESS_LEVEL.ROOT);
                    } else {
                        targetNodeKnowledge.addAccessLevel(NetworkNode.ACCESS_LEVEL.USER);
                    }
                }
            }
            //or learned by auth bypass/credential leak
            //TODO can/should DB ever get root through auth bypass/ cred leak
            if(!targetNodeKnowledge.hasAccessLevelRoot())
                targetNodeKnowledge.addAccessLevel(NetworkNode.ACCESS_LEVEL.USER);
            //learn own IP if new
            if (!targetNodeKnowledge.hasPrivIp()){
                newState.addNodePrivIp(target, Simulation.getNodeByType(target).getPriv_ip());
            }
            return newState;
        }
    },
    EXPLOIT_FOR_CLIENT_EXECUTION {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> viewableNodes = getViewableNodes(currentActor);
            Set<NetworkNode.TYPE> attackableNodes = new HashSet<>();
            for(NetworkNode.TYPE node: currentState.getSoftwareKnowledgeMap().keySet()){
                //check if we could attack the node from our current location
                if(viewableNodes.contains(node)) {
                    for (SoftwareKnowledge softwareKnowledge : currentState.getSoftwareKnowledgeMap().get(node)) {
                        for (Vulnerability v : softwareKnowledge.getVulnerabilities()) {
                            if (v.getExploitType().equals(Exploit.TYPE.EXPLOIT_FOR_CLIENT_EXECUTION)) {
                                attackableNodes.add(node);
                            }
                        }
                    }
                }
            }
            return attackableNodes;
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            // check if we have not root access so we do not override it
            if(!newState.getNodeKnowledgeMap().get(target).hasAccessLevelRoot())
                newState.getNodeKnowledgeMap().get(target).addAccessLevel(NetworkNode.ACCESS_LEVEL.USER);
            //learn own IP if new
            if (!newState.getNodeKnowledgeMap().get(target).hasPrivIp()){
                newState.addNodePrivIp(target, Simulation.getNodeByType(target).getPriv_ip());
            }
            return newState;
        }
    },
    CREATE_ACCOUNT {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> attackableNodes = new HashSet<>();
            for(NetworkNode.TYPE node : currentState.getNodeKnowledgeMap().keySet()){
                //check if we have  root access on the node
                if(!currentActor.equals(NetworkNode.TYPE.ADVERSARY)&&currentActor.equals(node) && currentState.getNodeKnowledgeMap().get(node).hasAccessLevelRoot()){
                    attackableNodes.add(node);
                }
            }
            return attackableNodes;
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            //create new credentials
            Data data = new Data(CREATE_ACC_ID, new Credentials(Credentials.TYPE.KEY,Credentials.ACCESS_GRANT_LEVEL.ROOT,"","",target),Data.GAINED_KNOWLEDGE.HIGH,Data.ORIGIN.CREATED,Data.ACCESS_REQUIRED.ROOT);
            NetworkNode node = Simulation.getNodeByType(target);
            //add credentials to node
            //TODO this changes the environment! is that ok?
            node.getDataSet().put(CREATE_ACC_ID, data);
            //add credentials to node knowledge
            newState.getNodeKnowledgeMap().get(target).getKnownData().put(CREATE_ACC_ID, data);
            return newState;
        }
    },
    EXPLOIT_FOR_PRIVILEGE_ESCALATION {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> viewableNodes = getViewableNodes(currentActor);
            Set<NetworkNode.TYPE> attackableNodes = new HashSet<>();
            for(NetworkNode.TYPE node: currentState.getSoftwareKnowledgeMap().keySet()){
                //check if we could attack the node from our current location
                if(viewableNodes.contains(node)) {
                    for (SoftwareKnowledge softwareKnowledge : currentState.getSoftwareKnowledgeMap().get(node)) {
                        for (Vulnerability v : softwareKnowledge.getVulnerabilities()) {
                            //check if we have user access on the node
                            if (v.getExploitType().equals(Exploit.TYPE.EXPLOIT_FOR_PRIVILEGE_ESCALATION) && currentState.getNodeKnowledgeMap().get(node).hasAccessLevelUser()) {
                                attackableNodes.add(node);
                            }
                        }
                    }
                }
            }
            return attackableNodes;
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            newState.getNodeKnowledgeMap().get(target).addAccessLevel(NetworkNode.ACCESS_LEVEL.ROOT);
            return newState;
        }
    },
    MAN_IN_THE_MIDDLE {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            //define that node has to selftarget here
            if (currentState.getNodesWithAnyNodeAccess().contains(currentActor) && !currentActor.equals(NetworkNode.TYPE.ADVERSARY)){
                return Set.of(currentActor);
            }else return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            Set<Integer> knownSniffedData = currentState.getNetworkKnowledge().getSniffedDataMap().keySet();
            Map<Integer, Data> actualDataMap = Simulation.getSimWorld().getSniffableData();
            for (int ID: actualDataMap.keySet()){
                if (!knownSniffedData.contains(ID)){
                    newState.addNetworkData(actualDataMap.get(ID));
                }
            }
            return newState;
        }
    },
    SOFTWARE_DISCOVERY {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            return getAttackableNodesOnSysAccess(currentState, currentActor);
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            NetworkNode node = Simulation.getNodeByType(target);
            // check if node is already in our software knowledge map
            if(newState.getSoftwareKnowledgeMap().containsKey(node.getType())) {
                Set<SoftwareKnowledge> softwareKnowledgeSet = newState.getSoftwareKnowledgeMap().get(target);
                for(Software s : node.getLocalSoftware()){
                    //get software knowledge if already exist for a specific softare
                    SoftwareKnowledge softwareKnowledge =AdversaryAction.findSoftwareByName(softwareKnowledgeSet, s.getName());
                    if(softwareKnowledge!=null){
                        softwareKnowledge.addVersion(s.getVersion());
                        softwareKnowledge.addVulnerabilities(s.getVulnerabilities());
                    }else{
                        softwareKnowledge = SoftwareKnowledge.addNew(s.getName(), false);
                        softwareKnowledge.addVersion(s.getVersion());
                        softwareKnowledge.addVulnerabilities(s.getVulnerabilities());
                    }
                }
            }else{
                Set<SoftwareKnowledge> softwareKnowledgeSet = new HashSet<>();
                for(Software s : node.getLocalSoftware()){
                    SoftwareKnowledge softwareKnowledge = SoftwareKnowledge.addNew(s.getName(), false);
                    softwareKnowledge.addVersion(s.getVersion());
                    softwareKnowledge.addVulnerabilities(s.getVulnerabilities());
                    softwareKnowledgeSet.add(softwareKnowledge);
                }
                newState.getSoftwareKnowledgeMap().put(target,softwareKnowledgeSet);
            }
            return newState;
        }
    },
    DATA_FROM_LOCAL_SYSTEM {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            return getAttackableNodesOnSysAccess(currentState, currentActor);
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            NodeKnowledge targetKnowledge = newState.getNodeKnowledgeMap().get(target);
            Set<Integer> knowndataSet = targetKnowledge.getKnownData().keySet();
            //assume ID always increases by one and starts with 0
            Map<Integer, Data> actualDataMap = Simulation.getNodeByType(target).getDataSet();
            for(int ID : actualDataMap.keySet()){
                if (!knowndataSet.contains(ID)){
                    //should add if either root, or only user is required
                    if(targetKnowledge.hasAccessLevelRoot() || actualDataMap.get(ID).getAccess().equals(Data.ACCESS_REQUIRED.USER)){
                        newState.addNodeData(target, ID, actualDataMap.get(ID));
                    }
                }
            }
            return newState;
        }
    };

    private static final Set<AdversaryAction> _actions = new LinkedHashSet<>();

    public static Set<AdversaryAction> allActions(){
        return _actions;
    }

    static {
        _actions.add(ACTIVE_SCAN_IP_PORT);
        _actions.add(ACTIVE_SCAN_VULNERABILITY);
        _actions.add(EXPLOIT_PUBLIC_FACING_APPLICATION);
        _actions.add(EXPLOIT_FOR_CLIENT_EXECUTION);
        _actions.add(EXPLOIT_FOR_PRIVILEGE_ESCALATION);
        _actions.add(VALID_ACCOUNTS);
        _actions.add(DATA_FROM_LOCAL_SYSTEM);
        _actions.add(CREATE_ACCOUNT);
        _actions.add(MAN_IN_THE_MIDDLE);
        _actions.add(SOFTWARE_DISCOVERY);
    }

    public static final int CREATE_ACC_ID = 9999;

    public abstract Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor);
    public abstract State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor);

    public static SoftwareKnowledge findSoftwareByName(Set<SoftwareKnowledge> softwareKnowledgeSet, String swName){
        for(SoftwareKnowledge softwareKnowledge: softwareKnowledgeSet){
            if(softwareKnowledge.getName().equals(swName))
                return softwareKnowledge;
        }
        return null;
    }

    public Set<NodeAction> getActionsWhichFulfillPrecondition(State currentState) {
        Set<NodeAction> nodeActions = new HashSet<>();
        Set<NetworkNode.TYPE> nodesWithAccess = currentState.getSetOfSystemWithAcess();
        for(NetworkNode.TYPE node: nodesWithAccess) {
            Set<NetworkNode.TYPE> targets = getTargetsWhichFulfillPrecondition(currentState, node);
            for(NetworkNode.TYPE target: targets){
                nodeActions.add(new NodeAction(target,node,this));
            }
        }
        return nodeActions;
    }

    private static Set<NetworkNode.TYPE> getViewableNodes(NetworkNode.TYPE currentActor){
        Set<NetworkNode.TYPE> viewableNodeTypes = new HashSet<>();
        Predicate<NetworkNode> isConnected = node -> NetworkTopology.getConnectedHosts(currentActor).contains(node.getType());
        Set<NetworkNode> viewableNodes = Simulation.getSimWorld().getNodes().stream().filter(isConnected).collect(Collectors.toSet());
        for (NetworkNode n : viewableNodes) {
            viewableNodeTypes.add(n.getType());
        }
        return viewableNodeTypes;
    }

    private static Set<NetworkNode.TYPE> getAttackableNodesOnSysAccess(State currentState, NetworkNode.TYPE currentActor) {
        Set<NetworkNode.TYPE> attackableNodes = new HashSet<>();
        //check if we have access on the node
        if (!currentActor.equals(NetworkNode.TYPE.ADVERSARY) && currentState.getNodeKnowledgeMap().containsKey(currentActor) && (currentState.getNodeKnowledgeMap().get(currentActor).hasAccessLevelUser() || currentState.getNodeKnowledgeMap().get(currentActor).hasAccessLevelRoot())) {
            attackableNodes.add(currentActor);
        }

        return attackableNodes;
    }

    /**
     * Makes a deep copy of any Java object that is passed.
     */
    private static Object deepCopy(Object object) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
            outputStrm.writeObject(object);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
            return objInputStream.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
