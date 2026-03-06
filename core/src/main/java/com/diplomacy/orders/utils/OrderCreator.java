package com.diplomacy.orders.utils;

import java.util.List;

import com.diplomacy.geography.basic.Location;
import com.diplomacy.geography.basic.Province;
import com.diplomacy.geography.utils.ProvinceType;
import com.diplomacy.orders.MovementPhaseOrder;
import com.diplomacy.orders.Order;
import com.diplomacy.orders.RetreatPhaseOrder;
import com.diplomacy.orders.SpawnPhaseOrder;
import com.diplomacy.orders.movementPhaseOrders.BeConvoyedOrder;
import com.diplomacy.orders.movementPhaseOrders.ConvoyOrder;
import com.diplomacy.orders.movementPhaseOrders.HoldOrder;
import com.diplomacy.orders.movementPhaseOrders.MoveOrder;
import com.diplomacy.orders.movementPhaseOrders.SupportOrder;
import com.diplomacy.orders.retreatPhaseOrders.DieOrder;
import com.diplomacy.orders.retreatPhaseOrders.RetreatOrder;
import com.diplomacy.orders.spawnPhaseOrders.DismissOrder;
import com.diplomacy.orders.spawnPhaseOrders.SpawnOrder;
import com.diplomacy.player.Player;
import com.diplomacy.units.Army;
import com.diplomacy.units.Fleet;
import com.diplomacy.units.Unit;
import com.diplomacy.utils.PhaseType;

public class OrderCreator {

    public verificationResult verifyOrder(OrderPrototype prototype, PhaseType phaseType) {
        if (prototype == null) {
            return verificationResult.DESTINATION_NOT_SET;
        }

        return switch (phaseType) {
            case MOVEMENT ->
                checkMovementPhase(prototype);
            case RETREAT ->
                checkRetreatPhase(prototype);
            case SPAWN ->
                checkSpawnPhase(prototype);
            default ->
                verificationResult.WRONG_PHASE;
        };
    }

