package knowledge;

import environment.Vulnerability;
import knowledge.impl.SoftwareKnowledgeImpl;

import java.util.Set;

public interface SoftwareKnowledge {

    static SoftwareKnowledge addNew(String service, boolean remote) {
        return new SoftwareKnowledgeImpl(service, remote);
    }

    //Vulnerabilities
    void addVersion(String version);
    void addVulnerabilities(Set<Vulnerability> vulnerabilities);
    String getName();
    String getVersion();
    Set<Vulnerability> getVulnerabilities();
    boolean hasVersion();
    boolean hasVulnerability();
    boolean isRemote();
}
