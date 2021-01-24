package core;

import environment.Data;
import environment.NetworkNode;
import environment.NetworkWorld;
import knowledge.NodeKnowledge;
import knowledge.SoftwareKnowledge;
import org.junit.BeforeClass;
import org.junit.Test;
import run.Simulation;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class ActiveScanningIPTest {
    static NetworkWorld networkWorld = Simulation.getSimWorld();
    static State startState = core.State.getStartState();
    static State testState = core.State.getStartState();
    static NetworkNode.TYPE target = NetworkNode.TYPE.ROUTER;
    static NetworkNode.TYPE currentActor = NetworkNode.TYPE.ADVERSARY;

    @BeforeClass
    public static void setUp() {
        Simulation.setupWorld(true);
        startState.addNodeKnowledge(target);
    }

    @Test
    public void testPostCondition(){
        //Test that new nodes are added to both Network and NodeKnowledge and IP, Hostname and Port information was added
        Set<NetworkNode.TYPE> addedNodes = Set.of(NetworkNode.TYPE.WEBSERVER, NetworkNode.TYPE.ADMINPC);
        for (NetworkNode.TYPE node : addedNodes){
            assertFalse(startState.getNetworkKnowledge().getKnownNodes().contains(node));
        }
        Set<NetworkNode.TYPE> knownNodes = startState.getNodeKnowledgeMap().keySet();
        assertEquals(2, knownNodes.size());
        assertTrue(knownNodes.contains(NetworkNode.TYPE.ROUTER));
        assertTrue(knownNodes.contains(NetworkNode.TYPE.ADVERSARY));

        Map<NetworkNode.TYPE, NodeKnowledge> oldKnowledgeMap = startState.getNodeKnowledgeMap();
        Set<NetworkNode.TYPE> oldKnownNodesActual = oldKnowledgeMap.keySet();
        assertEquals(2, oldKnownNodesActual.size());
        assertTrue(oldKnowledgeMap.containsKey(NetworkNode.TYPE.ROUTER));
        assertTrue(oldKnowledgeMap.containsKey(NetworkNode.TYPE.ADVERSARY));
        //Execute
        State newState = AdversaryAction.ACTIVE_SCAN_IP_PORT.executePostConditionOnTarget(target,startState,currentActor);
        Set<NetworkNode.TYPE> newKnownNodes = newState.getNetworkKnowledge().getKnownNodes();
        assertEquals(4, newKnownNodes.size());
        assertTrue(newKnownNodes.containsAll(addedNodes));

        Map<NetworkNode.TYPE, NodeKnowledge> nodeKnowledgeMap = newState.getNodeKnowledgeMap();
        Set<NetworkNode.TYPE> newKnownNodesActual = nodeKnowledgeMap.keySet();
        assertEquals(4, newKnownNodesActual.size());
        assertTrue(nodeKnowledgeMap.containsKey(NetworkNode.TYPE.WEBSERVER));
        assertTrue(nodeKnowledgeMap.containsKey(NetworkNode.TYPE.ADMINPC));

        NodeKnowledge webserverKnowledge = nodeKnowledgeMap.get(NetworkNode.TYPE.WEBSERVER);
        assertTrue(webserverKnowledge.hasPubIp());
        assertTrue(webserverKnowledge.hasHostname());

        Set<SoftwareKnowledge> wsSoftware = newState.getSoftwareKnowledgeMap().get(NetworkNode.TYPE.WEBSERVER);
        assertEquals(4, wsSoftware.size());
        Set<String> expected = Set.of(Simulation.SERVICE_HTTP, Simulation.SERVICE_HTTPS, Simulation.SERVICE_NGINX, Simulation.SERVICE_PHP);
        for (SoftwareKnowledge sw: wsSoftware){
            assertTrue(expected.contains(sw.getName()));
        }
    }

    @Test
    public void testAttackableNodes(){
        Set<NetworkNode.TYPE> attackableNodes = AdversaryAction.ACTIVE_SCAN_IP_PORT.getTargetsWhichFulfillPrecondition(startState,currentActor);
        assertEquals(1,attackableNodes.size());
        assertTrue(attackableNodes.contains(target));
    }
}
