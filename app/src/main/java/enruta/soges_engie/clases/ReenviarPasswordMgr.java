package enruta.soges_engie.clases;

import android.content.Context;
import android.util.Log;

import enruta.soges_engie.entities.ReenviarPasswordRequest;
import enruta.soges_engie.entities.ReenviarPasswordResponse;
import enruta.soges_engie.services.WebApiManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReenviarPasswordMgr {
    protected Context mContext;
    protected ReenviarPasswordCallBack mCallback = null;

    public final int EXITO = 0;
    public final int FALTA = 1;
    public final int ERROR_ENVIAR_1 = 2;
    public final int ERROR_ENVIAR_2 = 3;
    public final int ERROR_ENVIAR_3 = 4;
    public final int ERROR_ENVIAR_4 = 5;

    ReenviarPasswordRequest mRequest = null;
    ReenviarPasswordResponse mResp = null;

    public interface ReenviarPasswordCallBack {
        public void enExito(ReenviarPasswordRequest request, ReenviarPasswordResponse resp);
        public void enFallo(ReenviarPasswordRequest request, ReenviarPasswordResponse resp);
    }

    public ReenviarPasswordMgr(Context context) {
        mContext = context;
    }

    public void setCallback(ReenviarPasswordCallBack callback) {
        this.mCallback = callback;
    }

    public void reenviarPassword(String usuario, String telefono) throws Exception {
        mRequest = new ReenviarPasswordRequest();
        mResp = new ReenviarPasswordResponse();

        mRequest.Usuario = usuario;
        mRequest.Telefono = telefono;

        try {
            WebApiManager.getInstance(mContext).reenviarPassword(mRequest,
                    new Callback<ReenviarPasswordResponse>() {
                        @Override
                        public void onResponse(Call<ReenviarPasswordResponse> call, Response<ReenviarPasswordResponse> response) {
                            String valor;
                            ReenviarPasswordResponse resp;

                            if (response.isSuccessful())
                                exito(mRequest, response.body());
                            else
                                fallo(mRequest, null, ERROR_ENVIAR_1, "No hay conexión a internet. Intente nuevamente. (1)", null);
                        }

                        @Override
                        public void onFailure(Call<ReenviarPasswordResponse> call, Throwable t) {
                            fallo(mRequest, null, ERROR_ENVIAR_2, "No hay conexión a internet. Intente nuevamente. (2)", t);
                        }
                    }
            );
        } catch (Exception ex) {
            fallo(mRequest, mResp, ERROR_ENVIAR_3, "No hay conexión a internet. Intente nuevamente. (3)", ex);
        }
    }

    private void exito(ReenviarPasswordRequest req, ReenviarPasswordResponse resp) {
        if (resp != null) {
            resp.NumError = 0;
        }

        if (mCallback != null)
            mCallback.enExito(req, resp);
    }

    private void fallo(ReenviarPasswordRequest req, ReenviarPasswordResponse resp, int codigo, String mensajeError, Throwable t) {
        String msg = "";

        if (!mensajeError.trim().equals("") && codigo >= ERROR_ENVIAR_1) {
            if (t != null)
                msg = t.getMessage();

            Log.d("CPL", mensajeError + " : " + msg);
        }

        if (resp == null)
            resp = new ReenviarPasswordResponse();

        resp.NumError = codigo;
        resp.MensajeError = msg;

        if (mCallback != null)
            mCallback.enFallo(req, resp);
    }
}
