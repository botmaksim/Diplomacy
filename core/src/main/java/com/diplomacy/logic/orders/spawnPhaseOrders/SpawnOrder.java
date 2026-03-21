package com.diplomacy.logic.orders.spawnPhaseOrders;

import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.orders.SpawnPhaseOrder;
import com.diplomacy.logic.player.Player;

public class SpawnOrder extends SpawnPhaseOrder {

    private final Player player;

    public SpawnOrder(Location target, Player player) {
        super(target);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean equals(Order other) {
        if (other instanceof SpawnOrder o) {
            return o.getTarget() == getTarget() && o.getPlayer() == getPlayer();
        }
        return false;
    }
}
