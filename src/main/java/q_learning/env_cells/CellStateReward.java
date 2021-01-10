package q_learning.env_cells;

import q_learning.interfaces.StateReward;

public class CellStateReward implements StateReward<CellState, CellAction> {
    private final CellState state;
    private final double reward;

    public CellStateReward(CellState state, double reward) {
        this.state = state;
        this.reward = reward;
    }

    @Override
    public double reward(CellAction action, CellState targetState) {
        return reward;
    }

    @Override
    public CellState state() {
        return state;
    }
}
