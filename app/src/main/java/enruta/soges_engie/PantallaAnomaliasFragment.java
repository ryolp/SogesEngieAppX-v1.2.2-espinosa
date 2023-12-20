package enruta.soges_engie;

import java.util.Vector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class PantallaAnomaliasFragment extends Fragment {
	DBHelper dbHelper;
	SQLiteDatabase db;
	ListView lv_lista;
	GridView gv_lista;
	TextView tv_mensaje;
	PantallaAnomaliasGridAdapter adapter;
	RelativeLayout rl_busquedaManual;
	String tipoAnomalia="";
	int tipo;
	
	ImageButton b_clearText;

	boolean tieneSubanomalia = false;
	// String is_anomalia;
	int ii_secuencial;
	// String is_lectura="";

	boolean tieneMensaje = false;
	View rootView;

	String is_desc = "";
	Cursor c;

	PantallaAnomalias pa_papa;

	EditText li_anomalia;
	TextView tv_label;

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.anomalias);
		pa_papa = (PantallaAnomalias) getActivity();
		rootView = inflater.inflate(R.layout.anomalias_fragment, container,
				false);
		// setTitle("");
//		String ls_filtro = "";
		li_anomalia = (EditText) rootView.findViewById(R.id.anom_et_anomalia);
		
		//Verificamos si la entrada de anomalias debe ser de numeros o tambien letras
		
		

			pa_papa.anomaliaTraducida=pa_papa.globales.traducirAnomalia();


		Bundle bu_params = this.getArguments();

		// ii_secuencial=bu_params.getInt("secuencial");
		// try{
		// pa_papa.is_lectura=bu_params.getString("lectura");
		// }catch(Throwable e){
		//
		// }

		tipo = bu_params.getInt("tipo");
		tipoAnomalia=pa_papa.globales.tll.getLecturaActual().is_tipoDeOrden;
		
		if (tipoAnomalia.equals("TO003")){   // Reconexion
			tipoAnomalia="I";
		}
		else if (tipoAnomalia.equals("TO004")){   // Rec-Remo
			tipoAnomalia="R";
		}
		else{
			tipoAnomalia="M";
		}
		li_anomalia.setText(bu_params.getString("anomalia"));

		b_clearText=(ImageButton) rootView.findViewById(R.id.im_clearText);
		lv_lista = (ListView) rootView.findViewById(R.id.anom_lv_lista);
		gv_lista = (GridView) rootView.findViewById(R.id.anom_gv_lista);
		tv_label = (TextView) rootView.findViewById(R.id.tv_label);
		tv_mensaje= (TextView) rootView.findViewById(R.id.tv_mensaje);
		rl_busquedaManual = (RelativeLayout)rootView.findViewById(R.id.rl_busquedaManual);
		
		b_clearText.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				//Borramos el texto 
				
				li_anomalia.getText().clear();
			}
			
		});
		
		
		//Hay que borrarse los tabs, asi que se tiene que volver a llamar a toda la rutina de inicializacion
		reinicializaTAB();
