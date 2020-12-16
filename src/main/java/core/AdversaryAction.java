package core;

import aima.core.agent.Action;

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

public enum AdversaryAction implements Action {
    ACTIVE_SCAN_IP_PORT{
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            // change that we are able to scan just the nodes viewable from the current actor
            return getViewableNodes(currentActor);
        }



        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {

            NetworkNode node = Simulation.getNodeByType(target);
            State newState = (State) deepCopy(currentState);
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

            NodeKnowledge nodeKnowledge = currentState.getNodeKnowledgeMap().get(target);
            //Get all the visible remote software FROM the node, where the scan was executed from
            Map<NetworkNode.TYPE, Set<Software>> remotelyVisibleSWInNetwork = NetworkTopology.getRemoteSWMapByScanningNode(currentActor);
            //when scanning on router, get port forwarded sw only
            if (target.equals(NetworkNode.TYPE.ROUTER)){
                Set<NetworkNode.TYPE> targets = new HashSet<>();
                targets.add(NetworkNode.TYPE.WEBSERVER);
                targets.add(NetworkNode.TYPE.ADMINPC);
                for (NetworkNode.TYPE scanned:targets){
                    for (Software software:remotelyVisibleSWInNetwork.get(scanned)){
                        newState.addNodeRemoteSoftwareName(scanned, software.getName(), true);
                    }
                }
            }else if (remotelyVisibleSWInNetwork.containsKey(target) && !remotelyVisibleSWInNetwork.get(target).isEmpty()){
                for (Software sw: remotelyVisibleSWInNetwork.get(target)){
                    newState.addNodeRemoteSoftwareName(target, sw.getName(), true);
                }
            }

            /*
            if (remotelyVisibleSWInNetwork.containsKey(target) && !remotelyVisibleSWInNetwork.get(target).isEmpty()){
                Set<Software> potentiallyNewSoftware = remotelyVisibleSWInNetwork.get(target);
                //Predicate<SoftwareKnowledge> isRemoteSw = software -> software.isRemote();
                Map<NetworkNode.TYPE, Set<SoftwareKnowledge>> swKnowledge = newState.getSoftwareKnowledgeMap();
                //if nothing was known about target
                if (swKnowledge.isEmpty() || !swKnowledge.containsKey(target)){
                    Set<SoftwareKnowledge> gainedSwKnowledge = new HashSet<>();
                    for (Software sw:potentiallyNewSoftware){
                        gainedSwKnowledge.add(SoftwareKnowledge.addNew(sw.getName(), true));

                    }
                    swKnowledge.put(target, gainedSwKnowledge);
                }
                for (Software sw: potentiallyNewSoftware){
                    newState.addNodeRemoteSoftwareName(target, sw.getName());
                }
            }

             */
            return newState;
        }
    },
    ACTIVE_SCAN_VULNERABILITY {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            //return just the nodes where we have an entry in our software knowledge map
            NetworkNode.TYPE scanning = currentActor;
            Predicate<NetworkNode> isConnected = node -> NetworkTopology.getConnectedHosts(scanning).contains(node.getType());
            Set<NetworkNode> viewableNodes = Simulation.getSimWorld().getNodes().stream().filter(isConnected).collect(Collectors.toSet());
            Set<NetworkNode.TYPE> viewableNodeTypes = new HashSet<>();
            for(NetworkNode n : viewableNodes){
                if(currentState.getSoftwareKnowledgeMap().containsKey(n.getType()))
                    viewableNodeTypes.add(n.getType());
            }
            return viewableNodeTypes;

        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            NetworkNode node = Simulation.getNodeByType(target);
            Set<Software> localSoftwareSet = node.getLocalSoftware();
            Set<Software> remoteSoftwareSet = node.getRemoteSoftware();
            Set<SoftwareKnowledge> softwareKnowledgeSet = newState.getSoftwareKnowledgeMap().get(target);
            // add to every software we know the version and the vulnerabilities
            addVersionAndVulnerabilities(localSoftwareSet, softwareKnowledgeSet);
            addVersionAndVulnerabilities(remoteSoftwareSet, softwareKnowledgeSet);
            //TODO fix
            return newState;
        }

        private void addVersionAndVulnerabilities(Set<Software> softwareSet, Set<SoftwareKnowledge> softwareKnowledgeSet) {
            for(Software s : softwareSet){
                SoftwareKnowledge softwareKnowledge = findSoftwareByName(softwareKnowledgeSet,s.getName());
                if(softwareKnowledge!=null){
                    softwareKnowledge.addVersion(s.getVersion());
                    softwareKnowledge.addVulnerabilities(s.getVulnerabilities());
                }
            }
        }

    },
    EXPLOIT_PUBLIC_FACING_APPLICATION {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> viewableNodes = getViewableNodes(currentActor);
            Set<NetworkNode.TYPE> attackableNodes = new HashSet<>();
            for(NetworkNode.TYPE node: currentState.getSoftwareKnowledgeMap().keySet()){
                //check if we could attack the node from our current location
                if(viewableNodes.contains(node)) {
                    for (SoftwareKnowledge softwareKnowledge : currentState.getSoftwareKnowledgeMap().get(node)) {
                        for (Vulnerability v : softwareKnowledge.getVulnerabilities()) {
                            //todo maybe we should check if we have already access?
                            if (v.getExploitType().equals(Exploit.TYPE.EXPLOIT_PUBLIC_FACING_APPLICATION)) {
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
            return newState;
        }
    },
    VALID_ACCOUNTS {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> viewableNodes = getViewableNodes(currentActor);
            Set<NetworkNode.TYPE> nodesWithCredentials = new HashSet<>();
            for(NetworkNode.TYPE node : currentState.getNodeKnowledgeMap().keySet()){
                for(Data data :currentState.getNodeKnowledgeMap().get(node).getKnownData()){
                    //check if data is a credential and the node is attackable from the current actor
                    if(data.containsCredentials() && viewableNodes.contains(data.getCredentials().getNode())){
                        nodesWithCredentials.add(data.getCredentials().getNode());
                    }
                }
            }
            return nodesWithCredentials;
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            NodeKnowledge nodeKnowledge = newState.getNodeKnowledgeMap().get(target);
            for(Data data : nodeKnowledge.getKnownData()){
                if(data.containsCredentials()){
                    Credentials.ACCESS_GRANT_LEVEL access_grant_level = data.getCredentials().getAccessGrantLevel();
                    if(!nodeKnowledge.hasAccessLevelRoot()){
                        if(access_grant_level==Credentials.ACCESS_GRANT_LEVEL.ROOT){
                            nodeKnowledge.addAccessLevel(NetworkNode.ACCESS_LEVEL.ROOT);
                        }else{
                            nodeKnowledge.addAccessLevel(NetworkNode.ACCESS_LEVEL.USER);
                        }

                    }
                }
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
            return newState;
        }
    },
    CREATE_ACCOUNT {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> attackableNodes = new HashSet<>();
            for(NetworkNode.TYPE node : currentState.getNodeKnowledgeMap().keySet()){
                //check if we have  root access on the node
                if(currentActor.equals(node) && currentState.getNodeKnowledgeMap().get(node).hasAccessLevelRoot()){
                    attackableNodes.add(node);
                }
            }
            return attackableNodes;
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            State newState = (State) deepCopy(currentState);
            //create new credentials
            Data data = new Data(new Credentials(Credentials.TYPE.KEY,Credentials.ACCESS_GRANT_LEVEL.ROOT,"","",target),Data.GAINED_KNOWLEDGE.HIGH,Data.ORIGIN.CREATED,Data.ACCESS_REQUIRED.ROOT);
            NetworkNode node = Simulation.getNodeByType(target);
            //add credentials to node
            node.getDataSet().add(data);
            //add credentials to node knowledge
            newState.getNodeKnowledgeMap().get(target).getKnownData().add(data);
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
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            return null;
        }
    },
    SOFTWARE_DISCOVERY {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> attackableNodes = new HashSet<>();
            //check if we have access on the node
            if(currentState.getNodeKnowledgeMap().containsKey(currentActor) && (currentState.getNodeKnowledgeMap().get(currentActor).hasAccessLevelUser() || currentState.getNodeKnowledgeMap().get(currentActor).hasAccessLevelRoot())){
                attackableNodes.add(currentActor);
            }

            return attackableNodes;
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
    EXPLOITATION_OF_REMOTE_SERVICE {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            return null;
        }
    },
    REMOTE_SERVICE {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            return null;
        }
    },
    DATA_FROM_LOCAL_SYSTEM {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor) {
            Set<NetworkNode.TYPE> attackableNodes = new HashSet<>();
            //check if we have access on the node
            if(currentState.getNodeKnowledgeMap().containsKey(currentActor) && (currentState.getNodeKnowledgeMap().get(currentActor).hasAccessLevelUser() || currentState.getNodeKnowledgeMap().get(currentActor).hasAccessLevelRoot())){
                attackableNodes.add(currentActor);
            }
            return attackableNodes;
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor) {
            NetworkNode node = Simulation.getNodeByType(target);
            State newState = (State) deepCopy(currentState);
            Set<Data> dataSet = currentState.getNodeKnowledgeMap().get(target).getKnownData();
            for(Data data : node.getDataSet()){
                if(newState.getNodeKnowledgeMap().get(target).hasAccessLevelRoot()){
                  dataSet.add(data);
                }else {
                    if (data.getAccess() == Data.ACCESS_REQUIRED.USER){
                        dataSet.add(data);
                    }
                }
            }
            return newState;
        }
    };

    private static final Set<AdversaryAction> _actions = new LinkedHashSet<>();

    public static final Set<AdversaryAction> allActions(){
        return _actions;
    }

    static {
        _actions.add(ACTIVE_SCAN_IP_PORT);
        _actions.add(ACTIVE_SCAN_VULNERABILITY);
        _actions.add(EXPLOIT_PUBLIC_FACING_APPLICATION);
        _actions.add(EXPLOITATION_OF_REMOTE_SERVICE);
        _actions.add(EXPLOIT_FOR_CLIENT_EXECUTION);
        _actions.add(EXPLOIT_FOR_PRIVILEGE_ESCALATION);
        _actions.add(VALID_ACCOUNTS);
        _actions.add(DATA_FROM_LOCAL_SYSTEM);
        _actions.add(CREATE_ACCOUNT);
        _actions.add(MAN_IN_THE_MIDDLE);
        _actions.add(REMOTE_SERVICE);
        _actions.add(SOFTWARE_DISCOVERY);
    }

    @Override
    public boolean isNoOp() {
        return false;
    }

    public static SoftwareKnowledge findSoftwareByName(Set<SoftwareKnowledge> softwareKnowledgeSet, String swName){
        for(SoftwareKnowledge softwareKnowledge: softwareKnowledgeSet){
            if(softwareKnowledge.getName().equals(swName))
                return softwareKnowledge;
        }
        return null;
    }

    public abstract Set<NetworkNode.TYPE> getTargetsWhichFulfillPrecondition(State currentState, NetworkNode.TYPE currentActor);
    public abstract State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState, NetworkNode.TYPE currentActor);

    private static Set<NetworkNode.TYPE> getViewableNodes(NetworkNode.TYPE currentActor){
        NetworkNode.TYPE scanning = currentActor;
        Predicate<NetworkNode> isConnected = node -> NetworkTopology.getConnectedHosts(scanning).contains(node.getType());
        Set<NetworkNode> viewableNodes = Simulation.getSimWorld().getNodes().stream().filter(isConnected).collect(Collectors.toSet());
        Set<NetworkNode.TYPE> viewableNodeTypes = new HashSet<>();
        for(NetworkNode n : viewableNodes){
            viewableNodeTypes.add(n.getType());
        }
        return viewableNodeTypes;
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
