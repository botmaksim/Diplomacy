package com.diplomacy.logic.gameControllingUnits.phase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.gameControllingUnits.utils.Resolver;
import com.diplomacy.logic.gameControllingUnits.utils.SupplyCentersReallocator;
import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.movementPhaseOrders.ConvoyOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.SupportOrder;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.save.gameHistory.HistoryPhase;

public class MovementPhase implements Phase {

    public GameMaster gameMaster;
    private final Map<Player, List<MovementPhaseOrder>> ordersByPlayer = new HashMap<>();

    @Override
    public void setGameMaster(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    @Override
    public boolean addOrder(Order order, Player player) {
        if (!(order instanceof MovementPhaseOrder moveOrder)) {
            return false;
        }

        for (MovementPhaseOrder o : getAllOrdersList()) {
            if (o.getTarget() == moveOrder.getTarget()) {
                return false;
            }
        }

        ordersByPlayer.computeIfAbsent(player, p -> new ArrayList<>()).add(moveOrder);
        return true;
    }

    @Override
    public List<MovementPhaseOrder> getOrders(Player player) {
        return ordersByPlayer.getOrDefault(player, new ArrayList<>());
    }

    @Override
    public List<MovementPhaseOrder> getAllOrders() {
        return getAllOrdersList();
    }

    private List<MovementPhaseOrder> getAllOrdersList() {
        List<MovementPhaseOrder> result = new ArrayList<>();
        for (List<MovementPhaseOrder> list : ordersByPlayer.values()) {
            result.addAll(list);
        }
        return result;
    }

    @Override
    public boolean removeOrder(Player player, int index) {
        List<MovementPhaseOrder> playerOrders = ordersByPlayer.get(player);
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
    public void operate() {
        List<MovementPhaseOrder> allOrders = getAllOrdersList();
        Resolver r = new Resolver();
        r.resolveMovements(allOrders, gameMaster);



        gameMaster.getHistory().addHistoryPhase(new HistoryPhase(gameMaster.getTurn(), allOrders));
        gameMaster.getExecutor().beginExecuteMovements(allOrders, gameMaster);
    }

    @Override
    public boolean ableNextPhase() {
        List<MovementPhaseOrder> allOrders = getAllOrdersList();
        Map<Location, MovementPhaseOrder> targetOrder = new HashMap<>();
        for (MovementPhaseOrder o : allOrders) {
            targetOrder.put(o.getTarget(), o);
        }

        List<SupportOrder> supportOrders = allOrders.stream()
                .filter(SupportOrder.class::isInstance)
                .map(SupportOrder.class::cast)
                .toList();
        for (SupportOrder s : supportOrders) {
            if (!targetOrder.containsKey(s.getSupportedUnit())) {
                return false;
            }
            MovementPhaseOrder o = targetOrder.get(s.getSupportedUnit());
            if (o instanceof com.diplomacy.logic.orders.movementPhaseOrders.HoldOrder || o instanceof ConvoyOrder || o instanceof SupportOrder) {
                if (!s.getSupportedUnit().equals(s.getDestination())) {
                    return false;
                }
            }
        }
        return true;
    }
}
