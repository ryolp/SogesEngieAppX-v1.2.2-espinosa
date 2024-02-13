package enruta.soges_engie.clases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import enruta.soges_engie.DBHelper;
import enruta.soges_engie.Globales;
import enruta.soges_engie.Lectura;
import enruta.soges_engie.Main;
import enruta.soges_engie.R;
import enruta.soges_engie.TodasLasLecturas;
import enruta.soges_engie.entities.OrdenEntity;
import enruta.soges_engie.entities.ResumenEntity;
import enruta.soges_engie.entities.TareasRequest;
import enruta.soges_engie.entities.TareasResponse;
import enruta.soges_engie.services.DbLecturasMgr;
import enruta.soges_engie.services.WebApiManager;

/*
    DescargarLecturasProceso().

    Clase que agrupa el proceso que lee las lecturas recibidas del servidor y las registra
    en la base de datos del celular.
    La instancia de esta clase se ejecuta en un thread diferente al principal.
*/

public class DescargarTareasMgr implements Runnable {
    protected final int EXITO = 0;
    protected final int FALTA = 1;
    protected final int ERROR_ENVIAR_1 = 2;
    protected final int ERROR_ENVIAR_2 = 3;
    protected final int ERROR_ENVIAR_3 = 4;
    protected final int ERROR_ENVIAR_4 = 5;

    private TareasRequest mRequest;
    protected Context mContext = null;
    protected TodasLasLecturas mTll;
    protected Vector<Lectura> mLecturas;
    protected Handler mHandler = null;
    protected DBHelper mDbHelper = null;
    protected SQLiteDatabase mDb = null;
    protected Globales mGlobales = null;
    protected ArchivosTareasMgr mArchivosTareasMgr = null;
    protected DbLecturasMgr mDbLectMgr = null;
    protected DescargarTareasMgr.EnNotificaciones mNotificaciones = null;

    /*
        EnNotificaciones().

        Interface para el envío de las notificaciones al proceso que llama a la instancia de esta clase.
    */
    public interface EnNotificaciones {
        public void enMensaje(String mensaje);

        public void enProgreso(String progreso, int porcentaje);

        public void enError(String mensajeError, String detalleError);

        public void enFinalizado(boolean exito);
    }

    /*
        DescargarLecturasProceso().

        Constructor de la clase.
    */

    public DescargarTareasMgr(Context context, Globales globales) {
        mContext = context;
        mGlobales = globales;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }
    public void setDatabase(SQLiteDatabase db) {
        mDb = db;
    }

    /*
        setEnNotificaciones().

        Metodo para guardar una referencia al método del proceso que llama a la instancia de esta clase,
        el cual recibirá las notificaciones de este proceso.
    */

    public void setEnNotificaciones(DescargarTareasMgr.EnNotificaciones progreso) {
        mNotificaciones = progreso;
    }

    /*
        run().

        Entrada principal para que un thread ejecute el proceso definido en esta clase.
    */

    public void run() {
        boolean resultado;
        TareasResponse resp;

        try {
            notificarMensaje("Descargando tareas...");
            resp = descargarTareas();

            if (resp != null) {
                notificarMensaje("Procesando tareas recibidas ...");
                procesarTareas(resp.Contenido);
                //           procesarOrdenes(mContenido);
//
                notificarFinalizado(true);
            }
            else
                notificarFinalizado(false);
        } catch (AppException e1) {
            notificarError(e1.getMessage(), "");
            notificarFinalizado(false);
        } catch (Throwable e2) {
            notificarError("Error inesperado", e2.getMessage());
            notificarFinalizado(false);
        }
    }