//		
//		lv_lista.setOnItemClickListener(new OnItemClickListener() {
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//
//				if (!tieneSubanomalia) {
//					TextView tv_anomalia = (TextView) view
//							.findViewById(android.R.id.text1);
//
//					is_desc = ((TextView) view
//							.findViewById(android.R.id.text2)).getText()
//							.toString();
//					String ls_anomalia = tv_anomalia.getText().toString();
//
//					pa_papa.is_anomaliaSelec = ls_anomalia;
//					selectAnomalia(ls_anomalia);
//
//				} else {
//
//					TextView tv_anomalia = (TextView) view
//							.findViewById(android.R.id.text1);
//					String ls_anomalia = tv_anomalia.getText().toString()
//							.substring(0, 4);
//					pa_papa.is_subAnomSelect = ls_anomalia;
//					selectAnomalia(ls_anomalia);
//
//				}
//
//			}
//		});
//
//		switch (tipo) {
//		case PantallaAnomaliasTabsPagerAdapter.TODAS:
//			if (pa_papa.is_lectura.length() > 0)
//				ls_filtro = " and (lectura='1' or ausente='0')";
//			else
//				ls_filtro = " and (lectura='0' and ausente='4')";
//
//			c = db.rawQuery(
//					"Select rowid _id, anomalia anom , desc desc from anomalia where subanomalia<>'S' and subanomalia<>'A' and activa='A' "
//							+ ls_filtro, null);
//			c.moveToFirst();
//
//			// String[] columns = new String[] { "anomalia", "desc" };
//			String[] columns = new String[] { "anom", "desc" };
//
//			ListAdapter adapter;
//			// if (Build.VERSION.SDK_INT>=11)
//			// //Solo para la nueva api
//			// adapter=new SimpleCursorAdapter(this,
//			// android.R.layout.simple_list_item_2, c, columns, new int[]
//			// {android.R.id.text1, android.R.id.text2 },0);
//			// else
//			adapter = new SimpleCursorAdapter(pa_papa,
//					android.R.layout.simple_list_item_2, c, columns, new int[] {
//							android.R.id.text1, android.R.id.text2 });
//			lv_lista.setAdapter(adapter);
//
////			lv_lista.setOnItemClickListener(new OnItemClickListener() {
////				public void onItemClick(AdapterView<?> parent, View view,
////						int position, long id) {
////
////					if (!tieneSubanomalia) {
////						TextView tv_anomalia = (TextView) view
////								.findViewById(android.R.id.text1);
////
////						is_desc = ((TextView) view
////								.findViewById(android.R.id.text2)).getText()
////								.toString();
////						String ls_anomalia = tv_anomalia.getText().toString();
////
////						is_anomaliaSelec = ls_anomalia;
////						selectAnomalia(ls_anomalia);
////
////					} else {
////
////						TextView tv_anomalia = (TextView) view
////								.findViewById(android.R.id.text1);
////						String ls_anomalia = tv_anomalia.getText().toString()
////								.substring(0, 4);
////						is_subAnomSelect = ls_anomalia;
////						selectAnomalia(ls_anomalia);
////
////					}
////
////				}
////			});
//
//			closeDatabase();
//
//			li_anomalia.setOnEditorActionListener(new OnEditorActionListener() {
//
//				@Override
//				public boolean onEditorAction(TextView arg0, int arg1,
//						KeyEvent arg2) {
//					// Si le damos al teclado mostramos
//					getAnomaliaEditText(arg0);
//					return false;
//				}
//			});
//			break;
//			
//		case PantallaAnomaliasTabsPagerAdapter.RECIENTES:
//			lv_lista.setVisibility(View.GONE);
//			rl_busquedaManual.setVisibility(View.GONE);
//			
//			setArrayOfAnomalias("Select anomalia anom from Anomalia  order by cast (fecha as Integer) desc limit 12");
//			break;
//			
//		case PantallaAnomaliasTabsPagerAdapter.MAS_USADAS:
//			lv_lista.setVisibility(View.GONE);
//			rl_busquedaManual.setVisibility(View.GONE);
//			setArrayOfAnomalias("Select anomalia anom from Anomalia  order by veces desc limit 12");
//			break;
//		}
//
//		li_anomalia.setInputType(InputType.TYPE_CLASS_NUMBER);

		return rootView;
	}
	
	public void setArrayOfAnomalias(String query){
		
		
		c = db.rawQuery(query
				, null);
		c.moveToFirst();
		
		if (c.getCount()>0){
			Vector <String> v_anomalias= new Vector <String>();
			gv_lista.setVisibility(View.VISIBLE);
			//Llenamos un arreglo
			for (int i=0; i<c.getCount();i++){
				v_anomalias.add(c.getString(c.getColumnIndex("anom")));
				
				if (i+1<c.getCount())
					c.moveToNext();
			}
			
			adapter = new PantallaAnomaliasGridAdapter(pa_papa, v_anomalias, pa_papa.ii_vp_height, pa_papa.ii_vp_width);
			gv_lista.setAdapter(adapter);
			
			gv_lista.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
						lv_lista.setVisibility(View.VISIBLE);
						rl_busquedaManual.setVisibility(View.VISIBLE);
						gv_lista.setVisibility(View.GONE);
						//Ya que no tenemos la descripcion habra que buscarla
						pa_papa.is_anomaliaSelec = adapter.getSecuencia(position);
						openDatabase();
						c= db.rawQuery("Select desc from Anomalia where "+pa_papa.anomaliaTraducida+"='"+pa_papa.is_anomaliaSelec+"'", null);
						
						c.moveToFirst();
						
						is_desc = c.getString(c.getColumnIndex("desc"));
						c.close();
						
						closeDatabase();
						
						
						selectAnomalia(pa_papa.is_anomaliaSelec);
				}
			});
			
		}else{
			//Mensaje de no hay 
			if (tipo==PantallaAnomaliasTabsPagerAdapter.MAS_USADAS){
				tv_mensaje.setText(R.string.msj_anomalias_no_hay);
			}
			else{
				tv_mensaje.setText(R.string.msj_anomalias_no_hay);
				
			}
			tv_mensaje.setVisibility(View.VISIBLE);
			gv_lista.setVisibility(View.GONE);
		}
	}

	@SuppressLint("NewApi")
	public void selectAnomalia(String ls_anomalia) {
		ls_anomalia=ls_anomalia.toUpperCase();
		String[] args = { ls_anomalia };
		String query="";
		// Buscamos si la anomalia tiene una sub anomalia
		
		if (!tieneSubanomalia){
			if (!pa_papa.globales.tdlg.esAnomaliaCompatible(ls_anomalia, pa_papa.is_anomalia) ){
				//Toast.makeText(pa_papa, R.string.msj_anomalias_no_compatible, Toast.LENGTH_LONG).show();
				pa_papa.mensajeOK(pa_papa.getString(R.string.msj_anomalias_no_compatible));
				if (tipo==PantallaAnomaliasTabsPagerAdapter.RECIENTES || tipo==PantallaAnomaliasTabsPagerAdapter.MAS_USADAS){
					reinicializaTAB();
				}
				
				return;
			}
		}
		
		String ls_mensaje=pa_papa.globales.tdlg.validaAnomalia(pa_papa.is_anomaliaSelec);
		if (!ls_mensaje.equals("")){
			//Toast.makeText(pa_papa, ls_mensaje, Toast.LENGTH_LONG).show();
			pa_papa.mensajeOK(ls_mensaje);
			if (tipo==PantallaAnomaliasTabsPagerAdapter.RECIENTES || tipo==PantallaAnomaliasTabsPagerAdapter.MAS_USADAS){
				reinicializaTAB();
			}
			return;
		}
		

		openDatabase();

		li_anomalia.setText("");

		c.close();
		
		
		if (!tieneSubanomalia) {
			query="select mens , capt from anomalia where "+pa_papa.anomaliaTraducida+"='"
					+ pa_papa.is_anomaliaSelec +"'";
		}
		else{
			query="select mens, capt from anomalia where substr(desc, 1, "+pa_papa.globales.longitudCodigoSubAnomalia+")='"
					+ pa_papa.is_subAnomSelect+"'";
		}

		
			c = db.rawQuery(
					query, null);
			c.moveToFirst();
			if (c.getInt(c.getColumnIndex("mens")) == 1 || c.getInt(c.getColumnIndex("capt")) == 1 ) {
				tieneMensaje = true;
			}
			c.close();
		

		c = db.rawQuery(
				"select rowid _id, "+pa_papa.anomaliaTraducida+" anom, desc desc from anomalia where "+pa_papa.anomaliaTraducida+"=? and subanomalia='S' and activa='A'",
				args);
		if (c.getCount() > 0 && !tieneSubanomalia && tieneMensaje) {
			// c.moveToFirst();
			tieneSubanomalia = true;

			tv_label.setText(ls_anomalia + " - " + is_desc);
			tv_label.setVisibility(View.VISIBLE);

			String[] columns = new String[] { "desc" };

			ListAdapter adapter;
			// if (Build.VERSION.SDK_INT>=11)
			// //Solo para la nueva api
			adapter = new SimpleCursorAdapter(pa_papa,
					android.R.layout.simple_list_item_1, c, columns,
					new int[] { android.R.id.text1 });
			// else
			// adapter=new SimpleCursorAdapter(this,
			// android.R.layout.simple_list_item_2, c, columns, new int[]
			// {android.R.id.text1, android.R.id.text2 });
			lv_lista.setAdapter(adapter);

			// Guardamos la anomalia principal
			//pa_papa.is_anomalia = ls_anomalia;

		} else {
			// vamos por el mensaje!! (que emocion...)
			
			if (tieneMensaje){
				//Hay que saber cual es la que se va a abrir, input o inputCampos generico
				//Para eso le preguntaremos a generica si tiene varios campos
				
				int [] campos=pa_papa.globales.tdlg.getCamposGenerico(pa_papa.is_anomaliaSelec);
				
				
					if (!tieneSubanomalia) {
						c = db.rawQuery(
								"select mens, desc, capt, "+pa_papa.anomaliaTraducida+" anomalia from anomalia where "+pa_papa.anomaliaTraducida+" ='"
										+ pa_papa.is_anomaliaSelec+"'", null);
					}
						else{
							
								c = db.rawQuery(
										"select mens, desc, capt, "+pa_papa.anomaliaTraducida+" anomalia from anomalia where substr(desc, 1, "+pa_papa.globales.longitudCodigoSubAnomalia+")='"
												+ pa_papa.is_subAnomSelect+"'", null);
							}
					
					c.moveToFirst();
					String ls_indicadorMensaje=pa_papa.globales.tdlg.remplazaValorDeArchivo(TomaDeLecturasGenerica.MENSAJE, !tieneSubanomalia? pa_papa.is_anomaliaSelec:pa_papa.is_subAnomSelect,  String.valueOf(c.getInt(c.getColumnIndex("mens"))));
					if (campos==null && (ls_indicadorMensaje.equals("1") || c.getInt(c.getColumnIndex("capt"))==1)){
					
						// Abrimos input
						Intent intent = new Intent(pa_papa, Input.class);
						intent.putExtra("tipo", Input.COMENTARIOS);
						intent.putExtra("comentarios", "");
						intent.putExtra("anomaliaquepidelectura", c.getString(c.getColumnIndex("anomalia")));

						String strEscribaSusComentarios = "";
//						strEscribaSusComentarios=c.getString(c.getColumnIndex("anomalia")) + " - " + c.getString(c.getColumnIndex("desc"));
						if (c.getString(c.getColumnIndex("anomalia")).equals("E"))
							strEscribaSusComentarios="ESCRIBA LA LECTURA DEL MEDIDOR: \n\n";
						else
							strEscribaSusComentarios="ESCRIBA SUS COMENTARIOS PARA LA REPERCUSION: \n\n" + c.getString(c.getColumnIndex("anomalia")) + " - " + c.getString(c.getColumnIndex("desc")) + "\n\n";
						// Con esto generamos la etiqueta que tendra el input
						intent.putExtra("label",strEscribaSusComentarios	+ "");
						
//						String codigoAnomalia="";
//						if (globales.convertirAnomalias)
//							codigoAnomalia=pa_papa.is_anomalia.is_conv;
//						else
//							codigoAnomalia=anom.is_anomalia;
						//Aqui mandamos el comportamiento de input, en otras palabras, le daremos la anomalia para que pueda configurarlo como se le de la gana
						intent.putExtra("behavior", pa_papa.is_anomaliaSelec);
						// Tambien debo mandar que etiqueta quiero tener
						pa_papa.startActivityForResult(intent, TomaDeLecturas.COMENTARIOS);
				}
				else if (campos!=null && (c.getInt(c.getColumnIndex("mens"))==1 || c.getInt(c.getColumnIndex("capt"))==1)){
					//Tiene mas datos a guardar
					Intent intent = new Intent(pa_papa, InputCamposGenerico.class);
					intent.putExtra("campos",campos);
					intent.putExtra("label", c.getString(c.getColumnIndex("anomalia")) + " - "
										+ c.getString(c.getColumnIndex("desc"))
										+ "\n");
					intent.putExtra("anomalia", c.getString(c.getColumnIndex("anomalia")));
					pa_papa.startActivityForResult(intent, TomaDeLecturas.INPUT_CAMPOS_GENERICO);
				}else{
					pa_papa.mandarAnomalia();
				}
				
				
				
			}else{
					pa_papa.globales.tdlg.RealizarModificacionesDeAnomalia(pa_papa.is_subAnomSelect);

				
				
				pa_papa.mandarAnomalia();
			}


		}

		// c.close();
		closeDatabase();

	}

	public void getAnomaliaEditText(View view) {
		boolean existe = false;
		esconderTeclado();
		
		if (li_anomalia.getText().toString().equals("")){
//			Toast.makeText(pa_papa,R.string.msj_anomalias_no_valida,
//					Toast.LENGTH_LONG).show();
			pa_papa.mensajeOK(pa_papa.getString(R.string.msj_anomalias_no_valida));
			return;
		}
		// Hay que validar...
		if (tieneSubanomalia
				&& li_anomalia.getText().toString().trim().length() < pa_papa.globales.longitudCodigoSubAnomalia) {
//	st.makeText(pa_papa, String.format(getString(R.string.msj_anomalias_validacion_subavisos), String.valueOf(pa_papa.globales.longitudCodigoSubAnomalia)),
//					Toast.LENGTH_LONG).show();
			pa_papa.mensajeOK(String.format(getString(R.string.msj_anomalias_validacion_subavisos), String.valueOf(pa_papa.globales.longitudCodigoSubAnomalia)));
			return;
		}

		else if (!tieneSubanomalia
				&& li_anomalia.getText().toString().trim().equals("0")) {
			pa_papa.borrarAnomalia();
			return;
		}

		openDatabase();

		// Mas validaciones
		if (!tieneSubanomalia) {

			String query = "Select rowid _id, "+pa_papa.anomaliaTraducida+" anom , desc desc from anomalia where subanomalia<>'S' and subanomalia<>'A' and activa='A' and "+pa_papa.anomaliaTraducida+"=";
			c = db.rawQuery(query + "'" +li_anomalia.getText().toString().trim().toUpperCase() + "'",
					null);

			pa_papa.is_anomaliaSelec = li_anomalia.getText().toString().trim();
		}

		else {
			String query = "Select rowid _id, "+pa_papa.anomaliaTraducida+" anom , desc desc from anomalia where subanomalia='S' and subanomalia<>'A' and activa='A' and "+pa_papa.anomaliaTraducida+"='";
			pa_papa.is_subAnomSelect = li_anomalia.getText().toString().trim().toUpperCase();
			c = db.rawQuery(query + pa_papa.is_anomaliaSelec
					+ "' and substr(desc, 1, "+pa_papa.globales.longitudCodigoSubAnomalia+")  ='"
					+ pa_papa.is_subAnomSelect + "'", null);
		}

		if (c.getCount() > 0) {
			existe = true;
			c.moveToFirst();
			is_desc = c.getString(c.getColumnIndex("desc"));
		}

		c.close();
		closeDatabase();
		if (existe) {
			selectAnomalia(li_anomalia.getText().toString());
			li_anomalia.setText("");
		} else {
//			Toast.makeText(pa_papa, R.string.msj_anomalias_no_valida,
//					Toast.LENGTH_LONG).show();
			pa_papa.mensajeOK(pa_papa.getString(R.string.msj_anomalias_no_valida));
		}

	}

	public void esconderTeclado() {
		InputMethodManager mgr = (InputMethodManager) pa_papa
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(li_anomalia.getWindowToken(), 0);
	}
	
	public void openDatabase(){
		dbHelper= new DBHelper(pa_papa);
		db = dbHelper.getReadableDatabase();
	}
	
	public void closeDatabase(){
		db.close();
		
		dbHelper.close();
	}
	
	public void reinicializaTAB(){
		
		tv_label.setVisibility(View.GONE);
		tieneSubanomalia=false;
		tieneMensaje=false;
		openDatabase();
		
		String ls_filtro = pa_papa.getFiltro();
				
				lv_lista.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						if (!tieneSubanomalia) {
							TextView tv_anomalia = (TextView) view
									.findViewById(android.R.id.text1);

							is_desc = ((TextView) view
									.findViewById(android.R.id.text2)).getText()
									.toString();
							String ls_anomalia = tv_anomalia.getText().toString();

							pa_papa.is_anomaliaSelec = ls_anomalia;
							selectAnomalia(ls_anomalia);

						} else {

							TextView tv_anomalia = (TextView) view
									.findViewById(android.R.id.text1);
							String ls_anomalia = tv_anomalia.getText().toString()
									.substring(0, pa_papa.globales.longitudCodigoSubAnomalia);
							pa_papa.is_subAnomSelect = ls_anomalia;
							selectAnomalia(ls_anomalia);

						}

					}
				});

				switch (tipo) {
				case PantallaAnomaliasTabsPagerAdapter.TODAS:
					//ls_filtro= pa_papa.getFiltro();

					c = db.rawQuery(
							"Select rowid _id, "+pa_papa.anomaliaTraducida+" anom , desc desc from anomalia where subanomalia<>'S' and subanomalia<>'A' and activa='A' and tipo='"+tipoAnomalia+"' "
									+ ls_filtro, null);
					c.moveToFirst();

					// String[] columns = new String[] { "anomalia", "desc" };
					String[] columns = new String[] { "anom", "desc" };

					ListAdapter adapter;
					// if (Build.VERSION.SDK_INT>=11)
					// //Solo para la nueva api
					// adapter=new SimpleCursorAdapter(this,
					// android.R.layout.simple_list_item_2, c, columns, new int[]
					// {android.R.id.text1, android.R.id.text2 },0);
					// else
					adapter = new SimpleCursorAdapter(pa_papa,
							android.R.layout.simple_list_item_2, c, columns, new int[] {
									android.R.id.text1, android.R.id.text2 });
					lv_lista.setAdapter(adapter);

//					lv_lista.setOnItemClickListener(new OnItemClickListener() {
//						public void onItemClick(AdapterView<?> parent, View view,
//								int position, long id) {
		//
//							if (!tieneSubanomalia) {
//								TextView tv_anomalia = (TextView) view
//										.findViewById(android.R.id.text1);
		//
//								is_desc = ((TextView) view
//										.findViewById(android.R.id.text2)).getText()
//										.toString();
//								String ls_anomalia = tv_anomalia.getText().toString();
		//
//								is_anomaliaSelec = ls_anomalia;
//								selectAnomalia(ls_anomalia);
		//
//							} else {
		//
//								TextView tv_anomalia = (TextView) view
//										.findViewById(android.R.id.text1);
//								String ls_anomalia = tv_anomalia.getText().toString()
//										.substring(0, 4);
//								is_subAnomSelect = ls_anomalia;
//								selectAnomalia(ls_anomalia);
		//
//							}
		//
//						}
//					});

					

					li_anomalia.setOnEditorActionListener(new OnEditorActionListener() {

						@Override
						public boolean onEditorAction(TextView arg0, int arg1,
								KeyEvent arg2) {
							// Si le damos al teclado mostramos
							getAnomaliaEditText(arg0);
							return false;
						}
					});
					break;
					
				case PantallaAnomaliasTabsPagerAdapter.RECIENTES:
					lv_lista.setVisibility(View.GONE);
					rl_busquedaManual.setVisibility(View.GONE);
					
					setArrayOfAnomalias("Select anoma."+pa_papa.anomaliaTraducida+" anom from Anomalia anoma, usoAnomalias uso  where anoma."+pa_papa.anomaliaTraducida+"=uso.anomalia and subanomalia<>'S' and subanomalia<>'A' and activa='A' and fecha<>'' "+ls_filtro+" and tipo='"+tipoAnomalia+"' order by cast (fecha as Integer) desc limit 12");
					//setArrayOfAnomalias("Select anoma."+pa_papa.anomaliaTraducida+" anom from Anomalia anoma LEFT JOIN usoAnomalias uso  on  anoma."+pa_papa.anomaliaTraducida+"=uso.anomalia and subanomalia<>'S' where subanomalia<>'A' and activa='A' "+ls_filtro+" order by cast (fecha as Integer) desc limit 12");
					
					break;
					
				case PantallaAnomaliasTabsPagerAdapter.MAS_USADAS:
					lv_lista.setVisibility(View.GONE);
					rl_busquedaManual.setVisibility(View.GONE);
					setArrayOfAnomalias("Select  anoma."+pa_papa.anomaliaTraducida+" anom from Anomalia anoma, usoAnomalias uso where anoma."+pa_papa.anomaliaTraducida+"=uso.anomalia and subanomalia<>'S' and subanomalia<>'A' and activa='A' and veces<>0 "+ls_filtro+" and tipo='"+tipoAnomalia+"' order by veces desc, cast (fecha as Integer) desc limit 12");
					//setArrayOfAnomalias("Select  anoma."+pa_papa.anomaliaTraducida+" anom from Anomalia anoma LEFT JOIN usoAnomalias uso on anoma."+pa_papa.anomaliaTraducida+"=uso.anomalia and subanomalia<>'S'  where subanomalia<>'A' and activa='A' "+ls_filtro+" order by veces desc, cast (fecha as Integer) desc limit 12");
					break;
				}

				if (pa_papa.globales.convertirAnomalias)
					li_anomalia.setInputType(/*InputType.TYPE_CLASS_TEXT|*/InputType.TYPE_TEXT_FLAG_CAP_WORDS| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
				else
					li_anomalia.setInputType(InputType.TYPE_CLASS_NUMBER);
				
				closeDatabase();
	}

	
	
	

}
