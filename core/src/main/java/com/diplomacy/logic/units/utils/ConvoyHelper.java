package com.diplomacy.logic.units.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.geography.utils.ProvinceType;
import com.diplomacy.logic.units.Army;
import com.diplomacy.logic.units.Unit;
import com.diplomacy.logic.utils.Pair;

public class ConvoyHelper {

    private final Unit unit;

    public ConvoyHelper() {
        this.unit = null;
    }

    public ConvoyHelper(Unit unit) {
        this.unit = unit;
    }

    public boolean CanConvoy(Location current, Province end, Army target) {
        return !current.isArmyLocation() && target.getLocation().getParentProvince().getType() == ProvinceType.COASTAL && target.getLocation().isArmyLocation() && !end.isOccupied() && doesPathExists(current, getFleetLocations(target.getLocation())) && doesPathExists(current, end.getLocations());
    }

    public List<Location> getListOfReachableByConvoyLocations(Location location, Function<Pair<Location, Unit>, Boolean> isAbleToConvoy) {
        List<Location> accessible = bfsFindAll(getFleetLocations(location), isAbleToConvoy);
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
            added = bfs(queue, visited, pair -> pair.first().getParentProvince().isOccupied());
            for (Location target : targets) {
                if (added.contains(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Location> bfsFindAll(List<Location> start, Function<Pair<Location, Unit>, Boolean> isAbleToConvoy) {
        List<Location> accessible = new ArrayList<>();
        Deque<Location> queue = new ArrayDeque<>(start);
        Set<Location> added = new HashSet<>(start);
        Set<Location> visited = new HashSet<>(start);
        while (!added.isEmpty()) {
            added = bfs(queue, visited, isAbleToConvoy);
            for (Location location : added) {
                if (location.getParentProvince().getType() == ProvinceType.COASTAL) {
                    accessible.add(getArmyLocation(location));
                }
            }
        }
        return accessible;

    }

    private Set<Location> bfs(Queue<Location> queue, Set<Location> visited, Function<Pair<Location, Unit>, Boolean> isAbleToConvoy) {
        Set<Location> added = new HashSet<>();

        while (!queue.isEmpty()) {
            Location current = queue.poll();
            for (Location neighbour : current.getNeighbours()) {
                if (!visited.contains(neighbour) && (neighbour.getParentProvince().getType() == ProvinceType.COASTAL || isAbleToConvoy.apply(new Pair<>(neighbour, unit)))) {
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
