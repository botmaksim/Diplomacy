package com.diplomacy.logic.gameControllingUnits.utils;

import java.util.List;
import com.diplomacy.logic.gameControllingUnits.GameMaster;

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

    public void reallocateSupplyCenters(GameMaster gameMaster) {
        if (gameMaster.getTurn().getSeason() != com.diplomacy.logic.turnClassificator.Season.FALL) {
            return;
        }

        for (com.diplomacy.logic.player.Player player : gameMaster.getPlayers()) {
            for (Unit u : player.getUnits()) {
                com.diplomacy.logic.geography.basic.Province p = u.getLocation().getParentProvince();
                if (p.isSupplyCenter()) {
                    for (com.diplomacy.logic.player.Player other : gameMaster.getPlayers()) {
                        other.getSupplyCenters().remove(p);
                    }
                    player.getSupplyCenters().add(p);
                }
            }
        }
    }
}
