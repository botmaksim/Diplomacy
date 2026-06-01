package com.diplomacy.desktop.controllers;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.movementPhaseOrders.ConvoyOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.MoveOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.SupportOrder;
import com.diplomacy.logic.orders.retreatPhaseOrders.RetreatOrder;
import com.diplomacy.logic.orders.spawnPhaseOrders.SpawnOrder;
import com.diplomacy.logic.orders.utils.OrderCreator;
import com.diplomacy.logic.orders.utils.OrderPrototype;
import com.diplomacy.logic.orders.utils.OrderType;
import com.diplomacy.logic.orders.utils.VerificationResult;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.turnClassificator.PhaseType;
import com.diplomacy.logic.units.Unit;

import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;

public class OrderController {

    private final GameMaster gameMaster;
    private final MapView mapView;
    private final ListView<String> ordersListView;
    private final TextArea logArea;
    private final OrderCreator orderCreator;

    private Unit selectedUnit;
    private OrderType currentOrderType;
    private OrderPrototype currentPrototype;
    private boolean waitingForSecondaryTarget;
    private Player currentPlayer;
    private boolean ordersConfirmed;

    public OrderController(GameMaster gm, MapView mv, ListView<String> orders, TextArea log) {
        this.gameMaster = gm;
        this.mapView = mv;
        this.ordersListView = orders;
        this.logArea = log;
        this.orderCreator = new OrderCreator();
        this.currentPrototype = new OrderPrototype();
    }

    private void log(String message) {
        System.out.println(message);
        if (logArea != null) {
            logArea.appendText(message + "\n");
        }
    }

    public void setOrderType(OrderType type) {
        clearSelection();
        this.currentOrderType = type;
        waitingForSecondaryTarget = false;
        log("[OrderController] Order type set to: " + type);
    }

    public void setCurrentPlayer(Player player) {
        clearSelection();
        this.currentPlayer = player;
        log("[OrderController] Current player set to: " + (player != null ? player.getName() : "null"));
        rebuildOrderListAndVisuals();
    }

    public OrderType getCurrentOrderType() {
        return currentOrderType;
    }

    private void clearCurrentOrder() {
        if (selectedUnit != null) {
            mapView.highlightSelectedUnit(selectedUnit, false);
        }
        selectedUnit = null;
        currentPrototype = new OrderPrototype();
        mapView.clearHighlights();
    }

    private void clearSelection() {
        clearCurrentOrder();
        currentOrderType = null;
        waitingForSecondaryTarget = false;
    }

    public void processClick(Province clicked) {
        if (ordersConfirmed) {
            return;
        }

        if (currentOrderType == null) {
            return;
        }

        if (waitingForSecondaryTarget) {
            completeSecondaryOrder(clicked);
            return;
        }

        if (selectedUnit == null) {
            if (isOneClickOrder(currentOrderType)) {
                handleOneClickOrder(clicked);
                return;
            }
            handleUnitSelection(clicked);
            return;
        }

        if (currentOrderType == OrderType.HOLD) {
            Province selectedProvince = selectedUnit.getLocation().getParentProvince();
            if (clicked != null && clicked.equals(selectedProvince)) {
                tryFinalizeOrder();
            } else {
                handleUnitSelection(clicked);
            }
            return;
        }

        if (currentOrderType == OrderType.DIE) {
            if (clicked != null && clicked.equals(selectedUnit.getLocation().getParentProvince())) {
                tryFinalizeOrder();
            } else {
                handleUnitSelection(clicked);
            }
            return;
        }

        if (currentOrderType == OrderType.DISMISS) {
            if (clicked != null && clicked.equals(selectedUnit.getLocation().getParentProvince())) {
                tryFinalizeOrder();
            } else {
                handleUnitSelection(clicked);
            }
            return;
        }

        handleOrderTarget(clicked);
    }

    private boolean isOneClickOrder(OrderType type) {
        return type == OrderType.SPAWN;
    }

    private void handleOneClickOrder(Province clicked) {
        if (clicked == null) {
            return;
        }

        switch (currentOrderType) {
            case SPAWN -> {
                if (clicked.getLocations().isEmpty()) {
                    log("[OrderController] Province " + clicked.getName() + " has no locations for spawning.");
                    return;
                }
                Location loc = clicked.getLocations().get(0);
                Player owner = findPlayerForProvince(clicked);
                if (owner == null) {
                    log("[OrderController] No player owns province " + clicked.getName() + " for spawning.");
                    return;
                }
                currentPrototype.setSelectedLocation(loc);
                currentPrototype.setPlayer(owner);
                currentPrototype.setOrderType(currentOrderType);
                tryFinalizeOrder();
            }
            default -> {}
        }
    }

