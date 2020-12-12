package visualize;

import core.AdversaryAction;
import environment.NetworkNode;

import java.util.Map;
import java.util.Set;

public class SimpleActionsPrint {
    public static void print(Map<AdversaryAction, Set<NetworkNode.TYPE>> actionSetMap, NetworkNode.TYPE actor){
        System.out.println("Current possible Actions for Adversary");
        System.out.println("\tExecutes Action FROM Node: "+ actor.toString());
        for (AdversaryAction action: actionSetMap.keySet()){
            Set<NetworkNode.TYPE> targets = actionSetMap.get(action);
            System.out.println("\t\tAction "+action.toString()+":");
            System.out.println("\t\tTargets: ");
            for (NetworkNode.TYPE target:targets){
                System.out.println("\t\t\t"+target.toString());
            }
        }
    }
}
