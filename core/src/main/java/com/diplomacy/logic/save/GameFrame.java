package com.diplomacy.logic.save;

import java.util.List;

import com.diplomacy.logic.geography.advanced.Map;
import com.diplomacy.logic.player.Player;

public class GameFrame {

    public final List<Player> players;
    public final Map map;

    public GameFrame(List<Player> players, Map map) {
        this.players = players;
        this.map = map;
    }

    public Map getMap() {
        return map;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
