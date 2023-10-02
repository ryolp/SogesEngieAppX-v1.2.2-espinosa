package enruta.soges_engie;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Date;

import enruta.soges_engie.clases.AppException;
import enruta.soges_engie.clases.OperacionesMgr;
import enruta.soges_engie.clases.OperacionRequest;
import enruta.soges_engie.clases.OperacionResponse;
import enruta.soges_engie.clases.PuntosGpsMgr;
import enruta.soges_engie.clases.Utils;
import enruta.soges_engie.entities.ResumenEntity;
import enruta.soges_engie.services.DbLecturasMgr;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Main extends FragmentActivity implements TabListener {

    final static int IMPORTAR = 0;
    final static int EXPORTAR = 1;
    final static int LECTURAS = 2;

    /**
     * Tiempo en segundo que se tardará el GPS
     */
    final static int TIME_OUT_GPS = 10;

    /**
     * Tiempo limite ente puntos GPS
     */
    private static final int TIME_LIMIT = 1000 * 30;


    final static int REQUEST_ENABLE_BT_IMP = 3;
    final static int REQUEST_ENABLE_BT_EXP = 4;

    final static int CONFIG = 5;

    final static int RECEPCION = 6;
    final static int TRANSMISION = 7;

    final static int CAMBIAR_USUARIO = 1;

    final static int FOTO_CHECK_SEGURIDAD = 10;     // RL, 2023-09,
    final static int FOTO_PROBAR_CAMARA = 11;

    //	LocationManager locationManager;
    LocationManager locationManager2;
    LocationListener locationListener2;
//	LocationListener locationListener;

    private int[] tabs = { /*R.string.lbl_principal,*/ R.string.lbl_resumen};
    boolean gpsEncendido;
    DBHelper dbHelper;
    SQLiteDatabase db;

    String is_nombre_Lect = "";

    TextView tv_versionNum;
    TextView tv_resumen;

    int versionNum;
    String version;

    int infoFontSize = 25;
    int versionFontSize = 13;

    Double porcentaje = 1.0;
    float factorPorcentaje = 0.10f;
    Double porcentaje2 = 1.0;

    int ii_rol = CPL.SUPERUSUARIO;

    Intent lrs;
    boolean esSuperUsuario = false;

    boolean bHabilitarImpresion = false;
    private ActionBar actionBar;
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    Button b_lecturas;
    int ii_lastSelectedTab = 0;
    Globales globales;

    AlertDialog alert;
    Handler mHandler;

    boolean cargarDespuesDeConfig = false;

    Vector<String> log = new Vector<String>();

    boolean primerPunto = true;

    private PuntosGpsMgr mPuntosGpsMgr = null;  // RL, 2023-09, Clase para el envío de los puntos GPS
    private Thread mThreadPuntosGps = null;     // RL, 2023-09, Thread para registrar periodicamente los puntos GPS
    private DialogoMensaje mDialogoMsg = null;  // RL, 2023-09, Clase para mostrar un dialogo de mensajes al usuario
    private Button btnOperacion;                // RL, 2023-09, Botón para Check-In, Check-Out y Check de Seguridad
    private OperacionesMgr mOperacionesMgr = null;

    /* ====================================================================================
        Creación del activity
    ==================================================================================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.);

        try {
            setContentView(R.layout.main_tabs);

            globales = ((Globales) getApplicationContext());

            porcentaje = globales.porcentaje_main;
            porcentaje2 = globales.porcentaje_main2;

            mHandler = new Handler();

//		
//		openDatabase();
//		db.execSQL("update ruta set verDatos=0");
//		closeDatabase();

            b_lecturas = (Button) this.findViewById(R.id.b_lecturas);

            b_lecturas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    inicia_tdl(view);
                }
            });
            //setTabs();

//		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
//        actionBar = getActionBar();
//        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
//        
//        viewPager.setAdapter(mAdapter);
//        actionBar.setHomeButtonEnabled(false);
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        
// 
//        // Adding Tabs
//        for (String tab_name : tabs) {
//            actionBar.addTab(actionBar.newTab().setText(tab_name)
//                    .setTabListener(this));
//        }

            setTitle("");

            Bundle bu_params = this.getIntent().getExtras();

            ii_rol = bu_params.getInt("rol");
            esSuperUsuario = bu_params.getBoolean("esSuperUsuario");
            if (CPL.LECTURISTA == ii_rol) {
                is_nombre_Lect = bu_params.getString("nombre");

                String ruta = getStringValue("ruta_descarga");
//		if (ruta.endsWith("Pruebas")){
////			globales.usuarioBD="u1003479_pruebas";
////			globales.serverBD= "db1003479_prueba";
//			globales.usuarioBD="u1003479_pruebc3";
//			globales.serverBD= "db1003479_cortexPrueba3";
//			globales.passwordBD= "Sotixe_69";
//		}
//		else if (ruta.endsWith("Pruebas2")){
//			globales.usuarioBD="u1003479_pruebc2";
//			globales.serverBD= "db1003479_cortexprueba2";
//			globales.passwordBD= "Sotixe_69";
//		}
//		else if (ruta.endsWith("Pruebas3")){
//			globales.usuarioBD="u1003479_pruebc3";
//			globales.serverBD= "db1003479_cortexPrueba3";
//			globales.passwordBD= "Sotixe_69";
//		}
//		else{
                globales.usuarioBD = "u1007397_webapp2";
                globales.serverBD = "db1007397_CortrexDemoDev";
                globales.passwordBD = "Sis#web#apps@2022";
//		}
            }


            //actualizaResumen();
            agregaRegistrosConfig();

            globales.calidadDeLaFoto = getIntValue("calidad_foto", globales.calidadDeLaFoto);
            globales.sonidos = getIntValue("sonidos", 0) == 0;


            tv_versionNum = (TextView) findViewById(R.id.tv_version);

            try {
                versionNum = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                tv_versionNum.setText(tv_versionNum.getText().toString() + " " + versionNum + "\n(" + version + ")");

            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //prueba();

//		versionFontSize=getIntValue( "versionFontSize",  versionFontSize);
//		infoFontSize=getIntValue( "infoFontSize",  infoFontSize);
//		
//		
//		
//		tv_versionNum.setTextSize(versionFontSize);
//		tv_resumen.setTextSize(infoFontSize);
            porcentaje = getDoubleValue("porcentaje_main", porcentaje);
            porcentaje2 = getDoubleValue("porcentaje2_main", porcentaje2);

            setSizes();

            //Quiero ver si existen los parametros... los agregamos si no
            openDatabase();

            Cursor c = db.rawQuery("Select * from config where key='server_gprs'", null);
            int canti = c.getCount();
            c.close();
            closeDatabase();
            if (canti == 0) {
                //Abrimos y cerramos
                Intent intent = new Intent(this, Configuracion.class);
                intent.putExtra("guardar", 1);
                intent.putExtra("rol", ii_rol);
                startActivityForResult(intent, CONFIG);

                cargarDespuesDeConfig = true;

            } else {
                estableceVariablesDeClave();
            }

            cargarOrdenes();


            actualizaTabs();
		/*View customNav = LayoutInflater.from(this).inflate(R.layout.configuracion, null);
		getActionBar().setCustomView(customNav);*/

            //invalidateOptionsMenu();

            //GrabarSDCard();

            enciendeGPS();
            rutinaDeEnvioDePuntos();
            activaEnvioPuntosRuta();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void actualizaTabs() {
        if (globales.tdlg == null)
            return;
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        int li_tabSelected = ii_lastSelectedTab;

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.removeAllTabs();
        // Adding Tabs
        for (int tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }

//     // Create a tab listener that is called when the user changes tabs.
//        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
//            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
//                // When the tab is selected, switch to the
//                // corresponding page in the ViewPager.
//            	viewPager.setCurrentItem(tab.getPosition());
//            }
//
//			@Override
//			public void onTabReselected(Tab tab, FragmentTransaction ft) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
//				// TODO Auto-generated method stub
//				
//			}
//            
//        };


        viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                        ii_lastSelectedTab = position;
                    }
                });
        viewPager.setCurrentItem(li_tabSelected);
        //getActionBar().setSelectedNavigationItem(li_tabSelected);

        inicializarControlesCheck();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem mi_lecturas, mi_filtrado, mi_exportar, mi_importar, mi_borrarRuta, mi_tamanoDeFuente, mi_grabarEnSD, mi_config, mi_cierreForzado;

        mi_lecturas = menu.findItem(R.id.m_lecturas);
        mi_filtrado = menu.findItem(R.id.m_filtrar);
        mi_exportar = menu.findItem(R.id.m_exportar);
        mi_importar = menu.findItem(R.id.m_importar);
        mi_borrarRuta = menu.findItem(R.id.m_borrarruta);
        mi_tamanoDeFuente = menu.findItem(R.id.m_verTamanosLetra);
        mi_grabarEnSD = menu.findItem(R.id.m_grabarEnSD);
        mi_config = menu.findItem(R.id.m_configuracion);
        mi_cierreForzado = menu.findItem(R.id.m_cierreForzado);

        //Vamos a establecer los roles encesarios
        switch (ii_rol) {
            case CPL.ADMINISTRADOR:
                mi_lecturas.setVisible(false);
                mi_filtrado.setVisible(false);
                mi_exportar.setVisible(false);
                mi_importar.setVisible(false);
                if (globales.mostrarGrabarEnSD) {
                    mi_grabarEnSD.setVisible(true);
                } else {
                    mi_grabarEnSD.setVisible(false);
                }
                mi_cierreForzado.setVisible(false);

                break;
            case CPL.LECTURISTA:
                mi_config.setVisible(true);
                mi_borrarRuta.setVisible(false);
                mi_lecturas.setVisible(false);
                b_lecturas.setVisibility(View.VISIBLE);
                if (globales.mostrarGrabarEnSD) {
                    mi_grabarEnSD.setVisible(true);
                } else {
                    mi_grabarEnSD.setVisible(false);
                }

                break;


        }

        if (!esSuperUsuario) {
            mi_tamanoDeFuente.setVisible(false);
        }

        return true;
    }

    public static String rellenaString(String texto, String relleno, int veces, boolean lugar) {
        String ls_final = texto;
        int li_restantes;

        li_restantes = veces - texto.length();

        for (int i = 0; i < li_restantes; i++) {
            if (lugar)
                ls_final = relleno + ls_final;
            else
                ls_final = ls_final + relleno;
        }
        return ls_final;
    }

    public static String obtieneFecha() {
        String ls_folio;
        Calendar c = Calendar.getInstance();

        ls_folio = rellenaString(String.valueOf(c.get(Calendar.YEAR)), "0", 4, true)
                + rellenaString(String.valueOf(c.get(Calendar.MONTH) + 1), "0", 2, true)
                + rellenaString(String.valueOf(c.get(Calendar.DAY_OF_MONTH)), "0", 2, true)
                + rellenaString(String.valueOf(c.get(Calendar.HOUR_OF_DAY)), "0", 2, true)
                + rellenaString(String.valueOf(c.get(Calendar.MINUTE)), "0", 2, true)
                + rellenaString(String.valueOf(c.get(Calendar.SECOND)), "0", 2, true);
        return ls_folio;
    }

    public static String obtieneFecha(String ls_format) {
        String ls_folio = "", ls_letraAct;
        Calendar c = Calendar.getInstance();
        for (int i = 0; i < ls_format.length(); i++) {
            ls_letraAct = ls_format.substring(i, i + 1)/*.toLowerCase()*/;
            if (ls_letraAct.equals("y")) {
                ls_folio += rellenaString(String.valueOf(c.get(Calendar.YEAR)), "0", 4, true);
            } else if (ls_letraAct.equals("Y")) {
                ls_folio += rellenaString(String.valueOf(c.get(Calendar.YEAR)).substring(2), "0", 2, true);
            } else if (ls_letraAct.equals("m")) {
                ls_folio += rellenaString(String.valueOf(c.get(Calendar.MONTH) + 1), "0", 2, true);
            } else if (ls_letraAct.equals("d")) {
                ls_folio += rellenaString(String.valueOf(c.get(Calendar.DAY_OF_MONTH)), "0", 2, true);
            } else if (ls_letraAct.equals("h")) {
                ls_folio += rellenaString(String.valueOf(c.get(Calendar.HOUR_OF_DAY)), "0", 2, true);
            } else if (ls_letraAct.equals("i")) {
                ls_folio += rellenaString(String.valueOf(c.get(Calendar.MINUTE)), "0", 2, true);
            } else if (ls_letraAct.equals("s")) {
                ls_folio += rellenaString(String.valueOf(c.get(Calendar.SECOND)), "0", 2, true);
            } else {
                ls_folio += ls_letraAct;
            }


        }
        return ls_folio;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        final Main main = this;
        AlertDialog.Builder builder;

        try {
            String ls_opciones[] = {getString(R.string.lbl_configuracion_modo_WIFI), getString(R.string.lbl_configuracion_modo_BT)};
            switch (item.getItemId()) {
                case R.id.m_importar:
                    hacerImportar();
//			 switch (tipoDeTransmisionPredeterminada()){
//			 	case 0: //Mostrar todas
//			 		builder = new AlertDialog.Builder(this);
//				    builder.setTitle(R.string.msj_main_select_metodo_trans)
//				           .setItems(ls_opciones, new DialogInterface.OnClickListener() {
//				               public void onClick(DialogInterface dialog, int which) {
//				            	   boolean ejecutar=true;
//				            	   switch (which) {
//				            	   case 0: //Wifi
//				            		   lrs = new Intent(main, trasmisionDatos.class);
//				            		   break;
//				            	   case 1: //bt
//
////				            		   BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
////				            		   if (mBluetoothAdapter != null) {
////				            			   if (!mBluetoothAdapter.isEnabled()) {
////				            				   lrs = new Intent(main, trasmisionDatosBt.class);
////				            				    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
////				            				    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_IMP);
////				            				    ejecutar=false;
////				            				}
////				            			   else{
////				            				   lrs = new Intent(main, trasmisionDatosBt.class);
////				            			   }
////				            		   }
////				            		   else{
////				            			   mensajeOK("Bluetooth no disponible.");
////				            			   return;
////				            		   }
//				            		   ejecutar=bluetoothDisponible(REQUEST_ENABLE_BT_IMP);
//				            		   break;
//				            	   }
//				            	   lrs.putExtra("tipo", trasmisionDatos.RECEPCION);
//				            	   if (ejecutar)
//				       				startActivityForResult(lrs, IMPORTAR);
//
//
//				           }
//				    });
//				    builder.show();
//				    break;
//			 	case 1: //WIFI
//			 		 lrs = new Intent(main, trasmisionDatos.class);
//			 		lrs.putExtra("tipo", trasmisionDatos.RECEPCION);
//			 		startActivityForResult(lrs, IMPORTAR);
//			 		break;
//			 	case 2: //bt
//			 		if (bluetoothDisponible(REQUEST_ENABLE_BT_IMP)){
//			 			lrs.putExtra("tipo", trasmisionDatos.RECEPCION);
//	            		 startActivityForResult(lrs, IMPORTAR);
//	            	 }
//			 		break;
//
//			 }

                    break;
                case R.id.m_exportar:
                    openDatabase();

                    db.execSQL("delete from fotos where temporal=" + CamaraActivity.TEMPORAL + " or temporal=" + CamaraActivity.ANOMALIA);


                    closeDatabase();

                    //aqui verificamos si hay una transmision predeterminada
                    switch (tipoDeTransmisionPredeterminada()) {
                        case 0: //Mostrar todas
                            builder = new AlertDialog.Builder(this);
                            builder.setTitle(R.string.msj_main_select_metodo_trans)
                                    .setItems(ls_opciones, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            boolean ejecutar = true;
                                            switch (which) {
                                                case 0: //Wifi
                                                    lrs = new Intent(main, trasmisionDatos.class);
                                                    break;
                                                case 1: //bt

//	     		            		   BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//	     		            		   if (mBluetoothAdapter != null) {
//	     		            			   if (!mBluetoothAdapter.isEnabled()) {
//	     		            				   lrs = new Intent(main, trasmisionDatosBt.class);
//	     		            				    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//	     		            				    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_EXP);
//	     		            				    ejecutar=false;
//	     		            				}
//	     		            			   else{
//	     		            				   lrs = new Intent(main, trasmisionDatosBt.class);
//	     		            				   
//	     		            			   }
//	     		            		   }
//	     		            		   else{
//	     		            			   mensajeOK("Bluetooth no disponible.");
//	     		            			   return;
//	     		            		   }
                                                    ejecutar = bluetoothDisponible(REQUEST_ENABLE_BT_EXP);
                                            }
                                            lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
                                            lrs.putExtra("transmiteFotos", true);
                                            lrs.putExtra("transmitirTodo", false);
                                            if (ejecutar)
                                                startActivityForResult(lrs, EXPORTAR);


                                        }
                                    });
                            builder.show();
                            break;
                        case 1: //wifi
                            lrs = new Intent(main, trasmisionDatos.class);
                            lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
                            lrs.putExtra("transmiteFotos", true);
                            lrs.putExtra("transmitirTodo", false);
                            startActivityForResult(lrs, EXPORTAR);
                            break;
                        case 2: //bt
                            if (bluetoothDisponible(REQUEST_ENABLE_BT_EXP)) {
                                lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
                                lrs.putExtra("transmiteFotos", true);
                                lrs.putExtra("transmitirTodo", false);
                                startActivityForResult(lrs, EXPORTAR);
                            }
                            break;

                    }
			
			
			/*lrs = new Intent(this, trasmisionDatos.class);
    		lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
    		lrs.putExtra("transmiteFotos", true);
    		lrs.putExtra("transmitirTodo", false);
			startActivityForResult(lrs, EXPORTAR);*/
                    break;
                case R.id.m_lecturas:
//			lrs = new Intent(this, TomaDeLecturas.class);
//			lrs.putExtra("esSuperUsuario", esSuperUsuario);
//			lrs.putExtra("nombre", this.is_nombre_Lect);
//			lrs.putExtra("bHabilitarImpresion", this.bHabilitarImpresion);
//    		
//			startActivityForResult(lrs,LECTURAS);
                    inicia_tdl(b_lecturas);
                    break;
                case R.id.m_filtrar:
                    lrs = new Intent(this, Filtrado.class);

                    startActivity(lrs);
                    break;
                case R.id.m_exportarTodo:
                    lrs = new Intent(this, trasmisionDatos.class);
                    lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
                    lrs.putExtra("transmiteFotos", true);
                    lrs.putExtra("transmitirTodo", true);
                    startActivityForResult(lrs, EXPORTAR);
                    break;
                case R.id.m_configuracion:
                    lrs = new Intent(this, Configuracion.class);
                    lrs.putExtra("guardar", 0);
                    lrs.putExtra("rol", ii_rol);
                    startActivityForResult(lrs/*, CONFIGURACION*/, CONFIG);
                    break;
                case R.id.m_salir:
                    finish();
                case R.id.m_cambiarUsuario:
                    Intent intent = new Intent();
                    intent.putExtra("opcion", CPL.CAMBIAR_USUARIO);

                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    break;

                case R.id.m_borrarruta:
                    builder = new AlertDialog.Builder(this);


                    builder.setMessage(R.string.str_warning_borrarRuta)
                            .setCancelable(false).setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    openDatabase();
                                    TransmisionesPadre.borrarRuta(db);
                                    closeDatabase();

                                    //actualizaResumen();
                                    actualizaTabs();

                                    Toast.makeText(main, R.string.msj_main_ruta_borrada, Toast.LENGTH_SHORT).show();

                                }
                            }).setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();

                                }
                            });
                    builder.show();

                    break;

                case R.id.m_acercaDe:
                    AlertDialog alert = null;
                    LayoutInflater inflater = this.getLayoutInflater();
                    ImageView iv_logo;

                    final View view = inflater.inflate(R.layout.cpl, null);
                    view.findViewById(R.id.b_admon).setVisibility(View.GONE);
                    view.findViewById(R.id.b_lecturista).setVisibility(View.GONE);
                    TextView tv_version = (TextView) view.findViewById(R.id.tv_version_lbl);

                    iv_logo = (ImageView) view.findViewById(R.id.iv_logo);
                    iv_logo.setImageResource(((Globales) this.getApplicationContext()).logo);

                    try {
                        tv_version.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode + ", " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);

                    } catch (NameNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    builder = new AlertDialog.Builder(this);

                    builder.setView(view).setCancelable(true);

                    alert = builder.create();


                    alert.show();


                    break;
                case R.id.m_verTamanosLetra:
                    //openDatabase();
                    String cadena = "";
                    //porcentaje_main, porcentaje_main2 (resumen), procentaje_hexateclado, porcentaje_teclado, porcentaje_lectura, porcentaje_info
                    cadena += "porcentaje_main " + getDoubleValue("porcentaje_main", globales.porcentaje_main);
                    cadena += "\nporcentaje_main2 " + getDoubleValue("porcentaje_main2", globales.porcentaje_main2);
                    cadena += "\nporcentaje_teclado " + getDoubleValue("porcentaje_teclado", globales.porcentaje_teclado);
                    cadena += "\nprocentaje_hexateclado " + getDoubleValue("procentaje_hexateclado", globales.porcentaje_hexateclado);
                    cadena += "\nporcentaje_lectura " + getDoubleValue("porcentaje_lectura", globales.porcentaje_lectura);
                    cadena += "\nporcentaje_info " + getDoubleValue("porcentaje_info", globales.porcentaje_info);
                    //closeDatabase();

                    mensajeOK(cadena);
                    break;
                case R.id.m_grabarEnSD:
                    GrabarSDCard();
                    break;
                case R.id.m_cierreForzado://Mensaje de si o no

                    builder = new AlertDialog.Builder(this);
                    builder.setMessage("¿Esta seguro de realizar el cierre forzado?")
                            .setCancelable(false)
                            .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    globales.tll = new TodasLasLecturas(main);
                                    globales.tll.forzarLecturas();

                                    Intent lrs = new Intent(main, trasmisionDatos.class);
                                    lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
                                    lrs.putExtra("transmiteFotos", true);
                                    startActivityForResult(lrs, EXPORTAR);
                                }
                            });

                    alert = builder.create();
                    alert.show();
                    break;
                case R.id.m_cerrarSesion:
                    cerrarSesion();
                    break;
                case R.id.m_probarCamara:
                    probarCamara();
                    break;
                case R.id.m_VerMapaDeTodos:
                    verMapaDeTodos();
                    break;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            mostrarMensaje("Alerta", "Ocurrió un problema inesperado", t);
        }

        return true;
    }

    private void verMapaDeTodos() {
        Lectura lectura;
        String miLatitud = "";
        String miLongitud = "";
        String uri = "";

        try {
// CE, 01/10/23, Vamos a poner fijo la Geoposicion hasta que nos llegue del servidor
//            miLatitud = lectura.getMiLatitud();
//            miLongitud = lectura.getMiLongitud();
            miLatitud = "25.696515021213962";
            miLongitud = "-100.34119561673539";

            if (miLatitud.trim().equals("") || miLongitud.trim().equals(""))
                return;

            uri = "geo:" + miLatitud + "," + miLongitud + "?q=" + miLatitud + "," + miLongitud + "(MapaDeLaRuta)&z=24";

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            this.startActivity(intent);
        } catch (Throwable t) {
            Utils.showMessageLong(this, t.getMessage());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bu_params;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (globales.sesionEntity == null) {
            cerrarSesion();
            return;
        }

        switch (requestCode) {
            case EXPORTAR:
                if (data == null) {
                    mensajeOK(getString(R.string.msj_main_operacion_cancelada));
                    return;

                }
                //actualizaResumen();
                actualizaTabs();
                bu_params = data.getExtras();

                if (resultCode == Activity.RESULT_OK) {
                    if (bu_params.getString("mensaje").length() > 0)
                        mensajeOK(bu_params.getString("mensaje"));

                } else {
                    mensajeOK(getString(R.string.msj_main_operacion_cancelada));
                }
                break;
            case IMPORTAR:

                //actualizaResumen();
                actualizaTabs();
                asignarSecuenciasReales();

                if (resultCode == Activity.RESULT_CANCELED) {
                    mensajeOK(getString(R.string.msj_main_operacion_cancelada));

                } else {
                    bu_params = data.getExtras();
                    if (bu_params.getString("mensaje").trim().length() > 0) {
                        //actualizaResumen();
                        actualizaTabs();
                        mensajeOK(bu_params.getString("mensaje"));

                    }
                    Intent lrs = new Intent(this, trasmisionDatos.class);
                    lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
                    startActivityForResult(lrs, TRANSMISION);
                }

                //actualizaResumen();
//                actualizaTabs();
//                asignarSecuenciasReales();

                break;

            case LECTURAS:
                //actualizaResumen();
                actualizaTabs();
                asignarSecuenciasReales();
                try {

                    bu_params = data.getExtras();
                    bHabilitarImpresion = bu_params.getBoolean("bHabilitarImpresion");
                } catch (Throwable e) {

                }

                break;
            case REQUEST_ENABLE_BT_IMP:
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter.isEnabled()) {
                    lrs = new Intent(this, trasmisionDatosBt.class);
                    lrs.putExtra("tipo", trasmisionDatos.RECEPCION);
                    startActivityForResult(lrs, IMPORTAR);
                }
                break;
            case REQUEST_ENABLE_BT_EXP:
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter.isEnabled()) {
                    lrs = new Intent(this, trasmisionDatosBt.class);
                    lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
                    startActivityForResult(lrs, EXPORTAR);

                }
                break;

            case CONFIG:

//				if (globales.sobreEscribirServidorConDefault){
//					//server_gprs, ruta_descarga
//					sobreEscribirCampos("server_gprs", globales.defaultServidorGPRS);
//					sobreEscribirCampos("ruta_descarga", globales.defaultRutaDescarga);
//				}

                actualizaTabs();

                if (cargarDespuesDeConfig) {
                    estableceVariablesDeClave();
                    cargarOrdenes();
                    cargarDespuesDeConfig = false;
                }


                break;

            case RECEPCION:
                actualizaTabs();
                asignarSecuenciasReales();
                //Recibimos si fue extoso el result
                if (resultCode == Activity.RESULT_OK) {

                    Intent lrs = new Intent(this, trasmisionDatos.class);
                    lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
                    startActivityForResult(lrs, TRANSMISION);
                }
                break;
            case FOTO_CHECK_SEGURIDAD:
                procesarFotoCheckSeguridad(requestCode, resultCode, data);
                break;
			case TRANSMISION:


//				//Actualizamos el secuencial Actual
//				globales.tll.getLecturaActual().corregirSecuenciaReal();
//				break;

        }
    }

    private void mensajeOK(String ls_mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(ls_mensaje)
                .setCancelable(false)
                .setNegativeButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();


    }

    public void actualizaResumen() {
        long ll_total;
        long ll_tomadas;
        long ll_fotos;
        long ll_restantes;
        long ll_conAnom;
        long ll_noRegistrados;
        String ls_archivo;
        String ls_resumen;

        try {

            tv_resumen = (TextView) findViewById(R.id.tv_resumen);

            Cursor c;
            openDatabase();
            c = db.rawQuery("Select count(*) canti from Ruta", null);
            c.moveToFirst();
            ll_total = Utils.getLong(c, "canti", 0);
            if (ll_total > 0) {
                try {
                    c = db.rawQuery("Select value from config where key='cpl'", null);
                    c.moveToFirst();
                    ls_archivo = Utils.getString(c, "value", "");
                } catch (Throwable e) {
                    ls_archivo = "";
                }

                c = db.rawQuery("Select count(*) canti from ruta where lectura<>''", null);
                c.moveToFirst();

                ll_tomadas = Utils.getLong(c, "canti", 0);
                c = db.rawQuery("Select count(*) canti from fotos", null);
                c.moveToFirst();
                ll_fotos = Utils.getLong(c, "canti", 0);
                c.close();

                c = db.rawQuery("Select count(*) canti from ruta where anomalia<>''", null);
                c.moveToFirst();
                ll_conAnom = Utils.getLong(c, "canti", 0);
                c.close();

                c = db.rawQuery("Select count(*) canti from ruta where anomalia='' and lectura=''", null);
                c.moveToFirst();
                ll_restantes = Utils.getLong(c, "canti", 0);
                c.close();

                c = db.rawQuery("Select count(*) canti from NoRegistrados", null);
                c.moveToFirst();
                ll_noRegistrados = Utils.getLong(c, "canti", 0);
                c.close();

                //ll_restantes = ll_total-ll_tomadas ;

                ls_resumen = getString(R.string.msj_main_total_lecturas) + " " + ll_total + "\n" +
                        getString(R.string.msj_main_medidores_con_lectura) + " " + +ll_tomadas + "\n" +
                        getString(R.string.msj_main_medidores_con_anomalias) + " " + ll_conAnom + "\n" +
                        getString(R.string.msj_main_lecturas_restantes) + " " + ll_restantes + "\n\n" +
                        getString(R.string.msj_main_fotos_tomadas) + " " + ll_fotos + "\n\n" +
                        getString(R.string.msj_main_no_registrados) + "No Registrados " + ll_noRegistrados;

                tv_resumen.setText(ls_resumen);
            } else {
                tv_resumen.setText(R.string.msj_main_no_hay_itinerarios);
            }

            closeDatabase();
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Ocurrió un problema inesperado", t);
        }
    }

    private void openDatabase() {
        dbHelper = new DBHelper(this);

        db = dbHelper.getReadableDatabase();
    }

    private void closeDatabase() {
        db.close();
        dbHelper.close();

    }

    public void prueba() {
        ContentValues cv_datos = new ContentValues(3);

        cv_datos.put("nombre", "secuencial" + "_" + Main.obtieneFecha() + ".jpg");
        cv_datos.put("foto", "hola mundo");
        cv_datos.put("envio", TomaDeLecturas.NO_ENVIADA);

        openDatabase();
        db.insert("fotos", null, cv_datos);
        closeDatabase();
    }


    private void agregaRegistrosConfig() {
        //Agregamos los registros, asi nadamas actualizamos.
        openDatabase();
        Cursor c;
        c = db.query("config", null, "key='modo'", null, null, null, null);

        if (c.getCount() == 0)
            db.execSQL("insert into config (key, value)values ('modo', '')");


        c.close();


        c = db.query("config", null, "key='ciudad'", null, null, null, null);

        if (c.getCount() == 0)
            db.execSQL("insert into config (key, value)values ('ciudad', '')");


        c.close();

        c = db.query("config", null, "key='medidor'", null, null, null, null);

        if (c.getCount() == 0)
            db.execSQL("insert into config (key, value)values ('medidor', '')");


        c.close();

        c = db.query("config", null, "key='cliente'", null, null, null, null);

        if (c.getCount() == 0)
            db.execSQL("insert into config (key, value)values ('cliente', '')");


        c.close();

        c = db.query("config", null, "key='direccion'", null, null, null, null);

        if (c.getCount() == 0)
            db.execSQL("insert into config (key, value)values ('direccion', '')");


        c.close();

        c = db.query("config", null, "key='brincarc'", null, null, null, null);

        if (c.getCount() == 0)
            db.execSQL("insert into config (key, value)values ('brincarc', 0)");


        c.close();


        closeDatabase();
    }

