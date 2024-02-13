package enruta.soges_engie;

import java.util.Vector;

import enruta.soges_engie.R;
import enruta.soges_engie.clases.Utils;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;

public class Lectura {

	public final static int SIN_FOTO_AL_FINAL = 0;
	public final static int FOTO_AL_FINAL = 1;

	public static int LEIDA_LECTURA = 0;
	public static int LEIDA_ANOMALIA = 1;

	public final static int CLIENTE_YA_PAGO_MONTO = 13;
	public final static int CLIENTE_YA_PAGO_FECHA = 14;
	public final static int CLIENTE_YA_PAGO_AGENTE = 15;

	String is_supervisionLectura="0", is_reclamacionLectura="0", is_reclamacion,
			is_sectorLargo, is_sectorCorto, is_tarifa, is_ilr, is_marcaMedidor,
			is_serieMedidor, is_tipoMedidor, is_aviso, is_comollegar1,
			is_comoLlegar2, numeroDePortal, numeroDeEdificio="",
			estadoDelSuministro, dondeEsta, terminacion, ordenDeLectura, numerodeesferasReal, is_advertencias, 
			serieMedidorReal, is_fechaAviso, is_tipoLectura, is_estadoDelSuministroReal, is_estadoDeLaOrden;

	byte[] registro;

	private String is_cliente, is_colonia, is_municipio, is_direccion,
			is_lectura, is_anomalia="", is_texto, is_fecha, is_hora,
			is_comentarios;

	String intento1, intento2, intento3, intento4, intento5, intento6, is_vencido, is_balance, is_ultimo_pago, is_fecha_ultimo_pago, giro,diametro_toma,
			is_subAnomalia="", is_subAnomaliaInterna="",ls_mensaje, is_escalera="", is_piso, is_puerta="", is_consumo, intentos, sospechosa, is_tipoDeOrden, is_entrecalles, is_habitado;

	int nis_rad, numerodeesferas, consAnoAnt, consBimAnt, secuencia,
			 verificacionForzada, baremo=0, secuenciaReal;
	
	long lecturaAnterior;
	String poliza, is_ubicacion, is_estimaciones, contadorAlterno, is_numOrden, is_calle;
	long is_idOrden = 0;

	private DBHelper dbHelper;

	private SQLiteDatabase db;

	private Context context;

	private int FotoAlFinal;

	//Anomalia anomalia = null, subAnomalia = null;
	
	Vector <Anomalia> anomalias = new Vector <Anomalia>(), subAnomalias= new Vector <Anomalia>();

	boolean sigoYo = true;
	boolean verDatos=false;

	long index;

	String is_longitud = "0.0", is_latitud = "0.0";
//************************************************************************************************************************************
// CE, 06/10/23, Aqui tenemos que agregar una variable a todos los CamposEngie que agreguemos y los vamos a inicializar
	String miLongitud = "0.0";
	String miLatitud = "0.0";
	String is_MensajeOut = "";
	String is_numAviso = "";
	String is_cuentaContrato = "";
	String is_idMaterialSolicitado = "";
	String is_CancelarEnApp = "";
	String is_TextoLibreSAP = "";

	String is_EncuestaDeSatisfaccion = "";
	String is_MedidorInstalado = "";
	String is_idMarcaInstalada = "";
	String is_LecturaReal = "";
	String is_Repercusion = "";
	String is_idMaterialUtilizado = "";
	String is_idTipoDeReconexion = "";
	String is_idTipoDeRemocion = "";
	String is_ClienteYaPagoMonto="";
	String is_ClienteYaPagoFecha="";
	String is_ClienteYaPagoAgente="";
	String is_QuienAtendio="";
	String is_MarcaInstalada="";
	String is_SeQuitoTuberia="";
	String is_TuberiaRetirada="";
	String is_MarcaRetirada="";
	String is_MedidorRetirado="";
//************************************************************************************************************************************

	String is_MaterialRecuperado = "";

	private Resources res;

	Globales globales;
	boolean requiereGPS = true;


	Lectura(Context context, int secuencial) throws Throwable {
		this.context = context;
		this.secuencia = secuencial;
		res = context.getResources();

		globales = ((Globales) context.getApplicationContext());

		llenacampos();
	}

	public String getTipoDeOrden() {
		String strTipoDeOrden="DESCONOCIDO";
		if (is_tipoDeOrden.equals("TO002"))
			strTipoDeOrden = "DESCONEXIÓN";
		if (is_tipoDeOrden.equals("TO003"))
			strTipoDeOrden = "RECONEXIÓN";
		if (is_tipoDeOrden.equals("TO004"))
			strTipoDeOrden= "REC/REMO";
		if (is_tipoDeOrden.equals("TO005"))
			strTipoDeOrden= "REMOCIÓN";
		if (is_tipoDeOrden.equals("TO006"))
			strTipoDeOrden= "RX.EXPRESS";

		return strTipoDeOrden;
	}

	public String getFechaHora() {
		return is_fecha + is_hora;
	}

