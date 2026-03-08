package com.diplomacy.logic.geography.advanced;

import java.util.List;

import com.diplomacy.logic.geography.basic.Province;

public class Region {

    private final List<Province> provinces;

    public Region(List<Province> provinces) {
        this.provinces = provinces;
    }

    public List<Province> getProvinces() {
        return provinces;
    }
}
