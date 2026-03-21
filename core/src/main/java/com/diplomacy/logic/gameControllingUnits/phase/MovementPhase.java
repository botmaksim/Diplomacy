package com.diplomacy.logic.gameControllingUnits.phase;

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
import com.diplomacy.logic.save.gameHistory.HistoryPhase;

public class MovementPhase implements Phase {

    public GameMaster gameMaster;
    public List<MovementPhaseOrder> orders;

    @Override
    public void setGameMaster(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    @Override
    public boolean addOrder(Order order) {
        if (order instanceof MovementPhaseOrder moveOrder) {
            for (MovementPhaseOrder o : orders) {
                if (o.getTarget() == moveOrder.getTarget()) {
                    return false;
                }
            }
            orders.add(moveOrder);
            return true;
        }

        return false;
    }

    @Override
    public List<MovementPhaseOrder> getOrders() {
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
    public void operate() {
        Resolver r = new Resolver();
        r.resolveMovements(orders, gameMaster);

        SupplyCentersReallocator reallocator = new SupplyCentersReallocator();
        reallocator.reallocateSupplyCentersMovement(orders);

        gameMaster.getHistory().addHistoryPhase(new HistoryPhase(gameMaster.getTurn(), orders));
        gameMaster.getExecutor().beginExecuteMovements(orders, gameMaster);

    }

    @Override
    public boolean ableNextPhase() {
        Map<Location, MovementPhaseOrder> targetOrder = new HashMap<>();

        for (MovementPhaseOrder o : orders) {
            targetOrder.put(o.getTarget(), o);
        }

        List<SupportOrder> supportOrders = orders.stream()
                .filter(SupportOrder.class::isInstance)
                .map(SupportOrder.class::cast)
                .toList();
        for (SupportOrder s : supportOrders) {
            if (!targetOrder.containsKey(s.getSupportedUnit())) {
                return false;
            }
            MovementPhaseOrder o = targetOrder.get(s.getSupportedUnit());
            if (o instanceof ConvoyOrder || o instanceof SupportOrder) {
                return false;
            }
        }

        return true;
    }

}
