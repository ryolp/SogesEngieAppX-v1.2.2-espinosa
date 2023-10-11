package enruta.soges_engie;

import java.util.Timer;
import java.util.Vector;

import enruta.soges_engie.entities.SesionEntity;

import android.app.Application;
import android.location.Location;

public class Globales extends Application {
	final static int NICARAGUA=0;
	final static int COLOMBIA=1;
	final static int ELECTRICARIBE=2;
	final static int ARGENTINA=3;
	final static int BRASIL=4;
	final static int PANAMA=6;
	final static int ENGIE=7;
	final static int DEMO=8;


	final static int USUARIO=0;
	final static int CONTRASEÑA=1;
	final static int AMBAS=2;
	final static int CON_SMS = 4;
	
	final static int CLAVE_COMAPA_ZC=29089051;
	final static int CLAVE_ENRUTA=19891231;
	final static int CLAVE_PRUEBAS2=20141209;
	final static int CLAVE_PRUEBAS3=20141217;
	final static int CLAVE_MEXICANA=20150223;
	final static int CLAVE_PREPAGO=20150928;
	int ii_claveIngresada=0;
	boolean debug=false;
	
	
//	String usuarioBD="u1003479_hcasta";
//	String serverBD= "db1003479_reportes";
	String usuarioBD="u1003479_pruebas";
	String serverBD= "db1003479_prueba";
	String passwordBD= "Sotixe_69";



	int nEstadoDeLaRepercusion;
	final static int ENTRO_EFECTIVA_SIN_DATOS_DESCONEXION = 1;
	final static int ENTRO_EFECTIVA_SIN_DATOS_REMOCION = 2;
	final static int ENTRO_EFECTIVA_SIN_DATOS_RECONEXION = 3;
	final static int ENTRO_EFECTIVA_SIN_DATOS_REC_REMO = 4;
	final static int ENTRO_EFECTIVA_SIN_DATOS_OTRO = 5;
	final static int ENTRO_NO_EFECTIVA_SIN_DATOS_DESCONEXION = 6;
	final static int ENTRO_NO_EFECTIVA_SIN_DATOS_REMOCION = 7;
	final static int ENTRO_NO_EFECTIVA_SIN_DATOS_RECONEXION = 8;
	final static int ENTRO_NO_EFECTIVA_SIN_DATOS_REC_REMO = 9;
	final static int ENTRO_NO_EFECTIVA_SIN_DATOS_OTRO = 10;

	final static int ENTRO_EFECTIVA_CON_DATOS_DESCONEXION = 11;
	final static int ENTRO_EFECTIVA_CON_DATOS_REMOCION = 12;
	final static int ENTRO_EFECTIVA_CON_DATOS_RECONEXION = 13;
	final static int ENTRO_EFECTIVA_CON_DATOS_REC_REMO = 14;
	final static int ENTRO_EFECTIVA_CON_DATOS_OTRO = 15;
	final static int ENTRO_NO_EFECTIVA_CON_DATOS_DESCONEXION = 16;
	final static int ENTRO_NO_EFECTIVA_CON_DATOS_REMOCION = 17;
	final static int ENTRO_NO_EFECTIVA_CON_DATOS_RECONEXION = 18;
	final static int ENTRO_NO_EFECTIVA_CON_DATOS_REC_REMO = 19;
	final static int ENTRO_NO_EFECTIVA_CON_DATOS_OTRO = 20;

	final static int ENTRO_NO_EFECTIVA_SIN_DATOS_RECONEXION_CLIENTE_PRESENTE = 21;
	final static int ENTRO_NO_EFECTIVA_SIN_DATOS_REC_REMO_CLIENTE_PRESENTE = 22;
	final static int ENTRO_NO_EFECTIVA_SIN_DATOS_RECONEXION_LITRAJE = 23;
	final static int ENTRO_NO_EFECTIVA_SIN_DATOS_REC_REMO_LITRAJE = 24;
	/**
	 * No se genera cambio en las lecturas que se no se han leido
	 */
	final static int NINGUNO=0;
	/**
	 * Se realiza un procedimiento para cerrar las lecturas que no se han tomado
	 */
	final static int FORZADO=1;
	/**
	 * No puede entregar la ruta si alguna de las lecturas no se ha tomado
	 */
	final static int RUTA_COMPLETA=2;
	
