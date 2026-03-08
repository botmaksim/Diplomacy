package com.diplomacy.logic.orders.utils;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.units.Unit;

public class OrderPrototype {

    private OrderType orderType;
    private Location selectedLocation;
    private Province destination;
    private Unit additionalUnit;
    private Player player;

    public OrderPrototype() {
    }

    public void setDestination(Province destination) {
        this.destination = destination;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public void setSelectedLocation(Location selectedLocation) {
        this.selectedLocation = selectedLocation;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setAdditionalUnit(Unit additionalUnit) {
        this.additionalUnit = additionalUnit;
    }

    public Location getSelectedLocation() {
        return selectedLocation;
    }

    public Province getDestination() {
        return destination;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public Player getPlayer() {
        return player;
    }

    public Unit getAdditionalUnit() {
        return additionalUnit;
    }

    public boolean isSetDestination() {
        return destination != null;
    }

    public boolean isSetSelectedLocation() {
        return selectedLocation != null;
    }

    public boolean isSetOrderType() {
        return orderType != null;
    }

    public boolean isSetPlayer() {
        return player != null;
    }

    public boolean isSetAdditionalUnit() {
        return additionalUnit == null;
    }

    public void Clear() {
        selectedLocation = null;
        orderType = null;
        destination = null;
        player = null;
    }
}
