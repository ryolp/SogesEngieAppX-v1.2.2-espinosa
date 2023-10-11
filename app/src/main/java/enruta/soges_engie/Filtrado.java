package enruta.soges_engie;

import enruta.soges_engie.R;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class Filtrado extends AppCompatActivity {

	Spinner sp_modo;
	EditText et_ciudad, et_medidor, et_cliente, et_direccion;
	CheckBox /*cb_brincarc*/cb_medidor, cb_cliente, cb_direccion, cb_ciudad;
	
	DBHelper dbHelper;
	SQLiteDatabase db;
	
	int spinner_pos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filtrado);
		
		inicializarCampos();
		agregaRegistrosConfig();
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.filtrado, menu);
		return true;
	}*/
	
	private void inicializarCampos(){
		sp_modo= (Spinner) findViewById(R.id.sp_modo);
		et_ciudad= (EditText) findViewById(R.id.et_ciudad);
		et_medidor= (EditText) findViewById(R.id.et_medidor);
		et_cliente= (EditText) findViewById(R.id.et_cliente);
		et_direccion= (EditText) findViewById(R.id.et_direccion);
		
		cb_medidor= (CheckBox) findViewById(R.id.cb_medidor);
		cb_cliente=(CheckBox) findViewById(R.id.cb_cliente);
		cb_direccion=(CheckBox) findViewById(R.id.cb_direccion);
		cb_ciudad=(CheckBox) findViewById(R.id.cb_ciudad);
		
		//cb_brincarc=(CheckBox) findViewById(R.id.cb_brincarc);
		
		//et_medidor.setVisibility(View.GONE);
		
		
		
		
		//Creamos un adaptador con los datos del recurso Strings y con el spiner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.Modo, android.R.layout.simple_spinner_item);
		//Estilo usado
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//Aplicamos los datos
		sp_modo.setAdapter(adapter);
		sp_modo.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
				// TODO Auto-generated method stub
				spinner_pos=pos;
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		esconderTeclado();
	}
	
	protected void onDestroy (){
		guardar();
    	
    	super.onDestroy();
    	
    }
	
	private void openDatabase(){
    	dbHelper= new DBHelper(this);
		
        db = dbHelper.getReadableDatabase();
    }
	
	 private void closeDatabase(){
	    	db.close();
	        dbHelper.close();
	    }
	 
	 private void guardar(){
		 
		 String[] ls_values= new String [3] ;
		 
		 ls_values[1]="0";
		 ls_values[2]="modo";
		 ls_values[0]=String.valueOf(spinner_pos);
		 update(ls_values);
		 
		 ls_values[1]=cb_ciudad.isChecked()?"1":"0";
		 ls_values[2]="ciudad";
		 ls_values[0]=et_ciudad.getText().toString();
		 update(ls_values);
		 
		 ls_values[1]=cb_medidor.isChecked()?"1":"0";
		 ls_values[2]="medidor";
		 ls_values[0]=et_medidor.getText().toString();
		 update(ls_values);
		 
		 ls_values[1]=cb_cliente.isChecked()?"1":"0";
		 ls_values[2]="cliente";
		 ls_values[0]=et_cliente.getText().toString();
		 update(ls_values);
		 
		 ls_values[1]=cb_direccion.isChecked()?"1":"0";
		 ls_values[2]="direccion";
		 ls_values[0]=et_direccion.getText().toString();
		 update(ls_values);
		 
		 /*ls_values[1]="brincarc";
		 ls_values[0]=cb_brincarc.isChecked()?"1":"0";
		 updateInt(ls_values);*/
		 
		 
		 
		 
	 }
	 
	 
	 private void update(String[] ls_values){
		 openDatabase();
		 
		 db.execSQL("update config set value=?, selected=? where key=? ", ls_values );
		 
		 closeDatabase();
	 }
	 
	 private void updateInt(String[] ls_values){
		 openDatabase();
		 
		 db.execSQL("update config set value=cast(? as Integer), selected=? where key=?", ls_values );
		 
		 closeDatabase();
	 }
	 
	 private void agregaRegistrosConfig(){
		 //Agregamos los registros, asi nadamas actualizamos.
		 openDatabase();
		/* Cursor c;
		 c=db.query("config", null, "key='modo'", null, null, null, null);
		 
		 if (c.getCount()==0)
			 db.execSQL("insert into config (key, value)values ('modo', '')");
		 else{
			 c.moveToFirst();
			 sp_modo.setSelection(c.getInt(c.getColumnIndex("value")), true);
		 }

		 c.close();*/
		 
		 getLlaves("modo", null, null, sp_modo);
		 getLlaves("ciudad", et_ciudad, cb_ciudad, null);
		 getLlaves("medidor", et_medidor, cb_medidor, null);
		 getLlaves("cliente", et_cliente, cb_cliente, null);
		 getLlaves("direccion", et_direccion, cb_direccion, null);
		 
		 
		 
		 /*c=db.query("config", null, "key='ciudad'", null, null, null, null);
		 
		 if (c.getCount()==0)
			 db.execSQL("insert into config (key, value)values ('ciudad', '')");
		 else
		 {
			 c.moveToFirst();
			 et_ciudad.setText(c.getString(c.getColumnIndex("value")));
		 }
		 
		 c.close();
		 
		 c=db.query("config", null, "key='medidor'", null, null, null, null);
		 
		 if (c.getCount()==0)
			 db.execSQL("insert into config (key, value)values ('medidor', '')");
		 else{
			 c.moveToFirst();
			 et_medidor.setText(c.getString(c.getColumnIndex("value")));
		 }
		 
		 c.close();
		 
		 c=db.query("config", null, "key='cliente'", null, null, null, null);
		 
		 if (c.getCount()==0)
			 db.execSQL("insert into config (key, value)values ('cliente', '')");
		 else
		 {
			 c.moveToFirst();
			 et_cliente.setText(c.getString(c.getColumnIndex("value")));
		 }
		 
		 c.close();
		 
		 c=db.query("config", null, "key='direccion'", null, null, null, null);
		 
		 if (c.getCount()==0)
			 db.execSQL("insert into config (key, value)values ('direccion', '')");
		 else
		 {
			 c.moveToFirst();
			 et_direccion.setText(c.getString(c.getColumnIndex("value")));
		 }
			 
		 c.close();
		 
		/* c=db.query("config", null, "key='brincarc'", null, null, null, null);
		 
		 if (c.getCount()==0)
			 db.execSQL("insert into config (key, value)values ('brincarc', 0)");
		 else
		 {
			 c.moveToFirst();
			 cb_brincarc.setChecked(c.getInt(c.getColumnIndex("value"))==0?false:true);
		 }
			 
		 c.close();*/
		 
		 
		 
		 closeDatabase();
	 }
	 
	 /**
	  * Establece las llaves de registro en la pantalla, si alguno de los datos es null, se omite
	  * Si el registro no existe, lo agrega
	  * @param key Es nombre de la llave de registro
	  * @param et_where Donde serán colocados los datos
	  * @param ck_where Cual será el checkbox
	  * @param sp_where Cual será el spinner
	  * @param value valor por default
	  * @param selected si esta seleccionado o no por default
	  */
	 public void getLlaves(String key, String value, String selected, EditText et_where, CheckBox ck_where, Spinner sp_where){
		 Cursor c;
		 c=db.query("config", null, "key='"+key+"'", null, null, null, null);
		 String ck_value;
		 
		 if (c.getCount()==0)
			 db.execSQL("insert into config (key, value, selected)values ('"+key+"', '"+value+"', '"+selected+"')");
		 else
		 {
			 c.moveToFirst();
			 if (et_where!=null)
				 et_where.setText(c.getString(c.getColumnIndex("value")));
			 
			 if (ck_where!=null){
				 ck_value=c.getString(c.getColumnIndex("selected"));
				 ck_where.setChecked(ck_value==null? false:ck_value.equals("1")?true:false );
			 }
				 
			 
			 if (sp_where!=null)
				 sp_where.setSelection(Integer.parseInt(c.getString(c.getColumnIndex("value")).equals("")? "0": c.getString(c.getColumnIndex("value"))) );
		 }
			 
		 c.close();
	 }
	 
	 /**
	  * Establece las llaves de registro en la pantalla, si alguno de los datos es null, se omite
	  * Si el registro no existe, lo agrega
	  * @param key Es nombre de la llave de registro
	  * @param et_where Donde serán colocados los datos
	  * @param ck_where Cual será el checkbox
	  * @param sp_where Cual será el spinner
	  * @param value valor por default
	  * @param selected si esta seleccionado o no por default
	  */
	 public void getLlaves(String key, EditText et_where, CheckBox ck_where, Spinner sp_where){
		 getLlaves(key, "", "", et_where, ck_where, sp_where);
	 }
	 
	 public void esconderTeclado() {
			InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(et_ciudad.getWindowToken(), 0);
		}

}
