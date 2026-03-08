package com.diplomacy.logic.geography.utils;

public class ResolveParameters {

    private boolean Battled = false;

    public ResolveParameters() {
    }

    public boolean isBattled() {
        return Battled;
    }

    public void setBattled(boolean Battled) {
        this.Battled = Battled;
    }

    public void clear() {
        Battled = false;
    }
}
