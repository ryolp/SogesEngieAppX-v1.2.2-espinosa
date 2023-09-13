package enruta.soges_engie.clases;

public class AppException extends RuntimeException {

    String message;
    Throwable cause;

    public AppException() {
        super();
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);

        this.cause = cause;
        this.message = message;
    }

    public AppException(String message) {
        super(message);

        this.cause = null;
        this.message = message;
    }
}
