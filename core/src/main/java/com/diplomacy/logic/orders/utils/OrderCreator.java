package com.diplomacy.logic.orders.utils;

import java.util.List;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.geography.utils.ProvinceType;
import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.RetreatPhaseOrder;
import com.diplomacy.logic.orders.SpawnPhaseOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.BeConvoyedOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.ConvoyOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.HoldOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.MoveOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.SupportOrder;
import com.diplomacy.logic.orders.retreatPhaseOrders.DieOrder;
import com.diplomacy.logic.orders.retreatPhaseOrders.RetreatOrder;
import com.diplomacy.logic.orders.spawnPhaseOrders.DismissOrder;
import com.diplomacy.logic.orders.spawnPhaseOrders.SpawnOrder;
import com.diplomacy.logic.turnClassificator.PhaseType;
import com.diplomacy.logic.units.Army;
import com.diplomacy.logic.units.Fleet;
import com.diplomacy.logic.units.Unit;

public class OrderCreator {

    public VerificationResult verifyOrder(OrderPrototype prototype, PhaseType phaseType) {
        if (prototype == null) {
            return VerificationResult.INCORRECT_PROTOTYPE;
        }

        if (!prototype.isSetSelectedLocation()) {
            return VerificationResult.SELECTED_LOCATION_NOT_SET;
        }

        if (!prototype.isSetPlayer()) {
            return VerificationResult.PLAYER_NOT_SET;
        }

        return switch (phaseType) {
            case MOVEMENT ->
                checkMovementPhase(prototype);
            case RETREAT ->
                checkRetreatPhase(prototype);
            case SPAWN ->
                checkSpawnPhase(prototype);
            default ->
                VerificationResult.WRONG_PHASE;
        };
    }

    public Order createOrder(OrderPrototype prototype, PhaseType phaseType) {
        if (verifyOrder(prototype, phaseType) != VerificationResult.OK) {
            return null;
        }

        return switch (phaseType) {
            case MOVEMENT ->
                createMovementPhase(prototype);
            case RETREAT ->
                createRetreatPhase(prototype);
            case SPAWN ->
                createSpawnPhase(prototype);
            default ->
                null;
        };
    }

    private VerificationResult checkMovementPhase(OrderPrototype prototype) {
        VerificationResult ownership = validateOwnership(prototype);
        if (ownership != VerificationResult.OK) {
            return ownership;
        }

        return switch (prototype.getOrderType()) {
            case HOLD ->
                checkHold(prototype);
            case MOVE ->
                checkMove(prototype);
            case SUPPORT ->
                checkSupport(prototype);
            case CONVOY ->
                checkConvoy(prototype);
            case BECONVOYED ->
                checkBeConvoyed(prototype);
            default ->
                VerificationResult.WRONG_ORDER_TYPE;
        };
    }

    private VerificationResult checkRetreatPhase(OrderPrototype prototype) {
        VerificationResult ownership = validateOwnership(prototype);
        if (ownership != VerificationResult.OK) {
            return ownership;
        }

        return switch (prototype.getOrderType()) {
            case RETREAT ->
                checkRetreat(prototype);
            case DIE ->
                checkDie(prototype);
            default ->
                VerificationResult.WRONG_ORDER_TYPE;
        };
    }

    private VerificationResult checkSpawnPhase(OrderPrototype prototype) {
        return switch (prototype.getOrderType()) {
            case SPAWN ->
                checkSpawn(prototype);
            case DISMISS ->
                checkDismiss(prototype);
            default ->
                VerificationResult.WRONG_ORDER_TYPE;
        };
    }

    private VerificationResult checkHold(OrderPrototype prototype) {
        if (prototype.isSetAdditionalUnit() || prototype.isSetDestination()) {
            return VerificationResult.EXTRA_PARAMETERS;
        }
        return VerificationResult.OK;
    }

    private VerificationResult checkDie(OrderPrototype prototype) {
        if (!prototype.getSelectedLocation().getParentProvince().getOccupyingUnit().isRetreating()) {
            return VerificationResult.NOT_RETREATING_UNIT;
        }
        if (prototype.isSetAdditionalUnit() || prototype.isSetDestination()) {
            return VerificationResult.EXTRA_PARAMETERS;
        }
        return VerificationResult.OK;
    }

