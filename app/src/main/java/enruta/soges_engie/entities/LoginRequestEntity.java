package enruta.soges_engie.entities;
import java.util.Date;

public class LoginRequestEntity {
    public long idEmpleado;
    public String Usuario;
    public String Email;
    public String Password;
    public String CodigoSMS;
    public String VersionName;
    public String VersionCode;
    public String Token;
    public Date FechaOperacion;

//    public LoginRequestEntity(String Usuario, String Email, String Password, String CodigoSMS, String VersionName, String VersionCode){
//        this.Usuario = Usuario;
//        this.Email = Email;
//        this.Password = Password;
//        this.CodigoSMS = CodigoSMS;
//        this.VersionName = VersionName;
//        this.VersionCode = VersionCode;
//    }
}
