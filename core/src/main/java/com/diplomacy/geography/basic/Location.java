package com.diplomacy.geography.basic;

import java.util.ArrayList;
import java.util.List;

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
        return neighbours.get(0).getParentProvince().getType() != ProvinceType.WATER;
    }
}
