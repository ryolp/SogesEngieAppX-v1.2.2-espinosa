package enruta.soges_engie;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.widget.TextView;
import android.widget.Toast;

import enruta.soges_engie.clases.Utils;
import enruta.soges_engie.entities.DatosEnvioEntity;

/**
 * Esta clase crea las validaciones y los campos a mostrar
 **/

public class TomaDeLecturasEngie extends TomaDeLecturasGenerica {
    public final static int MEDIDOR_ANTERIOR = 0;
    public final static int MEDIDOR_POSTERIOR = 1;
    public final static int NUM_ESFERAS = 2;
    public final static int NUM_MEDIDOR = 3;
    public final static int MARCA = 4;
    public final static int CALLE = 5;
    public final static int NUMERO = 6;
    public final static int PORTAL = 7;
    public final static int ESCALERA = 8;
    public final static int PISO = 9;
    public final static int PUERTA = 10;
    public final static int COMPLEMENTO = 11;
    public final static int IC = 12;
    public final static int CLIENTE_YA_PAGO_MONTO = 13;
    public final static int CLIENTE_YA_PAGO_FECHA = 14;
    public final static int CLIENTE_YA_PAGO_AGENTE = 15;


    Vector<TextView> textViews = new Vector<TextView>();
    MensajeEspecial mj_estaCortado;
    MensajeEspecial mj_sellos;
    MensajeEspecial mj_consumocero;
    MensajeEspecial mj_consumoceroRecuperado;
    MensajeEspecial mj_ubicacionVacia;
    MensajeEspecial mj_anomalia_seis;
    MensajeEspecial mj_ver_datos;
    MensajeEspecial mj_ver_datosFotoDeLlegada;
    //	MensajeEspecial mj_habitado;
    Hashtable<String, Integer> ht_calidades;

    //int versionAnom=0;

    public TomaDeLecturasEngie(Context context) {
        super(context);
        long_registro = 649;

        //Creamos los campos que serán de salida
        globales.tlc.getListaDeCamposFormateado(new String[]{"numOrden", "estadoDeLaOrden", "anomalia", "anomalia", "serieMedidor", "lectura", "fecha", "hora",
                "sospechosa", "lecturista", "fechaEnvio", "fechaDeInicio", "fecha", "hora", "comentarios", "latitud", "longitud", "poliza", "habitado", "registro"});
        //globales.tlc.getListaDeCamposFormateado();

        mj_ver_datos = new MensajeEspecial(MensajeEspecial.SIN_MENSAJE_ESPECIAL, "Iniciar Servicio", VER_DATOS);
        mj_ver_datos.color = R.color.EngieBoton;

        mj_ver_datosFotoDeLlegada = new MensajeEspecial(MensajeEspecial.SIN_MENSAJE_ESPECIAL, "Foto de Llegada", VER_DATOS);
        mj_ver_datosFotoDeLlegada.color = R.color.EngieBoton;

        mj_estaCortado = new MensajeEspecial("¿Sigue Cortado?", PREGUNTAS_SIGUE_CORTADO);
        mj_estaCortado.cancelable = false;
        Vector<Respuesta> respuesta = new Vector<Respuesta>();
        respuesta.add(new Respuesta("0", "Con Sellos"));
        respuesta.add(new Respuesta("1", "Sin Sellos"));
        respuesta.add(new Respuesta("2", "Reconectado"));

        mj_sellos = new MensajeEspecial("¿Tiene Sellos?", respuesta, PREGUNTAS_EN_EJECUCION);

//*********************************************************************************************
// CE, 01/10/23, Vamos a usar la Encuesta de Consumo Cero para capturar el Material Utilizado
/*        respuesta = new Vector<Respuesta>();
        respuesta.add(new Respuesta("4J", "J-Sin Servicio"));
        respuesta.add(new Respuesta("4O", "O-No usan gas"));
        respuesta.add(new Respuesta("4P", "P-Refaccion"));
        respuesta.add(new Respuesta("4Q", "Q-Bien Tomado"));
        respuesta.add(new Respuesta("4U", "U-Desocupado"));
        respuesta.add(new Respuesta("4X", "X-Cerrado Vacaciones"));
        mj_consumocero = new MensajeEspecial("Consumo Cero. Seleccione una de las opciones", respuesta, PREGUNTAS_CONSUMO_CERO);
        mj_consumocero.cancelable = false;*/

        respuesta = new Vector<Respuesta>();
        respuesta.add(new Respuesta("EX", "X-Expander"));
        respuesta.add(new Respuesta("JC", "J-Junta Ciega"));
        respuesta.add(new Respuesta("DJ", "D-Doble Junta Ciega"));
        respuesta.add(new Respuesta("CV", "V-Cierre de Válvula"));
        respuesta.add(new Respuesta("SR", "S-Sello Rojo / Marbete"));
        respuesta.add(new Respuesta("TP", "T-Tapón de Bloqueo"));
// CE, 09/11/23, Por lo pronto no vamos a agregar estas opciones que nos había solicitado
//         respuesta.add(new Respuesta("RZ", "R-Raizer"));
//         respuesta.add(new Respuesta("LL", "L-Cierre de Llaves"));
        mj_consumocero = new MensajeEspecial("Seleccione el Material Utilizado", respuesta, PREGUNTAS_CONSUMO_CERO);
        mj_consumocero.cancelable = false;
//*********************************************************************************************

        respuesta = new Vector<Respuesta>();
        respuesta.add(new Respuesta("EX", "X-Expander"));
        respuesta.add(new Respuesta("JC", "J-Junta Ciega"));
        respuesta.add(new Respuesta("DJ", "D-Doble Junta Ciega"));
        respuesta.add(new Respuesta("TP", "T-Tapón de Bloqueo"));
        mj_consumoceroRecuperado = new MensajeEspecial("Seleccione el Material Recuperado", respuesta, PREGUNTAS_CONSUMO_CERO);
        mj_consumoceroRecuperado.cancelable = false;
//*********************************************************************************************


        respuesta = new Vector<Respuesta>();
        respuesta.add(new Respuesta("1A", "A - Accesible"));
        respuesta.add(new Respuesta("1B", "B - Batería"));
        respuesta.add(new Respuesta("1V", "V - Vivienda"));
        respuesta.add(new Respuesta("1R", "R - Accesible tras reja"));
        mj_ubicacionVacia = new MensajeEspecial("Capt. Ubicación", respuesta, PREGUNTAS_UBICACION_VACIA);
        mj_ubicacionVacia.cancelable = true;

        respuesta = new Vector<Respuesta>();
        respuesta.add(new Respuesta("G", "G - Vidrio empañado u opaco"));
        respuesta.add(new Respuesta("R", "R - Puerta trabada"));
        respuesta.add(new Respuesta("Z", "Z - Acceso Bloqueado"));
        respuesta.add(new Respuesta("5", "5 - No puede acceder por barrios carenciados"));
        mj_anomalia_seis = new MensajeEspecial("Seleccione", respuesta, ANOMALIA_SEIS);
        mj_anomalia_seis.cancelable = false;

        mj_habitado = new MensajeEspecial("¿Esta el cliente presente?", TomaDeLecturasGenerica.PREGUNTAS_ESTA_HABITADO);
        mj_registro = new MensajeEspecial("¿Tiene válvula?", TomaDeLecturasGenerica.PREGUNTAS_TIENE_REGISTRO);
        globales.logo = R.drawable.logo_engie;

        globales.GPS = true;

        globales.multiplesAnomalias = false;
        globales.convertirAnomalias = true;

        globales.longitudCodigoAnomalia = 2;
        globales.longitudCodigoSubAnomalia = 2;

        globales.rellenoAnomalia = ".";
        globales.rellenarAnomalia = false;

        globales.repiteAnomalias = true;

        globales.remplazarDireccionPorCalles = false;

        globales.mostrarCuadriculatdl = true;

        globales.mostrarRowIdSecuencia = true;

        globales.dejarComoAusentes = true;

        globales.mensajeDeConfirmar = R.string.msj_lecturas_verifique_1;

        globales.mostrarNoRegistrados = false;
        globales.tipoDeValidacion = Globales.CON_SMS;
        globales.mensajeContraseñaLecturista = R.string.str_login_msj_lecturista;
        globales.controlCalidadFotos = 0;

        globales.sonidoCorrecta = Sonidos.BEEP;
        globales.sonidoIncorrecta = Sonidos.URGENT;
        globales.sonidoConfirmada = Sonidos.BEEP;

        globales.mostrarMacBt = false;
        globales.mostrarMacImpresora = false;
        globales.mostrarServidorGPRS = true;
        globales.mostrarFactorBaremo = false;
        globales.mostrarTamañoFoto = true;
        globales.mostrarMetodoDeTransmision = false;
        globales.mostrarIngresoFacilMAC = false;

        globales.defaultLote = "";
        globales.defaultCPL = "CPL025";
        globales.defaultTransmision = "1";
        globales.defaultRutaDescarga = "C:\\";
//		globales.defaultRutaDescarga="C:\\CortrexPruebas";
        globales.defaultServidorGPRS = BuildConfig.BASE_URL;

        globales.letraPais = "A";

        globales.mostrarCodigoUsuario = true;

        globales.tomaMultiplesFotos = false;

        globales.porcentaje_main = 1.0;
        globales.porcentaje_main2 = 1.0;
        globales.porcentaje_hexateclado = .74998;
        globales.porcentaje_teclado = .6410;
        globales.porcentaje_lectura = 1.0;
        globales.porcentaje_info = 1.1;
        globales.mostrarCuadriculatdl = false;

        globales.calidadDeLaFoto = 100;

        globales.modoDeCierreDeLecturas = Globales.NINGUNO;

        globales.mostrarGrabarEnSD = false;

        globales.mostrarCalidadFoto = true;

        globales.legacyCaptura = false;

        globales.puedoVerLosDatos = false;

        globales.sobreEscribirServidorConDefault = true;

        InicializarMatrizDeCompatibilidades();
        inicializaTablaDeCalidades();

        agregarAnomalias();
    }

