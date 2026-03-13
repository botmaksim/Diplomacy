package com.diplomacy.logic.save.gameHistory;

import java.util.List;

import com.diplomacy.logic.turnClassificator.TurnClassificator;

public class History {

    private List<HistoryPhase> history = null;

    public History() {
    }

    public History(History fullHistory, TurnClassificator lastTurn) {
        for (HistoryPhase t : history) {
            if (t.getTurn().equals(lastTurn));
            history.add(t);
        }
    }

    public History(List<HistoryPhase> history) {
        this.history = history;
    }

    public void addHistoryPhase(HistoryPhase hp) {
        history.add(hp);
    }

    public HistoryPhase getLastHistoryPhase() {
        return history.getLast();
    }

    public HistoryPhase getHistoryPhase(TurnClassificator turn) {
        int l = 0;
        int r = history.size() - 1;

        while (l <= r) {
            int m = l + (r - l) / 2;

            if (history.get(m).getTurn().equals(turn)) {
                return history.get(m);
            }

            if (history.get(m).getTurn().less(turn)) {
                l = m + 1;
            } else {
                r = m - 1;
            }
        }

        return null;
    }

    public TurnClassificator getLastTurn() {
        return history.getLast().getTurn();
    }
}
