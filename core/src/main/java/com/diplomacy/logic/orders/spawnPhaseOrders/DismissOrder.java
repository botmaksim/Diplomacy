package com.diplomacy.orders.spawnPhaseOrders;

import com.diplomacy.orders.SpawnPhaseOrder;
import com.diplomacy.units.Unit;

public class DismissOrder extends SpawnPhaseOrder {

    private final Unit unitToDismiss;

    public DismissOrder(Unit unitToDismiss) {
        this.unitToDismiss = unitToDismiss;
    }

    public Unit getUnitToDismiss() {
        return unitToDismiss;
    }
}