    private void agregarAnomaliasMexicana() {
        // TODO Auto-generated method stub
        //Agregaremos las anomalias
        openDatabase();
        ContentValues cv_anom = new ContentValues();

        db.execSQL("Delete from Anomalia");
        agregarAnomalia(db, "1", "Medidor Dañado ", 1, 0);
        agregarAnomalia(db, "2", "Carátula Dañada", 1, 0);
        agregarAnomalia(db, "3", "Mica Opaca", 1, 0);
        agregarAnomalia(db, "4", "Medidor Obstruido", 1, 1);
        agregarAnomalia(db, "5", "Cliente Conflictivo ", 1, 1);
        agregarAnomalia(db, "7", "Cliente no permitió realizar proceso", 1, 1);
        agregarAnomalia(db, "8", "Medidor con Reja o Protector ", 1, 0);
        agregarAnomalia(db, "9", "Medidor Empotrado ", 1, 0);
        agregarAnomalia(db, "10", "Número de Medidor Diferente", 1, 1);
        agregarAnomalia(db, "11", "Medidor en interior", 1, 0);
        agregarAnomalia(db, "12", "No se Encontró Acometida", 1, 1);
        agregarAnomalia(db, "14", "Perro Bravo ", 1, 0);
        agregarAnomalia(db, "15", "Zona Conflictiva", 0, 0);
        agregarAnomalia(db, "16", "Falta de Material", 0, 1);
        agregarAnomalia(db, "17", "Guardia no Permitió la Entrada", 1, 1);
        agregarAnomalia(db, "18", "Válvula Dañada ", 1, 0);
        agregarAnomalia(db, "19", "Válvula Enterrada", 1, 0);
        agregarAnomalia(db, "20", "Medidor Enterrado ", 1, 0);
        agregarAnomalia(db, "21", "Sin Válvula", 1, 0);
        agregarAnomalia(db, "22", "Predio sin Medidor", 1, 0);
        agregarAnomalia(db, "23", "Domicilio Inaccesible", 1, 1);
        agregarAnomalia(db, "24", "Posible uso ilícito", 1, 1);
        agregarAnomalia(db, "25", "Medidor Conectado al Revés", 1, 1);
        agregarAnomalia(db, "26", "Pie derecho desoldado", 1, 1);
        agregarAnomalia(db, "27", "Fuga en red interior", 1, 1);
        agregarAnomalia(db, "28", "Fuga en pie derecho", 1, 1);
        agregarAnomalia(db, "29", "Fuga en medidor y conexión", 1, 1);
        agregarAnomalia(db, "30", "Cambio de medidor", 1, 1);
        agregarAnomalia(db, "31", "Ya está cortado", 1, 1);
        agregarAnomalia(db, "32", "Tuercas muy apretadas", 1, 1);
        agregarAnomalia(db, "33", "Cliente se reconectó", 1, 1);
        agregarAnomalia(db, "99", "Texto libre", 1, 1);

        agregarAnomalia(db, "101", "Fuga", 1, 0, "I");
        agregarAnomalia(db, "102", "Falta PH", 1, 0, "I");
        agregarAnomalia(db, "103", "Pie Derecho", 1, 0, "I");
        agregarAnomalia(db, "104", "Red Interior", 1, 0, "I");
        agregarAnomalia(db, "105", "Sin Acometida", 1, 0, "I");
        agregarAnomalia(db, "106", "Sin Calca", 1, 0, "I");
        agregarAnomalia(db, "107", "Requisitos", 1, 0, "I");
        agregarAnomalia(db, "108", "Texto Libre", 1, 0, "I");
        agregarAnomalia(db, "109", "Serie Duplicada", 1, 0, "I");


        db.execSQL("Delete from codigosEjecucion");
        agregarCodigoEjecucion(db, "10", "Cortado");
//		agregarCodigoEjecucion( db, "11", "Cortado con Diametro De 1/2\"");
//		agregarCodigoEjecucion( db, "12", "Cortado con Diametro De 3/4\"");
//		agregarCodigoEjecucion( db, "13", "Cortado con Diametro De 1\"");
        agregarCodigoEjecucion(db, "20", "Cliente Presentó Pago ");
//		agregarCodigoEjecucion( db, "21", "Pago en Sucursal ");
//		agregarCodigoEjecucion( db, "22", "Pago en Cajero Automático ");
//		agregarCodigoEjecucion( db, "23", "Pago en Medio Externo");
        agregarCodigoEjecucion(db, "30", "No se Realizó El Corte");
//		agregarCodigoEjecucion( db, "31", "Impedimiento Temporal ");
//		agregarCodigoEjecucion( db, "32", "Impedimiento Permanente");
        agregarCodigoEjecucion(db, "40", "No Localizado ", 0);
//		agregarCodigoEjecucion( db, "41", "No se Localizó la Colonia en Municipio", 0);
//		agregarCodigoEjecucion( db, "42", "No se Localizó la Calle en Colonia", 0);
//		agregarCodigoEjecucion( db, "43", "No se Localizó No. Exterior de la Dirección", 0);
//		agregarCodigoEjecucion( db, "44", "No se Localizó la Acometida", 0);
        agregarCodigoEjecucion(db, "50", "No Visitado", 0);
//		agregarCodigoEjecucion( db, "51", "Fin de Jornada", 0);
//		agregarCodigoEjecucion( db, "52", "Inclemencias del Clima", 0);
//		agregarCodigoEjecucion( db, "53", "Falta de Iluminación Solar", 0);
//		agregarCodigoEjecucion( db, "54", "Instrucción/Orden Superior", 0);
//		agregarCodigoEjecucion( db, "55", "Accidente/Enfermedad", 0);
//		agregarCodigoEjecucion( db, "56", "Falla Vehicular", 0);
        agregarCodigoEjecucion(db, "60", "Reconexión Realizada");
        agregarCodigoEjecucion(db, "70", "No se Realizó la Reconexión");
        agregarCodigoEjecucion(db, "80", "Cliente Autoreconectado");
        agregarCodigoEjecucion(db, "90", "Remoción Realizada");

        closeDatabase();
    }

    private void agregarAnomalias() {
        // TODO Auto-generated method stub
        //Agregaremos las anomalias
        openDatabase();
        ContentValues cv_anom = new ContentValues();

        db.execSQL("Delete from Anomalia");
        agregarAnomalia(db, "1", "1 - Sin efectos ", 0, 0, "D");
        agregarAnomalia(db, "2", "2 - Limitación de producción ", 0, 0, "D");
        agregarAnomalia(db, "3", "3 - Parada de la producción ", 0, 0, "D");
        agregarAnomalia(db, "4", "B - Reja Perimetral ", 1, 1);
        agregarAnomalia(db, "5", "C - Medidor interno  ", 1, 1);
        agregarAnomalia(db, "7", "D - Cliente no permitió. Cliente agresivo ", 1, 1);
        agregarAnomalia(db, "8", "E - Se encontró servicio abierto ", 1, 1, "I");
        agregarAnomalia(db, "9", "F - Retiro de válvula o regulador ", 1, 1);
        agregarAnomalia(db, "10", "G - Cliente ya pagó en Banco o Agencia ", 1, 1, "M", 1);
        agregarAnomalia(db, "11", "H - Dirección incorrecta del servicio ", 1, 1);
        agregarAnomalia(db, "12", "I - Condición insegura ", 1, 1);
        agregarAnomalia(db, "14", "J - No usuario ", 1, 1, "I");
        agregarAnomalia(db, "15", "K - Faltante de tubería ", 1, 1, "I");
        agregarAnomalia(db, "16", "L - Duplicada ", 0, 0, "I");
        agregarAnomalia(db, "46", "L - Duplicada ", 0, 0);
        agregarAnomalia(db, "17", "M - Medidor enrejado ", 1, 1);
        agregarAnomalia(db, "18", "N - No enviada a campo ", 0, 0, "D");
        agregarAnomalia(db, "19", "O - Toma compartida ", 1, 1);
        agregarAnomalia(db, "20", "R - Zona de riesgo ", 1, 1);
        agregarAnomalia(db, "21", "S - Sin válvula ", 1, 1);
        agregarAnomalia(db, "22", "V - Vigilancia no permite ", 1, 1);
        agregarAnomalia(db, "23", "X - Órdenes erróneas ", 0, 0);
        agregarAnomalia(db, "43", "X - Órdenes erróneas ", 0, 0, "I");
        agregarAnomalia(db, "24", "Z - Otro ", 1, 1);
        agregarAnomalia(db, "31", "E - Se encontró medidor instalado ", 1, 1, "R");
        agregarAnomalia(db, "32", "J - No usuario ", 1, 1, "R");
        agregarAnomalia(db, "33", "K - Faltante de tubería ", 1, 1, "R");
        agregarAnomalia(db, "16", "L - Duplicada ", 0, 0, "R");
        agregarAnomalia(db, "43", "X - Órdenes erróneas ", 0, 0, "R");
        agregarAnomalia(db, "99", "Texto libre", 1, 1, "D");

        db.execSQL("Delete from codigosEjecucion");
        agregarCodigoEjecucion(db, "10", "Cortado");
        agregarCodigoEjecucion(db, "20", "Cliente Presentó Pago ");
        agregarCodigoEjecucion(db, "30", "No se Realizó El Corte");
        agregarCodigoEjecucion(db, "40", "No Localizado ", 0);
        agregarCodigoEjecucion(db, "50", "No Visitado", 0);
        agregarCodigoEjecucion(db, "60", "Reconexión Realizada");
        agregarCodigoEjecucion(db, "70", "No se Realizó la Reconexión");
        agregarCodigoEjecucion(db, "80", "Cliente Autoreconectado");
        agregarCodigoEjecucion(db, "90", "Remoción Realizada");

        closeDatabase();
    }

    public void agregarAnomalia(SQLiteDatabase db, String anomalia, String desc) {
        agregarAnomalia(db, anomalia, desc, 1, 1);
    }

    public void agregarAnomalia(SQLiteDatabase db, String anomalia, String desc, int foto, int mensaje) {
        this.context = context;
        Resources res = context.getResources();

        //openDatabase();
        ContentValues cv_params = new ContentValues();
        cv_params.put("desc", desc);
        cv_params.put("conv", desc.substring(0, 1));
        cv_params.put("capt", 0);
        cv_params.put("subanomalia", ".");
        cv_params.put("ausente", "4");
        cv_params.put("mens", mensaje);
        cv_params.put("lectura", 0);
        cv_params.put("anomalia", anomalia);
        cv_params.put("activa", "A");
        cv_params.put("tipo", "M");
        cv_params.put("pais", "M");
        cv_params.put("foto", foto);

        db.insert("Anomalia", null, cv_params);
    }

    public void agregarAnomalia(SQLiteDatabase db, String anomalia, String desc, int foto, int mensaje, String tipo) {
        this.context = context;
        Resources res = context.getResources();

        //openDatabase();
        ContentValues cv_params = new ContentValues();
        cv_params.put("desc", desc);
        cv_params.put("conv", desc.substring(0, 1));
        cv_params.put("capt", 0);
        cv_params.put("subanomalia", ".");
        cv_params.put("ausente", "4");
        cv_params.put("mens", mensaje);
        cv_params.put("lectura", 0);
        cv_params.put("anomalia", anomalia);
        cv_params.put("activa", "A");
        cv_params.put("tipo", tipo);
        cv_params.put("pais", "M");
        cv_params.put("foto", foto);

        db.insert("Anomalia", null, cv_params);
    }

    public void agregarAnomalia(SQLiteDatabase db, String anomalia, String desc, int foto, int mensaje, String tipo, int capt) {
        this.context = context;
        Resources res = context.getResources();

        //openDatabase();
        ContentValues cv_params = new ContentValues();
        cv_params.put("desc", desc);
        cv_params.put("conv", desc.substring(0, 1));
        cv_params.put("capt", capt);
        cv_params.put("subanomalia", ".");
        cv_params.put("ausente", "4");
        cv_params.put("mens", mensaje);
        cv_params.put("lectura", 0);
        cv_params.put("anomalia", anomalia);
        cv_params.put("activa", "A");
        cv_params.put("tipo", tipo);
        cv_params.put("pais", "M");
        cv_params.put("foto", foto);

        db.insert("Anomalia", null, cv_params);
    }

    public void agregarCodigoEjecucion(SQLiteDatabase db, String anomalia, String desc) {
        agregarCodigoEjecucion(db, anomalia, desc, 1);
    }

    public void agregarCodigoEjecucion(SQLiteDatabase db, String anomalia, String desc, int foto) {
        this.context = context;
        Resources res = context.getResources();

        //openDatabase();
        ContentValues cv_params = new ContentValues();
        cv_params.put("desc", desc);
        cv_params.put("conv", desc.substring(0, 1));
        cv_params.put("capt", 0);
        cv_params.put("subanomalia", ".");
        cv_params.put("ausente", "4");
        cv_params.put("mens", 0);
        cv_params.put("lectura", 0);
        cv_params.put("anomalia", anomalia);
        cv_params.put("activa", "A");
        cv_params.put("tipo", "M");
        cv_params.put("pais", "M");
        cv_params.put("foto", foto);

        db.insert("CodigosEjecucion", null, cv_params);
    }

