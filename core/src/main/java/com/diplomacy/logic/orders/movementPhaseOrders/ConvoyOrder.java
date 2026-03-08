package com.diplomacy.logic.orders.movementPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.units.Unit;

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
