package enruta.soges_engie.clases;

import enruta.soges_engie.entities.TareasRequest;
import enruta.soges_engie.entities.TareasResponse;

public interface ArchivosTareasCallback {
    public void enExitoComunicacion(TareasRequest request, TareasResponse resp);
    public void enFalloComunicacion(TareasRequest request, TareasResponse resp, int numError, String mensajeError);
    public void enSinArchivos();
}
