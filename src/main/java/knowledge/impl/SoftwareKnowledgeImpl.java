package knowledge.impl;

import environment.Vulnerability;
import knowledge.SoftwareKnowledge;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class SoftwareKnowledgeImpl implements SoftwareKnowledge, Serializable {
    private String name;
    private String version;
    private Set<Vulnerability> vulnerabilities;
    private boolean remote;

    public SoftwareKnowledgeImpl(String name, boolean remote) {
        this.name = name;
        this.version = "";
        this.vulnerabilities = new HashSet<>();
        this.remote = remote;
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
    public String getVersion() {
        return version;
    }

    @Override
    public Set<Vulnerability> getVulnerabilities() {
        return this.vulnerabilities;
    }

    @Override
    public boolean hasVersion() {
        return !version.isEmpty();
    }

    @Override
    public boolean hasVulnerability() {
        return !vulnerabilities.isEmpty();
    }

    @Override
    public boolean isRemote() {
        return remote;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoftwareKnowledgeImpl)) return false;
        SoftwareKnowledgeImpl that = (SoftwareKnowledgeImpl) o;
        return remote == that.remote &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(vulnerabilities, that.vulnerabilities);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, remote);
    }
}