    /**
     * Validacion de una lectura
     *
     * @param ls_lectAct
     * @return Regresa el mensaje de error
     */
    public String validaLectura(String ls_lectAct) throws Exception {

        if (ls_lectAct.equals("")) {
            return NO_SOSPECHOSA + "|" + "No se ha ingresado ninguna lectura";
        }

// CE, 11/10/23, Las lecturas deben tener exactamente 3 decimales y de 1 a 5 enteros
        int nPosicionDelPunto = 0;
        nPosicionDelPunto = ls_lectAct.indexOf('.');
        if (nPosicionDelPunto == 0)
            return NO_SOSPECHOSA + "|" + "Las lecturas deben tener al menos un número entero";
        if (nPosicionDelPunto > 5)
            return NO_SOSPECHOSA + "|" + "Las lecturas deben tener un máximo de cinco enteros";
        if (nPosicionDelPunto == -1)
            return NO_SOSPECHOSA + "|" + "Las lecturas deben tener exactamente tres decimales";
        if (nPosicionDelPunto != ls_lectAct.length() - 4)
            return NO_SOSPECHOSA + "|" + "Las lecturas deben tener exactamente tres decimales";

//*****************************************************************************************
// CE, 04/10/23, Vamos a aceptar cualquier lectura escrita por el técnico
/*
        openDatabase();
        globales.ignorarContadorControlCalidad = true;

        Cursor c = db.rawQuery("Select count(*) canti from codigosEjecucion where anomalia='" + ls_lectAct + "'", null);

        c.moveToFirst();
        if (Utils.getInt(c, "canti", 0) == 0) {
            return NO_SOSPECHOSA + "|" + "El código ingresado es invalido";
        }

        if (globales.tll.getLecturaActual().is_tipoDeOrden.equals("TO002") && Integer.parseInt(ls_lectAct) > 59) {//Corte
            return NO_SOSPECHOSA + "|" + "El código no corresponde a las desconexiones";
        }

        if (globales.tll.getLecturaActual().is_tipoDeOrden.equals("TO005") && Integer.parseInt(ls_lectAct) > 59) {//Corte
            return NO_SOSPECHOSA + "|" + "El código no corresponde a las remociones";
        }

        if (globales.tll.getLecturaActual().is_tipoDeOrden.equals("TO003") && Integer.parseInt(ls_lectAct) < 40) {//Reconexion
            return NO_SOSPECHOSA + "|" + "El código no corresponde a las reconexiones";
        }

        if (globales.tll.getLecturaActual().is_tipoDeOrden.equals("TO004") && Integer.parseInt(ls_lectAct) < 40) {//Reconexion
            return NO_SOSPECHOSA + "|" + "El código no corresponde a las rec/remos";
        }

        closeDatabase();

// CE, 10/04/15, Vamos a hacer una prueba para que no pida foto en No Visitado y No Localizado
        globales.fotoForzada = true;
//		if (Integer.parseInt(ls_lectAct) > 39 && Integer.parseInt(ls_lectAct) < 60)
//			globales.fotoForzada=false;
//		else
//			globales.fotoForzada=true;

        globales.is_terminacion = "_1";
        globales.ignorarContadorControlCalidad = true;
*/
        return "";
//
//		int esferas=0;
//
//		if (globales.tll.getLecturaActual().numerodeesferasReal.equals(""))
//			esferas=globales.tll.getLecturaActual().numerodeesferas;
//		else
//			esferas=Integer.parseInt(globales.tll.getLecturaActual().numerodeesferasReal);
//
//		if (ls_lectAct.length() > esferas) {
//			return NO_SOSPECHOSA + "|" + globales.getString(R.string.msj_validacion_esferas);
//		}
//
//		if (ls_lectAct.equals("")) {
//			return NO_SOSPECHOSA +"|"+ globales.getString(R.string.msj_validacion_no_hay_lectura);
//		}
//
//		long ll_lectAct = Long.parseLong(ls_lectAct);
//
//		//La anomalia H, como es cambio de medidor siempre va a aceptar la lectura
//		if (globales.tll.getLecturaActual().getAnomaliasCapturadas().contains("H")){
//			return "";
//		}
//
//		//is_lectAnt tiene la lectura anteriormente ingresada, si esta vacio, quiere decir que iniciamos con las validaciones.
//		if (is_lectAnt.equals("")) {
//			//Si el estado del suministro es cortado, debería aceptar la lectura anterior
//			if (ls_lectAct.equals(String.valueOf(globales.tll.getLecturaActual().lecturaAnterior))&& (globales.tll.getLecturaActual().estadoDelSuministro.equals("1") || globales.tll.getLecturaActual().estadoDelSuministro.equals("2"))){
//				is_lectAnt = ls_lectAct;
//				return "";
//			}else if (!ls_lectAct.equals(String.valueOf(globales.tll.getLecturaActual().lecturaAnterior))&& (globales.tll.getLecturaActual().estadoDelSuministro.equals("1") || globales.tll.getLecturaActual().estadoDelSuministro.equals("2"))){
//				is_lectAnt = ls_lectAct;
//				return /*TomaDeLecturas.FUERA_DE_RANGO;*/ SOSPECHOSA +"|"+ globales.getString(globales.mensajeDeConfirmar);
//			}
//
//			//Si consumo y baremo son 0 y no esta cortado debe confirmar
//			if (getConsumo(ls_lectAct)==0 && globales.tll.getLecturaActual().baremo==0  && !(globales.tll.getLecturaActual().estadoDelSuministro.equals("1") || globales.tll.getLecturaActual().estadoDelSuministro.equals("2"))){
//				is_lectAnt = ls_lectAct;
//				return /*TomaDeLecturas.FUERA_DE_RANGO;*/ SOSPECHOSA +"|"+ globales.getString(globales.mensajeDeConfirmar);
//			}
//			//Hay que checar si las estimaciones son  mayores para pedir confirmacion.
//			if (Integer.parseInt(globales.tll.getLecturaActual().is_estimaciones)>0){
//				is_lectAnt = ls_lectAct;
//				globales.ignorarContadorControlCalidad=true;
//				return /*TomaDeLecturas.FUERA_DE_RANGO;*/ SOSPECHOSA +"|"+ globales.getString(globales.mensajeDeConfirmar);
//			}
//			if (globales.il_lect_max < ll_lectAct || globales.il_lect_min > ll_lectAct
//					||  globales.tll.getLecturaActual().confirmarLectura() /*|| globales.bModificar*/) {
//				is_lectAnt = ls_lectAct;
//				boolean seEquivoco = false;
//
//				if (globales.il_lect_max < ll_lectAct || globales.il_lect_min > ll_lectAct) {
//					seEquivoco = true;
//				}
//
//				if (globales.tll.getLecturaActual().is_supervisionLectura.equals("1")) {
//					globales.ignorarContadorControlCalidad=true;
//					if (seEquivoco)
//						globales.is_terminacion = "Y1";
//					else
//						globales.is_terminacion = "-S";
//				}
//
//				if (globales.tll.getLecturaActual().is_reclamacionLectura.equals("1")) {
//					globales.ignorarContadorControlCalidad=true;
//					if (seEquivoco)
//						globales.is_terminacion = "X1";
//					else
//						globales.is_terminacion = "-R";
//				}
//
//				//sonidos.playSoundMedia(Sonidos.URGENT);
//
//				return /*TomaDeLecturas.FUERA_DE_RANGO;*/ SOSPECHOSA +"|"+ globales.getString(globales.mensajeDeConfirmar);
//			}
//		}
//		else {
//
//			if (!is_lectAnt.equals(ls_lectAct)) {
//				is_lectAnt = ls_lectAct;
//				//Contador de lectura distinta
//				globales.tll.getLecturaActual().intentos=String.valueOf(globales.tll.getLecturaActual().intentos)+1;
//				//sonidos.playSoundMedia(Sonidos.URGENT);
//				globales.ignorarContadorControlCalidad=true;
//				return /*NO_*/SOSPECHOSA +"|"+ globales.getString(R.string.msj_lecturas_verifique);
//			}
//
//		}
//
////		if (is_lectAnt.equals("")) {
////			//sonidos.playSoundMedia(Sonidos.BEEP);
////		}
//		//Borramos la anomalia 4 cuando campturemos la lectura
//		globales.tll.getLecturaActual().deleteAnomalia("4");
//		is_lectAnt = "";
//		return "";
    }

    public String getNombreFoto(Globales globales, SQLiteDatabase db, long secuencial, String is_terminacion) {
        String ls_nombre = "", ls_unicom;
        Cursor c;
        /**
         * Este es el fotmato del nombre de la foto
         *
         * Poliza a 8 posiciones, ultimos 4 digitos del encabezado (Itinerario), los 2 digitos antes de los 4 anteriores (Ruta), YYYYMMDDHHIISS
         *
         * la terminacion... -1 Regularmente
         * Si es de anomalia ... La anomalia ingresada
         *
         */
//Quiero su nis_rad

//    	c= db.rawQuery("Select poliza from ruta where cast(secuenciaReal as Integer) ="+secuencial, null);
//    	c.moveToFirst();
//
//    	ls_nombre+=Main.rellenaString(c.getString(c.getColumnIndex("poliza")), "0", 7, true);
//
//    	c.close();
//
//		c= db.rawQuery("Select registro from encabezado", null);
//		ls_nombre+="20";
//
//    	c.moveToFirst();
//    	ls_unicom= new String (c.getBlob(c.getColumnIndex("registro")));
//
//    	ls_nombre+= ls_unicom.substring(ls_unicom.length()-4, ls_unicom.length())+ls_unicom.substring(ls_unicom.length()-6, ls_unicom.length()-4);
//    	c.close();
//
//
//
//    	//ls_nombre=caseta+ "_"+ secuencial + "_" + Main.obtieneFecha()+".jpg";
//
//    	ls_nombre+=Main.obtieneFecha("ymdhis");

        ls_nombre = Main.rellenaString(globales.tll.getLecturaActual().is_numOrden.trim(), "0", globales.tlc.getLongCampo("numOrden"), true) + "_" + Main.rellenaString(globales.tll.getLecturaActual().poliza.trim(), "0", globales.tlc.getLongCampo("poliza"), true);
        //Hay que preguntar por la terminacion
        ls_nombre += Main.obtieneFecha("ymdhis") + ".JPG";

        return ls_nombre;
    }

        /*
        Función para regresar datos de la foto, incluyendo datos de nombre, unidad, porción, regional.
     */

    // RL, 2023-01-02, Regresar una estructura de datos, con la información suficiente para transmitir la foto con sus datos relacionados.

    public DatosEnvioEntity getInfoFoto(Globales globales, SQLiteDatabase db, long secuencial, String is_terminacion) throws Exception {
        String ls_nombre = "", ls_unicom;
        Cursor c;
        DatosEnvioEntity infoFoto = new DatosEnvioEntity();
        Lectura lect;

        /**
         * Este es el fotmato del nombre de la foto
         *
         * NumMedidor a 10 posiciones,
         * fecha	  a YYYYMMDD
         * hora		  a HHMMSS
         */

        lect = globales.tll.getLecturaActual();

        ls_nombre = Main.rellenaString(lect.is_numOrden.trim(), "0", globales.tlc.getLongCampo("numOrden"), true) + "_" + Main.rellenaString(lect.poliza.trim(), "0", globales.tlc.getLongCampo("poliza"), true);
        //Hay que preguntar por la terminacion
        ls_nombre += Main.obtieneFecha("ymdhis") + ".JPG";

        infoFoto.nombreArchivo = ls_nombre;
        infoFoto.SerieMedidor = lect.getSerieMedidor();
        infoFoto.idOrden = Utils.convToLong(lect.poliza);
//        infoFoto.idArchivo = lect.idArchivo;
        infoFoto.idEmpleado = globales.getIdEmpleado();
//        infoFoto.Unidad = lect.unidad;
//        infoFoto.Regional = lect.mRegional;
//        infoFoto.Porcion = lect.mPorcion;
        infoFoto.Lectura = lect.getLectura();

        return infoFoto;
    }

    // RL, 2023-01-02, Regresar una estructura de datos, con la información suficiente para transmitir la foto con sus datos relacionados.

    public DatosEnvioEntity getInfoFoto(Globales globales, SQLiteDatabase db) throws Exception {
        String ls_nombre = "", ls_unicom;
        Cursor c;
        DatosEnvioEntity infoFoto = new DatosEnvioEntity();
        Lectura lect;

        /**
         * Este es el fotmato del nombre de la foto
         *
         * NumMedidor a 10 posiciones,
         * fecha	  a YYYYMMDD
         * hora		  a HHMMSS
         */

        try {
            infoFoto.nombreArchivo = "";
            infoFoto.idEmpleado = globales.getIdEmpleado();

            if (globales == null)
                return infoFoto;

            if (globales.tll == null)
                return infoFoto;

            lect = globales.tll.getLecturaActual();

            if (lect == null)
                return infoFoto;

            infoFoto.idOrden = Utils.convToLong(lect.poliza);
//            infoFoto.idArchivo = lect.idArchivo;
//            infoFoto.Unidad = lect.unidad;
//            infoFoto.Regional = lect.mRegional;
//            infoFoto.Porcion = lect.mPorcion;
        } catch (Exception e) {
            throw new Exception("Error al obtener información de la lectura");
        }

        return infoFoto;
    }

    public String getDatosSAP(Lectura lectura, int nCampo) throws Exception {
        String strCardViewMedidor = "";
        switch (nCampo) {
            case 1:
                strCardViewMedidor = lectura.is_serieMedidor;
                break;
            case 2:
                strCardViewMedidor = lectura.is_cuentaContrato;
                break;
            case 3:
                strCardViewMedidor = lectura.poliza;
                break;
            case 4:
                strCardViewMedidor = lectura.is_numAviso;
                break;
        }
        return strCardViewMedidor;
    }

    public String getDatosDelCliente(Lectura lectura) throws Exception {
        String strClienteMasSaldo = "";
        strClienteMasSaldo = lectura.getNombreCliente().trim();
        if (!lectura.is_vencido.equals(""))
            if ((lectura.getTipoDeOrden().equals("DESCONEXIÓN") || lectura.getTipoDeOrden().equals("REMOCIÓN")))
                strClienteMasSaldo += "\nSALDO:  $ " + lectura.is_vencido;
        return strClienteMasSaldo;
//        return lectura.getNombreCliente().trim();
    }

