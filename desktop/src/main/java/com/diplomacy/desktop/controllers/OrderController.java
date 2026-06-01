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

    private OrderType currentOrderType;
    private Unit selectedUnit;
    private Unit supportTargetUnit;   // для поддержки: кого поддерживаем
    private Province supportDestination; // цель атаки/удержания поддерживаемого
    private OrderPrototype currentPrototype;

    // Состояния конечного автомата
    private enum State { IDLE, UNIT_SELECTED, SUPPORT_TARGET_SELECTED }
    private State state = State.IDLE;

    public OrderController(GameMaster gm, MapView mv, ListView<String> orders) {
        this.gameMaster = gm;
        this.mapView = mv;
        this.ordersListView = orders;
        this.orderCreator = new OrderCreator();
    }

    /**
     * Вызывается при нажатии кнопки типа приказа.
     * Сбрасывает всё предыдущее состояние и устанавливает новый тип.
     */
    public void setOrderType(OrderType type) {
        reset();
        currentOrderType = type;
        state = State.IDLE;
        System.out.println("[OrderController] Order type set to: " + type);
    }

    /**
     * Обработчик клика по провинции.
     * В зависимости от состояния и типа приказа выполняет соответствующий шаг.
     */
    public void processClick(Province clicked) {
        if (currentOrderType == null) {
            System.out.println("[OrderController] No order type selected. Click ignored.");
            return;
        }

        switch (currentOrderType) {
            case HOLD -> handleHold(clicked);
            case MOVE -> handleMove(clicked);
            case SUPPORT -> handleSupport(clicked);
            default -> {
                System.out.println("[OrderController] Unsupported order type: " + currentOrderType);
                reset();
            }
        }
    }

    // ---------- Обработчики типов приказов ----------

    private void handleHold(Province clicked) {
        if (state == State.IDLE) {
            Unit unit = getOwnUnit(clicked);
            if (unit == null) return;
            selectedUnit = unit;
            state = State.UNIT_SELECTED;
            System.out.println("[HOLD] Unit selected: " + unit.getTypeName() + " in " + clicked.getName());
            mapView.highlightSelectedUnit(unit, true);
            // Сразу финализируем
            finalizeOrder();
        } else {
            // Повторный клик при выбранном юните – игнорируем
            System.out.println("[HOLD] Unit already selected, ignoring click.");
        }
    }

    private void handleMove(Province clicked) {
        switch (state) {
            case IDLE -> {
                Unit unit = getOwnUnit(clicked);
                if (unit == null) return;
                selectedUnit = unit;
                state = State.UNIT_SELECTED;
                System.out.println("[MOVE] Unit selected: " + unit.getTypeName() + " in " + clicked.getName());
                mapView.highlightSelectedUnit(unit, true);
                // Подсветим возможные цели (соседние провинции)
                highlightPossibleMoves(unit);
            }
            case UNIT_SELECTED -> {
                if (clicked == null || selectedUnit == null) { reset(); return; }
                // Проверка, что цель не та же провинция (можно добавить другие проверки)
                if (clicked.equals(selectedUnit.getLocation().getParentProvince())) {
                    System.out.println("[MOVE] Cannot move to same province.");
                    return;
                }
                // Установим destination в прототип и завершим приказ
                currentPrototype = new OrderPrototype();
                currentPrototype.setOrderType(OrderType.MOVE);
                currentPrototype.setSelectedLocation(selectedUnit.getLocation());
                currentPrototype.setPlayer(selectedUnit.getOwner());
                currentPrototype.setDestination(clicked);
                finalizeOrder();
            }
            default -> reset();
        }
    }

    private void handleSupport(Province clicked) {
        switch (state) {
            case IDLE -> {
                Unit unit = getOwnUnit(clicked);
                if (unit == null) return;
                selectedUnit = unit;
                state = State.UNIT_SELECTED;
                System.out.println("[SUPPORT] Supporting unit selected: " + unit.getTypeName() + " in " + clicked.getName());
                mapView.highlightSelectedUnit(unit, true);
                // Подсвечиваем соседей, где есть юниты (потенциальные поддерживаемые)
                highlightSupportCandidates(unit);
            }
            case UNIT_SELECTED -> {
                // Выбор поддерживаемого юнита (может быть любым, не обязательно своим)
                Unit targetUnit = clicked == null ? null : clicked.getOccupyingUnit();
                if (targetUnit == null) {
                    System.out.println("[SUPPORT] No unit in " + (clicked != null ? clicked.getName() : "null") + ", cannot support.");
                    return;
                }
                if (targetUnit == selectedUnit) {
                    System.out.println("[SUPPORT] Cannot support yourself.");
                    return;
                }
                supportTargetUnit = targetUnit;
                state = State.SUPPORT_TARGET_SELECTED;
                System.out.println("[SUPPORT] Supported unit selected: " + targetUnit.getTypeName() + " in " + clicked.getName());
                mapView.highlightProvince(clicked.getId(), Color.CYAN);
                // Подсвечиваем возможные цели для поддержки (куда может пойти поддерживаемый)
                highlightSupportDestinations(targetUnit);
            }
            case SUPPORT_TARGET_SELECTED -> {
                // Выбор цели атаки/удержания поддерживаемого
                if (clicked == null) {
                    System.out.println("[SUPPORT] Destination is null, cancelling.");
                    reset();
                    return;
                }
                supportDestination = clicked;
                // Формируем прототип
                currentPrototype = new OrderPrototype();
                currentPrototype.setOrderType(OrderType.SUPPORT);
                currentPrototype.setSelectedLocation(selectedUnit.getLocation());
                currentPrototype.setPlayer(selectedUnit.getOwner());
                currentPrototype.setAdditionalUnit(supportTargetUnit);
                currentPrototype.setDestination(supportDestination);
                finalizeOrder();
            }
            default -> reset();
        }
    }

    // ---------- Вспомогательные методы ----------

    /** Возвращает юнита в провинции, если он принадлежит текущему игроку, иначе null. */
    private Unit getOwnUnit(Province province) {
        if (province == null) return null;
        Unit unit = province.getOccupyingUnit();
        if (unit == null) return null;
        // Для теста пока разрешаем любого, потом добавить проверку текущего игрока
        // if (!unit.getOwner().equals(gameMaster.getCurrentPlayer())) return null;
        return unit;
    }

    private void highlightPossibleMoves(Unit unit) {
        for (var loc : unit.getLocation().getNeighbours()) {
            Province p = loc.getParentProvince();
            mapView.highlightProvince(p.getId(), Color.LIGHTGREEN);
        }
    }

    private void highlightSupportCandidates(Unit unit) {
        for (var loc : unit.getLocation().getNeighbours()) {
            Province p = loc.getParentProvince();
            if (p.isOccupied()) {
                mapView.highlightProvince(p.getId(), Color.SKYBLUE);
            }
        }
    }

    private void highlightSupportDestinations(Unit supported) {
        for (var loc : supported.getLocation().getNeighbours()) {
            Province p = loc.getParentProvince();
            mapView.highlightProvince(p.getId(), Color.LIGHTGREEN);
        }
    }

    /** Финализирует приказ, создаёт его и отображает стрелки/текст. */
    private void finalizeOrder() {
        if (currentPrototype == null) {
            System.out.println("[OrderController] Prototype is null, cannot finalize.");
            reset();
            return;
        }
        PhaseType phaseType = (gameMaster.getPhase() != null) ? gameMaster.turn.getPhase() : PhaseType.MOVEMENT;
        Order order = orderCreator.createOrder(currentPrototype, phaseType);
        if (order != null) {
            System.out.println("[OrderController] Order created: " + order.getClass().getSimpleName());
            gameMaster.addOrder(order);
            ordersListView.getItems().add(formatOrderText());
            drawArrowsForOrder();
        } else {
            System.out.println("[OrderController] Order creation failed.");
        }
        reset();
    }

    /** Рисует стрелки в зависимости от типа приказа. */
    private void drawArrowsForOrder() {
        if (selectedUnit == null) return;
        String fromId = selectedUnit.getLocation().getParentProvince().getId();
        switch (currentOrderType) {
            case MOVE -> {
                String toId = currentPrototype.getDestination().getId();
                mapView.drawOrderArrow(fromId, toId, OrderType.MOVE);
            }
            case SUPPORT -> {
                // Стрелка поддержки (синяя пунктирная) от поддерживающего к поддерживаемому
                String supportedId = supportTargetUnit.getLocation().getParentProvince().getId();
                mapView.drawSupportArrow(fromId, supportedId);
                // Стрелка атаки (красная сплошная) от поддерживаемого к цели
                if (supportDestination != null) {
                    mapView.drawOrderArrow(supportedId, supportDestination.getId(), OrderType.MOVE);
                }
            }
            // HOLD и другие без стрелок
        }
    }

    private String formatOrderText() {
        Unit u = selectedUnit;
        String base = u.getTypeName() + " в " + u.getLocation().getParentProvince().getName();
        return switch (currentOrderType) {
            case HOLD -> base + " держит позицию";
            case MOVE -> base + " → " + currentPrototype.getDestination().getName();
            case SUPPORT -> {
                String sup = supportTargetUnit.getTypeName() + " в " +
                             supportTargetUnit.getLocation().getParentProvince().getName();
                String dest = supportDestination != null ?
                              supportDestination.getName() : "удержание";
                yield base + " поддерживает " + sup + " → " + dest;
            }
            default -> base + " " + currentOrderType;
        };
    }

    /** Полный сброс состояния и подсветки. */
    private void reset() {
        if (selectedUnit != null) mapView.highlightSelectedUnit(selectedUnit, false);
        selectedUnit = null;
        supportTargetUnit = null;
        supportDestination = null;
        currentPrototype = null;
        state = State.IDLE;
        mapView.clearHighlights();
    }
}