package com.diplomacy.logic.gameControllingUnits.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.RetreatPhaseOrder;
import com.diplomacy.logic.orders.SpawnPhaseOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.BeConvoyedOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.ConvoyOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.MoveOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.SupportOrder;
import com.diplomacy.logic.orders.retreatPhaseOrders.RetreatOrder;
import com.diplomacy.logic.units.Army;

public class Resolver {

    public void resolveMovements(List<MovementPhaseOrder> orders, GameMaster gameMaster) {
        List<MoveOrder> moveOrders = orders.stream()
                .filter(MoveOrder.class::isInstance)
                .map(MoveOrder.class::cast)
                .toList();
        List< SupportOrder> supportOrders = orders.stream()
                .filter(SupportOrder.class::isInstance)
                .map(SupportOrder.class::cast)
                .toList();

        List< BeConvoyedOrder> beConvoyedOrders = orders.stream()
                .filter(BeConvoyedOrder.class::isInstance)
                .map(BeConvoyedOrder.class::cast)
                .toList();

        Map<Location, MovementPhaseOrder> targetOrder = new HashMap<>();
        for (MovementPhaseOrder o : orders) {
            targetOrder.put(o.getTarget(), o);
        }

        Map<Location, Integer> holdPower = new HashMap<>();
        Map<Location, Integer> movePower = new HashMap<>();
        Map<Location, Integer> convoyPower = new HashMap<>();

        for (MoveOrder m : moveOrders) {
            movePower.put(m.getTarget(), 1);
            MovementPhaseOrder o = targetOrder.get(m.getDestination());
            if (o instanceof SupportOrder s) {
                s.setExecutable(false);
                supportOrders.remove(s);
            }
            m.getDestination().getParentProvince().setBattled(true);
            m.setExecutable(false);
        }

        for (SupportOrder s : supportOrders) {
            MovementPhaseOrder o = targetOrder.get(s.getSupportedUnit());
            if (o instanceof MoveOrder m) {
                movePower.merge(m.getTarget(), 1, Integer::sum);
            } else if (o instanceof BeConvoyedOrder bc) {
                convoyPower.merge(bc.getTarget(), 1, Integer::sum);
            } else if (holdPower.containsKey(s.getSupportedUnit())) {
                holdPower.put(s.getSupportedUnit(), 2);
            } else {
                holdPower.merge(s.getTarget(), 1, Integer::sum);
            }
        }

        BattleGraph battleGraph = new BattleGraph(moveOrders, movePower, holdPower);
        List<Location> executableLocation = battleGraph.getExecutableOrders();

        for (Location l : executableLocation) {
            MoveOrder m = (MoveOrder) targetOrder.get(l);

            if (targetOrder.containsKey(m.getDestination())) {
                Order o = targetOrder.get(m.getDestination());
                if (o instanceof ConvoyOrder c) {
                    c.setExecutable(false);
                }
            }
        }

        for (BeConvoyedOrder bc : beConvoyedOrders) {
            Army a = (Army) bc.getTarget().getParentProvince().getOccupyingUnit();
            if (!a.canConvoyTo(bc.getDestination(), pair -> {
                MovementPhaseOrder o = targetOrder.get(pair.first());
                return o instanceof ConvoyOrder c && c.getConvoyedArmy().getParentProvince().getOccupyingUnit() == pair.second() && c.isExecutable();
            })) {
                bc.setExecutable(false);
                beConvoyedOrders.remove(bc);
            } else {
                MovementPhaseOrder o = targetOrder.get(bc.getDestination());
                if (o instanceof SupportOrder s) {
                    s.setExecutable(false);
                    supportOrders.remove(s);
                    MovementPhaseOrder d = targetOrder.get(s.getSupportedUnit());
                    switch (d) {
                        case MoveOrder m -> {
                            battleGraph.getPower().merge(m.getTarget(), -1, Integer::sum);
                        }
                        case BeConvoyedOrder dbc ->
                            convoyPower.merge(dbc.getTarget(), -1, Integer::sum);
                        default -> {
                            battleGraph.getDef().merge(s.getSupportedUnit(), -1, Integer::sum);
                        }
                    }
                }
            }
        }

        battleGraph.add(beConvoyedOrders, convoyPower);
        executableLocation = battleGraph.getExecutableOrders();

        for (Location l : executableLocation) {
            MovementPhaseOrder o = targetOrder.get(l);
            o.setExecutable(true);
            if (o instanceof MoveOrder m) {
                m.getDestination().getParentProvince().setBattled(true);
                m.getDestination().getParentProvince().getOccupyingUnit().setRetreating(true);
            }

            if (o instanceof BeConvoyedOrder bc) {
                bc.getDestination().getParentProvince().setBattled(true);
                bc.getDestination().getParentProvince().getOccupyingUnit().setRetreating(true);
            }
        }
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
