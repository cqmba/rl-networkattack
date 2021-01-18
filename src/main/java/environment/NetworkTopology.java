package environment;

import run.Simulation;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NetworkTopology{
    public static Set<NetworkNode.TYPE> getConnectedHosts(NetworkNode.TYPE source){
        Set<NetworkNode.TYPE> connectedHosts = new HashSet<>();
        if(source.equals(NetworkNode.TYPE.ADVERSARY)){
            connectedHosts.add(NetworkNode.TYPE.ROUTER);
            connectedHosts.add(NetworkNode.TYPE.WEBSERVER);
            connectedHosts.add(NetworkNode.TYPE.ADMINPC);
            return connectedHosts;
        }else if (source.equals(NetworkNode.TYPE.ROUTER)){
            connectedHosts.add(NetworkNode.TYPE.WEBSERVER);
            connectedHosts.add(NetworkNode.TYPE.ADMINPC);
            connectedHosts.add(NetworkNode.TYPE.DATABASE);
            return connectedHosts;
        }else if (source.equals(NetworkNode.TYPE.WEBSERVER)){
            connectedHosts.add(NetworkNode.TYPE.ROUTER);
            connectedHosts.add(NetworkNode.TYPE.ADMINPC);
            connectedHosts.add(NetworkNode.TYPE.DATABASE);
            return connectedHosts;
        }else if (source.equals(NetworkNode.TYPE.ADMINPC)){
            connectedHosts.add(NetworkNode.TYPE.ROUTER);
            connectedHosts.add(NetworkNode.TYPE.WEBSERVER);
            connectedHosts.add(NetworkNode.TYPE.DATABASE);
            return connectedHosts;
        }else if (source.equals(NetworkNode.TYPE.DATABASE)){
            connectedHosts.add(NetworkNode.TYPE.ROUTER);
            connectedHosts.add(NetworkNode.TYPE.WEBSERVER);
            connectedHosts.add(NetworkNode.TYPE.ADMINPC);
            return connectedHosts;
        }else {
            return connectedHosts;
        }
    }

    //this implements inner & outer firewall behaviour
    public static Map<NetworkNode.TYPE, Set<Software>> getRemoteSWMapByScanningNode(NetworkNode.TYPE scanning){
        Map<NetworkNode.TYPE, Set<Software>> visibleSoftware = new EnumMap<>(NetworkNode.TYPE.class);
        //from all possible nodes, only check those who are actually connected
        Predicate<NetworkNode> isConnected = node -> getConnectedHosts(scanning).contains(node.getType());
        Set<NetworkNode> viewableNodes = Simulation.getSimWorld().getNodes().stream().filter(isConnected).collect(Collectors.toSet());
        //assume Adversary is scanning
        if (scanning.equals(NetworkNode.TYPE.ADVERSARY)){

            Predicate<Software> isVisibleByAdv;
            //scans on Router
            List<String> webserverPublicWhitelist = List.of(Simulation.SERVICE_HTTP, Simulation.SERVICE_HTTPS, Simulation.SERVICE_PHP, Simulation.SERVICE_NGINX);
            isVisibleByAdv = sw -> webserverPublicWhitelist.contains(sw.getName());
            visibleSoftware.put(NetworkNode.TYPE.WEBSERVER, Simulation.getNodeByType(NetworkNode.TYPE.WEBSERVER).getRemoteSoftware().stream().filter(isVisibleByAdv).collect(Collectors.toSet()));

            List<String> adminPublicWhitelist = List.of(Simulation.SERVICE_SSH);
            isVisibleByAdv = sw -> adminPublicWhitelist.contains(sw.getName());
            visibleSoftware.put(NetworkNode.TYPE.ADMINPC, Simulation.getNodeByType(NetworkNode.TYPE.ADMINPC).getRemoteSoftware().stream().filter(isVisibleByAdv).collect(Collectors.toSet()));
        }else if (scanning.equals(NetworkNode.TYPE.ROUTER)){
            for (NetworkNode node: viewableNodes){
                visibleSoftware.put(node.getType(), new HashSet<>(node.getRemoteSoftware()));
            }
        }else if (scanning.equals(NetworkNode.TYPE.WEBSERVER)){
            for (NetworkNode node: viewableNodes){
                //TODO implement inner firewall
                if (node.getType().equals(NetworkNode.TYPE.ADMINPC)){
                    visibleSoftware.put(NetworkNode.TYPE.ADMINPC, new HashSet<>(node.getRemoteSoftware()));
                }else if (node.getType().equals(NetworkNode.TYPE.DATABASE)){
                    visibleSoftware.put(NetworkNode.TYPE.DATABASE, new HashSet<>(node.getRemoteSoftware()));
                }
            }
        }else {
            //Admin & Database see everything
            for (NetworkNode node: viewableNodes){
                visibleSoftware.put(node.getType(), new HashSet<>(node.getRemoteSoftware()));
            }
        }
        return visibleSoftware;
    }
}
