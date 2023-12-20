package enruta.soges_engie;

import enruta.soges_engie.R;
import java.util.Vector;

public class MensajeEspecial {
	
	final static int SIN_MENSAJE_ESPECIAL=0;
	final static int MENSAJE_SI_NO=1;
	final static int OPCION_MULTIPLE=2;
	
	final static int SI=0;
	final static int NO=1;
	
	String codigo, descripcion;
	Vector<Respuesta> respuestas = new Vector<Respuesta>();
	int color=R.color.Pink;
	int tipo;
	
	/**
	 * Establece si se puede cancelar el mensaje
	 * Por default, se puede cancelar
	 */
	boolean cancelable=true;
	
	/**
	 * Esta variable tiene a quien se va a responder el mensaje
	 */
	int respondeA;
	
	/**
	 * Crea un mensaje sin respuestas
	 * @param tipo
	 * @param descripcion
	 *  @param Para personalizar lo que se hará despues de seleccionar la respuesta
	 */
	MensajeEspecial(int tipo, String descripcion, int respondeA){
		this.tipo= tipo;
		this.descripcion= descripcion;
		this.respondeA=respondeA;
	}
	
	/**
	 * Crea un mensaje de si o no
	 * @param descripcion
	 * @param Para personalizar lo que se hará despues de seleccionar la respuesta
	 */
	MensajeEspecial(String descripcion, int respondeA){
		this.tipo= MENSAJE_SI_NO;
		this.descripcion= descripcion;
		this.respondeA=respondeA;
	}
	
	/**
	 * Crea un mensaje con sus respuestas predefinidas
	 * @param descripcion
	 * @param respuestas
	 *  @param Para personalizar lo que se hará despues de seleccionar la respuesta
	 */
	MensajeEspecial(String descripcion, Vector<Respuesta> respuestas, int respondeA){
		this.tipo= OPCION_MULTIPLE;
		this.respuestas=respuestas;
		this.descripcion= descripcion;
		this.respondeA=respondeA;
	}
	
	public String regresaValor(int pos){
		if (tipo==MENSAJE_SI_NO){
				return String.valueOf(pos);
		}
		else if (tipo==OPCION_MULTIPLE){
			return respuestas.get(pos).codigo;
		}
		return "";
	}
	
	public String regresaDescripcion(String codigo){
		if (tipo==MENSAJE_SI_NO){
			if (String.valueOf(SI).equals(codigo))
				return "Si";
			else
				return "No";
		}
		else if (tipo==OPCION_MULTIPLE){
			//Buscamos en el arreglo
			for (Respuesta actual: respuestas){
				if (actual.codigo.equals(codigo)){
					//Regresamos la correcta
					return actual.descripcion;
				}
			}
		}
		return "";
	}
	
	public void agregarRespuestas(String codigo, String descripcion){
		agregarRespuestas(new Respuesta(codigo,  descripcion));
	}
	
	public void agregarRespuestas(Respuesta respuesta){
		if (tipo==MENSAJE_SI_NO || tipo==SIN_MENSAJE_ESPECIAL){
			return;
		}
			respuestas.add(respuesta);
	}
	
	public String [] getArregloDeRespuestas(){
		String [] respuestas = new String[this.respuestas.size()];
		int i=0;
		for (Respuesta resp: this.respuestas){
			respuestas[i]=resp.descripcion;
			i++;
		}
		return respuestas;
	}
}
