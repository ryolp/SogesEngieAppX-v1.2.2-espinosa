package enruta.soges_engie;

import java.util.ArrayList;
import java.util.Vector;

import enruta.soges_engie.R;
import enruta.soges_engie.Configuracion.Entry;
import enruta.soges_engie.Configuracion.XmlEditText;
import enruta.soges_engie.Configuracion.XmlSpinner;
import enruta.soges_engie.Configuracion.XmlSpinnerItem;
import enruta.soges_engie.Configuracion.XmlTextView;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class NoRegistrados extends Activity {
	
Vector <Entry> objetosAMostar;
	
	
	DBHelper dbHelper;
	SQLiteDatabase db;
	
	View view;
	
	long il_ultimoSegReg=0;
	
	boolean bCONDIR=true;
	
	Globales globales;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.no_registrados);
		
		globales = ((Globales) getApplicationContext());
		
		setTitle("");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		 DialogInterface.OnCancelListener cancelListener=new DialogInterface.OnCancelListener(){

			@Override
			public void onCancel(DialogInterface arg0) {
				// TODO Auto-generated method stub
	        	finish();
			}
				
			};
		
		AlertDialog alert;
		
		Bundle bu_params = getIntent().getExtras();
		Resources res= this.getResources();
		
		il_ultimoSegReg=bu_params.getLong("il_ultimoSegReg");
		
		view= findViewById( R.id.ll_config);
		
		objetosAMostar= new Vector<Entry>();
		
		objetosAMostar.add( new XmlTextView(getString(R.string.msj_no_registrados_num_serie), "16", "TextView", null, true, "", 0,Typeface.ITALIC) );
		objetosAMostar.add(new XmlEditText("", "16", "EditText",null, true, "numSerie", String.valueOf(globales.tlc.getLongCampo("serieMedidor"))
				, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,"")); 
		
		objetosAMostar.add( new XmlTextView(getString(R.string.msj_no_registrados_num_esferas), "16", "TextView", null, true, "", 0,Typeface.ITALIC) );
		objetosAMostar.add(new XmlEditText("", "16", "EditText",null, true, "numDiales",String.valueOf(globales.tlc.getLongCampo("numEsferas")), InputType.TYPE_CLASS_NUMBER,"")); 
		
		objetosAMostar.add( new XmlTextView(getString(R.string.str_lectura), "16", "TextView", null, true, "", 0,Typeface.ITALIC) );
		objetosAMostar.add(new XmlEditText("", "16", "EditText",null, true, "lectura",String.valueOf(globales.tlc.getLongCampo("lectura")), InputType.TYPE_CLASS_NUMBER,"")); 
		
		
		objetosAMostar.add( new XmlTextView(getString(R.string.msj_observaciones), "16", "TextView", null, true, "", 0,Typeface.ITALIC) );
		objetosAMostar.add(new XmlEditText("", "16", "EditText",null, true, "observ","30", InputType.TYPE_CLASS_TEXT,"")); 
		
		agregaCampos();
		
		builder.setMessage(R.string.msj_tiene_medidor).setOnCancelListener(cancelListener).setCancelable(true)
	       .setPositiveButton(R.string.Si, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id){
	        	   //recepcion();
	        	   bCONDIR=false;
	                dialog.dismiss();
	           }
	       })
	       .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id){
	        	   guardar();
	        	   //dialog.cancel();
	        	   bCONDIR=true;
	        	   dialog.dismiss();
	        	   finish();
	                
	           }
	       });
		
		alert = builder.create();
		alert.show();
		
		
	}
	
	
	
	private boolean guardar(){
		Resources res= this.getResources();
		Cursor c;
		String ls_unicom;
		String ls_cadena="";
		
		
		//Si no pasa las validaciones , no podemos continuar
		if (!ValidarNoRegistrados())
			return false;
		
		
		try{
			Lectura lectura= new Lectura(this, (int) il_ultimoSegReg);
			
			
			openDatabase();
			
			c= db.rawQuery("Select registro from encabezado", null);
			c.moveToFirst();
			ls_unicom=new String(c.getBlob(c.getColumnIndex("registro")),res.getInteger(R.integer.POS_DATOS_UNICOM), res.getInteger(R.integer.LONG_CAMPO_UNICOM)) ;
			
			
			
			
			ls_cadena+=ls_unicom;
			ls_cadena+=Main.rellenaString(String.valueOf(il_ultimoSegReg), "0", globales.tlc.getLongCampo("secuencia"), true);
			EditText et_view= (EditText) view.findViewWithTag("numDiales");
			ls_cadena+=Main.rellenaString(String.valueOf(et_view.getText().toString()), "0", globales.tlc.getLongCampo("numEsferas"), true);
			
			ls_cadena+=Main.rellenaString(lectura.getColonia(), " ", globales.tlc.getLongCampo("colonia") -1, false);
			ls_cadena+=Main.rellenaString(lectura.numeroDeEdificio, " ", globales.tlc.getLongCampo("numEdificio"), false);
			ls_cadena+=Main.rellenaString(lectura.numeroDePortal, " ", globales.tlc.getLongCampo("numPortal"), false);
			et_view= (EditText) view.findViewWithTag("lectura");
			ls_cadena+=Main.rellenaString(String.valueOf(et_view.getText().toString()), "0", globales.tlc.getLongCampo("lectura"), true);
			et_view= (EditText) view.findViewWithTag("numSerie");
			ls_cadena+=Main.rellenaString(String.valueOf(et_view.getText().toString()), " ", globales.tlc.getLongCampo("serieMedidor"), false);
			ls_cadena+=Main.rellenaString(lectura.is_serieMedidor.trim(), " ", globales.tlc.getLongCampo("serieMedidor"), false);
			ls_cadena+="31/01/2005";
			ls_cadena+="5699828230704";
			et_view= (EditText) view.findViewWithTag("observ");
			ls_cadena+=Main.rellenaString(et_view.getText().toString().trim(), " ", 45, false);// en el programa original rellena aqui 45 pero no veo referencias
			
			ContentValues cv=new ContentValues();
			cv.put("poliza", ls_cadena.getBytes());
			
			db.insert("NoRegistrados", null,  cv);
			
			closeDatabase();
			
			
			
			
		}
		catch(Throwable e){
			
			
		}
		
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.configuracion, menu);
		
		MenuItem mi_mac=menu.findItem(R.id.m_ingresarMAC);
		MenuItem mi_impr=menu.findItem(R.id.m_ingresarMACIMPR);
		
		mi_mac.setVisible(false);
		mi_impr.setVisible(false);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_guardar:
			 if (guardar())
				 finish();
			break;
		}
		return true;
	}
	
	
	private void openDatabase(){
    	dbHelper= new DBHelper(this);
		
        db = dbHelper.getReadableDatabase();
    }
	
	 private void closeDatabase(){
	    	db.close();
	        dbHelper.close();
	        
	    }
	 
	 
		private boolean ValidarNoRegistrados() {
			String strTemp1;
			String strTemp2;
			
			if (!bCONDIR) {
				
				EditText et_view= (EditText) view.findViewWithTag("numSerie");
				strTemp1 = et_view.getText().toString(); //Numero de serie del medidor
				
				et_view= (EditText) view.findViewWithTag("numDiales");
				strTemp2 = et_view.getText().toString(); //Numero de esferas
				if (strTemp1.trim().equals(""))
					{
					Toast.makeText(this, String.format(getString(R.string.msj_campo_vacio), getString(R.string.msj_no_registrados_num_serie)), Toast.LENGTH_SHORT).show();
					 et_view= (EditText) view.findViewWithTag("numSerie");
					 et_view.requestFocus();
					return  false; //NUmero de serie no puede estar vacio
					}
				if (strTemp2.trim().equals("")) {
					Toast.makeText(this, String.format(getString(R.string.msj_campo_vacio), getString(R.string.msj_no_registrados_num_esferas)), Toast.LENGTH_SHORT).show();
					 et_view= (EditText) view.findViewWithTag("numDiales");
					 et_view.requestFocus();
					return false; //Numero de esferas no puede estar vacio
				}
			}
			return true;
		}
		
		 public void agregaCampos(){
			 
			 Cursor c;
			    String tmp_text = null;
			    
			    int id;
			    View view = null;
			    
				//c=db.rawQuery("Select * from config" , null);
				//c.moveToFirst();
				 LinearLayout layout=(LinearLayout) findViewById(R.id.ll_config);
				 
			 for (Entry tmp : objetosAMostar) {    
			    	if (tmp.type.equals("TextView")){
			    		XmlTextView entry= (XmlTextView) tmp;
			    		 TextView tv_view;
			    		if (!tmp.crear){
			    			 id=getResources().getIdentifier(tmp.view_name, "id", this.getPackageName());
			    			 view= findViewById(id);
			    			 tv_view=(TextView) view;
			    		}
			    		else{
				    		 tv_view=new TextView(this);
				    		 tv_view.setTag(tmp.view_name);
			    		}
			    		
			    		 
			    	 	if(entry.label!=null){
			    	 		tv_view.setText(entry.label);
			    	 	}
			    	 	
			    	 	if(entry.color!=null){
			    	 		tv_view.setTextColor(Color.parseColor (entry.color));
			    	 	}
			    	 	
			    	 	if(entry.size!=null){
			    	 		tv_view.setTextSize(Integer.parseInt(entry.size));
			    	 	}

			    	 	//Agregamos la gravedad de android
			    	 	tv_view.setGravity(entry.gravity);
			    	 	//Agregamos el Style (negritas, normal o Italicas)
			    	 	tv_view.setTypeface(null, entry.style);
			    	 	
			    	 	//Agregamos al layout
			    	 	layout.addView(tv_view);
			    	}
			    	if (tmp.type.equals("EditText")){
			    		XmlEditText entry= (XmlEditText) tmp;
			    		 EditText et_view;
			    		if (!tmp.crear){
			    			 id=getResources().getIdentifier(tmp.view_name, "id", this.getPackageName());
			    			 view= findViewById(id);
			    			 et_view=(EditText) view;
			    		}
			    		else{
			    			et_view=new EditText(this);
			    			et_view.setTag(tmp.view_name);
			    		}
			    		
			    		 //Vamos a poner el valor que se encuentre en la base de datos, pero si no esta, deberemos
			    		if (entry.dbField!=null && !entry.dbField.equals("") ){

			    			//Aqui buscamos la key
			    			openDatabase();
			    			
			    			c= db.rawQuery("Select * from config where key='"+entry.dbField+"'", null);
			    			if(c.getCount()>0){
			    				tmp_text=c.getString(c.getColumnIndex("value"));
				    			
				    			
			    			}
			    			
			    			et_view.setText(tmp_text==null?entry.label:tmp_text);
			    			
			    			c.close();
			    			closeDatabase();
			    			
			    			tmp_text=null;
			    			
			    			
			    			
			    		}
			    		else if(entry.label!=null){
			    	 		et_view.setText(entry.label);
			    	 	}
			    	 	
			    	 	if(entry.color!=null){
			    	 		et_view.setTextColor(Color.parseColor (entry.color));
			    	 	}
			    	 	
			    	 	if(entry.size!=null){
			    	 		et_view.setTextSize(Integer.parseInt(entry.size));
			    	 	}
			    	 	et_view.setInputType(entry.inputType);
			    	 	
			    	 	InputFilter[] FilterArray = new InputFilter[1];
			    	 	FilterArray[0] = new InputFilter.LengthFilter(entry.maxLenght);
			    	 	et_view.setFilters(FilterArray);

			    	 	
			    	 	//Agregamos al layout
			    	 	layout.addView(et_view);
			    	}
			    	
			    	if (tmp.type.equals("Spinner")){
			    		XmlSpinner entry = (XmlSpinner) tmp;
			    		Spinner spinner;
			    		
			    		if (!tmp.crear){
			    			 id=getResources().getIdentifier(tmp.view_name, "id", this.getPackageName());
			    			 view= findViewById(id);
			    			 spinner=(Spinner) view;
			    		}
			    		else{
			    			spinner= new Spinner(this);
			    			spinner.setTag(tmp.view_name);
			    		}
			    		
			    		
			    		
			    		ArrayList<String> spinnerArray = new ArrayList<String>();
			    		
			    		for(XmlSpinnerItem sp:entry.items){
			    			spinnerArray.add(sp.label);
			    		}
			    		
			    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
			    		//Estilo usado
			    		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			    		//Aplicamos los datos
			    		spinner.setAdapter(adapter);
			    		
			    		//Agregamos al layout
			    	 	layout.addView(spinner);
			    	}
			    		
			        
			    }
			// c.close();
			//closeDatabase();
		 }
		 

		    

}
