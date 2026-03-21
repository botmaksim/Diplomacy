package com.diplomacy.logic.gameControllingUnits.utils;

import java.util.List;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.RetreatPhaseOrder;
import com.diplomacy.logic.orders.SpawnPhaseOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.BeConvoyedOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.MoveOrder;
import com.diplomacy.logic.orders.retreatPhaseOrders.DieOrder;
import com.diplomacy.logic.orders.retreatPhaseOrders.RetreatOrder;
import com.diplomacy.logic.orders.spawnPhaseOrders.DismissOrder;
import com.diplomacy.logic.orders.spawnPhaseOrders.SpawnOrder;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.units.Army;
import com.diplomacy.logic.units.Fleet;
import com.diplomacy.logic.units.Unit;

public class Executor {

    List<UnfinishedMoveOrder> unfinished;

    public Executor() {
    }

    private void remove(Unit u) {
        u.getLocation().getParentProvince().setOccupyingUnit(null);
        u.setLocation(null);
    }

    private void eliminate(Unit u) {
        remove(u);
        u.getOwner().getUnits().remove(u);
    }

    private void add(Location to, Unit u) {
        u.setLocation(to);
        to.getParentProvince().setOccupyingUnit(u);
    }

    private void move(Unit u, Location to) {
        remove(u);
        add(to, u);
    }

    private void spawn(Location to, Player p) {
        Unit u;

        if (to.isArmyLocation()) {
            u = new Army(p, to);
        } else {
            u = new Fleet(p, to);
        }

        to.getParentProvince().setOccupyingUnit(u);
        p.getUnits().add(u);
    }

    public void beginExecuteMovements(List<MovementPhaseOrder> orders, GameMaster gameMaster) {
        for (Order o : orders) {
            if (o.isExecutable() && o instanceof MoveOrder m) {
                remove(m.getTarget().getParentProvince().getOccupyingUnit());
                unfinished.add(new UnfinishedMoveOrder(m.getTarget().getParentProvince().getOccupyingUnit(), m.getDestination()));
            }

            if (o.isExecutable() && o instanceof BeConvoyedOrder bc) {
                remove(bc.getTarget().getParentProvince().getOccupyingUnit());
                unfinished.add(new UnfinishedMoveOrder(bc.getTarget().getParentProvince().getOccupyingUnit(), bc.getDestination()));

            }
        }
    }

    public void endExecuteMovements(List<MovementPhaseOrder> orders, GameMaster gameMaster) {
        for (UnfinishedMoveOrder u : unfinished) {
            add(u.getDestination(), u.getUnit());
        }
    }

    public void executeRetreats(List<RetreatPhaseOrder> orders, GameMaster gameMaster) {
        for (Order o : orders) {
            if (o.isExecutable() && o instanceof DieOrder d) {
                eliminate(d.getTarget().getParentProvince().getOccupyingUnit());
            }

            if (!o.isExecutable() && o instanceof RetreatOrder r) {
                eliminate(r.getTarget().getParentProvince().getOccupyingUnit());

            }

            if (o.isExecutable() && o instanceof RetreatOrder r) {
                move(r.getTarget().getParentProvince().getOccupyingUnit(), r.getDestination());
            }
        }

        for (Unit u : gameMaster.getRetreating()) {
            eliminate(u);
        }
    }

    public void executeSpawns(List<SpawnPhaseOrder> orders, GameMaster gameMaster) {
        for (SpawnPhaseOrder o : orders) {
            if (o.isExecutable() && o instanceof SpawnOrder s) {
                spawn(s.getTarget(), s.getPlayer());
            }
            if (o.isExecutable() && o instanceof DismissOrder d) {
                eliminate(d.getTarget().getParentProvince().getOccupyingUnit());
            }
        }
    }
}
