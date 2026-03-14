package com.diplomacy.logic.orders.retreatPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.RetreatPhaseOrder;

public class RetreatOrder extends RetreatPhaseOrder {

    private final Location destination;

    public RetreatOrder(Location destination, Location unitToRetreat) {
        super(unitToRetreat);
        this.destination = destination;
    }

    public Location getDestination() {
        return destination;
    }
}
