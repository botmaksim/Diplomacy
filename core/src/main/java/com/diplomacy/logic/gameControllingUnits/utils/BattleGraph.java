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

    }

    public Map<Location, Integer> getPower() {
        return power;
    }

    public Map<Location, Integer> getDef() {
        return def;
    }

}
