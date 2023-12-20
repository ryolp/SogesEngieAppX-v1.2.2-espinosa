package enruta.soges_engie;

import java.util.Hashtable;

import enruta.soges_engie.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class CopyOftrasmisionDatos extends AppCompatActivity {
	TextView tv_progreso, tv_indicador;
	ProgressBar pb_progress;
	Handler mHandler;
	
	DBHelper dbHelper;
	 SQLiteDatabase db;
	
	public final static int TRANSMISION=0;
	public final static int RECEPCION=1;
	
	private final static int PROGRESO=0;
	private final static int MENSAJE=1;
	private final static int BARRA=2;
	private final static int TOPE=3;
	
	
	public final static int ERROR=-99;
	public final static int CANCELADA_POR_USUARIO=-98;
	
	boolean yaAcabo=false;
	boolean mostrarAlerta=true;
	boolean transmitirTodo=false;
	
	String ls_categoria, ls_servidor;
	boolean transmiteFotos=true;
	//int id;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	        setContentView(R.layout.enproceso);
	        String [] ls_params;
	        
	       Bundle bu_params= getIntent().getExtras();
	       
	       try{
	    	   transmiteFotos=bu_params.getBoolean("transmiteFotos");
	       }
	       catch(Throwable e){}
	      
	       
	       openDatabase();
	       
	       
	       Cursor c=db.rawQuery("Select value from config where key='servidor'", null);
			 c.moveToFirst();
			   if (c.getCount()==0){
				   
					   //muere(true, "No se ha configurado algún servidor.\nConfigure el servidor en la pantalla 'Configuración' que se en cuentra en el menu de la 'Categoria" +
					   //		" Principal'.");
				   
				   db.execSQL("insert into config(key, value) values ('servidor', 'http://www.espinosacarlos.com')");
				   c=db.rawQuery("Select value from config where key='servidor'", null);
				   c.moveToFirst();
					  // return;
				   ls_servidor=c.getString(c.getColumnIndex("value"));
				   
			   }else{
				   ls_servidor=c.getString(c.getColumnIndex("value"));
				   if (ls_servidor.equals("")){
					   muere(true, "No se ha configurado algún servidor.\nConfigure el servidor en la pantalla 'Configuración' que se en cuentra en el menu de la 'Categoria" +
					   		" principal'.");
					   return;
				   }
				   
			   }
	       
	       closeDatabase();
	       
	        
	        tv_progreso = (TextView) findViewById(R.id.ep_tv_progreso);
			tv_indicador = (TextView) findViewById(R.id.ep_tv_indicador);
			pb_progress = (ProgressBar) findViewById(R.id.ep_gauge);
			
			mHandler=new Handler();
			
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			AlertDialog alert;
				
			openDatabase();
			//Aqui tomo el itinerario a cargar de parametros
			
			
					
			closeDatabase();
			
			
			switch( bu_params.getInt("tipo")){
			case TRANSMISION:
				int li_datos, li_fotos;
				//transmitir();
				openDatabase();
				ls_params=new String[1];
				ls_params[0]=String.valueOf(TomaDeLecturas.NO_ENVIADA);
				transmitirTodo= bu_params.getBoolean("transmitirTodo");
				if (!transmitirTodo){
					c= db.rawQuery("select * from lecturas where envio="+TomaDeLecturas.NO_ENVIADA, null);
					li_datos=c.getCount();
					c= db.rawQuery("select * from fotos where envio="+TomaDeLecturas.NO_ENVIADA, null);
					li_fotos=c.getCount();
				}
				else{
					c= db.rawQuery("select * from lecturas", null);
					li_datos=c.getCount();
					li_fotos=0;
				}
				
				
				if (li_datos==0 && li_fotos==0){
   					muere(true, "No hay datos que exportar");
   					return;
   				}
				c= db.rawQuery("select value from config where key='archivo'", null);
				
				if (c.getCount()==0){
   					muere(true, "No se ha importado algún archivo de lecturas.");
   					return;
   				}
				c.moveToFirst();
				ls_categoria=c.getString(c.getColumnIndex("value"));
				closeDatabase();
				 transmitir();
				
				break;
			case RECEPCION:
				//recepcion();
				
				builder.setMessage(R.string.msj_warning_importar)
				       .setCancelable(false).setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id){
				        	   //recepcion();
				        	   preguntaArchivo();
				                dialog.dismiss();
				           }
				       })
				       .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id){
				        	   dialog.cancel();
				        	   mostrarAlerta=false;
				        	   muere(true, "");
				                
				           }
				       });
				
				alert = builder.create();
				alert.show();
				break;
			}
			
	}
	
	public void transmitir(){
		final Context context= this;
		Thread hilo= new Thread(){
			int cantidad;
			 String mPhoneNumber ;
			
			public void run() {
				
				// TODO Auto-generated method stub
				Serializacion serial= new Serializacion(Serializacion.WIFI);
		   		 String ls_cadena="";
		   		String ls_nombre_final;

		   		 //Con esta instruccion obteniamos el celular, sin embargo, no funciona porque puede que la tarjeta SIM no tenga impreso el telefono
		   		 /*TelephonyManager tMgr =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		   		  String mPhoneNumber = tMgr.getLine1Number();*/
		   		 // ProgressDialog dialog = ProgressDialog.show(context, "Exportar", "Se esta exportando el archivo, espere", true);
		   		  
		   		 //mostrarMensaje(MENSAJE, "Remplazando datos descargados anteriormente.");
		   		mostrarMensaje(PROGRESO, "Espere...");
		   		Cursor c = null;
		   	        try {openDatabase();
		   	        
		   	        
		   	        
		   	        
		   	        	//borrarArchivo( "maxigas/"+ ls_categoria+".txt");
		   	        	
		   	        	
		   	        	byte[] lby_cadena= new byte[10];
		   	        	
		   	        	String [] ls_params={String.valueOf(TomaDeLecturas.NO_ENVIADA)};
		   	        	
		   	        	if (!transmitirTodo)
		   	        		 c= db.rawQuery("select * from lecturas where envio="+TomaDeLecturas.NO_ENVIADA, null);
		   	        	else
		   	        		c= db.rawQuery("select * from lecturas ", null);
		   				
		   				
		   				cantidad=c.getCount();
		   				
		   				
		   				mHandler.post(new Runnable() {
		   		            public void run() {
		   		            	pb_progress.setMax(cantidad);
		   					}
		   				});
		   				
		   				mostrarMensaje(PROGRESO, "Generando datos a exportar");
		   				mostrarMensaje(MENSAJE, "Espere...");
		   				
		   				
		   				//c.moveToFirst();
		   				if (!transmitirTodo)
		   					ls_nombre_final= ls_categoria.substring(0, ls_categoria.length() - 4) + "_exp.txt";
		   				else{
		   					ls_nombre_final=ls_categoria.substring(0, ls_categoria.length() - 4) + "_todo.txt";
		   					borrarArchivo( "maxigas/lecturas/"+ls_nombre_final);
		   				}
		   					
		   				
		   				for (int i=0;i<c.getCount();i++){
		   					c.moveToPosition(i);
		   					serial.open(ls_servidor, "maxigas/lecturas",ls_nombre_final, Serializacion.ESCRITURA,
									0, 0, 0, "", 0, context);
		   					
		   					ls_cadena=generaCadenaAEnviar(c);
		   					serial.write(ls_cadena);
		   					
		   					
		   					String bufferLenght;
    						int porcentaje =(i*100) / c.getCount();
    						bufferLenght=String.valueOf(c.getCount());
    						serial.close();
    						openDatabase();
    						
    						String whereClause="secuencial=?";
    						String[] whereArgs={String.valueOf(c.getLong(c.getColumnIndex("secuencial")))};
    						ContentValues cv_datos=new ContentValues(1);
    				    	
    				    	cv_datos.put("envio",TomaDeLecturas.ENVIADA);
    				    	
    				    	int j=db.update("lecturas", cv_datos, whereClause, whereArgs);
    						
    						closeDatabase();
    						//Marcar como enviada
    						mostrarMensaje(MENSAJE, (i+1) +" de "+bufferLenght + " Registros.\n" +String.valueOf(porcentaje)+"%");
	    						mostrarMensaje(BARRA,String.valueOf(1));
	    						
		   				}
		   				c.close();
		   				
		   				if (transmiteFotos){
		   				
			   				mostrarMensaje(PROGRESO, "Generando fotos a exportar");
			   				mostrarMensaje(MENSAJE, "Espere...");
			   				
			   				openDatabase();
			   				c= db.rawQuery("select nombre, foto , rowid from fotos where cast(envio as Integer)=" + TomaDeLecturas.NO_ENVIADA, null);
			   				
			   				
			   				
			   				cantidad=c.getCount();
			   				//closeDatabase();
			   				
			   				c.moveToFirst();
			   				
			   				for (int i=0;i<c.getCount();i++){
			   					serial.open(ls_servidor, "maxigas/lecturas/fotos", ls_categoria, Serializacion.ESCRITURA,
										0, 0, 0, "", 0, context);
			   					
			   					//ls_cadena=generaCadenaAEnviar(c);
			   					serial.write(c.getString(c.getColumnIndex("nombre")),c.getBlob(c.getColumnIndex("foto")));
			   					
			   					
			   					String bufferLenght;
	    						int porcentaje =(i*100) / c.getCount();
	    						bufferLenght=String.valueOf(c.getCount());
	    						serial.close();
	    						openDatabase();
	    						
	    						String whereClause="rowid=?";
	    						String[] whereArgs={c.getString(c.getColumnIndex("rowid"))};
	    						ContentValues cv_datos=new ContentValues(1);
	    				    	
	    						if (!transmitirTodo){
	    							cv_datos.put("envio",TomaDeLecturas.ENVIADA);
		    				    	
	    				    		int j=db.update("fotos", cv_datos, whereClause, whereArgs);
	    						}
	    						//closeDatabase();
	    						//Marcar como enviada
	    				    	c.moveToNext();
	    						mostrarMensaje(MENSAJE, (i+1) +" de "+bufferLenght + " Fotos.\n" +String.valueOf(porcentaje)+"%");
		    						mostrarMensaje(BARRA,String.valueOf(1));
			   				}
		   				}
		   				
		   				
		   				//mostrarMensaje(PROGRESO, "Mandando datos al servidor");
		   				mostrarMensaje(MENSAJE, "Espere...");
		   				//serial.close();
		   				yaAcabo=true;
		   				muere(true, "La información ha sido exportada con éxito");
		   				c.close();
		   			} catch (Throwable e) {
		   				// TODO Auto-generated catch block
		   				e.printStackTrace();
		   			  muere(true, "Ocurrió un problema al importar.\n Verifique que se encuentre conectado a internet y que el servidor se encuentre disponible. Error:"+e.getMessage());
		   			}
		   	        finally{
		   	        	closeDatabase();

		   	        	//dialog.cancel();
		   	        }
		   	     
			}
		
			
		
		};
			


		
		hilo.start();
		
	}
	
	public void recepcion(){
		final Context context= this;
		Thread hilo= new Thread(){
			int cantidad;
			
			public void run() {
				
				// TODO Auto-generated method stub
				Serializacion serial= new Serializacion(Serializacion.WIFI);
		   		 String ls_cadena="";
		   		 byte[] lby_cadena;
		   		 String [] lineas;
		   		 String [] ls_cambios;
		   		 
		   		String mPhoneNumber;

		   		 /*TelephonyManager tMgr =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		   		  String mPhoneNumber = tMgr.getLine1Number();*/
		   		 // ProgressDialog dialog = ProgressDialog.show(context, "Exportar", "Se esta exportando el archivo, espere", true);
		   		  
		   		 mostrarMensaje(MENSAJE, "Recibiendo datos.");
		   		mostrarMensaje(PROGRESO, "Espere...");
		   		int i=0;
		   		
		   		  
		   	        try {
		   	        	
		   	        	
		   	        	openDatabase();
		   	        	
		   	         /*Cursor parm= db.rawQuery("select value from params where key='telefono'", null);
			   	        
			   	        parm.moveToFirst();
			   	        
			   	     mPhoneNumber= parm.getString(parm.getColumnIndex("value"));*/
			   	        
			   	        
			   	        	//borrarArchivo( "uploads/"+ mPhoneNumber+".txt");
		   	        	
		   				
		   				
		   				//Cursor c= db.rawQuery("select id , nombre , padre , telefono, tipo, avance, total, color, orden, pordefecto, horaInicio, horaFin, porcion, unidad  from direcciones ", null);
		   				
		   				//cantidad=c.getCount();
		   				
		   				/*mHandler.post(new Runnable() {
		   		            public void run() {
		   		            	pb_progress.setMax(cantidad);
		   					}
		   				});*/
		   						   				//mostrarMensaje(PROGRESO, "Generando datos a importar");
		   				//mostrarMensaje(MENSAJE, "Espere...");
		   				serial.open(ls_servidor,  "maxigas/lecturas", ls_categoria, Serializacion.LECTURA,
								0, 0, 0, "", 0, context);
		   				
		   				lby_cadena= new byte[serial.longitudDelArchivo];
		   				serial.read(lby_cadena);
		   				ls_cadena= new String(lby_cadena);
		   				//String ls_update;
		   				
		   				
		   				if (ls_cadena.length()==0){
		   					//no se encontro el archivo
		   					muere(false, "No se encontro algun archivo exportado.");
		   				}
		   				
		   				if (ls_cadena.toUpperCase().startsWith("<HTML>")){
		   					//Error general
		   					muere(false, "Ocurrio un problema con la red, verifique que cuente con internet y vuelva a intentarlo.");
		   					
		   				}
		   				
		   				db.execSQL("delete from lecturas ");
		   				db.execSQL("delete from fotos ");

		   				
		   				//lineas= ls_cadena.split("\\r?\\n");	
		   				
		   				lineas= ls_cadena.split("\\|\\n");	
		   				
		   				tope(Integer.parseInt(String.valueOf(lineas.length)));
		   				
		   				for(i=0;i<lineas.length;i++){
		   					ls_cambios=lineas[i].split("\\t");
		   					
		   					ContentValues cv_datos=new ContentValues(11);
		   					if (ls_cambios[0].trim().equals("27")){
		   						int a=0;
		   						a++;
		   					}
		   					
		   					cv_datos.put("secuencial", ls_cambios[0].trim());
		   			    	cv_datos.put("caseta",  ls_cambios[1].trim());
		   			    	cv_datos.put("intcial",  ls_cambios[2].trim());
		   			    	cv_datos.put("nombre",  ls_cambios[3].trim());
		   			    	cv_datos.put("municipio",  ls_cambios[4].trim());
		   			    	cv_datos.put("direccion",  ls_cambios[5].trim());
		   			    	cv_datos.put("lectmin",  quitarCaracteres(ls_cambios[6].trim()) );
		   			    	cv_datos.put("lectmax",  quitarCaracteres(ls_cambios[7].trim()) );
		   			    	cv_datos.put("lectact", "");
		   			    	cv_datos.put("envio", TomaDeLecturas.ENVIADA);
		   			    	cv_datos.put("sospechosa", TomaDeLecturas.NO_SOSPECHOSA);
		   			    	
		   			    	db.insert("lecturas", null, cv_datos);
		   					
		   			    	int porcentaje =(i*100) /lineas.length;
		   			    	mostrarMensaje(MENSAJE, (i+1) +" de "+lineas.length + " Registros.\n" +String.valueOf(porcentaje)+"%");
    						mostrarMensaje(BARRA,String.valueOf(1));
		   					
		   					
		   				}
		   				
		   				mostrarMensaje(MENSAJE, "Espere...");
		   				serial.close();
		   				yaAcabo=true;
		   				muere(true, "Se han importado los datos correctamente.");
		   			} catch (Throwable e) {
		   				// TODO Auto-generated catch block
		   				e.printStackTrace();
		   				db.execSQL("delete from lecturas ");
		   			  muere(true, "Ocurrio un problema al importar.\n Verifique que se encuentre conectado a internet y que el servidor se encuentre disponible." + i + " "+ e.getMessage());
		   			}
		   	        finally{
		   	        	closeDatabase();
		   	        	
		   	        	//dialog.cancel();
		   	        }
		   	     
			}
		
			
		
		};
			


		
		hilo.start();
		
	}
	
	public String remplazaNulls(String ls_cadena){
    	ls_cadena=(ls_cadena==null?"":ls_cadena);
    	
    	return (ls_cadena.trim().equals("")?"":ls_cadena);
    }
	
	public void openDatabase(){
    	dbHelper= new DBHelper(this);
		
        db = dbHelper.getReadableDatabase();
    }
    
    public void closeDatabase(){
    	db.close();
        dbHelper.close();
    }
    
    private void borrarArchivo(String ls_ruta) throws Throwable{
		//HCG 20/07/2012 Manda los datos del wifi antes de cerrar la conexion
		String ruta, cadenaAEnviar;
		
		Hashtable params = new Hashtable();
		//params.put("cadena",cadenaAEnviar);
		params.put("ruta", ls_ruta);
		
		
		try {
			HttpMultipartRequest http = new HttpMultipartRequest(ls_servidor + "/deleteFile.php", params, "upload_field","", "text/plain", new String("").getBytes());
			byte[] response=http.send();
			//new String (response); Esta es la respuesta del servidor
			
			if (!new String(response).trim().equals("0")){
				throw new Throwable(new String(response));
			} 
			
			//Enviamos las fotos que tenemos pendientes
			//enviaFotosWifi();
			
			}catch(Throwable e){
				throw e;
			}
		
		
	}
        private void mostrarMensaje(final int tipo, final String mensaje){
		//Esta funcion manda un request para que se cambie algun elemento en patanlla
		mHandler.post(new Runnable() {
            public void run() {
            	switch(tipo){
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
    
    public void muere(boolean yaAcabo, String mensaje){
		Intent resultado =new Intent();
		resultado.putExtra("mensaje", mensaje);
		if (yaAcabo){
			setResult(Activity.RESULT_OK, resultado);
			finish();
		}
		else {
			setResult(ERROR, resultado);
			finish();
		}
			
	}
    
    public void setMensaje(String texto){
		tv_indicador.setText(texto);
	}
	
	public void setProgreso(String texto){
		tv_progreso.setText(texto);
	}
	
	public void avanzaProgreso(int avance){
		if (pb_progress.isIndeterminate()) pb_progress.setIndeterminate(false);
		pb_progress.incrementProgressBy(avance);
	}
	
	public void tope(int avance){
		if (pb_progress.isIndeterminate()) pb_progress.setIndeterminate(false);
		pb_progress.setMax(avance);
	}
	public void setAcabado(){
		yaAcabo=true;
	}
	
	
	private String generaCadenaAEnviar(Cursor c){
		String ls_cadena;
		ls_cadena= remplazaNulls(c.getString(c.getColumnIndex("secuencial")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("caseta")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("intcial")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("nombre")));
			//ls_cadena+="\t";
			//ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("tipo")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("municipio")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("direccion")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("lectmin")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("lectmax")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("lectact")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("presion")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("comentarios")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("problemas")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("sospechosa")));
			ls_cadena+="\t";
			ls_cadena+= remplazaNulls(c.getString(c.getColumnIndex("horadelectura")));
			ls_cadena+="\r\n";
			
			return ls_cadena;
	}
	
	public void preguntaArchivo(){
		AlertDialog alert;
	
		LayoutInflater inflater = this.getLayoutInflater();
		
		String ls_archivo;
    	
    	final View view=inflater.inflate(R.layout.lote_a_cargar, null);
    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
		final CopyOftrasmisionDatos slda= this;
		final String []selectionArgs ={"archivo"};
		builder.setView(view);
		
		final EditText et_archivocarga= (EditText) view.findViewById(R.id.et_archivocarga);
		
		openDatabase();
		
		
		
		Cursor c = db.rawQuery("Select value from config where key=?", selectionArgs);
		
		
		
		if (c.getCount()>0){
			c.moveToFirst();
			ls_archivo=c.getString(c.getColumnIndex("value"));
			if (ls_archivo.indexOf(".")>0){
				et_archivocarga.setText(ls_archivo.substring(0, ls_archivo.indexOf(".") - 1));
			}
			else
			{
				et_archivocarga.setText(ls_archivo);
			}
			
		}
			
		
		
		
		
		closeDatabase();
		
		builder
	       .setCancelable(false).setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id){
	        	   ls_categoria=et_archivocarga.getText().toString().trim()+".txt";
	        	   if (ls_categoria.length()==0)
	        		   mensajeVacioLote();
	        	   else{
	        		   openDatabase();
	        			
	        			
	        			
	        			Cursor c = db.rawQuery("Select value from config where key=?", selectionArgs);
	        			
	        			if (c.getCount()>0)
	        				db.execSQL("update config set value='"+ls_categoria+"' where key='archivo'");
	        			else
	        				db.execSQL("insert into config(key, value) values('archivo', '"+ls_categoria+"')");
	        			
	        			
	        			closeDatabase();
	        			recepcion();	
	        	   }
	        	   	
	        	   
	                dialog.dismiss();
	           }
	       })
	       .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id){
	        	   dialog.cancel();
	        	   mostrarAlerta=false;
	        	   muere(true, "");
	                
	           }
	       });
		
		builder.show();
		esconderTeclado(et_archivocarga);
	
	}
	
	//Elimina todo lo que necesite para que sea numero
	public long quitarCaracteres(String ls_cadena){
		String ls_numero = "", ls_caracter;
		
		for (int i=0 ; i<ls_cadena.length();i++){
			ls_caracter=ls_cadena.substring(i, i+1);
			if (esNumero(ls_caracter)){
				ls_numero+=ls_caracter;
			}
			
		}
		
		return Long.parseLong(ls_numero);
		
	}
	public boolean esNumero(String ls_cadena){
		try{
			Integer.parseInt(ls_cadena);
			return true;
		}
		catch(Throwable e){
			return false;
			
		}
	}
	
	public void mensajeVacioLote(){
		final CopyOftrasmisionDatos slda= this;
		AlertDialog.Builder message= new AlertDialog.Builder(slda);
		   message.setMessage(R.string.str_emptyField).setCancelable(false)
		   .setPositiveButton(R.string.aceptar,  new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog,
					int which) {
				
				preguntaArchivo();
			}
			   
		   });
		   AlertDialog alerta = message.create();
		   alerta.show();
	}
	
	public void esconderTeclado(View v){
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
	


}


