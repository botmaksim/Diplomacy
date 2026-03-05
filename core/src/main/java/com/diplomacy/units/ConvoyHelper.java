package com.diplomacy.units;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.diplomacy.geography.basic.Location;
import com.diplomacy.geography.basic.ProvinceType;

public class ConvoyHelper {

    public List<Location> getListOfReachableByConvoyLocations(Location location) {
        if (location.getParentProvince().getType() != ProvinceType.COASTAL || !location.isArmyLocation()) {
            return new ArrayList<>();
        }

        Queue<Location> queue = new ArrayDeque<>(getFleetLocations(location));
        Set<Location> visited = new HashSet<>(getFleetLocations(location));
        Set<Location> ReachableByConvoy = new HashSet<>();

        while (!queue.isEmpty()) {
            Location current = queue.poll();
            if (current.getParentProvince().getType() == ProvinceType.COASTAL && current != location) {
                ReachableByConvoy.add(getArmyLocation(current));
            }
            for (Location neighbour : current.getNeighbours()) {
                if (!visited.contains(neighbour) && (neighbour.getParentProvince().getType() == ProvinceType.COASTAL || neighbour.getParentProvince().isOccupied())) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }

        return new ArrayList<>(ReachableByConvoy);
    }

    private List<Location> getFleetLocations(Location location) {
        List<Location> allLocations = new ArrayList<>(location.getParentProvince().getLocations());
        allLocations.remove(location);
        return allLocations;
    }

    private Location getArmyLocation(Location location) {
        List<Location> allLocations = new ArrayList<>(location.getParentProvince().getLocations());
        for (Location current : allLocations) {
            if (current.isArmyLocation()) {
                return current;
            }
        }
        return null;
    }
}
