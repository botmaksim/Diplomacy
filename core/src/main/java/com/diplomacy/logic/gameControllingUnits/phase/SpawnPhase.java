package com.diplomacy.logic.gameControllingUnits.phase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.gameControllingUnits.utils.Resolver;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.SpawnPhaseOrder;
import com.diplomacy.logic.orders.spawnPhaseOrders.DismissOrder;
import com.diplomacy.logic.orders.spawnPhaseOrders.SpawnOrder;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.save.gameHistory.HistoryPhase;

public class SpawnPhase implements Phase {

    public GameMaster gameMaster;
    private final Map<Player, List<SpawnPhaseOrder>> ordersByPlayer = new HashMap<>();

    public SpawnPhase() {}

    @Override
    public void setGameMaster(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    @Override
    public boolean addOrder(Order order, Player player) {
        if (order instanceof SpawnOrder spawnOrder) {
            for (SpawnOrder o : getAllOrdersList().stream().filter(SpawnOrder.class::isInstance).map(SpawnOrder.class::cast).toList()) {
                if (o.getTarget() == spawnOrder.getTarget()) {
                    return false;
                }
            }
            ordersByPlayer.computeIfAbsent(player, k -> new ArrayList<>()).add(spawnOrder);
            return true;
        }

        if (order instanceof DismissOrder dismissOrder) {
            for (DismissOrder o : getAllOrdersList().stream().filter(DismissOrder.class::isInstance).map(DismissOrder.class::cast).toList()) {
                if (o.getTarget() == dismissOrder.getTarget()) {
                    return false;
                }
            }
            ordersByPlayer.computeIfAbsent(player, k -> new ArrayList<>()).add(dismissOrder);
            return true;
        }

        return false;
    }

    @Override
    public List<SpawnPhaseOrder> getOrders(Player player) {
        return ordersByPlayer.getOrDefault(player, new ArrayList<>());
    }

    @Override
    public List<SpawnPhaseOrder> getAllOrders() {
        return getAllOrdersList();
    }

    private List<SpawnPhaseOrder> getAllOrdersList() {
        return ordersByPlayer.values().stream().flatMap(List::stream).toList();
    }

    @Override
    public boolean removeOrder(Player player, int index) {
        List<SpawnPhaseOrder> playerOrders = ordersByPlayer.get(player);
        if (playerOrders == null || index < 0 || index >= playerOrders.size()) {
            return false;
        }
        playerOrders.remove(index);
        return true;
    }

    @Override
    public void clearAllOrders() {
        ordersByPlayer.clear();
    }

    @Override
    public boolean ableNextPhase() {
        List<SpawnPhaseOrder> allOrders = getAllOrdersList();
        Map<Player, Integer> unitChange = new HashMap<>();
        for (Player p : gameMaster.getPlayers()) {
            unitChange.put(p, 0);
        }

        for (SpawnPhaseOrder o : allOrders) {
            if (o instanceof DismissOrder d) {
                unitChange.merge(d.getTarget().getParentProvince().getOccupyingUnit().getOwner(), -1, Integer::sum);
            }
            if (o instanceof SpawnOrder s) {
                unitChange.merge(s.getPlayer(), 1, Integer::sum);
            }
        }

        for (Player p : gameMaster.getPlayers()) {
            if (p.getSupplyCenters().size() - p.getUnits().size() - unitChange.get(p) < 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void operate() {
        List<SpawnPhaseOrder> allOrders = getAllOrdersList();
        Resolver r = new Resolver();
        r.resolveSpawns(allOrders, gameMaster);

        gameMaster.getHistory().addHistoryPhase(new HistoryPhase(gameMaster.getTurn(), allOrders));
        gameMaster.getExecutor().executeSpawns(allOrders, gameMaster);
    }
}
