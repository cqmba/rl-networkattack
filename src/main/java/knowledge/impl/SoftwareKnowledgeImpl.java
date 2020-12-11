package knowledge.impl;

import knowledge.SoftwareKnowledge;

public class SoftwareKnowledgeImpl implements SoftwareKnowledge {
    private String name;

    public SoftwareKnowledgeImpl(String name) {
        this.name = name;
    }

    @Override
    public boolean versionIsKnown(String version) {
        return false;
    }
}
