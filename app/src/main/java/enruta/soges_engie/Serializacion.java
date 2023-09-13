package enruta.soges_engie;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Base64;

import enruta.soges_engie.entities.SubirFotoRequest;
import enruta.soges_engie.entities.SubirFotoResponse;
import enruta.soges_engie.services.WebApiManager;


public class Serializacion {
    //HCG 16/07/2012 Declaramos las variables estaticas de el tipo de comunicacion.
    public final static int BLUETOOTH = 0;
    public final static int WIFI = 1;
    public final static int SDCard = 2;

    //HCG 16/07/2012 Declaramos las variables estaticas de el modo de comunicacion.
    public final static int LECTURA = 0;
    public final static int ESCRITURA = 1;


    //HCG 16/07/2012 Declaramos las variables para el tipo de encriptado.
    public final static int SIN_ENCRIPTADO = 0;
    public final static int PANAMA = 1;
    public final static int ELECTRICARIBE = 2;

    public final static int FIN_DE_LA_COMUNICACION = 999;

    //HCG 16/07/2012 Variables de Instancia
    private int ii_tipo, ii_modo;

    //HCG 16/07/2012 Variables de Instancia para la conexion
	/*private StreamConnection conexionBluetooth;
	private HttpConnection conexionHttp;
	private FileConnection conexionArchivo;*/


    private DataInputStream iis_entrada = null;
    private DataOutputStream iis_salida = null;
    private boolean estaAbierto = false;

    private String is_servidor, is_carpeta, is_archivo;
    private long mIdEmpleado = 0;

    public boolean bCancelar = false;

    HttpMultipartRequest http;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mDevice;
    BluetoothSocket socket = null;


    //HCG 19/07/2012 Apartir de aqui presentamos variables del manejo del archivo
    private boolean EOF = true; //Indica si se ha llegado al fin del archivo
    public int longitudDelArchivo = 0; //Cantidad de bytes total en el archivo
    private int apuntador = 0; // Indica en donde me quede en el archivo
    private byte[] bytesAEnviar; //Bytes en el buffer

    /*HCG 23/07/2012 Aveces tendremos que  mandar paramatros antes de mandar la conexion de bluetooth... llevaremos un "Stack" en el orden que estas
    deben ser llamadas*/
    private int[] ii_params;


    private String Mensaje;

    //Agregamos un hashtable para las fotos
    Vector vFotos;
    Vector vNombres;

    // RL / 2023-07-24 / Se agrega el parámetro con el contexto

    private Context mContext;
    private String mSerieMedidor = "";
    private long mIdOrden = 0;

    Serializacion(int li_tipo) {
        //Le indicamos el tipo de conexion (bluetooth, wifi o sdCard)
        ii_tipo = li_tipo;
        vFotos = new Vector();
        vNombres = new Vector();
    }


    public void open(String ls_servidor, String ls_carpeta, String ls_archivo, int li_modo, int li_zip,
                     int li_encripta, long idEmpleado, String serieMedidor, long idOrden,
                     Context context) throws Throwable {
        //HCG 20/07/2012 Esta funcion es la encargada de abrir la conexion por bluetooth
        //Recibe :
        //				ls_servidor: El servidor donde esta el archivo a leer
        //				ls_carpeta: Carpeta en donde se encuentra el archivo
        //				ls_archivo: El nombre del archivo
        //				li_modo: Si sera de lectura o escritura
        //				li_zip: Si se encuentra en formato ZIP
        //				li_encripta: Si se encuentra encriptado

        if (estaAbierto)
            throw new Exception("Ya se ha abierto una conexion.");

        ii_modo = li_modo;
        is_servidor = ls_servidor;
        is_carpeta = ls_carpeta;
        is_archivo = ls_archivo;
        mIdEmpleado = idEmpleado;
        mIdOrden = idOrden;
        mSerieMedidor = serieMedidor;
        mContext = context;

        String ls_url, ls_urlConArchivo;

        if (is_carpeta.equals("")) {
            ls_urlConArchivo = is_servidor + "/" + is_archivo;
            ls_url = is_servidor;
        } else {
            ls_urlConArchivo = is_servidor + "/" + is_carpeta + "/" + is_archivo;
            ls_url = is_servidor + "/" + is_carpeta;
        }


        try {
            switch (ii_tipo) {
                case BLUETOOTH:
                    openBluetooth();
                    break;
                case SDCard:
                    openSDCard(ls_url, ls_urlConArchivo);
                    break;
                case WIFI:
                    openWifi(ls_urlConArchivo);
                    break;

            }

        } catch (Throwable e) {
            throw e;
        }
    }

