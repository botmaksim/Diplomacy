package com.diplomacy.logic.orders;

import com.diplomacy.logic.geography.basic.Location;

public abstract class MovementPhaseOrder extends Order {

    private final Location executor;

    public MovementPhaseOrder(Location executor) {
        this.executor = executor;
    }

    public Location getExecutor() {
        return executor;
    }
}
