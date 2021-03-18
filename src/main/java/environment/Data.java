package environment;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class implements all metadata attributed to a data object
 */
public class Data implements Serializable {
    private final boolean containsCredentials;
    private GAINED_KNOWLEDGE gain;
    private final ORIGIN origin;
    private final ACCESS_REQUIRED access;
    private Credentials credentials = null;
    private final int ID;
    public enum GAINED_KNOWLEDGE{
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

    public Data(int ID, GAINED_KNOWLEDGE gain, ORIGIN origin, ACCESS_REQUIRED access) {
        this.ID = ID;
        this.containsCredentials = false;
        this.gain = gain;
        this.origin = origin;
        this.access = access;
    }

    public Data(int ID, Credentials credentials, GAINED_KNOWLEDGE gain, ORIGIN origin, ACCESS_REQUIRED access){
        this.ID = ID;
        this.containsCredentials = true;
        this.credentials = credentials;
        this.gain = gain;
        this.origin = origin;
        this.access = access;
    }

    public boolean containsCredentials() {
        return containsCredentials;
    }

    public int getID() {
        return ID;
    }

    public GAINED_KNOWLEDGE getGain() {
        return gain;
    }

    public void setGain(GAINED_KNOWLEDGE gain) {
        this.gain = gain;
    }

    public ACCESS_REQUIRED getAccess() {
        return access;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Data)) return false;
        Data data = (Data) o;
        return Objects.equals(containsCredentials, data.containsCredentials) &&
                Objects.equals(ID, data.ID) &&
                Objects.equals(gain, data.gain) &&
                Objects.equals(origin, data.origin) &&
                Objects.equals(access, data.access) &&
                Objects.equals(credentials, data.credentials);
    }

    @Override
    public int hashCode() {

        return Objects.hash(ID, containsCredentials, gain, origin, access, credentials);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
