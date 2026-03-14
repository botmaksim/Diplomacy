package com.diplomacy.logic.gameControllingUnits.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.orders.RetreatPhaseOrder;
import com.diplomacy.logic.orders.SpawnPhaseOrder;
import com.diplomacy.logic.orders.retreatPhaseOrders.RetreatOrder;

public class Resolver {

    public void resolveMovements(List<MovementPhaseOrder> orders, GameMaster gameMaster) {
    }

    public void resolveRetreats(List<RetreatPhaseOrder> orders, GameMaster gameMaster) {
        Map<Province, List<RetreatOrder>> map = new HashMap<>();
        for (RetreatPhaseOrder o : orders) {
            if (o instanceof RetreatOrder r) {
                if (!map.containsKey(r.getDestination().getParentProvince())) {
                    map.put(r.getDestination().getParentProvince(), new ArrayList<>());
                }
                map.get(r.getDestination().getParentProvince()).add(r);
            }
        }
        map.forEach((player, os) -> {
            if (orders.size() != 1) {
                for (RetreatOrder r : os) {
                    r.setExecutable(false);
                }
            }
        });
    }

    public void resolveSpawns(List<SpawnPhaseOrder> orders, GameMaster gameMaster) {
    }

}
