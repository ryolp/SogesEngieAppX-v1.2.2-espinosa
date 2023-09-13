package enruta.soges_engie;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Vector;

import enruta.soges_engie.R;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


public class trasmisionDatosBt extends TransmisionesPadre {

	
	BluetoothAdapter mBluetoothAdapter;
	BluetoothDevice mDevice;
	BluetoothSocket socket = null;
	InputStream is;
	DataOutputStream dos;
	DataInputStream dis;
	 
	 
	static final int OPCION_PROBAR_CONEXION 	= 0;
	static final int OPCION_PC_A_CPL_MEDIDORES 	= 8; //Lo cambiamos para que acepte el encoding, 1 Para nokia
	static final int OPCION_CPL_A_PC_MEDIDORES 	= 2;
	static final int OPCION_CPL_A_PC_MENSAJES 	= 3;
	static final int OPCION_CPL_A_PC_NOREGISTRADOS 	= 4;
	static final int OPCION_CPL_A_PC_FOTOS	 	= 7; //Hay que cambiarlo a 7(Fotos de cpl a pc android), ya que esta rutina en nokia no es compatible, 5 para nokia
	static final int OPCION_FIN_DE_COMUNICACION 	= 999;
	
//	Globales globales;
	//int id;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	        setContentView(R.layout.enproceso);
	        String [] ls_params;
	        
	        globales = ((Globales) getApplicationContext());
	        	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        	
	        	 if (mBluetoothAdapter == null) {
                    muere(true,getString(R.string.msj_trans_bt_not_found));
                     return;
             }

             if (!mBluetoothAdapter.isEnabled()) {
            	 
            	 
            	 muere(true,getString(R.string.msj_trans_bt_off));
            	 
                     
                    /* if (!mBluetoothAdapter.isEnabled()) {
                    	    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    	    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    	}*/
                     
                     return;
             }
	        
	        
	        tll =new TodasLasLecturas(this, false);
	        resources= this.getResources();
	       bu_params= getIntent().getExtras();
	       
	       try{
	    	   transmiteFotos=bu_params.getBoolean("transmiteFotos");
	       }
	       catch(Throwable e){}
	      
	       
	       openDatabase();
	       
	       //Tomamos el servidor desde la pantalla de configuracion
	       Cursor c=db.rawQuery("Select value from config where key='mac_bluetooth'", null);
			 c.moveToFirst();
			 
			  if (!validaCampoDeConfig(c, String.format(getString(R.string.msj_config_no_disponible),getString(R.string.info_macBluetooth) , getString(R.string.str_configuracion),getString(R.string.info_macBluetooth))))
				  return;
			  
			 ls_servidor=c.getString(c.getColumnIndex("value"));
			   
			   c.close();
			   //Ahora vamos a ver que archivo es el que vamos a recibir... para nicaragua es el clp + la extension
			   //Lo vamos a guardar en categoria, Asi le llamamos a los archivos desde "SuperLibretaDeDirecciones" 2013 (c) ;)
			   
			   c=db.rawQuery("Select value from config where key='cpl'", null);
			   
			   c.moveToFirst();
			   if (!validaCampoDeConfig(c, String.format(getString(R.string.msj_config_no_disponible),getString(R.string.info_CPL) , getString(R.string.str_configuracion),getString(R.string.info_CPL))))
					  return;
				  
			   ls_categoria=c.getString(c.getColumnIndex("value")) +"." + ls_extension;
			   
			   c.close();
			   
			   //Por ultimo la ruta de descarga... Como es un servidor web, hay que quitarle el C:\... mejor empezamos desde lo que sigue. De cualquier manera, deberá tener el siguiente formato
			   //Ruta de descarga.subtr(3) + Entrada  + \ + lote 
			   c=db.rawQuery("Select value from config where key='ruta_descarga'", null);
			   c.moveToFirst();
			   if (!validaCampoDeConfig(c, String.format(getString(R.string.msj_config_no_disponible),getString(R.string.info_rutaDescarga) , getString(R.string.str_configuracion),getString(R.string.info_rutaDescarga))))
					  return;
			   
