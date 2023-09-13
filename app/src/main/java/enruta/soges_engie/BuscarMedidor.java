package enruta.soges_engie;


import enruta.soges_engie.R;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


@SuppressLint("NewApi")
public class BuscarMedidor extends FragmentActivity implements TabListener {
	
	
	final static int BUSCAR=0;
	final static int MOVER=1;

	int tipoDeMedidoresABuscar=0;
	
	final static int SIN_LECTURA=0;
	final static int CON_LECTURA=1;
	final static int TODOS=2;
	int ii_lastSelectedTab=0;
	
	boolean seSelecciono=false;
	
	private int[] tabs ={ R.string.lbl_medidor,R.string.lbl_direccion, R.string.lbl_ic};
	
	private ActionBar actionBar;
	private ViewPager viewPager;
	private BuscarMedidorTabsPagerAdapter mAdapter;
	
	int ii_tipoDeBusqueda=BUSCAR;
	
	boolean cambiandoFiltro=false;

	Globales globales;
	
	boolean bModificar=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buscar_medidor);
		
		globales = ((Globales) getApplicationContext());
		
		viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new BuscarMedidorTabsPagerAdapter(getSupportFragmentManager(), globales.remplazarDireccionPorCalles);
        int li_tabSelected=2;
        
        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        
 
        actionBar.removeAllTabs();
        // Adding Tabs
        for (int tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText( globales.remplazarDireccionPorCalles && tab_name==BuscarMedidorTabsPagerAdapter.DIRECCION?R.string.lbl_calles:tab_name)
                    .setTabListener(this).setTag(tabs));
        }
        
        Bundle bu_params = getIntent().getExtras();
		
		bModificar=bu_params.getBoolean("modificar");
		ii_tipoDeBusqueda=bu_params.getInt("tipoDeBusqueda");
		
		if (bModificar)
			tipoDeMedidoresABuscar=TODOS;
        

        
        
        viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                        ii_lastSelectedTab=position;
                    }
                });
        viewPager.setCurrentItem(li_tabSelected);
        //getActionBar().setSelectedNavigationItem(li_tabSelected);
		
      //actionBar.setSelectedNavigationItem(li_tabSelected);
        //viewPager.setCurrentItem(li_tabSelected);
	}
	
//	public void esconderTeclado() {
//		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		mgr.hideSoftInputFromWindow(et_medidor.getWindowToken(), 0);
//	}
	
	
	void regresaResultado(int secuencia){
		Intent intent= new Intent();
		intent.putExtra("secuencia", secuencia);
		
		setResult(Activity.RESULT_OK, intent);
		
		finish();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bucar_medidor_menu, menu);

		manejaEstadosDelMenu(menu);

		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		// En este metodo se cambian las opciones del menu
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.bucar_medidor_menu, menu);



		manejaEstadosDelMenu(menu);

		return super.onPrepareOptionsMenu(menu);
	}


	private void manejaEstadosDelMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuItem mi_sinLectura = menu.findItem(R.id.m_sinLectura);
		MenuItem mi_conLectura = menu.findItem(R.id.m_conLectura);
		MenuItem mi_todos = menu.findItem(R.id.m_todos);
		
		cambiandoFiltro=true;
		mi_sinLectura.setChecked(false);
		mi_sinLectura.setIcon(R.drawable.ic_sin_lectura_unselected);
		mi_conLectura.setChecked(false);
		mi_conLectura.setIcon(R.drawable.ic_con_lectura_unselected);
		mi_todos.setChecked(false);
		mi_todos.setIcon(R.drawable.ic_todas_las_lecturas_unchecked);
		switch (tipoDeMedidoresABuscar) {
		case SIN_LECTURA:
			mi_sinLectura.setChecked(true);
			mi_sinLectura.setIcon(R.drawable.ic_sin_lectura_selected);
			break;
		case CON_LECTURA:
			mi_conLectura.setChecked(true);
			mi_conLectura.setIcon(R.drawable.ic_con_lectura_selected);
			break;
		case TODOS:
			mi_todos.setChecked(true);
			mi_todos.setIcon(R.drawable.ic_todas_las_lecturas_checked);
			break;
		}
		
		
//		if (seSelecciono){
//			Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
//		     // based on the current position you can then cast the page to the correct 
//		     // class and call the method:
//		     if (viewPager.getCurrentItem() == 0 && page != null) {
//		          ((BuscarMedidorFragment)page).buscar();     
//		     } 
//		}
//		seSelecciono=false;
		
	}
	
	@SuppressLint("NewApi")
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.m_sinLectura:
			tipoDeMedidoresABuscar= SIN_LECTURA;
			
			break;
		case R.id.m_conLectura:
			tipoDeMedidoresABuscar= CON_LECTURA;
			break;
		case R.id.m_todos:
			tipoDeMedidoresABuscar= TODOS;
			break;
			

		}

		if (Build.VERSION.SDK_INT >= 11)
			invalidateOptionsMenu();
		
		//seSelecciono=true;
		
		Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
	     // based on the current position you can then cast the page to the correct 
	     // class and call the method:
	     if (/*viewPager.getCurrentItem() == 0 &&*/ page != null) {
	          ((BuscarMedidorFragment)page).buscar();     
	     } 
		return true;
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
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + tab.getPosition());
	     // based on the current position you can then cast the page to the correct 
	     // class and call the method:
	     if (/*viewPager.getCurrentItem() == 0 &&*/ page != null) {
	          ((BuscarMedidorFragment)page).reinicializaTAB();     
	     } 
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
	
