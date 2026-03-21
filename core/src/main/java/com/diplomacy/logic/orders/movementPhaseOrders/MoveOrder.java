package com.diplomacy.logic.orders.movementPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.MovementPhaseOrder;

public class MoveOrder extends MovementPhaseOrder {

    private final Location destination;

    public MoveOrder(Location destination, Location target) {
        super(target);
        this.destination = destination;
    }

    public Location getDestination() {
        return destination;
    }

}
