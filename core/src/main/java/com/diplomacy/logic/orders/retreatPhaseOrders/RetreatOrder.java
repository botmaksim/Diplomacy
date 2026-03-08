package com.diplomacy.logic.orders.retreatPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.RetreatPhaseOrder;
import com.diplomacy.logic.units.Unit;

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
