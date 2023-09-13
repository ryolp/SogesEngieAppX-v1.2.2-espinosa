package enruta.soges_engie;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import enruta.soges_engie.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public abstract class TomaDeLecturasPadre extends Activity {
	final static public int CORRECTA = 0;
	final static public int FUERA_DE_RANGO = -1;
	final static public int LECTURA_VACIA = -2;
	final static public int PRESION_VACIA = -3;
	final static public int VERIFIQUE = -4;

	static final int SWIPE_MIN_DISTANCE = 50;
	static final int SWIPE_MAX_OFF_PATH = 250;
	static final int SWIPE_THRESHOLD_VELOCITY = 800;

	final static int TOMADELECTURAS = 1;
	final static int COMENTARIOS = 2;
	final static int INPUT_CAMPOS_GENERICO=3;
	

	final static int ENVIADA = 0;
	final static int NO_ENVIADA = 1;

	final static int NO_SOSPECHOSA = 0;
	final static int SOSPECHOSA = 1;

	final static int FOTOS = 3;
	final static int BUSCAR_MEDIDOR = 4;
	final static int NO_REGISTADOS = 5;
	final static int REQUEST_ENABLE_BT = 6;
	final static int TRANSMISION=7;
	final static int RECEPCION=8;

	final static int LECTURA = 0;
	final static int PRESION = 1;
	final static int ANOMALIA = 2;

	final static int ASC = 0;
	final static int DESC = 1;

	final static int NINGUNO = 2;
	final static int INFO_CLIENTE = 0;
	final static int DETALLE = 1;
	
	String ultimaAnomaliaSeleccionada="";
	String ultimaSubAnomaliaSeleccionada="";
	Handler handle;
	
	/**
	 * Tiempo en segundo que se tardará el GPS
	 */
	final static int TIME_OUT_GPS = 10; 
	
	/**
	 * Tiempo limite ente puntos GPS
	 */
	private static final int TIME_LIMIT = 1000 * 30;

	DBHelper dbHelper;
	SQLiteDatabase db;
	//Globales globales;
	
	int NINGUNA=-1;
	int regreseDe=NINGUNA;
	
	String is_mensaje_direccion= "";
	
	LocationManager locationManager;
	LocationListener locationListener;
	
	int modo = Input.NORMAL;
	
	boolean voyATomarFoto=false;
	

	// respaldo de la lectura del recibo
	Lectura ilec_lectura;

	CImpresora impresoraZebra;
	int nPosicionImpresion = 10;
	String strMACImpresora = "";
	boolean hayMACImpr = false;
	boolean bHabilitarImpresion = false, bImprimirEncabezado = true,
			bEsElFInal = false;
	
	boolean timeOutAlcanzado=false;
	boolean seguirConLaCapturaSinPunto=false;
	
	Handler mHandler;
	
	Globales globales;
	MensajeEspecial me;
	boolean  preguntaSiBorraDatos=false;
	boolean  preguntaSiBorrarEnAnomaliaAusentes=true;
	int contadorControlCalidadFotos=0;
	
	@Override
	protected void onPause() {
		stopListeningGPS();
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		//Ahora si abrimos
		if (globales.tdlg==null){
			super.onResume();
			Intent i = getBaseContext().getPackageManager()
		             .getLaunchIntentForPackage( getBaseContext().getPackageName() );
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			System.exit(0);
			return;
		}
		startListeningGPS();
		super.onResume();
	}

	
	@Override
	protected void onStop(){
		stopListeningGPS();
		super.onStop();
	}
	
	@Override
	protected void onDestroy(){
		stopListeningGPS();
		super.onDestroy();
	}
	
	protected void permiteCerrar() {
		if (globales.tdlg==null)
			return;
	 
		if (globales.bModificar)
			globales.bcerrar = false;
		else {
			openDatabase();
			Cursor c = db.query("Ruta", null,globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.AUSENTES),
					null, null, null, null, "1");
			if (c.getCount() > 0)
				globales.bcerrar = false;
			else
				globales.bcerrar = true;
			closeDatabase();
		}
	}

	protected void openDatabase() {
		//dbHelper = new DBHelper(this);
		
		dbHelper=DBHelper.getInstance(this);

		db = dbHelper.getReadableDatabase();
	}

	protected void closeDatabase() {
		db.close();
		//dbHelper.close();
	}

	protected void getUltLect() {
		/*
		 * Cursor c; String ls_select; //String[]
		 * ls_selectionArgs={String.valueOf(il_lect_act)}; String
		 * ls_filtrado=formaCadenaFiltrado(); openDatabase();
		 * 
		 * if (globales.bModificar){
		 * 
		 * String ls_comentarios="";
		 * 
		 * if (filtrarComentarios) ls_comentarios=
		 * " (lectact<>'' or (comentarios<>'' and comentarios is not null)) ";
		 * else ls_comentarios=" lectact<>'' ";
		 * 
		 * c= db.query("lecturas", null, ls_comentarios +ls_filtrado, null,
		 * null, null, "cast (secuencial as integer) desc", "1"); } else{
		 * /*ls_select="Select * from lecturas " +
		 * "where secuencial<"+il_lect_act+ " and lectact=''";
		 */
		// c= db.query("lecturas", null, "secuencial<? and lectact=''",
		// ls_selectionArgs, null, null, null, "1");
		/*
		 * c= db.query("lecturas", null, "lectact=''"+ls_filtrado, null, null,
		 * null, "cast (secuencial as integer) desc", "1"); }
		 * 
		 * //c= db.rawQuery(ls_select, null);
		 * 
		 * c.moveToFirst(); closeDatabase();
		 */
		
		if(preguntaSiBorraDatos /*&& !globales.bModificar*/){
			if (!globales.tll.getLecturaActual().getAnomaliasCapturadas().equals("") &&
					!globales.modoCaptura && !globales.tdlg.esSegundaVisita(globales.tll.getLecturaActual().getAnomaliasCapturadas(), globales.tll.getLecturaActual().is_subAnomalia)){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				AlertDialog alert;
				
				//Si ingresé datos, estoy en modo captura y  me estoy moviendo deberia preguntar
				builder.setMessage(R.string.str_pregunta_guardar_cambios)
			       .setCancelable(false).setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			        	   //recepcion();
			        	   preguntaSiBorraDatos=false;
			        	   getUltLect() ;
			                dialog.dismiss();
			           }
			       })
			       .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			        	   dialog.cancel();
			                
			           }
			       });
				
				alert = builder.create();
				alert.show();
				return;
			}
		}

		try {
			globales.tll.ultimoMedidorACapturar(globales.bModificar,
					globales.bcerrar);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setDatos();

	}

	protected void getPrimLect() {
		/*
		 * Cursor c; String ls_filtrado=formaCadenaFiltrado();
		 * 
		 * openDatabase();
		 * 
		 * if (globales.bModificar){
		 * 
		 * String ls_comentarios="";
		 * 
		 * if (filtrarComentarios) ls_comentarios=
		 * " (lectact<>'' or (comentarios<>'' and comentarios is not null)) ";
		 * else ls_comentarios=" lectact<>'' ";
		 * 
		 * 
		 * c= db.query("lecturas", null, ls_comentarios +ls_filtrado, null,
		 * null, null, null, "1"); } else{ c= db.query("lecturas", null,
		 * "lectact=''" +ls_filtrado , null, null, null, null, "1"); }
		 * 
		 * c.moveToFirst();
		 * 
		 * 
		 * closeDatabase();
		 */
		if(preguntaSiBorraDatos /*&& !globales.bModificar*/){
			if (!globales.tll.getLecturaActual().getAnomaliasCapturadas().equals("") &&
					!globales.modoCaptura && !globales.tdlg.esSegundaVisita(globales.tll.getLecturaActual().getAnomaliasCapturadas(), globales.tll.getLecturaActual().is_subAnomalia)){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				AlertDialog alert;
				
				//Si ingresé datos, estoy en modo captura y  me estoy moviendo deberia preguntar
				builder.setMessage(R.string.str_pregunta_guardar_cambios)
			       .setCancelable(false).setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			        	   //recepcion();
			        	   preguntaSiBorraDatos=false;
			        	   getPrimLect() ;
			                dialog.dismiss();
			           }
			       })
			       .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			        	   dialog.cancel();
			                
			           }
			       });
				
				alert = builder.create();
				alert.show();
				return;
			}
		}

		try {
			globales.tll.primerMedidorACapturar(globales.bModificar,
					globales.bcerrar);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setDatos();

	}

	protected void getSigLect() {
		if(preguntaSiBorraDatos /* && !globales.bModificar*/){
			if (!globales.tll.getLecturaActual().getAnomaliasCapturadas().equals("") &&
					!globales.modoCaptura && !globales.tdlg.esSegundaVisita(globales.tll.getLecturaActual().getAnomaliasCapturadas(), globales.tll.getLecturaActual().is_subAnomalia)){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				AlertDialog alert;
				
				//Si ingresé datos, estoy en modo captura y  me estoy moviendo deberia preguntar
				builder.setMessage(R.string.str_pregunta_guardar_cambios)
			       .setCancelable(false).setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			        	   //recepcion();
			        	   preguntaSiBorraDatos=false;
			        	   getSigLect();
			                dialog.dismiss();
			           }
			       })
			       .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			        	   dialog.cancel();
			                
			           }
			       });
				
				alert = builder.create();
				alert.show();
				return;
			}
		}

		// Por peticion de Irene, Si es CF y doy siguiente debo guardar la
		// lectura como 0
		if (globales.is_caseta.contains("CF") && globales.is_lectura.equals("")
				&& globales.is_presion.equals("") && !globales.bModificar) {
			globales.tll.guardarLectura("0");
			// permiteCerrar();
		}

		try {
			// Voy a guardar la variable de modificar ambas variables son
			// identicas quiere decir que se encuentra en el mismo modo en el
			// cual busqué
			if (globales.moverPosicion
					&& globales.bEstabaModificando == globales.bModificar) {
				// Movemos a donde estaba
				globales.tll.regresarDondeEstaba();
			} else {
				permiteCerrar();
				globales.tll.siguienteMedidorACapturar(globales.bModificar,
						globales.bcerrar);
			}

		} catch (Throwable e) {
			mensajeOK(e.getMessage());
		}
		globales.moverPosicion = false;
		globales.bEstabaModificando = false;
