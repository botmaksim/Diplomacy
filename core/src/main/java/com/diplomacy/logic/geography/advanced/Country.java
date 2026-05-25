package com.diplomacy.logic.geography.advanced;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.diplomacy.logic.geography.basic.Province;

public class Country extends Region {

    private final String name;
    private final Map<Province, String> startingUnitTypes;

    public Country(String name, List<Province> provinces, Map<Province, String> startingUnitTypes) {
        super(provinces);
        this.name = name;
        this.startingUnitTypes = Collections.unmodifiableMap(startingUnitTypes);
    }

    public void resetBattleFlags() {
        for (Province current : getProvinces()) {
            current.setBattled(false);
        }
    }

    public String getName() {
        return name;
    }

    public Map<Province, String> getStartingUnitTypes() {
        return startingUnitTypes;
    }
}
