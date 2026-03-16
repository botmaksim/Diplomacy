package com.diplomacy.logic.gameControllingUnits.phase;

import java.util.List;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.gameControllingUnits.utils.Resolver;
import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.orders.Order;
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
                if (o.getExecutor() == moveOrder.getExecutor()) {
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

        gameMaster.getHistory().addHistoryPhase(new HistoryPhase(gameMaster.getTurn(), orders));
        gameMaster.getExecutor().beginExecuteMovements(orders, gameMaster);
    }

    @Override
    public boolean ableNextPhase() {
        return true;
    }

}
