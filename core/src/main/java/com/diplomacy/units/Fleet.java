package com.diplomacy.units;

import com.diplomacy.geography.basic.Location;
import com.diplomacy.player.Player;
import com.diplomacy.units.utils.ConvoyHelper;

public class Fleet extends Unit {

    public Fleet(Player owner, Location location) {
        super(owner, location);
    }

    public boolean canConvoy(Location destination, Army armyToConvoy) {
        return new ConvoyHelper().CanConvoy(getLocation(), destination, armyToConvoy);
    }
}
