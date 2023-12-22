package enruta.soges_engie.clases;

public class AppSinGps extends RuntimeException {

    String message;
    Throwable cause;

    public AppSinGps() {
        super();
    }

    public AppSinGps(String message, Throwable cause) {
        super(message, cause);

        this.cause = cause;
        this.message = message;
    }

    public AppSinGps(String message) {
        super(message);

        this.cause = null;
        this.message = message;
    }
}