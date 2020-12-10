package environment;

import java.util.LinkedHashSet;
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
}
