package com.diplomacy.orders.spawnPhaseOrders;

import com.diplomacy.geography.basic.Location;
import com.diplomacy.orders.SpawnPhaseOrder;
import com.diplomacy.player.Player;

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
