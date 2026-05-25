package com.diplomacy.desktop.controllers;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.utils.OrderCreator;
import com.diplomacy.logic.orders.utils.OrderPrototype;
import com.diplomacy.logic.orders.utils.OrderType;
import com.diplomacy.logic.turnClassificator.PhaseType;
import com.diplomacy.logic.units.Unit;

import javafx.scene.control.ListView;
import javafx.scene.paint.Color;

public class OrderController {

    private final GameMaster gameMaster;
    private final MapView mapView;
    private final ListView<String> ordersListView;
    private final OrderCreator orderCreator;

    private Unit selectedUnit;
    private OrderType currentOrderType;          // тип, выбранный кнопкой (не сбрасывается автоматически)
    private OrderPrototype currentPrototype;     // временный прототип для текущего приказа
    private boolean waitingForSupportTarget;     // для SUPPORT: ожидаем цель после выбора поддерживаемого юнита

    public OrderController(GameMaster gm, MapView mv, ListView<String> orders) {
        this.gameMaster = gm;
        this.mapView = mv;
        this.ordersListView = orders;
        this.orderCreator = new OrderCreator();
        this.currentPrototype = new OrderPrototype();
    }

    // Вызывается из UI при нажатии кнопки типа приказа (MOVE, SUPPORT и т.д.)
    // Устанавливает активный тип, сбрасывает ожидание и выделение юнита.
    public void setOrderType(OrderType type) {
        // Очищаем предыдущее состояние
        clearSelection();
        this.currentOrderType = type;
        waitingForSupportTarget = false;
        System.out.println("[OrderController] Order type set to: " + type);
    }

    // Сброс текущего приказа (очистка выбранного юнита и прототипа), но тип остается.
    private void clearCurrentOrder() {
        if (selectedUnit != null) {
            mapView.highlightSelectedUnit(selectedUnit, false);
        }
        selectedUnit = null;
        currentPrototype = new OrderPrototype();
        mapView.clearHighlights();
    }

    // Полный сброс (включая тип) – вызывается при смене режима или отмене.
    private void clearSelection() {
        clearCurrentOrder();
        currentOrderType = null;
        waitingForSupportTarget = false;
    }

    public void processClick(Province clicked) {
        System.out.println("[OrderController] processClick, currentOrderType=" + currentOrderType);

        // Если тип не выбран – ничего не делаем (или можно выделять юниты без приказа?)
        if (currentOrderType == null) {
            System.out.println("[OrderController] No order type selected. Click ignored.");
            return;
        }

        // Если ждём цель для поддержки – отдельная ветка
        if (waitingForSupportTarget) {
            completeSupportOrder(clicked);
            return;
        }

        // Если юнит ещё не выбран – пытаемся выбрать
        if (selectedUnit == null) {
            handleUnitSelection(clicked);
            return;
        }

        // Юнит выбран – обрабатываем цель в зависимости от типа
        handleOrderTarget(clicked);
    }

    private void handleUnitSelection(Province clicked) {
        if (clicked == null) {
            System.out.println("[OrderController] Clicked on empty area, cannot select unit.");
            return;
        }
        Unit u = clicked.getOccupyingUnit();
        if (u == null) {
            System.out.println("[OrderController] Province " + clicked.getName() + " is not occupied.");
            return;
        }
        // Проверка принадлежности юнита текущему игроку
        // if (!u.getOwner().equals(gameMaster.getCurrentPlayer())) {
        //     System.out.println("[OrderController] Unit belongs to " + u.getOwner().getCountry().getName() + ", not yours.");
        //     return;
        // }
        // Выбираем юнит
        clearCurrentOrder(); // сбрасываем предыдущий незавершённый приказ
        selectedUnit = u;
        currentPrototype.setSelectedLocation(selectedUnit.getLocation());
        currentPrototype.setPlayer(selectedUnit.getOwner());
        currentPrototype.setOrderType(currentOrderType);
        System.out.println("[OrderController] Unit selected: " + u.getTypeName() + " in " + clicked.getName());
        mapView.highlightSelectedUnit(u, true);

        // Для HOLD – сразу финализируем без выбора цели
        if (currentOrderType == OrderType.HOLD) {
            tryFinalizeOrder();
        }
        // Для MOVE, SUPPORT, CONVOY – ожидаем следующий клик (цель или поддержку)
    }

