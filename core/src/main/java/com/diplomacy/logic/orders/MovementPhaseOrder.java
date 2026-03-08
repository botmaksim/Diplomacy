package com.diplomacy.logic.orders;

import com.diplomacy.logic.units.Unit;

public abstract class MovementPhaseOrder extends Order {

    private final Unit executor;

    public MovementPhaseOrder(Unit executor) {
        this.executor = executor;
    }

    public Unit getExecutor() {
        return executor;
    }
}
