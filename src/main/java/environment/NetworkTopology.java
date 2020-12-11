package environment;

import run.Simulation;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NetworkTopology{
    public static List<NetworkNode.TYPE> getConnectedHosts(NetworkNode.TYPE source){
        List<NetworkNode.TYPE> connectedHosts = new ArrayList<>();
        if(source.equals(NetworkNode.TYPE.ADVERSARY)){
            connectedHosts.add(NetworkNode.TYPE.ROUTER);
            return connectedHosts;
        }else if (source.equals(NetworkNode.TYPE.ROUTER)){
            connectedHosts.add(NetworkNode.TYPE.WEBSERVER);
            connectedHosts.add(NetworkNode.TYPE.ADMINPC);
            return connectedHosts;
        }else if (source.equals(NetworkNode.TYPE.WEBSERVER)){
            connectedHosts.add(NetworkNode.TYPE.ADMINPC);
            connectedHosts.add(NetworkNode.TYPE.DATABASE);
            return connectedHosts;
        }else if (source.equals(NetworkNode.TYPE.ADMINPC)){
            connectedHosts.add(NetworkNode.TYPE.WEBSERVER);
            connectedHosts.add(NetworkNode.TYPE.DATABASE);
            return connectedHosts;
        }else if (source.equals(NetworkNode.TYPE.DATABASE)){
            connectedHosts.add(NetworkNode.TYPE.WEBSERVER);
            connectedHosts.add(NetworkNode.TYPE.ADMINPC);
            return connectedHosts;
        }else {
            return connectedHosts;
        }
    }

    //this implements inner & outer firewall behaviour
    public static Map<NetworkNode.TYPE, List<Software>> getRemoteSWMapByScanningNode(NetworkNode.TYPE scanning){
        Map<NetworkNode.TYPE, List<Software>> visibleSoftware = new HashMap<>();
        //from all possible nodes, only check those who are actually connected
        Predicate<NetworkNode> isConnected = node -> getConnectedHosts(scanning).contains(node.getType());
        Set<NetworkNode> viewableNodes = Simulation.getSimWorld().getNodes().stream().filter(isConnected).collect(Collectors.toSet());
        //assume Adversary is scanning
        //TODO do we want to return own remote sw here aswell?
        if (scanning.equals(NetworkNode.TYPE.ADVERSARY)){
            for (NetworkNode node:viewableNodes){
                //default = drop
                Predicate<Software> isVisibleByAdv = sw -> false;
                //scans on Webserver
                if (node.getType().equals(NetworkNode.TYPE.WEBSERVER)){
                    //define outer firewall
                    List<String> webserverPublicWhitelist = List.of(Simulation.SERVICE_HTTP, Simulation.SERVICE_HTTPS, Simulation.SERVICE_PHP, Simulation.SERVICE_NGINX);
                    isVisibleByAdv = sw -> webserverPublicWhitelist.contains(sw.getName());
                }else if (node.getType().equals(NetworkNode.TYPE.ADMINPC)){
                    List<String> adminPublicWhitelist = List.of(Simulation.SERVICE_SSH);
                    isVisibleByAdv = sw -> adminPublicWhitelist.contains(sw.getName());
                }
                visibleSoftware.put(node.getType(), node.getRemoteSoftware().stream().filter(isVisibleByAdv).collect(Collectors.toList()));
            }
        }else if (scanning.equals(NetworkNode.TYPE.ROUTER)){
            //TODO define behaviour
            //should we be able to scan from the router (we never own it currently)
        }else if (scanning.equals(NetworkNode.TYPE.WEBSERVER)){
            for (NetworkNode node: viewableNodes){
                //scan self
                if (node.getType().equals(NetworkNode.TYPE.WEBSERVER)){
                    visibleSoftware.put(NetworkNode.TYPE.WEBSERVER, new ArrayList<>(node.getRemoteSoftware()));
                }
                //TODO implement inner firewall
                else if (node.getType().equals(NetworkNode.TYPE.ADMINPC)){
                    visibleSoftware.put(NetworkNode.TYPE.ADMINPC, new ArrayList<>(node.getRemoteSoftware()));
                }else if (node.getType().equals(NetworkNode.TYPE.DATABASE)){
                    visibleSoftware.put(NetworkNode.TYPE.DATABASE, new ArrayList<>(node.getRemoteSoftware()));
                }
            }
        }else {
            //Admin & Database see everything
            for (NetworkNode node: viewableNodes){
                visibleSoftware.put(node.getType(), new ArrayList<>(node.getRemoteSoftware()));
            }
        }
        return visibleSoftware;
    }
}
