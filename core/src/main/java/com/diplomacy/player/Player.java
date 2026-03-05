package com.diplomacy.player;

import java.util.ArrayList;
import java.util.List;

import com.diplomacy.geography.advanced.Country;
import com.diplomacy.geography.basic.Province;
import com.diplomacy.units.Unit;

public class Player {

    private final Country country;

    private final Password password;

    private List<Unit> units;

    private List<Province> supplyCenters;

    private boolean ordersSubmitted;

    public Player(Country country, Password password) {

        this.country = country;
        this.password = password;

        this.units = getInitialUnits(country);

        this.supplyCenters = country.getProvinces();

    }

    public Player(Country country, Password password, List<Unit> units, List<Province> supplyCenters) {

        this.country = country;

        this.password = password;

        this.units = units;

        this.supplyCenters = supplyCenters;

    }

    public Country getCountry() {

        return country;

    }

    public List<Unit> getUnits() {

        return units;

    }

    public void setUnits(List<Unit> units) {

        this.units = units;

    }

    public List<Province> getSupplyCenters() {

        return supplyCenters;

    }

    public void setSupplyCenters(List<Province> supplyCenters) {

        this.supplyCenters = supplyCenters;

    }

    public boolean isOrdersSubmitted() {

        return ordersSubmitted;

    }

    public void setOrdersSubmitted(boolean flag) {

        this.ordersSubmitted = flag;

    }

    public boolean tryLogin(Password p) {

        return p.check(password);

    }

    private List<Unit> getInitialUnits(Country c) {

        List<Unit> u = new ArrayList<>();

        for (Province p : c.getProvinces()) {

            if (p.isOccupied()) {
                u.add(p.getOccupyingUnit());
            }

        }

        return u;

    }

}
