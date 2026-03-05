package com.diplomacy.geography.basic;

import java.util.ArrayList;
import java.util.List;

import com.diplomacy.units.Unit;

public class Province {

    private final boolean isSupplyCenter;
    private final String name;
    private final ProvinceType type;
    private final List<Location> locations;

    private boolean isBattled = false;
    private Unit occupyingUnit = null;

    public Province(boolean isSupplyCenter, String name, ProvinceType type, List<Location> locations) {
        this(isSupplyCenter, name, type, locations, false, null);
    }

    public Province(boolean isSupplyCenter, String name, ProvinceType type, List<Location> locations, boolean isBattled) {
        this(isSupplyCenter, name, type, locations, isBattled, null);
    }

    public Province(boolean isSupplyCenter, String name, ProvinceType type, List<Location> locations, Unit occupyingUnit) {
        this(isSupplyCenter, name, type, locations, false, occupyingUnit);
    }

    public Province(boolean isSupplyCenter, String name, ProvinceType type, List<Location> locations, boolean isBattled, Unit occupyingUnit) {
        this.isSupplyCenter = isSupplyCenter;
        this.name = name;
        this.type = type;
        this.locations = (locations != null) ? locations : new ArrayList<>();
        this.isBattled = isBattled;
        this.occupyingUnit = occupyingUnit;
    }

    public String getName() {
        return name;
    }

    public ProvinceType getType() {
        return type;
    }

    public boolean isSupplyCenter() {
        return isSupplyCenter;
    }

    public List<Location> getLocations() {
        return List.copyOf(locations);
    }

    public Unit getOccupyingUnit() {
        return occupyingUnit;
    }

    public void setOccupyingUnit(Unit occupyingUnit) {
        this.occupyingUnit = occupyingUnit;
    }

    public void release() {
        this.occupyingUnit = null;
    }

    public boolean isOccupied() {
        return occupyingUnit != null;
    }

    public boolean isBattled() {
        return isBattled;
    }

    public void setBattled(boolean battled) {
        isBattled = battled;
    }
}
