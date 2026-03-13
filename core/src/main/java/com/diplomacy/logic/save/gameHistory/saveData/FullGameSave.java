package com.diplomacy.logic.save.gameHistory.saveData;

import java.util.List;

import com.diplomacy.logic.geography.advanced.Map;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.save.gameHistory.History;

class FullGameSave implements SaveContainer {

    private static final long serialVersionUID = 1L;
    public final History history;
    public final Map map;
    public final List<Player> players;

    public FullGameSave(History history, Map map, List<Player> players) {
        this.history = history;
        this.map = map;
        this.players = players;
    }

    @Override
    public SaveType getType() {
        return SaveType.FULL;
    }
}
