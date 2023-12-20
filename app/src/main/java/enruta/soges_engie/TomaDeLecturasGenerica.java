package enruta.soges_engie;

import java.util.Hashtable;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import enruta.soges_engie.entities.DatosEnvioEntity;

/** Esta clase crea las validaciones y los campos a mostrar**/
public abstract class TomaDeLecturasGenerica {
	Globales globales;
	String is_lectAnt = "";
	Context context;
	
	DBHelper dbHelper;
	SQLiteDatabase db;
	
	static final int PREGUNTAS_CONSUMO_CERO=0;
	static final int PREGUNTAS_SIGUE_CORTADO=1;
	static final int PREGUNTAS_EN_EJECUCION=2;
	static final int PREGUNTAS_UBICACION_VACIA=3;
	static final int ANOMALIA_SEIS=4;
	static final int VER_DATOS=5;
	static final int PREGUNTAS_ESTA_HABITADO=6;
	static final int PREGUNTAS_TIENE_REGISTRO=7;
	
	static final int NO_SOSPECHOSA=0;
	static final int SOSPECHOSA=1;
	
	static final int AUSENTES=0;
	static final int LEIDAS=1;
	
	static final int ENTRADA=0;
	static final int SALIDA=1;
	static final int MOTIVOS=2;
	
	MensajeEspecial mj_habitado;
	MensajeEspecial mj_registro;
	
	/**Variables que tienen que ver con sobrescribir**/
	static final int FOTOS=0;
	static final int MENSAJE=1;
	
	/**
	 * Longitud del registro
	 */
	int long_registro=543;
	
	
	/** Arreglo en tipo de hash (para facilitar la busqueda) que contiene
	 * Key = Anomalia a ser ingresada
	 * Value = String de valores
	 */
	Hashtable <String, String> anomaliasCompatibles;
	TomaDeLecturasGenerica(Context context){
		this.context=context;
		globales= ((Globales) context.getApplicationContext());
		globales.tlc =new TodosLosCampos(getCamposBDAdicionales());
		creaTodosLosCampos();
	}
	
	
	
	/**
	 * Validacion de una lectura
	 * @param ls_lectAct
	 * @return Regresa el mensaje de error
	 */
	public abstract String validaLectura(String ls_lectAct) throws Exception;

	/**
	 * Nombra una foto
	 * @param globales Objeto en donde se encuentran todas las variables globales
	 * @param db Objeto que contiene la base de datos
	 * @param secuencial Secuencial de lectura
	 * @param is_terminacion Terminacion de la foto (En caso de tener)
	 * @return
	 */
	public abstract String getNombreFoto(Globales globales, SQLiteDatabase db, long secuencial, String is_terminacion );
	
	public abstract  Vector<String> getInformacionDelMedidor( Lectura lectura) throws Exception;

	public abstract  String getDatosSAP(Lectura lectura, int nCampo) throws Exception;
	public abstract  String getDatosDelCliente(Lectura lectura) throws Exception;
	public abstract  String getDatosDireccion(Lectura lectura) throws Exception;

	public abstract  MensajeEspecial getMensaje();
	
	/**
	 * Verifica si la anomalia ingresada es compatible con las anomalias que se ingresaron anteriormente
	 * @param anomaliaAInsertar Anomalia por Ingresar
	 * @param anomaliasCapturadas Anomalias Ingresadas anteriormente
	 * @return Regresa un objeto boleano indicando "verdadero" si es compatible o "falso" si no es compatible
	 */
	public abstract  boolean esAnomaliaCompatible(String anomaliaAInsertar, String anomaliasCapturadas);
	
	/**
	 * Devuelve el aviso que debe indicar la pantalla de input despues de insertar una anomalia
	 * @param anomalia Anomalia seleccionada
	 * @return El aviso con el que cuenta la anomalia, de no tener, se devuelve null
	 */
	public abstract ComentariosInputBehavior getAvisoMensajeInput(String anomalia);
	
	/**Realiza los cambios necesarios en la bd segun la anomalia seleccionada
	 * 
	 * @param Anomalia Anomalia seleccionada
	 */
	public abstract void RealizarModificacionesDeAnomalia(String anomalia, String comentarios);
	
	/**Deshace los cambios necesarios en la bd segun la anomalia seleccionada
	 * 
	 * @param Anomalia Anomalia borrada
	 */
	public abstract void DeshacerModificacionesDeAnomalia(String anomalia);
	
	/**
	 * Considera si se deberá mostrar un mensaje cuando se ingresa un consumo
	 * @param ls_lectAct lectura ingresada
	 * @return El mensaje que se deberá mostrar o de no haber mensaje, devolverá nulo
	 */
	public abstract MensajeEspecial mensajeDeConsumo(String ls_lectAct);
	
	/**
	 * Maneja lo que debe de hacer cierto pais con la seleccion de un mensaje
	 * @param me Mensaje especial que se mostró
	 * @param respuesta la respuesta de seleccionada
	 */
	public abstract void RespuestaMensajeSeleccionada(MensajeEspecial me, int respuesta);
	
