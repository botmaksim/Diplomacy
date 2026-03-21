package com.diplomacy.logic.gameControllingUnits.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.movementPhaseOrders.BeConvoyedOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.MoveOrder;

public class BattleGraph {

    private final List<Location> vertices;
    private final Map<Location, Location> edges;
    private final Map<Location, Integer> power;

    public BattleGraph(List<MoveOrder> orders, Map<Location, Integer> powerMap) {

    }

    public void add(List<BeConvoyedOrder> orders, Map<Location, Integer> powerMap) {

    }

    public List<Location> getExecutableOrders() {

    }

    public Map<Location, Integer> getPower() {
        return power;
    }
}
