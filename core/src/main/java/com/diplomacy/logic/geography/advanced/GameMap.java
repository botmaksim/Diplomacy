package com.diplomacy.logic.geography.advanced;

import java.util.List;

import com.diplomacy.logic.geography.basic.Province;

public class GameMap extends Region {

    private final List<Country> countries;

    public GameMap(List<Province> provinces, List<Country> countries) {
        super(provinces);
        this.countries = countries;
    }

    public Country getCountry(String name) {
        for (Country c : countries) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    public Province getProvince(String id) {
        for (Province p : getProvinces()) {
            if (p.getId().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    public List<Country> getCountries() {
        return countries;
    }

    public void resetBattleFlags() {
        for (Province current : getProvinces()) {
            current.setBattled(false);
        }
    }
}
