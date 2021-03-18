package environment;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class defines all metadata attributed to a credential object, which can be used to gain access to a particular host
 */
public class Credentials implements Serializable {
    private final TYPE type;
    private final ACCESS_GRANT_LEVEL accessGrantLevel;
    private final String useForPrivIP;
    private final String useInSW;
    private final NetworkNode.TYPE node;

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

    public TYPE getType() {
        return type;
    }

    public String getUseInSW() {
        return useInSW;
    }

    public NetworkNode.TYPE getNode() {
        return node;
    }

    public ACCESS_GRANT_LEVEL getAccessGrantLevel() {
        return accessGrantLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Credentials)) return false;
        Credentials that = (Credentials) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(accessGrantLevel, that.accessGrantLevel) &&
                Objects.equals(useForPrivIP, that.useForPrivIP) &&
                Objects.equals(useInSW, that.useInSW) &&
                Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, accessGrantLevel, useForPrivIP, useInSW, node);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