    private void openBluetooth() throws Throwable {
        //HCG 16/07/2012 Esta funcion es la encargada de abrir la conexion por bluetooth

        try {
            if (ii_modo == LECTURA) {
                //conexionBluetooth.close();
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                mDevice = mBluetoothAdapter.getRemoteDevice(is_servidor);
                Method m = mDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
                socket = (BluetoothSocket) m.invoke(mDevice, Integer.valueOf(1));

                mBluetoothAdapter.cancelDiscovery();

                socket.connect();

                iis_entrada = new DataInputStream(socket.getInputStream());
                leerDatosBluetooth();


                socket.close();


            }

        } catch (Throwable e) {
            estaAbierto = false;
            EOF = true;
            throw e;
        }

        estaAbierto = true;


    }


    private void openSDCard(String ls_url, String ls_urlConArchivo) throws Throwable {
        //HCG 16/07/2012 Esta funcion es la encargada de abrir la conexion con la SDCard.
        //Recibe :
        //				ls_url: Ubicacion del archivo
        //				ls_urlConArchivo: ubicacion y nombre del archivo


        if (ii_modo == LECTURA) {
			
			/*conexionArchivo = (FileConnection) Connector.open("file:///" + ls_urlConArchivo);
			
			if(!conexionArchivo.exists()){
				estaAbierto=false;
				throw new Exception ("El archivo no existe / Ruta invalida");
			}
			
			iis_entrada=conexionArchivo.openDataInputStream();
			
			leerDatos();
			
			conexionArchivo.close();
			
			
			//EOF=false;*/

        }

        estaAbierto = true;

    }

    private void openWifi(String ls_urlConArchivo) throws Throwable {
        //HCG 16/07/2012 Esta funcion es la encargada de abrir la conexion con el WIFI o cualquier tipo de conexion a internet.
        //Recibe:
        //		ls_urlConArchivo: ubicacion y nombre del archivo


        //Cambiaremos la forma de leer los archivos por wifi, ahora, recibiremos la pagina web. Hay que estar atentos porque hay dos posibilidades
        //-Que devuelva la pagina web
        //-Que devuelva los valores realmente solicitados
        if (ii_modo == LECTURA) {
            String ruta;
            //cadenaAEnviar=new String(bytesAEnviar);
            if (is_carpeta.equals(""))
                ruta = is_archivo;
            else
                ruta = is_carpeta + "\\" + is_archivo;
            Hashtable params = new Hashtable();
            //params.put("cadena",cadenaAEnviar);
            params.put("ruta", ruta);
            params.put("carpeta", is_carpeta);
            params.put("idEmpleado", String.valueOf(mIdEmpleado));


            try {
                http = new HttpMultipartRequest(is_servidor + "/api/operaciones/getFile", params, "upload_field", "", "text/plain", new String("").getBytes());
                //http = new HttpMultipartRequest(is_servidor + "/getFile.php", params, "upload_field","", "text/plain", new String("").getBytes());
                //byte[] response=http.send();
                //new String (response); Esta es la respuesta del servidor

                bytesAEnviar = http.send();

                longitudDelArchivo = bytesAEnviar.length;

                if (longitudDelArchivo == 0) EOF = true;
                else EOF = false;


            } catch (FileNotFoundException e2) {
                estaAbierto = false;
                EOF = true;
                longitudDelArchivo = 0;

            } catch (Throwable e) {

                estaAbierto = false;
                EOF = true;
                throw e;
            }
		

		/*try{
			if (ii_modo==LECTURA){
				
				conexionHttp = (HttpConnection)Connector.open(ls_urlConArchivo);
				if (conexionHttp.getResponseCode() != HttpConnection.HTTP_OK)
		        {
					estaAbierto=false;
					throw new Exception ("El archivo no existe / Ruta invalida");
		        }
				iis_entrada=conexionHttp.openDataInputStream();
				
				leerDatos();
				
				conexionHttp.close();
				
				
				//EOF=false;
			}
		}catch(Throwable e){
			estaAbierto=false;
			EOF=true;
			throw e;*/

        }

        estaAbierto = true;
    }


