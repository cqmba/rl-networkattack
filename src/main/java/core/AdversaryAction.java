package core;

import aima.core.agent.Action;

import environment.NetworkNode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public enum AdversaryAction implements Action {
    ACTIVE_SCAN_IP_PORT,
    ACTIVE_SCAN_VULNERABILITY,
    EXPLOIT_PUBLIC_FACING_APPLICATION,
    VALID_ACCOUNTS,
    EXPLOIT_FOR_CLIENT_EXECUTION,
    CREATE_ACCOUNT,
    EXPLOIT_FOR_PRIVILEGE_ESCALATION,
    MAN_IN_THE_MIDDLE,
    SOFTWARE_DISCOVERY,
    EXPLOITATION_OF_REMOTE_SERVICE,
    REMOTE_SERVICE,
    DATA_FROM_LOCAL_SYSTEM
    ;
    private List<NetworkNode> listOfAvailableNodesToActUpon;

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
}