    public String getDatosDireccion(Lectura lectura) throws Exception {
        String strNuevaDireccion = "";
        String comodin = "";
        if (!lectura.is_numOrden.equals("0")) {
            strNuevaDireccion += lectura.is_comollegar1;
// CE, 25/10/23, Vamos a empezar a recibir la direccion en un solo campo, por eso no necesitamos mostrar los demas
            //            strNuevaDireccion += lectura.is_calle + " No. " + lectura.numeroDeEdificio.trim() + (!lectura.numeroDePortal.trim().equals("") ? " - " + lectura.numeroDePortal.trim() : "");
            strNuevaDireccion += lectura.is_calle;
            if (!lectura.getColonia().equals("")) {
                strNuevaDireccion += "\n" + lectura.getColonia();
            }
            if (!lectura.is_entrecalles.equals("")) {
                strNuevaDireccion += "\n" + lectura.is_entrecalles;
            }
            if (!lectura.is_escalera.trim().equals("")) {
                comodin = "\n" + "Esc: " + lectura.is_escalera.trim();
            }
            if (!lectura.is_piso.trim().equals("")) {
                if (!comodin.equals(""))
                    comodin += " ";
                comodin += "Piso: " + lectura.is_piso.trim();
            }
            if (!lectura.is_puerta.trim().equals("")) {
                if (!comodin.equals(""))
                    comodin += " ";
                comodin += "Puerta: " + lectura.is_puerta.trim();
            }
            if (!comodin.equals("")) {
                strNuevaDireccion += comodin;
            }
            if (lectura.miLatitud.equals("") || lectura.miLongitud.equals(""))
                strNuevaDireccion = strNuevaDireccion + "\nMedidor sin Punto GPS";
            else if ((globales.location == null)) {
                strNuevaDireccion = strNuevaDireccion + "\nTécnico sin Ubicación Actual";
            } else {
                try {
                    float distanciaEnMetros = 0;
                    Location origen = new Location("Origen");
                    origen.setLatitude(globales.location.getLatitude());
                    origen.setLongitude(globales.location.getLongitude());
                    Location destino = new Location("Destino");
                    destino.setLatitude(Float.parseFloat(lectura.miLatitud));
                    destino.setLongitude(Float.parseFloat(lectura.miLongitud));
                    distanciaEnMetros = origen.distanceTo(destino);
                    strNuevaDireccion = strNuevaDireccion + "\nDISTANCIA: " + String.format(Locale.US, "%,.0f", distanciaEnMetros) + " MTS.";
                } catch (Exception e) {
                    throw new Exception("Error al obtener punto GPS del técnico");
                }
            }
        }
        return strNuevaDireccion;
    }

    public Vector<String> getInformacionDelMedidor(Lectura lectura) throws Exception {
        if (lectura == null)
            throw new Exception("El dato está vacío en getInformacionDelMedidor");

        Vector<String> datos = new Vector<String>();

        //Esta variable la usaremos para poder determinar si algun dato es vacio y mostrar solo lo necesario en la pantalla
        String comodin = "";

        //datos.add(lectura.is_tipoDeOrden);
        String strCardViewMedidor = "";
        strCardViewMedidor = "Medidor: " + lectura.is_serieMedidor;
//        datos.add("Medidor: " + lectura.is_serieMedidor);

 /*       if (lectura.is_tipoDeOrden.equals("TO002")){
            datos.add("Tipo de Orden: Desconexión");
        }else if (lectura.is_tipoDeOrden.equals("TO003")){
            datos.add("Tipo de Orden: Reconexión");
        }else if (lectura.is_tipoDeOrden.equals("TO005")){
            datos.add("Tipo de Orden: Remoción");
        }else if (lectura.is_tipoDeOrden.equals("TO004")){
            datos.add("Tipo de Orden: Rec/Remo");
        }
        else{
            datos.add("Tipo de Orden: Orden manual");
        }*/
//        datos.add("Tipo de Orden: " + lectura.getTipoDeOrden());
/*
        datos.add("Cuenta Contrato: "+lectura.is_cuentaContrato);
        datos.add("Interlocutor: "+lectura.poliza);
        datos.add("Aviso SAP: "+lectura.is_numAviso);
        datos.add("Adeudo: " + lectura.is_vencido);
*/
        strCardViewMedidor += "\nCuenta Contrato: " + lectura.is_cuentaContrato;
        strCardViewMedidor += "\nInterlocutor: " + lectura.poliza;
        strCardViewMedidor += "\nAviso SAP: " + lectura.is_numAviso;
        strCardViewMedidor += "\nAdeudo: " + lectura.is_vencido;
        datos.add(strCardViewMedidor);
/*
            if (!lectura.is_EncuestaDeSatisfaccion.equals("")) {
                datos.add("Encuesta: " + lectura.is_EncuestaDeSatisfaccion);
            }
            if (!lectura.is_idMaterialUtilizado.equals("")) {
                datos.add("Material: " + lectura.is_idMaterialUtilizado);
            }
 //       }
*/
        String strNuevaDireccion = "";
        if (!lectura.is_numOrden.equals("0")) {
            datos.add(lectura.is_comollegar1);
            strNuevaDireccion += lectura.is_comollegar1;
//			if (lectura.verDatos){
            datos.add(lectura.is_calle + " #" + lectura.numeroDeEdificio.trim() + (!lectura.numeroDePortal.trim().equals("") ? "-" + lectura.numeroDePortal.trim() : ""));
            strNuevaDireccion += lectura.is_calle + " #" + lectura.numeroDeEdificio.trim() + (!lectura.numeroDePortal.trim().equals("") ? "-" + lectura.numeroDePortal.trim() : "");
//			}
//			else{
//				datos.add(lectura.is_calle );
//			}
//
            if (!lectura.getColonia().equals("")) {
                datos.add(lectura.getColonia());
                strNuevaDireccion += lectura.getColonia();
            }


            if (!lectura.is_entrecalles.equals("")) {
                datos.add(lectura.is_entrecalles);
                strNuevaDireccion += lectura.is_entrecalles;
            }

//			if (lectura.verDatos){
            //Escalera
            if (!lectura.is_escalera.trim().equals("")) {
                comodin = "Esc: " + lectura.is_escalera.trim();
            }


            //Piso
            if (!lectura.is_piso.trim().equals("")) {
                if (!comodin.equals(""))
                    comodin += " ";
                comodin += "Piso: " + lectura.is_piso.trim();
            }

            // Puerta
            if (!lectura.is_puerta.trim().equals("")) {
                if (!comodin.equals(""))
                    comodin += " ";
                comodin += "Puerta: " + lectura.is_puerta.trim();
            }
//			}

            if (!comodin.equals("")) {
                datos.add(comodin);
                strNuevaDireccion += comodin;
            }
//			if (lectura.verDatos){
            datos.add(lectura.getNombreCliente().trim());
//			}
//            TomaDeLecturas.setDatoEnTextView(2,lectura.getNombreCliente().trim());
//            TomaDeLecturas.setDatoEnTextView(3,strNuevaDireccion);
        }


        //Vamos a agregar los campos que se van llenando mientras se agregan anomalias
        String ls_anom = lectura.getAnomaliasCapturadas();
        //if (ls_anom.contains("B") || ls_anom.contains("H")){

        String ls_comentarios = "";
        if (!globales.tll.getLecturaActual().getAnomaliaAMostrar().equals("")) {
            // Tiene una anomalia
            ls_comentarios = context.getString(R.string.str_anomalia) + ": " + globales.is_presion;
            if (!globales.tll.getLecturaActual().getSubAnomaliaAMostrar().equals("")) {
                // Tiene una subanomalia
                ls_comentarios += /*", " */ "\n" + context.getString(R.string.str_subanomalia) + ": "
                        + globales.tll.getLecturaActual().getSubAnomaliaAMostrar();
            }
            ls_comentarios += "\n";

        }
        ls_comentarios = "\n" + ls_comentarios
                + globales.tll.getLecturaActual().getComentarios();
        if (!ls_comentarios.trim().equals("")) ;
        datos.add(ls_comentarios);

        if (!lectura.is_ClienteYaPagoMonto.equals("")) {
            datos.add("Monto Pagado: " + lectura.is_ClienteYaPagoMonto);
            datos.add("Fecha de Pago: " + lectura.is_ClienteYaPagoFecha);
            datos.add("Agente: " + lectura.is_ClienteYaPagoAgente);
        }

        //       if (!lectura.is_CancelarEnApp.equals("")) datos.add("CancelarEnApp: " + lectura.is_CancelarEnApp);

        if (!lectura.is_QuienAtendio.equals(""))
            datos.add("QuienAtendio: " + lectura.is_QuienAtendio);
        if (!lectura.is_MarcaInstalada.equals(""))
            datos.add("MarcaInstalada: " + lectura.is_MarcaInstalada);
        if (!lectura.is_SeQuitoTuberia.equals(""))
            datos.add("SeQuitoTuberia: " + lectura.is_SeQuitoTuberia);
        if (!lectura.is_TuberiaRetirada.equals(""))
            datos.add("TuberiaRetirada: " + lectura.is_TuberiaRetirada);
        if (!lectura.is_MarcaRetirada.equals(""))
            datos.add("MarcaRetirada: " + lectura.is_MarcaRetirada);
        if (!lectura.is_MedidorRetirado.equals(""))
            datos.add("MedidorRetirado: " + lectura.is_MedidorRetirado);

        if (!lectura.is_TextoLibreSAP.equals("")) datos.add("\n" + lectura.is_TextoLibreSAP);
        //}


        //datos.add(lectura.getDireccion());

//		//Vamos a obtener cual es el tipo de consumo
//		int li_clave = Integer
//				.parseInt(lectura.is_tarifa.trim()
//						.substring(2));
//		String tipoConsumo="";
//
//		switch (li_clave) {
//		case 11:
//			tipoConsumo = "Cliente Doméstico";
//			break;
//		case 12:
//			tipoConsumo = "Cliente Comercial";
//			break;
//
//		default:
//			tipoConsumo = globales.tll.getLecturaActual().is_tarifa;
//			break;
//		}
//
//			datos.add(tipoConsumo);

        return datos;

//		TextView tv_view;
//		LinearLayout ll_linear;
//		LayoutParams layout_params;
//
//		ll_generico.removeAllViewsInLayout();
//
//
//		//La primera linea debe tener el acceso y el estado del suministro en la misma linea
//		//El acceso debe ser 70% y el estado un 30%
//
//		ll_linear=new LinearLayout(context);
//		tv_view=new TextView(context);
//
//
//		ll_linear.setOrientation(LinearLayout.HORIZONTAL);
//
//		layout_params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//
//		tv_view.setText(lectura.getAcceso());
//		tv_view.setTextSize(TypedValue.COMPLEX_UNIT_PX,fontSize);
//		layout_params.weight=0.70f;
//		ll_linear.addView(tv_view, layout_params);
//
//
//		//Ahora, el estado del suministro
//		tv_view=new TextView(context);
//		layout_params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//
//		tv_view.setText(lectura.estadoDelSuministro);
//		tv_view.setTextSize(TypedValue.COMPLEX_UNIT_PX,fontSize);
//		layout_params.weight=0.20f;
//		layout_params.gravity= Gravity.RIGHT;
//		ll_linear.addView(tv_view, layout_params);
//
//		layout_params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
//		ll_generico.addView(ll_linear, layout_params);
//
//
//		//Ahora el nombre del cliente
//		layout_params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
//		tv_view=new TextView(context);
//		tv_view.setTextSize(TypedValue.COMPLEX_UNIT_PX,fontSize);
//		tv_view.setText(lectura.getNombreCliente());
//		ll_generico.addView(tv_view, layout_params);
//
//		//Direccion
//		layout_params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
//		tv_view=new TextView(context);
//		tv_view.setTextSize(TypedValue.COMPLEX_UNIT_PX,fontSize);
//		tv_view.setText(lectura.getDireccion());
//		ll_generico.addView(tv_view, layout_params);
//
//		//Marca
//		layout_params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
//		tv_view=new TextView(context);
//		tv_view.setTextSize(TypedValue.COMPLEX_UNIT_PX,fontSize);
//		tv_view.setText("HOLA");
//		ll_generico.addView(tv_view, layout_params);
//
//		//Vamos a obtener cual es el tipo de consumo
//		int li_clave = Integer
//				.parseInt(lectura.is_tarifa.trim()
//						.substring(2));
//		String tipoConsumo="";
//
//		switch (li_clave) {
//		case 11:
//			tipoConsumo = "Cliente Doméstico";
//			break;
//		case 12:
//			tipoConsumo = "Cliente Comercial";
//			break;
//
//		default:
//			tipoConsumo = globales.tll.getLecturaActual().is_tarifa;
//			break;
//
//		}
//
//		//Ahora el nombre del tipo de consumo
//		layout_params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
//		tv_view=new TextView(context);
//		tv_view.setTextSize(TypedValue.COMPLEX_UNIT_PX,fontSize);
//		tv_view.setText(tipoConsumo);
//		ll_generico.addView(tv_view, layout_params);


    }

    @Override
    public MensajeEspecial getMensaje() {
        // TODO Auto-generated method stub
        //Ejemplo de la prueba
//		if (tipo%2==0)
//			return mj_activo;
//		else if (tipo%3==0)
//			return mj_sellos;
//
//		if (globales.tll.getLecturaActual().is_ubicacion.trim().equals("")){
//			return mj_ubicacionVacia;
//		}

        if (!globales.tll.getLecturaActual().verDatos) {
// CE, 02/02/24, Vamos a quitar la Foto de Llegada de las REC/REMO a solicitud de Cesar Chavez
//            if (globales.tll.getLecturaActual().getTipoDeOrden().equals("REC/REMO"))
//                return mj_ver_datosFotoDeLlegada;
//           else
                return mj_ver_datos;
        }
        return null;
    }