    private VerificationResult checkDismiss(OrderPrototype prototype) {
        VerificationResult check = validateOwnership(prototype);
        if (check != VerificationResult.OK) {
            return check;
        }

        if (prototype.isSetAdditionalUnit() || prototype.isSetDestination()) {
            return VerificationResult.EXTRA_PARAMETERS;
        }

        return VerificationResult.OK;
    }

    private VerificationResult checkMove(OrderPrototype prototype) {
        if (!prototype.isSetDestination()) {
            return VerificationResult.DESTINATION_NOT_SET;
        }

        if (prototype.isSetAdditionalUnit()) {
            return VerificationResult.EXTRA_PARAMETERS;
        }

        Unit unit = prototype.getSelectedLocation().getParentProvince().getOccupyingUnit();

        if (getProvinceLocation(unit.getLocation().getNeighbours(), prototype.getDestination()) == null) {
            return VerificationResult.DESTINATION_NOT_REACHABLE;
        }

        return VerificationResult.OK;

    }

    private VerificationResult checkSupport(OrderPrototype prototype) {
        if (!prototype.isSetDestination()) {
            return VerificationResult.DESTINATION_NOT_SET;
        }

        if (prototype.isSetAdditionalUnit()) {
            return VerificationResult.SUPPORTED_NOT_SET;
        }

        Unit executor = prototype.getSelectedLocation().getParentProvince().getOccupyingUnit();
        if (executor.equals(prototype.getAdditionalUnit())) {
            return VerificationResult.CANNOT_SUPPORT_SELF;
        }

        if (getProvinceLocation(executor.getLocation().getNeighbours(), prototype.getDestination()) == null) {
            return VerificationResult.CANNOT_SUPPORT_HERE;
        }

        if (getProvinceLocation(executor.getLocation().getNeighbours(), prototype.getDestination()) == null) {
            return VerificationResult.SUPPORTED_CANNOT_REACH_DESTINATION;
        }

        return VerificationResult.OK;
    }

    private VerificationResult checkConvoy(OrderPrototype prototype) {
        Unit executor = prototype.getSelectedLocation().getParentProvince().getOccupyingUnit();

        if (!(executor instanceof Fleet)) {
            return VerificationResult.MUST_BE_FLEET;
        }
        if (executor.getLocation().getParentProvince().getType() != ProvinceType.WATER) {
            return VerificationResult.FLEET_MUST_BE_IN_WATER;
        }
        if (prototype.getAdditionalUnit() == null) {
            return VerificationResult.CONVOYED_UNIT_NOT_SET;
        }
        if (!(prototype.getAdditionalUnit() instanceof Army)) {
            return VerificationResult.MUST_BE_ARMY;
        }

        if (prototype.getAdditionalUnit().getLocation().getParentProvince().getType() != ProvinceType.COASTAL) {
            return VerificationResult.ARMY_MUST_BE_COASTAL;
        }

        if (getProvinceLocation(((Army) prototype.getAdditionalUnit()).getListOfReachableByConvoyLocations(), prototype.getDestination()) == null) {
            return VerificationResult.ARMY_CANNOT_CONVOY;
        }

        if (!((Fleet) executor).canConvoy(prototype.getDestination(), ((Army) prototype.getAdditionalUnit()))) {
            return VerificationResult.CANNOT_CONVOY_VIA_SELCTED;
        }

        return VerificationResult.OK;
    }

    private VerificationResult checkBeConvoyed(OrderPrototype prototype) {
        Unit executor = prototype.getSelectedLocation().getParentProvince().getOccupyingUnit();
        if (!(executor instanceof Army army)) {
            return VerificationResult.MUST_BE_ARMY;
        }
        if (!prototype.isSetDestination()) {
            return VerificationResult.DESTINATION_NOT_SET;
        }
        if (getProvinceLocation(army.getListOfReachableByConvoyLocations(), prototype.getDestination()) == null) {
            return VerificationResult.DESTINATION_NOT_REACHABLE;
        }
        if (prototype.isSetAdditionalUnit()) {
            return VerificationResult.EXTRA_PARAMETERS;
        }
        return VerificationResult.OK;
    }

