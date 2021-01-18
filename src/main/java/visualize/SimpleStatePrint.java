package visualize;

import core.State;
import environment.Credentials;
import environment.Data;
import environment.NetworkNode;
import environment.Vulnerability;
import knowledge.NetworkKnowledge;
import knowledge.NodeKnowledge;
import knowledge.SoftwareKnowledge;

import java.util.Map;
import java.util.Set;

public class SimpleStatePrint {
    public static void print(State state){
        System.out.println("\nState info:");
        printAdversaryKnowledge(state);
    }

    private static void printAdversaryKnowledge(State state){
        System.out.println("Adversary Knowledge:");
        printNetworkKnowledge(state.getNetworkKnowledge());
        for (NetworkNode.TYPE node:state.getNodeKnowledgeMap().keySet()){
            System.out.println("\tNode Detail: "+node.toString());
            NodeKnowledge nodeKnowledge = state.getNodeKnowledgeMap().get(node);
            printNodeKnowledge(nodeKnowledge);
            if (state.getSoftwareKnowledgeMap().containsKey(node)){
                System.out.println("\t\tSoftware:");
                printNodeSoftwareKnowledge(state.getSoftwareKnowledgeMap().get(node));
            }
            if (!nodeKnowledge.getKnownData().isEmpty()){
                printDataKnowledge(nodeKnowledge.getKnownData());
            }
        }
        printNetworkDataKnowledge(state.getNetworkKnowledge().getSniffedDataMap());
    }

    private static void printNetworkDataKnowledge(Map<Integer, Data> dataMap){
        System.out.println("\tNetwork Data:");
        for (int ID : dataMap.keySet()){
            String line = "\t\t\t\tID "+ID;
            if (dataMap.get(ID).containsCredentials()){
                Credentials creds = dataMap.get(ID).getCredentials();
                line = line.concat("\t\tCredentials found! Type: "+creds.getType()+" Software: "+creds.getUseInSW()+ " On: "+creds.getNode()+" Access: "+creds.getAccessGrantLevel());
            }
            System.out.println(line);
        }
    }

    private static void printNodeSoftwareKnowledge(Set<SoftwareKnowledge> softwareKnowledges) {
        for (SoftwareKnowledge sw:softwareKnowledges){
            System.out.println("\t\t\t"+sw.getName());
            if (sw.hasVersion()){
                System.out.println("\t\t\tVersion: "+sw.getVersion());
            }
            if (sw.hasVulnerability()){
                System.out.println("\t\t\tVulnerabilities found!");
                for (Vulnerability vuln:sw.getVulnerabilities()){
                    System.out.println("\t\t\t\t"+vuln.getType());
                }
            }
        }
    }

    private static void printDataKnowledge(Map<Integer, Data> dataMap){
        System.out.println("\t\tData found: "+dataMap.keySet().size());
        for (int ID : dataMap.keySet()){
            String line = "\t\t\tID "+ID;
            if (dataMap.get(ID).containsCredentials()){
                Credentials creds = dataMap.get(ID).getCredentials();
                line = line.concat("\t\t\t\tCredentials found! Type: "+creds.getType()+" Software: "+creds.getUseInSW()+ " On: "+creds.getNode()+" Access: "+creds.getAccessGrantLevel());
            }
            System.out.println(line);
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
        if (knowledge.hasHostname()) System.out.println("\t\t\tHostname known");
        if (knowledge.hasOperatingSystem()) System.out.println("\t\t\tOS known");
        if (knowledge.hasOSVersion()) System.out.println("\t\t\tOS Version known");
        if (knowledge.hasAccessLevelUser()) System.out.println("\t\t\tHas user access");
        if (knowledge.hasAccessLevelRoot()) System.out.println("\t\t\tHas root access");
    }
}
