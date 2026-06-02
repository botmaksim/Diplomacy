package com.diplomacy.desktop.controllers;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.units.Army;
import com.diplomacy.logic.units.Fleet;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class PlayerSession {

    private Player currentPlayer;
    private final GameMaster gameMaster;
    private final OrderLedger orderLedger;
    private final Label currentPlayerLabel;
    private final Label playerInfoLabel;
    private final Button loginButton;
    private final Button confirmOrdersButton;
    private final ToggleGroup orderTypeGroup;

    public PlayerSession(GameMaster gameMaster, OrderLedger orderLedger,
                         Label currentPlayerLabel, Label playerInfoLabel,
                         Button loginButton, Button confirmOrdersButton,
                         ToggleGroup orderTypeGroup) {
        this.gameMaster = gameMaster;
        this.orderLedger = orderLedger;
        this.currentPlayerLabel = currentPlayerLabel;
        this.playerInfoLabel = playerInfoLabel;
        this.loginButton = loginButton;
        this.confirmOrdersButton = confirmOrdersButton;
        this.orderTypeGroup = orderTypeGroup;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isSpectator() {
        return currentPlayer == null;
    }

    public void setCurrentPlayer(Player player) {
        currentPlayer = player;
        if (player != null) {
            currentPlayerLabel.setText(player.getName());
            loginButton.setText("Switch");
            long fleets = player.getUnits().stream().filter(u -> u instanceof Fleet).count();
            long armies = player.getUnits().stream().filter(u -> u instanceof Army).count();
            int sc = player.getSupplyCenters().size();
            playerInfoLabel.setText(player.getName() + " | SC: " + sc + " | A: " + armies + " F: " + fleets);
            confirmOrdersButton.setDisable(false);
            confirmOrdersButton.setText(orderLedger.isConfirmed(player) ? "Cancel Confirmation" : "Confirm Orders");
        } else {
            currentPlayerLabel.setText("Spectator");
            loginButton.setText("Login");
            playerInfoLabel.setText("");
            confirmOrdersButton.setDisable(true);
            confirmOrdersButton.setText("Confirm Orders");
        }
        orderLedger.rebuild(player);
    }

    public void showLoginDialog(Window ownerWindow) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/playerLoginDialog.fxml"));
            Parent root = loader.load();
            PlayerLoginDialogController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(ownerWindow);
            dialog.setTitle("Player Login");
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));

            controller.initData(gameMaster.getPlayers());
            controller.setStage(dialog);

            dialog.showAndWait();

            Player authenticated = controller.getAuthenticatedPlayer();
            if (authenticated != null) {
                setCurrentPlayer(authenticated);
            }
        } catch (Exception e) {
            System.err.println("Failed to load login dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enterSpectatorMode() {
        setCurrentPlayer(null);
        for (Toggle t : orderTypeGroup.getToggles()) {
            if ("None".equals(((RadioButton) t).getText())) {
                orderTypeGroup.selectToggle(t);
                break;
            }
        }
    }

    public void updateConfirmButton() {
        if (currentPlayer != null) {
            confirmOrdersButton.setText(orderLedger.isConfirmed(currentPlayer) ? "Cancel Confirmation" : "Confirm Orders");
        }
    }
}
