package enruta.soges_engie.clases;

import java.util.Date;

public class OperacionResponse {
    public long idEmpleado = 0;
    public int idOperacionTipo;
    public String OperacionTipo = "";
    public Date FechaEstatus = null;
    public Boolean Exito =false;
    public String Mensaje = "";
    public int NumError = 0;
    public String MensajeError = "";
    public long idArchivo;
    public String Archivo;
    public boolean RequiereCheckIn = false;
    public boolean RequiereCheckSeguridad = false;
    public boolean RequiereCheckOut = false;
}
