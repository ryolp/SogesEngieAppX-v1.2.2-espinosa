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
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class InputCamposGenerico extends Activity {
	
Vector <Entry> objetosAMostar;
	
	
	DBHelper dbHelper;
	SQLiteDatabase db;
	
	View view;
	
	long il_ultimoSegReg=0;
	
	boolean bCONDIR=true;
	
	Globales globales;
	
	String ultimoEditView="";
	boolean primerEditView=true;
	
	String is_anomalia="";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.no_registrados);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		
		globales = ((Globales) getApplicationContext());
		Button button= (Button) findViewById(R.id.b_continuar);
		button.setVisibility(View.VISIBLE);
		
		
		button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (guardar())
					 finish();
			}
			
		});
		setTitle("");
		
		//Vamos a poner un bundle (OBLIGATORIO) para que sepamos que vamos a ingresar
		Bundle bu_params = getIntent().getExtras();
		is_anomalia=bu_params.getString("anomalia");
		objetosAMostar= new Vector<Entry>();
		try{//Mostramos la informacion que viene de anomalias
			objetosAMostar.add( new XmlTextView(bu_params.getString("label"), "20", "TextView", null, true, "", 0,Typeface.ITALIC) );
		}catch(Throwable e){
			
		}
		
		
		//Serán puros numeros en un arreglo, cada casilla del arreglo representa un campo
		int[] campos=bu_params.getIntArray("campos");
		
		
		view= findViewById( R.id.ll_config);
		//Hay que agregar cada campo en la pantalla
		for(int campo: campos){
			ComentariosInputBehavior cib=globales.tdlg.getCampoGenerico(campo);
			if (cib==null){
				continue;
			}
			if (!cib.mensaje.equals("Agente")) {
				objetosAMostar.add(new XmlTextView(cib.mensaje, "20", "TextView", null, true, "", 0, Typeface.ITALIC));
				objetosAMostar.add(new XmlEditText(cib.texto, "20", "EditText", null, true, String.valueOf(campo), String.valueOf(cib.longitud), cib.tipo, String.valueOf(campo)));
			} else {
				Vector <Configuracion.XmlSpinnerItem> agentesDelSpin = null;
				objetosAMostar.add(new XmlTextView(cib.mensaje, "20", "TextView", null, true, "", 0, Typeface.ITALIC));
				objetosAMostar.add(new XmlSpinner(cib.mensaje,"20", "Spinner",true, String.valueOf(campo), String.valueOf(campo), null, agentesDelSpin));
			}
		}
		
		agregaCampos();
		setListener();
		
	}
	
	
	
	private boolean guardar(){
		Intent intent= new Intent();
		String texto="";
		String dbField="";
		String desc="";
		
		if (objetosAMostar!=null){
			 for (Entry tmp : objetosAMostar) {    
				 
				 if (tmp.type.equals("TextView")){
					 desc= ((XmlTextView)tmp).label;
					 continue;
				 }
					
				 
				 texto="";
				 if (tmp.type.equals("EditText")){
					EditText et_view= (EditText) view.findViewWithTag(tmp.view_name);
					
					texto=et_view.getText().toString();
					dbField= ((XmlEditText)tmp).dbField;
					
					ComentariosInputBehavior cib=globales.tdlg.getCampoGenerico(Integer.parseInt(dbField));
					
					if (cib.obligatorio && texto.equals("")){
						Toast.makeText(this, String.format(
								getString(R.string.msj_campo_vacio),"'"+desc+"'"), Toast.LENGTH_LONG).show();
						return false;
					}
					
				 }
				 if (tmp.type.equals("Spinner")){
					 Spinner sp_view= (Spinner) view.findViewWithTag(tmp.view_name);
// CE, 09/10/23, En el caso de Engie, queremos extraer el nombre del Agente
//					 texto= String.valueOf(sp_view.getSelectedItemPosition());
					 texto = String.valueOf(sp_view.getSelectedItem());
					 dbField= ((XmlSpinner)tmp).dbField;
				 }
				 
				 intent.putExtra(dbField, texto);
			 }
		}
		if (!is_anomalia.equals("")){
			String mensaje=globales.tdlg.validaCamposGenericos(is_anomalia, intent.getExtras() );
			if (!mensaje.equals("")){
				Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
				return false;
			}
		}
		
		setResult(Activity.RESULT_OK, intent);
		return true;
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.configuracion, menu);
//		
//		MenuItem mi_mac=menu.findItem(R.id.m_ingresarMAC);
//		MenuItem mi_impr=menu.findItem(R.id.m_ingresarMACIMPR);
//		
//		mi_mac.setVisible(false);
//		mi_impr.setVisible(false);
//		return true;
//	}
	
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
			    		ultimoEditView=tmp.view_name;
//			    		if (primerEditView){
//			    			primerEditView=false;
//			    			et_view.requestFocus();
//			    			mostrarTeclado();
//			    		}
//			    		
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
//			    		for(XmlSpinnerItem sp:entry.items){
//			    			spinnerArray.add(sp.label);
//			    		}
						spinnerArray.add("7 Eleven");
						spinnerArray.add("American Express");
						spinnerArray.add("Arteli");
						spinnerArray.add("App Engie");
						spinnerArray.add("BBVA-Bancomer CIE");
						spinnerArray.add("Caja Bienestar");
						spinnerArray.add("Caja Popular Gonzalo Vega");
						spinnerArray.add("Citibanamex");
						spinnerArray.add("Elektra");
						spinnerArray.add("HEB");
						spinnerArray.add("Mi Cuenta");
						spinnerArray.add("OXXO");
						spinnerArray.add("Soriana");
						spinnerArray.add("Super Q");
						spinnerArray.add("Otro");
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
		 
//		 public void mostrarTeclado(){
//				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//				  imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
//			}
		 
		 public void setListener(){
			//Si es el ultimo le agregamos una accion
			 EditText et_view=(EditText) view.findViewWithTag(ultimoEditView);
	    	 	et_view.setOnEditorActionListener(new OnEditorActionListener() {

	    			

	    			@Override
	    			public boolean onEditorAction(TextView arg0, int arg1,
	    					KeyEvent arg2) {
	    				// TODO Auto-generated method stub
	    				if (guardar())
	   					 finish();
	    				return false;
	    			}
	    	       });
		 }

		    

}
