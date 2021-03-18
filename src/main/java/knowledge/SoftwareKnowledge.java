package knowledge;

import environment.Vulnerability;
import knowledge.impl.SoftwareKnowledgeImpl;

import java.util.Set;

public interface SoftwareKnowledge {

    /**
     * This method creates a new instance for a particular software product.
     * @param service - the software name
     * @param remote - if the software is remote
     * @return - a new instance
     */
    static SoftwareKnowledge addNew(String service, boolean remote) {
        return new SoftwareKnowledgeImpl(service, remote);
    }

    void addVersion(String version);
    void addVulnerabilities(Set<Vulnerability> vulnerabilities);
    String getName();
    String getVersion();
    Set<Vulnerability> getVulnerabilities();
    boolean hasVersion();
    boolean hasVulnerability();
    boolean isRemote();
}
