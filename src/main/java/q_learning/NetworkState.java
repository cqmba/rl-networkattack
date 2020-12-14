package q_learning;

/**
 * A simple implementation of a state.
 * The Scenario is a cellworld. Each state is therefore a cell with x and y coordinates, where 0,0 is the top left.
 */
public class NetworkState {
    private final int x;
    private final int y;

    public NetworkState(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof NetworkState))
            return false;

        NetworkState other = (NetworkState) o;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return (100 * x) + y;
    }

    @Override
    public String toString() {
        return String.format("<%d, %d>", x, y);
    }
}
