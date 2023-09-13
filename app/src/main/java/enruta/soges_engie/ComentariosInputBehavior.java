package enruta.soges_engie;

import android.text.InputType;

/**
 * Esta clase contiene el comportamiento del EditText de Input
 * @author Humberto Casgar
 *
 */
public class ComentariosInputBehavior {

	String mensaje="";
	int longitud=0;
	int tipo=InputType.TYPE_CLASS_TEXT;
	boolean obligatorio=true;
	String texto;
	
	ComentariosInputBehavior(String mensaje, int tipo, int longitud,String texto){
		this.mensaje=mensaje;
		this.longitud=longitud;
		this.tipo=tipo;
		this.texto=texto;
	}
	
}
