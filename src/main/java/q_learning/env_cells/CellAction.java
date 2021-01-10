package q_learning.env_cells;

import aima.core.agent.Action;

/**
 * A simple implementation of an Action.
 * The Scenario is a cellworld. The Actions are moving Up, Down, Left and Right.
 */
public class CellAction implements Action {
    private final int direction;

    public CellAction(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    @Override
    public boolean isNoOp() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CellAction) {
            CellAction other = (CellAction) o;
            return this.direction == other.direction;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return direction;
    }

    @Override
    public String toString() {
        switch (direction) {
            case 0: return "UP";
            case 1: return "DOWN";
            case 2: return "LEFT";
            case 3: return "RIGHT";
            default: return "Error";
        }
    }
}
