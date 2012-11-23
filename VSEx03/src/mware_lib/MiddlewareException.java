package mware_lib;

public class MiddlewareException extends RuntimeException {
    private static final long serialVersionUID = 3959649758964210746L;
    
    public MiddlewareException(String message) {
        super(message);
    }
}