	private void llenacampos() throws Throwable {
		openDatabase();

		String params[] = { String.valueOf(secuencia) };
		Cursor c;

		c = db.rawQuery(
				"Select *, rowid from ruta where cast(secuenciaReal as Integer)= cast(? as Integer)",
				params);

		if (c.getCount() > 0) {
			c.moveToFirst();
			/*is_supervisionLectura = c.getString(c
					.getColumnIndex("supervisionLectura"));
			is_reclamacionLectura = c.getString(c
					.getColumnIndex("reclamacionLectura"));*/
			//lamacion = Utils.getString(c, "reclamacion")).trim();
			is_reclamacionLectura="1";
			is_habitado=Utils.getString(c, "habitado", "").trim();
			giro=Utils.getString(c, "giro", "").trim();
			diametro_toma=Utils.getString(c, "diametro", "").trim();
			is_vencido=Utils.getString(c, "vencido", "").trim();
			is_balance=Utils.getString(c, "balance", "").trim();
			is_ultimo_pago=Utils.getString(c, "ultimo_pago", "").trim();
			is_fecha_ultimo_pago=Utils.getString(c, "fecha_utlimo_pago", "").trim();
			is_entrecalles=Utils.getString(c, "entrecalles", "").trim();
			is_tipoDeOrden= Utils.getString(c, "tipoDeOrden", "").trim();
			is_estadoDeLaOrden=Utils.getString(c, "estadoDeLaOrden", "").trim();
			is_sectorLargo = Utils.getString(c, "sectorlargo", "");
			is_sectorCorto = Utils.getString(c, "sectorCorto", "");
			is_tarifa = Utils.getString(c, "tarifa", "");
			is_ilr = Utils.getString(c, "ilr", "");
			is_marcaMedidor = Utils.getString(c, "marcaMedidor", "");
			is_serieMedidor = Utils.getString(c, "serieMedidor", "").trim();
			is_tipoMedidor = Utils.getString(c, "tipoMedidor", "");
			is_aviso = Utils.getString(c, "aviso", "");
			is_comollegar1 = Utils.getString(c, "comoLlegar1", "");
			is_comoLlegar2 = Utils.getString(c, "comoLlegar2", "");
			contadorAlterno =Utils.getString(c, "rowid", "");
			serieMedidorReal=Utils.getString(c, "serieMedidorReal", "");
			secuenciaReal= Utils.getInt(c, "secuenciaReal", 0);
			is_cliente = Utils.getString(c, "cliente", "");
			is_colonia = Utils.getString(c, "colonia", "");
			is_direccion = Utils.getString(c, "direccion", "");
			is_lectura = Utils.getString(c, "lectura", "").trim();
			String ls_anomalia = Utils.getString(c, "anomalia", "");
			//is_texto = Utils.getString(c, "texto"));
			is_fecha = Utils.getString(c, "fecha", "");
			is_hora = Utils.getString(c, "hora", "");
			String ls_subAnomalia = Utils.getString(c, "subAnomalia", "");
			is_comentarios = Utils.getString(c, "comentarios", "").trim();
			is_advertencias=Utils.getString(c, "advertencias", "");
			is_consumo=Utils.getString(c, "consumo", "");
			is_tipoLectura=Utils.getString(c, "tipoLectura", "");

			if (is_comentarios == null) {
				is_comentarios = "";
			}
			
//			is_escalera = Utils.getString(c, "escalera"));
			is_piso = Utils.getString(c, "piso", "");
//			is_puerta = Utils.getString(c, "puerta"));
//			is_ubicacion=Utils.getString(c, "ubicacion"));
//			is_estimaciones=Utils.getString(c, "estimaciones")).trim();
			is_fechaAviso=Utils.getString(c, "fechaAviso", "").trim();
			

//			nis_rad = toInteger(Utils.getString(c, "nisRad")));
			poliza = Utils.getString(c, "poliza", "");
			is_numOrden=Utils.getString(c, "numOrden", "");
//			numerodeesferas =  toInteger(Utils.getString(c, "numEsferas")));
//			numerodeesferasReal= Utils.getString(c, "numEsferasReal")).trim();
//			consAnoAnt = toInteger(c.getString(c
//					.getColumnIndex("consAnoAnt")));
//			consBimAnt = toInteger(c.getString(c
//					.getColumnIndex("consBimAnt")));
			numeroDePortal = Utils.getString(c, "numPortal", "");
			numeroDeEdificio = Utils.getString(c, "numEdificio", "");
//			secuencia = toInteger(c.getString(c
//					.getColumnIndex("secuencia")));
			intentos = Utils.getString(c, "intentos", "").trim();
			sospechosa =  Utils.getString(c, "sospechosa", "").trim();
			is_calle=  Utils.getString(c, "calle", "").trim();
			
//			lecturaAnterior= toLong(Utils.getString(c, "lecturaAnterior")));
			
//			baremo =toInteger(Utils.getString(c, "baremo")));

//			if (secuencia % 2 == 0)
//				requiereGPS = true;
//			else
//				requiereGPS = false;
			
			requiereGPS = true;

			ordenDeLectura = Utils.getString(c, "ordenDeLectura", "");

			intento1 = Utils.getString(c, "intento1", "");
			intento2 = Utils.getString(c, "intento2", "");
			intento3 = Utils.getString(c, "intento3", "");
			intento4 = Utils.getString(c, "intento4", "");
			intento5 = Utils.getString(c, "intento5", "");
			intento6 = Utils.getString(c, "intento6", "");

			FotoAlFinal = Utils.getInt(c, "fotoAlFinal", 0);

			terminacion = Utils.getString(c, "terminacion", "");

			is_latitud = Utils.getString(c, "latitud", "");
			is_longitud = Utils.getString(c, "longitud", "");

//************************************************************************************************************************************
// CE, 06/10/23, Aqui vamos a inicializar las variables de todos los CamposEngie que agregamos
			miLatitud = Utils.getString(c, "miLatitud", "");
			miLongitud = Utils.getString(c, "miLongitud", "");
			is_MensajeOut = Utils.getString(c, "MensajeOut", "");
			is_numAviso = Utils.getString(c, "NumAviso", "");
			is_cuentaContrato = Utils.getString(c, "CuentaContrato", "");
			is_idMaterialSolicitado = Utils.getString(c, "idMaterialSolicitado", "");
//			is_CancelarEnApp = Utils.getString(c, "CancelarEnApp", "");
			is_TextoLibreSAP = Utils.getString(c, "TextoLibreSAP", "");

			is_EncuestaDeSatisfaccion = Utils.getString(c, "EncuestaDeSatisfaccion", "");
			is_MedidorInstalado = Utils.getString(c, "MedidorInstalado", "");
			is_idMarcaInstalada = Utils.getString(c, "idMarcaInstalada", "");
			is_LecturaReal = Utils.getString(c, "LecturaReal", "");
			is_Repercusion = Utils.getString(c, "Repercusion", "");
			is_idMaterialUtilizado = Utils.getString(c, "idMaterialUtilizado", "");
			is_idTipoDeReconexion = Utils.getString(c, "idTipoDeReconexion", "");
			is_idTipoDeRemocion = Utils.getString(c, "idTipoDeRemocion", "");
			is_ClienteYaPagoMonto = Utils.getString(c, "ClienteYaPagoMonto", "");
			is_ClienteYaPagoFecha = Utils.getString(c, "ClienteYaPagoFecha", "");
			is_ClienteYaPagoAgente = Utils.getString(c, "ClienteYaPagoAgente", "");
			is_QuienAtendio = Utils.getString(c, "QuienAtendio", "");
			is_MarcaInstalada = Utils.getString(c, "MarcaInstalada", "");
			is_SeQuitoTuberia = Utils.getString(c, "SeQuitoTuberia", "");
			is_TuberiaRetirada = Utils.getString(c, "TuberiaRetirada", "");
			is_MarcaRetirada = Utils.getString(c, "MarcaRetirada", "");
			is_MedidorRetirado = Utils.getString(c, "MedidorRetirado", "");
			is_MaterialRecuperado = Utils.getString(c, "MaterialRecuperado", "");
//************************************************************************************************************************************

			ls_mensaje= Utils.getString(c, "mensaje", "");
			is_idOrden = Utils.getLong(c, "idOrden", 0);

			estadoDelSuministro = Utils.getString(c, "estadoDelSuministro", "").trim();
			
			if (!globales.puedoVerLosDatos)
				verDatos =Utils.getInt(c, "verDatos", 0)==0? false:true;
			else
				verDatos=true;
			//registro = c.getBlob(c.getColumnIndex("registro"));
//			dondeEsta = Utils.getString(c, "dondeEsta"));

//			if (ls_anomalia.equals("") && secuencia%3==0)
//				ls_anomalia="AC*";
			if (!ls_anomalia.trim().equals(""))
				setAnomalia(ls_anomalia.trim());
			

			if (!ls_subAnomalia.trim().equals(""))
				setSubAnomalia(ls_subAnomalia.trim());

			// La lectura decidirá si es ausente o no
			if (!lecturaAusente() && is_lectura.equals("")) {
				sigoYo = true;
			} else
				sigoYo = false;
		} else {
			throw new Throwable(
					"No se encontraron lecturas con dicho secuencial");
		}

		if (globales.habilitarPuntoDecimal) {
//			is_lectura = String.valueOf(Double.parseDouble(is_lectura) / 1000f);
		}

		c.close();
		closeDatabase();

	}