    private void InicializarMatrizDeCompatibilidades() {

        anomaliasCompatibles = new Hashtable<String, String>();

        anomaliasCompatibles.put("A", "CEST13");
        anomaliasCompatibles.put("B", "CEFGIJKNSTVWY1234");
        anomaliasCompatibles.put("C", "ABEFGHIJKLNRSTVWYZ1234");
        anomaliasCompatibles.put("D", "");
        anomaliasCompatibles.put("E", "ABCFGHIJKLNRSTVWY1234");
        anomaliasCompatibles.put("F", "BCEGHIJKNSTVWY1234");
        anomaliasCompatibles.put("G", "BCEFHIJKNSTVWY12346");
        anomaliasCompatibles.put("H", "CEFGJKNSTVY13");
        anomaliasCompatibles.put("I", "BCEFGJKNSTVY1234");
        anomaliasCompatibles.put("J", "BCEFGHIKNOPQSTUVWXY12345");
        anomaliasCompatibles.put("K", "BCEFGHIJNSTVWY1234");

        anomaliasCompatibles.put("L", "CENSTVY3");
        anomaliasCompatibles.put("N", "BCEFGHIJKLRSTVWY1234");
        anomaliasCompatibles.put("R", "CENSTVY136");
        anomaliasCompatibles.put("S", "ABCEFGHIJKLNRTVWYZ1234");
        anomaliasCompatibles.put("T", "ABCEFGHIJKLNRSVWYZ1234");
        anomaliasCompatibles.put("V", "ABCEFGHIJKLNRSTWYZ1234");
        anomaliasCompatibles.put("W", "BCEFGJKNSTWY1234");
        anomaliasCompatibles.put("Y", "BCEFGHIJKLNRSTVWZ1234");
        anomaliasCompatibles.put("Z", "CSTVY36");
        anomaliasCompatibles.put("1", "ABCEFGHIJKNRSTVWY234");
        anomaliasCompatibles.put("2", "BCEFGIJKNSTVWY134");
        anomaliasCompatibles.put("3", "ABCEFGHIJKLNRSTVWYZ124");
        anomaliasCompatibles.put("4", "BCEFGIJKNSTVWY123");
        anomaliasCompatibles.put("5", "56");
        anomaliasCompatibles.put("6", "6RZ5");

    }

    @Override
    public boolean esAnomaliaCompatible(String anomaliaAInsertar,
                                        String anomaliasCapturadas) {

        return true;
    }

    @Override
    public ComentariosInputBehavior getAvisoMensajeInput(String anomalia) {
        ComentariosInputBehavior cib_config = null;
//		int longitud_disponible=globales.tlc.getLongCampo("comentarios")-(globales.tll.getLecturaActual().getComentarios().length())+3;
//		// TODO Auto-generated method stub
//		if (anomalia.equals("2")) {
//			cib_config = new ComentariosInputBehavior(context.getString(R.string.lbl_comentarios_ingrese_esferas), InputType.TYPE_CLASS_NUMBER, 1, globales.tll.getLecturaActual().numerodeesferasReal.equals("")?String.valueOf(globales.tll.getLecturaActual().numerodeesferas):globales.tll.getLecturaActual().numerodeesferasReal);
//
//		}
//
////		if (anomalia.equals("R")) {
////			cib_config = new ComentariosInputBehavior("", InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, longitud_disponible,"");
////
////		}
////
////		if (anomalia.equals("S")) {
////			cib_config = new ComentariosInputBehavior("", InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, longitud_disponible,"");
////
////		}
//
//		if (anomalia.equals("N")) {
//			cib_config = new ComentariosInputBehavior(context.getString(R.string.lbl_comentarios_ingrese_direccion), InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("comentarios"), String.valueOf(globales.tll.getLecturaActual().getDireccion()));
//
//		}
//		if (anomalia.equals("1")) {
//			cib_config = new ComentariosInputBehavior(context.getString(R.string.lbl_comentarios_ingrese_ubicacion), InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, 2, "");
//		}
//		if (anomalia.equals("T")) {
//			cib_config = new ComentariosInputBehavior(context.getString(R.string.lbl_comentarios_ingrese_entrecalles), InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("aviso"),  globales.tll.getLecturaActual().is_aviso.trim());
//			cib_config.obligatorio=false;
//
//		}
//		if (anomalia.equals("Y")) {
//			cib_config = new ComentariosInputBehavior(context.getString(R.string.lbl_comentarios_ingrese_nombre),InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("cliente"),  String.valueOf(globales.tll.getLecturaActual().getCliente()));
//			cib_config.obligatorio=false;
//		}
//		if (anomalia.equals("C")) {
//			cib_config = new ComentariosInputBehavior(context.getString(R.string.lbl_comentarios_ingrese_medidor_antpos), InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("comentarios"), "");
//		}
////		if (anomalia.equals("3")) {
////			csTablaAnomalias += "Escriba la SubAnomalia";
////		}
//		if (anomalia.equals("E")) {
//
//			cib_config = new ComentariosInputBehavior(context.getString(R.string.lbl_comentarios_ingrese_advertencia), InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("advertencias") ,  globales.tll.getLecturaActual().is_advertencias.trim());
//			cib_config.obligatorio=false;
//		}
//		if (anomalia.equals("V")) {
//			cib_config = new ComentariosInputBehavior(context.getString(R.string.lbl_comentarios_ingrese_act_comercial), InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,longitud_disponible , "");
//		}
        return cib_config;
    }

    @Override
    public void RealizarModificacionesDeAnomalia(String anomalia, String comentarios) {
        // TODO Auto-generated method stub
        String queActualizar = "";
        boolean actualizar = false;

// CE, 11/10/23, Esta funcionalidad no aplica para Engie
/*
        if (anomalia.equals("Y")) {
            //Cambiamos Nombre del cliente
            queActualizar = "cliente='" + comentarios + "'";
            actualizar = true;
            globales.tll.getLecturaActual().setCliente(comentarios);
        }
//		else if (anomalia.equals("1")){
//			//numEsferas
//			queActualizar="ubicacion='"+comentarios+"'";
//			actualizar=false;
//			globales.tll.getLecturaActual().is_ubicacion=comentarios;
//		}
        else if (anomalia.equals("T")) {
            //numEsferas
            queActualizar = "aviso='" + comentarios + "'";
            actualizar = true;
            globales.tll.getLecturaActual().is_aviso = comentarios;
        } else if (anomalia.equals("E")) {
            //numEsferas
            queActualizar = "advertencias='" + comentarios + "'";
            actualizar = false;
            globales.tll.getLecturaActual().is_advertencias = comentarios;
        }
*/
        if (actualizar) {
            openDatabase();
            db.execSQL("Update ruta set " + queActualizar + " where cast(secuenciaReal as integer)=" + globales.tll.getLecturaActual().secuenciaReal);
            closeDatabase();
        }
    }

    @Override
    public void RealizarModificacionesDeAnomalia(String anomalia) {
// CE, 11/10/23, Esta funcionalidad no aplica para Engie
/*
        if (anomalia.startsWith("1")) {
            globales.tll.getLecturaActual().is_ubicacion = anomalia.substring(1);
        }
*/
    }

    @Override
    public void DeshacerModificacionesDeAnomalia(String anomalia) {
        // TODO Auto-generated method stub

// CE, 11/10/23, Esta funcionalidad no aplica para Engie
/*
        if (anomalia.equals("2")) {
            RealizarModificacionesDeAnomalia(anomalia, "");
        } else if (anomalia.equals("E")) {
            RealizarModificacionesDeAnomalia(anomalia, "");
        } else if (anomalia.equals("H")) {
            globales.tll.getLecturaActual().serieMedidorReal = "";
            globales.tll.getLecturaActual().numerodeesferasReal = "";
        } else if (anomalia.equals("B")) {
            globales.tll.getLecturaActual().serieMedidorReal = "";
        }
*/
    }

    @Override
    public MensajeEspecial mensajeDeConsumo(String ls_lectAct) {
        // TODO Auto-generated method stub
        //Hay que convertirla a entero

//*************************************************************************
// CE, 01/10/23, Vamos a usar la Encuesta de Consumo Cero para capturar el material utilizado. Siempre debe pedirlo
        if (globales.tll.getLecturaActual().is_tipoDeOrden.equals("TO002"))
            return mj_consumocero;
// CE, 29/01/24, También vamos a pedir el material utilizado si son RX Normal o RX Express
        if (globales.tll.getLecturaActual().is_tipoDeOrden.equals("TO003"))
            return mj_consumoceroRecuperado;
        if (globales.tll.getLecturaActual().is_tipoDeOrden.equals("TO006"))
            return mj_consumoceroRecuperado;

        return null;
/*
        //Vamos a realizar una prueba... no se los estados del suministro, asi que si es par es 0 y non 4
        String estadoDelSuministro = globales.tll.getLecturaActual().estadoDelSuministro;

        int li_lectAct = Integer.parseInt(ls_lectAct);
        if (globales.tll.getLecturaActual().lecturaAnterior != li_lectAct && (estadoDelSuministro.equals("1") || estadoDelSuministro.equals("2"))) {
            //Si es la misma o menor... quiere decir que no hubo un consumo
            return mj_estaCortado;
        }

        //Hay la otra causa de un mensaje de consumo.. cuando esta el estado de suministro igual a 4 (cortado) y el consumo es diferente de 0

        if (globales.tll.getLecturaActual().lecturaAnterior == li_lectAct && estadoDelSuministro.equals("0")) {
            //Si es la misma o menor... quiere decir que no hubo un consumo
            return mj_consumocero;
        }
        return null;*/
//*************************************************************************
    }

    @Override
    public void RespuestaMensajeSeleccionada(MensajeEspecial me, int respuesta) {
        // TODO Auto-generated method stub

        switch (me.respondeA) {
            case PREGUNTAS_SIGUE_CORTADO:
                if (respuesta == MensajeEspecial.NO) {
                    //Borramos si hay una j
                    globales.tll.getLecturaActual().deleteAnomalia("J");
                    //Agregamos la anomalia J al vector de anomalias
                    cambiosAnomaliaAntesDeGuardar(globales.is_lectura);
                    globales.tll.getLecturaActual().setAnomalia("J");
                    globales.is_presion = globales.tll.getLecturaActual().getAnomalia();
                    globales.tll.getLecturaActual().is_estadoDelSuministroReal = "0";
                } else {
                    globales.tll.getLecturaActual().is_estadoDelSuministroReal = "1";
                }
                break;
            case PREGUNTAS_CONSUMO_CERO:
                if (globales.tll.getLecturaActual().is_tipoDeOrden.equals("TO002"))
                    globales.tll.getLecturaActual().is_idMaterialUtilizado = me.regresaValor(respuesta);
                if (globales.tll.getLecturaActual().is_tipoDeOrden.equals("TO003"))
                    globales.tll.getLecturaActual().is_MaterialRecuperado = me.regresaValor(respuesta);
                if (globales.tll.getLecturaActual().is_tipoDeOrden.equals("TO006"))
                    globales.tll.getLecturaActual().is_MaterialRecuperado = me.regresaValor(respuesta);
//**********************************************************
// CE, 01/10/23, Vamos a usar la Encuesta de Consumo Cero para capturar el Material Utilizado
/*
                //Borramos la anomalia y la sub

                globales.tll.getLecturaActual().deleteAnomalia(me.regresaValor(respuesta).substring(0, 1));
                //Agregamos
                cambiosAnomaliaAntesDeGuardar(globales.is_lectura);
                globales.tll.getLecturaActual().setAnomalia(me.regresaValor(respuesta).substring(0, 1));
                globales.tll.getLecturaActual().setSubAnomalia(me.regresaValor(respuesta));

                globales.is_presion = globales.tll.getLecturaActual().getAnomalia();*/
//**********************************************************
                break;
            case PREGUNTAS_EN_EJECUCION:
                break;

            case PREGUNTAS_UBICACION_VACIA:
                globales.tll.getLecturaActual().is_ubicacion = me.regresaValor(respuesta).substring(1, 2);
                globales.tll.getLecturaActual().deleteAnomalia(me.regresaValor(respuesta).substring(0, 1));
                globales.tll.getLecturaActual().setAnomalia(me.regresaValor(respuesta).substring(0, 1));
                globales.tll.getLecturaActual().setSubAnomalia(me.regresaValor(respuesta));
                break;

            case ANOMALIA_SEIS:
                globales.tll.getLecturaActual().deleteAnomalia("6");
//			globales.tll.getLecturaActual().deleteAnomalia("R");
//			globales.tll.getLecturaActual().deleteAnomalia("Z");
//			globales.tll.getLecturaActual().deleteAnomalia("5");
//			globales.tll.getLecturaActual().deleteAnomalia("G");
//			globales.tll.getLecturaActual().deleteAnomalia(me.regresaValor(respuesta));
                globales.tll.getLecturaActual().setAnomalia(me.regresaValor(respuesta));
                globales.tll.getLecturaActual().setAnomalia("6");
                break;

//		case PREGUNTAS_ESTA_HABITADO:
//			if (respuesta== MensajeEspecial.NO){
//				globales.tll.getLecturaActual().is_habitado="0";
//			}
//			else{
//				globales.tll.getLecturaActual().is_habitado="1";
//			}

        }

    }

