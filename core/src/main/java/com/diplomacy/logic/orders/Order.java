package com.diplomacy.logic.orders;

import com.diplomacy.logic.geography.basic.Location;

public abstract class Order {

    private final Location target;
    private boolean executable = true;

    public Order(Location target) {
        this.target = target;
    }

    public Location getTarget() {
        return target;
    }

    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    public boolean equals(Order other) {
        return target == other.getTarget();
    }
}
