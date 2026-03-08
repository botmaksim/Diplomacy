package com.diplomacy.logic.geography.basic;

import java.util.ArrayList;
import java.util.List;

import com.diplomacy.logic.geography.utils.ProvinceSignature;
import com.diplomacy.logic.geography.utils.ProvinceType;
import com.diplomacy.logic.geography.utils.ResolveParameters;
import com.diplomacy.logic.units.Unit;

public class Province {

    private final ProvinceSignature signature;
    private final List<Location> locations;
    private final ResolveParameters resolveParameters;
    private Unit occupyingUnit = null;

    public Province(ProvinceSignature signature, List<Location> locations) {
        this(signature, locations, new ResolveParameters(), null);
    }

    public Province(ProvinceSignature signature, List<Location> locations, ResolveParameters resolveParameters) {
        this(signature, locations, resolveParameters, null);
    }

    public Province(ProvinceSignature signature, List<Location> locations, Unit occupyingUnit) {
        this(signature, locations, new ResolveParameters(), occupyingUnit);
    }

    public Province(ProvinceSignature signature, List<Location> locations, ResolveParameters resolveParameters, Unit occupyingUnit) {
        this.signature = signature;
        this.locations = (locations != null) ? locations : new ArrayList<>();
        this.resolveParameters = resolveParameters;
        this.occupyingUnit = occupyingUnit;
    }

    public String getName() {
        return signature.getName();
    }

    public ProvinceType getType() {
        return signature.getType();
    }

    public boolean isSupplyCenter() {
        return signature.isSupplyCenter();
    }

    public List<Location> getLocations() {
        return List.copyOf(locations);
    }

    public Unit getOccupyingUnit() {
        return occupyingUnit;
    }

    public void setOccupyingUnit(Unit occupyingUnit) {
        this.occupyingUnit = occupyingUnit;
    }

    public void release() {
        this.occupyingUnit = null;
    }

    public boolean isOccupied() {
        return occupyingUnit != null;
    }

    public ProvinceSignature getSignature() {
        return signature;
    }

    public ResolveParameters getResolveParameters() {
        return resolveParameters;
    }

    public boolean isBattled() {
        return resolveParameters.isBattled();
    }

    public void setBattled(boolean battled) {
        resolveParameters.setBattled(battled);
    }
}
