package enruta.soges_engie;


import enruta.soges_engie.R;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class CodigosEjecucion {
	final static int LECTURA_AUSENTE=0;
	final static int LECTURA_OPCIONAL=1;
	final static int LECTURA_OBLIGATORIA=2;
	final static int SIN_ANOMALIA=3;
	
	
	DBHelper dbHelper;
	
	SQLiteDatabase db;
	
	Context context;
	
	int 
	
	ii_capt, //captura datos;
	ii_ausente, //si es ausente
	ii_mensaje, //mensaje
	ii_lectura, //si requiere lectura
	ii_foto //Requiere foto
	;
	String is_desc, //descripcion
	is_tipo, is_pais, is_activa, is_conv,//Convertir;
	is_anomalia;  
	String index;
	boolean esCodigoAnomalia;
	boolean esSubAnomalia;
	Globales globales;
	
	String campoAnomaliaTraducido="";
	String is_subanomalia /*Subanomalia*/;
	
	//db.execSQL("CREATE TABLE Anomalia (desc , conv , capt , subanomalia , ausente , mens , lectura , foto , anomalia , activa , tipo , pais TEXT)");
	
	CodigosEjecucion(Context context, int index){
		this.context=context;
		this.index=String.valueOf(index);
		this.esCodigoAnomalia=false;
		globales=((Globales) context.getApplicationContext());
		campoAnomaliaTraducido=globales.traducirAnomalia();
		llenarCampos();
	}
	
	CodigosEjecucion(Context context, String index, boolean esSubAnomalia){
		this.context=context;
		//this.index=Long.parseLong(index);
		this.index=index;
		this.esCodigoAnomalia=true;
		this.esSubAnomalia=esSubAnomalia;
		globales=((Globales) context.getApplicationContext());
		campoAnomaliaTraducido=globales.traducirAnomalia();
		llenarCampos();
	}
	
	

	
	CodigosEjecucion(Context context, byte[] medidor,SQLiteDatabase db ){
		this.context=context;
		Resources res=context.getResources();
		
		//openDatabase();
		ContentValues cv_params=new ContentValues();
		
		cv_params.put("desc", new String (medidor,res.getInteger(R.integer.ANOM_POSI_DESC) ,res.getInteger(R.integer.ANOM_LONG_DESC)).trim());
		cv_params.put("conv",new String (medidor,res.getInteger(R.integer.ANOM_POSI_CONV) ,res.getInteger(R.integer.ANOM_LONG_CONV)).trim());
		cv_params.put("capt", new String (medidor,res.getInteger(R.integer.ANOM_POSI_CAPT) ,res.getInteger(R.integer.ANOM_LONG_CAPT)).trim());
		cv_params.put("subanomalia",new String (medidor,res.getInteger(R.integer.ANOM_POSI_SUBANOM) ,res.getInteger(R.integer.ANOM_LONG_SUBANOM)).trim());
		cv_params.put("ausente",new String (medidor,res.getInteger(R.integer.ANOM_POSI_AUSENTE) ,res.getInteger(R.integer.ANOM_LONG_AUSENTE)).trim());
		cv_params.put("mens",new String (medidor,res.getInteger(R.integer.ANOM_POSI_MENS) ,res.getInteger(R.integer.ANOM_LONG_MENS)).trim());
		cv_params.put("lectura", new String (medidor,res.getInteger(R.integer.ANOM_POSI_LECT) ,res.getInteger(R.integer.ANOM_LONG_LECT)).trim());
		cv_params.put("anomalia",new String (medidor,res.getInteger(R.integer.ANOM_POSI_ANOM) ,res.getInteger(R.integer.ANOM_LONG_ANOM)).trim());
		cv_params.put("activa",new String (medidor,res.getInteger(R.integer.ANOM_POSI_ACTIVA) ,res.getInteger(R.integer.ANOM_LONG_ACTIVA)).trim());
		cv_params.put("tipo",new String (medidor,res.getInteger(R.integer.ANOM_POSI_TIPO) ,res.getInteger(R.integer.ANOM_LONG_TIPO)).trim());
		cv_params.put("pais",new String (medidor,res.getInteger(R.integer.ANOM_POSI_PAIS) ,res.getInteger(R.integer.ANOM_LONG_PAIS)).trim());
		cv_params.put("foto",new String (medidor,res.getInteger(R.integer.ANOM_POSI_FOTO) ,res.getInteger(R.integer.ANOM_LONG_FOTO)).trim());
		
		index=String.valueOf(db.insert("CodigosEjecucion", null, cv_params));
		globales=((Globales) context.getApplicationContext());
		campoAnomaliaTraducido=globales.traducirAnomalia();
		 //closeDatabase();
		 esCodigoAnomalia=false;
		 //llenarCampos();
	}
	
	CodigosEjecucion(Context context, String medidor,SQLiteDatabase db ){
		this.context=context;
		Resources res=context.getResources();
		
		//openDatabase();
		ContentValues cv_params=new ContentValues();
		
		cv_params.put("desc",medidor.substring(res.getInteger(R.integer.ANOM_POSI_DESC), res.getInteger(R.integer.ANOM_POSI_DESC) + res.getInteger(R.integer.ANOM_LONG_DESC)).trim());
		cv_params.put("conv",medidor.substring(res.getInteger(R.integer.ANOM_POSI_CONV), res.getInteger(R.integer.ANOM_POSI_CONV) + res.getInteger(R.integer.ANOM_LONG_CONV)).trim());
		cv_params.put("capt",  medidor.substring(res.getInteger(R.integer.ANOM_POSI_CAPT) , res.getInteger(R.integer.ANOM_POSI_CAPT)+res.getInteger(R.integer.ANOM_LONG_CAPT)).trim());
		cv_params.put("subanomalia",medidor.substring(res.getInteger(R.integer.ANOM_POSI_SUBANOM) ,res.getInteger(R.integer.ANOM_POSI_SUBANOM) + res.getInteger(R.integer.ANOM_LONG_SUBANOM))  .trim());
		cv_params.put("ausente",medidor.substring(res.getInteger(R.integer.ANOM_POSI_AUSENTE) , res.getInteger(R.integer.ANOM_POSI_AUSENTE)+res.getInteger(R.integer.ANOM_LONG_AUSENTE)).trim());
		cv_params.put("mens",medidor.substring(res.getInteger(R.integer.ANOM_POSI_MENS) , res.getInteger(R.integer.ANOM_POSI_MENS)+res.getInteger(R.integer.ANOM_LONG_MENS)).trim());
		cv_params.put("lectura", medidor.substring(res.getInteger(R.integer.ANOM_POSI_LECT) ,res.getInteger(R.integer.ANOM_POSI_LECT)+res.getInteger(R.integer.ANOM_LONG_LECT)).trim());
		cv_params.put("anomalia",medidor.substring(res.getInteger(R.integer.ANOM_POSI_ANOM) , res.getInteger(R.integer.ANOM_POSI_ANOM) +res.getInteger(R.integer.ANOM_LONG_ANOM)).trim());
		cv_params.put("activa",medidor.substring(res.getInteger(R.integer.ANOM_POSI_ACTIVA) , res.getInteger(R.integer.ANOM_POSI_ACTIVA) +res.getInteger(R.integer.ANOM_LONG_ACTIVA)).trim());
		cv_params.put("tipo",medidor.substring(res.getInteger(R.integer.ANOM_POSI_TIPO) , res.getInteger(R.integer.ANOM_POSI_TIPO) +res.getInteger(R.integer.ANOM_LONG_TIPO)).trim());
		cv_params.put("pais",medidor.substring(res.getInteger(R.integer.ANOM_POSI_PAIS) , res.getInteger(R.integer.ANOM_POSI_PAIS)+res.getInteger(R.integer.ANOM_LONG_PAIS)).trim());
		cv_params.put("foto",medidor.substring(res.getInteger(R.integer.ANOM_POSI_FOTO) , res.getInteger(R.integer.ANOM_POSI_FOTO) +res.getInteger(R.integer.ANOM_LONG_FOTO)).trim());
		
		index=String.valueOf(db.insert("CodigosEjecucion", null, cv_params));
		globales=((Globales) context.getApplicationContext());
		campoAnomaliaTraducido=globales.traducirAnomalia();
		 //closeDatabase();
		 esCodigoAnomalia=false;
		 //llenarCampos();
	}
	
	
	public void llenarCampos(){
		
		
		openDatabase();
		
		//String params[]={String.valueOf(index)};
		Cursor c;
		
		if (esCodigoAnomalia){
			if (esSubAnomalia){
				c=db.rawQuery("Select * from CodigosEjecucion where (substr(desc, 1,"+globales.longitudCodigoSubAnomalia+") ='"+index+"' and subanomalia='S')",null);
			}
			else
			{
				c=db.rawQuery("Select * from CodigosEjecucion where ("+campoAnomaliaTraducido+"='"+index+"' and subanomalia<>'S' )",null);
			}
//			c=db.rawQuery("Select * from anomalia where (cast(anomalia as Integer)=cast('"+index+"' as Integer) and subanomalia<>'S' )" +
//					" or (cast(substr(desc, 1, 4) as Integer)=cast('"+index+"' as Integer) and subanomalia='S')",null);
		}
		else
		c=db.rawQuery("Select * from CodigosEjecucion where rowid='"+index+"'",null);

		
		if (c.getCount()==0){
			if (esCodigoAnomalia  && index=="777"){
				is_subanomalia= ".";
				is_conv= "0";
				ii_capt= 0;
				ii_ausente= 4;
				ii_mensaje= 0;
				ii_lectura= 0;
				ii_foto= 0;
				is_anomalia= "777";
				is_desc= "";
				is_tipo= "M";
				is_pais= "N";
				is_activa= "";
			}
			else if (esCodigoAnomalia){
				is_subanomalia= ".";
				is_conv= globales.convertirAnomalias?index:"0";
				ii_capt= 0;
				ii_ausente= 4;
				ii_mensaje= 0;
				ii_lectura= 0;
				ii_foto= 0;
				is_anomalia=  globales.convertirAnomalias?"0":index;
				is_desc= "";
				is_tipo= "M";
				is_pais= "N";
				is_activa= "I";
			}
			return;
		}
		
		c.moveToFirst();
		
		
		
		is_subanomalia= c.getString(c.getColumnIndex("subanomalia"));
		is_conv= c.getString(c.getColumnIndex("conv"));
		ii_capt= c.getInt(c.getColumnIndex("capt"));
		ii_ausente= c.getInt(c.getColumnIndex("ausente"));
		ii_mensaje= c.getInt(c.getColumnIndex("mens"));
		ii_lectura= c.getInt(c.getColumnIndex("lectura"));
		ii_foto= c.getInt(c.getColumnIndex("foto"));
		is_anomalia= c.getString(c.getColumnIndex("anomalia"));
		is_desc= c.getString(c.getColumnIndex("desc"));
		is_tipo= c.getString(c.getColumnIndex("tipo"));
		is_pais= c.getString(c.getColumnIndex("pais"));
		is_activa= c.getString(c.getColumnIndex("activa"));
		
		c.close();
		 closeDatabase();
		
	}
	private void openDatabase(){
    	dbHelper= new DBHelper(context);
		
        db = dbHelper.getReadableDatabase();
    }
	
	 private void closeDatabase(){
	    	db.close();
	        dbHelper.close();
	        
	    }
	 
	 public boolean esAusente(){
		 if(ii_ausente==4)
			 return true;
		 else
			 return false;
	 }
	 
	 /**
	  * Decide si requiere lectura y devuelve el tipo
	  * @return El tipo de lectura a tomar: Ausente, Opcional u Obligatoria
	  */
	 public int requiereLectura(){
		 if (ii_lectura == 0 && ii_ausente == 4) {
			 return LECTURA_AUSENTE;
		 }
		 else if (ii_lectura == 1 && ii_ausente == 0) {
			 return LECTURA_OPCIONAL;
		 }
		 else
		 {
			 return LECTURA_OBLIGATORIA;
		 }
	 }

}
