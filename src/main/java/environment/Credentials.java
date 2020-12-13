package environment;

public class Credentials {
    private TYPE type;
    private ACCESS_GRANT_LEVEL accessGrantLevel;
    private String useForPrivIP;
    private String useInSW;
    private NetworkNode.TYPE node;

    public enum TYPE{
        KEY,
        PASSWORD_FILE
    }
    public enum ACCESS_GRANT_LEVEL {
        USER,
        ROOT
    }

    public Credentials(TYPE type, ACCESS_GRANT_LEVEL accessGrantLevel, String useForPrivIP, String useInSW, NetworkNode.TYPE node) {
        this.type = type;
        this.accessGrantLevel = accessGrantLevel;
        this.useForPrivIP = useForPrivIP;
        this.useInSW = useInSW;
        this.node = node;
    }

    public NetworkNode.TYPE getNode() {
        return node;
    }

    public ACCESS_GRANT_LEVEL getAccessGrantLevel() {
        return accessGrantLevel;
    }

    public void setAccessGrantLevel(ACCESS_GRANT_LEVEL accessGrantLevel) {
        this.accessGrantLevel = accessGrantLevel;
    }
}
