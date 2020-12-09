package environment;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Software {
    private String name = "";
    private String version = "";
    private Set<Vulnerability> vulnerabilities = new LinkedHashSet<>();

    //TODO needs modelling of remote visibility (firewall blocked or passing?)

    public Software(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public Set<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(Set<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public void addVulnerability(Vulnerability vulnerability){
        vulnerabilities.add(vulnerability);
    }

    public void addVulnerabilityList(List<Vulnerability> vulnerabilities){
        vulnerabilities.addAll(vulnerabilities);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}
