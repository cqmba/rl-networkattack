package core;

import environment.Credentials;
import environment.Data;
import environment.NetworkNode;
import environment.NetworkWorld;
import org.junit.BeforeClass;
import org.junit.Test;
import run.Simulation;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ValidAccountsTest {
    static NetworkWorld networkWorld = Simulation.getSimWorld();
    static State stateWithValidAccount = core.State.getStartState();
    static State withOutValidAccount = core.State.getStartState();
    static NetworkNode.TYPE target = NetworkNode.TYPE.ADMINPC;
    static NetworkNode.TYPE webserver = NetworkNode.TYPE.WEBSERVER;
    static NetworkNode.TYPE currentActor = NetworkNode.TYPE.ADVERSARY;

    @BeforeClass
    public static void setUp(){
        Simulation.setupWorld(true);
        NetworkNode actualTarget = Simulation.getNodeByType(target);
        stateWithValidAccount.addNodeKnowledge(target);
        stateWithValidAccount.addNodePubIp(target, actualTarget.getPub_ip());
        stateWithValidAccount.addNodeHostname(target, actualTarget.getHostname());
        stateWithValidAccount.addNodeKnowledge(webserver);
        Credentials ssh_key = new Credentials(Credentials.TYPE.KEY, Credentials.ACCESS_GRANT_LEVEL.ROOT, actualTarget.getPriv_ip(), Simulation.SERVICE_SSH, NetworkNode.TYPE.ADMINPC);
        stateWithValidAccount.getNodeKnowledgeMap().get(webserver).addData(0, new Data(0, ssh_key, Data.GAINED_KNOWLEDGE.HIGH, Data.ORIGIN.LOCAL, Data.ACCESS_REQUIRED.USER));


        withOutValidAccount.addNodeKnowledge(target);
        withOutValidAccount.addNodePubIp(target, actualTarget.getPub_ip());
        withOutValidAccount.addNodeHostname(target, actualTarget.getHostname());

    }

    @Test
    public void testPostCondition(){
        State newState = AdversaryAction.VALID_ACCOUNTS_CRED.executePostConditionOnTarget(target,stateWithValidAccount,currentActor);
        assertTrue(newState.getNodeKnowledgeMap().get(target).hasAccessLevelRoot());
    }

    @Test
    public void testAttackableNodes(){
        Set<NetworkNode.TYPE> attackableNodes = AdversaryAction.VALID_ACCOUNTS_CRED.getTargetsWhichFulfillPrecondition(withOutValidAccount,currentActor);
        assertTrue(attackableNodes.isEmpty());
        attackableNodes = AdversaryAction.VALID_ACCOUNTS_CRED.getTargetsWhichFulfillPrecondition(stateWithValidAccount,currentActor);
        assertTrue(attackableNodes.contains(target));
        assertEquals(1,attackableNodes.size());

    }

}