    private VerificationResult checkRetreat(OrderPrototype prototype) {
        if (!prototype.isSetDestination()) {
            return VerificationResult.DESTINATION_NOT_SET;
        }
        Unit unit = prototype.getSelectedLocation().getParentProvince().getOccupyingUnit();

        if (!unit.isRetreating()) {
            return VerificationResult.NOT_RETREATING_UNIT;
        }

        if (getProvinceLocation(unit.getLocation().getNeighbours(), prototype.getDestination()) == null) {
            return VerificationResult.DESTINATION_NOT_REACHABLE;
        }

        if (prototype.getDestination().isBattled() || prototype.getDestination().isOccupied()) {
            return VerificationResult.CANNOT_RETREAT_DESTINATION;
        }

        if (prototype.isSetAdditionalUnit()) {
            return VerificationResult.EXTRA_PARAMETERS;
        }

        return VerificationResult.OK;
    }

    private VerificationResult checkSpawn(OrderPrototype prototype) {
        if (prototype.getPlayer().getSupplyCenters().contains(prototype.getSelectedLocation().getParentProvince())) {
            return VerificationResult.PROVINCE_NOT_OWNED_BY_PLAYER;
        }

        if (!prototype.getPlayer().getCountry().getProvinces().contains(prototype.getSelectedLocation().getParentProvince())) {
            return VerificationResult.NOT_INITIAL_COUNTRY_PROVINCE;
        }

        if (prototype.isSetDestination() || prototype.isSetAdditionalUnit()) {
            return VerificationResult.EXTRA_PARAMETERS;
        }

        return VerificationResult.OK;
    }

    private MovementPhaseOrder createMovementPhase(OrderPrototype prototype) {
        return switch (prototype.getOrderType()) {
            case HOLD ->
                new HoldOrder(prototype.getSelectedLocation());
            case MOVE ->
                new MoveOrder(getProvinceLocation(prototype.getSelectedLocation().getNeighbours(), prototype.getDestination()), prototype.getSelectedLocation());
            case SUPPORT ->
                new SupportOrder(prototype.getAdditionalUnit().getLocation(), getProvinceLocation(prototype.getAdditionalUnit().getLocation().getNeighbours(), prototype.getDestination()), prototype.getSelectedLocation());
            case CONVOY ->
                new ConvoyOrder(prototype.getAdditionalUnit().getLocation(), getProvinceLocation(((Army) prototype.getAdditionalUnit()).getListOfReachableByConvoyLocations(), prototype.getDestination()), prototype.getSelectedLocation());
            case BECONVOYED ->
                new BeConvoyedOrder(getProvinceLocation(((Army) prototype.getSelectedLocation().getParentProvince().getOccupyingUnit()).getListOfReachableByConvoyLocations(), prototype.getDestination()), prototype.getSelectedLocation());
            default ->
                null;
        };
    }

    private RetreatPhaseOrder createRetreatPhase(OrderPrototype prototype) {
        return switch (prototype.getOrderType()) {
            case RETREAT ->
                new RetreatOrder(getProvinceLocation(prototype.getSelectedLocation().getNeighbours(), prototype.getDestination()), prototype.getSelectedLocation());
            case DIE ->
                new DieOrder(prototype.getSelectedLocation());
            default ->
                null;
        };
    }

    private SpawnPhaseOrder createSpawnPhase(OrderPrototype prototype) {
        return switch (prototype.getOrderType()) {
            case SPAWN ->
                new SpawnOrder(prototype.getSelectedLocation(), prototype.getPlayer());
            case DISMISS ->
                new DismissOrder(prototype.getSelectedLocation());
            default ->
                null;
        };
    }

    private VerificationResult validateOwnership(OrderPrototype prototype) {
        if (!prototype.getSelectedLocation().getParentProvince().isOccupied()) {
            return VerificationResult.SELECTED_LOCATION_NOT_OCCUPIED;
        }

        Unit unit = prototype.getSelectedLocation().getParentProvince().getOccupyingUnit();
        if (unit.getLocation() != prototype.getSelectedLocation()) {
            return VerificationResult.SELECTED_LOCATION_NOT_OCCUPIED;
        }

        if (!prototype.getPlayer().getUnits().contains(unit)) {
            return VerificationResult.UNIT_NOT_OWNED_BY_PLAYER;
        }

        return VerificationResult.OK;
    }

    private Location getProvinceLocation(List<Location> locations, Province province) {
        if (locations == null || province == null) {
            return null;
        }
        for (Location location : locations) {
            if (location.getParentProvince().equals(province)) {
                return location;
            }
        }
        return null;
    }
}
