package com.diplomacy.orders.movementPhaseOrders;

import com.diplomacy.geography.basic.Location;
import com.diplomacy.orders.MovementPhaseOrder;
import com.diplomacy.units.Unit;

public class ConvoyOrder extends MovementPhaseOrder {

    private final Unit convoyedArmy;
    private final Location destination;

    public ConvoyOrder(Unit convoyedArmy, Location destination, Unit executor) {
        super(executor);
        this.convoyedArmy = convoyedArmy;
        this.destination = destination;
    }

    public Unit getConvoyedArmy() {
        return convoyedArmy;
    }

    public Location getDestination() {
        return destination;
    }
}
