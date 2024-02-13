package enruta.soges_engie.entities;

import java.util.List;

public class TareasResponse {
    public boolean Exito = false;
    public int NumError = 0;
    public String Mensaje = "";
    public String MensajeError = "";
    public long idTarea = 0;
    public boolean EsUsuarioValido = false;
    public List<String> Contenido = null;
    public List<String> Contenido2 = null;
    public List<Long> ListadoTareasBorrar = null;
    public List<OrdenEstatusEntity> ListadoOrdenesBorrar = null;
}
