package com.diplomacy.logic.orders;

import com.diplomacy.logic.geography.basic.Location;

public abstract class RetreatPhaseOrder extends Order {

    public RetreatPhaseOrder(Location target) {
        super(target);
    }
}
