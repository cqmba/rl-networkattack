package core;

import aima.core.agent.Action;

import environment.NetworkNode;
import environment.Software;
import knowledge.NodeKnowledge;
import run.Simulation;

import java.util.*;

public enum AdversaryAction implements Action {
    ACTIVE_SCAN_IP_PORT{
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return currentState.getNetworkKnowledge().getKnownNodes();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {
            NetworkNode node = Simulation.getNodeByType(target);
            //check IP or hostname was gained
            if (!currentState.getNetworkKnowledge().getKnownNodes().contains(target)){
                currentState.addNodeKnowledge(target);
                currentState.addNodePubIp(target, node.getPub_ip());
                //I dont think we see private IP on an IP Scan, only if we get system access
                currentState.addNodeHostname(target, node.getHostname());
            }

            NodeKnowledge nodeKnowledge = currentState.getNodeKnowledgeMap().get(target);
            //Get all the visible remote software FROM the node, where the scan was executed from
            Map<NetworkNode.TYPE, Set<Software>> remotelyVisibleSWInNetwork = Simulation.getNodeByType(currentState.getCurrentActor()).getRemotelyVisibleSWInNetwork();
            //can only see SW running on target
            if (remotelyVisibleSWInNetwork.containsKey(target) && !remotelyVisibleSWInNetwork.get(target).isEmpty()){
                Set<Software> potentiallyNewSoftware = remotelyVisibleSWInNetwork.get(target);
                for (Software sw: potentiallyNewSoftware){
                    if (!nodeKnowledge.isRemoteServiceKnown(sw.getName())){
                        currentState.addNodeRemoteSoftwareName(target, sw.getName());
                    }
                }
            }
        }
    },
    ACTIVE_SCAN_VULNERABILITY {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            currentState.getNetworkKnowledge().getKnownNodes();
            return new HashSet<>();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {

        }
    },
    EXPLOIT_PUBLIC_FACING_APPLICATION {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {

        }
    },
    VALID_ACCOUNTS {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {

        }
    },
    EXPLOIT_FOR_CLIENT_EXECUTION {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {

        }
    },
    CREATE_ACCOUNT {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {

        }
    },
    EXPLOIT_FOR_PRIVILEGE_ESCALATION {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {

        }
    },
    MAN_IN_THE_MIDDLE {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {

        }
    },
    SOFTWARE_DISCOVERY {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {

        }
    },
    EXPLOITATION_OF_REMOTE_SERVICE {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {

        }
    },
    REMOTE_SERVICE {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {

        }
    },
    DATA_FROM_LOCAL_SYSTEM {
        @Override
        public Set<NetworkNode.TYPE> getTargetsWhichFullfillPrecondition(State currentState) {
            return new HashSet<>();
        }

        @Override
        public void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState) {

        }
    };

    private static final Set<AdversaryAction> _actions = new LinkedHashSet<>();

    public State ipScan(State s, List<NetworkNode> nodes, NetworkNode acting, NetworkNode target){

        //TODO implement
        return null;
    }

    public State vulnScan(State s, List<NetworkNode> nodes, NetworkNode acting, NetworkNode target){
        //TODO implement
        return null;
    }

    public State exploitPFA(State s, List<NetworkNode> nodes, NetworkNode acting, NetworkNode target){
        //TODO implement
        return null;
    }

    public State validAccount(State s, List<NetworkNode> nodes, NetworkNode acting, NetworkNode target){
        //TODO implement
        return null;
    }

    public State exploitCE(State s, List<NetworkNode> nodes, NetworkNode acting, NetworkNode target){
        //TODO implement
        return null;
    }

    public State createAccount(State s, List<NetworkNode> nodes, NetworkNode acting, NetworkNode target){
        //TODO implement
        return null;
    }

    public State exploitPE(State s, List<NetworkNode> nodes, NetworkNode acting, NetworkNode target){
        //TODO implement
        return null;
    }

    public State mitm(State s, List<NetworkNode> nodes, NetworkNode acting, NetworkNode target){
        //TODO implement
        return null;
    }

    public State softwareDiscovery(State s, List<NetworkNode> nodes, NetworkNode acting, NetworkNode target){
        //TODO implement
        return null;
    }

    public State exploitRS(State s, List<NetworkNode> nodes, NetworkNode acting, NetworkNode target){
        //TODO implement
        return null;
    }

    public State remoteService(State s, List<NetworkNode> nodes, NetworkNode acting, NetworkNode target){
        //TODO implement
        return null;
    }

    public State dataFromLocalSystem(State s, List<NetworkNode> nodes, NetworkNode acting, NetworkNode target){
        //TODO implement
        return null;
    }

    //perform a certain Action from an acting node towards a target node
    public State performGivenAction(State s, List<NetworkNode> nodes, AdversaryAction action, NetworkNode acting, NetworkNode target){
        if (action.equals(AdversaryAction.ACTIVE_SCAN_IP_PORT)){
            return ipScan(s, nodes, acting, target);
        }else if (action.equals(AdversaryAction.ACTIVE_SCAN_VULNERABILITY)){
            return vulnScan(s, nodes, acting, target);
        }else if (action.equals(AdversaryAction.CREATE_ACCOUNT)){
            return createAccount(s, nodes, acting, target);
        }else if (action.equals(AdversaryAction.DATA_FROM_LOCAL_SYSTEM)){
            return dataFromLocalSystem(s, nodes, acting, target);
        }else if (action.equals(AdversaryAction.EXPLOIT_FOR_CLIENT_EXECUTION)){
            return exploitCE(s, nodes, acting, target);
        }else if (action.equals(AdversaryAction.EXPLOIT_FOR_PRIVILEGE_ESCALATION)){
            return exploitPE(s, nodes, acting, target);
        }else if (action.equals(AdversaryAction.EXPLOIT_PUBLIC_FACING_APPLICATION)){
            return exploitPFA(s, nodes, acting, target);
        }else if (action.equals(AdversaryAction.EXPLOITATION_OF_REMOTE_SERVICE)){
            return exploitRS(s, nodes, acting, target);
        }else if (action.equals(AdversaryAction.MAN_IN_THE_MIDDLE)){
            return mitm(s, nodes, acting, target);
        }else if (action.equals(AdversaryAction.REMOTE_SERVICE)){
            return remoteService(s, nodes, acting, target);
        }else if (action.equals(AdversaryAction.SOFTWARE_DISCOVERY)){
            return softwareDiscovery(s, nodes, acting, target);
        }else if (action.equals(AdversaryAction.VALID_ACCOUNTS)){
            return validAccount(s, nodes, acting, target);
        }else {
            return s;
        }
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
    public abstract void executePostConditionOnTarget(NetworkNode.TYPE target, State currentState);
}
