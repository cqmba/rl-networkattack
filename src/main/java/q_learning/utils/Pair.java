package q_learning.utils;

import java.io.Serializable;
import java.util.Objects;

/**
 * A Pair class, since the one from AIMA was poorly implemented and not allowing for null values (even though they
 * set null values in their own code)
 * @param <A> The first Value
 * @param <B> The second Value
 */
public class Pair<A extends Serializable, B extends Serializable> implements Serializable {
    private final A a;
    private final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Pair) {
            Pair other = (Pair) o;
            return Objects.equals(this.a, other.a) && Objects.equals(this.b, other.b);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public String toString() {
        return "[" + Objects.toString(a) + ", " + Objects.toString(b)
                + "]";
    }
}
