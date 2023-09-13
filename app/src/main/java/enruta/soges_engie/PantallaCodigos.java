package enruta.soges_engie;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import enruta.soges_engie.R;

public class PantallaCodigos extends Activity {

	RelativeLayout rl_busquedaManual;
	
	DBHelper dbHelper;
	SQLiteDatabase db;
	
	ListView lv_lista;
	
	String ls_anomalia="";
	String ls_comentarios="";
	
	Globales globales;
	
	final static int COMENTARIOS=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.anomalias_fragment);
		globales = ((Globales) getApplicationContext());
		setTitle("Codigos");
		rl_busquedaManual= (RelativeLayout) findViewById(R.id.rl_busquedaManual);
		rl_busquedaManual.setVisibility(View.GONE);
		
		
		openDatabase();
		
		Cursor c= db.rawQuery("Select rowid _id, anomalia anom, desc from codigosEjecucion", null);
		
		String[] columns = new String[] { "anom", "desc" };
		
		ListAdapter adapter;
		
		lv_lista = (ListView) findViewById(R.id.anom_lv_lista);
		
		adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, c, columns, new int[] {
						android.R.id.text1, android.R.id.text2 });
		lv_lista.setAdapter(adapter);
		
		final PantallaCodigos pc=this;
		
		lv_lista.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				
					TextView tv_anomalia = (TextView) view
							.findViewById(android.R.id.text1);

					String is_desc = ((TextView) view
							.findViewById(android.R.id.text2)).getText()
							.toString();
					ls_anomalia = tv_anomalia.getText().toString();

//					pa_papa.is_anomaliaSelec = ls_anomalia;
//					selectAnomalia(ls_anomalia);
					
					Intent intent = new Intent(pc, Input.class);
					intent.putExtra("tipo", Input.COMENTARIOS);
					intent.putExtra("comentarios", "");

					// Con esto generamos la etiqueta que tendra el input
					intent.putExtra("label",
							ls_anomalia + " - "
									+ is_desc
									+ "");
					
//					String codigoAnomalia="";
//					if (globales.convertirAnomalias)
//						codigoAnomalia=pa_papa.is_anomalia.is_conv;
//					else
//						codigoAnomalia=anom.is_anomalia;
					
					//Aqui mandamos el comportamiento de input, en otras palabras, le daremos la anomalia para que pueda configurarlo como se le de la gana
//					intent.putExtra("behavior", pa_papa.is_anomaliaSelec);
					// Tambien debo mandar que etiqueta quiero tener
					startActivityForResult(intent, COMENTARIOS);

				

			}
		});
		
		closeDatabase();
	}
	
	public void selectAnomalia(){
		Intent intent= new Intent();

		
		intent.putExtra("anomalia", ls_anomalia);
		
		
		setResult(Activity.RESULT_OK, intent);
		
		this.finish();
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.pantalla_codigos, menu);
//		return true;
//	}

	
	public void openDatabase(){
		dbHelper= new DBHelper(this);
		db = dbHelper.getReadableDatabase();
	}
	
	public void closeDatabase(){
		db.close();
		
		dbHelper.close();
	}

	@SuppressLint("NewApi")
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
		case COMENTARIOS:
			if (resultCode == Activity.RESULT_OK) {
				Bundle bu_params =data.getExtras();
				globales.tll.getLecturaActual().setComentarios(bu_params.getString("input"));
				selectAnomalia();
			}
			break;
		}
	}
}
