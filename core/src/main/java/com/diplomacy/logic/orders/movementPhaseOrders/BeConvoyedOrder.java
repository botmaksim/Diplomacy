package com.diplomacy.logic.orders.movementPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.units.Unit;

public class BeConvoyedOrder extends MovementPhaseOrder {

    private final Location destination;

    public BeConvoyedOrder(Location destination, Unit executor) {
        super(executor);
        this.destination = destination;
    }

    public Location getDestination() {
        return destination;
    }
}