    private Player findPlayerForProvince(Province province) {
        for (Player p : gameMaster.getPlayers()) {
            if (p.getCountry().getProvinces().contains(province)) {
                return p;
            }
        }
        return null;
    }

    private void handleUnitSelection(Province clicked) {
        if (clicked == null) {
            log("[OrderController] Clicked on empty area, cannot select unit.");
            return;
        }
        Unit u = clicked.getOccupyingUnit();
        if (u == null) {
            log("[OrderController] Province " + clicked.getName() + " is not occupied.");
            return;
        }
        if (currentPlayer != null && !u.getOwner().equals(currentPlayer)) {
            log("[OrderController] Cannot select unit of " + u.getOwner().getName() + " — you are " + currentPlayer.getName());
            return;
        }
        clearCurrentOrder();
        selectedUnit = u;
        currentPrototype.setSelectedLocation(selectedUnit.getLocation());
        currentPrototype.setPlayer(selectedUnit.getOwner());
        currentPrototype.setOrderType(currentOrderType);
        log("[OrderController] Unit selected: " + u.getTypeName() + " in " + clicked.getName());
        mapView.highlightSelectedUnit(u, true);
    }

    private void handleOrderTarget(Province target) {
        if (target == null) {
            log("[OrderController] Target is null, cancelling order.");
            clearCurrentOrder();
            return;
        }

        switch (currentOrderType) {
            case MOVE -> {
                currentPrototype.setDestination(target);
                tryFinalizeOrder();
            }
            case BECONVOYED -> {
                currentPrototype.setDestination(target);
                tryFinalizeOrder();
            }
            case RETREAT -> {
                currentPrototype.setDestination(target);
                tryFinalizeOrder();
            }
            case SUPPORT -> {
                Unit supported = target.getOccupyingUnit();
                if (supported == null) {
                    log("[OrderController] No unit in " + target.getName() + " to support.");
                    clearCurrentOrder();
                    return;
                }
                if (currentPlayer != null && !supported.getOwner().equals(currentPlayer)) {
                    log("[OrderController] Cannot support unit of " + supported.getOwner().getName() + " — you are " + currentPlayer.getName());
                    clearCurrentOrder();
                    return;
                }
                currentPrototype.setAdditionalUnit(supported);
                waitingForSecondaryTarget = true;
                log("[OrderController] Support target unit selected: " + supported.getTypeName() + " in " + target.getName());
                highlightSupportDestinations(supported);
            }
            case CONVOY -> {
                Unit armyToConvoy = target.getOccupyingUnit();
                if (armyToConvoy == null) {
                    log("[OrderController] No unit in " + target.getName() + " to convoy.");
                    clearCurrentOrder();
                    return;
                }
                if (currentPlayer != null && !armyToConvoy.getOwner().equals(currentPlayer)) {
                    log("[OrderController] Cannot convoy unit of " + armyToConvoy.getOwner().getName() + " — you are " + currentPlayer.getName());
                    clearCurrentOrder();
                    return;
                }
                currentPrototype.setAdditionalUnit(armyToConvoy);
                waitingForSecondaryTarget = true;
                log("[OrderController] Convoy army selected: " + armyToConvoy.getTypeName() + " in " + target.getName());
            }
            default -> {
                log("[OrderController] Unsupported order type: " + currentOrderType);
                clearCurrentOrder();
            }
        }
    }

    private void completeSecondaryOrder(Province destination) {
        if (destination == null) {
            log("[OrderController] Secondary target is null, cancelling.");
            clearCurrentOrder();
            waitingForSecondaryTarget = false;
            return;
        }
        log("[OrderController] Secondary target: " + destination.getName());
        currentPrototype.setDestination(destination);
        tryFinalizeOrder();
    }

    private PhaseType getPhaseForOrderType(OrderType type) {
        if (type == null) return PhaseType.MOVEMENT;
        return switch (type) {
            case MOVE, HOLD, SUPPORT, CONVOY, BECONVOYED -> PhaseType.MOVEMENT;
            case RETREAT, DIE -> PhaseType.RETREAT;
            case SPAWN, DISMISS -> PhaseType.SPAWN;
        };
    }

    public boolean isOrdersConfirmed() {
        return ordersConfirmed;
    }

    public void confirmOrders() {
        ordersConfirmed = true;
        clearCurrentOrder();
        log("[OrderController] Orders confirmed for " + currentPlayer.getName() + ". Further changes blocked.");
    }

    public void cancelConfirmation() {
        ordersConfirmed = false;
        currentOrderType = null;
        log("[OrderController] Orders confirmation cancelled for " + currentPlayer.getName() + ".");
    }

