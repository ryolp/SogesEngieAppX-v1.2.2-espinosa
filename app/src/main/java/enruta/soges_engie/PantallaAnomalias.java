package enruta.soges_engie;
import android.annotation.SuppressLint;
import android.app.ActionBar.TabListener;
import android.app.ActionBar.Tab;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Toast;




@SuppressLint("NewApi")
public class PantallaAnomalias extends FragmentActivity implements TabListener {
	DBHelper dbHelper;
	SQLiteDatabase db;
//	ListView lv_lista;
	
	String is_anomalia;
	int ii_secuencial;
	String is_lectura="";
	
	boolean tieneMensaje=false;

	String is_anomaliaSelec="", is_subAnomSelect="", is_comentarios="", is_desc="";
	Cursor c;
	
	int ii_lastSelectedTab=0;
	
	private int[] tabs = { /*R.string.lbl_recientes, R.string.lbl_mas_usadas, */R.string.lbl_todas};
	
	private ActionBar actionBar;
	private ViewPager viewPager;
	private PantallaAnomaliasTabsPagerAdapter mAdapter;
	
	int ii_vp_height=0, ii_vp_width=0;
	
	Globales globales;
	
	String anomaliaTraducida;
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.anomalias);
		String ls_filtro="";
		
		globales = ((Globales) getApplicationContext());
		
		viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new PantallaAnomaliasTabsPagerAdapter(getSupportFragmentManager());
        
        int li_tabSelected=ii_lastSelectedTab;
        
        final PantallaAnomalias pa= this;
        
        Bundle bu_params= getIntent().getExtras();
		
		ii_secuencial=bu_params.getInt("secuencial");
		try{
			is_lectura=bu_params.getString("lectura");
		}catch(Throwable e){
			
		}
		is_anomalia=bu_params.getString("anomalia");
		
		//Tenemos que verificar si tenemos ya seleccionada una anomalia, si ya hay una debemos mostrar la pagina de todas.