//	 @Override
//	 public boolean dispatchKeyEvent(KeyEvent event) {
//	     int action = event.getAction();
//	     int keyCode = event.getKeyCode();
//	         switch (keyCode) {
//	         case KeyEvent.KEYCODE_VOLUME_UP:
//	             if (action == KeyEvent.ACTION_UP) {
//	                 //TODO
//	            	 versionFontSize++;
//	            	 infoFontSize++;
//	            	 tv_versionNum.setTextSize(versionFontSize);
//	            	 tv_resumen.setTextSize(infoFontSize);
//	            	 
//	            	 openDatabase();
//	            	 guardaValor("versionFontSize",  versionFontSize);
//	            	 guardaValor("infoFontSize",  infoFontSize);
//		             //db.execSQL("Update config  set value=" +versionFontSize+" where  key='versionFontSize'");
//		             //db.execSQL("Update config set  value=" +infoFontSize+" where  key='infoFontSize'");
//		             
//		             closeDatabase();
//	             }
//	             
//	             return true;
//	         case KeyEvent.KEYCODE_VOLUME_DOWN:
//	             if (action == KeyEvent.ACTION_DOWN) {
//	                 //TODO
//	            	 if (versionFontSize>=1)
//	            		 versionFontSize--;
//	            	 if (infoFontSize>=1)
//	            		 infoFontSize--;
//	            	 tv_versionNum.setTextSize(versionFontSize);
//	            	 tv_resumen.setTextSize(infoFontSize);
//	            	 
//	            	 openDatabase();
//	            	 guardaValor("versionFontSize",  versionFontSize);
//	            	 guardaValor("infoFontSize",  infoFontSize);
//		             
//		             closeDatabase();
//	             }
//	             return true;
//	         default:
//	             return super.dispatchKeyEvent(event);
//	         }
//	     }


    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {
                    if (ii_lastSelectedTab == 0) {
                        porcentaje += factorPorcentaje;
                    } else {
                        porcentaje2 += factorPorcentaje * 1.5;
                    }


                    //porcentaje= getFloatValue("porcentaje", porcentaje);

                    setSizes();
                    openDatabase();
                    guardaValor("porcentaje_main", porcentaje);
                    guardaValor("porcentaje2_main", porcentaje2);


                    closeDatabase();
                }

                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                    if (ii_lastSelectedTab == 0) {
                        if (porcentaje >= .05f) {
                            porcentaje -= factorPorcentaje;
                            //porcentaje= getFloatValue("porcentaje", porcentaje);
                        }

                    } else {
                        if (porcentaje2 >= .05f) {
                            porcentaje2 -= factorPorcentaje;
                            //porcentaje= getFloatValue("porcentaje", porcentaje);
                        }
                    }


                    setSizes();

                    openDatabase();
                    guardaValor("porcentaje_main", porcentaje);
                    closeDatabase();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    public void setSizes() {
        tv_versionNum.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (porcentaje * versionFontSize));
        //tv_resumen.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)(porcentaje * infoFontSize));
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + 0);
        // based on the current position you can then cast the page to the correct
        // class and call the method:
