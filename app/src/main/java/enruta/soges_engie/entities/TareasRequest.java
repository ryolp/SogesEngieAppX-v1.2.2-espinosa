package enruta.soges_engie.entities;

import java.util.Date;
import java.util.List;

public class TareasRequest {
    public long idEmpleado = 0;
    public String Token = "";
    public Date FechaOperacion = null;
    public int NumGrupo = 0;
    public List<Long> listadoIdsTareas;
}
