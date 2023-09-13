package enruta.soges_engie;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;


public class HttpMultipartRequest
{
	
	HttpURLConnection connection= null;
	
	boolean connected=false;
	boolean error=false;
	boolean bCancelar=false;
	String ls_errorMsg="";
	int ii_timeOut=1, ii_segundos=0;
	 
	InputStream is = null;

	ByteArrayOutputStream bos =null;
	
	//static final String BOUNDARY = "----------V2ymHFg03ehbqgZCaKO6jy";
	static final String BOUNDARY = "*****";
	
	static final String twoHyphens = "--";
	static final String lineEnd = "\r\n";
 
	byte[] postBytes = null;
	String url = null;
	byte[] res ;
 
	public HttpMultipartRequest(String url, Hashtable params, String fileField, String fileName, String fileType, byte[] fileBytes) throws Exception
	{
		this.url = url;
 
		String boundary = getBoundaryString();
 
		String boundaryMessage = getBoundaryMessage(boundary, params, fileField, fileName, fileType);
 
		String endBoundary = "\r\n--" + boundary + "--\r\n";
		//String endBoundary = "\r\n";
 
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 
		bos.write(boundaryMessage.getBytes());
		
 
		bos.write(fileBytes);
 
		bos.write(endBoundary.getBytes());
 
		this.postBytes = bos.toByteArray();
 
		bos.close();
	}
 
	String getBoundaryString()
	{
		return BOUNDARY;
	}
 
	String getBoundaryMessage(String boundary, Hashtable params, String fileField, String fileName, String fileType)
	{
		StringBuffer res = new StringBuffer("--").append(boundary).append("\r\n");
		//res = new StringBuffer();
 
		Enumeration keys = params.keys();
 
		while(keys.hasMoreElements())
		{
			String key = (String)keys.nextElement();
			String value = (String)params.get(key);
 
			res.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n")    
				.append("\r\n").append(value).append("\r\n")
				.append("--").append(boundary).append("\r\n");
		}
		res.append("Content-Disposition: form-data; name=\"").append(fileField).append("\"; filename=\"").append(fileName).append("\"\r\n") 
			.append("Content-Type: ").append(fileType).append("\r\n\r\n");
		
		/*while(keys.hasMoreElements())
		{
			String key = (String)keys.nextElement();
			String value = (String)params.get(key);
 
			if (res.length()>0)
				res.append("&");
			res.append(key).append("=").append(value)    
				;
			
			
		}
		
		res.append("Content-Disposition: form-data; name=\"").append(fileField).append("\"; filename=\"").append(fileName).append("\"\r\n") 
		.append("Content-Type: ").append(fileType).append("\r\n\r\n");*/
 
		
		return res.toString();
	}
 
	public byte[] send() throws Throwable
	{
		
	       OutputStreamWriter request = null;

	            URL url  = new URL(this.url);   
	            String response = null;
	            StringBuilder sb ;
	            String encoding ="ISO-8859-1";
	           // String parameters = "username="+mUsername+"&password="+mPassword;   

	            try
	            {
	                
	                connection = (HttpURLConnection) url.openConnection();
	                connection.setDoOutput(true);
	                connection.setRequestProperty("Connection", "Keep-Alive");
	            	
	                connection.setRequestProperty("Content-Type", "multipart/form-data;charset=ISO-8859-1;boundary="+BOUNDARY);
	                //connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	                connection.setRequestMethod("POST");    
	                
	                connection.setConnectTimeout(30000);

	                request = new OutputStreamWriter(connection.getOutputStream());
	                request.write(new String(postBytes));
	                request.flush();
	                request.close();            
	                String line = "";               
	                InputStreamReader isr = new InputStreamReader(connection.getInputStream(), encoding);
	                BufferedReader reader = new BufferedReader(isr);
	                sb = new StringBuilder();
	                while ((line = reader.readLine()) != null)
	                {
	                    sb.append(line + "\n");
	                }
	                // Response from server after login process will be stored in response variable.                
	                res = sb.toString().getBytes();
	                // You can perform UI operations here
	                //Toast.makeText(this,"Message from Server: \n"+ response, 0).show();             
	                isr.close();
	                reader.close();

	            }
	            catch(Throwable e)
	            {
	               throw e;
	            }
	    
		return res;
	}
	
	
}