	void establecerFotoAlFinal(int estado) {
		openDatabase();

		db.execSQL("Update ruta set fotoAlFinal=" + estado
				+ " where cast(secuenciaReal as Integer)= cast(" + secuencia
				+ " as Integer)");

		closeDatabase();

	}

	private void openDatabase() {
		dbHelper = DBHelper.getInstance(this.context);

		db = dbHelper.getReadableDatabase();
	}

	private void closeDatabase() {
		db.close();
		dbHelper.close();

	}

	public String getColonia() {
		return is_colonia.trim();
	}
	
	public void setColonia(String ls_colonia) {
		is_colonia=ls_colonia;
	}

	/*
	 * public void setLectura(String ls_lectura, String ls_anomalia){
	 * 
	 * }
	 */
	public boolean lecturaAusente() {
		boolean retorno = false;
		
		for (Anomalia anomalia:anomalias){
			if (anomalia.esAusente())
			{
				retorno=true;
				break;
			}
		}

//		if (anomalia != null) {
//			retorno = anomalia.esAusente();
//		}

		return retorno;
	}

	public void guardarSospechosa() {
		guardarSospechosa(Integer.parseInt(sospechosa));
	}

	public void guardarSospechosa(int veces) {
		openDatabase();
		ContentValues cv_params = new ContentValues();

		sospechosa = String.valueOf(veces);

		cv_params.put("sospechosa", sospechosa);

		String params[] = { String.valueOf(secuenciaReal) };

		db.update("ruta", cv_params,
				"cast(secuenciaReal as Integer)= cast(? as Integer)", params);

		closeDatabase();
	}

	public void guardar(int ordenDeLectura) {
		guardar(true, ordenDeLectura);
	}

	public void guardar(boolean agregarOrdenDeLectura, int ordenDeLectura) {
		openDatabase();
		ContentValues cv_params = new ContentValues();

		//globales.tdlg.setConsumo();
		
		//Guardamos el dia que se ingreso el aviso en caso de tener uno
		//if ((globales.tdlg.esSegundaVisita(is_anomalia, is_subAnomalia) || requiereLectura()==Anomalia.LECTURA_AUSENTE)&& is_fechaAviso.equals("")){
//		if (/*!is_anomalia.equals("") && */is_fechaAviso.equals("") && this.getAnomaliasABorrar().respuestas.size()>0){
//			is_fechaAviso= Main.obtieneFecha(globales.tlc.getRellenoCampo("fechaAviso"));
//		}
//		else if(/*is_anomalia.equals("")*/ this.getAnomaliasABorrar().respuestas.size()==0){
//			is_fechaAviso="";
//		}
		
		//Si no tienen lectura quitamos la fecha y hora, de lo contrario la calculamos
		if (is_lectura.equals("") && is_anomalia.equals("") ){
			is_fecha="";
			is_hora="";
		}else{
			is_fecha=Main.obtieneFecha(globales.tlc.getRellenoCampo("fecha"));
			is_hora=Main.obtieneFecha(globales.tlc.getRellenoCampo("hora"));
		}
		
		if (is_lectura.equals("")){
			is_lectura=Main.rellenaString("", " ", globales.tlc.getLongCampo("lectura"), false);
			is_estadoDeLaOrden="EO002";
		}else
		{
			is_estadoDeLaOrden="EO004";
		}
		
//		if (!sospechosa.trim().equals("") ||!intentos.trim().equals("") ){
//			sospechosa=Main.rellenaString(sospechosa, "0", globales.tlc.getLongCampo("sospechosa"), true);
//			intentos=Main.rellenaString(intentos, "0", globales.tlc.getLongCampo("intentos"), true);
//		}
		
		
//		if (!intentos.trim().equals("")){
//			intentos=Main.rellenaString(intentos, "0", globales.tlc.getLongCampo("intentos"), true);
//		}
		
		globales.tdlg.setTipoLectura();
		
		if (globales.GPS){
			try{
				is_latitud=String.valueOf(globales.location.getLatitude());
				is_longitud=String.valueOf(globales.location.getLongitude());
			}catch(Throwable e){
				is_latitud="0.0";
				is_longitud="0.0";
			}
			
		}
		
// CE, 12/10/23, Vamos a dejar la lectura capturada
//		if(!is_anomalia.equals(""))
//			is_lectura="30";

		cv_params.put("lectura", is_lectura);
		cv_params.put("consumo", is_consumo);
		cv_params.put("anomalia", is_anomalia);
		cv_params.put("subAnomalia", is_subAnomalia);
		cv_params.put("terminacion", terminacion);
		cv_params.put("sospechosa", sospechosa);//Confirmada
		cv_params.put("intentos", intentos);//Distinta
		cv_params.put("fecha", is_fecha);
		cv_params.put("hora", is_hora);
		//cv_params.put("registro", registro);
		cv_params.put("comentarios", is_comentarios);
		cv_params.put("intento1", intento1);
		cv_params.put("intento2", intento2);
		cv_params.put("intento3", intento3);
		cv_params.put("intento4", intento4);
		cv_params.put("intento5", intento5);
		cv_params.put("intento6", intento6);
		cv_params.put("mensaje", ls_mensaje);
		cv_params.put("ordenDeLectura", this.ordenDeLectura);
		cv_params.put("ubicacion", is_ubicacion);
		cv_params.put("advertencias", is_advertencias);
		cv_params.put("numEsferasReal", numerodeesferasReal);
		cv_params.put("serieMedidorReal", serieMedidorReal);
		cv_params.put("direccion", is_direccion);
		cv_params.put("colonia", is_colonia);
		cv_params.put("numEdificio", numeroDeEdificio);
		cv_params.put("numPortal", numeroDePortal);
		cv_params.put("escalera", is_escalera);
		cv_params.put("piso", is_piso);
		cv_params.put("puerta", is_puerta);
		cv_params.put("lecturista", globales.getUsuario());
		cv_params.put("fechaAviso", is_fechaAviso);
		cv_params.put("aviso", is_aviso);
		cv_params.put("tipoLectura", is_tipoLectura);
		cv_params.put("estadoDelSuministroReal", is_estadoDelSuministroReal);
		cv_params.put("latitud", is_latitud);
		cv_params.put("longitud", is_longitud);
		cv_params.put("estadoDeLaOrden", is_estadoDeLaOrden);
		cv_params.put("envio", 1);

//************************************************************************************************************************************
// CE, 06/10/23, Aqui vamos a poner todos los CamposEngie que vamos a enviar de regreso al servidor
		cv_params.put("EncuestaDeSatisfaccion", is_EncuestaDeSatisfaccion);
		cv_params.put("MedidorInstalado", is_MedidorInstalado);
		cv_params.put("idMarcaInstalada", is_idMarcaInstalada);
		cv_params.put("LecturaReal", is_LecturaReal);
		cv_params.put("Repercusion", is_Repercusion);
		cv_params.put("idMaterialUtilizado", is_idMaterialUtilizado);
		cv_params.put("idTipoDeReconexion", is_idTipoDeReconexion);
		cv_params.put("idTipoDeRemocion", is_idTipoDeRemocion);
		cv_params.put("ClienteYaPagoMonto", is_ClienteYaPagoMonto);
		cv_params.put("ClienteYaPagoFecha", is_ClienteYaPagoFecha);
		cv_params.put("ClienteYaPagoAgente", is_ClienteYaPagoAgente);
		cv_params.put("QuienAtendio", is_QuienAtendio);
		cv_params.put("MarcaInstalada", is_MarcaInstalada);
		cv_params.put("SeQuitoTuberia", is_SeQuitoTuberia);
		cv_params.put("TuberiaRetirada", is_TuberiaRetirada);
		cv_params.put("MarcaRetirada", is_MarcaRetirada);
		cv_params.put("MedidorRetirado", is_MedidorRetirado);
		cv_params.put("MaterialRecuperado", is_MaterialRecuperado);
//************************************************************************************************************************************

		String params[] = { String.valueOf(secuenciaReal) };

		db.update("ruta", cv_params,
				"cast(secuenciaReal as Integer)= cast(? as Integer)", params);

		closeDatabase();
	}

