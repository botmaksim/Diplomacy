package com.diplomacy.desktop.controllers;

import java.util.List;

import com.diplomacy.logic.geography.basic.Location;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CoastSelectionDialogController {

    @FXML private Label titleLabel;
    @FXML private VBox coastButtonsBox;
    @FXML private Button cancelButton;

    private Location selectedLocation;
    private Stage stage;

    public void initData(List<Location> coastLocations, String provinceName) {
        titleLabel.setText("Choose a coast for " + provinceName + ":");
        for (Location loc : coastLocations) {
            String coastName = formatCoastName(loc.getName());
            Button btn = new Button(coastName);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> selectCoast(loc));
            coastButtonsBox.getChildren().add(btn);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Location getSelectedLocation() {
        return selectedLocation;
    }

    @FXML
    private void onCancel() {
        if (stage != null) {
            stage.close();
        }
    }

    private void selectCoast(Location loc) {
        selectedLocation = loc;
        if (stage != null) {
            stage.close();
        }
    }

    private String formatCoastName(String name) {
        if (name == null || !name.startsWith("coast_")) {
            return name != null ? name : "Unknown";
        }
        String dir = name.substring(6);
        return dir.substring(0, 1).toUpperCase() + dir.substring(1) + " Coast";
    }
}
