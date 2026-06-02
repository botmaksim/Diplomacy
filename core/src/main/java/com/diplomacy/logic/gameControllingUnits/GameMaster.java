package com.diplomacy.logic.gameControllingUnits;

import java.util.ArrayList;
import java.util.List;

import com.diplomacy.logic.gameControllingUnits.phase.Phase;
import com.diplomacy.logic.gameControllingUnits.utils.Executor;
import com.diplomacy.logic.geography.advanced.GameMap;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.save.gameHistory.History;
import com.diplomacy.logic.turnClassificator.TurnClassificator;
import com.diplomacy.logic.units.Unit;

public class GameMaster {

    public TurnClassificator turn;
    public Phase phase;
    public final List<Player> players;
    public final GameMap map;
    public final History history;
    public final Executor executor;

    public GameMaster(List<Player> players, GameMap map, History history, Executor executor) {
        if (history != null && history.getLastTurn() != null) {
            this.turn = history.getLastTurn();
        } else {
            this.turn = new TurnClassificator();
        }
        this.phase = turn.getPhaseClass();
        if (this.phase != null) {
            this.phase.setGameMaster(this);
        }
        this.players = players;
        this.map = map;
        this.history = history;
        this.executor = executor;
    }

    public GameMaster(List<Player> players, GameMap map, TurnClassificator turn, Executor executor) {
        this.turn = turn;
        this.phase = turn.getPhaseClass();
        if (this.phase != null) {
            this.phase.setGameMaster(this);
        }
        this.players = players;
        this.map = map;
        this.history = new History();
        this.executor = executor;
    }

    public GameMaster(List<Player> players, GameMap map, Executor executor) {
        this(players, map, new History(), executor);
    }

    public GameMaster(List<Player> players, GameMap map) {
        this(players, map, new Executor());
    }

    public void addOrder(Order order, Player player) {
        if (phase != null) {
            phase.addOrder(order, player);
        }
    }

    public List<Order> getPendingOrders(Player player) {
        if (phase == null) return new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Order> orders = (List<Order>) phase.getOrders(player);
        return orders;
    }

    public void removeOrder(Player player, int index) {
        if (phase != null) {
            phase.removeOrder(player, index);
        }
    }

    public void clearAllPendingOrders() {
        if (phase != null) {
            phase.clearAllOrders();
        }
    }

    public void updatePhaseObject() {
        this.phase = turn.getPhaseClass();
        if (this.phase != null) {
            this.phase.setGameMaster(this);
        }
    }

    public GameMap getMap() {
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
        if (phase == null || !phase.ableNextPhase()) {
            return false;
        }
        phase.operate();
        turn.nextTurn();
        updatePhaseObject();
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
