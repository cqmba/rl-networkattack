package core;

import environment.*;
import knowledge.NodeKnowledge;
import knowledge.SoftwareKnowledge;
import run.Simulation;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class implements all Actions (ATT&CK techniques) available to the adversary. For each technique both pre- & postcondition
 * are implemented from the abstract methods. The techniques specification is adapted from the MITRE ATT&CK description and explained
 * in the report.
 */
public enum AdversaryAction implements Serializable {
    ACTIVE_SCAN_IP_PORT{
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> scannableNodes = new HashSet<>();
            if (currentState.isStartState()){
                scannableNodes.add(NetworkNode.TYPE.ROUTER);
            }else {
                scannableNodes.addAll(getViewableNodes(currentActor));
            }
            if (!Simulation.isPreconditionFilterEnabled()){
                return scannableNodes;
            }
            return getTargetsWhereActionResultsInStateChange(scannableNodes, currentState, currentActor);
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            NetworkNode node = Simulation.getNodeByType(target);
            State newState = (State) deepCopy(currentState);
            assert newState != null;
            if (currentState.isStartState()){
                newState.setStartState(false);
            }
            Set<NetworkNode.TYPE> knownNodes = newState.getNetworkKnowledge().getKnownNodes();
            addIPKnowledge(newState, knownNodes, target);
            //implement router port forwarding
            if (target.equals(NetworkNode.TYPE.ROUTER)){
                for (NetworkNode.TYPE relayTarget: Set.of(NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.ADMINPC)){
                    addIPKnowledge(newState, knownNodes, relayTarget);
                }
            }
            Set<NetworkNode.TYPE> internal = Simulation.getSimWorld().getInternalNodes();
            //Get all the visible remote software FROM the node, where the scan was executed from
            Map<NetworkNode.TYPE, Set<Software>> remotelyVisibleSWInNetwork = NetworkTopology.getRemoteSWMapByScanningNode(currentActor);
            //internal scans also reveal Priv IP
            if (internal.contains(currentActor)){
                if (!newState.getNodeKnowledgeMap().get(target).hasPrivIp()){
                    newState.addNodePrivIp(target, node.getPriv_ip());
                }
                addRemoteSw(remotelyVisibleSWInNetwork, target, newState);
            } else if (!internal.contains(currentActor) && target.equals(NetworkNode.TYPE.ROUTER)){
                //for ROUTER we relay
                Set<NetworkNode.TYPE> targets = Set.of(NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.ADMINPC);
                for (NetworkNode.TYPE scanned:targets){
                    addRemoteSw(remotelyVisibleSWInNetwork, scanned, newState);
                }
            }else {
                //from outside against WS/ADMIN PC
                addRemoteSw(remotelyVisibleSWInNetwork, target, newState);
            }
            return newState;
        }

        private void addRemoteSw(Map<NetworkNode.TYPE, Set<Software>> remotelyVisibleSWInNetwork, NetworkNode.TYPE target, State newState){
            if (!remotelyVisibleSWInNetwork.containsKey(target)){
                return;
            }
            for (Software sw: remotelyVisibleSWInNetwork.get(target)){
                if (!newState.getSoftwareKnowledgeMap().containsKey(target) || !newState.isSoftwareContainedInSet(sw.getName(), newState.getSoftwareKnowledgeMap().get(target))){
                    newState.addNodeRemoteSoftwareName(target, sw.getName(), true);
                }
            }
        }

