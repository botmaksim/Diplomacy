package com.diplomacy.logic.gameControllingUnits.phase;

import com.diplomacy.logic.orders.Order;

public interface Phase {

    public void addOrder(Order order);

    public void resolve(Order order);

    public boolean ableNextPhase();

    public void execute();
}
