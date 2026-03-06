package com.diplomacy.orders;

public abstract class Order {

    private boolean executable = true;

    public Order() {
    }

    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }
}
