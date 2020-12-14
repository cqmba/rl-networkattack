package knowledge.impl;

import environment.Vulnerability;
import knowledge.SoftwareKnowledge;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


public class SoftwareKnowledgeImpl implements SoftwareKnowledge, Serializable {
    private String name;
    private String version;
    private Set<Vulnerability> vulnerabilities = new HashSet<>();

    public SoftwareKnowledgeImpl(String name) {
        this.name = name;
    }


    @Override
    public void addVersion(String version) {
        this.version = version;
    }

    @Override
    public void addVulnerabilities(Set<Vulnerability> vulnerabilities) {
        this.vulnerabilities.addAll(vulnerabilities);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<Vulnerability> getVulnerability() {
        return this.vulnerabilities;
    }
}