	void setTerminacion(String terminacion) {
		this.terminacion = terminacion;

		openDatabase();
		ContentValues cv_params = new ContentValues();

		cv_params.put("terminacion", terminacion);

		String params[] = { String.valueOf(secuenciaReal) };

		db.update("ruta", cv_params,
				"cast(secuenciaReal as Integer)= cast(? as Integer)", params);

		closeDatabase();

	}

	/*
	 * public Anomalia getAnomalia(){ return anomalia; }
	 */

	public void forzarLecturas() {

		// if (is_serieMedidor.contains("CF") /*&& is_anomalia.equals("") &&
		// is_lectura.equals("")*/){
		// is_lectura="0";
		// }
		// else{
		is_anomalia = "888";
		is_comentarios = "NO HABILITADO";
		is_tipoLectura="4";
		// }

		guardar(false, 0);
	}

	public boolean setClienteYaPago(Bundle bu_params) {
		if (bu_params == null)
			return false;
		globales.tll.getLecturaActual().is_ClienteYaPagoMonto = bu_params.getString(String.valueOf(CLIENTE_YA_PAGO_MONTO));
		globales.tll.getLecturaActual().is_ClienteYaPagoFecha = bu_params.getString(String.valueOf(CLIENTE_YA_PAGO_FECHA));
		globales.tll.getLecturaActual().is_ClienteYaPagoAgente = bu_params.getString(String.valueOf(CLIENTE_YA_PAGO_AGENTE));
		if (!globales.tll.getLecturaActual().is_vencido.equals("")) {
			if ((Float.parseFloat(globales.tll.getLecturaActual().is_ClienteYaPagoMonto) + 100.0) < Float.parseFloat(globales.tll.getLecturaActual().is_vencido)) {
				return true;
			}
		}
		return false;
	}

	public void setComentarios(String ls_comentarios) {

		if (globales.multiplesAnomalias){
			//No se pueden agregar comentarios vacios
			if (ls_comentarios.equals("")){
				return;
			}
			//No puede tener punto y coma, se confundirá el programa
			ls_comentarios= eliminaCaracter(ls_comentarios, ";");
//			if (!is_comentarios.equals("")){
//				is_comentarios +=";";
//			}
			is_comentarios += ls_comentarios+";";
		}
		else
			is_comentarios = ls_comentarios;
	}

	public String getAnomalia() {
		return getAnomaliaAMostrar();
	}

	public void setAnomalia(String ls_anomalia) {
		int tope=1, comienzo=0;
		
		
		
		Anomalia anomalia=null;
		
//		if (ls_anomalia.equals("") && !globales.multiplesAnomalias){
//		
//			//Si no acepta anomalias multiples debe limpiar el vector
//
//				anomalias.clear();
//			return;
//		}
		
		if (ls_anomalia.equals(""))
				{
			//No se aceptan vacios
			return;
				}
		
		if (!globales.multiplesAnomalias){
			is_anomalia = ls_anomalia;
			
		}
		else{
			//Cuando acepta multiples se concatenan
			is_anomalia += ls_anomalia;
			tope=ls_anomalia.length();
			comienzo=ls_anomalia.lastIndexOf("*") +1;
			}
		
		if (ls_anomalia.equals("*")){
			//Se agrego un asterisco lo que haya antes no nos interesa
			anomalias.clear();
			return;
		}

		for (int i=comienzo; i<tope;i++){
			
			anomalia=new Anomalia(context, ls_anomalia.substring(i, i+1), false);
			
			if (globales.multiplesAnomalias || anomalias.size()==0){
				anomalias.add( anomalia);
			}else{
				//Si no soporta multiples anomalias debe siempre guardar en el primero
						
					anomalias.set(0, anomalia);

			}
		}
		
		
//		if (!ls_anomalia.equals(""))
//			anomalia = new Anomalia(context, ls_anomalia, false);
//		else
//			anomalia = null;
	}

