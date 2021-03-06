package q_learning.env_cells;

import java.io.Serializable;
import java.util.Objects;

/**
 * A simple implementation of a state.
 * The Scenario is a cellworld. Each state is therefore a cell with x and y coordinates, where 0,0 is the top left.
 */
public class CellState implements Serializable {
    private final int x;
    private final int y;

    /**
     * The state of the MDP. 0, 0 is the top left corner
     * @param x x-coordinate
     * @param y y coordinate
     */
    public CellState(int x, int y) {
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
        if (!(o instanceof CellState))
            return false;

        CellState other = (CellState) o;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return String.format("<%d, %d>", x, y);
    }
}
