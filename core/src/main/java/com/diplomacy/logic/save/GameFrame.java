package com.diplomacy.logic.save;

import java.util.List;

import com.diplomacy.logic.geography.advanced.GameMap;
import com.diplomacy.logic.player.Player;

public class GameFrame {

    public final List<Player> players;
    public final GameMap map;

    public GameFrame(List<Player> players, GameMap map) {
        this.players = players;
        this.map = map;
    }

    public GameMap getMap() {
        return map;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
