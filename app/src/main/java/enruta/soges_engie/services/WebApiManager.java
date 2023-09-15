package enruta.soges_engie.services;

import android.content.Context;

import enruta.soges_engie.Globales;
import enruta.soges_engie.clases.OperacionRequest;
import enruta.soges_engie.clases.OperacionResponse;
import enruta.soges_engie.entities.SubirFotoRequest;
import enruta.soges_engie.entities.SubirFotoResponse;
import enruta.soges_engie.entities.TareasRequest;
import enruta.soges_engie.entities.TareasResponse;
import enruta.soges_engie.entities.LoginRequestEntity;
import enruta.soges_engie.entities.LoginResponseEntity;
import enruta.soges_engie.entities.PuntoGpsRequest;
import enruta.soges_engie.entities.PuntoGpsResponse;
import enruta.soges_engie.interfaces.IWebApi;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebApiManager {
    private IWebApi service = null;
    private static WebApiManager apiManager;
    private static String _servidorUrl="";
    private static Globales globales;

    private WebApiManager(String servidor) throws Exception {
        createConection(servidor);
    }

    protected void createConection(String servidor)  throws Exception {
        if (service == null) {
            _servidorUrl = servidor;
            service = WebApiService.Create(servidor);
        }
        else if (!_servidorUrl.equals(servidor)){
            // Si la URL cambió entonces destruye el objeto de transmisión de Retrofil y crea uno nuevo..
            // ... con la nueva URL

            _servidorUrl = servidor;
            service = null;
            service = WebApiService.Create(_servidorUrl);
        }
    }

    // Para crear una sola instancia de esta clase que será de gestión para solicitar la autenticación
    private static WebApiManager getInstance(String servidor) throws Exception {
        if (apiManager == null)
            apiManager = new WebApiManager(servidor);
        else
            apiManager.createConection(servidor);

        return apiManager;
    }

    // Para crear una sola instancia de esta clase que será de gestión para solicitar la autenticación
    public static WebApiManager getInstance(Context context) throws Exception {
        String servidor = "";

        if (context == null)
            throw new Exception("No se ha definido un contexto");

        servidor = DbConfigMgr.getInstance().getServidor(context);

        if (servidor.trim().equals(""))
            servidor = globales.defaultServidorGPRS;

        if (apiManager == null)
            apiManager = new WebApiManager(servidor);
        else
            apiManager.createConection(servidor);

        return apiManager;
    }

    // Se hace una llamada asíncrona al web api, y el resultado lo recibirá la función que se defina en callback
    public void echoPing(Callback<String> callback){
        Call<String> call = service.echoping();

        call.enqueue(callback);
    }

    public void autenticarEmpleado(LoginRequestEntity loginRequestEntity, Callback<LoginResponseEntity> callBack){
        Call<LoginResponseEntity> call = service.autenticarEmpleado(loginRequestEntity);

        call.enqueue(callBack);
    }

    public void validarEmpleadoSMS(LoginRequestEntity loginRequestEntity, Callback<LoginResponseEntity> callBack){
        Call<LoginResponseEntity> call = service.validarEmpleadoSMS(loginRequestEntity);

        call.enqueue(callBack);
    }

//    public void autenticarEmpleado2(LoginRequestEntity loginRequestEntity, Callback<LoginResponseEntity> callBack){
//        Call<LoginResponseEntity> call = service.autenticarEmpleado2(loginRequestEntity.Usuario, loginRequestEntity.Password);
//
//        call.enqueue(callBack);
//    }
//
//    public void validarEmpleadoSMS2(LoginRequestEntity loginRequestEntity, Callback<LoginResponseEntity> callBack){
//        Call<LoginResponseEntity> call = service.validarEmpleadoSMS2(loginRequestEntity.Usuario, loginRequestEntity.CodigoSMS);
//
//        call.enqueue(callBack);
//    }

    public void checkIn(OperacionRequest request, Callback<OperacionResponse> callBack){
        Call<OperacionResponse> call = service.checkIn(request);

        call.enqueue(callBack);
    }

    public void checkOut(OperacionRequest request, Callback<OperacionResponse> callBack){
        Call<OperacionResponse> call = service.checkOut(request);

        call.enqueue(callBack);
    }

    public void checkSeguridad(OperacionRequest request, Callback<OperacionResponse> callBack){
        Call<OperacionResponse> call = service.checkSeguridad(request);

        call.enqueue(callBack);
    }
//
//    public void cerrarArchivo(OperacionRequest request, Callback<OperacionResponse> callBack){
//        Call<OperacionResponse> call = service.cerrarArchivo(request);
//
//        call.enqueue(callBack);
//    }
//
////    public void marcarArchivoDescargado(OperacionRequest request, Callback<OperacionResponse> callBack){
////        Call<OperacionResponse> call = service.marcarArchivoDescargado(request);
////
////        call.enqueue(callBack);
////    }

    public void marcarArchivoDescargado(TareasRequest request, Callback<TareasResponse> callBack){
        Call<TareasResponse> call = service.marcarArchivoTareasDescargado(request);

        call.enqueue(callBack);
    }

    public void descargarTareasAsync(TareasRequest request, Callback<TareasResponse> callBack){
        Call<TareasResponse> call = service.descargarTareas(request);

        call.enqueue(callBack);
    }

    public TareasResponse descargarTareas(TareasRequest request) throws Exception{
        Call<TareasResponse> call = service.descargarTareas(request);

        Response<TareasResponse> resp = call.execute();

        return resp.body();
    }

//
//    public void marcarArchivoTerminado(ArchivosLectRequest request, Callback<ArchivosLectResponse> callBack){
//        Call<ArchivosLectResponse> call = service.marcarArchivoTerminado(request);
//
//        call.enqueue(callBack);
//    }

    public void solicitarAyuda(OperacionRequest request, Callback<OperacionResponse> callBack){
        Call<OperacionResponse> call = service.solicitarAyuda(request);

        call.enqueue(callBack);
    }

    public void verificarConexion(LoginRequestEntity request, Callback<LoginResponseEntity> callBack){
        Call<LoginResponseEntity> call = service.verificarConexion(request);

        call.enqueue(callBack);
    }

//    public void registrarLogSupervisor(SupervisorLogRequest request, Callback<SupervisorLogResponse> callBack){
//        Call<SupervisorLogResponse> call = service.registrarLogSupervisor(request);
//
//        call.enqueue(callBack);
//    }
//
//    public void buscarMedidor(BuscarMedidorRequest request, Callback<BuscarMedidorResponse> callBack){
//        Call<BuscarMedidorResponse> call = service.buscarMedidor(request);
//
//        call.enqueue(callBack);
//    }
//
//    public void operacionGenerica(OperacionGenericaRequest request, Callback<OperacionGenericaResponse> callBack){
//        Call<OperacionGenericaResponse> call = service.operacionGenerica(request);
//
//        call.enqueue(callBack);
//    }

    public void registrarPuntoGpsAsync(PuntoGpsRequest request, Callback<PuntoGpsResponse> callBack){
        Call<PuntoGpsResponse> call = service.registrarPuntoGPS(request);

        call.enqueue(callBack);
    }

    public PuntoGpsResponse registrarPuntoGps(PuntoGpsRequest request) throws Exception{
        Call<PuntoGpsResponse> call = service.registrarPuntoGPS(request);

        Response<PuntoGpsResponse> resp = call.execute();

        return resp.body();
    }

    public SubirFotoResponse subirFoto(SubirFotoRequest req, byte[] foto) throws Exception {



        try {

            // RequestBody fileRequestBody = RequestBody.create(MediaType.parse("image/jpeg"), foto);

            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", req.nombre, RequestBody.create(MediaType.parse("image/*"), foto));

            RequestBody ruta = RequestBody.create(MediaType.parse("text/plain"), req.ruta);
            RequestBody carpeta = RequestBody.create(MediaType.parse("text/plain"), req.carpeta);
            RequestBody nombreArchivo = RequestBody.create(MediaType.parse("text/plain"), req.nombre);
            RequestBody serieMedidor = RequestBody.create(MediaType.parse("text/plain"), req.serieMedidor);
            RequestBody idOrden= RequestBody.create(MediaType.parse("text/plain"), String.valueOf(req.idOrden));

            Call<SubirFotoResponse> call = service.subirFoto(filePart, ruta, carpeta, nombreArchivo, serieMedidor, idOrden);

            Response<SubirFotoResponse> resp = call.execute();

            return resp.body();
        } catch (Throwable t)
        {
            throw new Exception("Error al subir foto: " + t.getMessage());
        }
    }

//    public SubirFotoResponse subirFoto(SubirFotoRequest req, byte[] foto) throws Exception {
//
//
//
//        try {
//
//            RequestBody fileRequestBody = RequestBody.create(MediaType.parse("image/jpeg"), foto);
//            //MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", req.nombre, RequestBody.create(MediaType.parse("image/*"), foto));
//
//            Call<SubirFotoResponse> call = service.subirFoto(req, fileRequestBody);
//
//            Response<SubirFotoResponse> resp = call.execute();
//
//            return resp.body();
//        } catch (Throwable t)
//        {
//            throw new Exception("Error al subir foto: " + t.getMessage());
//        }
//    }

//    public String echoPing(){
//        try {
//            String resultado;
//
//            Call<String> echoPingCall = service.echoping();
//
//            Response<String> respuesta = echoPingCall.execute();
//
//            return respuesta.body();
//        }
//        catch (Exception ex ){
//            return "";
//        }
//    }
}
