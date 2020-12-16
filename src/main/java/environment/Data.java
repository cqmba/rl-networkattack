package environment;

import java.io.Serializable;
import java.util.Objects;

public class Data implements Serializable {
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
        SNIFFED,
        CREATED
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

    public Boolean containsCredentials() {
        return containsCredentials;
    }


    public GAINED_KNOWLEDGE getGain() {
        return gain;
    }

    public void setGain(GAINED_KNOWLEDGE gain) {
        this.gain = gain;
    }

    public ORIGIN getOrigin() {
        return origin;
    }

    public void setOrigin(ORIGIN origin) {
        this.origin = origin;
    }

    public ACCESS_REQUIRED getAccess() {
        return access;
    }

    public void setAccess(ACCESS_REQUIRED access) {
        this.access = access;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Data)) return false;
        Data data = (Data) o;
        return Objects.equals(containsCredentials, data.containsCredentials) &&
                gain == data.gain &&
                origin == data.origin &&
                access == data.access &&
                Objects.equals(credentials, data.credentials);
    }

    @Override
    public int hashCode() {

        return Objects.hash(containsCredentials, gain, origin, access, credentials);
    }
}
