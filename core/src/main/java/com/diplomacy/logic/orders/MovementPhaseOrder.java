package com.diplomacy.orders;

import com.diplomacy.units.Unit;

public abstract class MovementPhaseOrder extends Order {

    private final Unit executor;

    public MovementPhaseOrder(Unit executor) {
        this.executor = executor;
    }

    public Unit getExecutor() {
        return executor;
    }
}
