package knowledge.impl;

import com.google.gson.Gson;
import environment.Vulnerability;
import knowledge.SoftwareKnowledge;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class implements the knowledge of an adversary about a single software product.
 */
public class SoftwareKnowledgeImpl implements SoftwareKnowledge, Serializable {
    private final String name;
    private String version;
    private final Set<Vulnerability> vulnerabilities;
    private final boolean remote;

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

        return Objects.hash(name, version, vulnerabilities, remote);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