    public int read(byte[] lby_cadena) throws Throwable {
        //HCG 16/07/2012 Esta funcion es la encargada de recorrer el archivo a obtener.
        //Recibe:
        //				lby_cadena: Es la cadena a escribir.
        //
        //
        //
        //Regresa:
        //				El numero de bytes leidos.

        //byte[] lby_cadena=null;

        int bytesALeer = 0, finalDeLectura, i = 0;

        if (!estaAbierto) throw new Exception("No se ha abierto algun archivo.");

        if (ii_modo != LECTURA) throw new Exception("No se encuentra en modo de lectura");

        if (lby_cadena == null) throw new Exception("Variable no inicializada");

        try {

            bytesALeer = lby_cadena.length;


            if (EOF) return -1;


            if (bytesALeer > longitudDelArchivo - apuntador) {
                finalDeLectura = longitudDelArchivo;
                EOF = true;
            } else
                finalDeLectura = bytesALeer + this.apuntador;


            for (i = 0; finalDeLectura > apuntador + i; i++) {

                lby_cadena[i] = bytesAEnviar[i + apuntador];

            }
            apuntador += i;

            if (longitudDelArchivo <= apuntador) EOF = true;


        } catch (Throwable e) {
            throw e;
        }

        return i;
    }

    public void close() throws Throwable {
        //HCG 16/07/2012 Esta funcion es cierra la conexion

        if (ii_modo == ESCRITURA && longitudDelArchivo > 0) {
            //Enviamos los datos si estamos en modo de escritura
            try {
                switch (ii_tipo) {
                    case BLUETOOTH:
                        closeBluetooth();
                        break;

                    case WIFI:
                        closeWIFI();
                        break;

                    case SDCard:
                        closeSDCard();
                        break;
                }
            } catch (Throwable e) {
                throw e;
            }
        }

        if (ii_modo == ESCRITURA && vFotos.size() > 0) {
            try {
                switch (ii_tipo) {

                    case WIFI:
                        enviaFotosWifi();
                        break;
                }
            } catch (Throwable e) {
                throw e;
            }

        }


        EOF = true;
        longitudDelArchivo = 0;
        apuntador = 0;
        bytesAEnviar = null;
        estaAbierto = false;
        vFotos.removeAllElements();
        vNombres.removeAllElements();

    }

    public void write(byte[] lby_cadena) throws Exception {
        //HCG 16/07/2012 Escribe un arreglo de bytes en el buffer
        //Recibe:
        //				lby_cadena: bytes a escribir

        if (ii_modo != ESCRITURA)
            throw new Exception("No se encuentra en modo de escritura.");

        byte[] lby_temp = new byte[longitudDelArchivo];

        if (bytesAEnviar == null) bytesAEnviar = new byte[0];

        lby_temp = bytesAEnviar;
        bytesAEnviar = new byte[lby_temp.length + lby_cadena.length];

        for (int i = 0; lby_temp.length > i; i++) bytesAEnviar[i] = lby_temp[i];

        for (int i = 0; lby_cadena.length > i; i++)
            bytesAEnviar[i + lby_temp.length] = lby_cadena[i];

        longitudDelArchivo = bytesAEnviar.length;

    }

    public void write(String ls_cadena) throws Exception {
        //HCG 16/07/2012 Escribe una cadena en el buffer
        //Recibe:
        //				ls_cadena: Cadena a escribir

        if (ii_modo != ESCRITURA)
            throw new Exception("No se encuentra en modo de escritura.");

        write(ls_cadena.getBytes());


    }

    public void writeInt(int li_param) throws Exception {
        //HCG 16/07/2012 Escribe una entero en el servidor, unicamente para bluetooth
        //Recibe:
        //				li_param: Parametro que queremos escribir
        if (ii_tipo == BLUETOOTH) {
            int[] ii_paramsTemp;
            //iv_params.addElement(new Integer(li_param));

            if (ii_params == null) {
                ii_params = new int[1];
                ii_params[0] = li_param;
            } else {
                ii_paramsTemp = new int[ii_params.length];
                for (int i = 0; i < ii_params.length; i++) ii_paramsTemp[i] = ii_params[i];

                ii_params = new int[ii_params.length + 1];
                for (int i = 0; i < ii_paramsTemp.length; i++) ii_params[i] = ii_paramsTemp[i];

                ii_params[ii_params.length - 1] = li_param;
            }
				
			/*try{
			//	conexionBluetooth.close();
				conexionBluetooth = (StreamConnection) Connector.open(is_servidor);
				iis_salida=conexionBluetooth.openDataOutputStream();
				
				iis_salida.writeInt(li_param);
				iis_salida.flush();
				
				iis_salida.close();
				conexionBluetooth.close();
				
			}catch(Throwable e){
				throw new Exception(e.getMessage());
				
			}*/
        } else {
            throw new Exception("Error al escribir entero: Verifique que se encuentre en modo bluetooth.");
        }

    }

