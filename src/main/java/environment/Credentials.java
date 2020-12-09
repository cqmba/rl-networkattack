package environment;

public class Credentials {
    private TYPE type;
    private ACCESS_GRANT_LEVEL accessGrantLevel;
    private String useForPrivIP;
    private String useInSW;
    public enum TYPE{
        KEY,
        PASSWORD_FILE
    }
    public enum ACCESS_GRANT_LEVEL {
        USER,
        ROOT
    }

    public Credentials(TYPE type, ACCESS_GRANT_LEVEL accessGrantLevel, String useForPrivIP, String useInSW) {
        this.type = type;
        this.accessGrantLevel = accessGrantLevel;
        this.useForPrivIP = useForPrivIP;
        this.useInSW = useInSW;
    }
}