    private void handleOrderTarget(Province target) {
        if (target == null) {
            System.out.println("[OrderController] Target is null, cancelling order.");
            clearCurrentOrder();
            return;
        }

        switch (currentOrderType) {
            case MOVE -> {
                System.out.println("[OrderController] Move destination: " + target.getName());
                currentPrototype.setDestination(target);
                tryFinalizeOrder();
            }
            case SUPPORT -> {
                // Первый клик после выбора юнита – выбор поддерживаемого юнита
                Unit supported = target.getOccupyingUnit();
                if (supported == null) {
                    System.out.println("[OrderController] No unit in " + target.getName() + " to support.");
                    clearCurrentOrder();
                    return;
                }
                if (supported.getOwner().equals(selectedUnit.getOwner())) {
                    System.out.println("[OrderController] Cannot support own unit? (разрешить?)");
                }
                currentPrototype.setAdditionalUnit(supported);
                waitingForSupportTarget = true;
                System.out.println("[OrderController] Support target unit selected: " + supported.getTypeName() + " in " + target.getName());
                // Подсветка возможных направлений поддержки (куда может пойти поддерживаемый юнит)
                highlightSupportDestinations(supported);
            }
            case CONVOY -> {
                // Аналогично SUPPORT: нужен конвоируемый юнит и цель
                // Пока заглушка
                System.out.println("[OrderController] Convoy order not fully implemented.");
                clearCurrentOrder();
            }
            default -> {
                System.out.println("[OrderController] Unsupported order type for target handling: " + currentOrderType);
                clearCurrentOrder();
            }
        }
    }

    private void completeSupportOrder(Province destination) {
        if (destination == null) {
            System.out.println("[OrderController] Support destination is null, cancelling.");
            clearCurrentOrder();
            waitingForSupportTarget = false;
            return;
        }
        System.out.println("[OrderController] Support destination: " + destination.getName());
        currentPrototype.setDestination(destination);
        tryFinalizeOrder();
    }

    private void tryFinalizeOrder() {
        System.out.println("[OrderController] Trying to finalize order. Prototype: " + currentPrototype);
        PhaseType phaseType = (gameMaster.getPhase() != null) ? gameMaster.turn.getPhase() : PhaseType.MOVEMENT;
        System.out.println("[OrderController] Using phase: " + phaseType);

        Order order = orderCreator.createOrder(currentPrototype, phaseType);
        if (order != null) {
            System.out.println("[OrderController] Order created successfully: " + order.getClass().getSimpleName());
            gameMaster.addOrder(order);
            ordersListView.getItems().add(formatOrderText());
            drawArrowIfNeeded();
        } else {
            System.out.println("[OrderController] Failed to create order. Order was null.");
            System.out.println("Order prototype details: orderType=" + currentPrototype.getOrderType()
                + " location=" + (currentPrototype.getSelectedLocation() != null ? currentPrototype.getSelectedLocation().getParentProvince().getName() : "null")
                + " destination=" + (currentPrototype.getDestination() != null ? currentPrototype.getDestination().getName() : "null")
                + " player=" + (currentPrototype.getPlayer() != null ? currentPrototype.getPlayer().getCountry().getName() : "null")
                + " additionalUnit=" + (currentPrototype.getAdditionalUnit() != null ? "present" : "null"));
        }
        // После завершения приказа очищаем текущий (юнит и прототип), но тип остаётся
        clearCurrentOrder();
        waitingForSupportTarget = false;
    }

    // Методы подсветки (заглушки)
    private void highlightSupportDestinations(Unit supported) {
        // Подсвечиваем возможные цели для поддержки (куда может двигаться поддерживаемый юнит)
        for (var loc : supported.getLocation().getNeighbours()) {
            Province p = loc.getParentProvince();
            mapView.highlightProvince(p.getId(), Color.LIGHTGREEN);
        }
    }

    private void drawArrowIfNeeded() {
        if (selectedUnit != null && currentPrototype.getDestination() != null) {
            String from = selectedUnit.getLocation().getParentProvince().getId();
            String to = currentPrototype.getDestination().getId();
            System.out.println("[OrderController] Drawing arrow from " + from + " to " + to);
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

    // Методы-заглушки для подсветки (можно реализовать позже)
    private void highlightPossibleMoves() {}
    private void highlightSupportCandidates() {}
    private void highlightPossibleConvoys() {}
}