	public void setSubAnomalia(String ls_anomalia) {
		int tope=1, cominezo=0;
		String[] subanomaliasArray;
		
		Anomalia subAnomalia=null;
		
		ls_anomalia= ls_anomalia.trim();
		if (ls_anomalia.equals("")){
			//Aqui no se aceptan
			return;
		}
		
		
		if (!globales.multiplesAnomalias ){
			is_subAnomalia = ls_anomalia;
		}
		else{
			//Cuando acepta multiples se concatenan, separandolos por un ;
//			if (is_subAnomalia.length()>0 && !ls_anomalia.endsWith("*") )
//				is_subAnomalia+=";";
			
			if (ls_anomalia.equals("*")){
				is_subAnomaliaInterna+="*";
			}
			else{
				is_subAnomalia += eliminaCaracter(ls_anomalia, "*");;
				if (!is_subAnomalia.endsWith(";")){
					is_subAnomalia+=";";
				}
				
				is_subAnomaliaInterna+=ls_anomalia;
				if (!is_subAnomaliaInterna.endsWith(";")){
					is_subAnomaliaInterna+=";";
				}
			}
			
					
					
					//is_subAnomaliaInterna=is_subAnomalia;
					
					if (anomalias.size()==0){
						is_subAnomaliaInterna+="*";
					}
			
			if (ls_anomalia.equals("*")){
				//Se agrego un asterisco lo que haya antes no nos interesa
				subAnomalias.clear();
				return;
			}
			
			ls_anomalia= is_subAnomaliaInterna.substring(is_subAnomaliaInterna.lastIndexOf("*")+1, is_subAnomaliaInterna.length());
			}
		
		if (ls_anomalia.equals("")){
			return;
		}
		
		
		subanomaliasArray= ls_anomalia.split(";");
		
		tope=subanomaliasArray.length;
		
		for (int i=0; i<tope;i++){
//			if (!ls_anomalia.equals(""))
			
				subAnomalia=new Anomalia(context, subanomaliasArray[i], true);
//			else
//			{
//				//Si no acepta anomalias multiples debe limpiar el vector
//				if (!globales.multiplesAnomalias ){
//					subAnomalias.clear();
//				}
//				else
//				{
//					//No agregaremos una subanomalia nula
//					return;
//				}
//			}
			
			if (globales.multiplesAnomalias || subAnomalias.size()==0){
				subAnomalias.add( subAnomalia);
			}else{
				//Si no soporta multiples anomalias debe siempre guardar en el primero
						
				subAnomalias.set(0, subAnomalia);

			}
			
			

			
		}
		
		
//		if (!ls_anomalia.equals(""))
//			subAnomalia = new Anomalia(context, ls_anomalia, true);
//		else
//			subAnomalia = null;

	}

	// public void setIntento(int intento, String ls_lectura, boolean
	// esCorrecta){
	// if(intentos>1){
	// sospechosa=1;
	// }
	// this.intentos=intento;
	//
	// switch(intento){
	// case 1:
	// intento1=ls_lectura;
	// break;
	//
	// case 2:
	// intento2=ls_lectura;
	// break;
	//
	// case 3:
	// intento3=ls_lectura;
	// break;
	//
	// case 4:
	// intento4=ls_lectura;
	// break;
	//
	// case 5:
	// intento5=ls_lectura;
	// break;
	//
	// case 6:
	// intento6=ls_lectura;
	// break;
	// }
	// if (esCorrecta)
	// is_lectura=ls_lectura;
	//
	//
	//
	//
	// guardar(false, 0);
	// }

	public void setLectura(String lectura) {
		is_lectura = lectura;
	}

	public String getDireccion() {
//		String ls_cadena = "";
//
//		// ls_cadena=is_direccion.trim();
//
//		/*
//		 * if(numeroDeEdificio.trim().length()>0){ ls_cadena+= " " +
//		 * numeroDeEdificio.trim(); }
//		 * 
//		 * if(numeroDePortal.trim().length()>0){ ls_cadena+= " " +
//		 * numeroDePortal.trim(); }
//		 * 
//		 * if(is_colonia.trim().length()>0){ ls_cadena+= " " +
//		 * is_colonia.trim(); }
//		 */
//
//		ls_cadena += is_colonia.trim() + "\n" + is_comollegar1.trim() + "\n"
//				+ dondeEsta.trim();
//
//		return ls_cadena;

// CE, 03/11/23, La direccion ya viene completa en el mismo campo
//		String direccion=is_calle + " #" +numeroDePortal.trim();
		String direccion=is_calle;

		return direccion.trim();
	}
	
	public void setDireccion(String ls_direccion) {

		
		is_direccion=ls_direccion;
	}

	public String getAcceso() {
		String ls_cadena = "";
		if (numeroDeEdificio.trim().length() != 0
				&& numeroDePortal.trim().length() != 0) {
			ls_cadena = "Pda. ";
			ls_cadena += numeroDeEdificio.trim() + "-" + numeroDePortal.trim();
		}
		return ls_cadena;
	}

	public String getLectura() {
		return is_lectura;
	}

	public String getNombreAnomalia() {
		
		if (!globales.multiplesAnomalias && anomalias.size()>0){
			return anomalias.get(0).is_desc;
		}
		else if (globales.multiplesAnomalias ){
			String cadena="";
			
			for (Anomalia anomalia:anomalias){
				cadena += anomalia.is_desc + " ";
			}
			return cadena;
		}
		
//		if (anomalia != null)
//			return anomalia.is_desc;
//		else
			return "";
	}

	public String getNombreCliente() {
		return is_cliente;
	}

	private String generaCadenaAEnviar() {
		String ls_cadena = "";
		String ls_lectura;
		String ls_tmpSubAnom = "";

		ls_cadena = is_lectura.length() == 0 ? "4" : "0"; // Indicador de tipo
															// de lectura
		ls_cadena += Main.rellenaString(is_lectura, "0",
				globales.tlc.getLongCampo("Lectura"), true);
		ls_cadena += Main.rellenaString(is_lectura, is_lectura.equals("") ? " "
				: "0", globales.tlc.getLongCampo("Lectura"), true);
		ls_cadena += is_fecha;
		ls_cadena += is_hora;
		ls_cadena += Main.rellenaString(is_anomalia,
				is_anomalia.equals("") ? " " : "0",
						globales.tlc.getLongCampo("anomalia"), true);
		// Esto no se bien de que se trata, asi que de momento dejaremos
		// ceros...
		ls_cadena += Main.rellenaString(String.valueOf(sospechosa), "0",
				globales.tlc.getLongCampo("sospechosa"), true);
		ls_cadena += Main.rellenaString(String.valueOf(intentos), "0",
				globales.tlc.getLongCampo("intentos"), true);

		if (is_anomalia.equals("888"))
			ls_tmpSubAnom = "9";
		else
			ls_tmpSubAnom = "";

		ls_cadena += Main
				.rellenaString(ls_tmpSubAnom, "0", globales.tlc.getLongCampo("anomInst"),
						true);

		return ls_cadena;
	}

	public String getComentarios() {
		String ls_cadena = "";

//		if (subAnomalia != null) {
//			ls_cadena += subAnomalia.is_desc;
//		}
//
//		if (!ls_cadena.equals("")) {
//			ls_cadena += "\n";
//		}

		ls_cadena += is_comentarios;
		return ls_cadena;
	}

	public boolean confirmarLectura() {
		return is_supervisionLectura.equals("1")
				|| is_reclamacionLectura.equals("1");

	}

