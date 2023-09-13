package enruta.soges_engie;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ActivityGen extends Activity{

	DBHelper dbHelper;
	SQLiteDatabase db;
	
	protected void openDatabase(){
    	dbHelper= new DBHelper(this);
		
        db = dbHelper.getReadableDatabase();
    }
	
	 protected void closeDatabase(){
	    	db.close();
	        dbHelper.close();
	    }
	 
	 public double getDoubleValue( String key,  double value){
		 openDatabase();
			
			Cursor c= db.rawQuery("Select * from config where key='"+key+"'", null);
			
			if (c.getCount()>0) {
				c.moveToFirst();
				value=c.getDouble(c.getColumnIndex("value"));
			}
			else
			{
				db.execSQL("Insert into config (key, value) values ('"+key+"', "+value+")");
			}
			c.close();
			
			
			
			closeDatabase();
			
			return value;
	 }
	 
}
