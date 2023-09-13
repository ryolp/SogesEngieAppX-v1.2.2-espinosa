package enruta.soges_engie;


import enruta.soges_engie.R;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class Usuario {
	
	DBHelper dbHelper;
	
	SQLiteDatabase db;
	
	Context context;
	
	String usuario, contrasena, nombre;
	long index;
	
	Usuario(Context context, int index){
		this.context=context;
		this.index=index;
		llenarCampos();
	}

	
	Usuario(Context context, byte[] medidor,SQLiteDatabase db ){
		this.context=context;
		Resources res=context.getResources();
		
		//openDatabase();
		ContentValues cv_params=new ContentValues();
		
		cv_params.put("usuario", new String (medidor,res.getInteger(R.integer.USER_POSI_LOGIN) ,res.getInteger(R.integer.USER_LONG_LOGIN)).trim());
		cv_params.put("contrasena",new String (medidor,res.getInteger(R.integer.USER_POSI_PASSWORD) ,res.getInteger(R.integer.USER_LONG_PASSWORD)).trim());
		cv_params.put("nombre", new String (medidor,res.getInteger(R.integer.USER_POSI_NOMBRE) ,res.getInteger(R.integer.USER_LONG_NOMBRE)).trim());
		
		String cadena =  new String (medidor);
		cv_params.put("rol", cadena.substring(medidor.length - 6 ,medidor.length - 5).trim());
		cv_params.put("fotosControlCalidad", cadena.substring(medidor.length - 5 ,medidor.length - 3).trim());
		cv_params.put("baremo", cadena.substring(medidor.length - 3,medidor.length).trim());

		
		index=db.insert("usuarios", null, cv_params);

	}
	
	Usuario(Context context, String medidor,SQLiteDatabase db ){
		this.context=context;
		Resources res=context.getResources();
		
		//openDatabase();
		ContentValues cv_params=new ContentValues();
		
		cv_params.put("usuario", medidor.substring(res.getInteger(R.integer.USER_POSI_LOGIN) , res.getInteger(R.integer.USER_POSI_LOGIN)+res.getInteger(R.integer.USER_LONG_LOGIN)).trim());
		cv_params.put("contrasena",medidor.substring(res.getInteger(R.integer.USER_POSI_PASSWORD) , res.getInteger(R.integer.USER_POSI_PASSWORD) + res.getInteger(R.integer.USER_LONG_PASSWORD)).trim());
		cv_params.put("nombre", medidor.substring(res.getInteger(R.integer.USER_POSI_NOMBRE) , res.getInteger(R.integer.USER_POSI_NOMBRE) +res.getInteger(R.integer.USER_LONG_NOMBRE)).trim());
		
		//String cadena =  new String (medidor);
		cv_params.put("rol", medidor.substring(medidor.length() - 6 ,medidor.length() - 5).trim());
		cv_params.put("fotosControlCalidad", medidor.substring(medidor.length() - 5 ,medidor.length() - 3).trim());
		cv_params.put("baremo", medidor.substring(medidor.length() - 3,medidor.length()).trim());

		
		index=db.insert("usuarios", null, cv_params);

	
	}
	
	public void llenarCampos(){
		openDatabase();
		
		//String params[]={String.valueOf(index)};
		Cursor c;
		

		c=db.rawQuery("Select * from usarios where rowid=cast('"+index+"' as Integer)",null);

		c.moveToFirst();
		
		
		
		usuario= c.getString(c.getColumnIndex("usuario"));
		contrasena= c.getString(c.getColumnIndex("contrasena"));
		nombre=c.getString(c.getColumnIndex("nombre"));
		
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
	 
	 

}