	public Spanned getInfoPreview(int tipoDeBusqueda, String textoBuscado, int totalMedidores) {
		String ls_preview = "";
		// int antes=0;
		//
		// antes=is_serieMedidor.indexOf(textoBuscado);
		//
		// //ls_preview= "<![CDATA[";
		//
		//
		// if (antes>=0){
		// if (antes>0)
		// ls_preview += is_serieMedidor.substring(0, antes-1);
		//
		// ls_preview += "<b>" +is_serieMedidor.substring(antes, antes +
		// textoBuscado.length() ) + "</b>";
		//
		// //if ((antes+ textoBuscado.length() + 1)<=is_serieMedidor.length())
		// ls_preview += is_serieMedidor.substring(antes+ textoBuscado.length()
		// /*+ 1*/);
		//
		//
		//
		// }
		// else{
		// ls_preview= this.is_serieMedidor;
		// }

		ls_preview=globales.tdlg.getDescripcionDeBuscarMedidor(this,
				tipoDeBusqueda, textoBuscado);

// CE, 05/11/23, Vamos a mostrar un listado mas conciso para mostrar mas renglones en la pantalla
//		ls_preview += "<br>" + (globales.mostrarRowIdSecuencia?contadorAlterno:secuencia) + " " + context.getString(R.string.de) + " " +totalMedidores;

		// ls_preview +="\n"+ getDireccion();
		// ls_preview += "<br>" +is_colonia.trim();
		// ls_preview +="<br>" + getAcceso();

		// ls_preview +="]]>" ;

		return Html.fromHtml(ls_preview);
	}

	public static String marcarTexto(String texto, String textoBuscado,
			boolean restarUnEspacio) {
		int antes = 0;
		String ls_preview = "";

		antes = texto.indexOf(textoBuscado);

		if (antes >= 0) {
			if (antes > 0)
				ls_preview += texto.substring(0, antes);

			ls_preview += "<b>"
					+ texto.substring(antes, antes + textoBuscado.length())
					+ "</b>";

			// if ((antes+ textoBuscado.length() + 1)<=is_serieMedidor.length())
			ls_preview += texto
					.substring(antes + textoBuscado.length() /* + 1 */);

		} else {
			ls_preview = texto;
		}

		return ls_preview;
	}

	// public String formatedInfoReadMetter(){
	// String ls_preview="";
	//
	// if ((is_lectura.length()==0 && is_anomalia.length()==0)){
	// ls_preview="L";
	// //color= R.color.Red;
	// }
	// else if ((is_lectura.length()!=0 && is_anomalia.length()==0)){
	// ls_preview="L";
	// //color= R.color.Green;
	// }
	// else if ((is_lectura.length()==0 && is_anomalia.length()!=0)){
	// ls_preview="A";
	// //color= R.color.Red;
	// }
	// else if ((is_lectura.length()!=0 && is_anomalia.length()!=0)){
	// ls_preview="A";
	// //color= R.color.Orange;
	// }
	//
	//
	//
	//
	// return ls_preview;
	// }

	public int colorInfoReadMetter(int tipo) {
		// String ls_preview="";
		int color = R.color.Gray;

		if (((is_lectura.length() != 0 && (is_anomalia.length() == 0 || is_anomalia.endsWith("*"))) || (is_lectura
				.length() != 0 && (is_anomalia.length() != 0|| !is_anomalia.endsWith("*"))))
				&& tipo == LEIDA_LECTURA) {
			// ls_preview="L";
			color = R.color.green;
		} else if ((is_lectura.length() == 0 && (is_anomalia.length() != 0 && !is_anomalia.endsWith("*")))
				&& tipo == LEIDA_ANOMALIA) {
			// ls_preview="A";
			color = R.color.red;
		} else if ((is_lectura.length() != 0 && (is_anomalia.length() != 0 && !is_anomalia.endsWith("*")))
				&& (tipo == LEIDA_ANOMALIA)) {
			// ls_preview="A";
			color = R.color.Orange;
		}

		return color;
	}

	public void setPuntoGPS(Location location) {
		if (location == null) {
			is_longitud = "0.0";
			is_latitud = "0.0";
		} else {
			is_longitud = String.valueOf(location.getLongitude());
			is_latitud = String.valueOf(location.getLatitude());
		}

		openDatabase();

		ContentValues cv_params = new ContentValues();

		String params[] = { String.valueOf(secuenciaReal) };

		cv_params.put("latitud", is_latitud);
		cv_params.put("longitud", is_longitud);

		db.update("Ruta", cv_params,
				"cast(secuenciaReal as Integer)= cast(? as Integer)", params);

		closeDatabase();

	}

//************************************************************************************************************************************
// CE, 06/10/23, Opcionalmente podemos agregar una funcion para leer y escribir todos los CamposEngie que agregamos
	public String getMiLatitud() {
		return miLatitud;
	}

	public String getMiLongitud() {
		return miLongitud;
	}
//************************************************************************************************************************************

	public String getAnomaliaAMostrar(){
//		String cadena="";
//		for (Anomalia anomalia:anomalias){
//			
//			if (globales.convertirAnomalias)
//				cadena += anomalia.is_conv ;
//		}
		
		return is_anomalia;
	}
	
	public String getSubAnomaliaAMostrar(){
//		String cadena="";
//		for (Anomalia subAnomalia:subAnomalias){
//			if (!cadena.equals(""))
//				cadena +=";";
//			
//			if (globales.convertirAnomalias && subAnomalia!=null)
//				cadena += subAnomalia.is_desc ;
//		}
		
		return is_subAnomalia;
	}
	
	public int requiereLectura(){
		if (anomalias.size()==0)
			return Anomalia.SIN_ANOMALIA;
		//for (int i=0; i<anomalias.size();i++){
		//int 
			
			if (subAnomalias.size()>0){
				if (globales.multiplesAnomalias){
					String index="";
					
					if (globales.convertirAnomalias)
						index=anomalias.get(anomalias.size()-1).is_conv;
					else
						index=anomalias.get(anomalias.size()-1).is_anomalia;
					
					for (Anomalia anom: subAnomalias){
						if ((anom.is_anomalia.startsWith(index) && !globales.convertirAnomalias) ||(anom.is_conv.startsWith(index) && globales.convertirAnomalias)){

								return anom.requiereLectura();
						}
					}
					
					return anomalias.get(anomalias.size()-1).requiereLectura();
					
				}else
				{
					return subAnomalias.get(0).requiereLectura();
				}
				
			}
			else{
				return anomalias.get(anomalias.size()-1).requiereLectura();
			}

			
		//}

	}
	
	public boolean hayAnomaliasConLecturaAusente(){
		if (anomalias.size()==0)
			return false;
		for (int i=0; i<anomalias.size();i++){
		//int 
			
			
			if (globales.multiplesAnomalias){
				if (subAnomalias.size()>0){
					String index="";
					
					if (globales.convertirAnomalias)
						index=anomalias.get(i).is_conv;
					else
						index=anomalias.get(i).is_anomalia;
					
					for (Anomalia anom: subAnomalias){
						if ((anom.is_anomalia.startsWith(index)) ||(anom.is_conv.startsWith(index))){

								if (Anomalia.LECTURA_AUSENTE==anom.requiereLectura()){
									return true;
								}
						}
					}
					
					if ( anomalias.get(i).requiereLectura()==Anomalia.LECTURA_AUSENTE){
						return true;
					}
					
				}else
				{
//					if ( subAnomalias.get(0).requiereLectura()==Anomalia.LECTURA_AUSENTE){
//						return true;
//					}
					if ( anomalias.get(i).requiereLectura()==Anomalia.LECTURA_AUSENTE){
						return true;
					}
					
				}
				
			}
			else{
				if ( anomalias.get(i).requiereLectura()==Anomalia.LECTURA_AUSENTE){
					return true;
				}
			}

			
		}
		
		return false;

	}
	
