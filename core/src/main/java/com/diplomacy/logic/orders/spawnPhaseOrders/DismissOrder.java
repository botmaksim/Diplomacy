package com.diplomacy.logic.orders.spawnPhaseOrders;

import com.diplomacy.logic.orders.SpawnPhaseOrder;
import com.diplomacy.logic.units.Unit;

public class DismissOrder extends SpawnPhaseOrder {

    private final Unit unitToDismiss;

    public DismissOrder(Unit unitToDismiss) {
        this.unitToDismiss = unitToDismiss;
    }

    public Unit getUnitToDismiss() {
        return unitToDismiss;
    }
}
