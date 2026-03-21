package com.diplomacy.logic.units;

import java.util.List;
import java.util.function.Function;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.units.utils.ConvoyHelper;
import com.diplomacy.logic.utils.Pair;

public class Army extends Unit {

    public Army(Player owner, Location location) {
        super(owner, location);
    }

    public boolean canConvoyTo(Location target) {
        return this.getListOfReachableByConvoyLocations().contains(target);
    }

    public boolean canConvoyTo(Location target, Function<Pair<Location, Unit>, Boolean> isAbleToConvoy) {
        return this.getListOfReachableByConvoyLocations(isAbleToConvoy).contains(target);
    }

    public List<Location> getListOfReachableByConvoyLocations() {
        return new ConvoyHelper().getListOfReachableByConvoyLocations(getLocation(), pair -> pair.first().getParentProvince().isOccupied());
    }

    public List<Location> getListOfReachableByConvoyLocations(Function<Pair<Location, Unit>, Boolean> isAbleToConvoy) {
        return new ConvoyHelper(this).getListOfReachableByConvoyLocations(getLocation(), isAbleToConvoy);
    }
}
