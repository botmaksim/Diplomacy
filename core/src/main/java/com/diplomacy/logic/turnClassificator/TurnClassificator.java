package com.diplomacy.logic.turnClassificator;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.diplomacy.logic.gameControllingUnits.phase.MovementPhase;
import com.diplomacy.logic.gameControllingUnits.phase.Phase;
import com.diplomacy.logic.gameControllingUnits.phase.RetreatPhase;
import com.diplomacy.logic.gameControllingUnits.phase.SpawnPhase;

public class TurnClassificator {

    private int year;
    private Season season;
    private PhaseType phase;

    public TurnClassificator() {
        this.year = 1900;
        this.season = Season.SPRING;
        this.phase = PhaseType.MOVEMENT;
    }

    public TurnClassificator(int year, Season season, PhaseType phase) {
        this.year = year;
        this.season = season;
        this.phase = phase;
    }

    public void setPhase(PhaseType phase) {
        this.phase = phase;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public PhaseType getPhase() {
        return phase;
    }

    public Season getSeason() {
        return season;
    }

    public int getYear() {
        return year;
    }

    public void nextTurn() {
        switch (phase) {
            case MOVEMENT -> phase = PhaseType.RETREAT;
            case RETREAT -> {
                if (season == Season.SPRING) {
                    season = Season.FALL;
                    phase = PhaseType.MOVEMENT;
                } else {
                    phase = PhaseType.SPAWN;
                }
            }
            case SPAWN -> {
                season = Season.SPRING;
                year++;
                phase = PhaseType.MOVEMENT;
            }
        }
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
        return year == turn.getYear() && phase == turn.getPhase() && season == turn.getSeason();
    }

    public boolean less(TurnClassificator turn) {
        if (year != turn.getYear()) {
            return year < turn.getYear();
        }
        if (season != turn.getSeason()) {
            return season == Season.SPRING;
        }
        return phase.ordinal() < turn.getPhase().ordinal();
    }
}