	final static String SECUENCIA_CORRECTA_SUPER="ABC";
	String secuenciaSuperUsuario="";
	
	boolean mostrarCodigoUsuario=false;
	
	/**
	 * Cambia el pais actual
	 */
	public int ii_pais=this.ENGIE;
	
	int modoDeCierreDeLecturas=FORZADO;
	
	int flash /* ah-ah! he is a miracle!*/= CamaraActivity.AUTO;
	int zoom /* ah-ah! he is a miracle!*/ = 0;
	int camaraFrontal /* ah-ah! he is a miracle!*/ = 0;
	
	//public boolean entrarComoSuperUsuario=true;
	
	public String letraPais="A";
	public int tipoDeValidacion = CON_SMS;
	boolean mostrarNoRegistrados = true;
	int calidadDeLaFoto = 50;
	/**Calidad que se remplaza segun otros parametros**/
	int calidadOverride=calidadDeLaFoto;
	boolean ignorarGeneracionCalidadOverride=false;
	/**Por cada cuantas lecturas malas toma una foto**/
	int controlCalidadFotos=1;
	boolean ignorarContadorControlCalidad=false;
	boolean ignorarfoto=false;
	
	/** Elementos que se pueden esconder de la pantalla de configuracion **/
	boolean mostrarMacBt=true;
	boolean mostrarMacImpresora=true;
	boolean mostrarServidorGPRS=true;
	boolean mostrarFactorBaremo=true;
	boolean mostrarTamañoFoto=true;
	boolean mostrarMetodoDeTransmision=true;
	boolean mostrarIngresoFacilMAC=false;
	boolean mostrarImpresion=false;
	boolean mostrarCalidadFoto=true;
	
	/**Elementos del menu que deben ser escondidos segun cada version**/
	boolean mostrarGrabarEnSD=false;
	
	/** Informacion por default de la pantalla de configuracion **/
	String defaultLote="ACTIVOS";
	String defaultCPL="";
	String defaultTransmision="0";
	String defaultTamañoFoto="0";
	String defaultRutaDescarga="C:\\Apps\\SGL\\Lectura";
	
	/** Default de los tamaños de fuente**/
	Double porcentaje_main=1.0,
			porcentaje_main2=1.0,
			porcentaje_hexateclado=1.0,
			porcentaje_teclado=1.0,
			porcentaje_lectura=1.0,
			porcentaje_info=1.0;
	
	/**
	 * Este baremo funciona para remplazar el baremo actual
	 */
	int baremo=75;
	
	int mensajeContraseñaLecturista=R.string.str_login_msj_lecturista;
	
	boolean sonidos=true;
	
	private String usuario=""; //Usuario actual

	/**Variables de toma de lecturas**/
	TodasLasLecturas tll; //Variable en donde estan todas las lecturas
	long il_ultimoSegReg=0; //Ultimo medidor guardado
	String idMedidorUltimaLectura="";
	String is_lectura, is_presion, is_caseta, is_terminacion;
	boolean bModificar=false, bcerrar=true;
	boolean moverPosicion=false;
	boolean bEstabaModificando=false;
	boolean capsModoCorreccion=false;
	boolean permiteDarVuelta=false;
	boolean sonLecturasConsecutivas=false;
	boolean estoyTomandoFotosConsecutivas=false;
	long il_lect_act=0, il_total, il_lect_max, il_lect_min;
	String is_nombre_Lect="";
	int ii_orden=TomaDeLecturasPadre.ASC;
	Vector <Lectura> lecturasConFotosPendientes=null;
	int ii_foto_cons_act=0;
	boolean estoyCapturando = false;
	boolean inputMandaCierre=false;
	boolean requiereLectura = false;
	//boolean mostrarDatosCompletos=false;
	//int il_lectDistinta = 0;
	
