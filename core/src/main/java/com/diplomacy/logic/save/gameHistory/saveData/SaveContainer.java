package com.diplomacy.logic.save.gameHistory.saveData;

import java.io.Serializable;

public interface SaveContainer extends Serializable {

    public SaveType getType();
}
