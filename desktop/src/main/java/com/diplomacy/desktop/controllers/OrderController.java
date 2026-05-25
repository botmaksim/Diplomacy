package com.diplomacy.desktop.controllers;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.orders.utils.OrderCreator;
import com.diplomacy.logic.orders.utils.OrderPrototype;
import com.diplomacy.logic.orders.utils.OrderType;
import com.diplomacy.logic.units.Unit;

import javafx.scene.control.ListView;
import javafx.scene.paint.Color;

public class OrderController {

    private final GameMaster gameMaster;
    private final MapView mapView;
    private final ListView<String> ordersListView;
    private final OrderCreator orderCreator;

    private Unit selectedUnit;
    private OrderPrototype currentPrototype;
    private OrderType currentOrderType;
    private boolean waitingForSupportTarget;

    public OrderController(GameMaster gm, MapView mv, ListView<String> orders) {
        this.gameMaster = gm;
        this.mapView = mv;
        this.ordersListView = orders;
        this.orderCreator = new OrderCreator();
        this.currentPrototype = new OrderPrototype();
    }

    public void processClick(Province clicked) {
        if (currentOrderType == null) {
            handleUnitSelection(clicked);
        } else if (waitingForSupportTarget) {
            completeSupportOrder(clicked);
        } else {
            handleOrderTarget(clicked);
        }
    }

    public void setOrderType(OrderType type) {
        if (selectedUnit == null) return;

        clearPrototypeExceptUnit();
        currentOrderType = type;
        currentPrototype.setOrderType(type);
        currentPrototype.setSelectedLocation(selectedUnit.getLocation());
        currentPrototype.setPlayer(selectedUnit.getOwner());

        switch (type) {
            case HOLD -> tryFinalizeOrder();
            case MOVE -> highlightPossibleMoves();
            case SUPPORT -> highlightSupportCandidates();
            case CONVOY -> highlightPossibleConvoys();
            default -> { /* другие фазы */ }
        }
    }

    private void handleUnitSelection(Province clicked) {
        if (clicked == null) {
            clearSelection();
            return;
        }
        Unit u = clicked.getOccupyingUnit();
        if (u != null /*&& u.getOwner().equals(gameMaster.getCurrentPlayer())*/) {
            selectUnit(u);
        } else {
            clearSelection();
        }
    }

    private void selectUnit(Unit u) {
        clearSelection();
        selectedUnit = u;
        mapView.highlightSelectedUnit(u, true);
    }

    private void handleOrderTarget(Province target) {
        if (target == null) {
            clearSelection();
            return;
        }
        switch (currentOrderType) {
            case MOVE -> {
                currentPrototype.setDestination(target);
                tryFinalizeOrder();
            }
            case SUPPORT -> startSupport(target);
            default -> { /* другие типы */ }
        }
    }

    private void startSupport(Province target) {
        Unit supported = target.getOccupyingUnit();
        if (supported == null) return;
        currentPrototype.setAdditionalUnit(supported);
        waitingForSupportTarget = true;
        highlightSupportDestinations(supported);
    }

    private void completeSupportOrder(Province destination) {
        if (destination == null) {
            clearSelection();
            return;
        }
        currentPrototype.setDestination(destination);
        tryFinalizeOrder();
    }

    private void tryFinalizeOrder() {
        // VerificationResult result = orderCreator.verifyOrder(currentPrototype, gameMaster.getCurrentPhaseType());
        // if (result == VerificationResult.OK) {
        //     var order = orderCreator.createOrder(currentPrototype, gameMaster.getCurrentPhaseType());
        //     if (order != null) {
        //         gameMaster.addOrder(order);
        //         ordersListView.getItems().add(formatOrderText());
        //         drawArrowIfNeeded();
        //     }
        // } else {
        //     System.out.println("Ошибка: " + result);
        // }
        // clearSelection();
    }

    private void clearSelection() {
        if (selectedUnit != null) {
            mapView.highlightSelectedUnit(selectedUnit, false);
        }
        selectedUnit = null;
        currentOrderType = null;
        waitingForSupportTarget = false;
        currentPrototype.Clear();
        mapView.clearHighlights();
    }

    private void clearPrototypeExceptUnit() {
        mapView.clearHighlights();
        currentPrototype.Clear();
    }

    private void highlightPossibleMoves() {
        // for (var loc : gameMaster.getValidMoves(selectedUnit)) {
        //     mapView.highlightProvince(loc.getParentProvince().getId(), Color.LIGHTGREEN);
        // }
    }

    private void highlightSupportCandidates() {
        for (var loc : selectedUnit.getLocation().getNeighbours()) {
            Province p = loc.getParentProvince();
            if (p.isOccupied()) {
                mapView.highlightProvince(p.getId(), Color.SKYBLUE);
            }
        }
    }

    private void highlightSupportDestinations(Unit supported) {
        // for (var loc : gameMaster.getValidMoves(supported)) {
        //     mapView.highlightProvince(loc.getParentProvince().getId(), Color.LIGHTBLUE);
        // }
        // mapView.highlightProvince(supported.getLocation().getParentProvince().getId(), Color.LIGHTBLUE);
    }

    private void drawArrowIfNeeded() {
        if (selectedUnit != null && currentPrototype.getDestination() != null) {
            String from = selectedUnit.getLocation().getParentProvince().getId();
            String to = currentPrototype.getDestination().getId();
            mapView.drawArrow(from, to);
        }
    }

    private String formatOrderText() {
        Unit u = selectedUnit;
        String base = u.getTypeName() + " в " + u.getLocation().getParentProvince().getName();
        OrderType type = currentPrototype.getOrderType();
        return switch (type) {
            case HOLD -> base + " держит позицию";
            case MOVE -> base + " → " + currentPrototype.getDestination().getName();
            case SUPPORT -> {
                String sup = currentPrototype.getAdditionalUnit().getTypeName() + " в " +
                             currentPrototype.getAdditionalUnit().getLocation().getParentProvince().getName();
                String dest = currentPrototype.getDestination() != null ?
                              currentPrototype.getDestination().getName() : "удержание";
                yield base + " поддерживает " + sup + " → " + dest;
            }
            default -> base + " " + type;
        };
    }

    private void highlightPossibleConvoys() {
        // TODO: реализовать подсветку возможных конвоев
        System.out.println("Highlight convoy not implemented yet");
    }
}