package com.diplomacy.logic.player;

public class Password {

    private final String key;

    public Password(String key) {
        this.key = key;
    }

    public boolean check(Password attempt) {
        return key.equals(attempt.getKey());
    }

    public String getKey() {
        return key;
    }
}