//		if (globales.bModificar)
//			is_mensaje_direccion= getString(R.string.msj_tdl_no_mas_lecturas_ingr_despues);
//		else
//			is_mensaje_direccion= getString(R.string.msj_tdl_no_mas_lecturas_despues);
		setDatos();
		// globales.permiteDarVuelta=false;

	}
	
	 protected void mensajeOK(String ls_mensaje, String titulo){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(ls_mensaje).setTitle(titulo)
			       .setCancelable(false)
			       .setNegativeButton(R.string.aceptar, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			                dialog.cancel();
			           }
			       });
			
			AlertDialog alert = builder.create();
			alert.show();
			
			
		}
	 
	 private void mensajeOK(String ls_mensaje){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(ls_mensaje)
			       .setCancelable(false)
			       .setNegativeButton(R.string.aceptar, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			                dialog.cancel();
			           }
			       });
			
			AlertDialog alert = builder.create();
			alert.show();
			
			
		}
	 
	 

	protected void getAntLect() {
		
		if(preguntaSiBorraDatos /* && !globales.bModificar*/){
			if (!globales.tll.getLecturaActual().getAnomaliasCapturadas().equals("") &&
					!globales.modoCaptura && !globales.tdlg.esSegundaVisita(globales.tll.getLecturaActual().getAnomaliasCapturadas(), globales.tll.getLecturaActual().is_subAnomalia)){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				AlertDialog alert;
				
				//Si ingresé datos, estoy en modo captura y  me estoy moviendo deberia preguntar
				builder.setMessage(R.string.str_pregunta_guardar_cambios)
			       .setCancelable(false).setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			        	   //recepcion();
			        	   preguntaSiBorraDatos=false;
			        	   getAntLect() ;
			                dialog.dismiss();
			           }
			       })
			       .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			        	   dialog.cancel();
			                
			           }
			       });
				
				alert = builder.create();
				alert.show();
				return;
			}
		}
		
		try {
			// Voy a guardar la variable de modificar ambas variables son
			// identicas quiere decir que se encuentra en el mismo modo en el
			// cual busqué
			if (globales.moverPosicion
					&& globales.bEstabaModificando == globales.bModificar) {
				// Movemos a donde estaba
				globales.tll.regresarDondeEstaba();
			} else
				globales.tll.anteriorMedidorACapturar(globales.bModificar,
						globales.bcerrar);
		} catch (Throwable e) {

		}
		globales.moverPosicion = false;
		globales.bEstabaModificando = false;
		
		
		