    public boolean puedoCargar() {
        boolean puedo = true;
//        openDatabase();
//
//        Cursor c = mDb.rawQuery("Select descargada from encabezado", null);
//
//        c.moveToFirst();
//
//        if (c.getCount() > 0) {
//            if (getInt(c, "descargada", 0) == 0) {
//                //Si no ha sido descargada, habrá que verificar si ya hay una lectura ingresada
//                c.close();
//                c = mDb.rawQuery("Select count(*) canti from Ruta where trim(tipoLectura)<>''"
//                        + "order by cast(secuencia as Integer) asc limit 1", null);
//                c.moveToFirst();
//                puedo = getInt(c, "canti", 0) == 0;
//            } else
//                puedo = true;
//        } else
//            puedo = true;
//
//        c.close();
//        closeDatabase();

        return puedo;
    }

   /*
        descargarLecturas().

        Método principal para iniciar el proceso para descargar las lecturas del servidor, registrarlas en la
        base de datos del celular y notificar al thread principal de los eventos y avances.
    */

    public TareasResponse descargarTareas() {
        List<Long> listadoIdTareas;
        mRequest = new TareasRequest();
        mRequest.idEmpleado = mGlobales.getIdEmpleado();
        mRequest.FechaOperacion = Utils.getDateTime();
        TareasResponse resp;

        try {
            listadoIdTareas = obtenerIdsTareas();

            mRequest.listadoIdsTareas = listadoIdTareas;

            resp = WebApiManager.getInstance(mContext).descargarTareas(mRequest);
            return resp;
        } catch (Throwable t) {
            notificarError("No hay conexión a internet. Intente nuevamente.", t);
            return null;
        }
    }

    /*
    * obtenerIdsTareas().
    *
    * Función que obtiene los Ids de todas las tareas registradas.
    */
    private ArrayList<Long> obtenerIdsTareas() {
        ArrayList<Long> listado = new ArrayList<>();
        Cursor c = null;

        try {
            String query;
            int cantidad;
            Long idTarea;

            query = "SELECT idTarea FROM Ruta GROUP BY idTarea";

            c = mDb.rawQuery(query, null);

            cantidad = c.getCount();

            while (c.moveToNext()) {
                idTarea = Utils.getLong(c, "idTarea", 0);
                listado.add(idTarea);
            }
        } catch (Throwable t) {
            Log.e("SOGES", t.getMessage());
        }
        finally {
            if (c != null)
                c.close();
            return listado;
        }
    }

    /*
        notificarMensaje().

        Función para notificar al thread principal de un evento.
    */

