package com.diplomacy.logic.orders;

import com.diplomacy.logic.units.Unit;

public abstract class RetreatPhaseOrder extends Order {

    private final Unit unitToRetreat;

    public RetreatPhaseOrder(Unit unitToRetreat) {
        this.unitToRetreat = unitToRetreat;
    }

    public Unit getUnitToRetreat() {
        return unitToRetreat;
    }
}
