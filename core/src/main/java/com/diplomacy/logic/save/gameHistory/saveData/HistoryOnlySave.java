package com.diplomacy.logic.save.gameHistory.saveData;

import com.diplomacy.logic.save.GameFrame;
import com.diplomacy.logic.save.gameHistory.History;

class HistoryOnlySave implements SaveContainer {

    private static final long serialVersionUID = 1L;
    public final History history;
    public final GameFrame init;

    public HistoryOnlySave(History history, GameFrame init) {
        this.history = history;
        this.init = init;
    }

    @Override
    public SaveType getType() {
        return SaveType.HISTORY_ONLY;
    }

    public History getHistory() {
        return history;
    }

    public GameFrame getInit() {
        return init;
    }

}
