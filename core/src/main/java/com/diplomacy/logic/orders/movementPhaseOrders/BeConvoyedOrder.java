package com.diplomacy.logic.orders.movementPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.MovementPhaseOrder;

public class BeConvoyedOrder extends MovementPhaseOrder {

    private final Location destination;

    public BeConvoyedOrder(Location destination, Location target) {
        super(target);
        this.destination = destination;
    }

    public Location getDestination() {
        return destination;
    }
}
