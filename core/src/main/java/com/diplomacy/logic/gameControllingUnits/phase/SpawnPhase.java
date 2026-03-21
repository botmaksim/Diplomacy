package com.diplomacy.logic.gameControllingUnits.phase;

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
    public List<SpawnPhaseOrder> orders;

    @Override
    public void setGameMaster(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    public SpawnPhase() {
    }

    @Override
    public boolean addOrder(Order order) {
        if (order instanceof SpawnOrder spawnOrder) {
            for (SpawnOrder o : orders.stream().filter(SpawnOrder.class::isInstance).map(SpawnOrder.class::cast).toList()) {
                if (o.getTarget() == spawnOrder.getTarget()) {
                    return false;
                }
            }
            orders.add(spawnOrder);
            return true;
        }

        if (order instanceof DismissOrder dismissnOrder) {
            for (DismissOrder o : orders.stream().filter(DismissOrder.class::isInstance).map(DismissOrder.class::cast).toList()) {
                if (o.getTarget() == dismissnOrder.getTarget()) {
                    return false;
                }
            }
            orders.add(dismissnOrder);
            return true;
        }

        return false;
    }

    @Override
    public List<SpawnPhaseOrder> getOrders() {
        return orders;
    }

    @Override
    public boolean removeLastOrder() {
        if (orders.isEmpty()) {
            return false;
        }
        orders.removeLast();
        return true;
    }

    @Override
    public boolean removeOrder(int i) {
        if (i < 0 || i >= orders.size()) {
            return false;
        }
        orders.remove(i);
        return true;
    }

    @Override
    public boolean ableNextPhase() {
        Map<Player, Integer> unitChange = new HashMap<>();
        for (Player p : gameMaster.getPlayers()) {
            unitChange.put(p, 0);
        }

        for (SpawnPhaseOrder o : orders) {
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
        Resolver r = new Resolver();
        r.resolveSpawns(orders, gameMaster);

        gameMaster.getHistory().addHistoryPhase(new HistoryPhase(gameMaster.getTurn(), orders));
        gameMaster.getExecutor().executeSpawns(orders, gameMaster);
    }

}