    private void notificarMensaje(String mensaje) {
        if (mNotificaciones != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mNotificaciones.enMensaje(mensaje);
                }
            });
        }
    }

    /*
        notificarProgreso().

        Función para notificar al thread principal del avance de la ejecución del proceso de descargar...
        ... las lecturas del servidor.
    */

    private void notificarProgreso(int valorActual, int cantidadTotal) {

        if (mNotificaciones != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int porcentaje;
                    String mensajeProgreso;

                    if (cantidadTotal > 0)
                        porcentaje = (valorActual * 100) / cantidadTotal;
                    else
                        porcentaje = 0;

                    mensajeProgreso = valorActual + " " + mContext.getString(R.string.de) + " " + cantidadTotal
                            + " " + mContext.getString(R.string.registros) + "\n"
                            + String.valueOf(porcentaje) + "%";

                    mNotificaciones.enProgreso(mensajeProgreso, porcentaje);
                }
            });
        }
    }

    /*
        notificarError().
        Función para notificar al thread principal de un error.
    */

    private void notificarError(String mensaje, String detalleError) {
        if (mNotificaciones != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mNotificaciones.enError(mensaje, detalleError);
                }
            });
        }
    }

    private void notificarError(String mensaje, Throwable t) {
        if (mNotificaciones != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mNotificaciones.enError(mensaje, t.getMessage());
                }
            });
        }
    }

    private void notificarFinalizado(boolean exito) {
        ResumenEntity resumen;

        resumen = DbLecturasMgr.getInstance().getResumen(mContext);

        if (mNotificaciones != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mNotificaciones.enFinalizado(exito);
                }
            });
        }
    }


    /*
        openDatabase().
        Función para abrir la base de datos.
    */
    private void openDatabase() {
        if (mDbHelper == null)
            mDbHelper = new DBHelper(mContext);

        if (mDb == null)
            mDb = mDbHelper.getReadableDatabase();
    }

    /*
        closeDatabase().
        Función para cerrar la base de datos.
    */
    private void closeDatabase() {
        if (mDb != null)
            if (mDb.isOpen()) {
                mDb.close();
                mDbHelper.close();
            }
    }

    /*
        procesarLecturas().

        Este método recibe un string con el listado de lecturas. El string está separado en renglones por un saldo de línea. Cada renglón está separado...
         ... en columnas por un pipe (|). Registra en la base de datos las lecturas recibidas y los parámetros.
         Los renglones que empiezan con L son las lecturas.
         Los renglones que empiezan con P con los parametros.
         Los renglones que empiezan con # son el catálogo de anomalías.
     */

    private void procesarTareas(List<String> contenido) throws Exception {
        Vector<String> vLecturas;
        String[] lineas;
        int i = 0;
        int secuenciaReal = 0;
        long idArchivo;
        int cantRegistros = 0;
        String mensajeProgreso;
        boolean resultado = false;
        String linea;
        OrdenEntity orden;

        openDatabase();

        vLecturas = new Vector<String>();

        try {
            borrarRuta(mDb);

            mDb.beginTransaction();

            cantRegistros = contenido.size();

            for (i = 0; i < cantRegistros; i++) {
                linea = contenido.get(i);

                if (linea.startsWith("O")) {
                    secuenciaReal++;
                    orden = convToOrden(linea);
                    agregarRegistro(orden);
                }

                i++;
                notificarProgreso(i, cantRegistros);
            }

            notificarProgreso(i, cantRegistros);

//            mGlobales.tdlg.AgregarAnomaliasManualmente(mDb);
//
//            mGlobales.tdlg.accionesDespuesDeCargarArchivo(mDb);

            mDb.setTransactionSuccessful();

            mDb.endTransaction();

            closeDatabase();

            resultado = true;

            notificarMensaje("Descarga finalizada");
        } catch (AppException e1) {
            mDb.execSQL("delete from Lecturas ");
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
            closeDatabase();
            throw new AppException(e1.getMessage());
        } catch (Exception e2) {
            mDb.execSQL("delete from Lecturas ");
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
            closeDatabase();
            throw e2;
        }
    }

    private void inicializarParams(ContentValues cv_params) {
        cv_params.put("sospechosa", "0");
        cv_params.put("intento1", "");
        cv_params.put("intento2", "");
        cv_params.put("intento3", "");
        cv_params.put("intento4", "");
        cv_params.put("intento5", "");
        cv_params.put("intento6", "");
        cv_params.put("intentos", 0);
        cv_params.put("nisRad", 0);
        cv_params.put("dondeEsta", "");
        cv_params.put("anomInst", "");
        cv_params.put("sectorCorto", "");
        cv_params.put("sectorLargo", "");
        cv_params.put("comoLlegar2", "");
        cv_params.put("lectura", "");
        cv_params.put("anomalia", "");
        cv_params.put("subanomalia", "");
        cv_params.put("comentarios", "");
    }

