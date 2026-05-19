package com.aa.client;

import com.aa.client.ui.AutoLoginConfig;
import com.aa.client.ui.ScreenManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private static boolean enableMcp = false;
    private static AutoLoginConfig autoLogin;

    @Override
    public void start(Stage stage) {
        ScreenManager screens = new ScreenManager(autoLogin);
        screens.init(stage);
        if (enableMcp) {
            screens.enableMcpMode();
        }
    }

    public static void main(String[] args) {
        String username = null;
        String password = null;
        boolean autoRegister = false;
        boolean autoCreate = false;
        boolean autoJoin = false;
        int x = -1, y = -1;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--mcp" -> enableMcp = true;
                case "--username" -> username = args[++i];
                case "--password" -> password = args[++i];
                case "--auto-register" -> autoRegister = true;
                case "--auto-create" -> autoCreate = true;
                case "--auto-join" -> autoJoin = true;
                case "--x" -> x = Integer.parseInt(args[++i]);
                case "--y" -> y = Integer.parseInt(args[++i]);
            }
        }

        if (username != null && password != null) {
            autoLogin = new AutoLoginConfig(username, password, autoRegister, autoCreate, autoJoin, x, y);
        }

        launch(args);
    }
}
