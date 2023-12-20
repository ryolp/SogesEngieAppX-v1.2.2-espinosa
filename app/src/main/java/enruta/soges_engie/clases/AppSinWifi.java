package enruta.soges_engie.clases;

public class AppSinWifi  extends RuntimeException {

    String message;
    Throwable cause;

    public AppSinWifi() {
        super();
    }

    public AppSinWifi(String message, Throwable cause) {
        super(message, cause);

        this.cause = cause;
        this.message = message;
    }

    public AppSinWifi(String message) {
        super(message);

        this.cause = null;
        this.message = message;
    }
}