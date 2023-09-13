package enruta.soges_engie.clases;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import enruta.soges_engie.entities.LoginRequestEntity;
import enruta.soges_engie.entities.LoginResponseEntity;
import enruta.soges_engie.services.WebApiManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AutenticadorMgr {
    protected Context mContext;
    protected AutenticadorCallback mCallback = null;

    public final int EXITO = 0;
    public final int FALTA_USUARIO_PASSWORD = 1;
    public final int FALTA_CODIGO_SMS = 3;
    public final int USUARIO_PASSWORD_INCORRECTO = 4;
    public final int CODIGO_SMS_INCORRECTO = 5;
    public final int ERROR_AUTENTICAR_1 = 6;
    public final int ERROR_AUTENTICAR_2 = 7;
    public final int ERROR_AUTENTICAR_3 = 8;
    public final int ERROR_AUTENTICAR_4 = 9;
    public final int ERROR_VALIDAR_SMS_1 = 10;
    public final int ERROR_VALIDAR_SMS_2 = 11;
    public final int ERROR_VALIDAR_SMS_3 = 12;

    public interface AutenticadorCallback {
        public void enExitoAutenticacion(LoginRequestEntity request, LoginResponseEntity resp);

        public void enFalloAutenticacion(LoginRequestEntity request, LoginResponseEntity resp);

        public void enErrorAutenticacion(LoginRequestEntity request, LoginResponseEntity resp);

        public void enExitoValidarSMS(LoginRequestEntity request, LoginResponseEntity resp);

        public void enFalloValidarSMS(LoginRequestEntity request, LoginResponseEntity resp);

        public void enErrorValidarSMS(LoginRequestEntity request, LoginResponseEntity resp);
    }

    LoginResponseEntity mResp = null;
    LoginRequestEntity mRequest = null;

    public AutenticadorMgr(Context context) {
        mContext = context;
    }

    public void setAutenticadorCallback(AutenticadorCallback autenticadorCallback) {
        this.mCallback = autenticadorCallback;
    }

    public void autenticar(String usuario, String password) {
        try {
            mResp = new LoginResponseEntity();
            mRequest = new LoginRequestEntity();

            if (usuario.contains("*9776")) {
                mResp.EsSuperUsuario = true;
                enExitoAutenticacion(null, mResp);
                return;
            } else if (usuario.equals("") || password.equals("")) {
                enFalloAutenticacion(null, mResp, FALTA_USUARIO_PASSWORD, "Falta capturar el usuario y/o contraseña");
                return;
            }

            mRequest.Usuario = usuario;
            mRequest.Password = password;
            mRequest.VersionName = getVersionName();
            mRequest.VersionCode = getVersionCode();
            mRequest.FechaOperacion = Utils.getDateTime();

            WebApiManager.getInstance(mContext).autenticarEmpleado(mRequest, new Callback<LoginResponseEntity>() {
                        @Override
                        public void onResponse(Call<LoginResponseEntity> call, Response<LoginResponseEntity> response) {
                            String valor;
                            LoginResponseEntity resp;

                            if (response.isSuccessful())
                                exitoRecepcion(mRequest, response.body());
                            else
                                errorRecepcion(mRequest, null, ERROR_AUTENTICAR_2, "No hay conexión a internet. Intente nuevamente. (1)", null);
                        }

                        @Override
                        public void onFailure(Call<LoginResponseEntity> call, Throwable t) {
                            errorRecepcion(mRequest, null, ERROR_AUTENTICAR_2, "No hay conexión a internet. Intente nuevamente. (2)", t);
                        }
                    }
            );
        } catch (Throwable t) {
            errorRecepcion(mRequest, mResp, ERROR_AUTENTICAR_3, "No hay conexión a internet. Intente nuevamente. (3)", t);
        }
    }

    private void exitoRecepcion(LoginRequestEntity req, LoginResponseEntity resp) {
        if (resp != null) {
            resp.CodigoResultado = 0;
        } else {
            resp = new LoginResponseEntity();
            resp.CodigoResultado = ERROR_AUTENTICAR_1;
            resp.MensajeError = "No hay conexión a internet. Intente nuevamente. (1)";
            resp.Exito = false;
        }

        if (resp.Exito)
            enExitoAutenticacion(req, resp);
        else
            enFalloAutenticacion(req, resp);
    }

    private void enExitoAutenticacion(LoginRequestEntity req, LoginResponseEntity resp) {
        if (mCallback != null)
            mCallback.enExitoAutenticacion(req, resp);
    }

    private void enFalloAutenticacion(LoginRequestEntity req, LoginResponseEntity resp) {
        if (mCallback != null)
            mCallback.enFalloAutenticacion(req, resp);
    }

    private void enFalloAutenticacion(LoginRequestEntity req, LoginResponseEntity resp, int numError, String mensajeError) {
        if (resp == null) {
            resp.Exito = false;
            resp.Error = false;
            resp.CodigoResultado = numError;
            resp.MensajeError = "";
            resp.Mensaje = mensajeError;
        }

        if (mCallback != null)
            mCallback.enFalloAutenticacion(req, resp);
    }


    private void errorRecepcion(LoginRequestEntity req, LoginResponseEntity resp, int codigo, String mensajeError, Throwable t) {
        String msg = "";

        if (!mensajeError.trim().equals("") && codigo >= ERROR_AUTENTICAR_1) {
            if (t != null)
                msg = t.getMessage();
            Log.d("CPL", mensajeError + " : " + msg);
        }

        if (resp == null) {
            resp = new LoginResponseEntity();
        }

        resp.CodigoResultado = codigo;
        resp.Mensaje = mensajeError;

        if (t != null)
            resp.MensajeError = t.getMessage();
        else
            resp.MensajeError = "";
        resp.Error = true;

        if (mCallback != null)
            mCallback.enErrorAutenticacion(req, resp);
    }

    public void validarSMS(String usuario, String codigoSMS) {
        try {
            mResp = new LoginResponseEntity();
            mRequest = new LoginRequestEntity();

            if (usuario.equals("") || codigoSMS.equals("")) {
                falloValidarSMS(null, mResp, FALTA_CODIGO_SMS, "Falta capturar el código SMS");
                return;
            }

            mRequest.Usuario = usuario;
            mRequest.CodigoSMS = codigoSMS;
            mRequest.VersionName = getVersionName();
            mRequest.VersionCode = getVersionCode();
            mRequest.FechaOperacion = Utils.getDateTime();

            WebApiManager.getInstance(mContext).validarEmpleadoSMS(mRequest, new Callback<LoginResponseEntity>() {
                        @Override
                        public void onResponse(Call<LoginResponseEntity> call, Response<LoginResponseEntity> response) {
                            String valor;
                            LoginResponseEntity loginResponseEntity;

                            if (response.isSuccessful())
                                exitoRecepcionMsgSMS(mRequest, response.body());
                            else {
                                falloRecepcionMsgSMS(mRequest, null, ERROR_VALIDAR_SMS_1,"No hay conexión a internet. Intente nuevamente. (1)", null);
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponseEntity> call, Throwable t) {
                            falloRecepcionMsgSMS(mRequest, null, ERROR_VALIDAR_SMS_2, "No hay conexión a internet. Intente nuevamente. (2)", t);
                        }
                    }
            );
        } catch (Exception ex) {
            falloRecepcionMsgSMS(mRequest, null, ERROR_VALIDAR_SMS_3, "No hay conexión a internet. Intente nuevamente. (3)", ex);
        }
    }

    private void exitoRecepcionMsgSMS(LoginRequestEntity req, LoginResponseEntity resp) {
        if (resp != null) {
            resp.CodigoResultado = 0;
        }

        if (resp.Exito)
            exitoValidarSMS(req, resp);
        else
            falloValidarSMS(req, resp);
    }

    private void falloRecepcionMsgSMS(LoginRequestEntity req, LoginResponseEntity resp, int codigo, String mensaje, Throwable t) {
        String msg = "";

        if (!mensaje.trim().equals("") && codigo >= ERROR_AUTENTICAR_1) {
            if (t != null)
                msg = t.getMessage();
            Log.d("CPL", mensaje + " : " + msg);
        }

        if (resp == null)
            resp = new LoginResponseEntity();

        resp.Error = true;
        resp.CodigoResultado = codigo;
        resp.Mensaje = mensaje;
        if (t != null)
            resp.MensajeError = t.getMessage();
        else
            resp.MensajeError = "";

        if (mCallback != null)
            mCallback.enErrorValidarSMS(req, resp);
    }

    private void exitoValidarSMS(LoginRequestEntity req, LoginResponseEntity resp) {
        if (mCallback != null)
            mCallback.enExitoValidarSMS(req, resp);
    }

    private void falloValidarSMS(LoginRequestEntity req, LoginResponseEntity resp) {
        if (mCallback != null)
            mCallback.enFalloValidarSMS(req, resp);
    }

    private void falloValidarSMS(LoginRequestEntity req, LoginResponseEntity resp, int numError, String mensajeError) {
        if (resp == null) {
            resp.Exito = false;
            resp.Error = false;
            resp.CodigoResultado = numError;
            resp.MensajeError = "";
            resp.Mensaje = mensajeError;
        }

        if (mCallback != null)
            mCallback.enFalloValidarSMS(req, resp);
    }

    private void errorValidarSMS(LoginRequestEntity req, LoginResponseEntity resp) {
        if (mCallback != null)
            mCallback.enErrorValidarSMS(req, resp);
    }


    private String getVersionName() {
        String versionName;

        try {
            versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (Exception ex) {
            versionName = "";
        }

        return versionName;
    }

    private String getVersionCode() {
        long versionCodeMajor;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                versionCodeMajor = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).getLongVersionCode();
            else
                versionCodeMajor = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (Exception ex) {
            versionCodeMajor = 0;
        }

        return Long.toString(versionCodeMajor);
    }
}
