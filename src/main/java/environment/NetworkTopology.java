package environment;

import com.google.gson.Gson;
import run.Simulation;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class implements the network topology that was presented in our project report.
 */
public class NetworkTopology{
    /**
     * This method returns all connected hosts of a given host
     * @param source - given host
     * @return - all connected hosts
     */
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

    /**
     * This method returns a map of available nodes with their remote software, from the position of a scanning node.
     * Thus this effectively implements the firewall and the router port forwarding.
     * @param scanning - scanning or source node
     * @return - map containing all scannable hosts with exactly the remote software, that is visible from the
     * perspective of the scanning node
     */
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

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
