package ActionTests;

import core.AdversaryAction;
import core.State;
import environment.*;
import knowledge.SoftwareKnowledge;
import knowledge.impl.SoftwareKnowledgeImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import run.Simulation;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class SoftwareDiscoveryTest {
    static NetworkWorld networkWorld = Simulation.getSimWorld();
    static State state = core.State.getStartState();
    static NetworkNode.TYPE target = NetworkNode.TYPE.WEBSERVER;
    static NetworkNode.TYPE currentActor = NetworkNode.TYPE.WEBSERVER;
    static Set<Software> sw = new LinkedHashSet<>();
    static Set<SoftwareKnowledge> softwareKnowledgeSet = new HashSet<>();

    @BeforeClass
    public static void setUp(){
        Simulation.setupWorld();
        // add  some local software to node
        Software http = new Software(Simulation.SERVICE_HTTP, "2");
        http.addVulnerability(new Vulnerability("", Vulnerability.TYPE.CREDENTIAL_LEAK, false));
        sw.add(http);
        SoftwareKnowledge httpKnowledge = new SoftwareKnowledgeImpl(http.getName(),false);
        httpKnowledge.addVersion(http.getVersion());
        httpKnowledge.addVulnerabilities(http.getVulnerabilities());
        softwareKnowledgeSet.add(httpKnowledge);

        Software https = new Software(Simulation.SERVICE_HTTPS, "1.3");
        https.addVulnerability(new Vulnerability("", Vulnerability.TYPE.CREDENTIAL_LEAK, false));
        sw.add(https);
        SoftwareKnowledge httpsKnowledge = new SoftwareKnowledgeImpl(https.getName(),false);
        httpsKnowledge.addVersion(https.getVersion());
        httpsKnowledge.addVulnerabilities(https.getVulnerabilities());
        softwareKnowledgeSet.add(httpsKnowledge);

        //get all software information on the target
        NetworkNode actualTarget = Simulation.getNodeByType(target);
        actualTarget.setLocalSoftware(sw);
        state.addNodeKnowledge(target);
        state.addNodePubIp(target, actualTarget.getPub_ip());
        state.addNodeHostname(target, actualTarget.getHostname());
        state.getNodeKnowledgeMap().get(currentActor).addAccessLevel(NetworkNode.ACCESS_LEVEL.USER);

    }

    @Test
    public void testPostCondition(){

        assertFalse(state.getSoftwareKnowledgeMap().containsKey(target));

        State newState = AdversaryAction.SOFTWARE_DISCOVERY.executePostConditionOnTarget(target,state,currentActor);

        Set<SoftwareKnowledge> softwareKnowledgeSetAfterAction = newState.getSoftwareKnowledgeMap().get(target);
        for(SoftwareKnowledge softwareKnowledge : softwareKnowledgeSet){
            assertTrue(softwareKnowledgeSetAfterAction.contains(softwareKnowledge));
        }


    }

    @Test
    public void testAttackableNodes(){
        Set<NetworkNode.TYPE> attackableNodes = AdversaryAction.SOFTWARE_DISCOVERY.getTargetsWhichFulfillPrecondition(state,currentActor);
        assertTrue(attackableNodes.contains(target));
        assertEquals(attackableNodes.size(),1);
    }

}