// CE, 24/10/23, Esta rutina la reescribió Reynol
    public void borrarRegistro(OrdenEntity orden) throws Exception {
// CE, 22/10/23, Hay que escribir esta rutina para borrar a los que ya pagaron
        try {
            mDb.execSQL("delete from ruta where CAST(idOrden as INTEGER) = CAST(" + String.valueOf(orden.idOrden) + " as INTEGER)");
            mGlobales.bPrenderCampana = true;
        } catch (Throwable t) {
            throw new Exception("Error al borrar registro. " + t.getMessage());
        }
    }

    public void agregarRegistro(OrdenEntity orden) throws Exception {
        ContentValues cv_params = new ContentValues();
        Cursor c;
        long secuenciaReal;
        long idOrden;
        long id;
        try {
            // Obtener la última secuencia añadida.
            c = mDb.rawQuery("Select secuenciaReal from ruta order by secuenciaReal desc limit 1", null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                secuenciaReal = Utils.getInt(c, "secuenciaReal", 0) + 1;
            }
            else
                secuenciaReal = 1;

            // Buscar si ya existe una orden con el mismo idOrden

            c = mDb.rawQuery("Select idOrden, MensajeOut, vencido, tipoDeOrden, balance from ruta WHERE CAST(idOrden as INTEGER) = CAST(? as INTEGER) limit 1", new String [] {String.valueOf(orden.idOrden)});

            if (c.getCount() > 0) {
                c.moveToFirst();
                idOrden = Utils.getInt(c, "idOrden", 0);
            }
            else
                idOrden = 0;
            inicializarParams(cv_params);
            if (idOrden != 0) {
//                c = mDb.rawQuery("Select idOrden from ruta WHERE (NOT (MensajeOut LIKE '" + orden.MensajeOut + "')) AND CAST(idOrden as INTEGER) = CAST(? as INTEGER) limit 1", new String [] {String.valueOf(orden.idOrden)});
//                if (c.getCount() > 0) {
                String strMensajeApp = Utils.getString(c, "MensajeOut", "");
                String strVencidoApp = Utils.getString(c, "vencido", "");
                String strTipoDeOrden = Utils.getString(c, "tipoDeOrden", "");
                String strBalanceAnterior = Utils.getString(c, "balance", "");
                String strBalance = "3";
// CE, 06/12/23, Vamos a marcar las que llegaron como nuevas
//                String strEnvio = Utils.getString(c, "envio", "");
//                if ((!strEnvio.equals(orden.envio)))
//                    strBalance = "2";
                if ((!strMensajeApp.equals(orden.MensajeOut)) || (!strVencidoApp.equals(orden.Vencido))) {
                    mDb.execSQL("update ruta set MensajeOut='" + orden.MensajeOut + "', vencido='" + orden.Vencido + "', balance='" + strBalance + "' where CAST(idOrden as INTEGER) = CAST(" + String.valueOf(orden.idOrden) + " as INTEGER)");
                    mGlobales.bPrenderCampana = true;
// CE, 02/02/24, Vamos a marcar las que no tuvieron cambio, para borrar al final todas las que no se hayan vuelto a recibir
                } else {
                    if (strTipoDeOrden.equals("TO006"))
                        strBalance = "1";
                    else {
                        if (strBalanceAnterior.equals(""))
                           strBalance = "4";
                        else
                            strBalance = strBalanceAnterior;
                    }
                    mDb.execSQL("update ruta set balance='" + strBalance + "' where CAST(idOrden as INTEGER) = CAST(" + String.valueOf(orden.idOrden) + " as INTEGER)");
                }
//                }
            } else {
                String strBalance = "1";
//                mGlobales.bPrenderCampana = true;
                cv_params.put("indicador", orden.Indicador);
                cv_params.put("secuenciaReal", secuenciaReal);
                cv_params.put("numOrden", orden.NumOrden);
                cv_params.put("idOrden", orden.idOrden);
                cv_params.put("idArchivo", orden.idArchivo);
                cv_params.put("idTarea", orden.idTarea);
                cv_params.put("idEmpleado", orden.idEmpleado);
                cv_params.put("ciclo", orden.Ciclo);
                cv_params.put("NumGrupo", orden.NumGrupo);
                cv_params.put("poliza", orden.Poliza);
                cv_params.put("cliente", orden.Cliente);
                cv_params.put("calle", orden.Calle);
                cv_params.put("numPortal", orden.NumInterior);
                cv_params.put("piso", "");
                cv_params.put("numEdificio", orden.NumExterior);
                cv_params.put("colonia", orden.Colonia);
                cv_params.put("municipio", orden.Municipio);
                cv_params.put("entrecalles", orden.EntreCalles);
                cv_params.put("comoLlegar1", orden.ComoLlegar);
                cv_params.put("aviso", orden.AvisoAlLector);
                cv_params.put("marcaMedidor", orden.MarcaMedidor);
                cv_params.put("tipoMedidor", orden.TipoMedidor);
                cv_params.put("estadoDelSuministro", orden.EstadoDelServicio);
                cv_params.put("tarifa", orden.Tarifa);
                cv_params.put("tipoDeOrden", orden.TipoDeOrden);
//        cv_params.put("TimeOfLife", orden.TimeOfLife );
//        cv_params.put("MedTimeOfLife", orden.MedTimeOfLife );
                cv_params.put("FechaDeAsignacion", orden.FechaDeAsignacion);
                cv_params.put("fechaDeRecepcion", Utils.getDateTimeStr("yyyyMMddHHmmss"));
//            cv_params.put("NumSello", orden.NumSello);
//            cv_params.put("Anio", orden.Anio);
                cv_params.put("consumo", "");
                cv_params.put("SerieMedidor", orden.SerieMedidor);
                cv_params.put("vencido", orden.Vencido);
// CE, 06/12/23, Vamos a marcar las que llegaron como nuevas
//                cv_params.put("balance", orden.Balance );
                cv_params.put("balance", strBalance );
                cv_params.put("ultimo_pago", orden.UltimoPago);
                cv_params.put("fecha_utlimo_pago", orden.FechaUltimoPago);
                cv_params.put("giro", orden.Giro);
                cv_params.put("diametro", orden.DiametroToma);
//************************************************************************************************************************************
// CE, 06/10/23, Aqui vamos a agregar solamente los CamposEngie que vienen en el archivo que descargamos del servidor
                cv_params.put("miLatitud", orden.miLatitud);
                cv_params.put("miLongitud", orden.miLongitud);
                cv_params.put("NumAviso", orden.NumAviso);
                cv_params.put("CuentaContrato", orden.CuentaContrato);
                cv_params.put("idMaterialSolicitado", orden.idMaterialSolicitado);
//                cv_params.put("CancelarEnApp", orden.CancelarEnApp);
                cv_params.put("TextoLibreSAP", orden.TextoLibreSAP);
                cv_params.put("MensajeOut", orden.MensajeOut);
//************************************************************************************************************************************

                orden.id = mDb.insertOrThrow("ruta", null, cv_params);
            }
        } catch (Throwable t) {
            throw new Exception("Error al agregar registro. " + t.getMessage());
        }
    }

    public void borrarRegistro2(OrdenEntity orden) throws Exception {
        Cursor c;
        long secuenciaReal;
        long nCant;

        try {
            // Buscar si ya existe una orden con el mismo idOrden

            c = mDb.rawQuery("Select idOrden from ruta WHERE CAST(idOrden as INTEGER) = CAST(? as INTEGER) limit 1", new String [] {String.valueOf(orden.idOrden)});

            if (c.getCount() > 0) {
                c.moveToFirst();

                if (orden.idOrden > 0)
                {
                    String whereClause="idOrden=?";
                    String whereArgs[] = { String.valueOf(orden.idOrden) };

                    nCant = mDb.delete("ruta", whereClause, whereArgs);
                }
            }
        } catch (Throwable t) {
            throw new Exception("Error al borrar registro. " + t.getMessage());
        }
    }

    public void borrarTodo() throws Exception {
        try {
            mDb.execSQL("delete from ruta");
        } catch (Throwable t) {
            throw new Exception("Error al borrar todos los registros. " + t.getMessage());
        }
    }

    /*
        borrarRuta().

        Este método borra el contenido de varias tablas para que puedan recibir la información que descarga.
     */
    private static void borrarRuta(SQLiteDatabase db) {
        db.execSQL("delete from ruta ");
        db.execSQL("delete from fotos ");
        db.execSQL("delete from Anomalia ");
        db.execSQL("delete from encabezado ");
        db.execSQL("delete from NoRegistrados ");
        db.execSQL("delete from usuarios ");
    }

    /*
        actualizarParametros().

        Este método actualiza la tabla que contiene la información de los parámetros.
    */
