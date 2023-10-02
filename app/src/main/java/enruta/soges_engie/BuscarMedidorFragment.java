package enruta.soges_engie;

import java.util.Vector;

import enruta.soges_engie.R;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

@SuppressLint("NewApi")
public class BuscarMedidorFragment extends Fragment {
	
	//Button  b_regresar, b_buscar;
	ImageButton b_clearText;
	TextView tv_msj_buscar;
	TodasLasLecturas tll;
	ListView lv_medidores;
	EditText et_medidor;
	View layout;
	int contador=0;
	BuscarMedidor bm_papa;
	int tipo= BuscarMedidorTabsPagerAdapter.DIRECCION;
	ProgressBar pb_ruleta;
	
	Handler mHandler;
	
	View rootView;
	
	BuscarMedidorGridAdapter adapter;
	
	Vector <Lectura> vLecturas = new Vector<Lectura>();
	Vector <String> vStrings= new Vector<String>();
	
	@Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	          Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.buscar_medidor);

		bm_papa= (BuscarMedidor) getActivity();
		rootView = inflater.inflate(R.layout.buscar_medidor_fragment, container, false);
	
		Bundle bundle = this.getArguments();
		if(bundle != null){
			tipo = bundle.getInt("tipo");
		}
		lv_medidores= (ListView) rootView.findViewById(R.id.lv_medidores);
		pb_ruleta= (ProgressBar) rootView.findViewById(R.id.progressBar1);
//		b_regresar= (Button) rootView.findViewById(R.id.b_regresar);
//		b_buscar= (Button) rootView.findViewById(R.id.b_buscar);
		b_clearText=(ImageButton) rootView.findViewById(R.id.im_clearText);
		et_medidor= (EditText) rootView.findViewById(R.id.et_medidor);
		tv_msj_buscar=(TextView)rootView.findViewById(R.id.tv_msj_buscar); //tv_msj_buscar
		
		if (tipo==BuscarMedidorTabsPagerAdapter.DIRECCION || tipo==BuscarMedidorTabsPagerAdapter.CALLES )
			et_medidor.setInputType(InputType.TYPE_CLASS_TEXT);

		switch(tipo){
	
		case BuscarMedidorTabsPagerAdapter.DIRECCION:
			tv_msj_buscar.setText(R.string.msj_buscar_direccion);
			break;
			
		case BuscarMedidorTabsPagerAdapter.NUMERO:
			tv_msj_buscar.setText(R.string.msj_buscar_numero);
			break;
		}
		tv_msj_buscar.setVisibility(View.VISIBLE);
		et_medidor.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1,
					KeyEvent arg2) {
					//Si le damos al teclado mostramos
					buscar();
				return false;
			}
	       });
		
//		b_regresar.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View view) {
//				// Es regresar asi que justo aqui ponemos el regresar
//				bm_papa.setResult(Activity.RESULT_CANCELED);
//				bm_papa.finish();
//				
//			}
//			
//		});
		
		b_clearText.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				//Borramos el texto 
				et_medidor.getText().clear();
			}
		});
		
		lv_medidores.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long id) {
				// TODO Auto-generated method stub
				bm_papa.regresaResultado(adapter.getSecuencia(pos));
			}
		});
		