    public boolean EOF() {
        //HCG 16/07/2012 indica que llego al final del archivo
        if (longitudDelArchivo <= apuntador) {
            EOF = true;
        }
        return EOF;
    }


    private void leerDatosBluetooth() throws Throwable {
        //HCG 09/08/2012 Esta funcion tiene el proposito de poder realizar una comunicacion  unicamente para el servidor de bluetooth
        int bytesAleer = 0, bytesRecibidos = 0, li_leidos = 0;
        StringBuffer buffer = new StringBuffer();
        if (ii_tipo == BLUETOOTH) {
            iis_salida = new DataOutputStream(socket.getOutputStream());


            //Indicamos la operacion a realizar
            //iis_salida.writeInt(LECTURA);
            iis_salida.writeInt(7); // Prueba

            //Mandamos el nombre del archivo a recibir y su tamaño
            iis_salida.writeInt(is_archivo.getBytes().length);
            iis_salida.write(is_archivo.getBytes());
            iis_salida.flush();

            //Obtenemos la cantidad de bytes que vamos a almacenar
            bytesAleer = iis_entrada.readInt();
            if (bytesAleer == 0) {
                iis_salida.close();
                iis_entrada.close();
                longitudDelArchivo = 0;
                EOF = true;
                throw new Throwable("Archivo no encontrado");
            }

            //Leemos hasta  que hayamos recibido todos los bytes
            while (bytesRecibidos != bytesAleer) {
                try {
                    li_leidos = iis_entrada.read();

                    buffer.append((char) li_leidos);
                    bytesRecibidos = buffer.length();

                } catch (Throwable e) {
                    iis_salida.close();
                    iis_entrada.close();
                    longitudDelArchivo = 0;
                    EOF = true;
                    throw e;
                }

            }

            iis_salida.writeInt(FIN_DE_LA_COMUNICACION);

            iis_salida.close();

            iis_entrada.close();

            almacenaBytesEnVariables(buffer);

        }
    }

    private void leerDatos() throws Throwable {
        //HCG 19/07/2012 Realiza la captura de los datos del servidor al buffer.
        int li_leidos;
        StringBuffer buffer = new StringBuffer();


        try {
            while ((li_leidos = iis_entrada.read()) != -1) {
                ;
                buffer.append((char) li_leidos);


                if (bCancelar) {
                    bCancelar = false;
                    throw new Throwable("Transmision cancelada");
                }
            }
        } catch (Throwable e) {
            iis_entrada.close();
            longitudDelArchivo = 0;
            EOF = true;
            throw e;
        }
        iis_entrada.close();
		
		/*bytesAEnviar=new byte[buffer.toString().getBytes().length];
		
		bytesAEnviar=buffer.toString().getBytes();
		
		longitudDelArchivo=bytesAEnviar.length;

		if (longitudDelArchivo==0 ) EOF=true;
		else EOF=false;*/

        almacenaBytesEnVariables(buffer);
    }

    public int getStringSize() {
        //HCG 19/07/2012 Obtiene la cantidad maxima de bytes que puede tener la cadena a leer
        return longitudDelArchivo;
    }

    private void closeWIFI() throws Throwable {
        //HCG 20/07/2012 Manda los datos del wifi antes de cerrar la conexion
        String ruta, cadenaAEnviar;
        cadenaAEnviar = new String(bytesAEnviar);
        if (is_carpeta.equals(""))
            ruta = is_archivo;
        else
            ruta = is_carpeta + "\\" + is_archivo;
        Hashtable params = new Hashtable();
        params.put("cadena", cadenaAEnviar);
        params.put("ruta", ruta);


        try {
            http = new HttpMultipartRequest(is_servidor + "/upload_string_and.php", params, "upload_field", "", "text/plain", new String("").getBytes());
            byte[] response = http.send();
            //new String (response); Esta es la respuesta del servidor

            if (!new String(response).trim().equals("0")) {
                throw new Throwable(new String(response));
            }

            //Enviamos las fotos que tenemos pendientes
            //enviaFotosWifi();

        } catch (Throwable e) {
            throw e;
        }


    }

