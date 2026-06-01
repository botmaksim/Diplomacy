package com.diplomacy.logic.gameControllingUnits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diplomacy.logic.gameControllingUnits.phase.Phase;
import com.diplomacy.logic.gameControllingUnits.utils.Executor;
import com.diplomacy.logic.geography.advanced.Country;
import com.diplomacy.logic.geography.advanced.GameMap;
import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.save.gameHistory.History;
import com.diplomacy.logic.turnClassificator.TurnClassificator;
import com.diplomacy.logic.units.Army;
import com.diplomacy.logic.units.Fleet;
import com.diplomacy.logic.units.Unit;

public class GameMaster {

    public TurnClassificator turn;
    public Phase phase;
    public final List<Player> players;
    public final GameMap map;
    public final History history;
    public final Executor executor;
    private final Map<Player, List<Order>> pendingOrdersByPlayer = new HashMap<>();

    public GameMaster(List<Player> players, GameMap map, History history, Executor executor) {
        if (history != null && history.getLastTurn() != null) {
            this.turn = history.getLastTurn();
        } else {
            this.turn = new TurnClassificator();
        }
        this.players = players;
        this.map = map;
        this.history = history;
        this.executor = executor;
    }

    public GameMaster(List<Player> players, GameMap map, TurnClassificator turn, Executor executor) {
        this.turn = turn;
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

    public void initializeStartPositions() {
        for (Player p : players) {
            p.getUnits().clear();
        }
        map.getProvinces().forEach(province -> 
            province.setOccupyingUnit(null)
        );

        for (Player player : players) {
            Country country = player.getCountry();
            if (country == null) {
                throw new IllegalArgumentException(
                    "Country is not defined"
                );
            }

            Map<Province, String> startingUnits = country.getStartingUnitTypes();
            if (startingUnits == null || startingUnits.isEmpty()) {
                throw new IllegalArgumentException(
                    "Country " + country.getName() + " doesn't have starting units defined"
                );
            }

            for (var entry : startingUnits.entrySet()) {
                Province province = entry.getKey();
                String unitType = entry.getValue();

                if (province == null || province.getLocations().isEmpty()) {
                    throw new IllegalArgumentException(
                        "Province is defined with some error"
                    );
                }

                Location location = selectStartingLocation(province, unitType);

                Unit unit;
                if ("army".equalsIgnoreCase(unitType)) {
                    unit = new Army(player, location);
                } else if ("fleet".equalsIgnoreCase(unitType)) {
                    unit = new Fleet(player, location);
                } else {
                    throw new IllegalArgumentException(
                        "Unsupported unit type: " + unitType
                    );
                }

                province.setOccupyingUnit(unit);
                player.getUnits().add(unit);
            }
        }
    } // ++

    private Location selectStartingLocation(Province province, String unitType) {
        if ("army".equalsIgnoreCase(unitType)) {
            for (Location loc : province.getLocations()) {
                if ("land".equals(loc.getName())) return loc;
            }
            throw new IllegalArgumentException(
                    "Location is not found"
                );
        } else {
            for (Location loc : province.getLocations()) {
                // if (loc.getName() != null && loc.getName().startsWith("coast")) return loc;
                if (loc.getName() != null && !loc.getName().startsWith("land")) return loc;
            }
            throw new IllegalArgumentException(
                    "Location is not found"
                );
        }
    } // ++

    public void addOrder(Order order, Player player) {
        pendingOrdersByPlayer.computeIfAbsent(player, k -> new ArrayList<>()).add(order);
    }

    public List<Order> getPendingOrders(Player player) {
        return pendingOrdersByPlayer.getOrDefault(player, new ArrayList<>());
    }

    public void removeOrder(Player player, int index) {
        List<Order> orders = pendingOrdersByPlayer.get(player);
        if (orders != null && index >= 0 && index < orders.size()) {
            orders.remove(index);
        }
    }

    public List<Order> getAllPendingOrders() {
        List<Order> all = new ArrayList<>();
        for (List<Order> orders : pendingOrdersByPlayer.values()) {
            all.addAll(orders);
        }
        return all;
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
