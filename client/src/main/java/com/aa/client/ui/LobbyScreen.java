package com.aa.client.ui;

import com.aa.client.game.GameClient;
import com.aa.client.util.ClientConfig;
import com.aa.shared.message.RoomListResponseMessage;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import java.util.List;
import java.util.Map;
import javafx.stage.Stage;

public class LobbyScreen {

    private static final Map<String, String> MAP_NAMES = Map.of(
        "map_01", "Warehouse",
        "map_02", "Desert Outpost",
        "map_03", "Frost Arena",
        "map_04", "Fortress"
    );

    private static final String BTN_STYLE = "-fx-background-color: #4a4a4a; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 4;";
    private static final String BTN_HOVER = "-fx-background-color: #5a5a5a; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 4;";
    private static final String BTN_DANGER = "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 4;";
    private static final String BTN_PRIMARY = "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 4;";
    private static final String PANEL = "-fx-background-color: #1e1e1e; -fx-padding: 12; -fx-background-radius: 6;";

    private final GameClient gameClient;
    private Label roomInfoLabel;
    private Label roomStatusLabel;
    private ComboBox<String> mapSelector;
    private ListView<String> playerList;
    private ListView<String> roomListView;
    private List<RoomListResponseMessage.RoomInfo> cachedRooms;
    private Label errorLabel;
    private VBox myRoomPanel;

