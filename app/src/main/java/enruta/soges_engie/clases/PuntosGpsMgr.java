package enruta.soges_engie.clases;

import android.content.Context;
import android.util.Log;

import java.util.Date;

import enruta.soges_engie.Globales;
import enruta.soges_engie.entities.PuntoGpsRequest;
import enruta.soges_engie.entities.PuntoGpsResponse;
import enruta.soges_engie.services.WebApiManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PuntosGpsMgr {
    protected Context mContext;

    protected final int EXITO = 0;
    protected final int FALTA = 1;
    protected final int ERROR_ENVIAR_1 = 2;
    protected final int ERROR_ENVIAR_2 = 3;
    protected final int ERROR_ENVIAR_3 = 4;
    protected final int ERROR_ENVIAR_4 = 5;
    protected Globales mGlobales = null;

    protected PuntosGpsMgr.EnCallback mCallBack = null;

    public PuntosGpsMgr(Context context, Globales globales)
    {
        mContext = context;
        mGlobales = globales;
    }

       /*
        setEnCallback().

        Metodo para guardar una referencia al método del proceso que llama a la instancia de esta clase,
        el cual recibirá las notificaciones de este proceso.
    */

    public void setEnCallback(PuntosGpsMgr.EnCallback callback) {
        mCallBack = callback;
    }

    /*
        EnCallback.

        Interface para el envío de las notificaciones al proceso que llama a la instancia de esta clase.
    */

    public interface EnCallback {
        public void enExito();

        public void enFallo(String mensajeError);

        public void enError(int numError, String mensajeError, String detalleError);
    }

    public void registrarPuntoAsync(long idEmpleado, String PTN, Date fecha, String latitud, String longitud, String tipo) {
        PuntoGpsRequest req = new PuntoGpsRequest();



        try {
            req.idEmpleado = idEmpleado;
            req.PTN = PTN;
            req.Fecha = fecha;
            req.Latitud = latitud;
            req.Longitud = longitud;
            req.Tipo = tipo;
            req.NivelBateria = BateriaMgr.getBatteryPercentage(mContext);

            WebApiManager.getInstance(mContext).registrarPuntoGpsAsync(req, new Callback<PuntoGpsResponse>() {
                @Override
                public void onResponse(Call<PuntoGpsResponse> call, Response<PuntoGpsResponse> response) {
                    PuntoGpsResponse resp;

                    if (response.isSuccessful()) {
                        resp = response.body();
                        if (resp.NumError == 0) {
                            enExito();
                        } else {
                            enFallo(ERROR_ENVIAR_1, "No hay conexión a internet. Intente nuevamente. (1)");
                        }
                    } else
                        enFallo(ERROR_ENVIAR_2, "No hay conexión a internet. Intente nuevamente. (2)");
                }

                @Override
                public void onFailure(Call<PuntoGpsResponse> call, Throwable t) {

                }
            });
        } catch (Throwable t) {
            enError(ERROR_ENVIAR_3, "Problema inesperado", t);
        }
    }

    public boolean registrarPunto(long idEmpleado, String PTN, Date fecha, String latitud, String longitud, String tipo) {
        PuntoGpsRequest req = new PuntoGpsRequest();
        PuntoGpsResponse resp;

        try {
            req.idEmpleado = idEmpleado;
            req.PTN = PTN;
            req.Fecha = fecha;
            req.Latitud = latitud;
            req.Longitud = longitud;
            req.Tipo = tipo;
            req.NivelBateria = BateriaMgr.getBatteryPercentage(mContext);

            resp = WebApiManager.getInstance(mContext).registrarPuntoGps(req);

            if (resp.NumError == 0)
                return true;
            else
                return false;
        }
        catch (Exception e) {
            return false;
        }
    }

    private void enExito(){
        if (mCallBack != null)
            mCallBack.enExito();
    }

    private void enFallo(int numError, String mensajeError) {
        String msg = "";

        if (!mensajeError.trim().equals("") && numError >= ERROR_ENVIAR_1) {
            Log.d("CPL", mensajeError + " : " + msg);
        }

        if (mCallBack != null)
            mCallBack.enFallo(mensajeError);
    }

    private void enError(int numError, String mensajeError, Throwable t) {
        String msg = "";

        if (!mensajeError.trim().equals("") && numError >= ERROR_ENVIAR_1) {
            if (t != null)
                msg = t.getMessage();

            Log.d("CPL", mensajeError + " : " + msg);
        }

        if (mCallBack != null)
            mCallBack.enError(numError, mensajeError, t.getMessage());
    }
}