//	     if (/*viewPager.getCurrentItem() == 0 &&*/ page != null) {
//	    	 if (globales.tdlg==null)
//					return;
//			 
//	          ((Principal)page).actualizaResumen();     
//	     } 
//	     
//	     page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + 1);
        // based on the current position you can then cast the page to the correct
        // class and call the method:
        if (/*viewPager.getCurrentItem() == 0 &&*/ page != null) {
            if (globales.tdlg == null)
                return;

            ((Resumen) page).actualizaResumen();
        }


    }
	 
	/* public void probarBluetooth(){
		 BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		 BluetoothDevice mDevice;
		 BluetoothSocket socket = null;
		 InputStream is;
		 DataOutputStream dos;

		 if(mBluetoothAdapter==null){
		       //No soporta bluetooth
		        return;
		    }
		 
		 
		 //Nos conectamos al dispositivo por medio de mac
		 mDevice=mBluetoothAdapter.getRemoteDevice("00:0A:94:11:A5:13");
		 
		 
		 try {
			 
			 UUID uuid = null;
			 //uuid = mDevice.getUuids()[0].getUuid();
			 //socket = mDevice.createRfcommSocketToServiceRecord(uuid);
			 
			 
			//socket = mDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			// socket = mDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66"));
			 
			 Method m = mDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
			 //socket = (BluetoothSocket) m.invoke(mDevice, 1);
			 
			// for (int i=1; i<=30;i++){
				 try{
					 socket = (BluetoothSocket) m.invoke(mDevice, 1);
			         
					mBluetoothAdapter.cancelDiscovery();
						 
					socket.connect();
				 }
				 catch(Throwable e){
					// socket.close();
					 
			 }
		//	 }
			
			 
			 dos=new DataOutputStream(socket.getOutputStream());
			 dos.writeInt(0);
			 dos.flush();
			 dos.close();
			 socket.close();
			 
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
	 }*/

    //public void EnviarMedidoresDeCPLaPC(){
    public void probarBluetooth() {
        byte[] medidor;
        byte[] medidorPanama;
        String strMedidorPanama;
        String strMedidorPanama1;
        String strMedidorPanama2;
        String strMedidorPanama3;
        String strMedidorPanama4;
        String strMedidorPanama4a;
        String strMedidorPanama4b;
        String strMedidorPanama5;
        String strMedidorPanama6;
        String strMedidorPanama6a;
        String strMedidorPanama6b;
        String strMedidorPanama7;
        String strMedidorPanama8;
        String strMedidorPanama9;
        String strMedidorPanama10;
        String strMedidorPanama11;
        String strMedidorPanama12;
        String strMedidorPanama13;
        String strMedidorPanama14;
        String strMedidorPanama15;
        String strMedidorPanama16;
        String strMedidorPanama17;
        String strMedidorPanama18;
        String strMedidorPanama19;
        String strMedidorCR;
        String strMedidorTab;
        int totalMedidoresMandados = 0;
        int totalBytesMandados = 0;
        int totalDeMedidores = 0;

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice mDevice;
        BluetoothSocket socket = null;
        InputStream is;
        DataOutputStream dos;

        try {
            //HCG 08/06/2012, Se realizaron cambios para que funcione con varios RecordStores
            openDatabase();
            Cursor c = db.rawQuery("Select count(*) canti from ruta", null);
            c.moveToFirst();
            totalDeMedidores = Utils.getInt(c, "canti", 0);
            c.close();
            byte[] bytesAEnviar;

            mDevice = mBluetoothAdapter.getRemoteDevice("00:0A:94:11:A5:13");
            Method m = mDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
            socket = (BluetoothSocket) m.invoke(mDevice, Integer.valueOf(1));

            mBluetoothAdapter.cancelDiscovery();
            socket.connect();

            dos = new DataOutputStream(socket.getOutputStream());


            dos.writeInt(2);
            dos.flush();

            //Primero el encabezado
            c = db.rawQuery("Select registro from encabezado", null);

            c.moveToFirst();
            bytesAEnviar = c.getBlob(c.getColumnIndex("registro"));
            dos.writeInt(totalDeMedidores);
            dos.writeInt(bytesAEnviar.length);
            dos.flush();
            dos.write(bytesAEnviar);
            dos.flush();


            c = db.rawQuery("Select registro from ruta", null);

            c.moveToFirst();

            for (int i = 0; i < c.getCount(); i++) {
                bytesAEnviar = (new String(c.getBlob(c.getColumnIndex("registro"))) + "\r\n").getBytes("ISO-8859-1");

                dos.writeInt(bytesAEnviar.length);
                dos.flush();
                dos.write(bytesAEnviar);
                dos.flush();
                c.moveToNext();
            }

            dos.close();
            socket.close();

            c.close();
            closeDatabase();

        } catch (Throwable e1) {
            e1.printStackTrace();
        }

    }

    public int getIntValue(String key, int value) throws Exception {
        openDatabase();

        Cursor c = db.rawQuery("Select * from config where key='" + key + "'", null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            value = Utils.getInt(c, "value", 0);
        } else {
            db.execSQL("Insert into config (key, value) values ('" + key + "', " + value + ")");
        }
        c.close();


        closeDatabase();

        return value;
    }

    public void guardaValor(String key, Double porcentaje3) {
        openDatabase();
        db.execSQL("Update config  set value=" + porcentaje3 + " where  key='" + key + "'");

        closeDatabase();
    }

    public float getFloatValue(String key, float value) {
        openDatabase();

        Cursor c = db.rawQuery("Select * from config where key='" + key + "'", null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            value = c.getFloat(c.getColumnIndex("value"));
        } else {
            db.execSQL("Insert into config (key, value) values ('" + key + "', " + value + ")");
        }
        c.close();


        closeDatabase();

        return value;
    }

    public void guardaValor(String key, float value) {
        openDatabase();
        db.execSQL("Update config  set value=" + value + " where  key='" + key + "'");

        closeDatabase();
    }

