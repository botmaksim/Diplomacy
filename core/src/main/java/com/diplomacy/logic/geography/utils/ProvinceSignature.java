package com.diplomacy.logic.geography.utils;

public class ProvinceSignature {

    private final String name;
    private final boolean SupplyCenter;
    private final ProvinceType type;

    public ProvinceSignature(String name, boolean SupplyCenter, ProvinceType type) {
        this.name = name;
        this.SupplyCenter = SupplyCenter;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean isSupplyCenter() {
        return SupplyCenter;
    }

    public ProvinceType getType() {
        return type;
    }
}