	/** 
	 * Indica que si se borra una lectura en modo de correccion, pueda mostrar el boton de grabar
	 */
	boolean dejarComoAusentes=false;
	String mensaje="";
	
	boolean modoCaptura = false;
	
	int ultimaPestanaAnomaliasUsada=PantallaAnomaliasTabsPagerAdapter.TODAS;
	

	
	boolean gpsEncendido=false;
	boolean requiereGPS=false;
	/**
	 * Indica si el  programa esta capacitado para tomar puntos gps
	 */
	boolean GPS=false;
	
	boolean fotoForzada = false; // Siempre tomará foto despues de una lectura
	boolean validar = true; // No se validará la lectura
	
	
	Location location;
	/**Fin de Variables de toma de lecturas**/
	
	TodosLosCampos tlc /*=new TodosLosCampos()*/;
	TomaDeLecturasGenerica tdlg;
	
	//TomaDeLecturasGenerica tdlGenerica= new TomaDeLecturasGenerica(this);	
	
	
	/** Estas son las variables de configuracion**/
	final static int USO=0;
	final static int ALFABETICAMENTE=1;
	
	boolean filtrarAnomaliasConLectura=false;
	int anomaliasPorMostrar=12;
	int orden=USO;
	
	int logo=R.drawable.logo_engie;
	
	boolean multiplesAnomalias=false;
	boolean convertirAnomalias=false;
	
	int longitudRealCodigoAnomalia=3;
	int longitudCodigoAnomalia=3;
	int longitudCodigoSubAnomalia=3;
	
	String rellenoAnomalia=".";
	boolean rellenarAnomalia=false;
	
	boolean repiteAnomalias=false;
	boolean remplazarDireccionPorCalles=false;
	String anomaliaARepetir="";
	String subAnomaliaARepetir="";
	Lectura lecturaARepetir=null;
	
	boolean mostrarCuadriculatdl=false;
	boolean mostrarRowIdSecuencia=false;
	
	int mensajeDeConfirmar=R.string.msj_lecturas_verifique;
	
	boolean tomaMultiplesFotos=true;
	
	/** Fin de variables de configuracion**/
	
	/** Aqui definimos los sonidos**/
	
	int sonidoCorrecta=Sonidos.BEEP;
	int sonidoIncorrecta=Sonidos.URGENT;
	int sonidoConfirmada=Sonidos.NINGUNO;
	
	/**
	 * Controla si el captura se comporta como en nokia
	 */
	boolean legacyCaptura=false;
	
	boolean puedoVerLosDatos=false;
	
	
	// String defaultServidorGPRS="http:\\www.espinosacarlos.com";
	public String defaultServidorGPRS = "http://192.168.2.123:8182";
	
	/**
	 * De estar encendida, ignora los cambios hechos en la pantalla de configuracion y 
	 * sobreescribe con los default
	 */
	boolean sobreEscribirServidorConDefault=false;
	
	boolean puedoCancelarFotos=false;

	public int longCampoContrasena = 10;
	public boolean esSuperUsuario = false;

	public SesionEntity sesionEntity = null;

	public int maxIntentosAutenticacion = 3;

	boolean habilitarPuntoDecimal = true;

	 Timer supervisor;

	// ================================================
	// Obtiene el usuario que se autenticó
	// ================================================
	public String getUsuario() {
		if (sesionEntity == null)
			return "";
		return sesionEntity.NumCPL;
	}


	// ====================================================================
	// Obtiene el id del técnico que se autenticó
	// ====================================================================

	public long getIdEmpleado() {
		if (sesionEntity == null)
			return 0;

		if (sesionEntity.empleado == null)
			return 0;

		return sesionEntity.empleado.idEmpleado;
	}

	// ====================================================================
	// Obtiene el token de la sesión
	// ====================================================================
	public String getSesionToken() {
		if (sesionEntity == null)
			return "";

		if (sesionEntity.Token == null)
			return "";

		return sesionEntity.Token;
	}

