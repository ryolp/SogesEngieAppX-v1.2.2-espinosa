package enruta.soges_engie;

import java.util.Locale;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
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

import enruta.soges_engie.clases.Utils;

@SuppressLint("NewApi")
public class BuscarMedidorActivity extends Activity {
    ImageButton b_clearText;
    TextView tv_msj_buscar, tv_instrucciones;
    TodasLasLecturas tll;
    ListView lv_medidores;
    EditText et_medidor;
    View layout;
    int contador = 0;
    int tipo = BuscarMedidorTabsPagerAdapter.DIRECCION;
    String strUltimaBusquedaRealizada = "";
    ProgressBar pb_ruleta;

    DBHelper dbHelper;
    SQLiteDatabase db;

    Handler mHandler;

    Vector<Lectura> vLecturas = new Vector<Lectura>();
    BuscarMedidorGridAdapter adapter;

    Globales globales;

    private DialogoMensaje mDialogoMsg = null; // RL, 2023-12-20, Clase para mostrar mensajes

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buscar_medidor_fragment);
        globales = ((Globales) getApplicationContext());

        setTitle("Listado de Órdenes");

        Bundle bu_params = getIntent().getExtras();
        if (bu_params != null) {
            tipo = bu_params.getInt("tipo");
            strUltimaBusquedaRealizada = bu_params.getString("ultimabusqueda");
        }
        lv_medidores = (ListView) findViewById(R.id.lv_medidores);
        pb_ruleta = (ProgressBar) findViewById(R.id.progressBar1);
        b_clearText = (ImageButton) findViewById(R.id.im_clearText);
        et_medidor = (EditText) findViewById(R.id.et_medidor);
        tv_msj_buscar = (TextView) findViewById(R.id.tv_msj_buscar); //tv_msj_buscar
        et_medidor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        et_medidor.setText(strUltimaBusquedaRealizada);
        tv_instrucciones = (TextView) findViewById(R.id.tv_instrucciones);

        tv_instrucciones.setText("Puede filtrar los datos escribiendo una parte del medidor, calle o colonia");

        tv_instrucciones.setBackgroundResource(R.color.SteelBlue);
        tv_instrucciones.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        tv_instrucciones.setTextColor(this.getResources().getColor(R.color.White));
        tv_instrucciones.setGravity(Gravity.CENTER_HORIZONTAL);

        tv_msj_buscar.setText(R.string.msj_buscar_direccion);
        tv_msj_buscar.setVisibility(View.VISIBLE);
        et_medidor.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView arg0, int arg1,
                                          KeyEvent arg2) {
                buscar();
                return false;
            }
        });

        b_clearText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                et_medidor.getText().clear();
            }
        });

        lv_medidores.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long id) {
                // TODO Auto-generated method stub
                regresaResultado(adapter.getSecuencia(pos));
            }
        });

        if (strUltimaBusquedaRealizada.equals("Campanita"))
            et_medidor.setVisibility(View.INVISIBLE);
        else
            et_medidor.setVisibility(View.VISIBLE);

        tv_msj_buscar.setBackgroundResource(R.drawable.buscar_pattern);
        mHandler = new Handler();
        buscar();
    }

    void regresaResultado(int secuencia) {
        Intent intent = new Intent();
        intent.putExtra("secuencia", secuencia);
        intent.putExtra("ultimabusqueda", et_medidor.getText().toString());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("ultimabusqueda", et_medidor.getText().toString());
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    private void setDatos() {
        pb_ruleta.setVisibility(View.GONE);
        if (vLecturas.isEmpty()) {
            tv_msj_buscar.setText(R.string.msj_buscar_no_medidores_direccion);
            tv_msj_buscar.setVisibility(View.VISIBLE);
            lv_medidores.setVisibility(View.GONE);
            return;
        } else {
            tv_msj_buscar.setVisibility(View.GONE);
            lv_medidores.setVisibility(View.VISIBLE);
            Toast.makeText(this, String.format(getString(R.string.msj_buscar_cant_med_encontrados), vLecturas.size()), Toast.LENGTH_SHORT).show();
        }
    }

    public void buscar() {
        Thread busqueda = new Thread() {
            public void run() {
                try {
                    boolean buscarPorDistancia = false;

                    buscarPorDistancia = MedirDistancias();

                    tll = new TodasLasLecturas(getApplicationContext(), 0);// Se buscaran las lecturas desde el principio
                    vLecturas = new Vector<Lectura>();
                    tll.ls_groupBy = "";
// CE, 14/10/23, Vamos a buscar entre los numeros de medidor tambien
//                tll.setFiltro("and upper(colonia || ' ' || calle || ' ' || entrecalles) like '%"+ et_medidor.getText().toString().trim().toUpperCase()+"%'");

// CE, 08/11/23, Vamos a buscar solamente entre las que están pendientes
//                tll.setFiltro("and ((upper(colonia || ' ' || calle || ' ' || entrecalles) like '%"+ et_medidor.getText().toString().trim().toUpperCase()+"%') or (serieMedidor like '%"+ et_medidor.getText().toString().trim()+"%'))");

// CE, 26/11/23, Vamos a permitir buscar por DX, RX, RM, RR y EX
                    String strTextoBuscado = "";
                    strTextoBuscado = et_medidor.getText().toString().trim().toUpperCase();
                    if (strTextoBuscado.equals("DX")) {
                        strTextoBuscado = " and tipoDeOrden = 'TO002'";
                    } else if (strTextoBuscado.equals("RX")) {
                        strTextoBuscado = " and (tipoDeOrden = 'TO003' or tipoDeOrden = 'TO006')";
                    } else if (strTextoBuscado.equals("RM")) {
                        strTextoBuscado = " and tipoDeOrden = 'TO005'";
                    } else if (strTextoBuscado.equals("RR")) {
                        strTextoBuscado = " and tipoDeOrden = 'TO004'";
                    } else if (strTextoBuscado.equals("EX")) {
                        strTextoBuscado = " and tipoDeOrden = 'TO006'";
                    } else {
                        strTextoBuscado = "and ((upper(colonia || ' ' || calle || ' ' || entrecalles) like '%" + et_medidor.getText().toString().trim().toUpperCase() + "%') or (serieMedidor like '%" + et_medidor.getText().toString().trim() + "%')) ";
                    }
                    if (strUltimaBusquedaRealizada.equals("Campanita"))
                        strTextoBuscado = " and ((balance = '1') or (balance = '2') or (balance = '3'))";
//                tll.setFiltro("and ((upper(colonia || ' ' || calle || ' ' || entrecalles) like '%" + et_medidor.getText().toString().trim().toUpperCase() + "%') or (serieMedidor like '%" + et_medidor.getText().toString().trim() + "%')) and anomalia='' and lectura=''");
                    tll.setFiltro(strTextoBuscado + " and anomalia='' and lectura=''");
                    try {
                        obtenerMedidor(buscarPorDistancia);
                        while (tll.encontrado) {
                            vLecturas.add(tll.getLecturaActual());
                            obtenerMedidor(buscarPorDistancia);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    adapter = new BuscarMedidorGridAdapter(getApplicationContext(), vLecturas, tipo, et_medidor.getText().toString().trim().toUpperCase(), tll.getNumRecords());
                    mHandler.post(new Runnable() {
                        public void run() {
                            try {
                                lv_medidores.setAdapter(adapter);
                                setDatos();
                            } catch (Throwable e) {
                                //Puede causarse un error si mientras se realiza la busqueda... se salen.
                            }
                        }
                    });
                } catch (Throwable t) {
                    mostrarMensajeToast(t.getMessage());
                }
            }
        };
        lv_medidores.setVisibility(View.GONE);
        pb_ruleta.setVisibility(View.VISIBLE);
        tv_msj_buscar.setVisibility(View.GONE);
        busqueda.start();
        esconderTeclado();
    }

    public void obtenerMedidor() throws Throwable {
        obtenerMedidor(false);
    }

    public void obtenerMedidor(boolean bPorDistancia) throws Throwable {
        if (bPorDistancia)
            tll.siguienteMedidorIndistintoPorDistancia();
        else
            tll.siguienteMedidorIndistinto();
    }

    public void esconderTeclado() {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(et_medidor.getWindowToken(), 0);
    }
/*
    public void reinicializaTAB(){
        pb_ruleta.setVisibility(View.GONE);
        tv_msj_buscar.setVisibility(View.VISIBLE);
        lv_medidores.setVisibility(View.GONE);
        et_medidor.getText().clear();
        tv_msj_buscar.setText(R.string.msj_buscar_direccion);
    }
*/

    private boolean MedirDistancias() throws Exception {
        //Aqui buscamos la key

        try {
            Cursor c;
            float distanciaEnMetros = 0;
            String strActualizarDistancia = "";
            double latitud = 0;
            double longitud = 0;
            String latitudStr = "";
            String longitudStr = "";
            String strTipoDeOrden = "";
            int cant = 0;

            if (globales.location == null)
                return false;

            latitud = globales.location.getLatitude();
            longitud = globales.location.getLongitude();

            openDatabase();
            c = db.rawQuery("Select * from ruta where lectura='' and anomalia='' order by cast(secuenciaReal as Integer) asc", null);

            cant = c.getCount();
            if (cant > 0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    // RL, 2023-12-19, Se guarda el valor de la latitud y longitud en una variable string.
                    latitudStr = Utils.getString(c, "miLatitud", "");
                    longitudStr = Utils.getString(c, "miLongitud", "");
                    strTipoDeOrden = Utils.getString(c, "tipoDeOrden", "");
// CE, 02/02/24, Vamos a poner primero a todas las Reconexiones Express
//                    if ((!latitudStr.equals("0.0")) && (!longitudStr.equals("0.0")) &&
//                            (!latitudStr.equals("")) && (!longitudStr.equals(""))) {
                    if ((!latitudStr.equals("0.0")) && (!longitudStr.equals("0.0")) &&
                            (!latitudStr.equals("")) && (!longitudStr.equals("")) &&
                            (!strTipoDeOrden.equals("TO006"))) {
                        try {
                            Location origen = new Location("Origen");
                            origen.setLatitude(latitud);
                            origen.setLongitude(longitud);
                            Location destino = new Location("Destino");
                            destino.setLatitude(Float.parseFloat(latitudStr));
                            destino.setLongitude(Float.parseFloat(longitudStr));
                            distanciaEnMetros = origen.distanceTo(destino);
                            strActualizarDistancia = "update ruta set diametro_toma = '" + String.format(Locale.US, "%.0f", distanciaEnMetros) + "' where secuenciaReal = " + Utils.getInt(c, "secuenciaReal", 0);
                        } catch (Exception e) {
//                        throw new Exception("Error al obtener punto GPS de un medidor");
                        }
                    } else {
                        strActualizarDistancia = "update ruta set diametro_toma = '0' where secuenciaReal = " + c.getInt(c.getColumnIndex("secuenciaReal"));
                    }
                    if (!strActualizarDistancia.equals(""))
                        db.execSQL(strActualizarDistancia);
                    c.moveToNext();
                }
            }
            c.close();
            closeDatabase();

            if (cant > 0)
                return true;
            else
                return false;
        } catch (Throwable t) {
            throw new Exception("Error al realizar la búsqueda: " + t.getMessage());
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

    private void mostrarMensaje(String titulo, String mensaje) {
        mHandler.post(new Runnable() {
            public void run() {
                mDialogoMsg.mostrarMensaje(titulo, mensaje, "");
            }
        });
    }

    private void mostrarMensajeToast(String mensaje) {
        mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(BuscarMedidorActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarMensaje(String titulo, String mensaje, String detalleError, DialogoMensaje.Resultado resultado) {
        mHandler.post(new Runnable() {
            public void run() {
                mDialogoMsg.setOnResultado(resultado);
                mDialogoMsg.mostrarMensaje(titulo, mensaje, detalleError);
            }
        });
    }
}

