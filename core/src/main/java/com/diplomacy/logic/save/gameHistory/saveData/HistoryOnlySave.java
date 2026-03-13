package com.diplomacy.logic.save.gameHistory.saveData;

import com.diplomacy.logic.save.gameHistory.History;

class HistoryOnlySave implements SaveContainer {

    private static final long serialVersionUID = 1L;
    public final History history;

    public HistoryOnlySave(History history) {
        this.history = history;
    }

    @Override
    public SaveType getType() {
        return SaveType.HISTORY_ONLY;
    }
}