//	 public void setTabs(){
//		 Resources res = getResources();
//		 
//		 TabHost tabs=(TabHost)findViewById(android.R.id.tabhost);
//		 tabs.setup();
//		  
//		 TabHost.TabSpec spec=tabs.newTabSpec("mitab1");
//		 spec.setContent(R.id.tab1);
//		 spec.setIndicator("Principal");
//		     //res.getDrawable(android.R.drawable.ic_btn_speak_now));
//		 tabs.addTab(spec);
//		  
//		 spec=tabs.newTabSpec("mitab2");
//		 spec.setContent(R.id.tab2);
//		 spec.setIndicator("Resumen");
//		    // res.getDrawable(android.R.drawable.ic_dialog_map));
//		 tabs.addTab(spec);
//		 
//		  
//		 tabs.setCurrentTab(0);
//	 }

    @Override
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction arg1) {
        // TODO Auto-generated method stub
        viewPager.setCurrentItem(tab.getPosition());
        ii_lastSelectedTab = tab.getPosition();
    }

    @Override
    public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub

    }

    public void inicia_tdl(View view) {
        lrs = new Intent(this, TomaDeLecturas.class);
        lrs.putExtra("esSuperUsuario", esSuperUsuario);
        lrs.putExtra("nombre", this.is_nombre_Lect);
        lrs.putExtra("bHabilitarImpresion", this.bHabilitarImpresion);

        startActivityForResult(lrs, LECTURAS);
    }

    public boolean bluetoothDisponible(int tipo) {
        boolean ejecutar = true;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                lrs = new Intent(this, trasmisionDatosBt.class);
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, tipo);
                ejecutar = false;
            } else {
                lrs = new Intent(this, trasmisionDatosBt.class);
            }
        } else {
            mensajeOK("Bluetooth no disponible.");
            return ejecutar;
        }
        return ejecutar;
    }

    public int tipoDeTransmisionPredeterminada() throws Exception {
        String ls_modo_trans;
        int metodo = 0;
        openDatabase();
        Cursor c = db.rawQuery("Select value from config where key='modo_trans'", null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            metodo = Utils.getInt(c, "value", 0);
        }

        c.close();
        closeDatabase();

        return metodo;
    }

    public double getDoubleValue(String key, double value) throws Exception {
        openDatabase();

        Cursor c = db.rawQuery("Select * from config where key='" + key + "'",
                null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            value = Utils.getDouble(c, "value", 0);
        } else {
            db.execSQL("Insert into config (key, value) values ('" + key
                    + "', " + value + ")");
        }
        c.close();

        closeDatabase();

        return value;
    }

    private void GrabarSDCard() {


        mensajeEspere();
        Thread thread = new Thread() {

            public void run() {

                byte[] foto = null;
                String medidor = null;
                byte[] nombreFotoByte = null;
                int totalDeFotos = 0;
                int totalDeMedidores = 0;
                int numeroFoto = 1;
                //int		LONG_CAMPO_NOMBRE_FOTOS = 36;
                String nombreFoto = "";
                String nombreLote = "";
                Cursor c = null;
                Serializacion serial = new Serializacion(Serializacion.WIFI);
                File filecon = null;
                OutputStream fo = null;
                openDatabase();
                try {


                    //String csEstatusDelProceso = "Se exportara las lecturas,\nconteste SI a todas las preguntas.\n";
                    //siMensaje.setText(csEstatusDelProceso);


                    c = db.rawQuery("Select count(*) canti from ruta", null);
                    c.moveToFirst();
                    totalDeMedidores = Utils.getInt(c, "canti", 0);
                    if (totalDeMedidores == 0) {
                        mHandler.post(new Runnable() {
                            public void run() {
                                alert.dismiss();
                                mensajeOK("No hay medidores a exportar");
                            }
                        });

                        c.close();
                        closeDatabase();
                        return;
                    }

                    c.close();


                    //Del encabezado
                    c = db.rawQuery("select registro from encabezado", null);
                    c.moveToFirst();
                    medidor = new String(c.getBlob(c.getColumnIndex("registro")));
                    c.close();

                    nombreFoto = medidor.substring(11, 21).trim();
                    nombreLote = medidor.substring(23, 30).trim();


//					borrarArchivo("apps/lecturasentrada/" +nombreLote+"/"+nombreFoto);
                    //
//					serial.open("http://www.espinosacarlos.com", "apps/lecturasentrada/"+nombreLote, nombreFoto,
//							Serializacion.ESCRITURA, 0, 0);

                    filecon = new File(Environment.getExternalStorageDirectory().toString() + "/SISTOLE/");
                    if (!filecon.exists()) {
                        filecon.mkdir();
                    }

                    filecon = new File(Environment.getExternalStorageDirectory().toString() + "/SISTOLE/" + nombreLote);

                    if (!filecon.exists()) {
                        filecon.mkdir();
                    }

                    filecon = new File(Environment.getExternalStorageDirectory().toString() + "/SISTOLE/" + nombreLote + "/" + nombreLote + "-" + nombreFoto);
                    if (filecon.exists()) {
                        filecon.delete();
                        //filecon =  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + "nombreLote/"+nombreFoto);
                    }

                    if (!filecon.exists()) {
                        filecon.createNewFile();
                    }

                    //csEstatusDelProceso += "Exportando lecturas\n.";

                    if (!filecon.exists()) {
                        throw new Throwable("El archivo no pudo ser creado, verifique que cuente con espacio y pueda escribir en la SD.");
                    }
                    fo = new FileOutputStream(filecon);
                    c = db.rawQuery("select " + globales.tlc.is_camposDeSalida + " as TextoSalida from Ruta ", null);
                    //c.moveToFirst();
                    for (int numeroMedidor = 0; numeroMedidor < totalDeMedidores; numeroMedidor++) {
                        //serial.write(c.getString(c.getColumnIndex("TextoSalida")));
                        c.moveToNext();
                        fo.write((c.getString(c.getColumnIndex("TextoSalida")) + "\r\n").getBytes("ISO-8859-1"));

                    }
                    //serial.close();

                    c.close();
                    fo.close();
//						bRutaDescargada	= true;
//						establecerRutaComoDescargada();

                    //csEstatusDelProceso += "\nLas lecturas ha sido grabadas.\nExportando fotografias\n.";
//						siMensaje.setText(csEstatusDelProceso);

                    c = db.rawQuery("Select count(*) canti from fotos", null);
                    c.moveToFirst();
                    totalDeFotos = Utils.getInt(c, "canti", 0);
                    if (totalDeFotos == 0) {
                        c.close();
                        closeDatabase();
                        mHandler.post(new Runnable() {
                            public void run() {
                                alert.dismiss();
                                mensajeOK("Ha terminado el proceso de exportacion a la SDCard.");
                            }
                        });


                        return;
                    }


                    String strTamano = "";
                    byte[] tamano = null;
                    //OutputStream out = null;
                    //FileConnection filecon = (FileConnection) Connector.open("file:///E:/"+nombreLote+"/" + nombreFoto.substring(0,6) + ".DAT");
                    // Always check whether the file or directory exists.
                    // Create the file if it doesn't exist.
                    //if(!filecon.exists()) {
                    filecon = new File(Environment.getExternalStorageDirectory().toString() + "/SISTOLE/" + nombreLote + "/" + nombreFoto.substring(0, 6) + ".DAT");

                    if (!filecon.exists()) {
                        filecon.createNewFile();
                    }

                    if (!filecon.exists()) {
                        throw new Throwable("El archivo no pudo ser creado, verifique que cuente con espacio y pueda escribir en la SD.");
                    }

                    fo = new FileOutputStream(filecon);
//					serial.open("http://www.espinosacarlos.com", "apps/lecturasentrada/"+nombreFoto.substring(0,6)+".DAT", nombreFoto,
//							Serializacion.ESCRITURA, 0, 0);

//			 	        	filecon.create();
//					        out = filecon.openOutputStream();
//						int nAvance = 1;
//						if (totalDeFotos > 65) nAvance = (int) (totalDeFotos / 65);
                    c = db.rawQuery("select nombre, foto from fotos ", null);
                    for (numeroFoto = 0; numeroFoto < totalDeFotos; numeroFoto++) {
                        c.moveToPosition(numeroFoto);
                        //Segun yo esto debe de ir en el nombre de la foto, ya que el nokia lo agrega desde ahi... pero bueno...
                        String ls_nombre = "0" + c.getString(c.getColumnIndex("nombre")).toUpperCase();
                        foto = c.getBlob(c.getColumnIndex("foto"));
                        byte[] fotoByte = new byte[ls_nombre.length() + foto.length];

                        for (int i = 0; i < ls_nombre.length(); i++)
                            fotoByte[i] = ls_nombre.getBytes()[i];
                        for (int i = 0; i < foto.length; i++)
                            fotoByte[i + ls_nombre.length()] = foto[i];
//							try{
//								foto = rsFotos.getRecord(numeroFoto);
//							}catch(Throwable e2){
//								log.log("Error al mandar foto: " + e2);	
//						   	}
                        //try{
                        strTamano = "0000000" + fotoByte.length;
                        strTamano = strTamano.substring(strTamano.length() - 6);
                        tamano = strTamano.getBytes();
                        //serial.write(fotoByte);
                        fo.write(tamano);
                        fo.write(fotoByte);
//								out.write(tamano,0,tamano.length);
//								out.write(foto,0,foto.length);
//		        					out.flush();
//								if ((numeroFoto % nAvance)==0) {
//									csEstatusDelProceso += ".";
//									siMensaje.setText(csEstatusDelProceso);
//								}
//						   	}catch(IOException ioe) {
//								log.log("Error al escribir a memoria: " + ioe);	
//						   	}

                        c.moveToNext();
                    }
                    //}

                    fo.close();


                    //Marcamos como descargada
                    db.execSQL("update encabezado set descargada=1");


                    mHandler.post(new Runnable() {
                        public void run() {
                            //alert.dismiss();
                            mensajeOK("Ha terminado el proceso de exportacion a la SDCard.");
                        }
                    });


//				csEstatusDelProceso += "\nHa terminado el proceso de exportacion a la SDCard.";
//				siMensaje.setText(csEstatusDelProceso);
                } catch (final Throwable e) {
                    e.printStackTrace();
                    if (c != null) {
                        if (!c.isClosed()) {
                            c.close();

                        }
                    }

                    if (fo != null) {
                        try {
                            fo.close();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }


                    mHandler.post(new Runnable() {
                        public void run() {
                            //alert.dismiss();
                            mensajeOK("Ha ocurrido un error:" + e.getMessage());
                        }
                    });
                }
                closeDatabase();
                mHandler.post(new Runnable() {
                    public void run() {
                        alert.dismiss();
                    }
                });

            }
        };

        thread.start();

    }

//	private void borrarArchivo(String ls_ruta) throws Throwable {
//		// HCG 20/07/2012 Manda los datos del wifi antes de cerrar la conexion
//		String ruta, cadenaAEnviar;
//
//		Hashtable params = new Hashtable();
//		// params.put("cadena",cadenaAEnviar);
//		params.put("ruta", ls_ruta);
//
//		try {
//			HttpMultipartRequest http = new HttpMultipartRequest("http://www.espinosacarlos.com"
//					+ "/deleteFile.php", params, "upload_field", "",
//					"text/plain", new String("").getBytes());
//			byte[] response = http.send();
//			// new String (response); Esta es la respuesta del servidor
//
//			if (!new String(response).trim().equals("0")) {
//				throw new Throwable(new String(response));
//			}
//
//			// Enviamos las fotos que tenemos pendientes
//			// enviaFotosWifi();
//
//		} catch (Throwable e) {
//			throw e;
//		}
//
//	}

    public void mensajeEspere() {


        final LayoutInflater inflater = this.getLayoutInflater();

        final View view = inflater.inflate(R.layout.wait_messagebox, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);


        builder.setView(view);


        builder
                .setCancelable(false);

        alert = builder.create();

        alert.show();
    }

/*
	Método para descargar las ordenes de Cortrex Web.
 */

    public void cargarOrdenes() {
        if (ii_rol == CPL.LECTURISTA) {
            Intent lrs = new Intent(this, trasmisionDatos.class);
            lrs.putExtra("tipo", trasmisionDatos.RECEPCION);
            startActivityForResult(lrs, RECEPCION);
        }
    }


    public void sobreEscribirCampos(String dbField, String texto) {
        String tableToUpdate = "config";
        openDatabase();

        //Solo vamos a actualizar pero primero deberemos ver si debemos actualizar
        Cursor c = db.rawQuery("Select * from " + tableToUpdate + " where key='" + dbField + "'", null);

        if (c.getCount() > 0) {
            db.execSQL("update " + tableToUpdate + " set key='" + dbField + "', value='" + texto + "' where key='" + dbField + "'");
        } else {
            db.execSQL("insert into " + tableToUpdate + " ( key, value) values('" + dbField + "', '" + texto + "')");
        }
        c.close();
        closeDatabase();
    }


    @Override
    protected void onResume() {
        //Ahora si abrimos
        if (globales.tdlg == null) {
            super.onResume();

            mostrarMensaje("Error", "No se pudieron inicializar las variables", "", new DialogoMensaje.Resultado() {
                @Override
                public void Aceptar(boolean EsOk) {
//                    Intent i = getBaseContext().getPackageManager()
//                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
//                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    cancelar();
                }
            });
        }
        super.onResume();
    }

    private void cancelar() {
        setResult(Activity.RESULT_CANCELED);
        this.finish();
    }

    private void estableceVariablesDeClave() {
        // TODO Auto-generated method stub

        switch (globales.ii_claveIngresada) {
            case Globales.CLAVE_COMAPA_ZC:
                sobreEscribirCampos("server_gprs", globales.defaultServidorGPRS);
                sobreEscribirCampos("ruta_descarga", globales.defaultRutaDescarga);
                reiniciarAplicacion();
                break;
            case Globales.CLAVE_ENRUTA:
                sobreEscribirCampos("server_gprs", globales.defaultServidorGPRS);
                sobreEscribirCampos("ruta_descarga", "C:\\CortrexPruebas");
                reiniciarAplicacion();
                break;
            case Globales.CLAVE_PRUEBAS2:
                sobreEscribirCampos("server_gprs", globales.defaultServidorGPRS);
                sobreEscribirCampos("ruta_descarga", "C:\\CortrexPruebas2");
                reiniciarAplicacion();
                break;
            case Globales.CLAVE_PRUEBAS3:
                sobreEscribirCampos("server_gprs", globales.defaultServidorGPRS);
                sobreEscribirCampos("ruta_descarga", "C:\\CortrexPruebas3");
            case Globales.CLAVE_MEXICANA:
            case Globales.CLAVE_PREPAGO:
                sobreEscribirCampos("server_gprs", globales.defaultServidorGPRS);
                sobreEscribirCampos("ruta_descarga", "C:\\");

                reiniciarAplicacion();


                break;
        }


    }

    private void reiniciarAplicacion() {
        // TODO Auto-generated method stub
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("SOGES");
        builder.setMessage("La aplicacion se reiniciara.")
                .setCancelable(false)
                .setNegativeButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        System.exit(0);
                        return;
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void asignarSecuenciasReales() {
        // TODO Auto-generated method stub

        //Realizamos un query con los datos que vamos a manejar
        openDatabase();
        Cursor c = db.rawQuery("Select numOrden, sinUso8 from ruta", null);

        //El numero de orden es imposible repetirlo, asi que nos basaremos en el para realizar

        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);

            if (c.getString(c.getColumnIndex("numOrden")).equals("0")) {
                db.execSQL("Update ruta set secuenciaReal=" + (i + 1) + " where sinUso8 ='" + c.getString(c.getColumnIndex("sinUso8")) + "'");
            } else {
                String ls_numOrden = c.getString(c.getColumnIndex("numOrden"));

                //Actualizamos la BD con el consecutivo
                db.execSQL("Update ruta set secuenciaReal=" + (i + 1) + " where numOrden ='" + ls_numOrden + "'");
            }


        }

        c.close();
        closeDatabase();
    }

    @SuppressLint("NewApi")
    public void startListeningGPS() {
        if (!(/*globales.requiereGPS && */globales.GPS))
            return;


//	stopListeningGPS();
        mHandler = new Handler();


        locationManager2 = (LocationManager) this.getSystemService(LOCATION_SERVICE);


        // Define a listener that responds to location updates


        locationListener2 = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.

                //Cancelamos y volvemos a activar el supervisor
                detenerSupervisor();
                activaSupervisor();

                try {
                    // Guardamos la localizacion en la BD
                    DBHelper dbHelper = new DBHelper(Main.this);
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    int id = 1;

//				Cursor c=db.rawQuery("Select max(id) id from rutaGPS", null); 
//				
//				c.moveToFirst();
//				if (c.getCount()>0){
//					id= Utils.getInt(c, "id"));
//					id++;
//				}
//				
//				c.close();
                    String tipo = "TP000";

                    if (primerPunto) {
                        primerPunto = false;
                        tipo = "TP003";
                    }

                    ContentValues cv_params = new ContentValues();

                    cv_params.put("latitud", location.getLatitude());
                    cv_params.put("longitud", location.getLongitude());
                    cv_params.put("fecha", obtieneFecha("ymdhis"));
                    cv_params.put("PTN", globales.getUsuario());
                    cv_params.put("tipo", tipo);
                    //cv_params.put("id", id);

                    db.insert("rutaGPS", null, cv_params);


                    db.close();
                    dbHelper.close();

                    agregaAlLog("Puntos Obtenido");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProviderEnabled(final String provider) {
                // TODO Auto-generated method stub
//				mHandler.post(new Thread() {
//					public void run() {
//						Toast.makeText(Main.this,
//								"Activado:" + provider,
//								Toast.LENGTH_LONG).show();
//						
//					}
//				});

                agregaAlLog(provider + " Activado");
            }

            @Override
            public void onStatusChanged(final String provider, final int status,
                                        Bundle extras) {
                // TODO Auto-generated method stub

//				mHandler.post(new Thread() {
//					public void run() {
//						Toast.makeText(Main.this,
//								"Cambio de Status:" + provider+" - "+ status,
//								Toast.LENGTH_LONG).show();
//						
//					}
//				});

                agregaAlLog(provider + " ha cambiado de status a " + (status == 0 ? "fuera de servicio" : status == 1 ? "temporalmente no disponible" : "disponible"));

            }

        };

        // Register the listener with the Location Manager to receive location updates
        //Establecemos que cada 10 segundos y 2 metros de diferencia quiero las actualizaciones

        locationManager2.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000, 2, locationListener2);
        locationManager2.requestLocationUpdates(LocationManager.GPS_PROVIDER/*provider*/, 300000, 2, locationListener2);
        gpsEncendido = true;
