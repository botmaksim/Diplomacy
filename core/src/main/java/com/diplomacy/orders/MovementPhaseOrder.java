package com.diplomacy.orders.movementPhaseOrders;

import com.diplomacy.units.Unit;

public abstract class MovementPhaseOrder {

    private final Unit executor;

    public MovementPhaseOrder(Unit executor) {
        this.executor = executor;
    }

    public Unit getExecutor() {
        return executor;
    }
}
