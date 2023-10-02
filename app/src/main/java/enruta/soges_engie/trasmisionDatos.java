package enruta.soges_engie;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import enruta.soges_engie.clases.AppUsuarioBloqueadoException;
import enruta.soges_engie.clases.DescargarTareasMgr;
import enruta.soges_engie.clases.FotosMgr;
import enruta.soges_engie.clases.Utils;
import enruta.soges_engie.entities.DatosEnvioEntity;
import enruta.soges_engie.entities.OrdenEntity;
import enruta.soges_engie.entities.SubirDatosRequest;
import enruta.soges_engie.entities.SubirDatosResponse;
import enruta.soges_engie.entities.SubirFotoRequest;
import enruta.soges_engie.entities.SubirFotoResponse;
import enruta.soges_engie.entities.TareasResponse;
import enruta.soges_engie.services.WebApiManager;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class trasmisionDatos extends TransmisionesPadre {
    private Serializacion serial;
    private FotosMgr fotoMgr = null;
    private DescargarTareasMgr mDescargarTareas;
    private DialogoMensaje mDialogoMsg = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enproceso);
        String[] ls_params;

        setTitle("");
        globales = ((Globales) getApplicationContext());

        mDescargarTareas = new DescargarTareasMgr(this, globales);

        tll = new TodasLasLecturas(this, false);
        resources = this.getResources();
        bu_params = getIntent().getExtras();

        try {
            transmiteFotos = bu_params.getBoolean("transmiteFotos");
        } catch (Throwable e) {
        }

        openDatabase();

        // Tomamos el servidor desde la pantalla de configuracion
        Cursor c = db.rawQuery(
                "Select value from config where key='server_gprs'", null);
        c.moveToFirst();

        if (!validaCampoDeConfig(c, String.format(
                getString(R.string.msj_config_no_disponible),
                getString(R.string.info_servidorGPRS),
                getString(R.string.str_configuracion),
                getString(R.string.info_servidorGPRS))))
            return;

        ls_servidor = c.getString(c.getColumnIndex("value"));

        c.close();
        // Ahora vamos a ver que archivo es el que vamos a recibir... para
        // nicaragua es el clp + la extension
        // Lo vamos a guardar en categoria, Asi le llamamos a los archivos desde
        // "SuperLibretaDeDirecciones" 2013 (c) ;)

//		c = db.rawQuery("Select value from config where key='cpl'", null);
//
//		c.moveToFirst();
//		if (!validaCampoDeConfig(c, String.format(
//				getString(R.string.msj_config_no_disponible),
//				getString(R.string.info_CPL),
//				getString(R.string.str_configuracion),
//				getString(R.string.info_CPL))))
//			return;
//
//		ls_categoria = c.getString(c.getColumnIndex("value")) + "."
//				+ ls_extension;
//
//		c.close();

        // Por ultimo la ruta de descarga... Como es un servidor web, hay que
        // quitarle el C:\... mejor empezamos desde lo que sigue. De cualquier
        // manera, deberá tener el siguiente formato
        // Ruta de descarga.subtr(3) + Entrada + \ + lote
        c = db.rawQuery("Select value from config where key='ruta_descarga'",
                null);
        c.moveToFirst();
        if (!validaCampoDeConfig(c, String.format(
                getString(R.string.msj_config_no_disponible),
                getString(R.string.info_rutaDescarga),
                getString(R.string.str_configuracion),
                getString(R.string.info_rutaDescarga))))
            return;

        ls_carpeta = c.getString(c.getColumnIndex("value"));

        if (ls_carpeta.indexOf(":") >= 0) {
            ls_carpeta = ls_carpeta.substring(ls_carpeta.indexOf(":") + 2);
        }

        c.close();

        if (ls_carpeta.endsWith("\\"))
            ls_carpeta = ls_carpeta.substring(0, ls_carpeta.length() - 1);

        ls_carpeta += bu_params.getInt("tipo") == TRANSMISION ? "\\in"
                : bu_params.getInt("tipo") == RECEPCION ? "\\out" : "\\CFG";


        if (bu_params.getInt("tipo") == TRANSMISION || bu_params.getInt("tipo") == RECEPCION) {
            ls_carpeta += "\\ordenes";
        }
