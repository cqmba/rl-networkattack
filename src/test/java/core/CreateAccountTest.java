package core;

import environment.Data;
import environment.NetworkNode;
import environment.NetworkWorld;
import org.junit.BeforeClass;
import org.junit.Test;
import run.Simulation;

import java.util.Set;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class CreateAccountTest {
    static NetworkWorld networkWorld = Simulation.getSimWorld();
    static State stateWithRootAcess = core.State.getStartState();
    static State withOutRootAcess = core.State.getStartState();
    static NetworkNode.TYPE target = NetworkNode.TYPE.WEBSERVER;
    static NetworkNode.TYPE webserver = NetworkNode.TYPE.WEBSERVER;
    static NetworkNode.TYPE currentActor = NetworkNode.TYPE.WEBSERVER;

    @BeforeClass
    public static void setUp() {
        Simulation.setupWorld();
        NetworkNode actualTarget = Simulation.getNodeByType(target);
        stateWithRootAcess.addNodeKnowledge(target);
        stateWithRootAcess.addNodePubIp(target, actualTarget.getPub_ip());
        stateWithRootAcess.addNodeHostname(target, actualTarget.getHostname());
        stateWithRootAcess.getNodeKnowledgeMap().get(target).addAccessLevel(NetworkNode.ACCESS_LEVEL.ROOT);

        withOutRootAcess.addNodeKnowledge(target);
        withOutRootAcess.addNodePubIp(target, actualTarget.getPub_ip());
        withOutRootAcess.addNodeHostname(target, actualTarget.getHostname());
        withOutRootAcess.getNodeKnowledgeMap().get(target).addAccessLevel(NetworkNode.ACCESS_LEVEL.USER);
    }

    @Test
    public void testPostCondition(){
        Predicate<Data> isCrediential;
        isCrediential = d -> d.containsCredentials();
        assertFalse(stateWithRootAcess.getNodeKnowledgeMap().get(target).getKnownData().values().stream().anyMatch(isCrediential));
        State newState = AdversaryAction.CREATE_ACCOUNT.executePostConditionOnTarget(target,stateWithRootAcess,currentActor);
        assertTrue(newState.getNodeKnowledgeMap().get(target).getKnownData().values().stream().anyMatch(isCrediential));
    }

    @Test
    public void testAttackableNodes(){
        Set<NetworkNode.TYPE> attackableNodes = AdversaryAction.CREATE_ACCOUNT.getTargetsWhichFulfillPrecondition(withOutRootAcess,currentActor);
        assertTrue(attackableNodes.isEmpty());
        attackableNodes = AdversaryAction.CREATE_ACCOUNT.getTargetsWhichFulfillPrecondition(stateWithRootAcess,currentActor);
        assertTrue(attackableNodes.contains(target));
        assertEquals(1, attackableNodes.size());

    }

}
