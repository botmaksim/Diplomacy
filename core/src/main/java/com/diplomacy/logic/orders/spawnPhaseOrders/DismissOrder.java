package com.diplomacy.logic.orders.spawnPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.SpawnPhaseOrder;

public class DismissOrder extends SpawnPhaseOrder {

    public DismissOrder(Location destination) {
        super(destination);
    }

    @Override
    public boolean equals(Order other) {
        if (other instanceof DismissOrder o) {
            return o.getTarget() == getTarget();
        }
        return false;
    }
}
