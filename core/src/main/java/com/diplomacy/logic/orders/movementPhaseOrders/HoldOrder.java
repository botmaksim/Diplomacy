package com.diplomacy.logic.orders.movementPhaseOrders;

import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.units.Unit;

public class HoldOrder extends MovementPhaseOrder {

    public HoldOrder(Unit executor) {
        super(executor);
    }
}
