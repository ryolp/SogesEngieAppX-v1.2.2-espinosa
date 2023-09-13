package enruta.soges_engie.entities;

import java.util.Calendar;
import java.util.Date;

public class UsuarioEntity {
    public String Usuario = "";
    public String Email = "";
    public boolean EsLecturista = false;
    public boolean EsAdministrador = false;
    public boolean EsSuperUsuario = false;
    public String NumCPL = "";
    public boolean AutenticarConSMS = false;
    public Date HoraFinSesion;
    public long IdEmpleado = 0;
    public String Nombre = "";
    public String ApPat = "";
    public String ApMat = "";
    public String NombreCompleto = "";
    public String Estado = "";
    public String InfoAlias = "";
    public String Nomina = "";
    public String Telefono = "";
    public Date FechaAntiguedad;
    public Date FechaActivo;

    public UsuarioEntity(){

    }

    public UsuarioEntity(LoginResponseEntity loginResponseEntity) {
//        this.Usuario = loginResponseEntity.Usuario;
//        this.Email = loginResponseEntity.Email;
//        this.EsLecturista = loginResponseEntity.EsLecturista;
//        this.EsAdministrador = loginResponseEntity.EsAdministrador;
//        this.EsSuperUsuario = loginResponseEntity.EsSuperUsuario;
//        this.NumCPL = loginResponseEntity.NumCPL;
//        this.AutenticarConSMS = loginResponseEntity.AutenticarConSMS;
//
//        if (loginResponseEntity.Empleado != null) {
//            this.IdEmpleado = loginResponseEntity.Empleado.IdEmpleado;
//            this.Nombre = loginResponseEntity.Empleado.Nombre;
//            this.ApPat = loginResponseEntity.Empleado.ApPat;
//            this.ApMat = loginResponseEntity.Empleado.ApMat;
//            this.NombreCompleto = loginResponseEntity.Empleado.NombreCompleto;
//            this.Estado = loginResponseEntity.Empleado.Estado;
//            this.InfoAlias = loginResponseEntity.Empleado.InfoAlias;
//            this.Nomina = loginResponseEntity.Empleado.Nomina;
//            this.Telefono = loginResponseEntity.Empleado.Telefono;
//            this.FechaAntiguedad = loginResponseEntity.Empleado.FechaAntiguedad;
//            this.FechaActivo = loginResponseEntity.Empleado.FechaActivo;
//        }

        inicializarHoraVencimiento();
    }

    public void inicializarHoraVencimiento(){
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.HOUR, 4);
        HoraFinSesion = calendar.getTime();
    }

    public boolean esSesionVencida(){
        Date horaActual;

        horaActual = Calendar.getInstance().getTime();

        if (horaActual.after(this.HoraFinSesion))
            return true;
        else
            return false;
    }

}
