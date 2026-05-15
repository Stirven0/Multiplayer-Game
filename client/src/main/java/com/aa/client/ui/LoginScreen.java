package com.aa.client.ui;

import com.aa.client.asset.AudioManager;
import com.aa.client.game.GameClient;
import com.aa.client.util.ClientConfig;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginScreen {
    private final GameClient gameClient;
    private Label status;
    private TextField user;
    private PasswordField pass;
    private Button btn;
    private boolean registerMode = false;
    private Hyperlink toggleLink;
    private VBox helpOverlay;

    public LoginScreen(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    public void setError(String msg) {
        Platform.runLater(() -> {
            if (status != null) {
                status.setText(msg);
                status.setStyle("-fx-text-fill: #ff5252;");
            }
        });
    }

    public Scene createScene(Stage stage) {
        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setStyle("-fx-padding: 20;");

        Label title = new Label("Shooter Login");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");

        user = new TextField("player1");
        user.setPromptText("Username");
        user.setMaxWidth(200);

        pass = new PasswordField();
        pass.setPromptText("Password");
        pass.setText("pass1");
        pass.setMaxWidth(200);

        status = new Label();
        status.setStyle("-fx-text-fill: orange;");

        btn = new Button("Connect & Login");
        btn.setOnAction(e -> doLogin());

        toggleLink = new Hyperlink("¿No tienes cuenta? Regístrate");
        toggleLink.setStyle("-fx-text-fill: #4fc3f7;");
        toggleLink.setOnAction(e -> toggleMode());

        Button exitBtn = new Button("Salir");
        exitBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;");
        exitBtn.setOnMouseEntered(e -> exitBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;"));
        exitBtn.setOnMouseExited(e -> exitBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;"));
        exitBtn.setOnAction(e -> Platform.exit());

        Button helpBtn = new Button("Ayuda");
        helpBtn.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;");
        helpBtn.setOnMouseEntered(e -> helpBtn.setStyle("-fx-background-color: #5a5a5a; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;"));
        helpBtn.setOnMouseExited(e -> helpBtn.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;"));
        helpBtn.setOnAction(e -> {
            helpOverlay.setVisible(true);
            helpOverlay.setManaged(true);
        });

        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER);
        bottomRow.getChildren().addAll(helpBtn, exitBtn);

        form.getChildren().addAll(title, user, pass, btn, toggleLink, status, bottomRow);

        // Help overlay
        helpOverlay = createHelpOverlay();
        helpOverlay.setVisible(false);
        helpOverlay.setManaged(false);

        StackPane centerStack = new StackPane(form, helpOverlay);

        BorderPane root = new BorderPane();
        root.setTop(TitleBar.create("Shooter Game", stage));
        root.setCenter(centerStack);
        root.setStyle("-fx-background-color: #2b2b2b;");

        return new Scene(root, ClientConfig.WIDTH, ClientConfig.HEIGHT + TitleBar.HEIGHT);
    }

    private VBox createHelpOverlay() {
        VBox overlay = new VBox(10);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-padding: 30;");

        Label helpTitle = new Label("AYUDA - CONTROLES");
        helpTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        String[] lines = {
            "WASD / Flechas ................. Moverse",
            "Mouse ......................... Apuntar",
            "Click izquierdo ............... Disparar",
            "SHIFT ......................... Correr",
            "",
            "Objetivo: Sé el último jugador en pie.",
            "Elimina a tus oponentes para ganar."
        };
        VBox textBox = new VBox(3);
        textBox.setAlignment(Pos.CENTER);
        for (String line : lines) {
            Label l = new Label(line);
            l.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px;");
            textBox.getChildren().add(l);
        }

        Button closeBtn = new Button("Cerrar");
        closeBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> {
            overlay.setVisible(false);
            overlay.setManaged(false);
        });

        overlay.getChildren().addAll(helpTitle, textBox, closeBtn);
        return overlay;
    }

    private void toggleMode() {
        registerMode = !registerMode;
        if (registerMode) {
            btn.setText("Register & Login");
            toggleLink.setText("Ya tienes cuenta? Inicia sesión");
            status.setText("Modo registro");
            status.setStyle("-fx-text-fill: orange;");
        } else {
            btn.setText("Connect & Login");
            toggleLink.setText("¿No tienes cuenta? Regístrate");
            status.setText("");
        }
    }

    private void doLogin() {
        btn.setDisable(true);
        AudioManager.playClick();
        status.setText("Conectando...");
        status.setStyle("-fx-text-fill: orange;");

        String username = user.getText().trim();
        String password = pass.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            status.setText("Usuario y contraseña requeridos");
            status.setStyle("-fx-text-fill: #ff5252;");
            btn.setDisable(false);
            return;
        }

        new Thread(() -> {
            boolean ok = gameClient.connect();

            Platform.runLater(() -> {
                if (ok) {
                    status.setText("Conectado. Enviando login...");
                    gameClient.sendLogin(username, password, registerMode);
                } else {
                    status.setText("Error de conexión");
                    status.setStyle("-fx-text-fill: #ff5252;");
                    btn.setDisable(false);
                }
            });
        }).start();
    }
}
