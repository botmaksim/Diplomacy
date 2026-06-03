package com.diplomacy;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DesktopLauncher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/views/mainGameView.fxml"));

        stage.setScene(new Scene(root));
        stage.setTitle("Diplomacy");

        stage.setWidth(1280);
        stage.setHeight(800);
        stage.setResizable(true);

        stage.show();
    }
}