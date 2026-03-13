package com.diplomacy.logic.save.gameHistory;

import java.util.List;

import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.turnClassificator.TurnClassificator;

public class HistoryPhase {

    private final TurnClassificator turn;
    private final List<Order> orders;

    public HistoryPhase(TurnClassificator turn, List<Order> orders) {
        this.turn = turn;
        this.orders = orders;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public TurnClassificator getTurn() {
        return turn;
    }

}
