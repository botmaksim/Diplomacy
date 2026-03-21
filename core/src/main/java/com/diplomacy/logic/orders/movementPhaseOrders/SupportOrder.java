package com.diplomacy.logic.orders.movementPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.MovementPhaseOrder;

public class SupportOrder extends MovementPhaseOrder {

    private final Location supportedUnit;
    private final Location destination;

    public SupportOrder(Location supportedUnit, Location destination, Location target) {
        super(target);
        this.supportedUnit = supportedUnit;
        this.destination = destination;
    }

    public Location getDestination() {
        return destination;
    }

    public Location getSupportedUnit() {
        return supportedUnit;
    }
}
