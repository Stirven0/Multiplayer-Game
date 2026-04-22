package com.aa.client;

import com.aa.client.ui.ScreenManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        ScreenManager screens = new ScreenManager();
        screens.init(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}