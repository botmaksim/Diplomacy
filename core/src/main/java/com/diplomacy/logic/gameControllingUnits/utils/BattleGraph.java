package com.diplomacy.logic.gameControllingUnits.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.movementPhaseOrders.BeConvoyedOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.MoveOrder;

public class BattleGraph {

    private final List<Location> vertices;
    private final Map<Location, Location> edges;
    private final Map<Location, Integer> power, def;

    public BattleGraph(List<MoveOrder> orders, Map<Location, Integer> powerMap, Map<Location, Integer> defMap) {
        vertices = new ArrayList<>();
        edges = new HashMap<>();
        power = powerMap;
        def = defMap;

        for (MoveOrder o : orders) {
            edges.put(o.getTarget(), o.getDestination());
            vertices.add(o.getTarget());
        }
    }

    public void add(List<BeConvoyedOrder> orders, Map<Location, Integer> powerMap) {
        power.putAll(powerMap);

        for (BeConvoyedOrder o : orders) {
            edges.put(o.getTarget(), o.getDestination());
            vertices.add(o.getTarget());
        }
    }

    public List<Location> getExecutableOrders() {
        List<Location> executable = new ArrayList<>();
        Map<Location, Integer> state = new HashMap<>();
        for (Location v : vertices) {
            state.put(v, 0);
        }

        for (Location v : vertices) {
            if (state.get(v) == 0) {
                dfs(v, state, new ArrayList<>(), executable);
            }
        }
        return executable;
    }

    private void dfs(Location current, Map<Location, Integer> state, List<Location> path, List<Location> executable) {
        state.put(current, 1);
        path.add(current);

        Location next = edges.get(current);
        if (next != null) {
            if (state.getOrDefault(next, 0) == 1) {
                int idx = path.indexOf(next);
                List<Location> cycle = path.subList(idx, path.size());
                
                boolean cycleValid = true;
                for (Location loc : cycle) {
                    Location target = edges.get(loc);
                    int p = power.getOrDefault(loc, 0);
                    int d = def.getOrDefault(target, 0);
                    if (p <= d) {
                        cycleValid = false;
                        break;
                    }
                }
                if (cycleValid) {
                    executable.addAll(cycle);
                }
            } else if (state.getOrDefault(next, 0) == 0) {
                dfs(next, state, path, executable);
            }
        }

        path.remove(path.size() - 1);
        state.put(current, 2);
    }

    public Map<Location, Integer> getPower() {
        return power;
    }

    public Map<Location, Integer> getDef() {
        return def;
    }

}
