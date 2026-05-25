package com.diplomacy.logic.geography.basic;

import java.util.ArrayList;
import java.util.List;

import com.diplomacy.logic.geography.utils.ProvinceType;

public class Location {

    private final List<Location> neighbours;
    private final Province parentProvince;
    private final String name;

    public Location(Province parentProvince, String name) {
        this.parentProvince = parentProvince;
        this.neighbours = new ArrayList<>();
        this.name = name;
    }

    public void addNeighbour(Location neighbour) {
        if (neighbour != null && !neighbours.contains(neighbour)) {
            this.neighbours.add(neighbour);
        }
    }

    public List<Location> getNeighbours() {
        return List.copyOf(neighbours);
    }

    public Province getParentProvince() {
        return parentProvince;
    }

    public String getName() {
        return name;
    }

    public boolean isArmyLocation() {
        if (neighbours.isEmpty()) {
            return true;
        }
        boolean isArmyLocation = true;
        for (Location neighbour : neighbours) {
            isArmyLocation = isArmyLocation && neighbour.getParentProvince().getType() != ProvinceType.WATER;
        }
        return isArmyLocation;
    }
}
