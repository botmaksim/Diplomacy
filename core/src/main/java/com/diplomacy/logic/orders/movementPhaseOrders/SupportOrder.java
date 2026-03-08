package com.diplomacy.logic.orders.movementPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.units.Unit;

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
