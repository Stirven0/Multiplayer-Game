package com.aa.client.util;

/**
 * Constantes de configuración del cliente.
 * Contiene URL del servidor, dimensiones de ventana y parámetros de cámara.
 */
public final class ClientConfig {

    private ClientConfig() {}

    /** URL del servidor WebSocket al que se conecta el cliente. */
    public static final String SERVER_URL = "ws://localhost:8080";
    /** Título de la ventana de la aplicación. */
    public static final String TITLE = "Shooter Client";
    /** Ancho por defecto de la ventana en píxeles. */
    public static final int WIDTH = 800;
    /** Alto por defecto de la ventana en píxeles. */
    public static final int HEIGHT = 450;

    /** Factor de suavizado para la interpolación de la cámara (0..1). */
    public static final double CAMERA_SMOOTH = 0.15;
}
