package com.diplomacy.logic.units;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.player.Player;

public abstract class Unit {

    private boolean retreating = false;
    private final Player owner;
    private Location location;

    public Unit(Player owner, Location location) {
        this.owner = owner;
        this.location = location;
    }

    public Player getOwner() {
        return owner;
    }

    public void setRetreating(boolean retreating) {
        this.retreating = retreating;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public boolean CanMoveTo(Location target) {
        return location.getNeighbours().contains(target);
    }

    public boolean isRetreating() {
        return retreating;
    }
}
