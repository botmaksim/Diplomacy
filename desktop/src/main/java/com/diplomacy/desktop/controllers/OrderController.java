package com.diplomacy.desktop.controllers;

import java.util.ArrayList;
import java.util.List;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.utils.OrderCreator;
import com.diplomacy.logic.orders.utils.OrderPrototype;
import com.diplomacy.logic.orders.utils.OrderType;
import com.diplomacy.logic.orders.utils.VerificationResult;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.turnClassificator.PhaseType;
import com.diplomacy.logic.units.Unit;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class OrderController {

    private final GameMaster gameMaster;
    private final MapView mapView;
    private final TextArea logArea;
    private final OrderCreator orderCreator;
    private final OrderLedger orderLedger;
    private final PlayerSession playerSession;

    private Unit selectedUnit;
    private OrderType currentOrderType;
    private OrderPrototype currentPrototype;
    private boolean waitingForSecondaryTarget;

    public OrderController(GameMaster gm, MapView mv, TextArea log,
                           OrderLedger orderLedger, PlayerSession playerSession) {
        this.gameMaster = gm;
        this.mapView = mv;
        this.logArea = log;
        this.orderCreator = new OrderCreator();
        this.currentPrototype = new OrderPrototype();
        this.orderLedger = orderLedger;
        this.playerSession = playerSession;
    }

    private void log(String message) {
        System.out.println(message);
        if (logArea != null) {
            logArea.setText(message);
        }
    }

    public void setOrderType(OrderType type) {
        clearSelection();
        this.currentOrderType = type;
        waitingForSecondaryTarget = false;
        log("Order type set to: " + type);
    }

    public OrderType getCurrentOrderType() {
        return currentOrderType;
    }

    public void clearSelection() {
        clearCurrentOrder();
        currentOrderType = null;
        waitingForSecondaryTarget = false;
    }

    private void clearCurrentOrder() {
        if (selectedUnit != null) {
            mapView.highlightSelectedUnit(selectedUnit, false);
        }
        selectedUnit = null;
        currentPrototype = new OrderPrototype();
        mapView.clearHighlights();
    }

    private boolean hasMultipleReachableCoasts(Unit unit, Province target) {
        if (unit == null || target == null) return false;
        List<Location> coastLocs = new ArrayList<>();
        for (Location n : unit.getLocation().getNeighbours()) {
            if (n.getParentProvince().equals(target) && n.getName() != null && n.getName().startsWith("coast")) {
                coastLocs.add(n);
            }
        }
        return coastLocs.size() > 1;
    }

    private List<Location> getReachableCoastLocations(Unit unit, Province target) {
        List<Location> result = new ArrayList<>();
        if (unit == null || target == null) return result;
        for (Location n : unit.getLocation().getNeighbours()) {
            if (n.getParentProvince().equals(target) && n.getName() != null && n.getName().startsWith("coast")) {
                result.add(n);
            }
        }
        return result;
    }

    private Location showCoastSelectionDialog(Province province, List<Location> coastLocs) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/coastSelectionDialog.fxml"));
            Parent root = loader.load();
            CoastSelectionDialogController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(mapView.getView().getScene().getWindow());
            dialog.setTitle("Choose Coast");
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));

            controller.initData(coastLocs, province.getName());
            controller.setStage(dialog);

            dialog.showAndWait();
            return controller.getSelectedLocation();
        } catch (Exception e) {
            log("Failed to load coast selection dialog: " + e.getMessage());
            return null;
        }
    }

    public void processClick(Province clicked) {
        if (!waitingForSecondaryTarget && currentOrderType == null) {
            mapView.clearHighlights();
            if (clicked != null) {
                mapView.highlightProvince(clicked.getId(), Color.LIGHTBLUE);
                log("Province clicked: " + clicked.getName());
            }
        }

        Player currentPlayer = playerSession.getCurrentPlayer();
        if (currentPlayer == null || orderLedger.isConfirmed(currentPlayer)) {
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
                    log("Province " + clicked.getName() + " has no locations for spawning.");
                    return;
                }
                Location loc = clicked.getLocations().get(0);
                Player owner = findPlayerForProvince(clicked);
                if (owner == null) {
                    log("No player owns province " + clicked.getName() + " for spawning.");
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
        Player currentPlayer = playerSession.getCurrentPlayer();
        if (clicked == null) {
            log("Clicked on empty area, cannot select unit.");
            return;
        }
        Unit u = clicked.getOccupyingUnit();
        if (u == null) {
            log("Province " + clicked.getName() + " is not occupied.");
            return;
        }
        if (currentPlayer != null && !u.getOwner().equals(currentPlayer)) {
            log("Cannot select unit of " + u.getOwner().getName() + " — you are " + currentPlayer.getName());
            return;
        }
        clearCurrentOrder();
        selectedUnit = u;
        currentPrototype.setSelectedLocation(selectedUnit.getLocation());
        currentPrototype.setPlayer(selectedUnit.getOwner());
        currentPrototype.setOrderType(currentOrderType);
        log("Unit selected: " + u.getTypeName() + " in " + clicked.getName());
        mapView.highlightSelectedUnit(u, true);
        mapView.highlightProvince(clicked.getId(), Color.LIGHTBLUE);
    }

    private void handleOrderTarget(Province target) {
        Player currentPlayer = playerSession.getCurrentPlayer();
        if (target == null) {
            log("Target is null, cancelling order.");
            clearCurrentOrder();
            return;
        }

        switch (currentOrderType) {
            case MOVE -> {
                currentPrototype.setDestination(target);
                if (selectedUnit != null && hasMultipleReachableCoasts(selectedUnit, target)) {
                    List<Location> coastLocs = getReachableCoastLocations(selectedUnit, target);
                    Location chosen = showCoastSelectionDialog(target, coastLocs);
                    if (chosen == null) {
                        log("Coast selection cancelled for " + target.getName());
                        clearCurrentOrder();
                        return;
                    }
                    currentPrototype.setDestinationLocation(chosen);
                }
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
                    log("No unit in " + target.getName() + " to support.");
                    clearCurrentOrder();
                    return;
                }
                if (currentPlayer != null && !supported.getOwner().equals(currentPlayer)) {
                    log("Cannot support unit of " + supported.getOwner().getName() + " — you are " + currentPlayer.getName());
                    clearCurrentOrder();
                    return;
                }
                currentPrototype.setAdditionalUnit(supported);
                waitingForSecondaryTarget = true;
                log("Support target unit selected: " + supported.getTypeName() + " in " + target.getName());
                highlightSupportDestinations(supported);
            }
            case CONVOY -> {
                Unit armyToConvoy = target.getOccupyingUnit();
                if (armyToConvoy == null) {
                    log("No unit in " + target.getName() + " to convoy.");
                    clearCurrentOrder();
                    return;
                }
                if (currentPlayer != null && !armyToConvoy.getOwner().equals(currentPlayer)) {
                    log("Cannot convoy unit of " + armyToConvoy.getOwner().getName() + " — you are " + currentPlayer.getName());
                    clearCurrentOrder();
                    return;
                }
                currentPrototype.setAdditionalUnit(armyToConvoy);
                waitingForSecondaryTarget = true;
                log("Convoy army selected: " + armyToConvoy.getTypeName() + " in " + target.getName());
            }
            default -> {
                log("Unsupported order type: " + currentOrderType);
                clearCurrentOrder();
            }
        }
    }

    private void completeSecondaryOrder(Province destination) {
        if (destination == null) {
            log("Secondary target is null, cancelling.");
            clearCurrentOrder();
            waitingForSecondaryTarget = false;
            return;
        }
        log("Secondary target: " + destination.getName());
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

    private void tryFinalizeOrder() {
        Player currentPlayer = playerSession.getCurrentPlayer();
        PhaseType phaseType = getPhaseForOrderType(currentOrderType);

        VerificationResult verification = orderCreator.verifyOrder(currentPrototype, phaseType);
        if (verification != VerificationResult.OK) {
            log("Order verification failed: " + verification);
            clearCurrentOrder();
            waitingForSecondaryTarget = false;
            return;
        }

        Order order = orderCreator.createOrder(currentPrototype, phaseType);
        if (order != null) {
            if (selectedUnit != null) {
                orderLedger.removeExistingOrdersForUnit(selectedUnit, currentPlayer);
            }
            log("Order created: " + order.getClass().getSimpleName());
            orderLedger.addOrder(order, currentPlayer);
            orderLedger.rebuild(currentPlayer);
        } else {
            log("Failed to create order.");
            log("  orderType=" + currentPrototype.getOrderType()
                + " location=" + (currentPrototype.getSelectedLocation() != null ? currentPrototype.getSelectedLocation().getParentProvince().getName() : "null")
                + " destination=" + (currentPrototype.getDestination() != null ? currentPrototype.getDestination().getName() : "null")
                + " player=" + (currentPrototype.getPlayer() != null ? currentPrototype.getPlayer().getCountry().getName() : "null")
                + " additionalUnit=" + (currentPrototype.getAdditionalUnit() != null ? "present" : "null"));
        }
        clearCurrentOrder();
        waitingForSecondaryTarget = false;
    }

    private void highlightSupportDestinations(Unit supported) {
        for (var loc : supported.getLocation().getNeighbours()) {
            Province p = loc.getParentProvince();
            mapView.highlightProvince(p.getId(), Color.LIGHTGREEN);
        }
    }
}
