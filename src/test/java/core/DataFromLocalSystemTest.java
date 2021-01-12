package core;

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

public class DataFromLocalSystemTest {

    static NetworkWorld networkWorld = Simulation.getSimWorld();
    static State state = core.State.getStartState();
    static NetworkNode.TYPE target = NetworkNode.TYPE.WEBSERVER;
    static NetworkNode.TYPE currentActor = NetworkNode.TYPE.WEBSERVER;
    static Set<Software> sw = new LinkedHashSet<>();
    static Map<Integer, Data> dataOnWebserver;

    @BeforeClass
    public static void setUp(){
        Simulation.setupWorld();

        NetworkNode actualTarget = Simulation.getNodeByType(target);
        dataOnWebserver = actualTarget.getDataSet();
        state.addNodeKnowledge(target);
        state.addNodePubIp(target, actualTarget.getPub_ip());
        state.addNodeHostname(target, actualTarget.getHostname());
        state.getNodeKnowledgeMap().get(currentActor).addAccessLevel(NetworkNode.ACCESS_LEVEL.ROOT);

    }

    @Test
    public void testPostCondition(){
        Map<Integer, Data> DataKnowledgeSetBeforeAction = state.getNodeKnowledgeMap().get(target).getKnownData();
        for(Integer ID : dataOnWebserver.keySet()){
            assertFalse(DataKnowledgeSetBeforeAction.containsKey(ID));
        }

        assertFalse(state.getSoftwareKnowledgeMap().containsKey(target));

        State newState = AdversaryAction.DATA_FROM_LOCAL_SYSTEM.executePostConditionOnTarget(target,state,currentActor);

        Map<Integer, Data> DataKnowledgeSetAfterAction = newState.getNodeKnowledgeMap().get(target).getKnownData();
        for(Integer ID : dataOnWebserver.keySet()){
            assertTrue(DataKnowledgeSetAfterAction.containsKey(ID));
        }


    }

    @Test
    public void testAttackableNodes(){
        Set<NetworkNode.TYPE> attackableNodes = AdversaryAction.DATA_FROM_LOCAL_SYSTEM.getTargetsWhichFulfillPrecondition(state,currentActor);
        assertTrue(attackableNodes.contains(target));
        assertEquals(attackableNodes.size(),1);
    }

}
