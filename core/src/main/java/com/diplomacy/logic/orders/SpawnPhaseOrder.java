package com.diplomacy.logic.orders;

import com.diplomacy.logic.geography.basic.Location;

public abstract class SpawnPhaseOrder extends Order {

    private final Location destination;

    public SpawnPhaseOrder(Location destination) {
        this.destination = destination;
    }

    public Location getDestination() {
        return destination;
    }
}
