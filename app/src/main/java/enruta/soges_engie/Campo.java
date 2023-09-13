package enruta.soges_engie;

public class Campo {
	
	private int ii_numColumna, ii_posicion, ii_longitud, ii_alineacion;
	private String is_nombre, is_relleno;
	boolean esDeEntrada=true;
	
	/*tipos de alineacion*/
	
	/**Alineacion a la derecha**/
	final static int D =0;
	/**Alineacion a la izquierda**/
	final static int I =1;
	/**Alineacion tipo fecha**/
	final static int F =2;

	
	/**
	 * Contructor de campo
	 * @param li_numColumna Numero de columna
	 * @param ls_nombre Nombre de la columna
	 * @param li_posicion posicion del campo en un arreglo de bytes
	 * @param li_longitud longitud maxima del campo
	 * @param ls_alineacion alineacion del campo
	 * @param ls_relleno relleno del campo
	 */
	Campo(int li_numColumna, String ls_nombre, int li_posicion,int li_longitud,int li_alineacion,String ls_relleno){
		ii_numColumna=li_numColumna;
		ii_posicion=li_posicion;
		ii_longitud=li_longitud;
		is_nombre=ls_nombre;
		ii_alineacion=li_alineacion;
		is_relleno=ls_relleno;
	}
	
	Campo(int li_numColumna, String ls_nombre, int li_posicion,int li_longitud,int li_alineacion,String ls_relleno, boolean esDeEntrada){
		ii_numColumna=li_numColumna;
		ii_posicion=li_posicion;
		ii_longitud=li_longitud;
		is_nombre=ls_nombre;
		ii_alineacion=li_alineacion;
		is_relleno=ls_relleno;
		this.esDeEntrada=esDeEntrada;
	}
	
	public String getNombre(){
		return is_nombre;
	}
	
	public int getLong(){
		return ii_longitud;
	}
	
	public int getPos(){
		return ii_posicion;
	}
	
	public String recortarByte(byte[] bytes){
		String s;

		try {
			if (bytes.length >= (ii_posicion + ii_longitud)) {
				s = new String(bytes, ii_posicion, ii_longitud);
				return s;
			}
			else {
				s = new String(bytes, ii_posicion, bytes.length - ii_posicion);
				return s;
			}
		} catch (Exception e) {
			return "";
		}
	}
	
	public String recortarByte(String valor){
		String s;
		int n;

		try {
			n = valor.length();
			if (n >= (ii_posicion + ii_longitud)) {
				s = valor.substring(ii_posicion, ii_posicion + ii_longitud);
				return s;
			}
			else {
				s = valor.substring(ii_posicion, n);
				return s;
			}
		} catch (Exception e) {
			return "";
		}
	}
	
	public static String rellenaString(String texto, String relleno, int veces, boolean lugar){
		String ls_final=texto;
		int li_restantes;
		
		//Si se supera la cantidad de relleno contra la longitud de la cadena, debera devolver un recorte de cadena
		if (texto.length()>veces){
			return texto.substring(0, veces);
		}
		
		li_restantes=veces - texto.length();
		
		for (int i=0; i<li_restantes;i++){
			if (lugar)
				ls_final=relleno+ls_final;
			else
				ls_final=ls_final + relleno;
			}
		return ls_final;
	}
	
	public String campoSQLFormateado(){
		String campo="";
		int li_longitud=0;
		switch(ii_alineacion){
		case D:
			li_longitud=ii_longitud;
			campo =" substr('"+rellenaString("", is_relleno, ii_longitud, true)+ "'|| " +is_nombre+", -"+li_longitud+", "+li_longitud+") " /*+ is_nombre*/;
			break;
		
		case I:
			li_longitud=ii_longitud+1;
			campo =" substr( " +is_nombre+"||'"+rellenaString("", is_relleno, ii_longitud, true)+"', "+li_longitud+", -"+li_longitud+ ") "/* + is_nombre*/;
			break;
			
		case F:
			li_longitud=ii_longitud+1;
			campo =" substr( " +is_nombre+"||'"+rellenaString("", " ", ii_longitud, true)+"', "+(li_longitud)+", -"+(li_longitud) +") "/* + is_nombre*/;
			break;
		}
		
		return campo;
	}
	
	public String getRelleno(){
		return is_relleno;
	}
	
	public int getAlineacion(){
		return ii_alineacion;
	}
}
