package enruta.soges_engie.clases;

public class AppSinInternet extends RuntimeException {

    String message;
    Throwable cause;

    public AppSinInternet() {
        super();
    }

    public AppSinInternet(String message, Throwable cause) {
        super(message, cause);

        this.cause = cause;
        this.message = message;
    }

    public AppSinInternet(String message) {
        super(message);

        this.cause = null;
        this.message = message;
    }
}