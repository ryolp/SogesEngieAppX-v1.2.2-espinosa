package enruta.soges_engie.entities;

public class LoginResponseEntity {
    public String Usuario = "";
    public String Email = "";
    public String Token = "";
    public String Mensaje = "";
    public String MensajeError = "";
    public String MensajeLecturista = "";
    public int CodigoResultado = 0;
    public boolean Exito = false;
    public boolean Error = false;
    public boolean SesionOk = false;
    public boolean EsLecturista = false;
    public boolean EsAdministrador = false;
    public boolean EsSuperUsuario = false;
    public boolean AutenticarConSMS = false;
    public String NumCPL = "";
    public String VersionWeb = "";
    public EmpleadoCplEntity Empleado;
    public ParametrosCelular Parametros;

//    public LoginResponseEntity(String Usuario, String Email, String Token, String Mensaje, boolean Exito, boolean Error,
//                               boolean EsLecturista, boolean EsAdministrador, boolean EsSuperUsuario, boolean AutenticarConSMS,
//                               String NumCPL, EmpleadoCplEntity empleado) {
//        this.Usuario = Usuario;
//        this.Email = Email;
//        this.Token = Token;
//        this.Mensaje = Mensaje;
//        this.Exito = Exito;
//        this.Error = Error;
//        this.EsLecturista = EsLecturista;
//        this.EsAdministrador = EsAdministrador;
//        this.EsSuperUsuario = EsSuperUsuario;
//        this.AutenticarConSMS = AutenticarConSMS;
//        this.NumCPL = NumCPL;
//        this.Empleado = empleado;
//    }
}