			   ls_carpeta=c.getString(c.getColumnIndex("value")) ;
			   
			   
			   c.close();
			   
			   if (ls_carpeta.endsWith("\\"))
				   ls_carpeta= ls_carpeta.substring(0, ls_carpeta.length() -1);
			   
			   
			   ls_carpeta+=bu_params.getInt("tipo")==TRANSMISION?"Entrada":"Salida";
			   
			   c=db.rawQuery("Select value from config where key='lote'", null);
			   c.moveToFirst();
			   if (!validaCampoDeConfig(c, String.format(getString(R.string.msj_config_no_disponible),getString(R.string.info_lote) , getString(R.string.str_configuracion),getString(R.string.info_lote))))
					  return;
			   
			   ls_subCarpeta=  c.getString(c.getColumnIndex("value"));
			   
			   ls_carpeta+="\\" + ls_subCarpeta ;
			   
			   c.close();

	       
	       closeDatabase();
	       
	        
	        tv_progreso = (TextView) findViewById(R.id.ep_tv_progreso);
			tv_indicador = (TextView) findViewById(R.id.ep_tv_indicador);
			pb_progress = (ProgressBar) findViewById(R.id.ep_gauge);
			
			mHandler=new Handler();
			
			
			seleccion();
			
	}
	
	public void openBluetooth() throws Throwable{
		mDevice=mBluetoothAdapter.getRemoteDevice(ls_servidor);
		 Method m = mDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
		 socket = (BluetoothSocket) m.invoke(mDevice,Integer.valueOf(1));
		 mBluetoothAdapter.cancelDiscovery();
		 socket.connect();
		 
	}
	
	public void closeBluetooth() throws Throwable{
		socket.close();
	}
	
	public void transmitir(){
		final trasmisionDatosBt context= this;
		
		
	
		hilo= new Thread(){
			int cantidad;
			 byte[] bytesAEnviar;
			
			public void run() {
				
				// TODO Auto-generated method stub
			
		   		 String ls_cadena="";
		   		 byte[] lby_registro, lby_cadenaEnBytes;
		   		String ls_nombre_final;
		   		
		   		
		   		switch (globales.modoDeCierreDeLecturas){
				case Globales.FORZADO:
					puedoCerrar = false;
					mostrarMensaje(PROGRESO, getString(R.string.msj_trans_forzando));
					// Abrimos el arreglo de todas las lecturas y forzamos
					tll.forzarLecturas();
					cancelar = false;
					break;
				}
//		   		puedoCerrar=false;
//		   		mostrarMensaje(PROGRESO, getString(R.string.msj_trans_forzando));
//		   		//Abrimos el arreglo de todas las lecturas y forzamos
//		   		tll.forzarLecturas();
//		   		cancelar=false;

		   		mostrarMensaje(PROGRESO, getString(R.string.str_espere));
		   		Cursor c = null;
		   	        try {
		   	        	openBluetooth();
		   	        	openDatabase();
		   	        	
		   	        	dos=new DataOutputStream(socket.getOutputStream());

		   	        	dos.writeInt(OPCION_CPL_A_PC_MEDIDORES);
						dos.flush();
						
						//Obtenemos el encabezado
						
						c=db.rawQuery("Select registro from encabezado", null);
						
						c.moveToFirst();
						bytesAEnviar=c.getBlob(c.getColumnIndex("registro"));
						c.close();
						
						
		   	        	
		   	        	
	   	        		c= db.rawQuery("select " + globales.tlc.is_camposDeSalida + " as TextoSalida from Ruta ", null);
		   				
		   				
		   				cantidad=c.getCount();
		   				
		   				//Escribimos la cantidad de medidores a enviar
		   				dos.writeInt(cantidad+1);
		   				//Ahora el encabezado
						dos.writeInt(bytesAEnviar.length);
						dos.flush();
						dos.write(bytesAEnviar);
						dos.flush();
		   				
		   				
		   				mHandler.post(new Runnable() {
		   		            public void run() {
		   		            	pb_progress.setMax(cantidad);
		   					}
		   				});
		   				
		   				mostrarMensaje(PROGRESO,  getString(R.string.msj_trans_generando));
		   				mostrarMensaje(MENSAJE,  getString(R.string.str_espere));
		   				

		   					
		   					for (int i=0;i<cantidad;i++){
		   						context.stop();
			   					c.moveToPosition(i);

			   					
			   					String ls_cadenaAEnviar=c.getString(c.getColumnIndex("TextoSalida"));
			   					if (ls_cadenaAEnviar.length()>globales.tdlg.long_registro)
			   						ls_cadenaAEnviar= ls_cadenaAEnviar.substring(0, globales.tdlg.long_registro);

			   					//Obtenemos los bytes a enviar
			   					bytesAEnviar=(ls_cadenaAEnviar + "\r\n").getBytes("ISO-8859-1");
			   					
			   					//Escribimos linea una por una
			   					dos.writeInt(bytesAEnviar.length);
								dos.flush();
								dos.write(bytesAEnviar);
								dos.flush();
								
			   					
			   					
			   					String bufferLenght;
	    						int porcentaje =((i+1)*100) / c.getCount();
	    						bufferLenght=String.valueOf(c.getCount());
	    						

	    						//Marcar como enviada
	    						mostrarMensaje(MENSAJE, (i+1) +" "+ getString(R.string.de)+" "+bufferLenght + " "+ getString(R.string.registros)+".\n" +String.valueOf(porcentaje)+"%");
		    						mostrarMensaje(BARRA,String.valueOf(1));
		    						
		    						
			   				}


		   				c.close();
		   				
		   				dos.writeInt(OPCION_FIN_DE_COMUNICACION);
						dos.flush();

		   				
		   				
		   			//Aqui enviamos los no registrados
		   			mostrarMensaje(PROGRESO,  getString(R.string.msj_trans_generando_no_registrados));
		   				mostrarMensaje(MENSAJE, getString(R.string.str_espere));
		   				mostrarMensaje(BARRA,String.valueOf(0));
		   				
		   				
		   				
		   				c= db.rawQuery("select * from NoRegistrados ", null);
		   				
		   				cantidad=c.getCount();
		   				//Enviamos cantidad de no registrados
		   				
		   				if (cantidad>0){
		   					
		   					dos.writeInt(OPCION_CPL_A_PC_NOREGISTRADOS);
							dos.flush();
		   					
							dos.writeInt(cantidad);
							dos.flush();
		   				
			   				

		   					
		   					mHandler.post(new Runnable() {
			   		            public void run() {
			   		            	pb_progress.setMax(cantidad);
			   					}
			   				});
		   					for (int i=0;i<cantidad;i++){
		   						context.stop();
			   					c.moveToPosition(i);

			   					
			   					bytesAEnviar=(new String(c.getBlob(c.getColumnIndex("poliza"))) + "\r\n").getBytes("ISO-8859-1");
			   					
			   					//Escribimos linea una por una
			   					dos.writeInt(bytesAEnviar.length);
								dos.flush();
								dos.write(bytesAEnviar);
								dos.flush();
			   					
			   					
			   					
			   					String bufferLenght;
	    						int porcentaje =((i+1)*100) / c.getCount();
	    						bufferLenght=String.valueOf(c.getCount());

	    						mostrarMensaje(MENSAJE, (i+1) +" "+ getString(R.string.de)+" "+bufferLenght + " "+ getString(R.string.registros)+"\n" +String.valueOf(porcentaje)+"%");
		    						mostrarMensaje(BARRA,String.valueOf(1));
		    						
		    						
			   				}

		   				c.close();
		   				
		   				dos.writeInt(OPCION_FIN_DE_COMUNICACION);
						dos.flush();

			   				
		   				}
		   				
		   				
		   				//Mandamos las fotos
		   				
		   				mostrarMensaje(BARRA,String.valueOf(0));
		   				transmiteFotos=true;
		   				if (transmiteFotos){
		   				
			   				mostrarMensaje(PROGRESO,  getString(R.string.msj_trans_generando_fotos));
			   				mostrarMensaje(MENSAJE,  getString(R.string.str_espere));
			   				
			   				dos.writeInt(OPCION_CPL_A_PC_FOTOS);
			   				dos.flush();
			   				
			   				openDatabase();
			   				//c= db.rawQuery("select nombre, foto , rowid from fotos where cast(envio as Integer)=" + TomaDeLecturas.NO_ENVIADA, null);
			   				c= db.rawQuery("select nombre, foto , rowid from fotos", null);
			   				
			   				
			   				
			   				cantidad=c.getCount();
			   				
			   				dos.writeInt(cantidad);
			   				dos.flush();
			   				
			   				mHandler.post(new Runnable() {
			   		            public void run() {
			   		            	pb_progress.setMax(cantidad);
			   					}
			   				});
			   				
			   				
			   				
			   							   				
			   				c.moveToFirst();
			   				
			   				for (int i=0;i<c.getCount();i++){
			   					context.stop();
			   					
//			   					bytesAEnviar=(c.getString(c.getColumnIndex("nombre")) +
//			   							new String( c.getBlob(c.getColumnIndex("foto"))) + "\r\n").getBytes();
			   					
			   					//bytesAEnviar=Base64.encode(bytesAEnviar, Base64.DEFAULT);
			   					byte[] fotoEnBytes=Base64.encode(c.getBlob(c.getColumnIndex("foto")), Base64.DEFAULT);
			   					byte[] bytesAEnviar=combineArrays((" " +c.getString(c.getColumnIndex("nombre")).toUpperCase()).getBytes(),fotoEnBytes);
			   					
			   					dos.writeInt(bytesAEnviar.length );
			   					dos.flush();
			   					//Thread.sleep(500);
			   					
			   					//dos.write(c.getString(c.getColumnIndex("nombre")).getBytes());
			   					//dos.write(/*Base64.encode(c.getBlob(c.getColumnIndex("foto")), Base64.DEFAULT)*/fotoEnBytes);
			   					//dos.write(bytesAEnviar);
			   					//dos.flush();
			   					//Thread.sleep(500);
			   					
			   					int offset = 0;
			   					//int cantidad = 500;
			   					int cantidad = 2000;
			   					while (offset < bytesAEnviar.length){
			   						if (offset + cantidad > bytesAEnviar.length)
			   							cantidad = bytesAEnviar.length - offset;
			   						//else cantidad = 500;
			   						else cantidad = 2000;
			   						dos.write(bytesAEnviar, offset, cantidad);
			   						dos.flush();
			   						offset += cantidad;
			   						try{
			   							//sleep(100);
			   							sleep(100);
			   						}catch(InterruptedException ie){}
			   					}
			   					
			   					//Prueba
			   					mostrarMensaje(PROGRESO, getString(R.string.msj_trans_generando_fotos) +" ("+getString(R.string.msj_trans_bytes_sent)+" " +(c.getString(c.getColumnIndex("nombre")).length() + fotoEnBytes.length)+")");
			   					
			   					String bufferLenght;
	    						int porcentaje =((i+1)*100) / c.getCount();
	    						bufferLenght=String.valueOf(c.getCount());

	    						String whereClause="rowid=?";
	    						String[] whereArgs={c.getString(c.getColumnIndex("rowid"))};

	    						//closeDatabase();
	    						//Marcar como enviada
	    				    	c.moveToNext();
	    						mostrarMensaje(MENSAJE, (i+1) +" "+getString(R.string.registros)+" "+bufferLenght + " "+getString(R.string.str_fotos)+".\n" +String.valueOf(porcentaje)+"%");
		    						mostrarMensaje(BARRA,String.valueOf(1));
			   				}
		   				}
		   				
		   				dos.writeInt(OPCION_FIN_DE_COMUNICACION);
						dos.flush();
		   				
						
						dos.writeInt(OPCION_FIN_DE_COMUNICACION);
						dos.flush();
		   				

		   				
		   				
		   				//mostrarMensaje(PROGRESO, "Mandando datos al servidor");
		   				mostrarMensaje(MENSAJE, getString(R.string.str_espere));
		   				//serial.close();
		   				yaAcabo=true;
		   				marcarComoDescargada();
		   				muere(true, String.format(getString(R.string.msj_trans_correcta), getString(R.string.str_exportado)));
		   				c.close();
		   			} catch (Throwable e) {
		   				// TODO Auto-generated catch block
		   				e.printStackTrace();
		   			  muere(true, String.format(getString(R.string.msj_trans_error), getString(R.string.str_exportar_lowercase))+e.getMessage());
		   			}
		   	        finally{
		   	        	closeDatabase();
		   	        	try {
		   	        		dos.close();
							closeBluetooth();
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

		   	        	//dialog.cancel();
		   	        }
		   	     
			}
		
			
		
		};
			


		
		hilo.start();
		
	}
	
	public void recepcion(){
		final trasmisionDatosBt context= this;
		puedoCerrar=false;
		hilo= new Thread(){
			int cantidad;
			String ls_linea;
			
			public void run() {
				int secuenciaReal=0;
				// TODO Auto-generated method stub
		   		 String ls_cadena="";
		   		 byte[] lby_cadena;
		   		 String [] lineas;
		   		 String [] ls_cambios;
		   		int totalDeMedidoresALeer = 0;
		   		byte[] datos = new byte[10];
		   		 
		   		 
		   		  
		   		 mostrarMensaje(MENSAJE, getString(R.string.msj_trans_recibiendo));
		   		mostrarMensaje(PROGRESO, getString(R.string.str_espere));
		   		int i=0, nStatusDeRecepcion;
		   		
		   		
		   		
		   		  
		   	        try {
		   	        	openBluetooth();
		   	        	dis=new DataInputStream(socket.getInputStream());
		   	        	dos=new DataOutputStream(socket.getOutputStream());
		   	        	
		   	        	
		   	        	openDatabase();
		   	        	
		   	        	
		   	        	//Indicamos la operacion a realizar
		   	        	dos.writeInt(OPCION_PC_A_CPL_MEDIDORES);
		   	        	dos.flush();
		   	        	
		   	        	//Total de medidores a leer?
		   	        	dos.writeInt(1);
		   	        	dos.flush();
		   	        	
		   	        	//Mandamos nombre del archivo a recibir, unicamente para nicaragua, despues nos preocupmos por lo demas
		   	        	lby_cadena= (globales.letraPais + ls_carpeta +"\\" + ls_categoria+Main.obtieneFecha("d/m/y  h:i:s")).getBytes();
		   	        	
		   	        	
		   	        	dos.writeInt(lby_cadena.length);
		   	        	dos.flush();
		   	        	
		   	        	dos.write(lby_cadena);
		   	        	dos.flush();
		   	        	
		   	        	dos.writeInt(OPCION_FIN_DE_COMUNICACION);
		   	        	dos.flush();

		   				context.stop();
		   				//lby_cadena= new byte[context.getResources().getInteger(R.integer.LONG_DATOS_MEDIDOR)];
		   				
		   				
		   				
		   				totalDeMedidoresALeer = dis.read(datos);
		   				totalDeMedidoresALeer = datos[0]*50;
		   				totalDeMedidoresALeer += datos[1];
		   				
		   				nStatusDeRecepcion = totalDeMedidoresALeer;
		   				if  	 (totalDeMedidoresALeer == 0) {
		   				}else if (totalDeMedidoresALeer == 5000) {
		   				}else if (totalDeMedidoresALeer == 6000){
		   					}
		   				else{
		   				}
		   				
		   				
		   				vLecturas=new Vector<String>();

		   				
		   				tope(Integer.parseInt(String.valueOf(totalDeMedidoresALeer)));
		   				
		   				//db.execSQL("delete from Lecturas ");
		   				
		   				/*db.execSQL("delete from ruta ");
		   				db.execSQL("delete from fotos ");
		   				db.execSQL("delete from Anomalia ");
		   				db.execSQL("delete from encabezado ");
		   				db.execSQL("delete from NoRegistrados ");*/
		   				
		   			 borrarRuta(db);
		   				//serial.close();
		   				
		   				db.beginTransaction();
		   				for ( i=0; i<totalDeMedidoresALeer;i++){
		   					context.stop();

		   					//lby_cadena=new byte[globales.tdlg.long_registro];
		   					lby_cadena=new byte[dis.readInt()];
		   					dis.read(lby_cadena);
		   					//Eliminamos la basura...
		   					//dis.read(new byte[2]);
		   					
		   					//lby_cadena= UByte(lby_cadena);
		   					lby_cadena=Base64.decode(lby_cadena, Base64.DEFAULT);
		   					ls_linea= new String (lby_cadena, "ISO-8859-1");
		   					ls_linea=ls_linea.substring(0, ls_linea.length()-2);

		   						
		   					

			   				if (ls_linea.length()!=globales.tdlg.long_registro){
			   					//db.setTransactionSuccessful();
			   					db.endTransaction();
			   					//db.execSQL("delete from Lecturas ");
			   					closeDatabase();
			   					dis.close();
			   					dos.close();
			   					closeBluetooth();
			   					algunError=true;
			   					muere(false, getString(R.string.msj_trans_file_not_found));
			   					
			   				}
			   				
			   				//Agregamos mientras verificamos...
			   				//vLecturas.add(ls_cadena);
			   				//db.execSQL("Insert into lecturas(registro) values ('"+ls_linea+"')");
			   				if (i!=0 && !ls_linea.startsWith("#") && !ls_linea.startsWith("!")){
			   					secuenciaReal++;
			   					globales.tlc.byteToBD(db,ls_linea/*.getBytes("US-ASCII")*/, secuenciaReal);
			   					//new Lectura(context, lby_cadena, db);
			   				}
			   				else if(ls_linea.startsWith("#")){//Esto indica que es una anomalia
			   					new Anomalia(context, /*lby_cadena*/ls_linea/*.getBytes("US-ASCII")*/, db);
			   				}
			   				else if (ls_linea.startsWith("!")){ //un usuario
			   					new Usuario(context, /*lby_cadena*/ls_linea/*.getBytes("US-ASCII")*/, db);
			   				}
			   				else if(i==0){
			   					//la primera
			   					ContentValues cv= new ContentValues();
			   					cv.put("registro", ls_linea.getBytes());
			   					
			   					db.insert("encabezado", null, cv);
			   				}
			   				
		   					
			   				
			   				int porcentaje =(i*100) /totalDeMedidoresALeer;
		   			    	mostrarMensaje(MENSAJE, (i+1) +" "+getString(R.string.de)+" "+totalDeMedidoresALeer + " "+getString(R.string.registros)+".\n" +String.valueOf(porcentaje)+"%");
    						mostrarMensaje(BARRA,String.valueOf(1));
    						dos.writeInt(OPCION_FIN_DE_COMUNICACION);
    						dos.flush();
			   				
		   					
		   				}
		   				
		   				//Una vez verificado que todos los registros fueron recibidos ahora si tenemos la seguridad de borrar
		   				
		   				
		   				//Una vez que borramos insertamos cada uno de los registros recibidos
		   			
		   				//for(String ls_lectura:vLecturas)
		   				
		   				if (!algunError)
		   					db.setTransactionSuccessful();	
		   				
		   				
		   				mostrarMensaje(MENSAJE, getString(R.string.str_espere));
		   				yaAcabo=true;
		   				
		   				//marcarComoDescargada();
		   				
		   				muere(true, String.format(getString(R.string.msj_trans_correcta), getString(R.string.str_importado)));
		   			} catch (Throwable e) {
		   				// TODO Auto-generated catch block
		   				e.printStackTrace();
		   				//db.endTransaction();
		   				//db.execSQL("delete from lecturas ");
		   			  muere(true, String.format(getString(R.string.msj_trans_error), getString(R.string.str_importar_lowercase)) + i + " "+ e.getMessage());
		   			}
		   	        finally{
		   	        	try{
		   	        		db.endTransaction();
		   	        	}catch(Throwable e){
		   	        		
		   	        	}
		   	        	
		   	        	try{
		   	        		dos.close();
		   	        		dis.close();
		   	        		closeBluetooth();
		   	        	}catch(Throwable e){
		   	        		
		   	        	}
		   				
		   				
		   	        	closeDatabase();
		   	        	
		   	        	
		   	        	//dialog.cancel();
		   	        }
		   	     
			}
		
			
		
		};
			


		
		hilo.start();
		
	}

	public void recepcion2()
	{

	}
	
	public String remplazaNulls(String ls_cadena){
    	ls_cadena=(ls_cadena==null?"":ls_cadena);
    	
    	return (ls_cadena.trim().equals("")?"":ls_cadena);
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
    
    
    
    public void setMensaje(String texto){
		tv_indicador.setText(texto);
	}
	
	public void setProgreso(String texto){
		tv_progreso.setText(texto);
	}
	
	public void avanzaProgreso(int avance){
		if (pb_progress.isIndeterminate()) pb_progress.setIndeterminate(false);
		
		if (avance>0)
			pb_progress.incrementProgressBy(avance);
		else
		{
			pb_progress.setProgress(0);
			pb_progress.setIndeterminate(true);
			
		}
	}
	
	public void tope(int avance){
		if (pb_progress.isIndeterminate()) pb_progress.setIndeterminate(false);
		pb_progress.setMax(avance);
	}
	public void setAcabado(){
		yaAcabo=true;
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
	
	public void preguntaArchivo(){
		AlertDialog alert;
	
		LayoutInflater inflater = this.getLayoutInflater();
		
		String ls_archivo;
    	
    	final View view=inflater.inflate(R.layout.lote_a_cargar, null);
    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
		final trasmisionDatosBt slda= this;
		final String []selectionArgs ={"archivo"};
		builder.setView(view);
		
		final EditText et_archivocarga= (EditText) view.findViewById(R.id.et_archivocarga);
		
		openDatabase();
		
		
		
		Cursor c = db.rawQuery("Select value from config where key=?", selectionArgs);
		
		
		
		if (c.getCount()>0){
			c.moveToFirst();
			ls_archivo=c.getString(c.getColumnIndex("value"));
			if (ls_archivo.indexOf(".")>0){
				et_archivocarga.setText(ls_archivo.substring(0, ls_archivo.indexOf(".") ));
			}
			else
			{
				et_archivocarga.setText(ls_archivo);
			}
			
		}
		/*else{
			et_archivocarga.setText("cpl001");
		}*/
			
		
		
		
		
		closeDatabase();
		
		builder
	       .setCancelable(false).setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id){
	        	   ls_categoria=et_archivocarga.getText().toString().trim()+"."+ls_extension;
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
	
	public String quitaComillas(String ls_candena){
		return ls_candena.replace("\"", "");
	}
	
	public void mensajeVacioLote(){
		final trasmisionDatosBt slda= this;
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
	
	
	
	
	public void cancelar(){
		
	}
	
	
	public String subirDirectorio(String ls_carpeta, int cuantos){
		String ls_discoDuro="";
		
		if (ls_carpeta.endsWith("\\"))
			ls_carpeta= ls_carpeta.substring(0,ls_carpeta.length()  );
		
		if (ls_carpeta.indexOf(":")>=0){
			ls_discoDuro=ls_carpeta.substring(0, ls_carpeta.indexOf(":") +1) ;
			ls_carpeta= ls_carpeta.substring(ls_carpeta.indexOf(":") +2);
		   }
		
		for (int i=0;i<cuantos;i++){
			if (ls_carpeta.lastIndexOf("\\")>=0)
				ls_carpeta= ls_carpeta.substring(0, ls_carpeta.lastIndexOf("\\") );
		}
		
		return ls_discoDuro + ls_carpeta;
		
		
		
		
	}
	
	
	private byte[] UByte(byte[] bs){
		byte b;
		for(int i=0; i< bs.length;i++){
			b= bs[i];
			if(b<0){
				// if negative
				b=(byte) ( (b & 0x7F) + 128 );
				bs[i]=b;
			}
	            
		}
		
		return bs;
        
    }
	
	private  byte[] combineArrays(byte[] one, byte[] two){
		byte[] combined = new byte[one.length + two.length];

		for (int i = 0; i < combined.length; ++i)
		{
		    combined[i] = i < one.length ? one[i] : two[i - one.length];
		}
		
		return combined;
	}
	


}


