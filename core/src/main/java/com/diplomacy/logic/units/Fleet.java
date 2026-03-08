package com.diplomacy.logic.units;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.units.utils.ConvoyHelper;

public class Fleet extends Unit {

    public Fleet(Player owner, Location location) {
        super(owner, location);
    }

    public boolean canConvoy(Province destination, Army armyToConvoy) {
        return new ConvoyHelper().CanConvoy(getLocation(), destination, armyToConvoy);
    }
}
