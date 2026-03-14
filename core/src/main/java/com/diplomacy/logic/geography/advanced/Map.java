package com.diplomacy.logic.geography.advanced;

import java.util.List;

import com.diplomacy.logic.geography.basic.Province;

public abstract class Map extends Region {

    public Map(List<Province> provinces) {
        super(provinces);
    }

    public void resetBattleFlags() {
        for (Province current : getProvinces()) {
            current.setBattled(false);
        }
    }
}
