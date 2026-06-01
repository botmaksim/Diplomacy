package com.diplomacy.desktop.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.movementPhaseOrders.ConvoyOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.MoveOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.SupportOrder;
import com.diplomacy.logic.orders.retreatPhaseOrders.RetreatOrder;
import com.diplomacy.logic.orders.spawnPhaseOrders.SpawnOrder;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.units.Unit;

import javafx.scene.control.ListView;

public class OrderLedger {

    private final GameMaster gameMaster;
    private final MapView mapView;
    private final ListView<String> ordersListView;
    private final Set<Player> confirmedPlayers = new HashSet<>();

    public OrderLedger(GameMaster gameMaster, MapView mapView, ListView<String> ordersListView) {
        this.gameMaster = gameMaster;
        this.mapView = mapView;
        this.ordersListView = ordersListView;
    }

    public void addOrder(Order order, Player player) {
        gameMaster.addOrder(order, player);
    }

    public void removeOrder(Player player, int index) {
        if (player == null || confirmedPlayers.contains(player)) return;
        List<Order> pending = gameMaster.getPendingOrders(player);
        if (index < 0 || index >= pending.size()) return;
        gameMaster.removeOrder(player, index);
        rebuild(player);
    }

    public List<Order> getOrders(Player player) {
        return gameMaster.getPendingOrders(player);
    }

    public boolean isConfirmed(Player player) {
        return player != null && confirmedPlayers.contains(player);
    }

    public void confirm(Player player) {
        if (player == null) return;
        confirmedPlayers.add(player);
    }

    public void cancelConfirmation(Player player) {
        if (player == null) return;
        confirmedPlayers.remove(player);
    }

    public void removeExistingOrdersForUnit(Unit unit, Player player) {
        List<Order> pending = gameMaster.getPendingOrders(player);
        pending.removeIf(o -> {
            Location loc = o.getTarget();
            if (loc == null) return false;
            Unit u = loc.getParentProvince().getOccupyingUnit();
            return unit.equals(u);
        });
    }

    public void rebuild(Player player) {
        ordersListView.getItems().clear();
        mapView.clearArrows();
        if (player == null) return;
        for (Order order : gameMaster.getPendingOrders(player)) {
            ordersListView.getItems().add(OrderFormatter.format(order));
            drawOrderVisual(order);
        }
    }

    private void drawOrderVisual(Order order) {
        if (order == null) return;
        Location target = order.getTarget();
        if (target == null) return;
        String from = target.getParentProvince().getId();

        if (order instanceof com.diplomacy.logic.orders.movementPhaseOrders.HoldOrder) {
            mapView.drawHoldMarker(from);
            return;
        }
        if (order instanceof MoveOrder mo) {
            mapView.drawOrderArrow(from, mo.getDestination().getParentProvince().getId(), com.diplomacy.logic.orders.utils.OrderType.MOVE);
            return;
        }
        if (order instanceof com.diplomacy.logic.orders.movementPhaseOrders.BeConvoyedOrder bco) {
            mapView.drawOrderArrow(from, bco.getDestination().getParentProvince().getId(), com.diplomacy.logic.orders.utils.OrderType.BECONVOYED);
            return;
        }
        if (order instanceof SupportOrder so) {
            Location dest = so.getDestination();
            if (dest != null) {
                mapView.drawOrderArrow(from, dest.getParentProvince().getId(), com.diplomacy.logic.orders.utils.OrderType.SUPPORT);
            }
            return;
        }
        if (order instanceof ConvoyOrder) {
            mapView.drawConvoyMarker(from);
            return;
        }
        if (order instanceof RetreatOrder ro) {
            mapView.drawOrderArrow(from, ro.getDestination().getParentProvince().getId(), com.diplomacy.logic.orders.utils.OrderType.RETREAT);
            return;
        }
        if (order instanceof com.diplomacy.logic.orders.retreatPhaseOrders.DieOrder) {
            mapView.drawDieMarker(from);
            return;
        }
        if (order instanceof com.diplomacy.logic.orders.spawnPhaseOrders.DismissOrder) {
            mapView.drawDismissMarker(from);
            return;
        }
        if (order instanceof SpawnOrder) {
            mapView.drawSpawnMarker(from);
            return;
        }
    }
}