//		b_buscar.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View view) {
//				// No es el que queremos asi que seguiremos buscando y buscando hasta no encontrar medidores
//				
//				buscar();
//				
//				
//			}
//			
//		});
//		
		if (bm_papa.ii_tipoDeBusqueda==BuscarMedidor.BUSCAR)
			tv_msj_buscar.setBackgroundResource(R.drawable.buscar_pattern);
		else
			tv_msj_buscar.setBackgroundResource(R.drawable.mover_pattern);
		mHandler=new Handler();
		if (tipo==BuscarMedidorTabsPagerAdapter.DIRECCION)
			buscar();
		return rootView;
	}

	private void setDatos(){
		pb_ruleta.setVisibility(View.GONE);
		if (vLecturas.isEmpty() && vStrings.isEmpty()){
			switch(tipo){
			
			case BuscarMedidorTabsPagerAdapter.DIRECCION:
				tv_msj_buscar.setText(R.string.msj_buscar_no_medidores_direccion);
				break;
			case BuscarMedidorTabsPagerAdapter.NUMERO:
				tv_msj_buscar.setText(R.string.msj_buscar_no_medidores_numero);
				break;
			case BuscarMedidorTabsPagerAdapter.CALLES:
				tv_msj_buscar.setText(R.string.msj_buscar_no_medidores_calles);
				break;
			default:
				tv_msj_buscar.setText(R.string.msj_buscar_no_medidores);
			}
			tv_msj_buscar.setVisibility(View.VISIBLE);
			//b_buscar.setVisibility(View.GONE);
			//i_datos.setVisibility(View.GONE);
			lv_medidores.setVisibility(View.GONE);
			return;
		}else{
			tv_msj_buscar.setVisibility(View.GONE);
			lv_medidores.setVisibility(View.VISIBLE);
			//Toast.makeText(bm_papa, "Se encontraron " + vLecturas.size() + " medidor(es)", Toast.LENGTH_SHORT).show();
			if (tipo==BuscarMedidorTabsPagerAdapter.CALLES){
				Toast.makeText(bm_papa, String.format(getString(R.string.msj_buscar_cant_calles_encontrados), vStrings.size()), Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(bm_papa, String.format(getString(R.string.msj_buscar_cant_med_encontrados), vLecturas.size()) , Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public void buscar(){
		if (et_medidor.getText().toString().trim().equals("") && tipo!=BuscarMedidorTabsPagerAdapter.DIRECCION)
		{
			if (!bm_papa.cambiandoFiltro)
				Toast.makeText(bm_papa,String.format(getString(R.string.msj_campo_vacio),getResources().getString(R.string.str_buscar_lowercase)) , Toast.LENGTH_SHORT).show();
			else
				bm_papa.cambiandoFiltro=false;
			return;
		}
		
		Thread busqueda= new Thread(){
			public void run() {
				tll= new TodasLasLecturas(bm_papa, 0);// Se buscaran las lecturas desde el principio
				vLecturas = new Vector<Lectura>();
				vStrings= new Vector<String>();
				tll.ls_groupBy="";
				switch(tipo){
				case BuscarMedidorTabsPagerAdapter.MEDIDOR:
					tll.setFiltro("and serieMedidor like '%"+ et_medidor.getText().toString().trim()+"%'");
					break;
				case BuscarMedidorTabsPagerAdapter.DIRECCION:
					tll.setFiltro("and upper(colonia || ' ' || calle || ' ' || entrecalles) like '%"+ et_medidor.getText().toString().trim().toUpperCase()+"%'");
					break;
				case BuscarMedidorTabsPagerAdapter.NUMERO:
					tll.setFiltro("and poliza like '%"+ et_medidor.getText().toString().trim()+"%'");
					break;
//				case BuscarMedidorTabsPagerAdapter.CALLES:
//					if (!et_medidor.getText().toString().trim().equals("")){
//						tll.setFiltro("and comoLlegar1 like '%"+ et_medidor.getText().toString().trim()+"%'");
//					}
//					else{
//						tll.ls_groupBy=" comoLlegar1 ";
//					}
//					
//					break;
				}
				
//				tll.setFiltro("and serieMedidor like '%"+ et_medidor.getText().toString().trim()+"%'");
				try{
//					if (!bModificar)
//						tll.siguienteMedidorACapturarSinVuelta(bModificar, false);
//					else
					//Vamos por el vector con sus lecturas

//					if (!bModificar)
//						tll.siguienteMedidorACapturarSinVuelta(bModificar, false);
//					else
//						tll.siguienteMedidorIndistinto();
					if (tipo==BuscarMedidorTabsPagerAdapter.CALLES){
						vStrings=bm_papa.globales.tll.getTodasLasCalles(bm_papa.tipoDeMedidoresABuscar, et_medidor.getText().toString().trim());
					}else{
						obtenerMedidor();
						while(tll.encontrado){
							vLecturas.add(tll.getLecturaActual());
//							if (!bModificar)
//								tll.siguienteMedidorACapturarSinVuelta(bModificar, false);
//							else
//								tll.siguienteMedidorIndistinto();	
							obtenerMedidor();
						}
					}
						//setDatos();
				}
					catch(Throwable e){
						e.printStackTrace();
					}
				
				if (tipo==BuscarMedidorTabsPagerAdapter.CALLES){
					adapter = new BuscarMedidorGridAdapter(bm_papa, vStrings, tipo, et_medidor.getText().toString().trim().toUpperCase());
				}
				else{
					adapter = new BuscarMedidorGridAdapter(bm_papa, vLecturas, tipo, et_medidor.getText().toString().trim().toUpperCase(), tll.getNumRecords());
				}
				mHandler.post(new Runnable() {
   		            public void run() {
   		            	try{
   		            		lv_medidores.setAdapter(adapter);
   		            		setDatos();
   		            	}catch(Throwable e){
   		            		//Puede causarse un error si mientras se realiza la busqueda... se salen.
   		            	}
   					}
   				});
			}
			
		};
		lv_medidores.setVisibility(View.GONE);
		pb_ruleta.setVisibility(View.VISIBLE);

		tv_msj_buscar.setVisibility(View.GONE);
		busqueda.start();
		esconderTeclado();
	}
	
	public void obtenerMedidor() throws Throwable{
		switch (bm_papa.tipoDeMedidoresABuscar) {
		case BuscarMedidor.SIN_LECTURA:
			tll.siguienteMedidorACapturarSinVuelta(false, false);
			break;
		case BuscarMedidor.CON_LECTURA:
			tll.siguienteMedidorACapturarSinVuelta(true, false);
			break;
		case BuscarMedidor.TODOS:
			tll.siguienteMedidorIndistinto();
			break;
		}
	}
	
	public void esconderTeclado() {
		InputMethodManager mgr = (InputMethodManager) bm_papa.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(et_medidor.getWindowToken(), 0);
	}

//	private void regresaResultado(int secuencia){
//		Intent intent= new Intent();
//		intent.putExtra("secuencia", secuencia);
//		
//		setResult(Activity.RESULT_OK, intent);
//		
//		bm_papafinish();
//	}
	
	public void reinicializaTAB(){
		pb_ruleta.setVisibility(View.GONE);
		tv_msj_buscar.setVisibility(View.VISIBLE);
		lv_medidores.setVisibility(View.GONE);
		et_medidor.getText().clear();
		
		switch(tipo){
		
		case BuscarMedidorTabsPagerAdapter.DIRECCION:
			tv_msj_buscar.setText(R.string.msj_buscar_direccion);
			break;
		case BuscarMedidorTabsPagerAdapter.NUMERO:
			tv_msj_buscar.setText(R.string.msj_buscar_numero);
			break;
		case BuscarMedidorTabsPagerAdapter.MEDIDOR:
			tv_msj_buscar.setText(R.string.msj_buscar);
			break;
		case BuscarMedidorTabsPagerAdapter.CALLES:
			buscar();
			break;
		}
	}
	
	
	}
	
