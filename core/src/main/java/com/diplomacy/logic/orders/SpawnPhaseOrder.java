package com.diplomacy.logic.orders;

import com.diplomacy.logic.geography.basic.Location;

public abstract class SpawnPhaseOrder extends Order {

    public SpawnPhaseOrder(Location target) {
        super(target);
    }
}
