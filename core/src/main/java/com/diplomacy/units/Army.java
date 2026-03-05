package com.diplomacy.units;

import java.util.List;

import com.diplomacy.geography.basic.Location;
import com.diplomacy.player.Player;

public class Army extends Unit {

    public Army(Player owner, Location location) {
        super(owner, location);
    }

    public boolean canConvoyTo(Location target) {
        return this.getListOfReachableByConvoyLocations().contains(target);
    }

    public List<Location> getListOfReachableByConvoyLocations() {
        return new ConvoyHelper().getListOfReachableByConvoyLocations(getLocation());
    }

}
