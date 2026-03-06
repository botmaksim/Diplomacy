package com.diplomacy.orders.utils;

import com.diplomacy.geography.basic.Location;
import com.diplomacy.geography.basic.Province;
import com.diplomacy.player.Player;
import com.diplomacy.units.Unit;

public class OrderPrototype {

    private OrderType orderType;
    private Location selectedLocation;
    private Province destination;
    private Unit ArmyToConvoy;
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

    public void setArmyToConvoy(Unit ArmyToConvoy) {
        this.ArmyToConvoy = ArmyToConvoy;
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

    public Unit getArmyToConvoy() {
        return ArmyToConvoy;
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

    public boolean isSetArmyToConvoy() {
        return ArmyToConvoy == null;
    }

    public void Clear() {
        selectedLocation = null;
        orderType = null;
        destination = null;
        player = null;
    }
}
