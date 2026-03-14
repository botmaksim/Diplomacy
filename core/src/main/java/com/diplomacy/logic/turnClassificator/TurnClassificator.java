package com.diplomacy.logic.turnClassificator;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.diplomacy.logic.gameControllingUnits.phase.MovementPhase;
import com.diplomacy.logic.gameControllingUnits.phase.Phase;
import com.diplomacy.logic.gameControllingUnits.phase.RetreatPhase;
import com.diplomacy.logic.gameControllingUnits.phase.SpawnPhase;

public class TurnClassificator {

    private int turnNumber;
    private Season season;
    private PhaseType phase;

    public TurnClassificator(int turnNumber, Season season, PhaseType phase) {
        this.turnNumber = turnNumber;
        this.season = season;
        this.phase = phase;
    }

    public void setPhase(PhaseType phase) {
        this.phase = phase;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public PhaseType getPhase() {
        return phase;
    }

    public Season getSeason() {
        return season;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void nextTurn() {

    }

    public Phase getPhaseClass() {
        return switch (phase) {
            case MOVEMENT ->
                new MovementPhase();
            case RETREAT ->
                new RetreatPhase();
            case SPAWN ->
                new SpawnPhase();
            default ->
                null;

        };
    }

    public boolean equals(TurnClassificator turn) {
        return turnNumber == turn.getTurnNumber() && phase == turn.getPhase() && season == turn.getSeason();
    }

    public boolean less(TurnClassificator turn) {
        if (turnNumber != turn.getTurnNumber()) {
            return turnNumber < turn.getTurnNumber();
        }
        if (season != turn.getSeason()) {
            return season == Season.SPRING;
        }
        for (PhaseType pt : Stream.of(PhaseType.values()).collect(Collectors.toList())) {
            if (turn.getPhase() == pt) {
                return false;
            }
            if (phase == pt) {
                return true;
            }
        }
        return false;
    }

}
