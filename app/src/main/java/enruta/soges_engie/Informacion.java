package enruta.soges_engie;

import enruta.soges_engie.R;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

public class Informacion extends Activity {

	private final static int Grabar = 0;
	private boolean bEsAdministrador;
	
	EditText et_centro, et_lote, et_CPL, et_pais, et_numBluetooth, et_macBluetooth, et_macImpresora, et_tamanofoto, et_rutaDescarga;
	LinearLayout ll_layout;
	SQLiteDatabase db=null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.informacion);
		
		Bundle bu_params= getIntent().getExtras();
		bEsAdministrador=bu_params.getBoolean("esAdministrador");
		
		//Inicializamos los campos
		et_centro= (EditText) findViewById(R.id.info_et_centro);
		et_lote=(EditText) findViewById(R.id.info_et_lote); 
		et_CPL=(EditText) findViewById(R.id.info_et_CPL);
		et_pais=(EditText) findViewById(R.id.info_et_pais);
		et_numBluetooth=(EditText) findViewById(R.id.info_et_numBluetooth); 
		et_macBluetooth=(EditText) findViewById(R.id.info_et_macBluetooth); 
		et_macImpresora=(EditText) findViewById(R.id.info_et_macImpresora); 
		et_tamanofoto=(EditText) findViewById(R.id.info_et_tamanofoto);
		et_rutaDescarga=(EditText) findViewById(R.id.info_et_rutaDescarga);
		ll_layout= (LinearLayout) findViewById(R.id.info_ll_layout);
		
		if (!bEsAdministrador)
			//bloquearObjetos();
			enableDisableView(ll_layout, false);

		// Obtenemos todos los datos
		DBHelper dbHelper = new DBHelper(this);

		try {
			db = dbHelper.getReadableDatabase();
			if (db != null) {
				
				Cursor c = db.rawQuery("SELECT * FROM url", null);
				if (c.getCount() > 0) {
						c.moveToFirst();
						et_CPL.setText(c.getString(0));
						et_lote.setText(c.getString(1));
						et_centro.setText(c.getString(2));
						et_pais.setText(c.getString(7));
						et_numBluetooth.setText(c.getString(4));
						et_macBluetooth.setText(c.getString(5));
						et_macImpresora.setText(c.getString(6));
						et_tamanofoto.setText(c.getString(12));
						et_rutaDescarga.setText(c.getString(11));
				}

			}
		} catch (Throwable e) {
			
		}

	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!bEsAdministrador) return true ;
		menu.add("Grabar");
		return true;

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		String ls_cpl, ls_lote, ls_centro, ls_pais, ls_numBluetooth, ls_macBluetooth, ls_macImpresora, ls_tamanoFoto, ls_rutaDescarga;
		
		ls_cpl=et_CPL.getText().toString();
		ls_lote=et_lote.getText().toString();
		ls_centro=et_centro.getText().toString();
		ls_pais=et_pais.getText().toString();
		ls_numBluetooth=et_numBluetooth.getText().toString();
		ls_macBluetooth=et_macBluetooth.getText().toString();
		ls_macImpresora=et_macImpresora.getText().toString();
		ls_tamanoFoto=et_tamanofoto.getText().toString();
		ls_rutaDescarga=et_rutaDescarga.getText().toString();
		
		if (item.getTitle() == "Grabar") {
			db.execSQL("delete from url");
			db.execSQL("insert into URL (cpl, lote, centro, pais, numBluetooth, macBluetooth, macImpresora, tamanoFotos, rutaDescarga) values ('"+ ls_cpl + "', '" +
					ls_lote + "', '"+ls_centro+"', '"+ ls_pais +"', '" + ls_numBluetooth + "', '"+ ls_macBluetooth+"', '"+ls_macImpresora +"', '" + ls_tamanoFoto +"','"+ ls_rutaDescarga +"')");
			//db.execSQL("update URL set pais=\"E\"");
			
		}

		return true;
	}
	
	private void enableDisableView(View view, boolean enabled) {
		//Bloquea todos los objetos en una vista
	    view.setEnabled(enabled);

	    if ( view instanceof ViewGroup ) {
	        ViewGroup group = (ViewGroup)view;

	        for ( int idx = 0 ; idx < group.getChildCount() ; idx++ ) {
	            enableDisableView(group.getChildAt(idx), enabled);
	        }
	    }
	}
	
	public void onDestroy(){
		//Esconde si hay un teclado activo
		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(ll_layout.getWindowToken(), 0);
		super.onDestroy();
	}

}
