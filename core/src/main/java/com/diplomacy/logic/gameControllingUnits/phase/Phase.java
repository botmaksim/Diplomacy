package com.diplomacy.logic.gameControllingUnits.phase;

import java.util.List;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.orders.Order;

public interface Phase {

    public void setGameMaster(GameMaster gameMaster);

    public boolean addOrder(Order order);

    public boolean removeOrder(int i);

    public boolean removeLastOrder();

    public List<? extends Order> getOrders();

    public void operate();

    public boolean ableNextPhase();
}
