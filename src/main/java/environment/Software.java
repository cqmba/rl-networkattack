package environment;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Software {
    private String name = "";
    private String version = "";
    private Set<Vulnerability> vulnerabilities = new LinkedHashSet<>();

    public Software(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public Set<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void addVulnerability(Vulnerability vulnerability){
        vulnerabilities.add(vulnerability);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Software)) return false;
        Software software = (Software) o;
        return Objects.equals(name, software.name) &&
                Objects.equals(version, software.version) &&
                Objects.equals(vulnerabilities, software.vulnerabilities);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, version, vulnerabilities);
    }
}
