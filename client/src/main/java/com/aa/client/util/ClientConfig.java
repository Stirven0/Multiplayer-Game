package com.aa.client.util;

public final class ClientConfig {
    private ClientConfig() {}

    public static final String SERVER_URL = "ws://localhost:8080";
    public static final String TITLE = "Shooter Client";
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    public static final double CAMERA_SMOOTH = 0.15;
}