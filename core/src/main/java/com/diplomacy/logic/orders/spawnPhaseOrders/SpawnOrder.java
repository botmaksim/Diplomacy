package com.diplomacy.logic.orders.spawnPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.SpawnPhaseOrder;
import com.diplomacy.logic.player.Player;

public class SpawnOrder extends SpawnPhaseOrder {

    private final Location destination;
    private final Player player;

    public SpawnOrder(Location destination, Player player) {
        this.destination = destination;
        this.player = player;
    }

    public Location getDestination() {
        return destination;
    }

    public Player getPlayer() {
        return player;
    }

}
