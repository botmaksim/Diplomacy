package com.diplomacy.logic.save.gameHistory.saveData;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.save.GameFrame;
import com.diplomacy.logic.save.gameHistory.History;

class FullGameSave implements SaveContainer {

    private static final long serialVersionUID = 1L;
    public final History history;
    public final GameFrame init;
    public final GameMaster gameMaster;

    public FullGameSave() {
        this.history = null;
        this.init = null;
        this.gameMaster = null;
    }

    @Override
    public SaveType getType() {
        return SaveType.FULL;
    }

    public History getHistory() {
        return history;
    }

    public GameFrame getInit() {
        return init;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public GameMaster getGameMaster() {
        return gameMaster;
    }
}
