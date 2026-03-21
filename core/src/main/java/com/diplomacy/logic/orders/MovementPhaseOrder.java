package com.diplomacy.logic.orders;

import com.diplomacy.logic.geography.basic.Location;

public abstract class MovementPhaseOrder extends Order {

    public MovementPhaseOrder(Location target) {
        super(target);
    }

}