//		c = db.rawQuery("Select value from config where key='lote'", null);
//		c.moveToFirst();
//		if (!validaCampoDeConfig(c, String.format(
//				getString(R.string.msj_config_no_disponible),
//				getString(R.string.info_lote),
//				getString(R.string.str_configuracion),
//				getString(R.string.info_lote))))
//			return;
//
//		ls_subCarpeta = c.getString(c.getColumnIndex("value"));
//
//		ls_carpeta += "\\" + ls_subCarpeta;
//
//		c.close();

        closeDatabase();

        tv_progreso = (TextView) findViewById(R.id.ep_tv_progreso);
        tv_indicador = (TextView) findViewById(R.id.ep_tv_indicador);
        pb_progress = (ProgressBar) findViewById(R.id.ep_gauge);

        mHandler = new Handler();

        if (mDialogoMsg == null) {
            mDialogoMsg = new DialogoMensaje(this);
        }


        //Vamos a verificar la hora y la fecha

        final trasmisionDatos td = this;

        Thread thread = new Thread() {
            public void run() {
                String fechaServidor = getFechaHoraServidor();
                String fechaActual = Main.obtieneFecha("ymdhis");
                SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
                DescargarTareasMgr mDescargarTareas;

                try {
                    Date d_fechaActual = f.parse(fechaActual);
                    Date d_fechaServidor = f.parse(fechaServidor);


//					if ( d_fechaActual.getTime()< d_fechaServidor.getTime() - 900000  || d_fechaActual.getTime()> d_fechaServidor.getTime() + 900000 ){
//						//Esa fecha no...
////						Intent intent=new Intent();
////						intent.setComponent(new ComponentName("com.android.settings",
////						         "com.android.settings.DateTimeSettingsSetupWizard"));
//
//						mHandler.post(new Runnable() {
//							public void run() {
//								AlertDialog.Builder builder = new AlertDialog.Builder(td);
//								builder.setMessage("La hora y la fecha se encuentran desfazadas, ingrese la hora y fecha correspondientes e intente cargar ordenes de nuevo.")
//								       .setCancelable(false)
//								       .setNegativeButton(R.string.aceptar, new DialogInterface.OnClickListener() {
//								           public void onClick(DialogInterface dialog, int id){
//								        	   startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
//								        	   muere(true, "");
//								                dialog.cancel();
//								           }
//								       });
//
//								AlertDialog alert = builder.create();
//								alert.show();
//
//
//							}
//						});
//
////						startActivity(intent);
//
//					}
//					else{
                    seleccion();
//					}

                    //seleccion();

                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //long milliseconds = d.getTime();

//				if ()
            }
        };

        thread.start();


    }

    public void transmitir() {
        final trasmisionDatos context = this;
        hilo = new Thread() {
            int cantidad;


            public void run() {
                ContentValues cv_datos = new ContentValues(1);
                // TODO Auto-generated method stub
                serial = new Serializacion(Serializacion.WIFI);
                String ls_cadena = "";
                String datos = "";
                String ls_cadenaAEnviar = "";
                byte[] lby_registro, lby_cadenaEnBytes;
                String ls_nombre_final;
                String nombreArchivo = "";
                String query = "";
                byte[] image;
                String serieMedidor = "";
                long idOrden = 0;

                List<Long> listadoOrdenes = new ArrayList<Long>();
                long idRegistro = 0L;

                GeneradorDatosEnvio genDatosEnvio = new GeneradorDatosEnvio();

                switch (globales.modoDeCierreDeLecturas) {
                    case Globales.FORZADO:
                        puedoCerrar = false;
                        mostrarMensaje(PROGRESO, getString(R.string.msj_trans_forzando));
                        // Abrimos el arreglo de todas las lecturas y forzamos
                        tll.forzarLecturas();
                        cancelar = false;
                        break;
                }

                db.execSQL("update ruta set registro=0 where registro is null");

                puedoCerrar = true;

                // -------------------------------------------------
                //  Enviar resultado de las ordenes
                // -------------------------------------------------

                mostrarMensaje(PROGRESO, getString(R.string.str_espere));
                Cursor c = null;
                try {
                    openDatabase();

                    String[] ls_params = {String
                            .valueOf(TomaDeLecturas.NO_ENVIADA)};

//					if (globales.tlc.is_camposDeSalida.equals(""))
//						c = db.rawQuery("select * from Ruta ", null);
//					else

                    //query = "select rowid, " + globales.tlc.is_camposDeSalida + " as TextoSalida "
                    //        + "from Ruta where envio=1 ";

                    query = "select * from Ruta where envio=1";

                    c = db.rawQuery(query, null);

                    cantidad = c.getCount();
                    mHandler.post(new Runnable() {
                        public void run() {
                            pb_progress.setMax(cantidad);
                        }
                    });

                    mostrarMensaje(PROGRESO,
                            getString(R.string.msj_trans_generando));
                    mostrarMensaje(MENSAJE, getString(R.string.str_espere));

//					borrarArchivo(ls_carpeta + "/" + globales.tdlg.getNombreArchvio(TomaDeLecturasGenerica.SALIDA));
                    //serial.open(ls_servidor, ls_carpeta, globales.tdlg.getNombreArchvio(TomaDeLecturasGenerica.SALIDA),
                    //		Serializacion.ESCRITURA, 0, 0, globales.getIdEmpleado(), "", 0, context);

                    nombreArchivo = globales.tdlg.getNombreArchvio(TomaDeLecturasGenerica.SALIDA);

                    for (int i = 0; i < cantidad; i++) {
                        context.stop();
                        c.moveToPosition(i);

                        // ls_cadena=generaCadenaAEnviar(c);
                        // lby_cadenaEnBytes=ls_cadena.getBytes();

                        // Ya tenemos los datos a enviar (que emocion!) asi que
                        // hay que agregarlos a la cadena final


                        // lby_registro = Utils.getString(c, "TextoSalida", "").getBytes();

                        // for (int j=0; j<lby_cadenaEnBytes.length;j++)
                        // lby_registro[j+resources.getInteger(R.integer.POS_DATOS_TIPO_LECTURA)]=lby_cadenaEnBytes[j];


//						if (globales.tlc.is_CamposDeSalida.equals("")) {
//							ls_cadenaAEnviar = new String(c.getBlob(c
//									.getColumnIndex("registro")));
//							if (ls_cadenaAEnviar.length() > globales.tlc
//									.getLongCampo("registro"))
//								;
//							ls_cadenaAEnviar = ls_cadenaAEnviar.substring(0,
//									globales.tdlg.long_registro);
//						}else{

                        // RL, 28-09-2023, Aquí se forma la cadena con los campos ...
                        // ... que se transmitirán posteriormente.

                        ls_cadenaAEnviar += genDatosEnvio.generarInfoOrdenes(c, globales.getIdEmpleado()) + "\r\n";
                        //ls_cadenaAEnviar += Utils.getString(c, "TextoSalida", "") + "\r\n";
//						}
                        // Escribimos los bytes en el archivo
                        //serial.write(ls_cadenaAEnviar + "\r\n");

                        listadoOrdenes.add(idRegistro);


                        String bufferLenght;
                        int porcentaje = ((i + 1) * 100) / c.getCount();
                        bufferLenght = String.valueOf(c.getCount());

                        /*
                         * openDatabase();
                         *
                         * String whereClause="secuencial=?"; String[]
                         * whereArgs=
                         * {String.valueOf(c.getLong(c.getColumnIndex("secuencial"
                         * )))}; ContentValues cv_datos=new ContentValues(1);
                         *
                         * cv_datos.put("envio",TomaDeLecturas.ENVIADA);
                         *
                         * int j=db.update("lecturas", cv_datos, whereClause,
                         * whereArgs);
                         *
                         * closeDatabase();
                         */
                        // Marcar como enviada
                        mostrarMensaje(MENSAJE, (i + 1) + " " + getString(R.string.de)
                                + " " + bufferLenght + " " + getString(R.string.registros)
                                + ".\n" + String.valueOf(porcentaje) + "%");
                        mostrarMensaje(BARRA, String.valueOf(1));

                    }

                    //serial.close();

                    // RL, 28-09-2023, Aquí se transmite los datos al servidor ...
                    // ... con la cadena que se generó previamente

                    if (!enviarDatos("", nombreArchivo, ls_cadenaAEnviar))
                        throw new AppUsuarioBloqueadoException();

                    cv_datos.put("envio", TomaDeLecturas.ENVIADA);

                    db.update("ruta", cv_datos,
                            null, null);

                    c.close();

                    // ------------------------------------------------------------------------
                    // Aqui enviamos los no registrados
                    // ------------------------------------------------------------------------

//                    mostrarMensaje(
//                            PROGRESO,
//                            getString(R.string.msj_trans_generando_no_registrados));
//                    mostrarMensaje(MENSAJE, getString(R.string.str_espere));
//                    mostrarMensaje(BARRA, String.valueOf(0));
//
//                    c = db.rawQuery("select * from NoRegistrados ", null);
//
//                    ls_nombre_final = ls_subCarpeta + "." + "NVO";
//                    borrarArchivo(ls_carpeta + "/" + ls_nombre_final);
//
//                    cantidad = c.getCount();
//
//                    serial.open(ls_servidor, ls_carpeta, ls_nombre_final,
//                            Serializacion.ESCRITURA, 0, 0, globales.getIdEmpleado(), "", 0, context);

//                    mHandler.post(new Runnable() {
//                        public void run() {
//                            pb_progress.setMax(cantidad);
//                        }
//                    });
//                    for (int i = 0; i < cantidad; i++) {
//                        context.stop();
//                        c.moveToPosition(i);
//
//                        // ls_cadena=generaCadenaAEnviar(c);
//                        // lby_cadenaEnBytes=ls_cadena.getBytes();
//
//                        // Ya tenemos los datos a enviar (que emocion!) asi que
//                        // hay que agregarlos a la cadena final
//
//                        lby_registro = c.getBlob(c.getColumnIndex("poliza"));
//
//                        // for (int j=0; j<lby_cadenaEnBytes.length;j++)
//                        // lby_registro[j+resources.getInteger(R.integer.POS_DATOS_TIPO_LECTURA)]=lby_cadenaEnBytes[j];
//
//                        // Escribimos los bytes en el archivo
//                        serial.write(new String(lby_registro) + "\r\n");
//
//                        String bufferLenght;
//                        int porcentaje = ((i + 1) * 100) / c.getCount();
//                        bufferLenght = String.valueOf(c.getCount());
//
//                        /*
//                         * openDatabase();
//                         *
//                         * String whereClause="secuencial=?"; String[]
//                         * whereArgs=
//                         * {String.valueOf(c.getLong(c.getColumnIndex("secuencial"
//                         * )))}; ContentValues cv_datos=new ContentValues(1);
//                         *
//                         * cv_datos.put("envio",TomaDeLecturas.ENVIADA);
//                         *
//                         * int j=db.update("lecturas", cv_datos, whereClause,
//                         * whereArgs);
//                         *
//                         * closeDatabase();
//                         */
//                        // Marcar como enviada
//                        mostrarMensaje(MENSAJE, (i + 1) + " "
//                                + getString(R.string.de) + " " + bufferLenght
//                                + " " + getString(R.string.registros) + ".\n"
//                                + String.valueOf(porcentaje) + "%");
//                        mostrarMensaje(BARRA, String.valueOf(1));
//
//                    }
//                    serial.close();
//
//                    c.close();

                    transmitirFotos(context);

                    // mostrarMensaje(PROGRESO, "Mandando datos al servidor");
                    mostrarMensaje(MENSAJE, getString(R.string.str_espere));
                    // serial.close();
                    yaAcabo = true;
                    marcarComoDescargada();
                    muere(true, String.format(
                            getString(R.string.msj_trans_correcta),
                            getString(R.string.str_exportado)));
                    c.close();
                } catch (AppUsuarioBloqueadoException eb) {
                    globales.sesionEntity = null;
                    mostrarMensaje("Alerta", getString(R.string.str_usuario_bloqueado), "", new DialogoMensaje.Resultado() {
                        @Override
                        public void Aceptar(boolean EsOk) {
                            muere(true, getString(R.string.str_usuario_bloqueado));
                        }
                    });
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    String msg;

                    msg = String.format(getString(R.string.msj_trans_error),
                            getString(R.string.str_exportar_lowercase)) + e.getMessage();

                    mostrarMensaje("Alerta", msg, "", new DialogoMensaje.Resultado() {
                        @Override
                        public void Aceptar(boolean EsOk) {
                            muere(true,msg);
                        }
                    });
                } finally {
                    closeDatabase();

                    // dialog.cancel();
                }

            }

        };

        hilo.start();
    }

    private void transmitirOrdenes() {

    }

    private void transmitirFotos(trasmisionDatos context) throws Exception {
        final int cantFotos;
        int idFoto = 0;
        String query = "";
        long idOrden = 0;
        long numOrden = 0;
        Cursor c = null;
        String nombreArchivo = "";
        String serieMedidor = "";
        byte[] image;
        DatosEnvioEntity datosEnvio = new DatosEnvioEntity();



        // --------------------------------------------------------------------------
        //  Enviar fotos
        // --------------------------------------------------------------------------

        // Por ahora, sabemos que las fotos andan rotundamente mal,
        // por ahora no envio nada
        // transmiteFotos=false;

        try {

            mostrarMensaje(BARRA, String.valueOf(0));

            if (transmiteFotos) {

                mostrarMensaje(PROGRESO,
                        getString(R.string.msj_trans_generando_fotos));
                mostrarMensaje(MENSAJE, getString(R.string.str_espere));

                openDatabase();

                if (fotoMgr == null)
                    fotoMgr = new FotosMgr();

                query = "SELECT F.idFoto, F.secuencial, F.nombre, length(F.foto) imageSize, L.serieMedidor, F.idOrden ";
                query += " FROM fotos F ";
                query += " LEFT JOIN (SELECT * FROM ruta R WHERE R.idOrden <> 0) L ON F.idOrden = L.idOrden ";
                query += " WHERE F.envio=1";

                c = db.rawQuery(query, null);

                c.moveToFirst();

                cantFotos = c.getCount();

                mHandler.post(new Runnable() {
                    public void run() {
                        pb_progress.setMax(cantFotos);
                    }
                });
                // closeDatabase();

                String ls_capertaFotos = subirDirectorio(ls_carpeta, 2)
                        + "/fotos/"/* + ls_subCarpeta + "/"
								+ Main.obtieneFecha("ymd")*/;


                for (int i = 0; i < cantFotos; i++) {
                    context.stop();

                    try {
                        nombreArchivo = Utils.getString(c, "nombre", "");
                        serieMedidor = Utils.getString(c, "serieMedidor", "");
                        idOrden = Utils.getLong(c, "idOrden", 0);
                        idFoto = Utils.getInt(c, "idFoto", 0);

                        String fecha = nombreArchivo.substring(nombreArchivo.length() - 18, nombreArchivo.length() - 10);

                        //serial.open(ls_servidor, ls_capertaFotos + fecha + "/", "",
                        //		Serializacion.ESCRITURA, 0, 0, globales.getIdEmpleado(), serieMedidor, idOrden, context);

                        long imageSize = Utils.getLong(c, "imageSize", 0);

                        // ls_cadena=generaCadenaAEnviar(c);

                        image = fotoMgr.obtenerFoto(db, idFoto, imageSize);

                        // ls_cadena=generaCadenaAEnviar(c);
                        // serial.write(nombreArchivo, c.getBlob(c.getColumnIndex("foto")));
                        // serial.write(nombreArchivo, image);

                        datosEnvio.Carpeta = ls_capertaFotos + fecha + "/";
                        datosEnvio.nombreArchivo = nombreArchivo;
                        datosEnvio.idEmpleado = globales.getIdEmpleado();
                        datosEnvio.idOrden = idOrden;

                        enviarFoto(datosEnvio, image);

                        // String bufferLenght;
                        int porcentaje = ((i + 1) * 100) / cantFotos;
                        // bufferLenght = String.valueOf(c.getCount());
                        //serial.close();
                        // openDatabase();

                        String whereClause = "idFoto=?";
                        String[] whereArgs = {Utils.getString(c, "idFoto", "")};


                        if (!transmitirTodo) {
//								cv_datos.put("envio", TomaDeLecturas.ENVIADA);
//
//								int j = db.update("fotos", cv_datos,
//										whereClause, whereArgs);
                            db.execSQL("delete from fotos where idFoto=?", whereArgs);
                        }
                        // closeDatabase();
                        // Marcar como enviada
                        c.moveToNext();
                        mostrarMensaje(MENSAJE, (i + 1) + " "
                                + getString(R.string.de) + " "
                                + cantFotos + " "
                                + getString(R.string.str_fotos) + ".\n"
                                + String.valueOf(porcentaje) + "%");
                        mostrarMensaje(BARRA, String.valueOf(1));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        } catch (Throwable t) {
            throw new Exception("Error al enviar fotos. (" + t.getMessage()+")");
        }
    }

    private Boolean enviarDatos(String carpeta, String archivo, String datos) throws Exception {
        String ruta, cadenaAEnviar;
        String msg;

        if (datos.equals(""))
            return true;

        cadenaAEnviar = new String(datos);

        try {
            SubirDatosRequest req = new SubirDatosRequest();

            req.NombreArchivo = archivo;
            req.Carpeta = carpeta;
            req.Datos = cadenaAEnviar;
            req.idEmpleado = globales.getIdEmpleado();

            SubirDatosResponse resp = WebApiManager.getInstance(globales.getApplicationContext()).subirDatos(req);

            if (resp == null)
                throw new Exception("Error al enviar datos al servidor. No se recibieron datos.");

            if (resp.NumError == 1)
                throw new Exception("Error al enviar datos al servidor (" + String.valueOf(resp.NumError) + "). " + resp.MensajeError);

            if (resp.NumError == 2)
                throw new Exception("No se recibieron los datos (" + String.valueOf(resp.NumError) + "). " + resp.MensajeError);

            return resp.EsUsuarioValido;
        } catch (Throwable e) {
            throw new Exception("Error al enviar mensaje : " + e.getMessage());
        }
    }


    private int enviarFoto(DatosEnvioEntity datosEnvio, byte[] foto) throws Throwable {
        Enumeration en_Nombres, en_Fotos;
        Hashtable params;
        String msg;
        SubirFotoResponse respFoto;

        String ls_foto, ls_urlConArchivo, ls_url;

        //params.put("Connection", "keep-alive");

        byte[] response = null;
        try {
            if (foto != null) {
                ls_foto = Base64.encodeToString(foto, Base64.DEFAULT);
                if (datosEnvio.Carpeta.equals("")) {
                    ls_urlConArchivo = "/" + datosEnvio.nombreArchivo;
                    ls_url = "";
                } else {
                    //ls_urlConArchivo= "/" +is_carpeta + "/2022/202207/20220724/" + nombre;
                    ls_urlConArchivo = "/" + datosEnvio.Carpeta + "/" + datosEnvio.nombreArchivo;
                    ls_url = datosEnvio.Carpeta;
                }

                SubirFotoRequest req = new SubirFotoRequest();

                req.carpeta = "/" + datosEnvio.Carpeta;
                req.ruta = ls_urlConArchivo;
                req.nombre = datosEnvio.nombreArchivo;

                respFoto = WebApiManager.getInstance(globales.getApplicationContext()).subirFoto(req, foto);

                if (respFoto == null)
                    throw new Exception("Error al enviar la foto");

                if (respFoto.NumError > 0)
                    throw new Exception("Error al enviar la foto. " + respFoto.Mensaje);
            }
        } catch (Exception e) {
            throw e;
        }

        return 0;
    }

    public void recepcion() {
        recepcionPorPipes();
    }

    private void recepcionPorAnchoFijo() {
        final trasmisionDatos context = this;
        puedoCerrar = false;
        final trasmisionDatos td = this;
        hilo = new Thread() {
            int cantidad;

            public void run() {
                int secuenciaReal = 0;

                // TODO Auto-generated method stub
                serial = new Serializacion(Serializacion.WIFI);
                String ls_cadena = "";
                byte[] lby_cadena;
                String[] lineas;
                String[] ls_cambios;

                String mPhoneNumber;
                boolean recibiOrdenes = false;
                int numRegistros;
                String ls_linea2 = "";

                TareasResponse resp;

                puedoCerrar = true;

                /*
                 * TelephonyManager tMgr
                 * =(TelephonyManager)context.getSystemService
                 * (Context.TELEPHONY_SERVICE); String mPhoneNumber =
                 * tMgr.getLine1Number();
                 */
                // ProgressDialog dialog = ProgressDialog.show(context,
                // "Exportar", "Se esta exportando el archivo, espere", true);

                mostrarMensaje(MENSAJE,
                        getString(R.string.msj_trans_recibiendo));
                mostrarMensaje(PROGRESO, getString(R.string.str_espere));
                int i = 0;

                try {

                    openDatabase();

                    //db.execSQL("Delete from ruta where envio=0 and estadoDeLaOrden in ('EO004', 'EO002', 'EO012')");

                    db.execSQL("Delete from ruta where envio=0 and tipoLectura<>'' and cast(substr(fechaEnvio, 1, 8) as integer)< " + getFechaServidor() + " ");

                    resp = mDescargarTareas.descargarTareas();

                    context.stop();
                    // lby_cadena= new
                    // byte[context.getResources().getInteger(R.integer.LONG_DATOS_MEDIDOR)];

                    vLecturas = new Vector<String>();

//					if (serial.longitudDelArchivo == 0) {
//						// no se encontro el archivo
//						serial.close();
//						muere(true,
//								"");
//						return;
//					}

                    if (resp == null) {
                        muere(true, "");
                        return;
                    }

                    if (!resp.EsUsuarioValido) {
                        throw new AppUsuarioBloqueadoException();
                    }

                    if (resp.Contenido == null) {
                        muere(true, "");
                        return;
                    }

                    numRegistros = resp.Contenido.size();

                    if (numRegistros == 0) {
                        muere(true, "");
                        return;
                    }

                    // Obtenemos el archivo recibido completo
//					lby_cadena = new byte[serial.longitudDelArchivo];
//					serial.read(lby_cadena);
//					ls_cadena = new String(lby_cadena);

                    // Hacemos split con el salto de linea
                    //lineas = ls_cadena.split("\\r\\n");

                    //tope(Integer.parseInt(String.valueOf(lineas.length)));
                    tope(numRegistros);

                    // db.execSQL("delete from Lecturas ");

                    /*
                     * db.execSQL("delete from ruta ");
                     * db.execSQL("delete from fotos ");
                     * db.execSQL("delete from Anomalia ");
                     * db.execSQL("delete from encabezado ");
                     * db.execSQL("delete from NoRegistrados ");
                     */
                    //borrarRuta(db);
                    // serial.close();

                    //recibiOrdenes=lineas.length>0;


                    recibiOrdenes = (numRegistros > 0);

                    puedoCerrar = false;
                    db.beginTransaction();

                    int j = 0;

                    for (String ls_linea : resp.Contenido) {

                        ls_linea2 = resp.Contenido2.get(j);
                        j++;

                        context.stop();

                        // Comprobamos que las lineas son las que esperamos
                        if (ls_linea.length() == 0) {
                            // no se encontro el archivo
                            serial.close();
                            db.execSQL("delete from Lecturas ");
                            closeDatabase();
                            // db.setTransactionSuccessful();
                            // db.endTransaction();
                            algunError = true;
                            // muere(false,
                            // "No se encontro algun archivo exportado.");
                            muere(true,
                                    "");
                        }

                        if (ls_linea.toUpperCase().startsWith("<HTML>")) {
                            // Error general
                            serial.close();
                            // db.execSQL("delete from Lecturas ");
                            db.endTransaction();
                            closeDatabase();
                            // db.setTransactionSuccessful();

                            algunError = true;
                            muere(false,
                                    getString(R.string.msj_trans_connection_problem));

                        }

//						if (ls_linea.length() != globales.tdlg.long_registro) {
//							// Error general
//							serial.close();
//							// db.setTransactionSuccessful();
//							db.endTransaction();
//							// db.execSQL("delete from Lecturas ");
//							closeDatabase();
//							algunError = true;
//							muere(false,
//									getString(R.string.msj_trans_file_doesnt_match));
//
//						}

                        // Agregamos mientras verificamos...
                        // vLecturas.add(ls_cadena);
                        // db.execSQL("Insert into lecturas(registro) values ('"+ls_linea+"')");
                        if (/*i != 0 && */!ls_linea.startsWith("#")
                                && !ls_linea.startsWith("!")) {
                            secuenciaReal++;
                            globales.tlc.byteToBD(globales.getApplicationContext(), db,
                                    ls_linea.getBytes("ISO-8859-1"), secuenciaReal);// Esta
                            // clase
                            // ahora
                            // guarda
                            // new Lectura(context,
                            // ls_linea.getBytes("ISO-8859-1"), db);
                        } else if (ls_linea.startsWith("#")) {// Esto indica que
                            // es una
                            // anomalia
                            new Anomalia(context,
                                    ls_linea.getBytes("ISO-8859-1"), db);
                        } else if (ls_linea.startsWith("!")) { // un usuario
                            new Usuario(context,
                                    ls_linea.getBytes("ISO-8859-1"), db);
                        } /*else if (i == 0) {
							// la primera
							ContentValues cv = new ContentValues();
							cv.put("registro", ls_linea.getBytes());

							db.insert("encabezado", null, cv);
						}*/

                        int porcentaje = (i * 100) / numRegistros;
                        mostrarMensaje(MENSAJE, (i + 1) + " "
                                + getString(R.string.de) + " " + numRegistros
                                + " " + getString(R.string.registros) + "\n"
                                + String.valueOf(porcentaje) + "%");
                        mostrarMensaje(BARRA, String.valueOf(1));
                        i++;

                    }

                    // Una vez verificado que todos los registros fueron
                    // recibidos ahora si tenemos la seguridad de borrar

                    // Una vez que borramos insertamos cada uno de los registros
                    // recibidos

                    // for(String ls_lectura:vLecturas)

                    if (!algunError)
                        db.setTransactionSuccessful();

                    mostrarMensaje(MENSAJE, getString(R.string.str_espere));
                    serial.close();


                    if (recibiOrdenes && !algunError) {
                        //Si hay un error al recibir o no recibí, mover el archivo no tiene sentido
                        // enviarBackup(ls_carpeta, globales.tdlg.getNombreArchvio(TomaDeLecturasGenerica.ENTRADA));

                        //Reordenamos la secuenciaReal
                        //asignarSecuenciasReales();

                    }


//					closeDatabase();

                    //Enviamos si acaso recibimos un pago
//					Intent lrs = new Intent(td, trasmisionDatos.class);
//					  lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
//		           		lrs.putExtra("transmiteFotos", true);
//		           		lrs.putExtra("transmitirTodo", false);
//		           		startActivity(lrs);

                    yaAcabo = true;

                    muere(true, String.format(
                            getString(R.string.msj_trans_correcta),
                            getString(R.string.str_importado)));
                }
                catch (AppUsuarioBloqueadoException eb){
                    mostrarMensaje("Alerta", getString(R.string.str_usuario_bloqueado), "", new DialogoMensaje.Resultado() {
                        @Override
                        public void Aceptar(boolean EsOk) {
                            globales.cerrarSesion();
                            muere(true, getString(R.string.str_usuario_bloqueado));
                        }
                    });
                }
                catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    // db.endTransaction();
                    // db.execSQL("delete from lecturas ");
                    muere(true,
                            String.format(getString(R.string.msj_trans_error),
                                    getString(R.string.str_importar_lowercase))
                                    + i + " " + e.getMessage());
                } finally {
                    try {
                        db.endTransaction();
                    } catch (Throwable e) {

                        e.printStackTrace();
                    }
                    closeDatabase();
                    // dialog.cancel();
                }
            }
        };

        hilo.start();
    }

    private void recepcionPorPipes() {
        final trasmisionDatos context = this;
        puedoCerrar = false;
        final trasmisionDatos td = this;

        hilo = new Thread() {
            int cantidad;

            public void run() {
                int secuenciaReal = 0;

                // TODO Auto-generated method stub
                // serial = new Serializacion(Serializacion.WIFI);
                String ls_cadena = "";
                byte[] lby_cadena;
                String[] lineas;
                String[] ls_cambios;

                String mPhoneNumber;
                boolean recibiOrdenes = false;
                int numRegistros = 0;

                // Adaptación del proceso usando clase para descargar y procesar.
                DescargarTareasMgr descargarTareasMgr = new DescargarTareasMgr(context, globales);
                TareasResponse resp;
                OrdenEntity orden;
                List<String> contenido;
                String registro;

                puedoCerrar = true;

                mostrarMensaje(MENSAJE,
                        getString(R.string.msj_trans_recibiendo));
                mostrarMensaje(PROGRESO, getString(R.string.str_espere));
                int i = 0;

                try {

                    openDatabase();

                    //db.execSQL("Delete from ruta where envio=0 and estadoDeLaOrden in ('EO004', 'EO002', 'EO012')");

                    db.execSQL("Delete from ruta where envio=0 and tipoLectura<>'' and cast(substr(fechaEnvio, 1, 8) as integer)< " + getFechaServidor() + " ");

                    descargarTareasMgr.setDatabase(db);

                    resp = descargarTareasMgr.descargarTareas();

                    context.stop();
                    // lby_cadena= new
                    // byte[context.getResources().getInteger(R.integer.LONG_DATOS_MEDIDOR)];

                    vLecturas = new Vector<String>();

                    if (resp == null) {
                        muere(true, "");
                        return;
                    }

                    if (!resp.EsUsuarioValido) {
                        throw new AppUsuarioBloqueadoException();
                    }

                    if (resp.Contenido == null) {
                        muere(true, "");
                        return;
                    }

                    contenido = resp.Contenido2;

                    if (contenido == null) {
                        // no se encontro el archivo o no tiene datos
                        muere(true,
                                "");
                        return;
                    }

                    numRegistros = contenido.size();

                    if (numRegistros == 0) {
                        // no se encontro el archivo o no tiene datos
                        muere(true, "");
                        return;
                    }

                    // Obtenemos el archivo recibido completo
//					lby_cadena = new byte[serial.longitudDelArchivo];
//					serial.read(lby_cadena);
//					ls_cadena = new String(lby_cadena);

                    // Hacemos split con el salto de linea
                    // lineas = ls_cadena.split("\\n");

                    //tope(Integer.parseInt(String.valueOf(lineas.length)));
                    tope(numRegistros);

                    // db.execSQL("delete from Lecturas ");

                    /*
                     * db.execSQL("delete from ruta ");
                     * db.execSQL("delete from fotos ");
                     * db.execSQL("delete from Anomalia ");
                     * db.execSQL("delete from encabezado ");
                     * db.execSQL("delete from NoRegistrados ");
                     */
                    //borrarRuta(db);
                    // serial.close();

                    recibiOrdenes = (numRegistros > 0);

                    puedoCerrar = false;
                    db.beginTransaction();
                    for (i = 0; i < numRegistros; i++) {
                        context.stop();

                        // RL, 28-09-2023, Aquí se reciben los datos del servidor ...
                        // ... se convierten a la estructura ordenEntity.
                        // ... y luego se insertan en la base de datos SQLLite.

                        registro = contenido.get(i);
                        orden = descargarTareasMgr.convToOrden(registro);
                        if (orden != null) {
                            descargarTareasMgr.agregarRegistro(orden);
                        }
                        // Comprobamos que las lineas son las que esperamos
//						if (ls_linea.length() == 0) {
//							// no se encontro el archivo
//							serial.close();
//							db.execSQL("delete from Lecturas ");
//							closeDatabase();
//							// db.setTransactionSuccessful();
//							// db.endTransaction();
//							algunError = true;
//							// muere(false,
//							// "No se encontro algun archivo exportado.");
//							muere(true,
//									"");
//						}

//						if (ls_linea.toUpperCase().startsWith("<HTML>")) {
//							// Error general
//							serial.close();
//							// db.execSQL("delete from Lecturas ");
//							db.endTransaction();
//							closeDatabase();
//							// db.setTransactionSuccessful();
//
//							algunError = true;
//							muere(false,
//									getString(R.string.msj_trans_connection_problem));
//
//						}

//						if (ls_linea.length() != globales.tdlg.long_registro) {
//							// Error general
//							serial.close();
//							// db.setTransactionSuccessful();
//							db.endTransaction();
//							// db.execSQL("delete from Lecturas ");
//							closeDatabase();
//							algunError = true;
//							muere(false,
//									getString(R.string.msj_trans_file_doesnt_match));
//
//						}

                        // Agregamos mientras verificamos...
                        // vLecturas.add(ls_cadena);
                        // db.execSQL("Insert into lecturas(registro) values ('"+ls_linea+"')");
//						if (/*i != 0 && */!ls_linea.startsWith("#")
//								&& !ls_linea.startsWith("!")) {
//							secuenciaReal++;
//							globales.tlc.byteToBD(db,
//									ls_linea.getBytes("ISO-8859-1"), secuenciaReal);// Esta
//							// clase
//							// ahora
//							// guarda
//							// new Lectura(context,
//							// ls_linea.getBytes("ISO-8859-1"), db);
//						} else if (ls_linea.startsWith("#")) {// Esto indica que
//							// es una
//							// anomalia
//							new Anomalia(context,
//									ls_linea.getBytes("ISO-8859-1"), db);
//						} else if (ls_linea.startsWith("!")) { // un usuario
//							new Usuario(context,
//									ls_linea.getBytes("ISO-8859-1"), db);
//						} /*else if (i == 0) {
//							// la primera
//							ContentValues cv = new ContentValues();
//							cv.put("registro", ls_linea.getBytes());
//
//							db.insert("encabezado", null, cv);
//						}*/

                        int porcentaje = (i * 100) / numRegistros;
                        mostrarMensaje(MENSAJE, (i + 1) + " "
                                + getString(R.string.de) + " " + String.valueOf(numRegistros)
                                + " " + getString(R.string.registros) + "\n"
                                + String.valueOf(porcentaje) + "%");
                        mostrarMensaje(BARRA, String.valueOf(1));
                    }

                    // Una vez verificado que todos los registros fueron
                    // recibidos ahora si tenemos la seguridad de borrar

                    // Una vez que borramos insertamos cada uno de los registros
                    // recibidos

                    // for(String ls_lectura:vLecturas)

                    if (!algunError)
                        db.setTransactionSuccessful();

                    mostrarMensaje(MENSAJE, getString(R.string.str_espere));
                    //serial.close();


                    if (recibiOrdenes && !algunError) {
                        //Si hay un error al recibir o no recibí, mover el archivo no tiene sentido
                        // enviarBackup(ls_carpeta, globales.tdlg.getNombreArchvio(TomaDeLecturasGenerica.ENTRADA));

                        //Reordenamos la secuenciaReal
                        //asignarSecuenciasReales();

                    }

                    yaAcabo = true;

                    muere(true, String.format(
                            getString(R.string.msj_trans_correcta),
                            getString(R.string.str_importado)));
                } catch (AppUsuarioBloqueadoException eb){
                    cerrarSesion();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    terminarRecepcion(e, i);
                } finally {
                    try {
                        db.endTransaction();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    closeDatabase();

                    // dialog.cancel();
                }
            }
        };
        hilo.start();
    }

    private void terminarRecepcion(Throwable t, int i)  {
        try {
            String msg;

            msg = String.format(getString(R.string.msj_trans_error),
                    getString(R.string.str_importar_lowercase)) + i + " ";

            mostrarMensaje("Alerta", msg, t.getMessage(), new DialogoMensaje.Resultado() {
                @Override
                public void Aceptar(boolean EsOk) {
                    muere(true, msg);
                }
            });
        } catch (Throwable t2) {
            t2.printStackTrace();
        }
    }

    private void cerrarSesion() {
        mostrarMensaje("Alerta", getString(R.string.str_usuario_bloqueado), "", new DialogoMensaje.Resultado() {
            @Override
            public void Aceptar(boolean EsOk) {
                globales.cerrarSesion();
                muere(true, getString(R.string.str_usuario_bloqueado));
            }
        });
    }


    public String remplazaNulls(String ls_cadena) {
        ls_cadena = (ls_cadena == null ? "" : ls_cadena);

        return (ls_cadena.trim().equals("") ? "" : ls_cadena);
    }

    private void borrarArchivo(String ls_ruta) throws Throwable {
        // HCG 20/07/2012 Manda los datos del wifi antes de cerrar la conexion
        String ruta, cadenaAEnviar;

        Hashtable params = new Hashtable();
        // params.put("cadena",cadenaAEnviar);
        params.put("ruta", ls_ruta);

        try {
            HttpMultipartRequest http = new HttpMultipartRequest(ls_servidor
                    + "/deleteFile.php", params, "upload_field", "",
                    "text/plain", new String("").getBytes());
            byte[] response = http.send();
            // new String (response); Esta es la respuesta del servidor

            if (!new String(response).trim().equals("0")) {
                throw new Throwable(new String(response));
            }

            // Enviamos las fotos que tenemos pendientes
            // enviaFotosWifi();

        } catch (Throwable e) {
            throw e;
        }

    }

    private void mostrarMensaje(final int tipo, final String mensaje) {
        // Esta funcion manda un request para que se cambie algun elemento en
        // patanlla
        mHandler.post(new Runnable() {
            public void run() {
                switch (tipo) {
                    case MENSAJE:
                        setMensaje(mensaje);
                        break;
                    case PROGRESO:
                        setProgreso(mensaje);
                        break;

                    case BARRA:
                        avanzaProgreso(Integer.parseInt(mensaje));
                        break;
                    case TOPE:
                        tope(Integer.parseInt(mensaje));
                        break;

                }
            }
        });
    }

    public void setMensaje(String texto) {
        tv_indicador.setText(texto);
    }

    public void setProgreso(String texto) {
        tv_progreso.setText(texto);
    }

    public void avanzaProgreso(int avance) {
        if (pb_progress.isIndeterminate())
            pb_progress.setIndeterminate(false);

        if (avance > 0)
            pb_progress.incrementProgressBy(avance);
        else {
            pb_progress.setProgress(0);
            pb_progress.setIndeterminate(true);

        }
    }

    public void tope(int avance) {
        if (pb_progress.isIndeterminate())
            pb_progress.setIndeterminate(false);
        pb_progress.setMax(avance);
    }

    public void setAcabado() {
        yaAcabo = true;
    }

    private String generaCadenaAEnviar(Cursor c) {
        String ls_cadena = "";
        String ls_lectura;
        c.moveToFirst();
        String ls_tmpSubAnom = "";

        ls_lectura = c.getString(c.getColumnIndex("lectura"));

        ls_cadena = ls_lectura.length() == 0 ? "4" : "0"; // Indicador de tipo
        // de lectura
        ls_cadena += Main.rellenaString(ls_lectura, "0",
                globales.tdlg.long_registro, true);
        ls_cadena += Main.rellenaString(ls_lectura, "0",
                globales.tdlg.long_registro, true);
        ls_cadena += c.getString(c.getColumnIndex("fecha"));
        ls_cadena += c.getString(c.getColumnIndex("hora"));

        ls_cadena += Main.rellenaString(
                c.getString(c.getColumnIndex("anomalia")), " ",
                globales.tdlg.long_registro, true);
        // Esto no se bien de que se trata, asi que de momento dejaremos
        // ceros...
        ls_cadena += Main.rellenaString("", "0",
                globales.tdlg.long_registro, true);
        ls_cadena += Main.rellenaString("", "0",
                globales.tdlg.long_registro, true);
        ls_cadena += Main.rellenaString("", "0",
                globales.tdlg.long_registro, true);

        return ls_cadena;
    }

    public void preguntaArchivo() {
        AlertDialog alert;

        LayoutInflater inflater = this.getLayoutInflater();

        String ls_archivo;

        final View view = inflater.inflate(R.layout.lote_a_cargar, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final trasmisionDatos slda = this;
        final String[] selectionArgs = {"archivo"};
        builder.setView(view);

        final EditText et_archivocarga = (EditText) view
                .findViewById(R.id.et_archivocarga);

        openDatabase();

        Cursor c = db.rawQuery("Select value from config where key=?",
                selectionArgs);

        if (c.getCount() > 0) {
            c.moveToFirst();
            ls_archivo = c.getString(c.getColumnIndex("value"));
            if (ls_archivo.indexOf(".") > 0) {
                et_archivocarga.setText(ls_archivo.substring(0,
                        ls_archivo.indexOf(".")));
            } else {
                et_archivocarga.setText(ls_archivo);
            }

        }
        /*
         * else{ et_archivocarga.setText("cpl001"); }
         */

        closeDatabase();

        builder.setCancelable(false)
                .setPositiveButton(R.string.continuar,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ls_categoria = et_archivocarga.getText()
                                        .toString().trim()
                                        + "." + ls_extension;
                                if (ls_categoria.length() == 0)
                                    mensajeVacioLote();
                                else {
                                    openDatabase();

                                    Cursor c = db
                                            .rawQuery(
                                                    "Select value from config where key=?",
                                                    selectionArgs);

                                    if (c.getCount() > 0)
                                        db.execSQL("update config set value='"
                                                + ls_categoria
                                                + "' where key='archivo'");
                                    else
                                        db.execSQL("insert into config(key, value) values('archivo', '"
                                                + ls_categoria + "')");

                                    closeDatabase();
                                    recepcion();
                                }

                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(R.string.cancelar,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                mostrarAlerta = false;
                                muere(true, "");

                            }
                        });

        builder.show();
        esconderTeclado(et_archivocarga);

    }

    // Elimina todo lo que necesite para que sea numero
    public long quitarCaracteres(String ls_cadena) {
        String ls_numero = "", ls_caracter;

        for (int i = 0; i < ls_cadena.length(); i++) {
            ls_caracter = ls_cadena.substring(i, i + 1);
            if (esNumero(ls_caracter)) {
                ls_numero += ls_caracter;
            }

        }

        return Long.parseLong(ls_numero);

    }

    public boolean esNumero(String ls_cadena) {
        try {
            Integer.parseInt(ls_cadena);
            return true;
        } catch (Throwable e) {
            return false;

        }
    }

    public String quitaComillas(String ls_candena) {
        return ls_candena.replace("\"", "");
    }

    public void mensajeVacioLote() {
        final trasmisionDatos slda = this;
        AlertDialog.Builder message = new AlertDialog.Builder(slda);
        message.setMessage(R.string.str_emptyField)
                .setCancelable(false)
                .setPositiveButton(R.string.aceptar,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                                int which) {

                                preguntaArchivo();
                            }

                        });
        AlertDialog alerta = message.create();
        alerta.show();
    }

    public void cancelar() {

    }

    public String subirDirectorio(String ls_carpeta, int cuantos) {
        String ls_discoDuro = "";

        if (ls_carpeta.endsWith("\\"))
            ls_carpeta = ls_carpeta.substring(0, ls_carpeta.length());

        if (ls_carpeta.indexOf(":") >= 0) {
            ls_discoDuro = ls_carpeta.substring(0, ls_carpeta.indexOf(":") + 1);
            ls_carpeta = ls_carpeta.substring(ls_carpeta.indexOf(":") + 2);
        }

        for (int i = 0; i < cuantos; i++) {
            if (ls_carpeta.lastIndexOf("\\") >= 0)
                ls_carpeta = ls_carpeta.substring(0,
                        ls_carpeta.lastIndexOf("\\"));
        }

        return ls_discoDuro + ls_carpeta;

    }

    public void enviarBackup(String ls_ruta, String ls_file) {
        //Como es muy importante que la fecha siempre sea la actual,  agregaremos una validacion para que ellos esten al tanto que la fecha sea la mas actual
        Hashtable params = new Hashtable();
        boolean esCorrecta = true;
        byte[] response;
        params.put("ruta", ls_ruta);
        params.put("archivo", ls_file);
        params.put("backup", getNombreArchivoBackup(ls_file));


        HttpMultipartRequest http;
        try {
            http = new HttpMultipartRequest(ls_servidor + "/createBackup.php", params, "upload_field", "", "text/plain", new String("").getBytes());
            response = http.send();
            new String(response);

        } catch (Throwable e) {

        }
    }

    public String getNombreArchivoBackup(String ls_nombre) {
        int li_pos;
        li_pos = ls_nombre.indexOf(".");

        ls_nombre = ls_nombre.substring(0, li_pos) + "_" + Main.obtieneFecha("ymdhis") + ls_nombre.substring(li_pos);
        return ls_nombre;
    }


    public void onBackPressed() {
        super.onBackPressed();
        if (puedoCerrar) {
            this.muere(false, "La operacion ha sido cancelada");
            try {
                serial.close();
            } catch (Throwable e) {

            }

        }

        cancelar = true;

    }


    public String getFechaServidor() {
        //Como es muy importante que la fecha siempre sea la actual,  agregaremos una validacion para que ellos esten al tanto que la fecha sea la mas actual

        String ls_fecha = getFechaHoraServidor();
        if (ls_fecha.length() != 14) {
            ls_fecha = "";

        } else {
            ls_fecha = ls_fecha.substring(0, 8);
        }

        return ls_fecha;
    }


    public String getFechaHoraServidor() {
        //Como es muy importante que la fecha siempre sea la actual,  agregaremos una validacion para que ellos esten al tanto que la fecha sea la mas actual
        Hashtable params = new Hashtable();
        boolean esCorrecta = true;
        byte[] response;

        String ls_fecha = Main.obtieneFecha("ymdhis");

        HttpMultipartRequest http;
        try {
            http = new HttpMultipartRequest(ls_servidor + "/getTime.php", params, "upload_field", "", "text/plain", new String("").getBytes());
            response = http.send();
            ls_fecha = new String(response).trim();

            if (ls_fecha.length() != 14) {
                ls_fecha = Main.obtieneFecha("ymdhis");

            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return ls_fecha;
    }

    private void mostrarMensaje(String titulo, String mensaje, String detalleError, DialogoMensaje.Resultado resultado) {
        mHandler.post(new Runnable() {
            public void run() {
                mDialogoMsg.setOnResultado(resultado);
                mDialogoMsg.mostrarMensaje(titulo, mensaje, detalleError);
            }
        });
    }

    private void mostrarMensaje(String titulo, String mensaje) {
        mHandler.post(new Runnable() {
            public void run() {
                mDialogoMsg.mostrarMensaje(titulo, mensaje, "");
            }
        });
    }
}
