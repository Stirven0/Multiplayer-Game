package com.aa.shared.message;

/**
 * Mensaje de autenticación.
 */
public class LoginMessage extends Message {
    private String username;
    private String password; // En producción: hash, no texto plano
    private String token;    // Para re-conexión con sesión existente
    private boolean register;

    public LoginMessage() {
        super(MessageType.LOGIN_REQUEST);
    }
    
    public LoginMessage(String username, String password) {
        this();
        this.username = username;
        this.password = password;
    }

    public LoginMessage(String username, String password, boolean register) {
        this();
        this.username = username;
        this.password = password;
        this.register = register;
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isRegister() { return register; }
    public void setRegister(boolean register) { this.register = register; }
}
