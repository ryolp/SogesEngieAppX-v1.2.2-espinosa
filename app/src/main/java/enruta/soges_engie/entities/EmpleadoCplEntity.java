package enruta.soges_engie.entities;

import java.util.Date;

public class EmpleadoCplEntity {
    public long idEmpleado = 0;
    public String Nombre = "";
    public String ApPat = "";
    public String ApMat = "";
    public String NombreCompleto = "";
    public String Estado = "";
    public String InfoAlias = "";
    public String Email = "";
    public String Nomina = "";
    public String Telefono = "";
    public Date FechaAntiguedad;
    public Date FechaIngreso;
    public int idAgencia=0;
    public String Agencia= "";
    public String Regional = "";
    public String FotoURL="";
    public int idOperacionTipo=0;
    public int ArchivoAbierto=0;
    public boolean RequiereCheckIn = false;
    public boolean RequiereCheckSeguridad = false;
    public boolean RequiereCheckOut = false;
    public String Token="";
    public boolean EsLecturista= false;
    public boolean EsAdministrador= false;
    public boolean EsSuperUsuario= false;
    public boolean EsSupervisor = false;
}