	public void borrarLecturasAusentes(){
		if (anomalias.size()==0)
			return;
		for (int i=anomalias.size()-1; i>=0;i--){
		//int 
			String index="";
			
			if (globales.convertirAnomalias)
				index=anomalias.get(i).is_conv;
			else
				index=anomalias.get(i).is_anomalia;
			
			if (globales.multiplesAnomalias){
				if (subAnomalias.size()>0){
					
					
					for (int j=subAnomalias.size()-1; j>=0;j--){
						Anomalia anom=subAnomalias.get(j);
						if ((anom.is_anomalia.startsWith(index)) ||(anom.is_conv.startsWith(index))){
							if (anom.ii_ausente==4)
								deleteAnomalia(index);
						}
					}
					
					
				}else
				{
					if (anomalias.get(i).ii_ausente==4)
						deleteAnomalia(index);
					}
					
				
				
			}
			else{
				if (anomalias.get(i).ii_ausente==4)
					deleteAnomalia(index);
			}

			
		}
		

	}
	
	public int requiereFotoAnomalia(){
		
		if (anomalias.size()>0){
			if (subAnomalias.size()>0){
				if (globales.multiplesAnomalias){
					String index=  anomalias.get(anomalias.size()-1).is_anomalia;
					
					if (globales.convertirAnomalias)
						index=anomalias.get(anomalias.size()-1).is_conv;
					else
						index=anomalias.get(anomalias.size()-1).is_anomalia;
					
					for (Anomalia anom: subAnomalias){
						
						String codigoAnomalia="";
						if (globales.convertirAnomalias)
							codigoAnomalia=anom.is_conv;
						else
							codigoAnomalia=anom.is_anomalia;
						
						if (codigoAnomalia.startsWith(index)){
							//return anom.ii_foto;
							String ls_indicadorFoto=globales.tdlg.remplazaValorDeArchivo(TomaDeLecturasGenerica.FOTOS, globales.convertirAnomalias? anom.is_conv: anom.is_anomalia, String.valueOf(anom.ii_foto));
							return Integer.parseInt(ls_indicadorFoto);
						}
					}
					
					//return anomalias.get(anomalias.size()-1).ii_foto; 
					String ls_indicadorFoto=globales.tdlg.remplazaValorDeArchivo(TomaDeLecturasGenerica.FOTOS, globales.convertirAnomalias? anomalias.get(anomalias.size()-1).is_conv: anomalias.get(anomalias.size()-1).is_anomalia, String.valueOf(anomalias.get(anomalias.size()-1).ii_foto));
					return Integer.parseInt(ls_indicadorFoto);
					
				}else
				{
					//return subAnomalias.get(subAnomalias.size()-1).ii_foto;
					String ls_indicadorFoto=globales.tdlg.remplazaValorDeArchivo(TomaDeLecturasGenerica.FOTOS, globales.convertirAnomalias? subAnomalias.get(subAnomalias.size()-1).is_conv: subAnomalias.get(subAnomalias.size()-1).is_anomalia, String.valueOf(subAnomalias.get(subAnomalias.size()-1).ii_foto));
					return Integer.parseInt(ls_indicadorFoto);
				}
				
			}
			else{
				String ls_indicadorFoto=globales.tdlg.remplazaValorDeArchivo(TomaDeLecturasGenerica.FOTOS, globales.convertirAnomalias? anomalias.get(anomalias.size()-1).is_conv: anomalias.get(anomalias.size()-1).is_anomalia, String.valueOf(anomalias.get(anomalias.size()-1).ii_foto));
				return Integer.parseInt(ls_indicadorFoto);
			}
		}
		
		
		
			return 0;
	}
	
	public Anomalia getUltimaAnomalia(){
		if (anomalias.size()>0)
			return anomalias.get(anomalias.size()-1);
		
		return null;
	}
	
	public MensajeEspecial getAnomaliasABorrar(){
		
		Vector <Respuesta> respuesta= new Vector <Respuesta> () ;
		
		for (Anomalia anom: anomalias){
			if (anom.is_activa.equals("I")){
				continue;
			}
			String codigoAnomalia="";
			if (globales.convertirAnomalias)
				codigoAnomalia=anom.is_conv;
			else
				codigoAnomalia=anom.is_anomalia;
			
			respuesta.add(new Respuesta(codigoAnomalia, anom.is_desc));
		}
		
		MensajeEspecial mj_activo= new MensajeEspecial("Seleccione anomalia a borrar", respuesta, 0);
		return mj_activo;
	}
	
public String getAnomaliasAIngresadas(){
		
		//Vector <Respuesta> respuesta= new Vector <Respuesta> () ;
		String respuesta ="";
		for (Anomalia anom: anomalias){
			if (anom.is_activa.equals("I")){
				continue;
			}
			String codigoAnomalia="";
			if (globales.convertirAnomalias)
				codigoAnomalia=anom.is_conv;
			else
				codigoAnomalia=anom.is_anomalia;
			
			//respuesta.add(new Respuesta(codigoAnomalia, anom.is_desc));
			respuesta+=codigoAnomalia;
		}
		
		//MensajeEspecial mj_activo= new MensajeEspecial("Seleccione anomalia a borrar", respuesta, 0);
		return respuesta;
	}
	
	/**
	 * Elimina una anomalia y su subanomalia del arreglo con dado numero de anomalia
	 * @param index Espacio en el arreglo en donde se encuentra la anomalia
	 * @return Un booleano que indica si la anomalia ha sido borrada
	 */
	public boolean  deleteAnomalia(String index){
		boolean borrada=false;
		for (int i=0; i<anomalias.size();i++){
			if (globales.convertirAnomalias)
				{
				if (anomalias.get(i).is_conv.equals(index)){
					borrada= deleteAnomalia(i);
					break;
				}
					
					
				}
			else{
				if (anomalias.get(i).is_anomalia.equals(index)){
					borrada= deleteAnomalia(i);
					break;
				}
					
			}
		}
		return borrada;
	}
	
