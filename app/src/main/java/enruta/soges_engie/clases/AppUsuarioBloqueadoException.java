package enruta.soges_engie.clases;

public class AppUsuarioBloqueadoException extends RuntimeException {

    String message;
    Throwable cause;

    public AppUsuarioBloqueadoException() {
        super();
    }

    public AppUsuarioBloqueadoException(String message, Throwable cause) {
        super(message, cause);

        this.cause = cause;
        this.message = message;
    }

    public AppUsuarioBloqueadoException(String message) {
        super(message);

        this.cause = null;
        this.message = message;
    }
}