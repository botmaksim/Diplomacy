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
        Adjudicator adjudicator = new Adjudicator();
        adjudicator.resolve(orders, gameMaster.getUnits());
    }

    public void resolveRetreats(List<RetreatPhaseOrder> orders, GameMaster gameMaster) {
        Map<Province, List<RetreatOrder>> destinationMap = new HashMap<>();

        for (RetreatPhaseOrder o : orders) {
            if (o instanceof RetreatOrder r) {
                Province dest = r.getDestination().getParentProvince();
                destinationMap.computeIfAbsent(dest, k -> new ArrayList<>()).add(r);
            }
        }

        for (Map.Entry<Province, List<RetreatOrder>> entry : destinationMap.entrySet()) {
            List<RetreatOrder> retreats = entry.getValue();
            Province dest = entry.getKey();

            if (retreats.size() > 1) {
                for (RetreatOrder r : retreats) {
                    r.setExecutable(false);
                }
            } else if (retreats.size() == 1) {
                RetreatOrder r = retreats.get(0);
                if (dest.isBattled() || dest.isOccupied()) {
                    r.setExecutable(false);
                } else {
                    r.setExecutable(true);
                }
            }
        }
    }

    public void resolveSpawns(List<SpawnPhaseOrder> orders, GameMaster gameMaster) {
        for (SpawnPhaseOrder o : orders) {
            o.setExecutable(true);
        }
    }

}
