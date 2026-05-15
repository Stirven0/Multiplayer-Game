package com.aa.shared.message;

public class LoginResponseMessage extends Message {
    private String token;
    private String userId;
    private String username;
    private boolean success;
    private String errorMessage;

    public LoginResponseMessage() {
        super(MessageType.LOGIN_RESPONSE);
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
