package knowledge;

import knowledge.impl.SoftwareKnowledgeImpl;

public interface SoftwareKnowledge {

    static SoftwareKnowledge addNew(String service) {
        return new SoftwareKnowledgeImpl(service);
    }

    boolean versionIsKnown(String version);

    //Vulnerabilities
}
