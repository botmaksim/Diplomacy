package com.diplomacy.orders.movementPhaseOrders;

import com.diplomacy.geography.basic.Location;
import com.diplomacy.orders.MovementPhaseOrder;
import com.diplomacy.units.Unit;

public class SupportOrder extends MovementPhaseOrder {

    private final Unit supportedUnit;
    private final Location destination;

    public SupportOrder(Unit supportedUnit, Location destination, Unit executor) {
        super(executor);
        this.supportedUnit = supportedUnit;
        this.destination = destination;
    }

    public Location getDestination() {
        return destination;
    }

    public Unit getSupportedUnit() {
        return supportedUnit;
    }
}
