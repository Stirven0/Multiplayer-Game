package com.aa.client.ui;

import com.aa.client.game.GameClient;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginScreen {
    private final GameClient gameClient;

    public LoginScreen(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    public Scene createScene() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label("Shooter Login");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");

        TextField user = new TextField("player1");
        user.setPromptText("Username");
        user.setMaxWidth(200);

        PasswordField pass = new PasswordField();
        pass.setPromptText("Password");
        pass.setText("pass1");
        pass.setMaxWidth(200);

        Label status = new Label();
        status.setStyle("-fx-text-fill: orange;");

        Button btn = new Button("Connect & Login");
        btn.setOnAction(e -> {
            btn.setDisable(true);
            status.setText("Conectando...");

            // Ejecutar en hilo background para no bloquear JavaFX
            new Thread(() -> {
                boolean ok = gameClient.connect();
                
                Platform.runLater(() -> {
                    if (ok) {
                        status.setText("Conectado. Enviando login...");
                        gameClient.sendLogin(user.getText(), pass.getText());
                    } else {
                        status.setText("Error de conexión");
                        btn.setDisable(false);
                    }
                });
            }).start();
        });

        root.getChildren().addAll(title, user, pass, btn, status);
        return new Scene(root, com.aa.client.util.ClientConfig.WIDTH, com.aa.client.util.ClientConfig.HEIGHT);
    }
}