package knowledge;

import environment.Vulnerability;
import knowledge.impl.SoftwareKnowledgeImpl;

import java.util.Set;

public interface SoftwareKnowledge {

    static SoftwareKnowledge addNew(String service) {
        return new SoftwareKnowledgeImpl(service);
    }

    //Vulnerabilities
    void addVersion(String version);
    void addVulnerabilities(Set<Vulnerability> vulnerabilities);
    String getName();
}
