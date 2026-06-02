package com.diplomacy.logic.gameControllingUnits.phase;

import java.util.List;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.orders.Order;
import com.diplomacy.logic.player.Player;

public interface Phase {

    void setGameMaster(GameMaster gameMaster);

    boolean addOrder(Order order, Player player);

    boolean removeOrder(Player player, int index);

    List<? extends Order> getOrders(Player player);

    List<? extends Order> getAllOrders();

    void clearAllOrders();

    void operate();

    boolean ableNextPhase();
}
