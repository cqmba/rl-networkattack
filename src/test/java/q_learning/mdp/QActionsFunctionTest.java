package q_learning.mdp;

import org.junit.Test;
import q_learning.env_cells.CellAction;
import q_learning.env_cells.CellState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

public class QActionsFunctionTest {

    @Test
    public void addAction_happyPath() {
        Map<CellState, Double> states = new HashMap<>();
        CellState state = new CellState(0, 0);
        states.put(state, 0.0);
        CellAction action = new CellAction(0);
        QActionsFunction<CellState, CellAction> QAF = new QActionsFunction<>(states);
        QAF.addAction(state, action);

        assertEquals(new HashSet<CellAction>() {{ add(action); }}, QAF.actions(state));
    }

    @Test(expected = NullPointerException.class)
    public void addAction_noValidState_angryPath() {
        Map<CellState, Double> states = new HashMap<>();
        CellState state = new CellState(0, 0);
        states.put(state, 0.0);

        CellState notAddedState = new CellState(1, 1);

        CellAction action = new CellAction(0);
        QActionsFunction<CellState, CellAction> QAF = new QActionsFunction<>(states);

        QAF.addAction(notAddedState, action);
    }
}