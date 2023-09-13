package enruta.soges_engie.clases;

public interface VerificadorConectividadCallback {
    public void enExito(boolean exitoConexion, boolean exitoSesion);
    public void enFallo(int numError, String mensaje);
}