//    private void actualizarParametros(String linea) {
//        DbConfigMgr.getInstance().actualizarParametros(mDb, linea);
//    }

    public void finalize() {
        closeDatabase();
    }

    public OrdenEntity convToOrden(String linea) throws Exception {
        String[] campos;
        OrdenEntity orden = new OrdenEntity();
        int numCampos;
        int i;

        campos = linea.split("\\|", -1);

        if (campos == null)
            throw new Exception("Cantidad incorrecta de campos");

        if (campos.length < 2)
            throw new Exception("Cantidad incorrecta de campos");

        numCampos = Utils.convToInt(campos[1]);

        if (campos.length < numCampos)
            throw new Exception("Cantidad incorrecta de campos");

        try {
            i = 2;
            orden.Indicador = campos[i++];
            orden.idOrden = Utils.convToLong(campos[i++]);
            orden.Poliza = campos[i++];
            orden.Cliente = campos[i++];
            orden.SerieMedidor = campos[i++];
            orden.Calle = campos[i++];
            orden.NumInterior = campos[i++];
            orden.NumExterior = campos[i++];
            orden.Colonia = campos[i++];
            orden.Municipio = campos[i++];
            orden.EntreCalles = campos[i++];
            orden.ComoLlegar = campos[i++];
            orden.TipoDeOrden = campos[i++];
            orden.TimeOfLife = campos[i++];
            orden.MedTimeOfLife = campos[i++];
            orden.FechaDeAsignacion = campos[i++];
            orden.Anio = campos[i++];
            orden.Vencido = campos[i++];
            orden.Balance = campos[i++];
            orden.UltimoPago = campos[i++];
            orden.FechaUltimoPago = campos[i++];
            orden.Giro = campos[i++];
            orden.DiametroToma = campos[i++];
            orden.idArchivo = Utils.convToLong(campos[i++]);
            orden.idTarea = Utils.convToLong(campos[i++]);
            orden.idEmpleado = Utils.convToLong(campos[i++]);
            orden.NumGrupo = campos[i++];
            orden.Ciclo = campos[i++];
            orden.NumSecuencia = Utils.convToInt(campos[i++]);
            orden.AvisoAlLector = campos[i++];
            orden.MarcaMedidor = campos[i++];
            orden.TipoMedidor = campos[i++];
            orden.EstadoDelServicio = campos[i++];
            orden.Tarifa = campos[i++];
            orden.NumSello = campos[i++];
//************************************************************************************************************************************
// CE, 06/10/23, Aqui vamos a agregar solamente los CamposEngie que vienen en el archivo que descargamos del servidor
            orden.MensajeOut = campos[i++];
            orden.miLatitud = campos[i++];
            orden.miLongitud = campos[i++];
            orden.NumAviso = campos[i++];
            orden.CuentaContrato = campos[i++];
            orden.idMaterialSolicitado = campos[i++];
            orden.CancelarEnApp = campos[i++];
            orden.TextoLibreSAP = campos[i];
// *** MUY IMPORTANTE: El ultimo campo no debe llevar el ++ ****
//************************************************************************************************************************************

            orden.NumOrden = String.valueOf(orden.idOrden);
        } catch (Throwable t) {
            throw new Exception("Error al obtener los valores de los campos. " + t.getMessage());
        }

        return orden;
    }
}