//		if (!bu_params.getString("anomalia").equals("")){
//			li_tabSelected=PantallaAnomaliasTabsPagerAdapter.TODAS;
//		}else{
//			//verificamos si exite un uso de anomalias
//			openDatabase();
//			Cursor c;
//			
//			c= db.rawQuery("Select count(*) canti  from Anomalia anoma, usoAnomalias uso where anoma.anomalia=uso.anomalia and subanomalia<>'S' and subanomalia<>'A' and activa='A' and fecha<>'' "+getFiltro(), null);
//			
//			c.moveToFirst();
//			if (c.getInt(c.getColumnIndex("canti"))==0){
//				//Quiere decir que no hay ninguna anomalia para mostrar en los primeros dos listados
//				li_tabSelected=PantallaAnomaliasTabsPagerAdapter.TODAS;
//			}
//			
//			c.close();
//			closeDatabase();
//		}
		
		li_tabSelected=globales.ultimaPestanaAnomaliasUsada;
		
		//li_tabSelected=ii_lastSelectedTab;
        
        ViewTreeObserver vto = viewPager.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				ii_vp_width=viewPager.getHeight();
				ii_vp_height=viewPager.getWidth();
				
				viewPager.setAdapter(mAdapter);
		        actionBar.setHomeButtonEnabled(false);
		        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        
		 
		        actionBar.removeAllTabs();
		        int i=0;
		        // Adding Tabs
		        int selectedTab=ii_lastSelectedTab;
		        for (int tab_name : tabs) {
		        	if (i==selectedTab){
		        		actionBar.addTab(actionBar.newTab().setText(tab_name)
			                    .setTabListener(pa).setTag(tabs), true);
		        	}else{
		        		actionBar.addTab(actionBar.newTab().setText(tab_name)
			                    .setTabListener(pa).setTag(tabs));
		        	}
		            i++;
		        }
				ViewTreeObserver obs = viewPager.getViewTreeObserver();
				obs.removeGlobalOnLayoutListener(this);
			}
		});
        
		viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                        ii_lastSelectedTab=position;
                        globales.ultimaPestanaAnomaliasUsada=ii_lastSelectedTab;
                    }
                });
		ii_lastSelectedTab=li_tabSelected;
		//actionBar.setSelectedNavigationItem(li_tabSelected);
        //viewPager.setCurrentItem(li_tabSelected);
	}
	
	public void mandarAnomalia(){
		Intent intent= new Intent();
		int longAnomalia;
		globales.tdlg.RealizarModificacionesDeAnomalia(is_subAnomSelect);
		if (globales.convertirAnomalias)
			longAnomalia=this.getResources().getInteger(R.integer.ANOM_LONG_CONV);
		else
			longAnomalia=globales.tlc.getLongCampo("anomalia");

		String ls_anomalia= is_anomaliaSelec;//Main.rellenaString(is_anomaliaSelec, "0", longAnomalia, true);
		
		intent.putExtra("anomalia", is_anomaliaSelec);
		intent.putExtra("subAnomalia", is_subAnomSelect);
		intent.putExtra("comentarios", is_comentarios);
		
		//Antes de continuar... vamos a guardar el numero de selecciones de la anomalia y la ultima fecha de uso
		openDatabase();
		
		//Primero verificamos si ya tiene un registro en usoAnomalias
		Cursor c;
		c= db.rawQuery("Select count(*) canti from usoAnomalias where anomalia='"+ls_anomalia+"'" , null);
		c.moveToFirst();
		if (c.getInt(c.getColumnIndex("canti"))>0)
			db.execSQL("update usoAnomalias set veces=veces + 1, fecha='" + Main.obtieneFecha("ymdhis")+"' where anomalia='"+ls_anomalia+"'");
		else
			db.execSQL("insert into usoAnomalias(anomalia, veces, fecha) values('"+ls_anomalia+"', 1, '" + Main.obtieneFecha("ymdhis")+"' )");
		closeDatabase();
		setResult(Activity.RESULT_OK, intent);
		this.finish();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bundle bu_params;
		PantallaAnomaliasFragment page = (PantallaAnomaliasFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" +ii_lastSelectedTab);
	     // based on the current position you can then cast the page to the correct 
	     // class and call the method:
	     
		switch(requestCode){
		case TomaDeLecturas.COMENTARIOS:
			if (resultCode == Activity.RESULT_OK){
				bu_params =data.getExtras();
				if (is_subAnomSelect.equals("")){
					globales.tdlg.regresaDeCamposGenericos(bu_params, is_anomaliaSelec);
					globales.tdlg.RealizarModificacionesDeAnomalia(is_anomaliaSelec, bu_params.getString("input"));
					//globales.tdlg.RealizarModificacionesDeAnomalia(is_anomaliaSelec);
				}
				else{
					globales.tdlg.regresaDeCamposGenericos(bu_params, is_subAnomSelect);
					globales.tdlg.RealizarModificacionesDeAnomalia(is_subAnomSelect, bu_params.getString("input"));
					
				}
				//is_comentarios=bu_params.getString("input");
				 mandarAnomalia();
			}
			else{
//				setResult(Activity.RESULT_CANCELED);
//				this.finish();
				if ( page != null) {
					if (page.tieneSubanomalia){
						page.tieneSubanomalia=false;
						page.selectAnomalia(is_anomaliaSelec);
					}
					else{
						page.reinicializaTAB();
					}
			     }
			}
			break;
		case TomaDeLecturas.INPUT_CLIENTE_YA_PAGO:
			if (resultCode == Activity.RESULT_OK){
				bu_params =data.getExtras();
				if (is_subAnomSelect.equals("")){
					globales.tdlg.regresaDeCamposGenericos(bu_params, is_anomaliaSelec);
					globales.tdlg.RealizarModificacionesDeAnomalia(is_anomaliaSelec, bu_params.getString("input"));
				}
				else{
					globales.tdlg.regresaDeCamposGenericos(bu_params, is_subAnomSelect);
					globales.tdlg.RealizarModificacionesDeAnomalia(is_subAnomSelect, bu_params.getString("input"));
				}
				mandarAnomalia();
			}
			break;
		case TomaDeLecturas.INPUT_CAMPOS_GENERICO:
			if (resultCode == Activity.RESULT_OK){
				bu_params =data.getExtras();
				if (is_subAnomSelect.equals("")){
					globales.tdlg.regresaDeCamposGenericos(bu_params, is_anomaliaSelec);
					globales.tdlg.RealizarModificacionesDeAnomalia(is_anomaliaSelec, bu_params.getString("input"));
				}
				else{
					globales.tdlg.regresaDeCamposGenericos(bu_params, is_subAnomSelect);
					globales.tdlg.RealizarModificacionesDeAnomalia(is_subAnomSelect, bu_params.getString("input"));
				}
				 mandarAnomalia();
			}
			else{
//			setResult(Activity.RESULT_CANCELED);
//			this.finish();
			if ( page != null) {
				if (page.tieneSubanomalia){
					page.tieneSubanomalia=false;
					page.selectAnomalia(is_anomaliaSelec);
				}
				else{
					page.reinicializaTAB();
				}
		     }
		}
			break;
		}
	}

//	public void esconderTeclado() {
//		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		mgr.hideSoftInputFromWindow(li_anomalia.getWindowToken(), 0);
//	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.m_anomalias, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.m_borrarAnom:
				if (globales.tll.getLecturaActual().getAnomaliasABorrar().respuestas.size()>1){
					//Muestra mensaje
					anomaliasABorrar(globales.tll.getLecturaActual().getAnomaliasABorrar());
				}
				else if (globales.tll.getLecturaActual().getAnomaliasABorrar().respuestas.size()==1)
				{
//					if (globales.tll.getLecturaActual().anomalias.get(0).is_activa.equals("I")){
//						// si es inactiva, ni la toques.
//						return true;
//					}
//					//Solo hay una, asi que la borramos
					if(globales.tll.getLecturaActual().deleteAnomalia(globales.tll.getLecturaActual().getAnomaliasAIngresadas())) {
						globales.BorrarTodasLosCamposEngie();
						Toast.makeText(this, R.string.msj_anomalias_borrada, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(this, R.string.msj_anomalias_error_borrado, Toast.LENGTH_LONG).show();
					}
					setResult(Activity.RESULT_CANCELED);
					this.finish();
				}
				else{
					//No hay... no puedo borrar
					setResult(Activity.RESULT_CANCELED);
					this.finish();
				}
				//borrarAnomalia();
				break;
				
//			case R.id.m_borrarEstaditicas:
//				openDatabase();
//				db.execSQL("delete from usoAnomalias");
//				
//				closeDatabase();
//				
////				//La ultima pestaña son todas, no se actualiza
//				for (int i=0; i<2; i++){
//					Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + i);
//				     // based on the current position you can then cast the page to the correct 
//				     // class and call the method:
//				     if (/*viewPager.getCurrentItem() == 0 &&*/ page != null) {
//				          ((PantallaAnomaliasFragment)page).reinicializaTAB();     
//				     } 
//				}
//				
//				
//				
//			
//				
//			break;
		}
		return true;
	}
	
