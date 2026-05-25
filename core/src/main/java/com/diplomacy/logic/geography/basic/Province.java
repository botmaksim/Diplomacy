package com.diplomacy.logic.geography.basic;

import java.util.ArrayList;
import java.util.List;

import com.diplomacy.logic.geography.utils.ProvinceSignature;
import com.diplomacy.logic.geography.utils.ProvinceType;
import com.diplomacy.logic.geography.utils.ResolveParameters;
import com.diplomacy.logic.units.Unit;

public class Province {

    private final String id; 
    private final ProvinceSignature signature;
    private final List<Location> locations;
    private final ResolveParameters resolveParameters;
    private Unit occupyingUnit = null;

    public Province(String id, ProvinceSignature signature, List<Location> locations) {
        this(id, signature, locations, new ResolveParameters(), null);
    }

    public Province(String id, ProvinceSignature signature, List<Location> locations, ResolveParameters resolveParameters) {
        this(id, signature, locations, resolveParameters, null);
    }

    public Province(String id, ProvinceSignature signature, List<Location> locations, Unit occupyingUnit) {
        this(id, signature, locations, new ResolveParameters(), occupyingUnit);
    }

    public Province(String id, ProvinceSignature signature, List<Location> locations, ResolveParameters resolveParameters, Unit occupyingUnit) {
        this.id = id;
        this.signature = signature;
        this.locations = (locations != null) ? locations : new ArrayList<>();
        this.resolveParameters = resolveParameters;
        this.occupyingUnit = occupyingUnit;
    }

    public String getId() {
        return id;
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

    public void addLocation(Location location) {
        if (location != null && !locations.contains(location)) {
            this.locations.add(location);
        }
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
