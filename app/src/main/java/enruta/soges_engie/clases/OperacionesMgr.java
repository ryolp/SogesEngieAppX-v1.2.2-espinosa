package enruta.soges_engie.clases;

import android.content.Context;
import android.util.Log;

import enruta.soges_engie.services.WebApiManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* ====================================================================================
    OperacionesMgr:

    Esta clase encapsula el envío de las operaciones de Check-In, Check-Out y ...
    ... Check-Seguridad al servidor. Proporciona unos callbacks para que donde...
    ... se utilice reciban el resultado de manera asíncrona.
==================================================================================== */
public class OperacionesMgr {
    protected Context mContext;
    protected OperacionesMgr.OperacionesCallback mCallback = null;

    public final int EXITO = 0;
    public final int ERROR_OPERACION_1 = 6;
    public final int ERROR_OPERACION_2 = 7;
    public final int ERROR_OPERACION_3 = 8;

    public interface OperacionesCallback {
        public void enExito(OperacionRequest request, OperacionResponse resp);

        public void enFallo(OperacionRequest request, OperacionResponse resp);

        public void enError(OperacionRequest request, OperacionResponse resp);
    }

    OperacionResponse mResp = null;
    OperacionRequest mRequest = null;

    public OperacionesMgr(Context context) {
        mContext = context;
    }


    public void checkSeguridad(long idEmpleado, OperacionesMgr.OperacionesCallback theCallback) {
        try {
            mResp = new OperacionResponse();
            mRequest = new OperacionRequest();

            mRequest.idEmpleado = idEmpleado;
            mRequest.FechaOperacion = Utils.getDateTime();

            this.mCallback = theCallback;

            WebApiManager.getInstance(mContext).checkSeguridad(mRequest, new Callback<OperacionResponse>() {
                        @Override
                        public void onResponse(Call<OperacionResponse> call, Response<OperacionResponse> response) {
                            String valor;
                            OperacionResponse resp;

                            if (response.isSuccessful())
                                exitoOperacion(mRequest, response.body());
                            else
                                enErrorOperacion(mRequest, null, ERROR_OPERACION_1, "No hay conexión a internet. Intente nuevamente. (1)", null);
                        }

                        @Override
                        public void onFailure(Call<OperacionResponse> call, Throwable t) {
                            enErrorOperacion(mRequest, null, ERROR_OPERACION_2, "No hay conexión a internet. Intente nuevamente. (2)", t);
                        }
                    }
            );
        } catch (Throwable t) {
            enErrorOperacion(mRequest, mResp, ERROR_OPERACION_3, "No hay conexión a internet. Intente nuevamente. (3)", t);
        }
    }

    public void checkIn(long idEmpleado, OperacionesMgr.OperacionesCallback theCallback) {
        try {
            mResp = new OperacionResponse();
            mRequest = new OperacionRequest();

            mRequest.idEmpleado = idEmpleado;
            mRequest.FechaOperacion = Utils.getDateTime();

            this.mCallback = theCallback;

            WebApiManager.getInstance(mContext).checkSeguridad(mRequest, new Callback<OperacionResponse>() {
                        @Override
                        public void onResponse(Call<OperacionResponse> call, Response<OperacionResponse> response) {
                            String valor;
                            OperacionResponse resp;

                            if (response.isSuccessful())
                                exitoOperacion(mRequest, response.body());
                            else
                                enErrorOperacion(mRequest, null, ERROR_OPERACION_1, "No hay conexión a internet. Intente nuevamente. (1)", null);
                        }

                        @Override
                        public void onFailure(Call<OperacionResponse> call, Throwable t) {
                            enErrorOperacion(mRequest, null, ERROR_OPERACION_2, "No hay conexión a internet. Intente nuevamente. (2)", t);
                        }
                    }
            );
        } catch (Throwable t) {
            enErrorOperacion(mRequest, mResp, ERROR_OPERACION_3, "No hay conexión a internet. Intente nuevamente. (3)", t);
        }
    }

    public void checkOut(long idEmpleado, OperacionesMgr.OperacionesCallback theCallback) {
        try {
            mResp = new OperacionResponse();
            mRequest = new OperacionRequest();

            mRequest.idEmpleado = idEmpleado;
            mRequest.FechaOperacion = Utils.getDateTime();

            this.mCallback = theCallback;

            WebApiManager.getInstance(mContext).checkSeguridad(mRequest, new Callback<OperacionResponse>() {
                        @Override
                        public void onResponse(Call<OperacionResponse> call, Response<OperacionResponse> response) {
                            String valor;
                            OperacionResponse resp;

                            if (response.isSuccessful())
                                exitoOperacion(mRequest, response.body());
                            else
                                enErrorOperacion(mRequest, null, ERROR_OPERACION_1, "No hay conexión a internet. Intente nuevamente. (1)", null);
                        }

                        @Override
                        public void onFailure(Call<OperacionResponse> call, Throwable t) {
                            enErrorOperacion(mRequest, null, ERROR_OPERACION_2, "No hay conexión a internet. Intente nuevamente. (2)", t);
                        }
                    }
            );
        } catch (Throwable t) {
            enErrorOperacion(mRequest, mResp, ERROR_OPERACION_3, "No hay conexión a internet. Intente nuevamente. (3)", t);
        }
    }

    private void exitoOperacion(OperacionRequest req, OperacionResponse resp) {
        if (resp != null) {
            resp.NumError = 0;
        } else {
            resp = new OperacionResponse();
            resp.NumError = ERROR_OPERACION_1;
            resp.MensajeError = "No hay conexión a internet. Intente nuevamente. (1)";
            resp.Exito = false;
        }

        if (mCallback != null) {
            if (resp.Exito)
                mCallback.enExito(req, resp);
            else
                mCallback.enFallo(req, resp);
        }
    }

    private void enFalloOperacion(OperacionRequest req, OperacionResponse resp) {
        if (mCallback != null)
            mCallback.enFallo(req, resp);
    }

    private void enErrorOperacion(OperacionRequest req, OperacionResponse resp, int codigo, String mensajeError, Throwable t) {
        String msg = "";

        if (!mensajeError.trim().equals("") && codigo >= ERROR_OPERACION_1) {
            if (t != null)
                msg = t.getMessage();
            Log.d("CPL", mensajeError + " : " + msg);
        }

        if (resp == null) {
            resp = new OperacionResponse();
        }

        resp.NumError = codigo;
        resp.Mensaje = mensajeError;

        if (t != null)
            resp.MensajeError = t.getMessage();
        else
            resp.MensajeError = "";

        if (mCallback != null)
            mCallback.enError(req, resp);
    }
}
