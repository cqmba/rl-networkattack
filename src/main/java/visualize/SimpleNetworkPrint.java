package visualize;

import environment.NetworkNode;
import environment.NetworkWorld;
import environment.Software;
import environment.Vulnerability;

public class SimpleNetworkPrint {
    public static void print(NetworkWorld world){
        printOverview(world);
    }

    private static void printOverview(NetworkWorld world){
        System.out.println("The current nodes are in the network:");
        int i = 1;
        for (NetworkNode node: world.getNodes()){
            System.out.println("Host "+i+": ");
            System.out.println("\tPUB_IP: "+node.getPub_ip());
            System.out.println("\tPRIV_IP: "+node.getPriv_ip());
            System.out.println("\tHostname: "+node.getHostname());
            System.out.println("\tOS: "+node.getOperatingSystem());
            System.out.println("\tOS Version: "+node.getOsVersion());
            printNodeDetail(node);
            System.out.println("");
            i++;
        }
    }

    private static void printNodeDetail(NetworkNode node){
        if (!node.getRemoteSoftware().isEmpty()){
            System.out.println("\tInstalled remotely accessible software:");
            for (Software software: node.getRemoteSoftware()){
                System.out.println("\t\tSoftware: "+software.getName() +" "+software.getVersion());
                if (!software.getVulnerabilities().isEmpty()){
                    printVulnerabilities(software);
                }else {
                    System.out.println("\t\tNo vulnerabilities");
                }
            }
        } else {
            System.out.println("\tNo remote software installed!");
        }
        if (!node.getLocalSoftware().isEmpty()){
            System.out.println("\tInstalled local software:");
            for (Software software: node.getLocalSoftware()){
                System.out.println("\t\tSoftware: "+software.getName() +" "+software.getVersion());
                if (!software.getVulnerabilities().isEmpty()){
                    printVulnerabilities(software);
                }else {
                    System.out.println("\t\tNo vulnerabilities");
                }
            }
        } else {
            System.out.println("\tNo local software installed!");
        }
        if (!node.getDataSet().isEmpty()){
            System.out.println("\tLocal data size: " + node.getDataSet().size());
        } else {
            System.out.println("\tNo local data found!");
        }
    }

    private static void printVulnerabilities(Software software) {
        System.out.println("\t\t\tVulnerabilities: " + software.getVulnerabilities().size());
        int i = 1;
        for (Vulnerability vuln: software.getVulnerabilities()){
            System.out.println("\t\t\t\tVulnerability "+i+"");
            System.out.println("\t\t\t\tType: "+vuln.getType().toString());
            if (!vuln.getCve().isEmpty()){
                System.out.println("\t\t\t\tRelated CVE: "+vuln.getCve());
            }
            if (vuln.getZeroday()){
                System.out.println("\t\t\t\tZeroday: yes!");
            }
            System.out.println("\t\t\t\tRelated Exploit: "+vuln.getExploitType());
            i++;
        }
    }
}
