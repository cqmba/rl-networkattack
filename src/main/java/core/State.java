package core;


import environment.NetworkNode;
import knowledge.NodeKnowledgeImpl;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class State implements Serializable {
    Set<NodeKnowledgeImpl> knowledgeSet = new LinkedHashSet<>();
    Map<Integer, NetworkNode> nodes = new LinkedHashMap<>();
    public static Set<AdversaryAction> computePossibleActions(){
        //TODO
        return null;
    }

    public Set<NodeKnowledgeImpl> getKnowledgeSet() {
        return knowledgeSet;
    }
}