    private void tryFinalizeOrder() {
        PhaseType phaseType = getPhaseForOrderType(currentOrderType);

        VerificationResult verification = orderCreator.verifyOrder(currentPrototype, phaseType);
        if (verification != VerificationResult.OK) {
            log("[OrderController] Order verification failed: " + verification);
            clearCurrentOrder();
            waitingForSecondaryTarget = false;
            return;
        }

        Order order = orderCreator.createOrder(currentPrototype, phaseType);
        if (order != null) {
            if (selectedUnit != null) {
                removeExistingOrdersForUnit(selectedUnit);
            }
            log("[OrderController] Order created: " + order.getClass().getSimpleName());
            gameMaster.addOrder(order, currentPlayer);
            rebuildOrderListAndVisuals();
        } else {
            log("[OrderController] Failed to create order.");
            log("  orderType=" + currentPrototype.getOrderType()
                + " location=" + (currentPrototype.getSelectedLocation() != null ? currentPrototype.getSelectedLocation().getParentProvince().getName() : "null")
                + " destination=" + (currentPrototype.getDestination() != null ? currentPrototype.getDestination().getName() : "null")
                + " player=" + (currentPrototype.getPlayer() != null ? currentPrototype.getPlayer().getCountry().getName() : "null")
                + " additionalUnit=" + (currentPrototype.getAdditionalUnit() != null ? "present" : "null"));
        }
        clearCurrentOrder();
        waitingForSecondaryTarget = false;
    }

    private void removeExistingOrdersForUnit(Unit unit) {
        var pending = gameMaster.getPendingOrders(currentPlayer);
        pending.removeIf(o -> {
            Location loc = o.getTarget();
            if (loc == null) return false;
            Unit u = loc.getParentProvince().getOccupyingUnit();
            return unit.equals(u);
        });
    }

    private void highlightSupportDestinations(Unit supported) {
        for (var loc : supported.getLocation().getNeighbours()) {
            Province p = loc.getParentProvince();
            mapView.highlightProvince(p.getId(), Color.LIGHTGREEN);
        }
    }

    public void removeOrder(int index) {
        if (ordersConfirmed) return;
        if (currentPlayer == null) return;
        var pending = gameMaster.getPendingOrders(currentPlayer);
        if (index < 0 || index >= pending.size()) return;
        gameMaster.removeOrder(currentPlayer, index);
        rebuildOrderListAndVisuals();
        log("[OrderController] Order removed at index " + index);
    }

    public void rebuildOrderListAndVisuals() {
        ordersListView.getItems().clear();
        mapView.clearArrows();

        if (currentPlayer == null) return;

        for (Order order : gameMaster.getPendingOrders(currentPlayer)) {
            String text = formatOrderText(order);
            ordersListView.getItems().add(text);
            drawOrderVisuals(order);
        }
    }

    private String formatOrderText(Order order) {
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
        if (order instanceof com.diplomacy.logic.orders.spawnPhaseOrders.DismissOrder) {
            return base + " dismissed";
        }
        return base;
    }

    private String getUnitPrefix(Location loc) {
        if (loc == null) return "?";
        Unit u = loc.getParentProvince().getOccupyingUnit();
        if (u == null) return "?";
        return u instanceof com.diplomacy.logic.units.Fleet ? "F" : "A";
    }

    private void drawOrderVisuals(Order order) {
        if (order == null) return;
        Location target = order.getTarget();
        if (target == null) return;
        String from = target.getParentProvince().getId();

        if (order instanceof com.diplomacy.logic.orders.movementPhaseOrders.HoldOrder) {
            mapView.drawHoldMarker(from);
            return;
        }
        if (order instanceof MoveOrder mo) {
            drawArrow(from, mo.getDestination().getParentProvince().getId(), OrderType.MOVE);
            return;
        }
        if (order instanceof com.diplomacy.logic.orders.movementPhaseOrders.BeConvoyedOrder bco) {
            drawArrow(from, bco.getDestination().getParentProvince().getId(), OrderType.BECONVOYED);
            return;
        }
        if (order instanceof SupportOrder so) {
            Location dest = so.getDestination();
            if (dest != null) {
                drawArrow(from, dest.getParentProvince().getId(), OrderType.SUPPORT);
            }
            return;
        }
        if (order instanceof ConvoyOrder co) {
            mapView.drawConvoyMarker(from);
            // String armyOrigin = co.getConvoyedArmy().getParentProvince().getId();
            // mapView.drawConvoyLink(from, armyOrigin);
            return;
        }
        if (order instanceof RetreatOrder ro) {
            drawArrow(from, ro.getDestination().getParentProvince().getId(), OrderType.RETREAT);
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

    private void drawArrow(String from, String to, OrderType type) {
        mapView.drawOrderArrow(from, to, type);
    }

    private void highlightPossibleMoves() {}

    private void highlightSupportCandidates() {}

    private void highlightPossibleConvoys() {}
}
