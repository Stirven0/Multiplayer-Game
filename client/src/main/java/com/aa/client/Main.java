package com.aa.client;

import com.aa.client.ui.ScreenManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private static boolean enableMcp = false;

    @Override
    public void start(Stage stage) {
        ScreenManager screens = new ScreenManager();
        screens.init(stage);
        if (enableMcp) {
            screens.enableMcpMode();
        }
    }

    public static void main(String[] args) {
        for (String arg : args) {
            if ("--mcp".equals(arg)) {
                enableMcp = true;
            }
        }
        launch(args);
    }
}