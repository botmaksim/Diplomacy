package com.diplomacy.logic.gameControllingUnits.phase;

import java.util.List;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.gameControllingUnits.utils.Resolver;
import com.diplomacy.logic.gameControllingUnits.utils.SupplyCentersReallocator;
import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.RetreatPhaseOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.BeConvoyedOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.MoveOrder;
import com.diplomacy.logic.orders.retreatPhaseOrders.RetreatOrder;
import com.diplomacy.logic.save.gameHistory.History;
import com.diplomacy.logic.save.gameHistory.HistoryPhase;
import com.diplomacy.logic.turnClassificator.PhaseType;
import com.diplomacy.logic.turnClassificator.TurnClassificator;

public class RetreatPhase implements Phase {

    public GameMaster gameMaster;
    public List<RetreatPhaseOrder> orders;

    @Override
    public void setGameMaster(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    @Override
    public boolean addOrder(Order order) {
        History history = gameMaster.getHistory();
        TurnClassificator turn = gameMaster.getTurn();
        turn.setPhase(PhaseType.MOVEMENT);
        List<MovementPhaseOrder> movementOrders = history.getHistoryPhase(turn).getOrders().stream().filter(MovementPhaseOrder.class::isInstance).map(MovementPhaseOrder.class::cast).toList();
        turn.setPhase(PhaseType.RETREAT);

        if (order instanceof RetreatOrder r) {
            for (RetreatPhaseOrder o : orders) {
                if (o.getTarget() == r.getTarget()) {
                    return false;
                }
            }
            for (MovementPhaseOrder o : movementOrders) {
                if (o.isExecutable() && (o instanceof MoveOrder m)) {
                    if (m.getDestination() == r.getTarget() && m.getTarget() == r.getDestination()) {
                        return false;
                    }
                }
                if (o.isExecutable() && (o instanceof BeConvoyedOrder bc)) {
                    if (bc.getDestination() == r.getTarget() && bc.getTarget() == r.getDestination()) {
                        return false;
                    }
                }
            }
            orders.add(r);
            return true;
        }
        return false;
    }

    @Override
    public List<RetreatPhaseOrder> getOrders() {
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
        r.resolveRetreats(orders, gameMaster);

        gameMaster.getHistory().addHistoryPhase(new HistoryPhase(gameMaster.getTurn(), orders));

        SupplyCentersReallocator reallocator = new SupplyCentersReallocator();
        reallocator.reallocateSupplyCentersRetreat(orders);

        gameMaster.getExecutor().executeRetreats(orders, gameMaster);
        gameMaster.getExecutor().executeRetreats(orders, gameMaster);

        gameMaster.getMap().resetBattleFlags();
    }

    @Override
    public boolean ableNextPhase() {
        return true;
    }

}