	/**
	 * Elimina una anomalia y su subanomalia del arreglo con dado numero de anomalia
	 * @param index Espacio en el arreglo en donde se encuentra la anomalia
	 * @return Un booleano que indica si la anomalia ha sido borrada
	 */
	public boolean  deleteAnomalia(int index){
		
		if (index>= anomalias.size()){
			return false;
		}
		
		Anomalia anom= anomalias.get(index);
		String codigoAnomalia="";
		if (globales.convertirAnomalias)
			codigoAnomalia=anom.is_conv;
		else
			codigoAnomalia=anom.is_anomalia;
		
		if (globales.multiplesAnomalias){
			
			
			
			//Borramos la subanomalia relacionada
			for(int i=0 ; i<subAnomalias.size();i++){
				
				if (subAnomalias.get(i).is_desc.startsWith(codigoAnomalia))
				{
					//Esa subAnomalia se borra
					
					borrarComentarioAnomalia(subAnomalias.get(i).is_desc.substring(0, globales.longitudCodigoSubAnomalia));
					globales.tdlg.DeshacerModificacionesDeAnomalia(subAnomalias.get(i).is_desc.substring(0, globales.longitudCodigoSubAnomalia));
					subAnomalias.removeElementAt(i);
					break;
				}
			}
			
			//ahora la anomalia
			anomalias.remove(index);
			
			//Hay que rehacer el is_anomalia y el is_subAnomalia
			String cadena=getAnomaliasCapturadas();
			
			is_anomalia= is_anomalia.substring(0, is_anomalia.lastIndexOf("*")+1) + cadena;
			
			cadena="";
			
			for (Anomalia anomalia:subAnomalias){
				
				
				if (globales.convertirAnomalias)
					cadena += anomalia.is_desc.substring(0, globales.longitudCodigoSubAnomalia) ;
				if (!cadena.equals("")){
					cadena+=";";
				}
			}
			
			
//			int asterico=is_subAnomaliaInterna.lastIndexOf("*");
//			if (asterico<0){
//				asterico=is_subAnomaliaInterna.length();
//			}
			
			is_subAnomaliaInterna= is_subAnomaliaInterna.substring(0, is_subAnomaliaInterna.lastIndexOf("*")+1) + cadena;
			//is_subAnomaliaInterna= is_subAnomaliaInterna.substring(0,asterico) + cadena;
			
			
			is_subAnomalia=eliminaCaracter(is_subAnomaliaInterna, "*");
			//is_subAnomalia= is_subAnomalia.substring(0, is_subAnomalia.lastIndexOf("*")+1) + cadena;
			

		}
		else{
			//Borramos la subAnomalia, si es que hay 
			if (subAnomalias.size()>0)
				subAnomalias.removeElementAt(0);
			is_anomalia="";
			is_subAnomalia="";
			is_subAnomaliaInterna="";
			anomalias.remove(index);
		}
		
		borrarComentarioAnomalia(codigoAnomalia);
		globales.tdlg.DeshacerModificacionesDeAnomalia(codigoAnomalia);
		globales.tdlg.cambiosAlBorrarAnomalia(codigoAnomalia);
		return true;
	
		
	}
	
	public void setCliente(String nombre){
		is_cliente= nombre;
	}
	
	public String getAnomaliasCapturadas(){
		String cadena="";
		
		for (Anomalia anomalia:anomalias){
			
			if (globales.convertirAnomalias)
				cadena += anomalia.is_conv ;
			else{
				cadena=anomalia.is_anomalia; 
			}
		}
		return cadena;
	}
	
	public boolean containsSubAnomalia(String ls_subAnom){
		
		for (Anomalia anomalia:subAnomalias){
			
			if (anomalia.is_desc.startsWith(ls_subAnom))
				return true;
		}
		return false;
	}
	
	public String getCliente(){
		return is_cliente.trim();
	}
	
	public static int toInteger(String valor){
		int li_valor=0;
		
		try{
			li_valor=Integer.parseInt(valor.trim());
		}
		catch(Throwable e){
			
		}
		
		return li_valor;
		
	}
	
	public static long toLong(String valor){
		long li_valor=0;
		
		try{
			li_valor=Long.parseLong(valor.trim());
		}
		catch(Throwable e){
			
		}
		
		return li_valor;
		
	}
	
	public void borrarComentarioAnomalia(String ls_anomalia){
		if (globales.multiplesAnomalias){
			String [] ls_comentarios=is_comentarios.split(";");
			
			//Obtenemos el label que tendra al principio
			String ls_prefijo=globales.tdlg.getPrefijoComentario(ls_anomalia);
			
			if (ls_prefijo.equals("")){
				return;
			}
			
			is_comentarios="";
			for (String ls_comentario:ls_comentarios){
				if (!ls_comentario.startsWith(ls_prefijo))
					setComentarios(ls_comentario);
			}
			
		}else{
			is_comentarios="";
		}
	}
	
	static String eliminaCaracter(String ls_cadena, String ls_caracter){
		
		while (ls_cadena.indexOf(ls_caracter)!=-1){
			ls_cadena=ls_cadena.substring(0, ls_cadena.indexOf(ls_caracter)) + ls_cadena.substring(ls_cadena.indexOf(ls_caracter)+1);
		}
		
		return ls_cadena;
		
	}
	
	public String getComentarioAnomalia(String ls_anomalia){
		if (globales.multiplesAnomalias){
			String [] ls_comentarios=is_comentarios.split(";");
			
			//Obtenemos el label que tendra al principio
			String ls_prefijo=globales.tdlg.getPrefijoComentario(ls_anomalia);

			for (String ls_comentario:ls_comentarios){
				if (ls_comentario.startsWith(ls_prefijo))
					return ls_comentario;
			}
			
		}else{
			return is_comentarios;
		}
		
		return "";
	}
	
	/**
	 * Borra lo que haya en anomalias y lo remplaza con el nuevo arreglo
	 * @param ls_anomalia
	 */
	public void reiniciaAnomalias(String ls_anomalia) {
		is_anomalia="";
		setAnomalia(ls_anomalia);
	}
	
	public void reiniciaSubAnomalias(String ls_anomalia) {
		is_subAnomalia="";
		setSubAnomalia(ls_anomalia);
	}
	
	//HCG 02/10/2014 Esta opcion generaba muchos problemas, cambiaba erroneamente el secuencial y generaba un problema de mostrar ordenes
	//Que no tenian relacion con la que se estaba trabajando
	
//	/**
//	 * Cuando hacemos un update en las ordenes, cambia la secuencia real y es visible para el usuario y pudiera causar problemas graves.
//	 * Asi que, esta funcion ayuda a reparar el secuencial ya creado en la orden
//	 */
//	public void corregirSecuenciaReal(){
//		openDatabase();
//		Cursor c=null;
////		try{
//			c=db.rawQuery("Select secuenciaReal from ruta where numOrden='" +is_numOrden+"'", null);
//			
//			c.moveToFirst();
//			
//			secuenciaReal=c.getInt(c.getColumnIndex("secuenciaReal"));
////		}
////		catch(Throwable e){
////			
////		}
////		finally{
//			if (c!=null){
//				c.close();
//			}
////		}
//		
//		
//		
//		closeDatabase();
//	}
	public String getSerieMedidor() {
	return is_serieMedidor;
}

}
