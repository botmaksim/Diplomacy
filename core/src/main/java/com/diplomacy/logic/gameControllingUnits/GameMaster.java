package com.diplomacy.logic.gameControllingUnits;

import java.util.List;

import com.diplomacy.logic.gameControllingUnits.phase.Phase;
import com.diplomacy.logic.geography.advanced.Map;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.save.gameHistory.History;
import com.diplomacy.logic.turnClassificator.TurnClassificator;

public class GameMaster {

    public TurnClassificator turn;
    public Phase phase;
    public final List<Player> players;
    public final Map map;
    public final History history;

    public GameMaster(List<Player> players, Map map, History history) {
        if (history.getLastHistoryPhase() != null) {
            this.turn = history.getLastTurn();
        }
        this.players = players;
        this.map = map;
        this.history = history;
    }

    public GameMaster(List<Player> players, Map map, TurnClassificator turn) {
        this.turn = turn;
        this.players = players;
        this.map = map;
        this.history = new History();
    }

    public Map getMap() {
        return map;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public History getHistory() {
        return history;
    }

    public TurnClassificator getTurn() {
        return turn;
    }

    public Phase getPhase() {
        return phase;
    }

    public boolean nextTurn() {
        return true;
    }

}