//		if (globales.bModificar)
//			is_mensaje_direccion= getString(R.string.msj_tdl_no_mas_lecturas_ingr_antes);
//		else
//			is_mensaje_direccion= getString(R.string.msj_tdl_no_mas_lecturas_antes);
		
		setDatos();

	}

	protected void ImprimirRecibo(String strR1, String strR2, String strR3,
			String strR4, String strR5, String strR6, String strR7,
			String strR8, String strR9, int nTipoImpresion) {
		if (impresoraZebra == null)
			// impresoraZebra = new CImpresora(this, strMACImpresora);
			impresoraZebra = new CImpresora(strMACImpresora);
		ImprimirReciboIndividual(strR1, strR2, strR3, strR4, strR5, strR6,
				strR7, strR8, strR9, nTipoImpresion);
		// ImprimirReciboIndividualEnLOG(strR1, strR2, strR3, strR4, strR5,
		// strR6, strR7, strR8, strR9, nTipoImpresion);
	}

	protected void ImprimirReciboIndividual(String strR1, String strR2,
			String strR3, String strR4, String strR5, String strR6,
			String strR7, String strR8, String strR9, int nTipoImpresion) {
		int valor;
		nPosicionImpresion = 10;

		try {
			if (nTipoImpresion == 1) {
				impresoraZebra.Imprimir("---------------------------\n\r");
				// impresoraZebra.Imprimir("         DISNORTE          \n\r");
				impresoraZebra.Imprimir(strR9 + "\n\r");
				impresoraZebra.Imprimir("   COMPROBANTE DE LECTURA  \n\r");
				impresoraZebra.Imprimir("---------------------------\n\r");

				impresoraZebra.Imprimir(strR1.substring(0, 10) + " "
						+ strR1.substring(10, 12) + ":"
						+ strR1.substring(12, 14) + ":"
						+ strR1.substring(14, 16) + "\n\r");
				impresoraZebra.Imprimir("Nis: "
						+ QuitarCerosDelInicio(strR2.trim()) + "\n\r");
				impresoraZebra.Imprimir("Cliente:\n\r");
				impresoraZebra.Imprimir(strR3 + "\n\r");
				impresoraZebra.Imprimir("No. Medidor: " + strR4 + "\n\r");
			}
			if (nTipoImpresion == 2) {
				impresoraZebra.Imprimir("Lectura Anterior:\n\r");
				impresoraZebra.Imprimir(strR8
						+ QuitarCerosDelInicio(strR5.trim()) + "\n\r");
				impresoraZebra.Imprimir("Lectura Actual:\n\r");
				impresoraZebra.Imprimir(strR8
						+ QuitarCerosDelInicio(strR6.trim()) + "\n\r");
			}
			if (nTipoImpresion == 3) {
				impresoraZebra.Imprimir("Lector: " + strR7 + "\n\r");
				impresoraZebra.Imprimir("---------------------------\n\r");
			}
			// impresoraZebra.Imprimir("       PARA CONSULTA       \n\r");
			// impresoraZebra.Imprimir("       LLAMAR AL 125       \n\r");
			// impresoraZebra.Imprimir("---------------------------\n\r");

		} catch (Throwable e) {
			valor = 0;
		}
	}

	protected void mandarAImprimir(Lectura llec_lectura) {

		// Si es ausente no debe imprimir
		if (llec_lectura.getLectura().equals(""))
			return;

		if (bHabilitarImpresion) {
			Intent lrs;

			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			if (mBluetoothAdapter != null) {
				if (!mBluetoothAdapter.isEnabled()) {
					lrs = new Intent(this, trasmisionDatosBt.class);
					Intent enableBtIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
					// Guardamos un respaldo de la lectura a realizar el recibo
					// ya que no se imprimió
					ilec_lectura = llec_lectura;
					return;
				}
			} else {
				Toast.makeText(this, "Bluetooth no disponible.",
						Toast.LENGTH_SHORT);
				return;
			}

			Toast mensaje = Toast.makeText(this, R.string.msj_imprimir,
					Toast.LENGTH_LONG);
			mensaje.setGravity(Gravity.TOP, 0, 0);
			mensaje.show();
			String strR1 = /*
							 * new String(medidor, midlet.POS_DATOS_FECHA_HORA,
							 * midlet.LONG_CAMPO_FECHA_HORA);
							 */llec_lectura.getFechaHora();
			String strR2 = String.valueOf(llec_lectura.nis_rad);
			String strR3 = llec_lectura.getNombreCliente();
			String strR4 = llec_lectura.is_serieMedidor;
			String strR7 = /* midlet.csNombreLecturista; */globales.is_nombre_Lect;
			String strR8 = llec_lectura.is_tarifa;

			String strR9 = getUnicomDelEncabezado().substring(0, 1);
			if (strR9.equals("2"))
				strR9 = "         DISNORTE          ";
			else
				strR9 = "          DISSUR           ";

			if (strR8.equals("CO011"))
				strR8 = "Lect kWh:       ";
			else if (strR8.equals("CO012"))
				strR8 = "Lect kWh:       ";
			else if (strR8.equals("CO013"))
				strR8 = "Lect kWh:       ";
			else if (strR8.equals("CO014"))
				strR8 = "Lect kWh:       ";
			else if (strR8.equals("CO015"))
				strR8 = "Lect kVar:      ";
			else if (strR8.equals("CO016"))
				strR8 = "Lect kW:        ";
			else if (strR8.equals("CO107"))
				strR8 = "Lect kW:        ";
			else if (strR8.equals("CO106"))
				strR8 = "LectEP kW:      ";
			else if (strR8.equals("CO116"))
				strR8 = "LectEP kW:      ";
			else if (strR8.equals("CO117"))
				strR8 = "Lect kW:        ";
			else if (strR8.equals("CO001"))
				strR8 = "Lect kWh:       ";
			else if (strR8.equals("CO002"))
				strR8 = "Lect kWh:       ";
			else if (strR8.equals("CO003"))
				strR8 = "Lect kWh:       ";
			else if (strR8.equals("CO004"))
				strR8 = "Lect kWh:       ";
			else if (strR8.equals("CO005"))
				strR8 = "Lect kVar:      ";
			else if (strR8.equals("CO006"))
				strR8 = "Lect kW:        ";
			else if (strR8.equals("CO007"))
				strR8 = "Demanda kW BT TotaliKW:";
			else if (strR8.equals("CO207"))
				strR8 = "Potencia Valle TotalKW:";
			else if (strR8.equals("CO018"))
				strR8 = "Control:        ";
			else if (strR8.equals("CO017"))
				strR8 = "Demanda kW:     ";
			else if (strR8.equals("CO217"))
				strR8 = "Potencia Valle: ";
			else if (strR8.equals("CO940"))
				strR8 = "Amortiz. de Cuotas (KW:";
			else if (strR8.equals("CO941"))
				strR8 = "Interes de cuotas (AKW:";
			else if (strR8.equals("CO216"))
				strR8 = "LectFP kW:      ";
			else if (strR8.equals("CO206"))
				strR8 = "LectFP kW:      ";

			String strR5 = String.valueOf(llec_lectura.consBimAnt);
			String strR6 = llec_lectura.getLectura();

			if (bImprimirEncabezado) {
				bImprimirEncabezado = false;
				ImprimirRecibo(strR1, strR2, strR3, strR4, strR5, strR6, strR7,
						strR8, strR9, 1);
			}
			ImprimirRecibo(strR1, strR2, strR3, strR4, strR5, strR6, strR7,
					strR8, strR9, 2);

			// int totalDeRegistrosImp = 0;
			// try{
			// //HCG 08/06/2012, Se realizaron cambios para que funcione con
			// varios RecordStores
			// totalDeRegistrosImp = midlet.cuantosMedidoresEnRecordset();
			// }catch(Throwable e){
			// midlet.log.log("Error al ver total de Registros en ruta "+e);
			// }

			if ((!globales.idMedidorUltimaLectura.equals(globales.is_caseta))
					|| bEsElFInal /* || (totalDeRegistrosImp == idMedidor) */) {
				bImprimirEncabezado = true;
				ImprimirRecibo(strR1, strR2, strR3, strR4, strR5, strR6, strR7,
						strR8, strR9, 3);
				bEsElFInal = false;
			}
		}
	}

	String getUnicomDelEncabezado() {
		openDatabase();
		Cursor c;
		c = db.rawQuery("Select registro from encabezado", null);

		c.moveToFirst();
		byte[] bytesAEnviar = c.getBlob(c.getColumnIndex("registro"));
		closeDatabase();
		return new String(bytesAEnviar, 400, 12);
	}

	public void getImpresora() {
		openDatabase();

		// Tomamos el servidor desde la pantalla de configuracion
		Cursor c = db.rawQuery(
				"Select value from config where key='mac_impresora'", null);
		c.moveToFirst();

		if (!validaCampoDeConfig(
				c,
				String.format(getString(R.string.msj_config_no_disponible),getString(R.string.info_macBluetooth) , getString(R.string.str_configuracion),getString(R.string.info_macBluetooth))))
			return;

		strMACImpresora = c.getString(c.getColumnIndex("value"));

		c.close();
		hayMACImpr = true;

		closeDatabase();
	}

	public boolean validaCampoDeConfig(Cursor c, String ls_mensaje) {
		String ls_valor;
		if (c.getCount() == 0) {

			return false;

		} else {
			ls_valor = c.getString(c.getColumnIndex("value"));
			if (ls_valor.equals("")) {
				Toast.makeText(this, ls_mensaje, Toast.LENGTH_LONG).show();
				return false;
			}

		}

		return true;
	}

	public String QuitarCerosDelInicio(String strTextoConCeros) {
		boolean bSalir = false;
		String csLetraInicial = "";
		while (!bSalir) {
			csLetraInicial = strTextoConCeros.substring(0, 1);
			if (csLetraInicial.equals("0")) {
				strTextoConCeros = strTextoConCeros.substring(1);
			} else
				bSalir = true;
			if (strTextoConCeros.equals(""))
				bSalir = true;
		}
		return strTextoConCeros.trim();
	}

	protected void irALaPrimeraSinEjecutarAlTerminar() {
		if (globales.permiteDarVuelta) {

			switch (globales.ii_orden) {
			case ASC:
				getPrimLect();
				break;
			case DESC:
				getUltLect();
				break;
			}

		}

	}

	protected abstract void setDatos();

	protected void asignaAnomaliaConsecutiva(String is_medidor,
			Anomalia anomalia) {
		// TODO Auto-generated method stub

		if (anomalia == null) {
			return;
		}

		if (anomalia.ii_ausente != 4) {
			return;
		}

		// Vector <Lectura>
		// lecturas=globales.tll.getMedidoresIgualesConFoto(is_medidor);
		Vector<Lectura> lecturas = globales.tll.getMedidoresIguales(is_medidor);

		for (Lectura lectura : lecturas) {

			if (globales.il_lect_act != lectura.secuenciaReal) {
				lectura.setAnomalia("777");
				lectura.setLectura("0");
				lectura.sospechosa = String.valueOf(0);
				lectura.intentos = String.valueOf(0);
				lectura.ordenDeLectura = String.valueOf(globales.tll
						.getSiguienteOrdenDeLectura());

				lectura.guardar(globales.tll.getSiguienteOrdenDeLectura());

			}

		}
	}

	protected void tomaFotosConsecutivas(String is_medidor) {
		// TODO Auto-generated method stub

		// Vector <Lectura>
		// lecturas=globales.tll.getMedidoresIgualesConFoto(is_medidor);
		if (!globales.estoyTomandoFotosConsecutivas) {
			globales.lecturasConFotosPendientes = globales.tll
					.getMedidoresIgualesConFoto(is_medidor);
			globales.ii_foto_cons_act = 0;
			globales.estoyTomandoFotosConsecutivas = true;
		}

		if (globales.lecturasConFotosPendientes.size() > 0) {
			// for (Lectura lectura:lecturasConFotosPendientes ){
			globales.is_terminacion = globales.lecturasConFotosPendientes
					.get(globales.ii_foto_cons_act).terminacion;

			if (globales.is_terminacion == null) {
				globales.is_terminacion = "-1";
			}
			tomarFoto(0,
					globales.lecturasConFotosPendientes
							.get(globales.ii_foto_cons_act), 1);
			globales.lecturasConFotosPendientes.get(globales.ii_foto_cons_act)
					.establecerFotoAlFinal(Lectura.SIN_FOTO_AL_FINAL);
			globales.is_terminacion = "-1";

			globales.ii_foto_cons_act++;
			if (globales.ii_foto_cons_act == globales.lecturasConFotosPendientes
					.size()) {
				globales.estoyTomandoFotosConsecutivas = false;
				if (globales.tll.getLecturaActual() != null)
					globales.is_terminacion = globales.tll.getLecturaActual().terminacion;
				if (globales.bcerrar) {
					muere();
				}
			}
			// if (!ls_anomalia.equals("") && )
			// }
		} else {
			if (!globales.tll.hayMasLecturas()) {
				muere();
			}

		}

	}

	abstract void muere();

	/**
	 * Version sin anomalia de tomar foto
	 * @param temporal Indica quien tomo la foto, cambia a 0 cuando ya no es de nadie y se puede borrar
	 * @param cantidad Cantidad de fotos a tomar
	 */
	public void tomarFoto(int temporal, int cantidad ) {
		tomarFoto( temporal, cantidad, "" ) ;
	}
	
	/**
	 * Version con anomalia de tomar foto
	 * @param temporal Indica quien tomo la foto, cambia a 0 cuando ya no es de nadie y se puede borrar
	 * @param cantidad Cantidad de fotos a tomar
	 * @param ls_anomalia anomalia que se acaba de insertar
	 */
	public void tomarFoto(int temporal, int cantidad, String ls_anomalia ) {

		if (globales.sonLecturasConsecutivas) {
			globales.tll.getLecturaActual().establecerFotoAlFinal(
					Lectura.FOTO_AL_FINAL);

		} else {
			tomarFoto(temporal, globales.tll.getLecturaActual(), cantidad, ls_anomalia);
		}

	}
	
	/**
	 * Version sin anomalia y el objeto lectura de tomar foto
	  * @param temporal Indica quien tomo la foto, cambia a 0 cuando ya no es de nadie y se puede borrar
	 * @param lectura Objeto de lectura 
	 * @param cantidad Cantidad de fotos a tomar
	 */
	public void tomarFoto(int temporal, Lectura lectura, int cantidad) {
		tomarFoto( temporal,  lectura,  cantidad, "");
	}

	/**
	 * Version con anomalia y el objeto lectura de tomar foto
	  * @param temporal Indica quien tomo la foto, cambia a 0 cuando ya no es de nadie y se puede borrar
	 * @param lectura Objeto de lectura 
	 * @param cantidad Cantidad de fotos a tomar
	 * @param ls_anomalia anomalia que se acaba de insertar
	 */
	public void tomarFoto(final int temporal, final Lectura lectura, final int cantidad, final String ls_anomalia) {

		voyATomarFoto=false;
		if (modo==Input.SIN_FOTOS) {
			 legacyCapturaEnModosSinFoto();
			 return;
		}
		 
		if (!(globales.tll.getLecturaActual().is_supervisionLectura.equals("1") || globales.tll.getLecturaActual().is_reclamacionLectura.equals("1"))
				 && modo==Input.FOTOS_CC ){
			 legacyCapturaEnModosSinFoto();
			// voyATomarFoto=true;
			 return;
		}
		 
		//Continuar con la toma
		if (!globales.tdlg.continuarConLaFoto() && modo!=Input.FOTOS ){
			return;
		}
		
		final Intent camara = new Intent(this, CamaraActivity.class);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setMessage(regreseDe!=ANOMALIA?(globales.is_terminacion.endsWith("1") ? "Obtención de Foto antes de ejecutar la acción": "Obtención de Foto una vez ejecutada la acción"):"Preparese para tomar la foto")
		builder.setMessage("Preparese para tomar la foto")
		.setTitle("Cámara")
		       .setCancelable(false)
		       .setNegativeButton(R.string.aceptar, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id){
		        	   
		        	   
		       		camara.putExtra("secuencial", lectura.secuenciaReal);
		       		camara.putExtra("caseta", lectura.is_serieMedidor);
		       		camara.putExtra("terminacion", globales.is_terminacion);
		       		camara.putExtra("temporal", temporal);
		       		camara.putExtra("cantidad", cantidad);
		       		camara.putExtra("anomalia", ls_anomalia);
		       		// vengoDeFotos = true;
		       		startActivityForResult(camara, FOTOS);
		                dialog.cancel();
		           }
		       });
		
		AlertDialog alert = builder.create();
		alert.show();
		voyATomarFoto=true;

	}
	
	@SuppressLint("NewApi")
	public void startListeningGPS(){
		if (!(globales.requiereGPS && globales.GPS))
			return;
		
		
		stopListeningGPS();
		handle= new Handler();
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

		
		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
		      makeUseOfNewLocation(location);
		    	
		    }

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}

		  };

		// Register the listener with the Location Manager to receive location updates
		//Establecemos que cada 10 segundos y 2 metros de diferencia quiero las actualizaciones
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 2, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER/*provider*/, 10000, 2, locationListener);
		globales.gpsEncendido=true;