    public LobbyScreen(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    public Scene createScene(Stage stage) {
        BorderPane content = new BorderPane();
        content.setStyle("-fx-background-color: #2b2b2b;");
        content.setPadding(new Insets(15));

        // === TOP BAR ===
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);

        String username = gameClient.getCurrentUsername();
        Label userLabel = new Label("Usuario: " + (username != null ? username : "?"));
        userLabel.setStyle("-fx-text-fill: #4fc3f7; -fx-font-size: 15px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Cerrar sesión");
        logoutBtn.setStyle(BTN_DANGER);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(BTN_DANGER.replace("#c0392b", "#e74c3c")));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(BTN_DANGER));
        logoutBtn.setOnAction(e -> gameClient.logout());

        topBar.getChildren().addAll(userLabel, spacer, logoutBtn);

        // === ERROR LABEL ===
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        // === CENTER: TWO COLUMNS ===
        HBox center = new HBox(20);
        center.setAlignment(Pos.TOP_CENTER);

        // --- LEFT COLUMN: My Room ---
        VBox left = new VBox(10);
        left.setPrefWidth(300);

        myRoomPanel = new VBox(8);
        myRoomPanel.setStyle(PANEL);

        Label myRoomTitle = new Label("MI SALA");
        myRoomTitle.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 14px; -fx-font-weight: bold;");

        roomInfoLabel = new Label("No estás en ninguna sala");
        roomInfoLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        roomStatusLabel = new Label();
        roomStatusLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");

        playerList = new ListView<>();
        playerList.setMaxHeight(120);
        playerList.setPrefHeight(120);
        playerList.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-fill: white; -fx-background-radius: 4;");

        HBox roomActions = new HBox(8);
        roomActions.setAlignment(Pos.CENTER);

        Button leaveBtn = new Button("Salir de sala");
        leaveBtn.setStyle(BTN_STYLE);
        leaveBtn.setOnMouseEntered(e -> leaveBtn.setStyle(BTN_HOVER));
        leaveBtn.setOnMouseExited(e -> leaveBtn.setStyle(BTN_STYLE));
        leaveBtn.setOnAction(e -> gameClient.leaveRoom());

        Button startBtn = new Button("Iniciar partida");
        startBtn.setStyle(BTN_PRIMARY);
        startBtn.setOnMouseEntered(e -> startBtn.setStyle(BTN_PRIMARY.replace("#2980b9", "#3498db")));
        startBtn.setOnMouseExited(e -> startBtn.setStyle(BTN_PRIMARY));
        startBtn.setOnAction(e -> gameClient.startGame());

        roomActions.getChildren().addAll(leaveBtn, startBtn);

        myRoomPanel.getChildren().addAll(myRoomTitle, roomInfoLabel, roomStatusLabel, playerList, roomActions);

        // --- Create Room sub-panel ---
        VBox createPanel = new VBox(8);
        createPanel.setStyle(PANEL);

        Label createTitle = new Label("CREAR SALA");
        createTitle.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 14px; -fx-font-weight: bold;");

        mapSelector = new ComboBox<>();
        for (var entry : MAP_NAMES.entrySet()) {
            mapSelector.getItems().add(entry.getValue() + " (" + entry.getKey() + ")");
        }
        mapSelector.setValue("Warehouse (map_01)");
        mapSelector.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white;");

        Button createBtn = new Button("Crear sala");
        createBtn.setStyle(BTN_PRIMARY);
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setOnMouseEntered(e -> createBtn.setStyle(BTN_PRIMARY.replace("#2980b9", "#3498db")));
        createBtn.setOnMouseExited(e -> createBtn.setStyle(BTN_PRIMARY));
        createBtn.setOnAction(e -> {
            String selected = mapSelector.getValue();
            String mapId = selected.substring(selected.lastIndexOf('(') + 1, selected.lastIndexOf(')'));
            gameClient.createRoom(mapId);
            createBtn.setDisable(true);
        });

        createPanel.getChildren().addAll(createTitle, mapSelector, createBtn);

        left.getChildren().addAll(myRoomPanel, createPanel);

        // --- RIGHT COLUMN: Available Rooms ---
        VBox right = new VBox(10);
        right.setPrefWidth(380);

        VBox roomsPanel = new VBox(8);
        roomsPanel.setStyle(PANEL);

        HBox roomsHeader = new HBox(8);
        roomsHeader.setAlignment(Pos.CENTER_LEFT);

        Label roomsTitle = new Label("SALAS DISPONIBLES");
        roomsTitle.setStyle("-fx-text-fill: #3498db; -fx-font-size: 14px; -fx-font-weight: bold;");

        Region roomsSpacer = new Region();
        HBox.setHgrow(roomsSpacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refrescar");
        refreshBtn.setStyle(BTN_STYLE);
        refreshBtn.setOnMouseEntered(e -> refreshBtn.setStyle(BTN_HOVER));
        refreshBtn.setOnMouseExited(e -> refreshBtn.setStyle(BTN_STYLE));
        refreshBtn.setOnAction(e -> gameClient.requestRoomList());

        roomsHeader.getChildren().addAll(roomsTitle, roomsSpacer, refreshBtn);

        roomListView = new ListView<>();
        roomListView.setPrefHeight(200);
        roomListView.setStyle("-fx-control-inner-background: #2a2a2a; -fx-text-fill: white; -fx-background-radius: 4;");
        roomListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                int idx = roomListView.getSelectionModel().getSelectedIndex();
                if (idx >= 0 && idx < cachedRooms.size()) {
                    gameClient.joinRoom(cachedRooms.get(idx).getRoomId());
                }
            }
        });

        Button joinSelectedBtn = new Button("Unirse a sala seleccionada");
        joinSelectedBtn.setStyle(BTN_PRIMARY);
        joinSelectedBtn.setMaxWidth(Double.MAX_VALUE);
        joinSelectedBtn.setOnMouseEntered(e -> joinSelectedBtn.setStyle(BTN_PRIMARY.replace("#2980b9", "#3498db")));
        joinSelectedBtn.setOnMouseExited(e -> joinSelectedBtn.setStyle(BTN_PRIMARY));
        joinSelectedBtn.setOnAction(e -> {
            int idx = roomListView.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < cachedRooms.size()) {
                gameClient.joinRoom(cachedRooms.get(idx).getRoomId());
            } else {
                setError("Selecciona una sala de la lista");
            }
        });

        roomsPanel.getChildren().addAll(roomsHeader, roomListView, joinSelectedBtn);
        right.getChildren().add(roomsPanel);

        center.getChildren().addAll(left, right);

        // === ASSEMBLE ===
        VBox topSection = new VBox(5);
        topSection.getChildren().addAll(topBar, errorLabel);

        content.setTop(topSection);
        content.setCenter(center);

        BorderPane root = new BorderPane();
        root.setTop(TitleBar.create("Lobby", stage, true));
        root.setCenter(content);

        Scene scene = new Scene(root, ClientConfig.WIDTH, ClientConfig.HEIGHT + TitleBar.HEIGHT);

        gameClient.requestRoomList();

        return scene;
    }

    public void setError(String msg) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText(msg);
                errorLabel.setVisible(true);
            }
        });
    }

    public void updateRoomInfo(String roomId) {
        Platform.runLater(() -> {
            roomInfoLabel.setText("Sala: " + roomId);
            roomStatusLabel.setText("Esperando jugadores...");
        });
    }

    public void updatePlayerList(List<String> playerIds) {
        Platform.runLater(() -> {
            playerList.getItems().setAll(playerIds);
        });
    }

    public void updateRoomList(List<RoomListResponseMessage.RoomInfo> rooms) {
        this.cachedRooms = rooms;
        Platform.runLater(() -> {
            roomListView.getItems().clear();
            if (rooms.isEmpty()) {
                roomListView.getItems().add("(No hay salas disponibles)");
            }
            for (RoomListResponseMessage.RoomInfo r : rooms) {
                String mapName = MAP_NAMES.getOrDefault(r.getMapId(), r.getMapId());
                roomListView.getItems().add(
                    r.getRoomId() + "  |  " + mapName + "  |  " +
                    r.getPlayerCount() + "/" + r.getMaxPlayers() + " jugs  |  " + r.getStatus()
                );
            }
        });
    }
}