	/**
	 * Genera un campo apartir de una clave
	 * @param campo Clave del campo a agregar
	 * @return Regresa el campo indicado, de no tener un campo para esa clave se regresa null
	 */
	public abstract ComentariosInputBehavior getCampoGenerico(int campo);
	
	/**
	 * Regresa los campos que tendrá la pantalla generica segun la anomalia dada
	 * @param anomalia Anomalia que pudiera tener campos genericos
	 * @return Los campos genericos de la anomalia, en caso de no tener regresa null
	 */
	public abstract int[] getCamposGenerico(String anomalia);
	
	/**
	 * Realiza las operaciones necesarias con los campos genericos presentados en pantalla
	 * @param bu_params Parametros regresados por la pantalla de input generico
	 */
	public abstract void regresaDeCamposGenericos(Bundle bu_params, String anomalia);
	
	/**
	 * Inicializa campos que no se encuentran en el archivo
	 */
	public abstract ContentValues getCamposBDAdicionales();
	
	/**
	 * Genera todos los campos necesarios para cada pais
	 */
	public abstract void creaTodosLosCampos();
	
	/**
	 * Al dar el nombre de la etiqueta, devuelve el valor que deberá tener. Esto se puede usar para partes del diseño que estan fijas y dificiles de recrear
	 * con programacion.
	 * 
	 * @param ls_etiqueta Nombre de la etique
	 */
	public abstract String obtenerContenidoDeEtiqueta(String ls_etiqueta);
	
	/**
	 * Regresa el formato de la etiqueta a insertar
	 * @return Regresa el formato a aplicar, si regresa nulo, no hay mensaje que mostrar
	 */
	public abstract FormatoDeEtiquetas getMensajedeRespuesta();
	
	/**
	 * Regresa el mensaje a mostrar en el espacio de Advertencias
	 * @return Mensaje a mostrar
	 */
	public abstract String getMensajedeAdvertencia();
	
	/**
	 * Realiza acciones posteriores despues de haber borrado la lectura
	 */
	public abstract void regresaDeBorrarLectura();
	
	/**
	 * Realiza los cambios que se le tienen que hacer a una anomalia dependiendo del pais
	 * @param ls_lect_act lectura que se tomó
	 */
	public abstract void cambiosAnomaliaAntesDeGuardar(String ls_lect_act);
	
	/**
	 * Establece las anomalias a repetir, si esta vacio, no hay nada que repetir
	 */
	public abstract void anomaliasARepetir();
	
	/**
	 *Establece las subanomalias a repetir, si esta vacio, no hay nada que repetir
	 */
	public abstract void subAnomaliasARepetir();
	
	/**
	 * Realiza las acciones propias del pais para repetir su anomalia
	 */
	public abstract void repetirAnomalias();
	
	
	/**
	 * Indica si es o no una anomalia de segunda visita
	 */
	public abstract boolean esSegundaVisita(String ls_anomalia , String ls_subAnom);
	
	/**
	 * Indica si debe moverse despues de haber capturado la anomalia seleccionada
	 */
	public abstract boolean avanzarDespuesDeAnomalia(String ls_anomalia , String ls_subAnom, boolean guardar);
	
	/**
	 * Indica si la anomalia debe insertarse o no
	 * @param ls_anomalia anomalia a insertarse
	 * @return devuelve el mensaje de error si no pasa la validacion. Si el mensaje esta vacio, quiere decir que pasó la validación
	 */
	public  abstract String validaAnomalia(String ls_anomalia);
	
	/**
	 * Regresa el prefijo con el que debe empezar un comentario, esta funcion solo funciona cuando se puede agregar una anomalia con multiples comentarios
	 * @param ls_anomalia anomalia a insertar
	 * @return el prefijo que deberá estar al principio del comentario
	 */
	public abstract String getPrefijoComentario(String ls_anomalia);
	
	/**
	 * Segun la lectura obtiene el consumo
	 * @param lectura
	 * @return
	 */
	public abstract long getConsumo(String lectura);
	
	/**
	 *  @param anomalia Anomalia que se esta insertando
	 * @param comentario 
	 * @return
	 */
	public abstract String validaCamposGenericos(String anomalia, Bundle comentario);
	
	/**
	 * Regresa el filtro segun el parametro indicado
	 * @param comoFiltrar Indique LEIDAS (1) para obtener todos los medidores con lectura y AUSENTES(0) para obtener todos
	 * los medidores sin lectura
	 * @return Regresa la cadena de filtrado
	 */
	public String getFiltroDeLecturas(int comoFiltrar){
		switch(comoFiltrar){
		case AUSENTES:
			//return " lectura=''  and anomalia='' " ;
			return " trim(tipoLectura)='' ";
		case LEIDAS:
			//return " (lectura<>'' or anomalia<>'') ";
			return " trim(tipoLectura)<>'' ";
		}
		
		return "";
	}
	
	/**
	 * Indica si debe moverse despues de haber capturado la anomalia seleccionada
	 */
	public abstract String getDescripcionDeBuscarMedidor(Lectura lectura, int tipoDeBusqueda, String textoBuscado);
	