        private void addIPKnowledge(State newState, Set<NetworkNode.TYPE> knownNodes, NetworkNode.TYPE target){
            if (!knownNodes.contains(target)){
                newState.addNodeKnowledge(target);
            }
            if (!newState.getNodeKnowledgeMap().get(target).hasPubIp()){
                newState.addNodePubIp(target, Simulation.getNodeByType(target).getPub_ip());
            }
            if (!newState.getNodeKnowledgeMap().get(target).hasHostname()){
                newState.addNodeHostname(target, Simulation.getNodeByType(target).getHostname());
            }
        }
    },
    ACTIVE_SCAN_VULNERABILITY {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> targets = new HashSet<>();
            Set<NetworkNode.TYPE> knownNodes = currentState.getNetworkKnowledge().getKnownNodes();
            Map<NetworkNode.TYPE, Set<SoftwareKnowledge>> currentSWKnowledge = currentState.getSoftwareKnowledgeMap();
            for (NetworkNode.TYPE host: knownNodes){
                //software has to beknown of the host and it has to be remote (currently ROUTER wont be a target, but WS & AdminPC
                if (currentSWKnowledge.containsKey(host)
                        && currentSWKnowledge.get(host).stream().anyMatch(SoftwareKnowledge::isRemote)){
                    targets.add(host);
                }
            }
            if (!currentState.isStartState()){
                targets.add(NetworkNode.TYPE.ROUTER);
            }
            //only targets that we can attack
            targets.retainAll(NetworkTopology.getConnectedHosts(currentActor));
            if (!Simulation.isPreconditionFilterEnabled()){
                return targets;
            }
            return getTargetsWhereActionResultsInStateChange(targets, currentState, currentActor);
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            assert newState != null;
            NetworkNode actualTarget = Simulation.getNodeByType(target);
            NodeKnowledge knownNode = newState.getNodeKnowledgeMap().get(target);
            //scanning for Router(no SW) and from WS to DB (inner firewall) is disabled
            if (!target.equals(NetworkNode.TYPE.ROUTER)
                    && !(target.equals(NetworkNode.TYPE.DATABASE) && currentActor.equals(NetworkNode.TYPE.WEBSERVER))){
                Set<SoftwareKnowledge> knownSoftware = newState.getSoftwareKnowledgeMap().get(target);
                Map<NetworkNode.TYPE, Set<Software>> remoteSW = NetworkTopology.getRemoteSWMapByScanningNode(currentActor);
                if (remoteSW.containsKey(target)){
                    // add to every software we know the version and the vulnerabilities
                    addVersionAndVulnerabilities(remoteSW.get(target), knownSoftware);
                }
            }
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
                    knownSoftware.remove(foundSw);
                    if (!foundSw.hasVersion()){
                        foundSw.addVersion(s.getVersion());
                    }
                    if (foundSw.getVulnerabilities().isEmpty()){
                        foundSw.addVulnerabilities(s.getVulnerabilities());
                    }
                    knownSoftware.add(foundSw);
                }
            }
        }

    },
    EXPLOIT_PUBLIC_FACING_APPLICATION {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> targets = getTargetsForExploitType(currentState.getSoftwareKnowledgeMap(), currentActor, Exploit.TYPE.EXPLOIT_PUBLIC_FACING_APPLICATION);
            //only keep those, which we actually need
            targets.retainAll(currentState.getNodesWithoutAnyAccess());
            if (!Simulation.isPreconditionFilterEnabled()){
                return targets;
            }
            return getTargetsWhereActionResultsInStateChange(targets, currentState, currentActor);
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            assert newState != null;
            // check if we have not root access so we do not override it
            if(!newState.getNodeKnowledgeMap().get(target).hasAccessLevelRoot()) {
                newState.getNodeKnowledgeMap().get(target).addAccessLevel(NetworkNode.ACCESS_LEVEL.USER);
                //learn own IP if new
                if (!newState.getNodeKnowledgeMap().get(target).hasPrivIp()){
                    newState.addNodePrivIp(target, Simulation.getNodeByType(target).getPriv_ip());
                }
            }
            return newState;
        }
    },
    VALID_ACCOUNTS_VULN {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> targets = getTargetsForExploitType(currentState.getSoftwareKnowledgeMap(), currentActor, Exploit.TYPE.VALID_ACCOUNTS_VULN);
            if (currentActor.equals(NetworkNode.TYPE.WEBSERVER) && targets.contains(NetworkNode.TYPE.DATABASE)){
                targets.remove(NetworkNode.TYPE.DATABASE);
            }
            if (!Simulation.isPreconditionFilterEnabled()){
                return targets;
            }
            return getTargetsWhereActionResultsInStateChange(targets, currentState, currentActor);
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            assert newState != null;
            NodeKnowledge targetNodeKnowledge = newState.getNodeKnowledgeMap().get(target);
            //or learned by auth bypass (DB and Admin)
            if(!targetNodeKnowledge.hasAccessLevelRoot()){
                targetNodeKnowledge.addAccessLevel(NetworkNode.ACCESS_LEVEL.ROOT);
            }
            //learn own IP if new
            if (!targetNodeKnowledge.hasPrivIp()){
                newState.addNodePrivIp(target, Simulation.getNodeByType(target).getPriv_ip());
            }
            return newState;
        }
    },
    VALID_ACCOUNTS_CRED{
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> viewableNodes = getViewableNodes(currentActor);
            Set<NetworkNode.TYPE> nodesWithCredentials = new HashSet<>();
            Map<NetworkNode.TYPE, NodeKnowledge> knownNodes = currentState.getNodeKnowledgeMap();
            //we can also use valid acc locally for priv esc (password file)
            viewableNodes.add(currentActor);
            Set<Credentials> credentials = getAllCredentialsFromData(knownNodes, currentState.getNetworkKnowledge().getSniffedDataMap());
            if (!credentials.isEmpty()){
                for (Credentials creds: credentials){
                    NetworkNode.TYPE accessibleNode = creds.getNode();
                    //changed to make privilege escalation possible
                    if (viewableNodes.contains(accessibleNode) && knownNodes.containsKey(accessibleNode)
                            && (currentState.getNodesWithoutAnyAccess().contains(accessibleNode)
                            || creds.getAccessGrantLevel().equals(Credentials.ACCESS_GRANT_LEVEL.ROOT))){
                        nodesWithCredentials.add(accessibleNode);
                    }
                }
            }
            if (!Simulation.isPreconditionFilterEnabled()){
                return nodesWithCredentials;
            }
            return getTargetsWhereActionResultsInStateChange(nodesWithCredentials, currentState, currentActor);
        }

        private Set<Credentials> getAllCredentialsFromData(Map<NetworkNode.TYPE, NodeKnowledge> knownNodes, Map<Integer, Data> sniffedDataMap){
            Set<Data> knownData = new HashSet<>();
            if (!sniffedDataMap.isEmpty()){
                knownData.addAll(sniffedDataMap.values());
            }
            for(NetworkNode.TYPE node : knownNodes.keySet()) {
                knownData.addAll(knownNodes.get(node).getKnownData().values());
            }
            Set<Credentials> credentialsSet = new HashSet<>();
            for (Data data: knownData){
                if (data.containsCredentials()){
                    credentialsSet.add(data.getCredentials());
                }
            }
            return credentialsSet;
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            assert newState != null;
            Map<NetworkNode.TYPE, NodeKnowledge> map = newState.getNodeKnowledgeMap();
            NodeKnowledge targetNodeKnowledge = map.get(target);
            Set<Credentials> credentials = getAllCredentialsFromData(map, newState.getNetworkKnowledge().getSniffedDataMap());
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
            return newState;
        }
    },
    EXPLOIT_FOR_CLIENT_EXECUTION {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> targets = getTargetsForExploitType(currentState.getSoftwareKnowledgeMap(), currentActor, Exploit.TYPE.EXPLOIT_FOR_CLIENT_EXECUTION);
            if (!Simulation.isPreconditionFilterEnabled()){
                return targets;
            }
            return getTargetsWhereActionResultsInStateChange(targets, currentState, currentActor);
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            assert newState != null;
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
    EXPLOIT_FOR_PRIVILEGE_ESCALATION {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> targets = getTargetsForExploitType(currentState.getSoftwareKnowledgeMap(), currentActor, Exploit.TYPE.EXPLOIT_FOR_PRIVILEGE_ESCALATION);
            Set<NetworkNode.TYPE> actualTargets = new HashSet<>(targets);
            for (NetworkNode.TYPE target: targets){
                Map<NetworkNode.TYPE, NodeKnowledge> map = currentState.getNodeKnowledgeMap();
                if (!map.containsKey(target) || !map.get(target).hasAccessLevelUser()){
                    actualTargets.remove(target);
                }
            }

            if (!Simulation.isPreconditionFilterEnabled()){
                return actualTargets;
            }
            return getTargetsWhereActionResultsInStateChange(actualTargets, currentState, currentActor);
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            assert newState != null;
            newState.getNodeKnowledgeMap().get(target).addAccessLevel(NetworkNode.ACCESS_LEVEL.ROOT);
            return newState;
        }
    },
    MAN_IN_THE_MIDDLE {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> attackableNodes = getSetWithSelfIfSysAccess(currentState, currentActor);
            if (!Simulation.isPreconditionFilterEnabled()){
                return attackableNodes;
            }
            return getTargetsWhereActionResultsInStateChange(attackableNodes,currentState, currentActor);
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            assert newState != null;
            Set<Integer> knownSniffedData = newState.getNetworkKnowledge().getSniffedDataMap().keySet();
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
            Set<NetworkNode.TYPE> attackableNodes = getSetWithSelfIfSysAccess(currentState, currentActor);
            if (!Simulation.isPreconditionFilterEnabled()){
                return attackableNodes;
            }
            return getTargetsWhereActionResultsInStateChange(attackableNodes,currentState, currentActor);
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            assert newState != null;
            NetworkNode node = Simulation.getNodeByType(target);
            Set<Software> swToFind = new HashSet<>(node.getLocalSoftware());
            swToFind.addAll(node.getRemoteSoftware());
            // check if node is already in our software knowledge map
            if(newState.getSoftwareKnowledgeMap().containsKey(node.getType())) {
                Set<SoftwareKnowledge> softwareKnowledgeSet = newState.getSoftwareKnowledgeMap().get(target);
                for(Software s : swToFind){
                    //get software knowledge if already exist for a specific softare
                    SoftwareKnowledge softwareKnowledge =AdversaryAction.findSoftwareByName(softwareKnowledgeSet, s.getName());
                    if(softwareKnowledge!=null){
                        softwareKnowledgeSet.remove(softwareKnowledge);
                        softwareKnowledge.addVersion(s.getVersion());
                        softwareKnowledge.addVulnerabilities(s.getVulnerabilities());
                        softwareKnowledgeSet.add(softwareKnowledge);
                    }else{
                        softwareKnowledge = SoftwareKnowledge.addNew(s.getName(), false);
                        softwareKnowledge.addVersion(s.getVersion());
                        softwareKnowledge.addVulnerabilities(s.getVulnerabilities());
                        softwareKnowledgeSet.add(softwareKnowledge);
                    }
                }
            }else{
                Set<SoftwareKnowledge> softwareKnowledgeSet = new HashSet<>();
                for(Software s : swToFind){
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
            Set<NetworkNode.TYPE> attackableNodes = getSetWithSelfIfSysAccess(currentState, currentActor);
            if (!Simulation.isPreconditionFilterEnabled()){
                return attackableNodes;
            }
            return getTargetsWhereActionResultsInStateChange(attackableNodes,currentState, currentActor);
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
                    if(targetKnowledge.hasAccessLevelRoot()
                            || actualDataMap.get(ID).getAccess().equals(Data.ACCESS_REQUIRED.USER)
                            || actualDataMap.get(ID).getAccess().equals(Data.ACCESS_REQUIRED.ALL)){
                        newState.addNodeData(target, ID, actualDataMap.get(ID));
                    }
                }
            }
            return newState;
        }
    };

    public static Set<AdversaryAction> allActions(){
        return Set.of(ACTIVE_SCAN_IP_PORT, ACTIVE_SCAN_VULNERABILITY,
                EXPLOIT_PUBLIC_FACING_APPLICATION, EXPLOIT_FOR_CLIENT_EXECUTION, EXPLOIT_FOR_PRIVILEGE_ESCALATION,
                VALID_ACCOUNTS_VULN, VALID_ACCOUNTS_CRED, DATA_FROM_LOCAL_SYSTEM, MAN_IN_THE_MIDDLE, SOFTWARE_DISCOVERY);
    }

    /**
     * This abstract method calculates for each Action all possible targets, which fulfill its precondition. The precondition
     * is different for each Action.
     * @param currentState - current state
     * @param currentActor - current action
     * @return - Set of targets
     */
    public abstract Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor);

    /**
     * This abstract method executes all changes to the state for a particular Action applied to a target in a particular state,
     * resulting in a new state.
     * The changes (post-condition) are different for each Action.
     * @param target - A single particular target
     * @param currentState - current state
     * @param currentActor - current actor
     * @return - The new state
     */
    public abstract State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor);

    /**
     * For a given target set and a current state and actor, calculate if the Action applied for a particular target
     * results in a state change.
     * @param targets - Set of targets
     * @param currentState - current state
     * @param currentActor - current actor
     * @return - Set of targets which will result in a state change when the action is executed
     */
    Set<NetworkNode.TYPE> getTargetsWhereActionResultsInStateChange(Set<NetworkNode.TYPE> targets, State currentState, NetworkNode.TYPE currentActor){
        Set<NetworkNode.TYPE> usefulTargets = new HashSet<>(targets);
        for (NetworkNode.TYPE target : targets){
            State simulatedNewState = executePostConditionOnTarget(target, currentState, currentActor);
            if (simulatedNewState.equals(currentState)){
                usefulTargets.remove(target);
            }
        }
        return usefulTargets;
    }

    /**
     * Return the SoftwareKnowledge of a software product by its name.
     * @param softwareKnowledgeSet - SoftwareKnowledge Set
     * @param swName - Software product name
     * @return - SoftwareKnowledge
     */
    private static SoftwareKnowledge findSoftwareByName(Set<SoftwareKnowledge> softwareKnowledgeSet, String swName){
        for(SoftwareKnowledge softwareKnowledge: softwareKnowledgeSet){
            if(softwareKnowledge.getName().equals(swName))
                return softwareKnowledge;
        }
        return null;
    }

    /**
     * For a given state, compute all possible NodeActions (transitions in the MDP)
     * @param currentState - current state
     * @return - set of possible NodeAction's for the Learner to chose his next Action
     */
    Set<NodeAction> getActionsWhichFulfillPrecondition(State currentState) {
        Set<NodeAction> nodeActions = new HashSet<>();
        Set<NetworkNode.TYPE> nodesWithAccess = currentState.getNodesWithAnyNodeAccess();
        for(NetworkNode.TYPE node: nodesWithAccess) {
            Set<NetworkNode.TYPE> targets = getTargetsWhichFulfillPrecondition(currentState, node);
            for(NetworkNode.TYPE target: targets){
                nodeActions.add(new NodeAction(target,node,this));
            }
        }
        return nodeActions;
    }

    /**
     * Calculates a set of all nodes a current actor may see in the network.
     * @param currentActor - current actor
     * @return - set of all visible nodes
     */
    private static Set<NetworkNode.TYPE> getViewableNodes(NetworkNode.TYPE currentActor){
        Set<NetworkNode.TYPE> viewableNodeTypes = new HashSet<>();
        Predicate<NetworkNode> isConnected = node -> NetworkTopology.getConnectedHosts(currentActor).contains(node.getType());
        Set<NetworkNode> viewableNodes = Simulation.getSimWorld().getNodes().stream().filter(isConnected).collect(Collectors.toSet());
        for (NetworkNode n : viewableNodes) {
            viewableNodeTypes.add(n.getType());
        }
        return viewableNodeTypes;
    }

    /**
     * Calculates if a target is vulnerable to a particular exploit by an actor.
     * @param swMap - Software Map listing software for each possible target
     * @param currentActor - current actor
     * @param exploit - exploit we are checking for
     * @return - set of vulnerable targets
     */
    private static Set<NetworkNode.TYPE> getTargetsForExploitType(Map<NetworkNode.TYPE, Set<SoftwareKnowledge>> swMap, NetworkNode.TYPE currentActor, Exploit.TYPE exploit){
        Set<NetworkNode.TYPE> attackableNodes = new HashSet<>();
        for(NetworkNode.TYPE node: swMap.keySet()){
            //check if we could attack the node from our current location
            if(getViewableNodes(currentActor).contains(node)) {
                for (SoftwareKnowledge softwareKnowledge : swMap.get(node)) {
                    for (Vulnerability v : softwareKnowledge.getVulnerabilities()) {
                        if (v.getExploitType().equals(exploit)) {
                            attackableNodes.add(node);
                        }
                    }
                }
            }
        }
        return attackableNodes;
    }

    /**
     * For techniques applicable only on its own node with sys access required and with exception to the adversary node,
     * this returns only the given node as possible target.
     * @param currentState - current state
     * @param currentActor - current actor
     * @return - set of targets
     */
    private static Set<NetworkNode.TYPE> getSetWithSelfIfSysAccess(State currentState, NetworkNode.TYPE currentActor) {
        Set<NetworkNode.TYPE> attackableNodes = new HashSet<>();
        if (currentState.getNodesWithAnyNodeAccess().contains(currentActor) && !currentActor.equals(NetworkNode.TYPE.ADVERSARY)){
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
