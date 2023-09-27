package enruta.soges_engie.interfaces;

import enruta.soges_engie.clases.OperacionRequest;
import enruta.soges_engie.clases.OperacionResponse;
import enruta.soges_engie.entities.SubirDatosRequest;
import enruta.soges_engie.entities.SubirDatosResponse;
import enruta.soges_engie.entities.SubirFotoResponse;
import enruta.soges_engie.entities.TareasRequest;
import enruta.soges_engie.entities.TareasResponse;
import enruta.soges_engie.entities.LoginRequestEntity;
import enruta.soges_engie.entities.LoginResponseEntity;
import enruta.soges_engie.entities.PuntoGpsRequest;
import enruta.soges_engie.entities.PuntoGpsResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface IWebApi {
    @GET("api/login/echoping")
    Call<String>echoping();

    @POST("api/loginv2/autenticarEmpleado")
    Call<LoginResponseEntity>autenticarEmpleado(@Body LoginRequestEntity loginRequestEntity);

    @POST("api/loginv2/validarEmpleadoSMS")
    Call<LoginResponseEntity>validarEmpleadoSMS(@Body LoginRequestEntity loginRequestEntity);

//    @GET("autenticarEmpleado.aspx")
//    Call<LoginResponseEntity>autenticarEmpleado2(@Query("usuario") String usuario, @Query("password") String password);
//
//    @GET("validarEmpleadoSMS.aspx")
//    Call<LoginResponseEntity>validarEmpleadoSMS2(@Query("usuario") String usuario, @Query("codigosms") String codigoSMS);

   @POST("api/operaciones/CheckIn")
    Call<OperacionResponse>checkIn(@Body OperacionRequest request);

    @POST("api/operaciones/CheckSeguridad")
    Call<OperacionResponse>checkSeguridad(@Body OperacionRequest request);

    @POST("api/operaciones/CheckOut")
    Call<OperacionResponse>checkOut(@Body OperacionRequest request);
//
//    @POST("api/operaciones/CerrarArchivo")
//    Call<OperacionResponse>cerrarArchivo(@Body OperacionRequest request);

    @POST("api/operaciones/SolicitarAyuda2")
    Call<OperacionResponse>solicitarAyuda(@Body OperacionRequest request);

//    @POST("api/operaciones/marcarArchivoDescargado")
//    Call<OperacionResponse>marcarArchivoDescargado(@Body OperacionRequest request);

    @POST("api/operaciones/marcarArchivoTareasDescargado")
    Call<TareasResponse> marcarArchivoTareasDescargado(@Body TareasRequest request);

    @POST("api/operaciones/DescargarTareas")
    Call<TareasResponse> descargarTareas(@Body TareasRequest request);

//    @POST("api/operaciones/marcarArchivoTerminado")
//    Call<ArchivosLectResponse>marcarArchivoTerminado(@Body ArchivosLectRequest request);

    @POST("api/loginv2/verificarConexion")
    Call<LoginResponseEntity>verificarConexion(@Body LoginRequestEntity request);

//    @POST("api/supervisor/RegistrarLog")
//    Call<SupervisorLogResponse>registrarLogSupervisor(@Body SupervisorLogRequest request);
//
//    @POST("api/operaciones/BuscarMedidor")
//    Call<BuscarMedidorResponse> buscarMedidor(@Body BuscarMedidorRequest request);
//
//    @POST("api/operaciones/OperacionGenerica")
//    Call<OperacionGenericaResponse> operacionGenerica(@Body OperacionGenericaRequest request);

    @POST("api/operaciones/RegistrarPuntoGPS")
    Call<PuntoGpsResponse> registrarPuntoGPS(@Body PuntoGpsRequest request);

//    @Headers({ "Content-Type: application/json;charset=UTF-8"})
//    @POST("api/operaciones/subirFoto2")
//    Call<SubirFotoResponse> subirFoto(@Body SubirFotoRequest req, @Part("file\"; filename=\"file.jpg\" ") RequestBody file);

//    @Multipart
//    @POST("api/operaciones/subirFoto2")
//    Call<SubirFotoResponse> subirFoto(@Part("file\"; filename=\"file.jpg\" ") RequestBody file, @Part("ruta") RequestBody ruta, @Part("carpeta") RequestBody carpeta,
//                                      @Part("nombreArchivo") RequestBody nombreArchivo);

    @Multipart
    @POST("api/operaciones/subirFoto2")
    Call<SubirFotoResponse> subirFoto(@Part MultipartBody.Part file, @Part("ruta") RequestBody ruta,
                                      @Part("carpeta") RequestBody carpeta,
                                      @Part("nombreArchivo") RequestBody nombreArchivo,
                                      @Part("serieMedidor") RequestBody serieMedidor,
                                      @Part("idOrden") RequestBody idOrden);

    @POST("api/operaciones/SubirDatos")
    Call<SubirDatosResponse> subirDatos(@Body SubirDatosRequest request);

}
