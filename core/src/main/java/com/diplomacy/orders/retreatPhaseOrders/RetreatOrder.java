package com.diplomacy.orders.retreatPhaseOrders;

import com.diplomacy.geography.basic.Location;
import com.diplomacy.orders.RetreatPhaseOrder;
import com.diplomacy.units.Unit;

public class RetreatOrder extends RetreatPhaseOrder {

    private final Location destination;

    public RetreatOrder(Location destination, Unit unitToRetreat) {
        super(unitToRetreat);
        this.destination = destination;
    }

    public Location getDestination() {
        return destination;
    }
}
