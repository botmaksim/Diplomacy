package com.diplomacy.logic.save.gameHistory.saveData;

import java.util.concurrent.Executor;

import com.diplomacy.logic.save.GameFrame;
import com.diplomacy.logic.save.gameHistory.History;

class FullGameSave implements SaveContainer {

    private static final long serialVersionUID = 1L;
    public final History history;
    public final GameFrame init, save;
    public final Executor executor;

    public FullGameSave(History history, GameFrame init, GameFrame save, Executor executor) {
        this.history = history;
        this.init = init;
        this.save = save;
        this.executor = executor;
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

    public Executor getExecutor() {
        return executor;
    }

    public GameFrame getSave() {
        return save;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}
