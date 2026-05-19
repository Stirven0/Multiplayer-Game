package com.aa.client.ui;

public class AutoLoginConfig {
    private final String username;
    private final String password;
    private final boolean autoRegister;
    private final boolean autoCreate;
    private final boolean autoJoin;
    private final int x;
    private final int y;

    public AutoLoginConfig(String username, String password, boolean autoRegister,
                           boolean autoCreate, boolean autoJoin, int x, int y) {
        this.username = username;
        this.password = password;
        this.autoRegister = autoRegister;
        this.autoCreate = autoCreate;
        this.autoJoin = autoJoin;
        this.x = x;
        this.y = y;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isAutoRegister() { return autoRegister; }
    public boolean isAutoCreate() { return autoCreate; }
    public boolean isAutoJoin() { return autoJoin; }
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean hasPosition() { return x >= 0 && y >= 0; }
}
