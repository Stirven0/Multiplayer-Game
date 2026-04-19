package com.aa.shared.message;

/**
 * Mensaje de error del servidor al cliente.
 */
public class ErrorMessage extends Message {
    private String errorCode;
    private String message;
    private boolean fatal; // Si true, el cliente debe desconectarse
    
    public ErrorMessage() {
        super(MessageType.ERROR);
    }
    
    public ErrorMessage(String errorCode, String message, boolean fatal) {
        this();
        this.errorCode = errorCode;
        this.message = message;
        this.fatal = fatal;
    }
    
    public static ErrorMessage fatal(String code, String msg) {
        return new ErrorMessage(code, msg, true);
    }
    
    public static ErrorMessage warning(String code, String msg) {
        return new ErrorMessage(code, msg, false);
    }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isFatal() { return fatal; }
    public void setFatal(boolean fatal) { this.fatal = fatal; }
}
