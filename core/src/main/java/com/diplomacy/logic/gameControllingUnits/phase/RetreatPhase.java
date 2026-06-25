package com.diplomacy.logic.gameControllingUnits.phase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.gameControllingUnits.utils.Resolver;
import com.diplomacy.logic.gameControllingUnits.utils.SupplyCentersReallocator;
import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.RetreatPhaseOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.BeConvoyedOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.MoveOrder;
import com.diplomacy.logic.orders.retreatPhaseOrders.RetreatOrder;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.save.gameHistory.History;
import com.diplomacy.logic.save.gameHistory.HistoryPhase;
import com.diplomacy.logic.turnClassificator.PhaseType;
import com.diplomacy.logic.turnClassificator.TurnClassificator;

public class RetreatPhase implements Phase {

    public GameMaster gameMaster;
    private final Map<Player, List<RetreatPhaseOrder>> ordersByPlayer = new HashMap<>();

    @Override
    public void setGameMaster(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    @Override
    public boolean addOrder(Order order, Player player) {
        History history = gameMaster.getHistory();
        TurnClassificator turn = gameMaster.getTurn();
        List<MovementPhaseOrder> movementOrders = history.getHistoryPhase(turn.getYear(), turn.getSeason(), PhaseType.MOVEMENT).getOrders()
            .stream().filter(MovementPhaseOrder.class::isInstance).map(MovementPhaseOrder.class::cast).toList();

        if (order instanceof RetreatOrder r) {
            for (RetreatPhaseOrder o : getAllOrdersList()) {
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
            ordersByPlayer.computeIfAbsent(player, k -> new ArrayList<>()).add(r);
            return true;
        }
        return false;
    }

    @Override
    public List<RetreatPhaseOrder> getOrders(Player player) {
        return ordersByPlayer.getOrDefault(player, new ArrayList<>());
    }

    @Override
    public List<RetreatPhaseOrder> getAllOrders() {
        return getAllOrdersList();
    }

    private List<RetreatPhaseOrder> getAllOrdersList() {
        return ordersByPlayer.values().stream().flatMap(List::stream).toList();
    }

    @Override
    public boolean removeOrder(Player player, int index) {
        List<RetreatPhaseOrder> playerOrders = ordersByPlayer.get(player);
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
        List<RetreatPhaseOrder> allOrders = getAllOrdersList();
        Resolver r = new Resolver();
        r.resolveRetreats(allOrders, gameMaster);

        gameMaster.getHistory().addHistoryPhase(new HistoryPhase(gameMaster.getTurn(), allOrders));

        SupplyCentersReallocator reallocator = new SupplyCentersReallocator();
        reallocator.reallocateSupplyCenters(gameMaster);

        gameMaster.getExecutor().executeRetreats(allOrders, gameMaster);

        gameMaster.getMap().resetBattleFlags();
    }

    @Override
    public boolean ableNextPhase() {
        return true;
    }
}
