package com.diplomacy.desktop.controllers;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.movementPhaseOrders.ConvoyOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.MoveOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.SupportOrder;
import com.diplomacy.logic.orders.retreatPhaseOrders.RetreatOrder;
import com.diplomacy.logic.orders.spawnPhaseOrders.DismissOrder;
import com.diplomacy.logic.orders.spawnPhaseOrders.SpawnOrder;
import com.diplomacy.logic.units.Fleet;
import com.diplomacy.logic.units.Unit;

public final class OrderFormatter {

    private OrderFormatter() {}

    public static String format(Order order) {
        if (order instanceof SpawnOrder) {
            return "Spawn in " + order.getTarget().getParentProvince().getName();
        }
        String base = getUnitPrefix(order.getTarget()) + " " + order.getTarget().getParentProvince().getName();

        if (order instanceof MoveOrder mo) {
            return base + " -> " + mo.getDestination().getParentProvince().getName();
        }
        if (order instanceof com.diplomacy.logic.orders.movementPhaseOrders.HoldOrder) {
            return base + " holds";
        }
        if (order instanceof SupportOrder so) {
            String sup = getUnitPrefix(so.getSupportedUnit()) + " "
                + so.getSupportedUnit().getParentProvince().getName();
            String dest = so.getDestination() != null
                ? so.getDestination().getParentProvince().getName() : "hold";
            return base + " supports " + sup + " -> " + dest;
        }
        if (order instanceof ConvoyOrder co) {
            String armyInfo = getUnitPrefix(co.getConvoyedArmy()) + " "
                + co.getConvoyedArmy().getParentProvince().getName();
            String dest = co.getDestination() != null
                ? co.getDestination().getParentProvince().getName() : "?";
            return base + " convoys " + armyInfo + " -> " + dest;
        }
        if (order instanceof com.diplomacy.logic.orders.movementPhaseOrders.BeConvoyedOrder bco) {
            return base + " (convoyed) -> " + bco.getDestination().getParentProvince().getName();
        }
        if (order instanceof RetreatOrder ro) {
            return base + " retreats -> " + ro.getDestination().getParentProvince().getName();
        }
        if (order instanceof com.diplomacy.logic.orders.retreatPhaseOrders.DieOrder) {
            return base + " dies";
        }
        if (order instanceof DismissOrder) {
            return base + " dismissed";
        }
        return base;
    }

    public static String getUnitPrefix(Location loc) {
        if (loc == null) return "?";
        Unit u = loc.getParentProvince().getOccupyingUnit();
        if (u == null) return "?";
        return u instanceof Fleet ? "F" : "A";
    }
}