    public Order createOrder(OrderPrototype prototype, PhaseType phaseType) {
        if (verifyOrder(prototype, phaseType) != verificationResult.OK) {
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

    private verificationResult checkMovementPhase(OrderPrototype prototype) {
        verificationResult ownership = validateOwnership(prototype);
        if (ownership != verificationResult.OK) {
            return ownership;
        }

        return switch (prototype.getOrderType()) {
            case HOLD ->
                verificationResult.OK;
            case MOVE ->
                checkMove(prototype);
            case SUPPORT ->
                checkSupport(prototype);
            case CONVOY ->
                checkConvoy(prototype);
            case BECONVOYED ->
                checkBeConvoyed(prototype);
            default ->
                verificationResult.WRONG_ORDER_TYPE;
        };
    }

    private verificationResult checkRetreatPhase(OrderPrototype prototype) {
        verificationResult ownership = validateOwnership(prototype);
        if (ownership != verificationResult.OK) {
            return ownership;
        }

        return switch (prototype.getOrderType()) {
            case RETREAT ->
                checkRetreat(prototype);
            case DIE ->
                verificationResult.OK;
            default ->
                verificationResult.WRONG_ORDER_TYPE;
        };
    }

    private verificationResult checkSpawnPhase(OrderPrototype prototype) {
        return switch (prototype.getOrderType()) {
            case SPAWN ->
                checkSpawn(prototype);
            case DISMISS ->
                validateOwnership(prototype);
            default ->
                verificationResult.WRONG_ORDER_TYPE;
        };
    }

    private verificationResult checkMove(OrderPrototype prototype) {
        if (!prototype.isSetDestination()) {
            return verificationResult.DESTINATION_NOT_SET;
        }

        Unit unit = prototype.getSelectedLocation().getParentProvince().getOccupyingUnit();

        if (getProvinceLocation(unit.getLocation().getNeighbours(), prototype.getDestination()) != null) {
            return verificationResult.OK;
        }

        if (unit instanceof Army army) {
            if (getProvinceLocation(army.getListOfReachableByConvoyLocations(), prototype.getDestination()) != null) {
                return verificationResult.OK;
            }
        }

        return verificationResult.DESTINATION_NOT_REACHABLE;
    }

    private verificationResult checkSupport(OrderPrototype prototype) {
        if (!prototype.isSetDestination()) {
            return verificationResult.DESTINATION_NOT_SET;
        }
        if (prototype.getAdditionalUnit() == null) {
            return verificationResult.ADDITIONAL_UNIT_NOT_SET;
        }

        Unit executor = prototype.getSelectedLocation().getParentProvince().getOccupyingUnit();
        if (executor.equals(prototype.getAdditionalUnit())) {
            return verificationResult.CANNOT_SUPPORT_SELF;
        }

        if (getProvinceLocation(executor.getLocation().getNeighbours(), prototype.getDestination()) == null) {
            return verificationResult.CANNOT_SUPPORT_HERE;
        }

        return verificationResult.OK;
    }

    private verificationResult checkConvoy(OrderPrototype prototype) {
        Unit executor = prototype.getSelectedLocation().getParentProvince().getOccupyingUnit();

        if (!(executor instanceof Fleet)) {
            return verificationResult.MUST_BE_FLEET;
        }
        if (executor.getLocation().getParentProvince().getType() != ProvinceType.WATER) {
            return verificationResult.FLEET_MUST_BE_IN_WATER;
        }
        if (prototype.getAdditionalUnit() == null) {
            return verificationResult.ADDITIONAL_UNIT_NOT_SET;
        }
        if (!(prototype.getAdditionalUnit() instanceof Army)) {
            return verificationResult.MUST_BE_ARMY;
        }

        return verificationResult.OK;
    }

    private verificationResult checkBeConvoyed(OrderPrototype prototype) {
        Unit executor = prototype.getSelectedLocation().getParentProvince().getOccupyingUnit();
        if (!(executor instanceof Army army)) {
            return verificationResult.MUST_BE_ARMY;
        }
        if (!prototype.isSetDestination()) {
            return verificationResult.DESTINATION_NOT_SET;
        }

        if (getProvinceLocation(army.getListOfReachableByConvoyLocations(), prototype.getDestination()) == null) {
            return verificationResult.DESTINATION_NOT_REACHABLE;
        }
        return verificationResult.OK;
    }

    private verificationResult checkRetreat(OrderPrototype prototype) {
        if (!prototype.isSetDestination()) {
            return verificationResult.DESTINATION_NOT_SET;
        }
        Unit unit = prototype.getSelectedLocation().getParentProvince().getOccupyingUnit();

        if (getProvinceLocation(unit.getLocation().getNeighbours(), prototype.getDestination()) == null) {
            return verificationResult.DESTINATION_NOT_REACHABLE;
        }
        return verificationResult.OK;
    }

    private verificationResult checkSpawn(OrderPrototype prototype) {
        if (!spawnAbleCheck(prototype.getPlayer(), prototype.getSelectedLocation())) {
            return verificationResult.UNABLE_TO_SPAWN;
        }
        if (prototype.getSelectedLocation().getParentProvince().isOccupied()) {
            return verificationResult.PROVINCE_ALREADY_OCCUPIED;
        }
        if (!prototype.getSelectedLocation().getParentProvince().isSupplyCenter()) {
            return verificationResult.NOT_A_SUPPLY_CENTER;
        }
        return verificationResult.OK;
    }

    private MovementPhaseOrder createMovementPhase(OrderPrototype prototype) {
        return switch (prototype.getOrderType()) {
            case HOLD ->
                new HoldOrder(prototype.getSelectedLocation().getParentProvince().getOccupyingUnit());
            case MOVE ->
                new MoveOrder(getProvinceLocation(prototype.getSelectedLocation().getNeighbours(), prototype.getDestination()), prototype.getSelectedLocation().getParentProvince().getOccupyingUnit());
            case SUPPORT ->
                new SupportOrder(prototype.getAdditionalUnit(), getProvinceLocation(prototype.getAdditionalUnit().getLocation().getNeighbours(), prototype.getDestination()), prototype.getSelectedLocation().getParentProvince().getOccupyingUnit());
            case CONVOY ->
                new ConvoyOrder(prototype.getAdditionalUnit(), getProvinceLocation(((Army) prototype.getAdditionalUnit()).getListOfReachableByConvoyLocations(), prototype.getDestination()), prototype.getSelectedLocation().getParentProvince().getOccupyingUnit());
            case BECONVOYED ->
                new BeConvoyedOrder(getProvinceLocation(((Army) prototype.getSelectedLocation().getParentProvince().getOccupyingUnit()).getListOfReachableByConvoyLocations(), prototype.getDestination()), prototype.getSelectedLocation().getParentProvince().getOccupyingUnit());
            default ->
                null;
        };
    }

    private RetreatPhaseOrder createRetreatPhase(OrderPrototype prototype) {
        return switch (prototype.getOrderType()) {
            case RETREAT ->
                new RetreatOrder(getProvinceLocation(prototype.getSelectedLocation().getNeighbours(), prototype.getDestination()), prototype.getSelectedLocation().getParentProvince().getOccupyingUnit());
            case DIE ->
                new DieOrder(prototype.getSelectedLocation().getParentProvince().getOccupyingUnit());
            default ->
                null;
        };
    }

    private SpawnPhaseOrder createSpawnPhase(OrderPrototype prototype) {
        return switch (prototype.getOrderType()) {
            case SPAWN ->
                new SpawnOrder(prototype.getSelectedLocation(), prototype.getPlayer());
            case DISMISS ->
                new DismissOrder(prototype.getSelectedLocation().getParentProvince().getOccupyingUnit());
            default ->
                null;
        };
    }

    private verificationResult validateOwnership(OrderPrototype prototype) {
        Province province = prototype.getSelectedLocation().getParentProvince();
        if (!province.isOccupied()) {
            return verificationResult.SELECTED_PROVINCE_NOT_OCCUPIED;
        }

        Unit unit = province.getOccupyingUnit();
        if (!prototype.getPlayer().getUnits().contains(unit)) {
            return verificationResult.UNIT_NOT_OWNED_BY_PLAYER;
        }
        return verificationResult.OK;
    }

    private boolean spawnAbleCheck(Player player, Location location) {
        return player.getSupplyCenters().contains(location.getParentProvince())
                && player.getCountry().getProvinces().contains(location.getParentProvince());
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
