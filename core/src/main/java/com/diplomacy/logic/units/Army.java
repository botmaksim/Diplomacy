package com.diplomacy.logic.units;

import java.util.List;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.units.utils.ConvoyHelper;

public class Army extends Unit {

    public Army(Player owner, Location location) {
        super(owner, location);
    }

    public boolean canConvoyToFiltred(Location target) {
        return this.getListOfReachableByConvoyLocationsFiltred().contains(target);
    }

    public List<Location> getListOfReachableByConvoyLocationsFiltred() {
        List<Location> candidates = new ConvoyHelper().getListOfReachableByConvoyLocations(getLocation());
        for (Location current : candidates) {
            if (current.getParentProvince().isOccupied()) {
                candidates.remove(current);
            }
        }
        return candidates;
    }

    public boolean canConvoyTo(Location target) {
        return this.getListOfReachableByConvoyLocations().contains(target);
    }

    public List<Location> getListOfReachableByConvoyLocations() {
        return new ConvoyHelper().getListOfReachableByConvoyLocations(getLocation());
    }

}
