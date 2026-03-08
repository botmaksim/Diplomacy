package com.diplomacy.logic.units.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.geography.utils.ProvinceType;
import com.diplomacy.logic.units.Army;

public class ConvoyHelper {

    public boolean CanConvoy(Location current, Province end, Army target) {
        return !current.isArmyLocation() && target.getLocation().getParentProvince().getType() == ProvinceType.COASTAL && target.getLocation().isArmyLocation() && !end.isOccupied() && doesPathExists(current, getFleetLocations(target.getLocation())) && doesPathExists(current, end.getLocations());
    }

    public List<Location> getListOfReachableByConvoyLocations(Location location) {
        List<Location> accessible = bfsFindAll(getFleetLocations(location));
        accessible.remove(location);
        return accessible;
    }

    private boolean doesPathExists(Location start, List<Location> targets) {
        Deque<Location> queue = new ArrayDeque<>();
        Set<Location> added = new HashSet<>();
        Set<Location> visited = new HashSet<>(getFleetLocations(start));
        queue.add(start);
        added.add(start);
        while (!added.isEmpty()) {
            added = bfs(queue, visited);
            for (Location target : targets) {
                if (added.contains(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Location> bfsFindAll(List<Location> start) {
        List<Location> accessible = new ArrayList<>();
        Deque<Location> queue = new ArrayDeque<>(start);
        Set<Location> added = new HashSet<>(start);
        Set<Location> visited = new HashSet<>(start);
        while (!added.isEmpty()) {
            added = bfs(queue, visited);
            for (Location location : added) {
                if (location.getParentProvince().getType() == ProvinceType.COASTAL) {
                    accessible.add(getArmyLocation(location));
                }
            }
        }
        return accessible;

    }

    private Set<Location> bfs(Queue<Location> queue, Set<Location> visited) {
        Set<Location> added = new HashSet<>();

        while (!queue.isEmpty()) {
            Location current = queue.poll();
            for (Location neighbour : current.getNeighbours()) {
                if (!visited.contains(neighbour) && (neighbour.getParentProvince().getType() == ProvinceType.COASTAL || neighbour.getParentProvince().isOccupied())) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                    added.add(neighbour);
                }
            }
        }
        return added;
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
