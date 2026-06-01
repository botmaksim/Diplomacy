package com.diplomacy.desktop.controllers;

import java.util.List;

import com.diplomacy.logic.player.Password;
import com.diplomacy.logic.player.Player;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class PlayerLoginDialogController {

    @FXML private ComboBox<String> playerCombo;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private List<Player> players;
    private Player authenticatedPlayer;
    private Stage stage;

    public void initData(List<Player> players) {
        this.players = players;
        for (Player p : players) {
            playerCombo.getItems().add(p.getName());
        }
        if (!players.isEmpty()) {
            playerCombo.getSelectionModel().select(0);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Player getAuthenticatedPlayer() {
        return authenticatedPlayer;
    }

    @FXML
    private void onOk() {
        errorLabel.setText("");
        int idx = playerCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= players.size()) {
            errorLabel.setText("No player selected.");
            return;
        }
        Player selected = players.get(idx);
        String passText = passwordField.getText();
        if (passText == null || passText.isEmpty()) {
            errorLabel.setText("Password is required.");
            return;
        }
        Password attempt = new Password(passText);
        if (!selected.tryLogin(attempt)) {
            errorLabel.setText("Incorrect password.");
            return;
        }
        authenticatedPlayer = selected;
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void onCancel() {
        if (stage != null) {
            stage.close();
        }
    }
}
