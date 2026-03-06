package com.diplomacy.orders.movementPhaseOrders;

import com.diplomacy.geography.basic.Location;
import com.diplomacy.orders.MovementPhaseOrder;
import com.diplomacy.units.Unit;

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