    @Override
    public ComentariosInputBehavior getCampoGenerico(int campo) {
        // TODO Auto-generated method stub

        ComentariosInputBehavior cib_config = null;

        switch (campo) {
            case MEDIDOR_ANTERIOR:
                cib_config = new ComentariosInputBehavior("Medidor Anterior", InputType.TYPE_CLASS_NUMBER, globales.tlc.getLongCampo("serieMedidor"), "");
                cib_config.obligatorio = false;
                break;
            case MEDIDOR_POSTERIOR:
                cib_config = new ComentariosInputBehavior("Medidor Posterior", InputType.TYPE_CLASS_NUMBER, globales.tlc.getLongCampo("serieMedidor"), "");
                cib_config.obligatorio = false;
                break;

            case NUM_MEDIDOR:
                cib_config = new ComentariosInputBehavior("Número de Medidor", InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("serieMedidor"), "");
                cib_config.obligatorio = false;
                break;

            case NUM_ESFERAS:
                cib_config = new ComentariosInputBehavior("Número de Esferas", InputType.TYPE_CLASS_NUMBER, globales.tlc.getLongCampo("numEsferas"), "");
                break;
            case MARCA:
                cib_config = new ComentariosInputBehavior("Marca de Medidor", InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, 3, "");
                break;
            case CALLE:
                cib_config = new ComentariosInputBehavior("Calle", InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("direccion"), globales.tll.getLecturaActual().getDireccion());
                cib_config.obligatorio = false;
                break;
            case NUMERO:
                cib_config = new ComentariosInputBehavior("Número", InputType.TYPE_CLASS_NUMBER, globales.tlc.getLongCampo("numEdificio"), globales.tll.getLecturaActual().numeroDeEdificio.trim());
                cib_config.obligatorio = false;
                break;
            case PORTAL:
                cib_config = new ComentariosInputBehavior("Portal", InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("numPortal"), globales.tll.getLecturaActual().numeroDePortal.trim());
                cib_config.obligatorio = false;
                break;
            case ESCALERA:
                cib_config = new ComentariosInputBehavior("Escalera", InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("escalera"), globales.tll.getLecturaActual().is_escalera.trim());
                cib_config.obligatorio = false;
                break;
            case PISO:
                cib_config = new ComentariosInputBehavior("Piso", InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("piso"), globales.tll.getLecturaActual().is_piso.trim());
                cib_config.obligatorio = false;
                break;
            case PUERTA:
                cib_config = new ComentariosInputBehavior("Puerta", InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("puerta"), globales.tll.getLecturaActual().is_puerta.trim());
                cib_config.obligatorio = false;
                break;
            case COMPLEMENTO:
                cib_config = new ComentariosInputBehavior("Complemento", InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("colonia"), globales.tll.getLecturaActual().getColonia());
                cib_config.obligatorio = false;
                break;

            case IC:
                cib_config = new ComentariosInputBehavior("IC", InputType.TYPE_CLASS_NUMBER, globales.tlc.getLongCampo("serieMedidor"), "");
                cib_config.obligatorio = true;
                break;

            case CLIENTE_YA_PAGO_MONTO:
                cib_config = new ComentariosInputBehavior("Monto Pagado", InputType.TYPE_CLASS_NUMBER, globales.tlc.getLongCampo("ClienteYaPagoMonto"), "");
                cib_config.obligatorio = true;
                break;
            case CLIENTE_YA_PAGO_FECHA:
                cib_config = new ComentariosInputBehavior("Fecha de Pago (ddmmyyyy)", InputType.TYPE_CLASS_NUMBER, globales.tlc.getLongCampo("ClienteYaPagoFecha"), "");
                cib_config.obligatorio = true;
                break;
            case CLIENTE_YA_PAGO_AGENTE:
                cib_config = new ComentariosInputBehavior("Agente", InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, globales.tlc.getLongCampo("ClienteYaPagoAgente"), "");
                cib_config.obligatorio = true;
                break;
        }

        return cib_config;
    }

    @Override
    public int[] getCamposGenerico(String anomalia) {
        // TODO Auto-generated method stub
        int[] campos = null;
        if (anomalia.equals("noregistrados")) {
            campos = new int[2];
            campos[0] = IC;
            campos[1] = NUM_MEDIDOR;
        } else if (anomalia.equals("cliente_ya_pago") || (anomalia.equals("G"))) {
            campos = new int[3];
            campos[0] = CLIENTE_YA_PAGO_MONTO;
            campos[1] = CLIENTE_YA_PAGO_FECHA;
            campos[2] = CLIENTE_YA_PAGO_AGENTE;
        }
/*        else if (anomalia.equals("B") || anomalia.equals("H")) {
            campos = new int[3];
            campos[0] = NUM_MEDIDOR;
            campos[1] = NUM_ESFERAS;
            campos[2] = MARCA;
        } else if (anomalia.equals("N")) {
            campos = new int[7];
            campos[0] = CALLE;
            campos[1] = NUMERO;
            campos[2] = PORTAL;
            campos[3] = ESCALERA;

            campos[4] = PISO;
            campos[5] = PUERTA;
            campos[6] = COMPLEMENTO;
        }
*/
        return campos;
    }

