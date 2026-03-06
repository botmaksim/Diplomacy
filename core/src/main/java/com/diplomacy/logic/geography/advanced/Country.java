package com.diplomacy.geography.advanced;

import java.util.List;

import com.diplomacy.geography.basic.Province;

public class Country extends Region {

    private final String name;

    public Country(String name, List<Province> provinces) {
        super(provinces);
        this.name = name;
    }

    public void resetBattleFlags() {
        for (Province current : getProvinces()) {
            current.setBattled(false);
        }
    }

    public String getName() {
        return name;
    }
}