	// ====================================================================
	// Obtiene la regional del usuario que se autenticó
	// ====================================================================
	public String getRegional() {
		if (sesionEntity == null)
			return "";

		if (sesionEntity.empleado == null)
			return "";

		return sesionEntity.empleado.Regional;
	}

	public void setUsuario(String s) {
		usuario = s;

	}

	public SesionEntity getSesionEntity() {
		return sesionEntity;
	}

	public boolean esSesionValida() {
		if (sesionEntity == null)
			return false;

		if (sesionEntity.esSesionVencida())
			return false;
		else
			return true;
	}

	public void cerrarSesion() {
		sesionEntity = null;
	}
	  
	  
	  public String traducirAnomalia(){
		  String anomaliaTraducida;
		  if (convertirAnomalias)
				anomaliaTraducida="conv";
			else
				anomaliaTraducida="anomalia";
		  
		  return anomaliaTraducida;
	  }

	public String getMaterialUtilizado() {
		String strMarbete = "";
		if (tll.getLecturaActual().is_Repercusion.equals("A")) {
			if (tll.getLecturaActual().getTipoDeOrden().equals("DESCONEXION"))
				strMarbete = "MARBETE DX EFE";
			else if (tll.getLecturaActual().getTipoDeOrden().equals("REMOCION"))
				strMarbete = "MARBETE REM N/E";
			else if (tll.getLecturaActual().getTipoDeOrden().equals("RECONEXION")) {
				if (tll.getLecturaActual().is_habitado.equals("0"))
					strMarbete = "2x ARANDELAS, MARBETE RX EFE CLIENTE PRESENTE";
				else
					strMarbete = "2x ARANDELAS, MARBETE RX EFE LITRAJE";
			}else if (tll.getLecturaActual().getTipoDeOrden().equals("REC/REMO")) {
				if (tll.getLecturaActual().is_habitado.equals("0"))
					strMarbete = "2x ARANDELAS, MARBETE R/R EFE CLIENTE PRESENTE";
				else
					strMarbete = "2x ARANDELAS, MARBETE R/R EFE LITRAJE";
			} else
				strMarbete = "";
		}else{
			if (tll.getLecturaActual().getTipoDeOrden().equals("DESCONEXION"))
				strMarbete = "";
			else if (tll.getLecturaActual().getTipoDeOrden().equals("REMOCION"))
				strMarbete = "";
			else if (tll.getLecturaActual().getTipoDeOrden().equals("RECONEXION"))
				strMarbete = "MARBETE RX N/E";
			else if (tll.getLecturaActual().getTipoDeOrden().equals("REC/REMO"))
				strMarbete = "MARBETE R/R N/E";
			else
				strMarbete = "";
		}
		return strMarbete;
	}

	public void BorrarTodasLosCamposEngie() {
		tll.getLecturaActual().is_EncuestaDeSatisfaccion = "";
		tll.getLecturaActual().is_MedidorInstalado = "";
		tll.getLecturaActual().is_idMarcaInstalada = "";
		tll.getLecturaActual().is_LecturaReal = "";
		tll.getLecturaActual().is_Repercusion = "";
		tll.getLecturaActual().is_idMaterialUtilizado = "";
		tll.getLecturaActual().is_idTipoDeReconexion = "";
		tll.getLecturaActual().is_idTipoDeRemocion = "";
		tll.getLecturaActual().is_ClienteYaPagoMonto="";
		tll.getLecturaActual().is_ClienteYaPagoFecha="";
		tll.getLecturaActual().is_ClienteYaPagoAgente="";
	}

	public String getMensajeParaMostrarAntesDeTomarLaFoto(){
		String strMensajeParaMostrar = "Preparese para tomar la foto";

		return strMensajeParaMostrar;
	}

	public int getEstadoDeLaRepercusion(){
		return nEstadoDeLaRepercusion;
	}