    @Override
    public void regresaDeCamposGenericos(Bundle bu_params, String anomalia) {
        // TODO Auto-generated method stub
//		if (anomalia.equals("C")) {
//			globales.tll.getLecturaActual().setComentarios("MA:" +bu_params.getString(String.valueOf(MEDIDOR_ANTERIOR)) +",MP:"+bu_params.getString(String.valueOf(MEDIDOR_POSTERIOR)));
//		}
//		else if (anomalia.equals("B") || anomalia.equals("H")) {
//			globales.tll.getLecturaActual().numerodeesferasReal=bu_params.getString(String.valueOf(NUM_ESFERAS));
//			globales.tll.getLecturaActual().serieMedidorReal=bu_params.getString(String.valueOf(NUM_MEDIDOR));
////			 if (anomalia.equals("B")){
////				 globales.tll.getLecturaActual().setAnomalia("2");
////			 }
//		} else if (anomalia.equals("N")) {
//			globales.tll.getLecturaActual().setDireccion(bu_params.getString(String.valueOf(CALLE)));
//			globales.tll.getLecturaActual().numeroDeEdificio=bu_params.getString(String.valueOf(NUMERO));
//			globales.tll.getLecturaActual().numeroDePortal=bu_params.getString(String.valueOf(PORTAL));
//			globales.tll.getLecturaActual().is_escalera=bu_params.getString(String.valueOf(ESCALERA));
//			globales.tll.getLecturaActual().is_piso=bu_params.getString(String.valueOf(PISO));
//			globales.tll.getLecturaActual().is_puerta=bu_params.getString(String.valueOf(PUERTA));
//			globales.tll.getLecturaActual().setColonia(bu_params.getString(String.valueOf(COMPLEMENTO)));
//
//		}if (anomalia.equals("34")) {
//			globales.tll.getLecturaActual().setComentarios("34:" +bu_params.getString("input"));
//		}
//		else if (anomalia.equals("ZT")) {
//			globales.tll.getLecturaActual().setComentarios("ZT:" +bu_params.getString("input"));
//		}
//		else if (anomalia.equals("R4")) {
//			globales.tll.getLecturaActual().setComentarios("R4:" +bu_params.getString("input"));
//		}
//		else if (anomalia.equals("S4")) {
//			globales.tll.getLecturaActual().setComentarios("S4:" +bu_params.getString("input"));
//		}
//		else if (anomalia.equals("V")) {
        if (anomalia.equals("noregistrados")) {
            //Agregar el nuevo registro
            openDatabase();
            globales.tlc.byteToBD(db, globales, bu_params.getString(String.valueOf(IC)), bu_params.getString(String.valueOf(NUM_MEDIDOR)));
            closeDatabase();
        } else if (anomalia.equals("cliente_ya_pago") || anomalia.equals("G")) {
            openDatabase();
            if (globales.tll.getLecturaActual().setClienteYaPago(bu_params)) {
//                    Toast.makeText(this.context, "El Monto es inferior al Adeudo. Proceda con la Desconexión/Remoción", Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
                builder.setMessage("El Monto pagado es insuficiente. Debe proceder a realizar la Desconexión/Remoción")
                        .setTitle("Realizar la Desconexión/Remoción")
                        .setCancelable(false)
                        .setNegativeButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
            closeDatabase();
        } else {
            globales.tll.getLecturaActual().setComentarios(bu_params.getString("input"));
        }
    }


    @Override
    public ContentValues getCamposBDAdicionales() {
        // TODO Auto-generated method stub
        ContentValues cv_params = new ContentValues();
        cv_params.put("intento1", "");
        cv_params.put("intento2", "");
        cv_params.put("intento3", "");
        cv_params.put("intento4", "");
        cv_params.put("intento5", "");
        cv_params.put("intento6", "");
        cv_params.put("intentos", 0);
        cv_params.put("sospechosa", "0");
        cv_params.put("nisRad", 0);
        cv_params.put("dondeEsta", "");
        cv_params.put("anomInst", "");
        cv_params.put("sectorCorto", "");
        cv_params.put("sectorLargo", "");
        cv_params.put("comoLlegar2", "");
        cv_params.put("lectura", "");
        cv_params.put("anomalia", "");
        cv_params.put("comentarios", "");
//		cv_params.put("comoLlegar1", "");
        return cv_params;
    }

    @Override
    public void creaTodosLosCampos() {
        // TODO Auto-generated method stub

        //globales.tlc.add(new Campo(0, "registro", 0, 490, Campo.D, ""));

        globales.tlc.add(new Campo(0, "indicador", 0, 1, Campo.D, " "));
        globales.tlc.add(new Campo(2, "numOrden", 1, 10, Campo.I, " "));
        globales.tlc.add(new Campo(1, "poliza", 11, 10, Campo.I, " "));
        globales.tlc.add(new Campo(5, "cliente", 21, 72, Campo.I, " "));
        globales.tlc.add(new Campo(23, "serieMedidor", 93, 20, Campo.I, " "));
        globales.tlc.add(new Campo(23, "calle", 113, 30, Campo.D, " "));
        globales.tlc.add(new Campo(8, "numPortal", 143, 10, Campo.I, " "));
        globales.tlc.add(new Campo(7, "numEdificio", 153, 10, Campo.I, " "));
        globales.tlc.add(new Campo(33, "piso", 163, 6, Campo.I, " "));
        globales.tlc.add(new Campo(24, "colonia", 169, 50, Campo.I, " "));
        globales.tlc.add(new Campo(24, "municipio", 219, 30, Campo.I, " "));
        globales.tlc.add(new Campo(24, "entrecalles", 249, 60, Campo.I, " "));
        globales.tlc.add(new Campo(24, "comollegar1", 309, 50, Campo.I, " "));
        globales.tlc.add(new Campo(9, "aviso", 359, 50, Campo.I, " "));
        globales.tlc.add(new Campo(21, "marcaMedidor", 409, 80, Campo.I, " "));
        globales.tlc.add(new Campo(22, "tipoMedidor", 489, 3, Campo.I, " "));
        globales.tlc.add(new Campo(42, "estadoDelSuministro", 492, 5, Campo.D, " "));
        globales.tlc.add(new Campo(41, "tarifa", 497, 1, Campo.I, " "));
        globales.tlc.add(new Campo(41, "tipoDeOrden", 498, 5, Campo.I, " "));
        globales.tlc.add(new Campo(14, "lectura", 270, 8, Campo.D, "0", false));
        globales.tlc.add(new Campo(14, "consumo", 270, 0, Campo.D, "0"));
        globales.tlc.add(new Campo(2, "comentarios", 0, 200, Campo.I, " ", false));
        globales.tlc.add(new Campo(17, "anomalia", 294, 5, Campo.I, " ", false));
        globales.tlc.add(new Campo(4, "subAnomalia", 104, 0, Campo.I, " "));
        globales.tlc.add(new Campo(4, "fecha", 104, 8, Campo.F, "ymd", false));
        globales.tlc.add(new Campo(4, "hora", 104, 6, Campo.F, "his", false));
        globales.tlc.add(new Campo(4, "lecturista", 0, 10, Campo.I, " ", false));
        globales.tlc.add(new Campo(4, "latitud", 0, 20, Campo.I, " ", false));
        globales.tlc.add(new Campo(4, "longitud", 0, 20, Campo.I, " ", false));
        globales.tlc.add(new Campo(4, "estadoDeLaOrden", 0, 5, Campo.I, " ", false));
        globales.tlc.add(new Campo(4, "sospechosa", 0, 1, Campo.I, " ", false));
        globales.tlc.add(new Campo(4, "habitado", 0, 1, Campo.I, " ", false));

        globales.tlc.add(new Campo(24, "vencido", 543, 11, Campo.I, " "));
        globales.tlc.add(new Campo(9, "balance", 554, 11, Campo.I, " "));
        globales.tlc.add(new Campo(21, "ultimo_pago", 565, 11, Campo.I, " "));
        globales.tlc.add(new Campo(22, "fecha_utlimo_pago", 576, 8, Campo.I, " "));


        globales.tlc.add(new Campo(22, "giro", 584, 60, Campo.I, " "));
        globales.tlc.add(new Campo(22, "diametro", 644, 5, Campo.I, " "));
        globales.tlc.add(new Campo(22, "fechaEnvio", 507, 14, Campo.I, " "));

        globales.tlc.add(new Campo(22, "fechaEnvio", 507, 14, Campo.I, " ", false));
        globales.tlc.add(new Campo(22, "fechaDeInicio", 507, 14, Campo.I, " ", false));
        globales.tlc.add(new Campo(22, "habitado", 507, 1, Campo.I, " ", false));
        globales.tlc.add(new Campo(22, "registro", 507, 1, Campo.I, " ", false));

//************************************************************************************************************************************
// CE, 06/10/23, Preguntar a Reynol si aqui deben estar todos los CamposEngie
        globales.tlc.add(new Campo(22, "ClienteYaPagoMonto", 507, 10, Campo.I, " ", false));
        globales.tlc.add(new Campo(22, "ClienteYaPagoFecha", 507, 10, Campo.I, " ", false));
        globales.tlc.add(new Campo(22, "ClienteYaPagoAgente", 507, 50, Campo.I, " ", false));
        globales.tlc.add(new Campo(22, "QuienAtendio", 507, 50, Campo.I, " ", false));
        globales.tlc.add(new Campo(22, "MarcaInstalada", 507, 50, Campo.I, " ", false));
        globales.tlc.add(new Campo(22, "SeQuitoTuberia", 507, 50, Campo.I, " ", false));
        globales.tlc.add(new Campo(22, "TuberiaRetirada", 507, 50, Campo.I, " ", false));
        globales.tlc.add(new Campo(22, "MarcaRetirada", 507, 50, Campo.I, " ", false));
        globales.tlc.add(new Campo(22, "MedidorRetirado", 507, 50, Campo.I, " ", false));
//************************************************************************************************************************************

        //globales.tlc.add(new Campo(3, "situacionDelSuministro", 97, 1, Campo.D, " "));
//			globales.tlc.add(new Campo(4, "subAnomalia", 104, 10, Campo.I, " "));
//
//			globales.tlc.add(new Campo(6, "direccion", 164, 30, Campo.I, " "));
//
//
//
//			globales.tlc.add(new Campo(10, "lecturaAnterior", 242, 10, Campo.D, "0"));
//			globales.tlc.add(new Campo(11, "baremo", 252, 7, Campo.D, "0"));
//			globales.tlc.add(new Campo(12, "ilr", 259, 10, Campo.D, "0"));
//			globales.tlc.add(new Campo(13, "tipoLectura", 269, 1, Campo.I, " "));
//			globales.tlc.add(new Campo(14, "lectura", 270, 7, Campo.D, "0"));
//			globales.tlc.add(new Campo(15, "consumo", 277, 7, Campo.D, "0"));
//			globales.tlc.add(new Campo(16, "fecha", 284, 10, Campo.F, "Ymdhi"));
//			globales.tlc.add(new Campo(17, "anomalia", 294, 30, Campo.I, " "));
//			globales.tlc.add(new Campo(18, "divisionContrato", 324, 2, Campo.D, "0"));
//			globales.tlc.add(new Campo(19, "sospechosa", 326, 2, Campo.D, " "));//Confirmadax
//			globales.tlc.add(new Campo(20, "intentos", 328, 2, Campo.D, " "));//Distinta
//
//
//			globales.tlc.add(new Campo(6, "sinUso2", 387, 1, Campo.I, " "));
//			globales.tlc.add(new Campo(25, "poliza", 388, 7, Campo.D, " "));
//			globales.tlc.add(new Campo(26, "numEsferas", 395, 1, Campo.D, " "));
//			globales.tlc.add(new Campo(27, "consAnoAnt", 395, 0, Campo.D, "0"));
//			globales.tlc.add(new Campo(28, "consBimAnt", 395, 0, Campo.D, "0"));
//			globales.tlc.add(new Campo(29, "hora", 396, 6, Campo.F, "his"));
//			globales.tlc.add(new Campo(30, "lecturista", 402, 4, Campo.D, "0"));
//			globales.tlc.add(new Campo(31, "ordenDeLectura", 406, 4, Campo.D, "0"));
//			globales.tlc.add(new Campo(32, "escalera", 410, 3, Campo.I, " "));
//			globales.tlc.add(new Campo(34, "puerta", 415, 5, Campo.D, " "));
//			globales.tlc.add(new Campo(35, "reclamacionLectura", 420, 1, Campo.D, " "));
//			globales.tlc.add(new Campo(36, "supervisionLectura", 421, 1, Campo.D, " "));
//			globales.tlc.add(new Campo(37, "reclamacion", 422, 1, Campo.D, " "));
//			globales.tlc.add(new Campo(38, "supervision", 423, 1, Campo.D, " "));
//
//
//			globales.tlc.add(new Campo(41, "estimaciones", 468, 1, Campo.I, " "));
//
//			globales.tlc.add(new Campo(43, "numEsferasReal", 470, 1, Campo.D, " "));
//			globales.tlc.add(new Campo(44, "fechaAviso", 471, 10, Campo.F, "Ymdhi"));
//			globales.tlc.add(new Campo(45, "serieMedidorReal", 481, 8, Campo.D, " "));
//			//add(new Campo(18, "rutaReal", 490, 4, Campo.D, ""));
//
//			globales.tlc.add(new Campo(35, "estadoDelSuministroReal", 489, 1, Campo.D, " "));

    }

    @Override
    public long getLecturaMinima() {

        return 0;
    }

    @Override
    public long getLecturaMaxima() {

        return 99999999;
    }

    @Override
    public String obtenerContenidoDeEtiqueta(String ls_etiqueta) {
        // TODO Auto-generated method stub
        if (ls_etiqueta.equals("campo0")) {
            return globales.tll.getLecturaActual().poliza.trim();
        } else if (ls_etiqueta.equals("campo1")) {
            return globales.tll.getLecturaActual().is_tarifa.trim();
        } else if (ls_etiqueta.equals("campo2")) {
            return globales.tll.getLecturaActual().is_ubicacion.trim();
        } else if (ls_etiqueta.equals("campo3")) {
            return globales.tll.getLecturaActual().estadoDelSuministro.trim();
        } else if (ls_etiqueta.equals("campo4")) {
            return globales.tll.getLecturaActual().is_estimaciones.trim();
        } else {
            return "";
        }
    }

    @Override
    public FormatoDeEtiquetas getMensajedeRespuesta() {
        // TODO Auto-generated method stub
//		if (!globales.tll.getLecturaActual().is_estimaciones.equals("0")){
//			return new FormatoDeEtiquetas("Lectura Crítica", R.color.red);
//		}
//		else if ((globales.tll.getLecturaActual().estadoDelSuministro.equals("1") || globales.tll.getLecturaActual().estadoDelSuministro.equals("2"))){
//			return new FormatoDeEtiquetas("Cortado", R.color.Orange);
//		}

//		if (globales.tll.getLecturaActual().is_tipoDeOrden.equals("TO003")){
//			return new FormatoDeEtiquetas("Reconexión", R.color.Orange);
//		}
//		else if (globales.tll.getLecturaActual().is_tipoDeOrden.equals("TO002")){
//			return new FormatoDeEtiquetas("Limitación", R.color.Red);
//		}
        return null;
    }

    @Override
    public String getMensajedeAdvertencia() {
        // TODO Auto-generated method stub
// CE, 09/10/23, Vamos a mostrar el campo MensajeOut en la parte superior
//        return globales.tll.getLecturaActual().is_advertencias.trim();
        return globales.tll.getLecturaActual().is_MensajeOut.trim();
    }

    @Override
    public void regresaDeBorrarLectura() {
        // TODO Auto-generated method stub
    }

    @Override
    public void cambiosAnomaliaAntesDeGuardar(String ls_lect_act) {
//		// TODO Auto-generated method stub
//		if (!ls_lect_act.equals("")){
//			String ls_anomalia=globales.tll.getLecturaActual().getAnomaliasCapturadas();
//
//			if ((ls_anomalia.contains("A") && globales.tll.getLecturaActual().containsSubAnomalia("AA") )||ls_anomalia.contains("R")||ls_anomalia.contains("Z")||ls_anomalia.contains("5")){
//				globales.tll.getLecturaActual().setAnomalia("*");
//				if (!globales.tll.getLecturaActual().getSubAnomaliaAMostrar().equals("")){
//					globales.tll.getLecturaActual().setSubAnomalia("*");
//				}
//			}
//		}
//
    }

    @Override
    public void anomaliasARepetir() {
        // TODO Auto-generated method stub


    }

    @Override
    public void subAnomaliasARepetir() {

    }

    @Override
    public boolean avanzarDespuesDeAnomalia(String ls_anomalia, String ls_subAnom, boolean guardar) {
        // TODO Auto-generated method stub
        //String ls_anomalia= globales.tll.getLecturaActual().getAnomaliaAMostrar();
        //if ((ls_anomalia.endsWith("A") && ls_subAnom.startsWith("AA"))||ls_anomalia.endsWith("R")||ls_anomalia.endsWith("Z")||ls_anomalia.endsWith("5")){
//		if (esSegundaVisita(ls_anomalia, ls_subAnom)){
//			//Grabamos
//			if (guardar)
//				globales.tll.getLecturaActual().guardar(true, globales.tll.getSiguienteOrdenDeLectura());
//			return true;
//		}
        return false;
    }

    @Override
    public boolean esSegundaVisita(String ls_anomalia, String ls_subAnom) {

        return false;
    }


//	public String getFiltroDeLecturas(int comoFiltrar){
//		switch(comoFiltrar){
//		case AUSENTES:
//			return " lectura=''  and (anomalia='' or anomalia like '%*' or anomalia like '%A' or anomalia like '%R' or anomalia like '%Z' or anomalia like '%5') " ;
//		case LEIDAS:
//			return " (lectura<>'' or (anomalia<>'' and anomalia not like '%*' and anomalia not like '%A' and anomalia not like '%R' and anomalia not like '%Z' and anomalia not like '%5' )) ";
//		}
//		return "";
//	}

    @Override
    public String getDescripcionDeBuscarMedidor(Lectura lectura, int tipoDeBusqueda, String textoBuscado) {
        String ls_preview = "";

        switch (tipoDeBusqueda) {
// CE, 14/10/23, Ya solamente existe un tipo de Buscar Medidor
            case BuscarMedidorTabsPagerAdapter.MEDIDOR:
/*
                ls_preview += "IC:" + lectura.poliza + "<br>";
                ls_preview += lectura.is_comollegar1 + "<br>";
                //ls_preview =lectura.is_comollegar1 +"<br>" // Lectura.marcarTexto(lectura.is_serieMedidor, textoBuscado, false);
                if (!lectura.getColonia().equals(""))
                    ls_preview += "<br>" + lectura.getColonia();
                ls_preview += "<br>" + lectura.getDireccion();
                ls_preview += "<br>" + lectura.is_entrecalles;
                break;
*/
            case BuscarMedidorTabsPagerAdapter.DIRECCION:
/*                if (lectura.is_tipoDeOrden.equals("TO002"))
                    ls_preview += "DESCONEXION" + "<br>";
                if (lectura.is_tipoDeOrden.equals("TO003"))
                    ls_preview += "RECONEXION" + "<br>";
                if (lectura.is_tipoDeOrden.equals("TO004"))
                    ls_preview += "REC/REMO" + "<br>";
                if (lectura.is_tipoDeOrden.equals("TO005"))
                    ls_preview += "REMOCION" + "<br>";*/

// CE, 05/11/23, Vamos a mostrar un listado mas conciso para mostrar mas renglones en la pantalla
//                ls_preview += lectura.getTipoDeOrden() + "<br>";

//                ls_preview += "Medidor:  " + lectura.is_serieMedidor + "(" + lectura.miLatitud + "," + lectura.miLongitud + ")<br>";
                String strGPS = "";
                float distanciaEnMetros = 0;
                if (lectura.miLatitud.equals("") || lectura.miLongitud.equals("") || globales.location == null)
                    strGPS = " (Sin GPS)";
                else {
                    Location origen = new Location("Origen");
                    origen.setLatitude(globales.location.getLatitude());
                    origen.setLongitude(globales.location.getLongitude());
//                    origen.setLatitude(Float.parseFloat(lectura.miLatitud));
//                    origen.setLongitude(Float.parseFloat(lectura.miLongitud));
                    Location destino = new Location("Destino");
                    destino.setLatitude(Float.parseFloat(lectura.miLatitud));
                    destino.setLongitude(Float.parseFloat(lectura.miLongitud));
                    distanciaEnMetros = origen.distanceTo(destino);
                    strGPS = " (" + String.format(Locale.US, "%,.0f", distanciaEnMetros) + " MTS.)";
                }
                ls_preview += "M: " + lectura.is_serieMedidor + strGPS + "<br>";

//                ls_preview += "Cuenta Contrato:  " + lectura.is_cuentaContrato + "<br>";
//                ls_preview += "Aviso SAP:  " + lectura.is_numAviso + "<br>";
//                ls_preview += lectura.is_comollegar1 + "<br>";
//                if (!lectura.getColonia().equals(""))
//                    ls_preview += "<br>"
//                            + Lectura.marcarTexto(lectura.getColonia(), textoBuscado, true);
                ls_preview += lectura.getDireccion();
//                ls_preview += "<br>" + lectura.is_entrecalles;
                break;

            case BuscarMedidorTabsPagerAdapter.NUMERO:
                ls_preview += "IC:" + Lectura.marcarTexto(lectura.poliza, textoBuscado, true) + "<br>";
                ls_preview += lectura.is_comollegar1 + "<br>";
                if (!lectura.getColonia().equals(""))
                    ls_preview += "<br>" + lectura.getColonia();
                ls_preview += "<br>" + Lectura.marcarTexto(lectura.getDireccion(), textoBuscado, true)/*+ " #" +lectura.numeroDeEdificio, textoBuscado, true)*/;
                ls_preview += "<br>" + lectura.is_entrecalles;
                break;

            case BuscarMedidorTabsPagerAdapter.CALLES:
                ls_preview = Lectura.marcarTexto(lectura.getDireccion(), textoBuscado, true);
                break;
        }

        return ls_preview;
    }

    @Override
    public String validaAnomalia(String ls_anomalia) {
        // TODO Auto-generated method stub

//		if (ls_anomalia.equals("A")){
//			if (globales.tll.getLecturaActual().is_ubicacion.trim().equals("A")){
//				return "Medidor accesible. Incidencia no permitida por el momento.";
//			}
//		}
//		else if (ls_anomalia.equals("6")){
//			if (!globales.tll.getLecturaActual().getAnomaliaAMostrar().contains("*")){
//				return "La Anomalia 6 solamente se permite en segunda visita";
//			}
//		}
        return "";
    }

    @Override
    public String getPrefijoComentario(String ls_anomalia) {
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public void repetirAnomalias() {

    }

    @Override
    public void setConsumo() {
        long ll_consumo = 0;
        String anomCapturadas = globales.tll.getLecturaActual().getAnomaliasCapturadas();


        if (globales.is_lectura.equals("")/* ||anomCapturadas.contains("H") */) {
            globales.tll.getLecturaActual().is_consumo = Main.rellenaString("", " ", globales.tlc.getLongCampo("consumo"), true);
            return;
        }


        float baremo_max = ((float) (100 + globales.baremo)) / 100;
        float baremo_min = ((float) (100 - globales.baremo)) / 100;

        if (baremo_min < 0) {
            baremo_min = 0;
        }


        //Hay que checar que las anomalias capturadas sean las que se dejan como AUSENTES

        globales.tll.getLecturaActual().deleteAnomalia("W");
        globales.tll.getLecturaActual().deleteAnomalia("I");
        cambiosAnomaliaAntesDeGuardar(globales.is_lectura);

        ll_consumo = getConsumo(globales.is_lectura);

        if (anomCapturadas.contains("H")) {
            if (ll_consumo < 0)
                ll_consumo = (long) (Math.pow(10, globales.tll.getLecturaActual().numerodeesferas) + ll_consumo);
            globales.tll.getLecturaActual().is_consumo = String.valueOf(ll_consumo);
            return;
        }

        if (ll_consumo < 0) {
            ll_consumo = (long) (Math.pow(10, globales.tll.getLecturaActual().numerodeesferas) + ll_consumo);
            if (ll_consumo <= globales.tll.getLecturaActual().baremo * baremo_max) {
                if (!anomCapturadas.contains("4")) {
                    globales.tll.getLecturaActual().setAnomalia("4");
                }
                globales.tll.getLecturaActual().setAnomalia("W");
            } else {
                if (!anomCapturadas.contains("4")) {
                    globales.tll.getLecturaActual().setAnomalia("4");
                }
                globales.ignorarContadorControlCalidad = true;
                globales.tll.getLecturaActual().setAnomalia("I");
            }
        } else if (ll_consumo >= 0 && !((globales.tll.getLecturaActual().estadoDelSuministro.equals("1") || globales.tll.getLecturaActual().estadoDelSuministro.equals("2")) && ll_consumo == 0)) {
            //if(ll_consumo < globales.tll.getLecturaActual().baremo * baremo_min){
            if (this.getLecturaMinima() > Long.valueOf(globales.is_lectura)) {
                if (!anomCapturadas.contains("4")) {
                    globales.tll.getLecturaActual().setAnomalia("4");
                }
            }
            //else if(ll_consumo > globales.tll.getLecturaActual().baremo * baremo_max){
            else if (this.getLecturaMaxima() < Long.valueOf(globales.is_lectura)) {
                if (!anomCapturadas.contains("4")) {
                    globales.tll.getLecturaActual().setAnomalia("4");
                }
            }
        }

        if (globales.tll.getLecturaActual().getAnomaliasCapturadas().contains("J")) {

            globales.tll.getLecturaActual().deleteAnomalia("J");
            globales.tll.getLecturaActual().setAnomalia("J");

        }


        globales.tll.getLecturaActual().is_consumo = String.valueOf(ll_consumo);


    }

    @Override
    public long getConsumo(String lectura) {
        // TODO Auto-generated method stub
        return Long.parseLong(lectura) - globales.tll.getLecturaActual().lecturaAnterior;
    }

    public void setTipoLectura() {
//		if  (globales.tll.getLecturaActual().getAnomaliasCapturadas().contains("A") && globales.tll.getLecturaActual().getAnomaliaAMostrar().contains("*")){
//			//Si las capturadas tiene A y en las anteriores tiene A, hay que tomarla como capturada
//
//			String anomalias=globales.tll.getLecturaActual().getAnomaliaAMostrar();
//
//
//			anomalias= anomalias.substring(0, anomalias.lastIndexOf("*"));
//
//			anomalias= anomalias.substring(anomalias.lastIndexOf("*")+1);
//
//			if (anomalias.contains("A")){
//				globales.tll.getLecturaActual().is_tipoLectura="4";
//				return;
//			}
//
//
//		}
//
//		if (globales.tll.getLecturaActual().getLectura().trim().equals("") &&
//				( (globales.tll.getLecturaActual().getAnomaliasCapturadas().contains("A") && globales.tll.getLecturaActual().containsSubAnomalia("AA") )
//				|| globales.tll.getLecturaActual().getAnomaliasCapturadas().contains("R")
//				|| globales.tll.getLecturaActual().getAnomaliasCapturadas().contains("Z")
//				|| globales.tll.getLecturaActual().getAnomaliasCapturadas().contains("5") )
//				&& !globales.tll.getLecturaActual().getAnomaliasCapturadas().contains("6")){
//			globales.tll.getLecturaActual().is_tipoLectura="";
//			return;
//		}

        super.setTipoLectura();

    }

    @Override
    public String validaCamposGenericos(String anomalia, Bundle bu_params) {
        // TODO Auto-generated method stub


//		if (anomalia.equals("C")) {
//
//			if (bu_params.getString(String.valueOf(MEDIDOR_ANTERIOR)).length()==0 && bu_params.getString(String.valueOf(MEDIDOR_POSTERIOR)).length()==0){
//				return "No se han ingresado datos";
//			}
//			int longitud_disponible=globales.tlc.getLongCampo("comentarios")-(globales.tll.getLecturaActual().getComentarios().length());
//			String campoAInsertar="MA:" +bu_params.getString(String.valueOf(MEDIDOR_ANTERIOR)) +",MP:"+bu_params.getString(String.valueOf(MEDIDOR_POSTERIOR));
//
//			if(longitud_disponible<campoAInsertar.length()){
//				return "Se excede la el límite de caracteres para el campo comentarios";
//			}
//
//
//		} else if (anomalia.equals("2")) {
//			if (bu_params.getString("input").equals("0")){
//				return "El número de esferas no puede ser 0";
//			}else if (Integer.parseInt(bu_params.getString("input"))>7){
//				return "El número de esferas no puede ser mayor a 7";
//			}
//		}
        return "";
    }


    @Override
    public MensajeEspecial regresaDeAnomalias(String ls_anomalia) {
        // TODO Auto-generated method stub
//		if (ls_anomalia.equals("6")){
//			return mj_anomalia_seis;
//		}else if (ls_anomalia.equals("B")){
//			globales.tll.getLecturaActual().setAnomalia("2");
//		}
        return null;
    }

    @Override
    public boolean puedoRepetirAnomalia() {
        return false;
    }

    @Override
    public String remplazaValorDeArchivo(int tipo, String ls_anomalia, String valor) {

        return valor;
    }

    @Override
    public void cambiosAnomalia(String anomalia) {
        //Vamos a hacer cambios al regresar de la pantalla de anomalias, asteriscos and stuff
        //Estamos basandonos en un cuadro


    }

    public void cambiosAlBorrarAnomalia(String anomaliaBorrada) {

//		else if (anomaliaBorrada.equals("6")){
//			if (globales.tll.getLecturaActual().getAnomaliasCapturadas().contains("G"))
//				globales.tll.getLecturaActual().deleteAnomalia("G");
//			else if(globales.tll.getLecturaActual().getAnomaliasCapturadas().contains("R"))
//				globales.tll.getLecturaActual().deleteAnomalia("R");
//			else if(globales.tll.getLecturaActual().getAnomaliasCapturadas().contains("Z"))
//				globales.tll.getLecturaActual().deleteAnomalia("Z");
//			else if(globales.tll.getLecturaActual().getAnomaliasCapturadas().contains("5"))
//				globales.tll.getLecturaActual().deleteAnomalia("5");
//		}
    }

    public void inicializaTablaDeCalidades() {
        ht_calidades = new Hashtable<String, Integer>();
        ht_calidades.put("AAA", 20);
        ht_calidades.put("AAU", 20);
        ht_calidades.put("AAX", 20);
        ht_calidades.put("AAP", 20);
        ht_calidades.put("B", 60);
        ht_calidades.put("FFS", 20);
        ht_calidades.put("FFC", 60);
        ht_calidades.put("G", 60);
        ht_calidades.put("H", 60);
        ht_calidades.put("L", 20);
        ht_calidades.put("N", 20);
        ht_calidades.put("RR1", 20);
        ht_calidades.put("RR2", 20);
        ht_calidades.put("RR3", 20);
        ht_calidades.put("RR4", 20);
        ht_calidades.put("SS1", 20);
        ht_calidades.put("SS2", 20);
        ht_calidades.put("SS3", 20);
        ht_calidades.put("SS4", 20);
        ht_calidades.put("ZZG", 20);
        ht_calidades.put("ZZR", 20);
        ht_calidades.put("ZZA", 20);
        ht_calidades.put("ZZL", 20);
        ht_calidades.put("ZZB", 20);
        ht_calidades.put("ZZP", 20);
        ht_calidades.put("ZZF", 20);
        ht_calidades.put("ZZM", 20);
        ht_calidades.put("ZZC", 20);
        ht_calidades.put("ZZT", 20);


    }

    @Override
    public int cambiaCalidadSegunTabla(String Anomalia, String subAnomalia) {

        return globales.calidadDeLaFoto;
    }

    public boolean continuarConLaFoto() {
        // TODO Auto-generated method stub

//		if (globales.tll.getLecturaActual().estadoDelSuministro.equals("2")){
//			return false;
//		}
        return true;
    }

}
