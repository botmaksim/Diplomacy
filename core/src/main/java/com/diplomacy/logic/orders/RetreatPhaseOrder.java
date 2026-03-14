package com.diplomacy.logic.orders;

import com.diplomacy.logic.geography.basic.Location;

public abstract class RetreatPhaseOrder extends Order {

    private final Location unitToRetreat;

    public RetreatPhaseOrder(Location unitToRetreat) {
        this.unitToRetreat = unitToRetreat;
    }

    public Location getUnitToRetreat() {
        return unitToRetreat;
    }
}
