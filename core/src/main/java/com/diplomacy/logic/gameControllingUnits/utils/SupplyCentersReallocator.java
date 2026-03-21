package com.diplomacy.logic.gameControllingUnits.utils;

import java.util.List;

import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.RetreatPhaseOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.BeConvoyedOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.MoveOrder;
import com.diplomacy.logic.orders.retreatPhaseOrders.RetreatOrder;
import com.diplomacy.logic.units.Unit;

public class SupplyCentersReallocator {

    public SupplyCentersReallocator() {
    }

    public void reallocateSupplyCentersMovement(List<MovementPhaseOrder> orders) {
        for (Order o : orders) {
            if (o.isExecutable() && o instanceof MoveOrder m) {
                if (m.getDestination().getParentProvince().isSupplyCenter()) {
                    Unit u = m.getTarget().getParentProvince().getOccupyingUnit();
                    u.getOwner().getSupplyCenters().add(u.getLocation().getParentProvince());
                }
            }

            if (o.isExecutable() && o instanceof BeConvoyedOrder bc) {
                if (bc.getDestination().getParentProvince().isSupplyCenter()) {
                    Unit u = bc.getTarget().getParentProvince().getOccupyingUnit();
                    u.getOwner().getSupplyCenters().add(u.getLocation().getParentProvince());
                }
            }
        }
    }

    public void reallocateSupplyCentersRetreat(List<RetreatPhaseOrder> orders) {
        for (Order o : orders) {
            if (o.isExecutable() && o instanceof RetreatOrder r) {
                if (r.getDestination().getParentProvince().isSupplyCenter()) {
                    Unit u = r.getTarget().getParentProvince().getOccupyingUnit();
                    u.getOwner().getSupplyCenters().add(u.getLocation().getParentProvince());
                }
            }

        }
    }
}
