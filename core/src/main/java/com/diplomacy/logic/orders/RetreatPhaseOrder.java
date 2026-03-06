package com.diplomacy.orders;

import com.diplomacy.units.Unit;

public class RetreatPhaseOrder extends Order {

    private final Unit unitToRetreat;

    public RetreatPhaseOrder(Unit unitToRetreat) {
        this.unitToRetreat = unitToRetreat;
    }

    public Unit getUnitToRetreat() {
        return unitToRetreat;
    }
}
