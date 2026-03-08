package com.diplomacy.logic.geography.basic;

import java.util.ArrayList;
import java.util.List;

import com.diplomacy.logic.geography.utils.ProvinceType;

public class Location {

    private final List<Location> neighbours;
    private final Province parentProvince;

    public Location(Province parentProvince) {
        this.parentProvince = parentProvince;
        this.neighbours = new ArrayList<>();
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

    public boolean isArmyLocation() {
        if (neighbours.isEmpty()) {
            return true;
        }
        boolean flag = true;
        for (Location neighbour : neighbours) {
            flag = flag && neighbour.getParentProvince().getType() != ProvinceType.WATER;
        }
        return flag;
    }
}
