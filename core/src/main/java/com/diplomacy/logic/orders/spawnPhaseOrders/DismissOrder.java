package com.diplomacy.logic.orders.spawnPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.SpawnPhaseOrder;

public class DismissOrder extends SpawnPhaseOrder {

    private final Location unitToDismiss;

    public DismissOrder(Location unitToDismiss) {
        this.unitToDismiss = unitToDismiss;
    }

    public Location getUnitToDismiss() {
        return unitToDismiss;
    }
}