    private void closeSDCard() throws Throwable {
        //HCG 20/07/2012 Manda los datos de la SDCard antes de cerrar la conexion
        String ls_urlConArchivo, ls_url;
        if (is_carpeta.equals("")) {
            ls_urlConArchivo = is_servidor + "/" + is_archivo;
            ls_url = is_servidor + "/";
        } else {
            ls_urlConArchivo = is_servidor + "/" + is_carpeta + "/" + is_archivo;
            ls_url = is_servidor + "/" + is_carpeta + "/";
        }


        //Primero verificamos que el directorio no exista
		
		
		/*conexionArchivo = (FileConnection) Connector.open("file:///" + ls_url);
		
		if(!conexionArchivo.exists()) {
			conexionArchivo.mkdir();
	     	}
		
		conexionArchivo.close();
		//Ahora verificamos que no exista el archivo
		
		
		conexionArchivo = (FileConnection) Connector.open("file:///" + ls_urlConArchivo);
		
		if(conexionArchivo.exists()) {
			conexionArchivo.delete();
	        	conexionArchivo.close();
	        	conexionArchivo = (FileConnection) Connector.open("file:///" + ls_urlConArchivo);
		}
		
		if(!conexionArchivo.exists()) {
			//ahora si lo creamos
			conexionArchivo.create();
	        	iis_salida=conexionArchivo.openDataOutputStream();
		}
	
		
		try {
			iis_salida.write(bytesAEnviar);
			iis_salida.flush();
		} catch (Throwable e) {
			throw e;
		}
		
		iis_salida.close();	
		conexionArchivo.close();*/

    }

    private void closeBluetooth() throws Throwable {
        //HCG 20/07/2012 Manda los datos del Bluetooth antes de cerrar la conexion
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevice = mBluetoothAdapter.getRemoteDevice(is_servidor);
        Method m = mDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
        socket = (BluetoothSocket) m.invoke(mDevice, Integer.valueOf(1));

        mBluetoothAdapter.cancelDiscovery();


        try {
            socket.connect();

            iis_salida = new DataOutputStream(socket.getOutputStream());

            //Indicamos la operacion a realizar
            iis_salida.writeInt(ESCRITURA);
            //iis_salida.writeInt(7); // Prueba
            iis_salida.flush();

            //Y el peso
            iis_salida.writeInt(is_archivo.length());
            iis_salida.flush();

            //Indicamos el nombre del archivo a enviar
            iis_salida.write(is_archivo.getBytes());
            iis_salida.flush();

            iis_salida.writeInt(longitudDelArchivo);
            iis_salida.write(bytesAEnviar);
            iis_salida.flush();

            iis_salida.writeInt(FIN_DE_LA_COMUNICACION);
            iis_salida.flush();
        } catch (Throwable e) {
            throw e;
        }

        iis_salida.close();
        socket.close();
    }

    public boolean isOpen() {
        //HCG 20/07/2012 Retorna si se ha abierto un archivo
        return estaAbierto;
    }


    private void almacenaBytesEnVariables(StringBuffer buffer) {
        //HCG 09/08/2012 Almacena la informacion leida del servidor en variables
        //Entrada:
        //			buffer: son los datos recibidos del servidor
        bytesAEnviar = new byte[buffer.toString().getBytes().length];

        bytesAEnviar = buffer.toString().getBytes();

        longitudDelArchivo = bytesAEnviar.length;

        if (longitudDelArchivo == 0) EOF = true;
        else EOF = false;
    }

    public void write(String nombre, byte[] raw) {
        //Funcionalidad probada unicamente por wifi
        if (ii_tipo == this.WIFI) {
            vFotos.addElement(raw);
            vNombres.addElement(nombre);
        }
    }
	
	/*private int  enviaFotosWifi() throws Throwable{
		Enumeration en_Nombres, en_Fotos;
		Hashtable params = new Hashtable();
		
		byte[] foto;
		
		en_Fotos=vFotos.elements();
		en_Nombres=vNombres.elements();
		
		//params.put("Connection", "keep-alive");
		params.put("carpeta", is_carpeta);
		byte[] response = null;
		try{
			while (en_Fotos.hasMoreElements())
			{
				
				

		        	String nombre=(String)en_Nombres.nextElement();
		        	foto=(byte[])en_Fotos.nextElement();
		        	
		        	//Creamos una instancia del objeto que se conectara con el archivo en PHP
					HttpMultipartRequest http = new HttpMultipartRequest(is_servidor + "/upload_imagen.php", params, "upload_field",nombre, "image/jpg", foto);
					//Mandamos el archivo y esa variable response nos ayudara a obtener el estado de la carga del archivo
					response=http.send();
					
					if (!new String(response).trim().equals("0")){
						throw new Throwable(new String(response));
					}
		        }
			} catch (Exception e) {
				throw e;
			}
	        
	        return 0;
			
		}*/