//	if (Build.VERSION.SDK_INT >= 11)
//		invalidateOptionsMenu();
    }

    @SuppressLint("NewApi")
    public void stopListeningGPS() {

        detenerSupervisor();
        if (!(globales.GPS))
            return;
        gpsEncendido = false;
        if (locationManager2 != null)
            locationManager2.removeUpdates(locationListener2);

        guardaPuntoDeError("TP002");

        //globales.location=null;

//	if (Build.VERSION.SDK_INT >= 11)
//		invalidateOptionsMenu();
    }


    /**
     * Determina si un Punto GPS (LOCATION) es mejor que el que que se se tiene como mejor
     *
     * @param location            Nuevo punto GPS a evaluar
     * @param currentBestLocation El punto GPS actual que se tiene como mejor
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIME_LIMIT;
        boolean isSignificantlyOlder = timeDelta < -TIME_LIMIT;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    void enciendeGPS() {
        if (!gpsEncendido) {

            startListeningGPS();
        }
//	else if( globales.gpsEncendido){
//		//globales.requiereGPS=false;
//		stopListeningGPS();
//	}
    }

    @Override
    protected void onDestroy() {
        stopListeningGPS();
        super.onDestroy();
    }

    protected void activaEnvioPuntosRuta() {
//	cambiarFuenteTimer.purge();


        Timer cambiarFuenteTimer = new Timer();
        cambiarFuenteTimer.schedule(new TimerTask() {

            public void run() {
                rutinaDeEnvioDePuntos();
                activaEnvioPuntosRuta();
            }

//	}, 300000);
        }, 60000);
    }

    protected void activaSupervisor() {
//	cambiarFuenteTimer.purge();


//	try{
//		globales.supervisor.cancel();
//		globales.supervisor.purge();
//		
//	}catch(Throwable e){
//		
//	}
        agregaAlLog("Supervisor Activado");
        globales.supervisor = new Timer();
        globales.supervisor.schedule(new TimerTask() {

            public void run() {
                guardaPuntoDeError("TP001");
                agregaAlLog("Punto de Supervisor");
                activaSupervisor();
            }

        }, 300000);
    }

    public void guardaPuntoDeError(String tipo) {
        DBHelper dbHelper = new DBHelper(Main.this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int id = 1;


        Location l = null;
        List<String> providers = locationManager2.getProviders(true);
        for (int i = providers.size() - 1; i >= 0; i--) {
            l = locationManager2.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }


        if (l == null) {
            db.close();
            dbHelper.close();
            return;
        }

        ContentValues cv_params = new ContentValues();

        cv_params.put("latitud", l.getLatitude());
        cv_params.put("longitud", l.getLongitude());
        cv_params.put("fecha", obtieneFecha("ymdhis"));
        cv_params.put("PTN", globales.getUsuario());
        cv_params.put("tipo", tipo);
        //cv_params.put("id", id);

        db.insert("rutaGPS", null, cv_params);


        db.close();
        dbHelper.close();
    }

    public void rutinaDeEnvioDePuntos() {


        if (mPuntosGpsMgr == null) {
            mPuntosGpsMgr = new PuntosGpsMgr(this, this.globales);
        }

        Thread mThreadPuntosGps = new Thread() {
            public void run() {
                Cursor c = null;
                long id = 0;
                long idEmpleado;
                String latitud = "";
                String longitud = "";
                String PTN = "";
                String tipo = "";
                Date fecha;
                Boolean exito = false;

                try {
                    DBHelper dbHelper = new DBHelper(Main.this);
                    SQLiteDatabase db = dbHelper.getReadableDatabase();

//			Serializacion serial = new Serializacion(Serializacion.WIFI);

//				serial.open(globales.defaultServidorGPRS, "", "hola",
//						Serializacion.ESCRITURA, 0, 0);

                    c = db.rawQuery("Select * from rutaGPS", null);

                    c.moveToFirst();

                    for (int i = 0; i < c.getCount(); i++) {
                        try {
                            id = Utils.getLong(c, "id", 0);
                            idEmpleado = globales.getIdEmpleado();
                            latitud = Utils.getString(c, "latitud", "");
                            longitud = Utils.getString(c, "longitud", "");
                            PTN = Utils.getString(c, "PTN", globales.getUsuario());

                            if (PTN.equals(""))
                                PTN = String.valueOf(idEmpleado);

                            tipo = Utils.getString(c, "tipo", "");
                            fecha = Utils.convToDate(Utils.getString(c, "fecha", ""), "yyyyMMddhhmmss");

                            exito = mPuntosGpsMgr.registrarPunto(idEmpleado, PTN, fecha, latitud, longitud, tipo);

                            if (exito) {
                                int columnas = db.delete("rutaGPS", "id=" + String.valueOf(id), null);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

//						serial.mandarSQL("Insert into rutaGPS(latitud, longitud, PTN, fecha, tipo) values ('"
//								+ c.getString(c.getColumnIndex("latitud"))
//								+ "', '"
//								+ c.getString(c.getColumnIndex("longitud"))
//								+ "', '"
//								+ c.getString(c.getColumnIndex("PTN"))
//								+ "',str_to_date('"
//								+ c.getString(c.getColumnIndex("fecha"))
//								+ "', '%Y%m%d%H%i%s')," +
//								" '"+c.getString(c.getColumnIndex("tipo"))+"')", globales.serverBD, globales.usuarioBD, globales.passwordBD);

                        // Se envió, podemos eliminar el punto...

                        c.moveToNext();
                    }
                    c.close();

//				serial.close();
                    agregaAlLog("Puntos enviados");
                } catch (final Throwable e) {
                    // TODO Auto-generated catch block
                    mHandler.post(new Thread() {
                        public void run() {
                            agregaAlLog("Error:" + e.getMessage());
//							Toast.makeText(Main.this,
//									"Error:" + e.getMessage(),
//									Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    });

                    try {
                        db.close();
                        dbHelper.close();
                    } catch (Exception e2) {

                    }

                    if (c != null) {
                        try {
                            c.close();
                        } catch (Throwable e3) {

                        }
                    }
                }
            }
        };

        mThreadPuntosGps.start();
    }

    private void finalizarEnvioPuntosGps() {
        if (mThreadPuntosGps != null) {
            mThreadPuntosGps.interrupt();
            mThreadPuntosGps = null;
        }
    }

    public void detenerSupervisor() {
        try {
            globales.supervisor.cancel();
            globales.supervisor.purge();
            agregaAlLog("Supervisor Desactivado");

        } catch (Throwable e) {

        }
    }

    public void agregaAlLog(final String fecha, final String informacion) {
        if (!globales.debug) {
            return;
        }
        mHandler.post(new Thread() {
            public void run() {
                log.add(informacion + "\t" + fecha);
                actualizaTabs();
            }
        });

    }

    public void agregaAlLog(String informacion) {
        if (!globales.debug) {
            return;
        }
        agregaAlLog(obtieneFecha("h:i:s"), informacion);
    }

    public String getStringValue(String key) {
        String value = "";
        openDatabase();

        Cursor c = db.rawQuery("Select * from config where key='" + key + "'",
                null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            value = c.getString(c.getColumnIndex("value"));
        }
        c.close();

        closeDatabase();

        return value;
    }

    private void hacerImportar() {
        //Utils.showMessageLong(this, "Importar...");

        Intent intent = new Intent(Main.this, DescargarTareasActivity.class);
        startActivityForResult(intent, IMPORTAR);
    }

    private void inicializarControlesCheck() {
        b_lecturas = (Button) this.findViewById(R.id.b_lecturas);
        btnOperacion = (Button) this.findViewById(R.id.btnOperacion);

        btnOperacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (globales == null) return;
                if (globales.sesionEntity == null) return;
                if (globales.sesionEntity.empleado == null) return;

                if (globales.sesionEntity.empleado.RequiereCheckSeguridad)
                    hacerCheckSeguridad();
            }
        });

        CambiarBotonesOperEstatus();
    }

    private void CambiarBotonesOperEstatus() {
        boolean habilitarLecturas = false;
        boolean sesionActiva = false;
        ResumenEntity resumen;

        if (globales != null) {
            if (globales.sesionEntity != null)
                if (globales.sesionEntity.empleado != null)
                    sesionActiva = true;
        }

        if (ii_rol == CPL.LECTURISTA && sesionActiva) {
            resumen = DbLecturasMgr.getInstance().getResumen(this);

            if (resumen != null) {
                if (resumen.totalRegistros > 0) {
                    if (resumen.cantLecturasPendientes > 0)
                        habilitarLecturas = true;
                    else
                        habilitarLecturas = false;
                }
            }

            if (habilitarLecturas) {
                if (globales.sesionEntity.empleado.RequiereCheckSeguridad) {
                    btnOperacion.setText("Hacer Check Seguridad");
                    btnOperacion.setVisibility(View.VISIBLE);
                    btnOperacion.setEnabled(true);
                    b_lecturas.setEnabled(false);
                }
                else {
                    btnOperacion.setVisibility(View.GONE);
                    btnOperacion.setEnabled(false);
                    b_lecturas.setEnabled(habilitarLecturas);
                }
            } else {
                b_lecturas.setVisibility(View.VISIBLE);
                b_lecturas.setEnabled(false);

                btnOperacion.setText("---");
                btnOperacion.setVisibility(View.VISIBLE);
                btnOperacion.setEnabled(false);
            }
        } else {
            b_lecturas.setVisibility(View.GONE);
            b_lecturas.setEnabled(false);
            btnOperacion.setVisibility(View.GONE);
            btnOperacion.setEnabled(false);
        }
    }

    /* ====================================================================================
        hacerCheckSeguridad()
        Solicita una foto al técnico y envía una notificación al servidor.
        También se encola la fotografía para que se envíe al servidor
    ==================================================================================== */

    private void hacerCheckSeguridad() {
        if (mOperacionesMgr == null) {
            mOperacionesMgr = new OperacionesMgr(this);
        }

        try {
            // Validar que las variables a ocupar no sean null

            if (globales == null) {
                mostrarMensaje("Alerta", "No se pudo hacer check de seguridad. Intente nuevamente.");
                return;
            }

            if (globales.sesionEntity == null) {
                mostrarMensaje("Alerta", "No se ha autenticado en la aplicación. Regrese a la pantalla inicial y capture su usuario y contraseña.");
                return;
            }

            if (globales.sesionEntity.empleado == null) {
                mostrarMensaje("Alerta", "No se ha autenticado en la aplicación. Regrese a la pantalla inicial y capture su usuario y contraseña.");
                return;
            }

            // Solicitar foto

            btnOperacion = (Button) this.findViewById(R.id.btnOperacion);
            FotoDeSeguridad(btnOperacion);

            // Notificar al servidor que se hizo el check de seguridad

            mOperacionesMgr.checkSeguridad(globales.getIdEmpleado(),
                    new OperacionesMgr.OperacionesCallback() {
                        @Override
                        public void enExito(OperacionRequest request, OperacionResponse resp) {
                            globales.sesionEntity.empleado.RequiereCheckIn = resp.RequiereCheckIn;
                            globales.sesionEntity.empleado.RequiereCheckSeguridad = resp.RequiereCheckSeguridad;
                            globales.sesionEntity.empleado.RequiereCheckOut = resp.RequiereCheckOut;
                            CambiarBotonesOperEstatus();
                        }

                        @Override
                        public void enFallo(OperacionRequest request, OperacionResponse resp) {
                            mostrarMensaje("Alerta", resp.Mensaje, resp.MensajeError, null);
                        }

                        @Override
                        public void enError(OperacionRequest request, OperacionResponse resp) {
                            mostrarMensaje("Alerta", resp.Mensaje, resp.MensajeError, null);
                        }
                    });

            Utils.showMessageShort(getApplicationContext(), "Enviada la notificación de check seguridad");
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Ha ocurrido un problema inesperado", t);
        }
    }

    // CE, 10/10/22, Vamos a tomar la Foto del Check de Seguridad
    public void FotoDeSeguridad(View view) {
        Intent camara = new Intent(this, CamaraActivity.class);
        camara.putExtra("secuencial", globales.sesionEntity.empleado.idEmpleado);
        camara.putExtra("caseta", Long.toString(globales.sesionEntity.empleado.idEmpleado));
        camara.putExtra("terminacion", "Check");
        camara.putExtra("temporal", 0);
        camara.putExtra("cantidad", 1);
        camara.putExtra("anomalia", "SinAnomalia");
        camara.putExtra("TipoFoto", CamaraActivity.TIPO_FOTO_EMPLEADO);
        // vengoDeFotos = true;
        startActivityForResult(camara, FOTO_CHECK_SEGURIDAD);
    }

    public void enviarAvance() {
        Intent lrs = new Intent(this, trasmisionDatos.class);
        lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
        lrs.putExtra("transmiteFotos", true);
        startActivityForResult(lrs, TRANSMISION);
    }

    private void procesarFotoCheckSeguridad(int requestCode, int resultCode, Intent data) {
        Bundle bu_params;
        int idFoto;

        try {
//            bu_params = data.getExtras();
//
//            if (bu_params == null)
//                throw new Exception("No se recibieron las parámetros de la Cámara");
//
//            idFoto = bu_params.getInt("idFoto");
            enviarAvance();
        } catch (AppException e) {
            mostrarMensaje("Alerta", e.getMessage());
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Error al recibir datos de la cámara", t);
        }
    }

    private void finalizarActivity() {
        stopListeningGPS();
        finalizarEnvioPuntosGps();

        Intent resultado = new Intent();
        resultado.putExtra("opcion", this.CAMBIAR_USUARIO);
        setResult(Activity.RESULT_OK, resultado);
    }

    public void onBackPressed() {
        finalizarActivity();
        finish();
    }

    private void mostrarMensaje(String titulo, String mensaje, String detalleError, DialogoMensaje.Resultado resultado) {
        if (mDialogoMsg == null) {
            mDialogoMsg = new DialogoMensaje(this);
        }

        mDialogoMsg.setOnResultado(resultado);
        mDialogoMsg.mostrarMensaje(titulo, mensaje, detalleError);
    }

    private void mostrarMensaje(String titulo, String mensaje) {
        if (mDialogoMsg == null) {
            mDialogoMsg = new DialogoMensaje(this);
        }

        mDialogoMsg.mostrarMensaje(titulo, mensaje, "");
    }

    private void mostrarMensaje(String titulo, String mensaje, Throwable t) {
        if (mDialogoMsg == null) {
            mDialogoMsg = new DialogoMensaje(this);
        }

        mDialogoMsg.mostrarMensaje(titulo, mensaje, t.getMessage());
    }

    private void probarCamara() {
        Intent camara = new Intent(this, CamaraActivity.class);
        camara.putExtra("secuencial", globales.sesionEntity.empleado.idEmpleado);
        camara.putExtra("caseta", Long.toString(globales.sesionEntity.empleado.idEmpleado));
        camara.putExtra("terminacion", "Check");
        camara.putExtra("temporal", 0);
        camara.putExtra("cantidad", 1);
        camara.putExtra("anomalia", "SinAnomalia");
        camara.putExtra("TipoFoto", CamaraActivity.TIPO_FOTO_EMPLEADO);
        // vengoDeFotos = true;
        startActivityForResult(camara, FOTO_PROBAR_CAMARA);
    }

    private void cerrarSesion() {
        globales.sesionEntity = null;
        finalizarActivity();
        finish();
    }
}


