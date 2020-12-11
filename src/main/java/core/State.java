package core;


import knowledge.NodeKnowledge;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public class State implements Serializable {
    Set<NodeKnowledge> knowledgeSet = new LinkedHashSet<>();

    public static State getStartState(){
        return new State();
    }

    public static Set<AdversaryAction> computePossibleActions(){
        //TODO
        return null;
    }

    public Set<NodeKnowledge> getKnowledgeSet() {
        return knowledgeSet;
    }
}
