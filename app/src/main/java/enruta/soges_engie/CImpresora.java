package enruta.soges_engie;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;


public class CImpresora
	extends CBaseClass
{
	private String m_sURL;
	//private StreamConnection m_oConnBT;
	private boolean m_bConectado;
	//private OutputStream m_oOut;
	//private CPL m_oCPL;
	
	BluetoothAdapter mBluetoothAdapter;
	BluetoothDevice mDevice;
	BluetoothSocket m_oConnBT = null;
	InputStream is;
	DataOutputStream m_oOut;
	//dos=new DataOutputStream(socket.getOutputStream());
	//DataInputStream dis;
	
	public void openBluetooth(String sURL) throws Throwable{
		
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
             return;
     }
		mDevice=mBluetoothAdapter.getRemoteDevice(sURL);
		 Method m = mDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
		 m_oConnBT = (BluetoothSocket) m.invoke(mDevice,Integer.valueOf(1));
		 mBluetoothAdapter.cancelDiscovery();
		 m_oConnBT.connect();
		 
	}

	public CImpresora(/*CPL oCPL,*/ String sURL)
	{
		//m_oCPL = oCPL;
		//m_sURL = "btspp://" + sURL + ":1";
		m_sURL =  sURL;
		m_bConectado = false;
	}

//	public CImpresora(String sURL)
//	{
//		//m_oCPL = null;
//		m_sURL = sURL;
//		m_bConectado = false;
//	}

	protected void AbrirPuerto() throws Exception
	{
		if (m_oConnBT == null)
		{
			try
			{
				//m_oConnBT = (StreamConnection)Connector.open(m_sURL);
				openBluetooth( m_sURL);
				if (m_oConnBT == null)
				{
					m_oOut = null;
					m_bConectado = false;
//m_oCPL.log.log("No se pudo abrir el puerto");
					throw new Exception("No se pudo abrir el puerto de impresora.");
				}

				//m_oOut = m_oConnBT.openOutputStream();
				m_oOut =new DataOutputStream(m_oConnBT.getOutputStream());
				if (m_oOut == null)
				{
					m_oConnBT.close();
					m_oConnBT = null;
					m_bConectado = false;
//m_oCPL.log.log("No se pudo abrir la conexion");
					throw new Exception("No se pudo abrir la conexión BT de salida para impresora.");
				}

				m_bConectado = true;
			}
			catch (Throwable e)
			{
				m_oConnBT = null;
				m_oOut = null;
				m_bConectado = false;
//m_oCPL.log.log("Otro error X");
				throw new Exception("Error al abrir puerto::" + e.toString());
			}
		}
	}

	protected void CerrarPuerto() throws Exception
	{
		try
		{
			if (m_bConectado)
			{
				m_bConectado = false;
				if (m_oOut != null)
					m_oOut.close();
				m_oOut = null;
				if (m_oConnBT != null)
					m_oConnBT.close();
				m_oConnBT = null;
			}
			else
			{
				m_oOut = null;
				m_oConnBT = null;
			}
		}
		catch (Exception e)
		{
			m_oOut = null;
			m_oConnBT = null;
			m_bConectado = false;
			throw new Exception("Error al cerrar puerto::" + e.toString());
		}
	}

	//public void Imprimir(byte c) throws Exception
	//{
	//  int nIntentos = 0;

	//  while (nIntentos <= 2)
	//  {
	//    try
	//    {
	//      if (m_oOut != null)
	//        m_oOut.write(c);
	//      break;
	//    }
	//    catch (Exception e)
	//    {
	//      nIntentos += 1;
	//      CerrarPuerto();
	//      throw new Exception("Error al imprimir::" + e.toString());
	//    }
	//  }
	//}

	public void ImprimirComando(String s) throws Exception
	{
		byte oBuffer[];

		try
		{
			oBuffer = new byte[1];
			oBuffer[0] = '^';
			Imprimir(oBuffer);
			Imprimir(s);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	public void Imprimir(String s) throws Exception
	{
		try
		{
			Imprimir(s.getBytes());
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	public void Imprimir(byte oBuffer[]) throws Exception
	{
		int nIntentos = 0;
		String s;

		while (nIntentos <= 2)
		{
			try
			{
//m_oCPL.log.log("Voy a abrir el puerto");
				if (m_oOut == null)
					AbrirPuerto();
				if ((m_oOut != null) && (oBuffer != null))
				{
//m_oCPL.log.log("Hago el flush");
					
					
					m_oOut.write(oBuffer);
					m_oOut.flush();
					
				}
				else {
//m_oCPL.log.log("Lanzo una excepcion");
					throw new Exception("Puerto impresora sin abrir");
				}
				break;
			}
			catch (Exception e)
			{
//m_oCPL.log.log("Hago otro intento");
				nIntentos += 1;
				CerrarPuerto();
				if (nIntentos <= 2)
				{
					try
					{
						AbrirPuerto();
					}
					catch (Exception e2)
					{
						throw new Exception("Error en 'Imprimir'::" + e.toString());
					}
				}
				else
					throw new Exception("Error al imprimir::" + e.toString());
			}
		}

//		m_oCPL.log.log("Fin impresion");
	}


}


