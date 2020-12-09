package environment;

public class Data {
    //private NetworkNode source;
    private Boolean containsCredentials;
    private GAINED_KNOWLEDGE gain;
    private ORIGIN origin;
    private ACCESS_REQUIRED access;
    private Credentials credentials;
    public enum GAINED_KNOWLEDGE{
        NONE,
        LOW,
        HIGH
    }
    public enum ORIGIN {
        LOCAL,
        SNIFFED
    }
    public enum ACCESS_REQUIRED {
        USER,
        ROOT,
        ALL
    }

    public Data(GAINED_KNOWLEDGE gain, ORIGIN origin, ACCESS_REQUIRED access) {
        this.containsCredentials = false;
        this.gain = gain;
        this.origin = origin;
        this.access = access;
    }

    public Data(Credentials credentials, GAINED_KNOWLEDGE gain, ORIGIN origin, ACCESS_REQUIRED access){
        this.containsCredentials = true;
        this.credentials = credentials;
        this.gain = gain;
        this.origin = origin;
        this.access = access;
    }
}
