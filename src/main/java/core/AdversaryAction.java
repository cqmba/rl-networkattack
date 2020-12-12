package core;

import aima.core.agent.Action;

import environment.NetworkNode;
import environment.NetworkTopology;
import environment.Software;
import knowledge.NetworkKnowledge;
import knowledge.NodeKnowledge;
import knowledge.SoftwareKnowledge;
import run.Simulation;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum AdversaryAction implements Action {
    ACTIVE_SCAN_IP_PORT{
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            // change that we are able to scan just the nodes viewable from the current actor
            NetworkNode.TYPE scanning = currentState.getCurrentActor();
            Predicate<NetworkNode> isConnected = node -> NetworkTopology.getConnectedHosts(scanning).contains(node.getType());
            Set<NetworkNode> viewableNodes = Simulation.getSimWorld().getNodes().stream().filter(isConnected).collect(Collectors.toSet());
            Set<NetworkNode.TYPE> scannableNodes = new HashSet<>();
            for(NetworkNode n : viewableNodes){
                scannableNodes.add(n.getType());
            }
            return scannableNodes;
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {

            NetworkNode node = Simulation.getNodeByType(target);
            //check IP or hostname was gained
            State newState = currentState;
            Set<NetworkNode.TYPE> knownNodes = currentState.getNetworkKnowledge().getKnownNodes();
            if (!knownNodes.contains(target)){
                newState.addNodeKnowledge(target);
                newState.addNodePubIp(target, node.getPub_ip());
                //I dont think we see private IP on an IP Scan, only if we get system access
                newState.addNodeHostname(target, node.getHostname());
            }
            //implement router port forwarding
            if (target.equals(NetworkNode.TYPE.ROUTER)){
                if (!knownNodes.contains(NetworkNode.TYPE.WEBSERVER)){
                    NetworkNode.TYPE actualTarget = NetworkNode.TYPE.WEBSERVER;
                    newState.setNetworkKnowledge(currentState.getNetworkKnowledge().addNewNode(NetworkNode.TYPE.WEBSERVER));
                    newState.addNodeKnowledge(actualTarget);
                    newState.addNodePubIp(actualTarget, node.getPub_ip());
                    //I dont think we see private IP on an IP Scan, only if we get system access
                    newState.addNodeHostname(actualTarget, node.getHostname());
                }
                if (!knownNodes.contains(NetworkNode.TYPE.ADMINPC)){
                    NetworkNode.TYPE actualTarget = NetworkNode.TYPE.ADMINPC;
                    newState.setNetworkKnowledge(currentState.getNetworkKnowledge().addNewNode(NetworkNode.TYPE.ADMINPC));
                    newState.addNodeKnowledge(actualTarget);
                    newState.addNodePubIp(actualTarget, node.getPub_ip());
                    //I dont think we see private IP on an IP Scan, only if we get system access
                    newState.addNodeHostname(actualTarget, node.getHostname());
                }
            }

            NodeKnowledge nodeKnowledge = currentState.getNodeKnowledgeMap().get(target);
            //Get all the visible remote software FROM the node, where the scan was executed from
            Map<NetworkNode.TYPE, Set<Software>> remotelyVisibleSWInNetwork = NetworkTopology.getRemoteSWMapByScanningNode(currentState.getCurrentActor());
            //can only see SW running on target
            if (remotelyVisibleSWInNetwork.containsKey(target) && !remotelyVisibleSWInNetwork.get(target).isEmpty()){
                Set<Software> potentiallyNewSoftware = remotelyVisibleSWInNetwork.get(target);
                for (Software sw: potentiallyNewSoftware){
                    if (!nodeKnowledge.isRemoteServiceKnown(sw.getName())){
                        newState.addNodeRemoteSoftwareName(target, sw.getName());
                    }
                }
            }
            return newState;
        }
    },
    ACTIVE_SCAN_VULNERABILITY {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            //return just the nodes where we have an entry in our software knowledge map
            return currentState.getSoftwareKnowledgeMap().keySet();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {
            NetworkNode node = Simulation.getNodeByType(target);
            Set<Software> localSoftwareSet = node.getLocalSoftware();
            Set<Software> remoteSoftwareSet = node.getRemoteSoftware();
            Set<SoftwareKnowledge> softwareKnowledgeSet = currentState.getSoftwareKnowledgeMap().get(target);
            // add to every software we know the version and the vulnerabilities
            addVersionAndVulnerabilities(localSoftwareSet, softwareKnowledgeSet);
            addVersionAndVulnerabilities(remoteSoftwareSet, softwareKnowledgeSet);
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
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {
            return null;
        }
    },
    VALID_ACCOUNTS {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {
            return null;
        }
    },
    EXPLOIT_FOR_CLIENT_EXECUTION {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {
            return null;
        }
    },
    CREATE_ACCOUNT {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {
            return null;
        }
    },
    EXPLOIT_FOR_PRIVILEGE_ESCALATION {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {
            return null;
        }
    },
    MAN_IN_THE_MIDDLE {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {
            return null;
        }
    },
    SOFTWARE_DISCOVERY {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {
            return null;
        }
    },
    EXPLOITATION_OF_REMOTE_SERVICE {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {
            return null;
        }
    },
    REMOTE_SERVICE {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {
            return null;
        }
    },
    DATA_FROM_LOCAL_SYSTEM {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {
            return null;
        }
    };

    private static final Set<AdversaryAction> _actions = new LinkedHashSet<>();

    public static State ipScan(State s, NetworkNode.TYPE target){

        //TODO implement
        return null;
    }

    public static State vulnScan(State s, NetworkNode.TYPE target){
        //TODO implement
        return null;
    }

    public static State exploitPFA(State s, NetworkNode.TYPE target){
        //TODO implement
        return null;
    }

    public static State validAccount(State s, NetworkNode.TYPE target){
        //TODO implement
        return null;
    }

    public static State exploitCE(State s, NetworkNode.TYPE target){
        //TODO implement
        return null;
    }

    public static State createAccount(State s, NetworkNode.TYPE target){
        //TODO implement
        return null;
    }

    public static State exploitPE(State s, NetworkNode.TYPE target){
        //TODO implement
        return null;
    }

    public static State mitm(State s, NetworkNode.TYPE target){
        //TODO implement
        return null;
    }

    public static State softwareDiscovery(State s, NetworkNode.TYPE target){
        //TODO implement
        return null;
    }

    public static State exploitRS(State s, NetworkNode.TYPE target){
        //TODO implement
        return null;
    }

    public static State remoteService(State s, NetworkNode.TYPE target){
        //TODO implement
        return null;
    }

    public static State dataFromLocalSystem(State s, NetworkNode.TYPE target){
        //TODO implement
        return null;
    }

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

    public abstract Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState);
    public abstract State executePostConditionOnTarget(NetworkNode.TYPE target, State currentState);
}
