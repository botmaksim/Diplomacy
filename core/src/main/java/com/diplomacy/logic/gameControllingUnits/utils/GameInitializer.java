package com.diplomacy.logic.gameControllingUnits.utils;

import java.util.Map;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.geography.advanced.Country;
import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.units.Army;
import com.diplomacy.logic.units.Fleet;
import com.diplomacy.logic.units.Unit;

public final class GameInitializer {

    private GameInitializer() {}

    public static void initializeStartPositions(GameMaster gameMaster) {
        for (var p : gameMaster.getPlayers()) {
            p.getUnits().clear();
        }
        gameMaster.getMap().getProvinces().forEach(province ->
            province.setOccupyingUnit(null));

        for (var player : gameMaster.getPlayers()) {
            Country country = player.getCountry();
            if (country == null) {
                throw new IllegalArgumentException("Country is not defined");
            }

            Map<Province, String> startingUnits = country.getStartingUnitTypes();
            if (startingUnits == null || startingUnits.isEmpty()) {
                throw new IllegalArgumentException(
                    "Country " + country.getName() + " doesn't have starting units defined");
            }

            for (var entry : startingUnits.entrySet()) {
                Province province = entry.getKey();
                String unitType = entry.getValue();

                if (province == null || province.getLocations().isEmpty()) {
                    throw new IllegalArgumentException("Province is defined with some error");
                }

                Location location = selectStartingLocation(province, unitType);

                Unit unit;
                if ("army".equalsIgnoreCase(unitType)) {
                    unit = new Army(player, location);
                } else if ("fleet".equalsIgnoreCase(unitType)) {
                    unit = new Fleet(player, location);
                } else {
                    throw new IllegalArgumentException("Unsupported unit type: " + unitType);
                }

                province.setOccupyingUnit(unit);
                player.getUnits().add(unit);
            }
        }
    }

    private static Location selectStartingLocation(Province province, String unitType) {
        if ("army".equalsIgnoreCase(unitType)) {
            for (Location loc : province.getLocations()) {
                if ("land".equals(loc.getName())) return loc;
            }
            throw new IllegalArgumentException("Location is not found");
        } else {
            for (Location loc : province.getLocations()) {
                if (loc.getName() != null && !loc.getName().startsWith("land")) return loc;
            }
            throw new IllegalArgumentException("Location is not found");
        }
    }
}