package visualize;

import core.State;
import environment.NetworkNode;
import knowledge.NetworkKnowledge;
import knowledge.NodeKnowledge;

public class SimpleStatePrint {
    public static void print(State state){
        System.out.println("\nState info:");
        printAdversaryKnowledge(state);
        printPossibleAdversaryActions();
    }

    private static void printAdversaryKnowledge(State state){
        System.out.println("Adversary Knowledge:");
        printNetworkKnowledge(state.getNetworkKnowledge());
        for (NetworkNode.TYPE node:state.getNodeKnowledgeMap().keySet()){
            System.out.println("\tNode Detail: "+node.toString());
            printNodeKnowledge(state.getNodeKnowledgeMap().get(node));
        }
    }

    private static void printNetworkKnowledge(NetworkKnowledge knowledge){
        System.out.println("\tNetwork:");
        for (NetworkNode.TYPE node:knowledge.getKnownNodes()){
            System.out.println("\t\tNode found: "+node.toString());
        }
    }

    private static void printNodeKnowledge(NodeKnowledge knowledge){
        if (knowledge.hasPubIp()) System.out.println("\t\t\tPubIP known");
        if (knowledge.hasPrivIp()) System.out.println("\t\t\tPrivIP known");
        if (knowledge.hasOperatingSystem()) System.out.println("\t\t\tOS known");
        if (knowledge.hasOSVersion()) System.out.println("\t\t\tOS Version known");
        if (knowledge.hasAccessLevelUser()) System.out.println("\t\t\tHas user access");
        if (knowledge.hasAccessLevelRoot()) System.out.println("\t\t\tHas root access");
    }

    private static void printPossibleAdversaryActions(){

    }
}
