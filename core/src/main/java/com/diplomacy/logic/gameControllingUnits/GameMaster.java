package com.diplomacy.logic.gameControllingUnits;

import java.util.ArrayList;
import java.util.List;

import com.diplomacy.logic.gameControllingUnits.phase.Phase;
import com.diplomacy.logic.gameControllingUnits.utils.Executor;
import com.diplomacy.logic.geography.advanced.Map;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.save.gameHistory.History;
import com.diplomacy.logic.turnClassificator.TurnClassificator;
import com.diplomacy.logic.units.Unit;

public class GameMaster {

    public TurnClassificator turn;
    public Phase phase;
    public final List<Player> players;
    public final Map map;
    public final History history;
    public final Executor executor;

    public GameMaster(List<Player> players, Map map, History history, Executor executor) {
        if (history.getLastHistoryPhase() != null) {
            this.turn = history.getLastTurn();
        }
        this.players = players;
        this.map = map;
        this.history = history;
        this.executor = executor;
    }

    public GameMaster(List<Player> players, Map map, TurnClassificator turn, Executor executor) {
        this.turn = turn;
        this.players = players;
        this.map = map;
        this.history = new History();
        this.executor = executor;
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
        if (!phase.ableNextPhase()) {
            return false;
        }
        phase.operate();
        turn.nextTurn();
        phase = turn.getPhaseClass();

        return true;
    }

    public List<Unit> getRetreating() {
        List<Unit> retreating = new ArrayList<>();
        for (Player p : players) {
            for (Unit u : p.getUnits()) {
                if (u.isRetreating()) {
                    retreating.add(u);
                }
            }
        }
        return retreating;
    }

    public List<Unit> getUnits() {
        List<Unit> units = new ArrayList<>();
        for (Player p : players) {
            units.addAll(p.getUnits());
        }
        return units;
    }

    public Executor getExecutor() {
        return executor;
    }
}
