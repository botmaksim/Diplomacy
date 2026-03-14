package com.diplomacy.logic.gameControllingUnits.utils;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.units.Unit;

public class UnfinishedMoveOrder {

    public final Unit unit;
    public final Location destination;

    public UnfinishedMoveOrder(Unit unit, Location destination) {
        this.unit = unit;
        this.destination = destination;
    }

    public Unit getUnit() {
        return unit;
    }

    public Location getDestination() {
        return destination;
    }
}
