package com.diplomacy.logic.orders.movementPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.MovementPhaseOrder;

public class ConvoyOrder extends MovementPhaseOrder {

    private final Location convoyedArmy;
    private final Location destination;

    public ConvoyOrder(Location convoyedArmy, Location destination, Location target) {
        super(target);
        this.convoyedArmy = convoyedArmy;
        this.destination = destination;
    }

    public Location getConvoyedArmy() {
        return convoyedArmy;
    }

    public Location getDestination() {
        return destination;
    }
}