	/**
	 * Realiza los cambios necesarios en la bd segun la anomalia seleccionada
	 * @param Anomalia Anomalia seleccionada
	 */
		public abstract void RealizarModificacionesDeAnomalia(String anomalia);
		
		/**
		 * Establece el consumo de la lectura actual
		 */
		public abstract void setConsumo();
		
		
		/**
		 * Hace los procesos necesarios despues de regresar de anomalias
		 * @param ls_anomalia Anomalia ingresada
		 * @return De tenerlo, regresa un mensaje especial, de lo contrario, regresa null.
		 */
		public abstract MensajeEspecial regresaDeAnomalias(String ls_anomalia);

		/**
		 * Verifica si la anomalia a repetir se puede repetir con la lectura actual
		 * @return
		 */
		public  boolean puedoRepetirAnomalia(){
			return false;
		}
		
		/**
		 * Segun su configuracion, sobreescribe lo que exista en los registros de anomalias con lo que querramos modificar
		 * @param tipo Tipo de remplazo, fotos o mensajes
		 * @param anomalia Anomalia o sub anomalia correspondiente
		 * @param valor valor a remplazar
		 * @return Valor remplazado, o si no hay, el mismo valor
		 */
		public String remplazaValorDeArchivo(int tipo, String anomalia, String valor){
			return valor;
		}
		
		
		/**
		 * Establece los cambios necesarios en la anomalia antes de ser insertada
		 * @param anomalia Anomalia a ser insertada
		 */
		public abstract void cambiosAnomalia(String anomalia);
		
		/**
		 * Hace los cambios necesarios despues de borrar una anomalia
		 * @param anomaliaBorrada
		 */
		public  void cambiosAlBorrarAnomalia(String anomaliaBorrada){
			
		}
	
	public abstract long getLecturaMinima();
	public abstract long getLecturaMaxima(); 
	
	protected void openDatabase() {
		//dbHelper = new DBHelper(context);

		dbHelper= DBHelper.getInstance(context);
		db = dbHelper.getReadableDatabase();
	}

	protected void closeDatabase() {
		db.close();
		//dbHelper.close();
	}
	

	
	public String devolverConfiguracion(String llave){
		openDatabase();
		String valor="";
		Cursor c = db.query("config", null, "key='"+llave+"'", null, null, null,
				null);

		if (c.getCount() > 0) {
			c.moveToFirst();
			valor= c.getString(c.getColumnIndex("value"));

		}

		c.close();
		
		closeDatabase();
		
		return valor;
	}
	
	public int devolverConfiguracionInt(String llave){
		int valor=0;
		try{
			valor = Integer.parseInt(devolverConfiguracion(llave));
		}
		catch(Throwable e){
			
		}
		
		return valor;
		
	}
	
	/**
	 * Devuelve el nombre del archivo segun el tipo
	 * @param tipo Tipo de archivo
	 * @return
	 */
	public String getNombreArchvio(int tipo){
//		switch(tipo){
//		case ENTRADA:
//		case SALIDA:
//		}
		//Por default es el numero de CPL
		String ls_extension="TXT";
		String ls_archivo="";
		switch(tipo){
		case ENTRADA:
		case SALIDA:
			ls_archivo=globales.getUsuario()+"."+ls_extension;
			break;
		case MOTIVOS:
			ls_archivo="motivos."+ls_extension;
			break;
		}
		
//		openDatabase();
//		
//		Cursor c= db.rawQuery("select value from config where key='cpl'", null);
//		
//		if (c.getCount()==0){
//			c.close();
//			closeDatabase();
//				return"";
//			}
//		c.moveToFirst();
//		if (c.getString(c.getColumnIndex("value")).trim().equals("")){
//			c.close();
//			closeDatabase();
//				return "";
//		}
//		ls_archivo=c.getString(c.getColumnIndex("value"))+"."+ls_extension;
//		c.close();
//		closeDatabase();
		
		return ls_archivo;
		
	}


	public void setTipoLectura(){
		if (!globales.tll.getLecturaActual().getLectura().trim().equals("")) {
			globales.tll.getLecturaActual().is_tipoLectura="0";
		}else  if (globales.tll.getLecturaActual().getLectura().trim().equals("") &&  !globales.tll.getLecturaActual().getAnomaliasCapturadas().equals("")){
			globales.tll.getLecturaActual().is_tipoLectura="4";
		}else{
			globales.tll.getLecturaActual().is_tipoLectura="";
		}
	}



	public int cambiaCalidadSegunTabla(String Anomalia, String subAnomalia) {
		// TODO Auto-generated method stub
		return globales.calidadDeLaFoto;
	}



	/**
	 * Indica si se debe serguir con la toma de una foto o no
	 * @return
	 */
	public boolean continuarConLaFoto() {
		// TODO Auto-generated method stub
		return true;
	}

	public DatosEnvioEntity getInfoFoto(Globales globales, SQLiteDatabase db, long secuencial, String is_terminacion) throws Exception {
		return null;
	}

    public DatosEnvioEntity getInfoFoto(Globales globales, SQLiteDatabase db) throws Exception {
		return null;
    }
}