//		if (Build.VERSION.SDK_INT >= 11)
//			invalidateOptionsMenu();
	}

	protected void makeUseOfNewLocation(Location location) {
		// TODO Auto-generated method stub
		
		if (isBetterLocation(  location, globales.location) ){
			//Toast.makeText(this,location.getLatitude() + ", " + location.getLongitude() + ":" + location.getAccuracy() , Toast.LENGTH_LONG).show();
			globales.location=location;
			
//			//Enviamos la locacion
//			Thread enviarLocalizacion= new Thread(){
//				public void run(){
//					Serializacion serial= new Serializacion(Serializacion.WIFI);
//					try {
//						serial.open(globales.defaultServidorGPRS, "", "hola", Serializacion.ESCRITURA, 0, 0);
//						serial.mandarSQL("Insert into rutaGPS(latitud, longitud, PTN, fecha) values ('"
//						+globales.location.getLatitude()+"', '"+globales.location.getLongitude()+"', '"+globales.getUsuario()
//						+"',str_to_date('"+Main.obtieneFecha("ymdhis")+"', '%Y%m%d%H%i%s'))");
//						serial.close();
//					} catch (final Throwable e) {
//						// TODO Auto-generated catch block
//						handle.post(new Thread(){
//							public void run(){
//								Toast.makeText(TomaDeLecturasPadre.this, "Error:"+e.getMessage(), Toast.LENGTH_LONG).show();
//								e.printStackTrace();
//							}
//						});
//						
//					}
//					
//				}
//			};
//			
//			enviarLocalizacion.start();
		}
		
	}
	
	

	/** Determina si un Punto GPS (LOCATION) es mejor que el que que se se tiene como mejor
	  * @param location  Nuevo punto GPS a evaluar
	  * @param currentBestLocation  El punto GPS actual que se tiene como mejor
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TIME_LIMIT;
	    boolean isSignificantlyOlder = timeDelta < -TIME_LIMIT;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

	
	@SuppressLint("NewApi")
	public void stopListeningGPS(){
		globales.gpsEncendido=false;
		if (!(globales.requiereGPS && globales.GPS))
			return;
		
		if (locationManager!=null)
			locationManager.removeUpdates(locationListener);
		//globales.location=null;
		
//		if (Build.VERSION.SDK_INT >= 11)
//			invalidateOptionsMenu();
	}
	
	void enciendeGPS(){
		if(globales.tll.getLecturaActual().requiereGPS && !globales.gpsEncendido)
		{
			
			startListeningGPS();
		}
		else if(!globales.tll.getLecturaActual().requiereGPS && globales.gpsEncendido){
			//globales.requiereGPS=false;
			stopListeningGPS();
		}
	}
	
	protected void esperarGPS() {
		// TODO Auto-generated method stub
		//Empezamos mostrnado el cuadro de dialogo
		
		Timer timeoutTimer = new Timer();
		
		
		LayoutInflater inflater = this.getLayoutInflater();
		
		
    	
    	final View view=inflater.inflate(R.layout.wait_messagebox, null);
    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
		
		builder.setView(view);
		
//		final EditText et_archivocarga= (EditText) view.findViewById(R.id.et_archivocarga);
		
		
		
		builder
	       .setCancelable(false);
		
		final AlertDialog alert = builder.create();
		alert.show();
		
		//Ahora ya tienen que esperar al cuadro de dialogo... para esto ocupamos dos cosas
		//Un timer que indique que el TIME OUT se ha alcanzado
		//Un while que verifque que la variable de localizacion no sea nula y que no se haya acabado el tiempo
		
		//Primero el timer
		try {
			timeoutTimer.cancel();
		} catch (Throwable e) {

		}

		timeoutTimer.purge();
		timeoutTimer = new Timer();
		timeoutTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				timeOutAlcanzado=true;
				
			}

		}, TIME_OUT_GPS * 1000);
		
		final TomaDeLecturasPadre tdlp= this;
		//Ahora el thread
		Thread thread = new Thread (){
			public void run(){
				while (globales.location==null && !timeOutAlcanzado){
					//Esperamos...
				}
				
				timeOutAlcanzado=false;
				alert.cancel();
				
				
				if (globales.location==null){
					//Avisamos con un mensaje que si quiere reintentar
					GPSNotFoundMsg(tdlp);
				}
				else{
					//Guardamos
					
					mHandler.post(new Runnable() {
			            public void run() {
			            	capturaDespuesDelPuntoGPS();	
			            }
					});
					
				}
				
			}
		};
		
		thread.start();
	}
	
	/**
	 * Esta funcion sirve para guardar el punto GPS despues que este estuviera vacio.
	 * Se conecta con la funcion que realiza el proceso de lecturas con sus hijos
	 */
	protected abstract void capturaDespuesDelPuntoGPS();
	
	void GPSNotFoundMsg(final TomaDeLecturasPadre tdlp){
		
		mHandler.post(new Runnable() {
	            public void run() {
	            	AlertDialog.Builder builder;

	        		// String ls_opciones[]={"Ninguno", "Info. del Cliente", "Detalle"};

	        		builder = new AlertDialog.Builder(tdlp);
	        		builder.setMessage(R.string.msj_gps_no_disponible)
	        		.setPositiveButton(R.string.reintentar, new DialogInterface.OnClickListener() {
	        							@Override
	        							public void onClick(DialogInterface dialog,
	        									int id) {
	        								esperarGPS();
	        							}
	        						})
	        				.setNegativeButton(R.string.cancelar,
	        						new DialogInterface.OnClickListener() {
	        							@Override
	        							public void onClick(DialogInterface dialog,
	        									int id) {
	        								seguirConLaCapturaSinPunto=true;
	        								capturaDespuesDelPuntoGPS();
	        								
	        							}
	        						});
	        		builder.show();
				}
			});
		
	}
	
	/** 
	 * Esta funcion determina si es necesaria la toma de GPS
	 * @return Regresa un valor booleano que indica que puede tomar el punto
	 */
	boolean deboTomarPuntoGPS(){
		return globales.location==null && !seguirConLaCapturaSinPunto && globales.tll.getLecturaActual().requiereGPS && globales.GPS;
	}
	
	protected void iniciarModoCorreccionCAPS() {
		// TODO Auto-generated method stub
		//setModoModificacion(false);
		
		if (!globales.capsModoCorreccion){
			mensajeOK(getString(R.string.msj_correccion_ultima), getString(R.string.msj_tdl_fin_de_ruta));
			//Toast.makeText(this, R.string.msj_correccion_ultima, Toast.LENGTH_LONG).show();
		}
		globales.bModificar=true;
		globales.capsModoCorreccion=true;
		//layout.setBackgroundResource(R.drawable.correccion_pattern);
		getPrimLect();
		
		
	}

	protected abstract void regresaDeMensaje(MensajeEspecial me, int respuesta);
	
	public void preguntaSiNo(final MensajeEspecial me){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(me.descripcion)
		.setPositiveButton(R.string.No,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int id) {
								globales.tdlg.RespuestaMensajeSeleccionada(me, MensajeEspecial.NO);
								regresaDeMensaje( me, MensajeEspecial.NO);
							}
						})
				.setNegativeButton(R.string.Si,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int id) {
								globales.tdlg.RespuestaMensajeSeleccionada(me, MensajeEspecial.SI);
								regresaDeMensaje( me, MensajeEspecial.SI);
								
							}
						}).setCancelable(me.cancelable);
		builder.show();
	}
	
	public void preguntaOpcionMultiple(final MensajeEspecial me){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(me.descripcion).setItems(me.getArregloDeRespuestas(), 
				new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int id) {
								globales.tdlg.RespuestaMensajeSeleccionada(me, id);
								 regresaDeMensaje( me, id);
							}
						}).setCancelable(me.cancelable);
		builder.show();
	}
	
	public void capturar() {
		
	}
	
	protected void avanzarDespuesDeAnomalia(){
		
	}
	
	public void legacyCapturaEnModosSinFoto(){
		if (regreseDe==ANOMALIA && globales.legacyCaptura){
			if (globales.tll.getLecturaActual().requiereLectura()==Anomalia.LECTURA_AUSENTE && !globales.tdlg.avanzarDespuesDeAnomalia(ultimaAnomaliaSeleccionada, ultimaSubAnomaliaSeleccionada, false)){
				voyATomarFoto=true;
				
				capturar();
				
			}
			else if (globales.tdlg.avanzarDespuesDeAnomalia(ultimaAnomaliaSeleccionada, ultimaSubAnomaliaSeleccionada, false)){
				voyATomarFoto=true;
				avanzarDespuesDeAnomalia();
				
			}
	 }
		
		else if (globales.legacyCaptura && regreseDe==LECTURA){
			voyATomarFoto=true;
			capturar();
		}
	}
}
