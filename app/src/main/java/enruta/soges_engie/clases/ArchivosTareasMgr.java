package enruta.soges_engie.clases;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import enruta.soges_engie.Globales;
import enruta.soges_engie.entities.TareasRequest;
import enruta.soges_engie.entities.TareasResponse;
import enruta.soges_engie.entities.ResumenEntity;
import enruta.soges_engie.services.DbLecturasMgr;
import enruta.soges_engie.services.WebApiManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArchivosTareasMgr {
    private final int EXITO = 0;
    private final int FALTA = 1;
    private final int ERROR_ENVIAR_1 = 2;
    private final int ERROR_ENVIAR_2 = 3;
    private final int ERROR_ENVIAR_3 = 4;
    private final int ERROR_ENVIAR_4 = 5;

    private Context mContext;
    private ArchivosTareasCallback mCallback = null;
    private TareasRequest mRequest;
    private Globales mGlobales;
    private ArrayList<Long> mListadoArchivosLect = new ArrayList<Long>();
    private long mIdArchivo = 0;
    private ResumenEntity mResumen;


    public ArchivosTareasMgr(Context context, Globales globales) {
        mContext = context;
        mGlobales = globales;
    }

    public void setCallback(ArchivosTareasCallback callback) {
        this.mCallback = callback;
    }

    public void marcarArchivosDescargados(){
        if (mListadoArchivosLect.size() == 0)
        {
            if (mCallback != null)
                mCallback.enSinArchivos();
        }
        else {
            mIdArchivo = mListadoArchivosLect.get(0);
            mListadoArchivosLect.remove(0);
            marcarArchivoDescargado(mIdArchivo);
        }
    }
    public void marcarArchivosTerminados()
    {
        mResumen = DbLecturasMgr.getInstance().getResumen(mContext);

        if (mResumen.totalRegistros >= 0 && mResumen.cantLecturasPendientes == 0) {
            mListadoArchivosLect = DbLecturasMgr.getInstance().getIdsArchivo(mContext);

            if (mListadoArchivosLect != null) {
                if (mListadoArchivosLect.size() != 0) {
                    mIdArchivo = mListadoArchivosLect.get(0);
                    mListadoArchivosLect.remove(0);
//                    marcarArchivoTerminado(mIdArchivo);
                }
            }
        }
    }

    public void marcarArchivoDescargado(long idArchivo) {
        mRequest = new TareasRequest();
        mRequest.idEmpleado = mGlobales.getIdEmpleado();

        try {
            WebApiManager.getInstance(mContext).marcarArchivoDescargado(mRequest, new Callback<TareasResponse>() {
                        @Override
                        public void onResponse(Call<TareasResponse> call, Response<TareasResponse> response) {
                            String valor;
                            TareasResponse resp;

                            if (response.isSuccessful()) {
                                resp = response.body();
                                exito(mRequest, resp);
                            } else
                                fallo(mRequest, null, ERROR_ENVIAR_2, "No hay conexión a internet (1). Intente nuevamente.", null);
                        }

                        @Override
                        public void onFailure(Call<TareasResponse> call, Throwable t) {
                            fallo(mRequest, null, ERROR_ENVIAR_3, "No hay conexión a internet (2). Intente nuevamente.", t);
                        }
                    }
            );
        } catch (Exception ex) {
            fallo(mRequest, null, ERROR_ENVIAR_4, "No hay conexión a internet (3). Intente nuevamente.", ex);
        }
    }

//    public void marcarArchivoTerminado(long idArchivo) {
//        mRequest = new ArchivosLectRequest();
//        mRequest.idEmpleado = mGlobales.getIdEmpleado();
//        mRequest.idArchivo = idArchivo;
//
//        try {
//            WebApiManager.getInstance(mContext).marcarArchivoTerminado(mRequest, new Callback<ArchivosLectResponse>() {
//                        @Override
//                        public void onResponse(Call<ArchivosLectResponse> call, Response<ArchivosLectResponse> response) {
//                            String valor;
//                            ArchivosLectResponse resp;
//
//                            if (response.isSuccessful()) {
//                                resp = response.body();
//                                if (resp.Exito) {
//                                    exitoTerminado(mRequest, resp);
//                                } else
//                                    falloTerminado(mRequest, null, ERROR_ENVIAR_1, "No hay conexión a internet. Intente nuevamente. (1)", null);
//                            } else
//                                falloTerminado(mRequest, null, ERROR_ENVIAR_2, "No hay conexión a internet. Intente nuevamente. (2)", null);
//                        }
//
//                        @Override
//                        public void onFailure(Call<ArchivosLectResponse> call, Throwable t) {
//                            falloTerminado(mRequest, null, ERROR_ENVIAR_3, "No hay conexión a internet. Intente nuevamente. (3)", t);
//                        }
//                    }
//            );
//        } catch (Exception ex) {
//            falloTerminado(mRequest, null, ERROR_ENVIAR_4, "No hay conexión a internet. Intente nuevamente. (4)", ex);
//        }
//    }

    private void exito(TareasRequest req, TareasResponse resp) {
        if (resp != null) {
            resp.NumError = 0;
        }

        marcarUsuarioRequiereCheckSeguridad();

        if (mListadoArchivosLect.size() == 0){
            if (mCallback != null)
                mCallback.enExitoComunicacion(req, resp);
        }
        else
        {
            mIdArchivo =mListadoArchivosLect.get(0);
            mListadoArchivosLect.remove(0);
            marcarArchivoDescargado(mIdArchivo);

        }
    }

    private void marcarUsuarioRequiereCheckSeguridad()
    {
        if (mGlobales == null)
            return;

        if (mGlobales.sesionEntity == null)
            return;

        if (mGlobales.sesionEntity.empleado == null)
            return;

        mGlobales.sesionEntity.empleado.RequiereCheckIn = true;
        mGlobales.sesionEntity.empleado.RequiereCheckSeguridad = true;
    }

    private void fallo(TareasRequest req, TareasResponse resp, int numError, String mensajeError, Throwable t) {
        String msg = "";

        if (!mensajeError.trim().equals("") && numError >= ERROR_ENVIAR_1) {
            if (t != null)
                msg = t.getMessage();

            Log.d("CPL", mensajeError + " : " + msg);
        }

        if (resp != null) {
            resp.NumError = numError;
            resp.MensajeError = mensajeError;
        }
        if (mCallback != null)
            mCallback.enFalloComunicacion(req, resp, numError, mensajeError);
    }

    private void exitoTerminado(TareasRequest req, TareasResponse resp) {
        if (resp != null) {
            resp.NumError = 0;
        }

        if (resp.Exito) {
            if (mListadoArchivosLect.size() == 0) {
                if (mCallback != null)
                    mCallback.enExitoComunicacion(req, resp);
            } else {
                mIdArchivo = mListadoArchivosLect.get(0);
                mListadoArchivosLect.remove(0);
//                marcarArchivoTerminado(mIdArchivo);
            }
        }
        else
        {
            if (mCallback != null)
                mCallback.enExitoComunicacion(req, resp);
        }
    }

    private void falloTerminado(TareasRequest req, TareasResponse resp, int numError, String mensajeError, Throwable t) {
        String msg = "";

        if (!mensajeError.trim().equals("") && numError >= ERROR_ENVIAR_1) {
            if (t != null)
                msg = t.getMessage();

            Log.d("CPL", mensajeError + " : " + msg);
        }

        if (resp != null) {
            resp.NumError = numError;
            resp.MensajeError = mensajeError;
        }
        if (mCallback != null)
            mCallback.enFalloComunicacion(req, resp, numError, mensajeError);
    }

    public void inicializarListaArchivosLect(){
        mListadoArchivosLect.clear();
    }

    public void agregarArchivoLect(long idArchivo)
    {
        if (!mListadoArchivosLect.contains(idArchivo))
            mListadoArchivosLect.add(idArchivo);
    }


}