    private int enviaFotosWifi() throws Throwable {
        Enumeration en_Nombres, en_Fotos;
        Hashtable params;
        SubirFotoResponse respFoto;

        byte[] foto;

        String ls_foto, ls_urlConArchivo, ls_url;
		String nombre;

        en_Fotos = vFotos.elements();
        en_Nombres = vNombres.elements();

        //params.put("Connection", "keep-alive");

        byte[] response = null;
        try {
            while (en_Fotos.hasMoreElements()) {
                params = new Hashtable();

				try {
					nombre = (String) en_Nombres.nextElement();
					foto = (byte[]) en_Fotos.nextElement();
				} catch (Throwable e) {
					e.printStackTrace();
					foto = null;
					nombre = "";
				}

                ls_foto = Base64.encodeToString(foto, Base64.DEFAULT);
                if (is_carpeta.equals("")) {
                    ls_urlConArchivo = "/" + is_archivo;
                    ls_url = "";
                } else {
                    ls_urlConArchivo = "/" + is_carpeta + "/" + nombre;
                    ls_url = is_carpeta;
                }
                params.put("ruta", ls_urlConArchivo);
                params.put("cadena", ls_foto);
                params.put("carpeta", "/" + is_carpeta);

                SubirFotoRequest req = new SubirFotoRequest();

                //Creamos una instancia del objeto que se conectara con el archivo en PHP
                //HttpMultipartRequest http = new HttpMultipartRequest(is_servidor + "/upload_imagebytes.php", params, "upload_field", nombre, "image/jpg", foto);

                //Prueba para ver la forma de sustituir el código PHP por llamadas a WebApis.
                //HttpMultipartRequest http = new HttpMultipartRequest(is_servidor + "/api/operaciones/SubirFoto", params, "upload_field", nombre, "image/jpg", foto);

                //Mandamos el archivo y esa variable response nos ayudara a obtener el estado de la carga del archivo
//                response = http.send();
//
//                if (!new String(response).trim().equals("0")) {
//                    throw new Throwable(new String(response));
//                }

                req.carpeta = "/" + is_carpeta;
                req.ruta = ls_urlConArchivo;
                req.nombre = nombre;

                respFoto = WebApiManager.getInstance(mContext).subirFoto(req, foto);

                if (respFoto == null)
                    throw new Exception("Error al enviar la foto");

                if (respFoto.NumError > 0)
                    throw new Exception("Error al enviar la foto. " + respFoto.Mensaje);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return 0;

    }

    public void cancelar() {
        bCancelar = true;
        http.bCancelar = true;
		
		/*switch(ii_tipo){
		
		case WIFI:
			if (http!=null)
				http.cancelar();
			
			break;
		}*/

    }

    void mandarSQL(String sql, String db, String usuario, String password) throws Throwable {
        //HCG 20/07/2012 Manda los datos del wifi antes de cerrar la conexion
        String ruta, cadenaAEnviar;
//		cadenaAEnviar=new String(bytesAEnviar);
        if (is_carpeta.equals(""))
            ruta = is_archivo;
        else
            ruta = is_carpeta + "\\" + is_archivo;
        Hashtable params = new Hashtable();
        params.put("cadena", sql);
//		params.put("ruta", ruta);
//		params.put("usuario",/*"u1003479_hcasta"*/ "u1003479_pruebas");
//		params.put("db",/*"db1003479_reportes"*/ "db1003479_prueba");
////		params.put("usuario","u1003479_hcasta");
////		params.put("db","db1003479_reportes");
//		params.put("password","Sotixe_69");


        params.put("usuario",/*"u1003479_hcasta"*/ usuario);
        params.put("db",/*"db1003479_reportes"*/ db);
        params.put("password", password);


        try {
            http = new HttpMultipartRequest(is_servidor + "/escribeSQL.php", params, "upload_field", "", "text/plain", new String("").getBytes());
            byte[] response = http.send();
            //new String (response); Esta es la respuesta del servidor

            if (!new String(response).trim().equals("")) {
                throw new Throwable(new String(response));
            }

            //Enviamos las fotos que tenemos pendientes
            //enviaFotosWifi();

        } catch (Throwable e) {
            throw e;
        }


    }

}