public void anomaliasABorrar(MensajeEspecial me){
	
	final PantallaAnomalias pa= this;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(me.descripcion).setItems(me.getArregloDeRespuestas(), 
				new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int id) {
								if(globales.tll.getLecturaActual().deleteAnomalia(id))
									Toast.makeText(pa, R.string.msj_anomalias_borrada, Toast.LENGTH_SHORT).show();
								else
									Toast.makeText(pa, R.string.msj_anomalias_error_borrado, Toast.LENGTH_LONG).show();
							}
						});
		builder.show();
	}

	public void borrarAnomalia(){
		Intent intent= new Intent();
		
		intent.putExtra("anomalia", "");
		intent.putExtra("subAnomalia", "");
		intent.putExtra("comentarios", "");
		
		setResult(Activity.RESULT_OK, intent);
		
		this.finish();
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		viewPager.setCurrentItem(tab.getPosition());
		 ii_lastSelectedTab=tab.getPosition();
		 globales.ultimaPestanaAnomaliasUsada=ii_lastSelectedTab;
		
		 is_anomaliaSelec="";
		 is_subAnomSelect="";
		 is_comentarios="";
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
		Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + tab.getPosition());
	     // based on the current position you can then cast the page to the correct 
	     // class and call the method:
	     if (/*viewPager.getCurrentItem() == 0 &&*/ page != null) {
	          ((PantallaAnomaliasFragment)page).reinicializaTAB();     
	     }
	}

	public String getFiltro() {
		// TODO Auto-generated method stub
		if (globales.filtrarAnomaliasConLectura){
			if (is_lectura.length() > 0)
				return " and (lectura='1' or ausente='0')";
			else
				return " and (lectura='0' and ausente='4')";
		}
		return "";
	}
	
	@Override
	public void onBackPressed() {	
		PantallaAnomaliasFragment page = (PantallaAnomaliasFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" +ii_lastSelectedTab);
	     // based on the current position you can then cast the page to the correct 
	     // class and call the method:
	     if (/*viewPager.getCurrentItem() == 0 &&*/ page != null) {
	    	 if (page.tieneSubanomalia)
	    		 page.reinicializaTAB();    
	    	 else{
	    		 Intent resultado = new Intent();
	 			setResult(Activity.RESULT_CANCELED, resultado);
	    		 finish();
	    	 }
	     }
	}
	
	  public void mensajeOK(String ls_mensaje){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(ls_mensaje)
			       .setCancelable(false)
			       .setNegativeButton(R.string.aceptar, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id){
			                dialog.cancel();
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
		}
	  
	  public void openDatabase(){
			dbHelper= new DBHelper(this);
			db = dbHelper.getReadableDatabase();
		}
		
		public void closeDatabase(){
			db.close();
			dbHelper.close();
		}
	
		@Override
		protected void onResume(){
			//Ahora si abrimos
			if (globales.tdlg==null){
				super.onResume();
				Intent i = getBaseContext().getPackageManager()
			             .getLaunchIntentForPackage( getBaseContext().getPackageName() );
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				System.exit(0);
				return;
			}
			super.onResume();
		}
}