	public void setEstadoDeLaRepercusion(boolean bEsUnaEfectiva, boolean bEsParaBorrar){
		if (!bEsParaBorrar) {
			if (bEsUnaEfectiva) {
				if (tll.getLecturaActual().getTipoDeOrden().equals("DESCONEXION"))
					nEstadoDeLaRepercusion = ENTRO_EFECTIVA_SIN_DATOS_DESCONEXION;
				else if (tll.getLecturaActual().getTipoDeOrden().equals("REMOCION"))
					nEstadoDeLaRepercusion = ENTRO_EFECTIVA_SIN_DATOS_REMOCION;
				else if (tll.getLecturaActual().getTipoDeOrden().equals("RECONEXION"))
					nEstadoDeLaRepercusion = ENTRO_EFECTIVA_SIN_DATOS_RECONEXION;
				else if (tll.getLecturaActual().getTipoDeOrden().equals("REC/REMO"))
					nEstadoDeLaRepercusion = ENTRO_EFECTIVA_SIN_DATOS_REC_REMO;
				else
					nEstadoDeLaRepercusion = ENTRO_EFECTIVA_SIN_DATOS_OTRO;
			} else {
				if (tll.getLecturaActual().getTipoDeOrden().equals("DESCONEXION"))
					nEstadoDeLaRepercusion = ENTRO_NO_EFECTIVA_SIN_DATOS_DESCONEXION;
				else if (tll.getLecturaActual().getTipoDeOrden().equals("REMOCION"))
					nEstadoDeLaRepercusion = ENTRO_NO_EFECTIVA_SIN_DATOS_REMOCION;
				else if (tll.getLecturaActual().getTipoDeOrden().equals("RECONEXION"))
					nEstadoDeLaRepercusion = ENTRO_NO_EFECTIVA_SIN_DATOS_RECONEXION;
				else if (tll.getLecturaActual().getTipoDeOrden().equals("REC/REMO"))
					nEstadoDeLaRepercusion = ENTRO_NO_EFECTIVA_SIN_DATOS_REC_REMO;
				else
					nEstadoDeLaRepercusion = ENTRO_NO_EFECTIVA_SIN_DATOS_OTRO;
			}
		} else {
			if (bEsUnaEfectiva) {
				if (tll.getLecturaActual().getTipoDeOrden().equals("DESCONEXION"))
					nEstadoDeLaRepercusion = ENTRO_EFECTIVA_CON_DATOS_DESCONEXION;
				else if (tll.getLecturaActual().getTipoDeOrden().equals("REMOCION"))
					nEstadoDeLaRepercusion = ENTRO_EFECTIVA_CON_DATOS_REMOCION;
				else if (tll.getLecturaActual().getTipoDeOrden().equals("RECONEXION"))
					nEstadoDeLaRepercusion = ENTRO_EFECTIVA_CON_DATOS_RECONEXION;
				else if (tll.getLecturaActual().getTipoDeOrden().equals("REC/REMO"))
					nEstadoDeLaRepercusion = ENTRO_EFECTIVA_CON_DATOS_REC_REMO;
				else
					nEstadoDeLaRepercusion = ENTRO_EFECTIVA_CON_DATOS_OTRO;
			} else {
				if (tll.getLecturaActual().getTipoDeOrden().equals("DESCONEXION"))
					nEstadoDeLaRepercusion = ENTRO_NO_EFECTIVA_CON_DATOS_DESCONEXION;
				else if (tll.getLecturaActual().getTipoDeOrden().equals("REMOCION"))
					nEstadoDeLaRepercusion = ENTRO_NO_EFECTIVA_CON_DATOS_REMOCION;
				else if (tll.getLecturaActual().getTipoDeOrden().equals("RECONEXION"))
					nEstadoDeLaRepercusion = ENTRO_NO_EFECTIVA_CON_DATOS_RECONEXION;
				else if (tll.getLecturaActual().getTipoDeOrden().equals("REC/REMO"))
					nEstadoDeLaRepercusion = ENTRO_NO_EFECTIVA_CON_DATOS_REC_REMO;
				else
					nEstadoDeLaRepercusion = ENTRO_NO_EFECTIVA_CON_DATOS_OTRO;
			}
		}
	}
}
