package enruta.soges_engie;

import enruta.soges_engie.clases.AutenticadorMgr;
import enruta.soges_engie.entities.LoginRequestEntity;
import enruta.soges_engie.entities.LoginResponseEntity;
import enruta.soges_engie.entities.SesionEntity;
import enruta.soges_engie.entities.UsuarioEntity;
import enruta.soges_engie.services.WebApiManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import java.util.Calendar;
import java.util.Date;

public class CPL extends Activity {

    public final static int NINGUNO = 0;
    public final static int ADMINISTRADOR = 1;
    public final static int LECTURISTA = 2;
    public final static int SUPERUSUARIO = 3;

    public final static int ENTRADA = 1;
    public final static int LOGIN = 2;
    public final static int MAIN = 3;

    public final static int CAMBIAR_USUARIO = 1;

    public int ii_perfil = NINGUNO;
    public int ii_pantallaActual = NINGUNO;


    boolean esSuperUsuario = false;

    String is_nombre_Lect = "";

    TextView tv_msj_login, tv_usuario, tv_contrasena, tv_version;
    EditText et_usuario, et_contrasena;

    DBHelper dbHelper;
    SQLiteDatabase db;

    String admonPass = "9776";
    String superUsuarioPass = "9776";

    String usuario = "";

    Globales globales;
    ImageView iv_logo;

    Button b_admon;

    // RL, 2022-07-14, Campos para validación SMS

    private ImageView iv_nosotros;
    private TextView lblMensaje;
    private TextView lblCodigoSMS;
    private EditText txtCodigoSMS;
    private Button btnEntrar;
    private Button btnAutenticar;
    private Button btnValidarSMS;
    private int mIntentosAutenticacion = 0;
    private int mIntentosCodigoSMS = 0;
    private boolean mRegresarPantallaInicial = false;
    private int clicksLogo = 0;
    private Date fechaClickLogo = null;

    // RL. 2023-07-25, quitar la referencia de eventos de los botones del layout...
    // ... y ponerlos en código como sugiere Android.

    private Button btnAdministrador;
    private Button btnLecturista;

    // RL, 2023-07-10, Migración del Cortrex

    private DialogoMensaje mDialogoMsg = null;
    private AutenticadorMgr mAutenticadorMgr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cpl);
        ii_pantallaActual = ENTRADA;

        clicksLogo = 0;

        globales = ((Globales) getApplicationContext());

        esconderAdministrador();

        inicializarControles();

        try {
            tv_version.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode + ", " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);

        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        estableceVariablesDePaises();
        validarPermisos();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        inicializarControles();
        inicializarEventosControles();
        validarPermisos();

        if (globales == null)
            return;

//        if (globales.sesionEntity == null)
//            return;
//
//        if (globales.sesionEntity.esSesionVencida())
//            globales.sesionEntity = null;
//        else
//            globales.sesionEntity.inicializarHoraVencimiento();
    }

    public void salir() {
        if (globales != null) {
            globales.sesionEntity = null;
        }
        finish();
    }

    /**
     * Aqui se van a cargar las variables que correspondan a cada pais
     */
    private void estableceVariablesDePaises() {
        // TODO Auto-generated method stub

        switch (globales.ii_pais) {
            case Globales.ARGENTINA:
                globales.tdlg = new TomaDeLecturasArgentina(this);
                break;
            case Globales.DEMO:
                globales.tdlg = new TomaDeLecturasDemo(this);
                break;
//		 case Globales.COLOMBIA:
//			 globales.tdlg= new TomaDeLecturasColombia(this);
//			 break;
        }

        iv_logo.setImageResource(globales.logo);

    }


//    public void entrarAdministrador(View v) {
//        ii_perfil = ADMINISTRADOR;
//        setContentView(R.layout.p_login);
//        ii_pantallaActual = LOGIN;
//        getObjetosLogin();
//        tv_msj_login.setText(R.string.str_login_msj_admon);
//        tv_usuario.setVisibility(View.GONE);
//        et_usuario.setVisibility(View.GONE);
//        globales.secuenciaSuperUsuario = "A";
//
//        deshabilitarControlesAutenticacionSMS();
//
//        et_contrasena.requestFocus();
//
//        mostrarTeclado();
//
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//
//    }

//    public void entrarAdministrador(View v) {
//        entrarAdministrador2(v, false);
//    }

    /* ====================================================================================

    ==================================================================================== */

    public void entrarAdministrador(View v) {
        if (esSesionActiva()) {
            ii_perfil = ADMINISTRADOR;
            globales.secuenciaSuperUsuario = "";
            irActivityMain();
            return;
        }

        Intent intent = new Intent(this, LoginActivity.class);

        intent.putExtra("tipoUsuarioLogin", ADMINISTRADOR);

        startActivityForResult(intent, LOGIN);
    }

    /* ====================================================================================

    ==================================================================================== */

    public void entrarLecturista(View v) {
        if (esSesionActiva()) {
            ii_perfil = LECTURISTA;
            globales.secuenciaSuperUsuario = "";
            irActivityMain();
            return;
        }

        Intent intent = new Intent(this, LoginActivity.class);

        intent.putExtra("tipoUsuarioLogin", LECTURISTA);

        startActivityForResult(intent, LOGIN);
    }

    /* ====================================================================================

    ==================================================================================== */

    public void entrarAdministrador2(View v, boolean bForzarAdministrador) {
        String secuencia = "";
        ii_perfil = ADMINISTRADOR;
        ii_pantallaActual = LOGIN;

        secuencia = getSecuenciaSuperUsuario();

        if (!esSesionActiva()) {
            if (!secuencia.equals("A")) {
                setContentView(R.layout.p_login);
                getObjetosLogin();

                et_contrasena.setFilters(new InputFilter[]{new InputFilter.LengthFilter(globales.longCampoContrasena)});
                tv_msj_login.setText(R.string.str_login_msj_admon);
            }

            if (globales.tipoDeValidacion == globales.CON_SMS && !bForzarAdministrador) {
                habilitarControlesAutenticacionSMS();
                esSuperUsuario = false;
            }
            else {
                if (secuencia.equals("")){
                    tv_usuario.setVisibility(View.GONE);
                    et_usuario.setVisibility(View.GONE);

                    et_contrasena.setVisibility(View.VISIBLE);
                    tv_contrasena.setVisibility(View.VISIBLE);

                    deshabilitarControlesAutenticacionSMS();

                    et_contrasena.setFocusable(true);
                    et_contrasena.requestFocus();

                    globales.secuenciaSuperUsuario = "A";
                    esSuperUsuario = false;
                }
                else if (esSuperUsuario() && secuencia.equals("A"))
                {
                    globales.secuenciaSuperUsuario = "";
                    esSuperUsuario = true;
                    irActivityMain();
                    return;
                }
            }

            mostrarTeclado();

            mIntentosAutenticacion = 0;

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else {
            globales.secuenciaSuperUsuario = "";
            irActivityMain();
        }
    }

//    public void entrarLecturista(View v) {
//        ii_perfil = LECTURISTA;
//        ii_pantallaActual = LOGIN;
//        setContentView(R.layout.p_login);
//        getObjetosLogin();
//        globales.secuenciaSuperUsuario += "C";
//
//        if (!esSesionActiva()) {
//            deshabilitarControlesAutenticacionSMS();
//            //Hay que adaptar según el tipo de validacion
//            switch (globales.tipoDeValidacion) {
//
//                case Globales.AMBAS:
//                    et_usuario.requestFocus();
//                    break;
//
//                case Globales.USUARIO:
//                    et_usuario.requestFocus();
//
//                    et_contrasena.setVisibility(View.GONE);
//                    tv_contrasena.setVisibility(View.GONE);
//
//                    tv_usuario.setVisibility(View.VISIBLE);
//                    et_usuario.setVisibility(View.VISIBLE);
//                    break;
//
//                case Globales.CONTRASEÑA:
//                    tv_usuario.setVisibility(View.VISIBLE);
//                    et_usuario.setVisibility(View.GONE);
//
//                    et_contrasena.setVisibility(View.VISIBLE);
//                    tv_contrasena.setVisibility(View.GONE);
//
//                    et_contrasena.requestFocus();
//                    break;
//                case Globales.CON_SMS:
//                    habilitarControlesAutenticacionSMS();
//                    break;
//            }
//
////		if(globales.tipoDeValidacion==Globales.CONTRASEÑA)
////			tv_msj_login.setText(R.string.str_login_msj_lecturista_contrasena);
////		else
//            tv_msj_login.setText(globales.mensajeContraseñaLecturista);
//
//            mostrarTeclado();
//
//            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//        } else
//            irActivityMain();
//    }


    /* ====================================================================================


    ==================================================================================== */

    public void entrar(View v) {
        boolean validar = false;
        switch (ii_perfil) {
            case ADMINISTRADOR:
                esconderTeclado();
                validar = validarAdministrador();
                break;
            case LECTURISTA:
                esconderTeclado();
                validar = validarLecturista();

                break;
        }

        if (validar) {
            //Aqui abrimos la actividad

            //Hay que empezar a restingir las cosas que cada uno puede hacer

            Intent intent = new Intent(this, Main.class);
            intent.putExtra("rol", ii_perfil);
            intent.putExtra("esSuperUsuario", esSuperUsuario);
            intent.putExtra("nombre", is_nombre_Lect);


            startActivityForResult(intent, MAIN);
        } else {

            switch (ii_perfil) {
                case ADMINISTRADOR:
                    Toast.makeText(this, getString(R.string.msj_cpl_verifique_contrasena), Toast.LENGTH_LONG).show();
                    globales.secuenciaSuperUsuario += "B";
                    break;
                case LECTURISTA:
                    if (globales.tipoDeValidacion == Globales.CONTRASEÑA)
                        Toast.makeText(this, getString(R.string.msj_cpl_verifique_contrasena), Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this, getString(R.string.msj_cpl_verifique_usuario_contrasena), Toast.LENGTH_LONG).show();
                    break;
            }
            et_usuario.setText("");
            et_contrasena.setText("");

        }
    }

    /* ====================================================================================

    ==================================================================================== */

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bu_params = null;
        if (data != null) {
            bu_params = data.getExtras();
        }
        switch (requestCode) {
            case MAIN:
                if (resultCode == Activity.RESULT_CANCELED) {
                    finish(); //Cancelo con el back
                } else if (resultCode == Activity.RESULT_OK) {
                    //Cuando cambia de usuario...
                    if (bu_params.getInt("opcion") == CAMBIAR_USUARIO) {
                        cambiarUsuario();
                    }
                }
            case LOGIN:
                procesarLogin(bu_params);
        }
    }

    /* ====================================================================================

    ==================================================================================== */

    public boolean validarLecturista() {
        if (et_contrasena.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Escriba un numero de usuario", Toast.LENGTH_LONG);
            return false;
        }

//		if (et_contrasena.getText().toString().trim().length()!=10){
//			Toast.makeText(this, "El numero de usuario debe ser de 10 digitos", Toast.LENGTH_LONG);
//			return false;
//		}
        globales.setUsuario(et_contrasena.getText().toString().trim());
        return true;
//		boolean esValido=false;
//		
//		esSuperUsuario=(et_contrasena.getText().toString().equals(superUsuarioPass)||et_usuario.getText().toString().equals(superUsuarioPass)) && globales.secuenciaSuperUsuario.equals(Globales.SECUENCIA_CORRECTA_SUPER);
//		
//		//Hay que buscar que la combinacion usuario y contraseña sean correctos
//		if (esSuperUsuario){
//			esValido=true;
//			is_nombre_Lect="Super Usuario";
//			globales.setUsuario("9776");
//		}
//		else{
//			openDatabase();
//			Cursor c;
//			
//			switch(globales.tipoDeValidacion){
//			case Globales.CONTRASEÑA:
//				c= db.rawQuery("Select * from usuarios where trim (contrasena)='" +et_contrasena.getText().toString().trim()+"'" , null) ;
//				break;
//			case Globales.USUARIO:
//				c= db.rawQuery("Select * from usuarios where trim(usuario)='" +et_usuario.getText().toString().trim() +"' " , null) ;
//				break;
//			default:
//				c= db.rawQuery("Select * from usuarios where trim(usuario)='" +et_usuario.getText().toString().trim() +"' "+
//						" and trim (contrasena)='" +et_contrasena.getText().toString().trim()+"'" , null) ;
//				break;
//			}
//			
//			
//			
//			if (c.getCount()>0){
//				esValido= true;
//				c.moveToFirst();
//				
//				if (globales.tipoDeValidacion==Globales.CONTRASEÑA){
//					globales.setUsuario(et_contrasena.getText().toString().trim());
//				}
//				else{
//					globales.setUsuario(et_usuario.getText().toString().trim());
//				}
//				globales.controlCalidadFotos=c.getInt(c.getColumnIndex("fotosControlCalidad"));
//				globales.baremo=Lectura.toInteger(c.getString(c.getColumnIndex("baremo")));
//				is_nombre_Lect=c.getString(c.getColumnIndex("nombre"));
//				
//			}
//				
//			
//			c.close();
////			c= db.rawQuery("Select * from usuarios ", null) ;
////			c.moveToFirst();
////			String usuario= c.getString(0);
////			String contraseña=c.getString(1);
////			
////			c.moveToNext();
////			usuario= c.getString(0);
////			contraseña=c.getString(1);
//
//			closeDatabase();
//		}
//
//		
//		return esValido;
    }

    private void openDatabase() {
        dbHelper = new DBHelper(this);

        db = dbHelper.getReadableDatabase();
    }

    private void closeDatabase() {
        db.close();
        dbHelper.close();
    }

    /* ====================================================================================

    ==================================================================================== */

    private void procesarLogin(Bundle bu_params) {
        int opcionLogin;
        int permisoUsuario;

        if (bu_params == null)
            return;

        opcionLogin = bu_params.getInt("opcionLogin");
        permisoUsuario= bu_params.getInt("permisoUsuario");

        if (globales.sesionEntity != null)
            mostrarMensaje("Mensaje", "Éxito login", null, null);
        else
            mostrarMensaje("Mensaje", "Fallo login", null, null);
    }

    /* ====================================================================================

    ==================================================================================== */

    public boolean validarAdministrador() {
        openDatabase();
        //Buscamos si existe la palabra administrador en los ususatios
        Cursor c;
        c = db.rawQuery("Select * from usuarios where rol in ('2', '3') ", null);

        if (c.getCount() > 0) {
            //Existe un administrador, usaremos su contraseña para entrar al sistema
            c.close();
            c = db.rawQuery("Select * from usuarios where rol in ('2', '3') and trim (contrasena)='" + et_contrasena.getText().toString().trim() + "'", null);
            if (c.getCount() > 0) {
                c.close();
                return true;
            } else {
                c.close();
                return false;
            }
        }
        c.close();
        closeDatabase();
        try {
            globales.ii_claveIngresada = Integer.parseInt(this.et_contrasena.getText().toString());
        } catch (Throwable e) {

        }
        if (this.et_contrasena.getText().toString().equals(String.valueOf(globales.CLAVE_COMAPA_ZC)) || this.et_contrasena.getText().toString().equals(String.valueOf(globales.CLAVE_ENRUTA))
                || this.et_contrasena.getText().toString().equals(String.valueOf(globales.CLAVE_PRUEBAS2))
                || this.et_contrasena.getText().toString().equals(String.valueOf(globales.CLAVE_PRUEBAS3))
                || this.et_contrasena.getText().toString().equals(String.valueOf(globales.CLAVE_MEXICANA))
                || this.et_contrasena.getText().toString().equals(String.valueOf(globales.CLAVE_PREPAGO))) {
            return true;
        }

        return this.et_contrasena.getText().toString().equals(admonPass);
    }

    /* ====================================================================================

    ==================================================================================== */

    public void cambiarUsuario() {
        setContentView(R.layout.cpl);
        esSuperUsuario = false;
        ii_pantallaActual = ENTRADA;
        ii_perfil = NINGUNO;
        TextView tv_version = (TextView) findViewById(R.id.tv_version_lbl);

        try {
            tv_version.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode + ", " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);

        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        iv_logo.setImageResource(globales.logo);
        globales.anomaliaARepetir = "";
        globales.subAnomaliaARepetir = "";


        esconderAdministrador();
    }

    /* ====================================================================================

    ==================================================================================== */

    private void esconderAdministrador() {
        // TODO Auto-generated method stub
        openDatabase();
        b_admon = (Button) findViewById(R.id.b_admon);
        Cursor c = db.rawQuery(
                "Select value from config where key='server_gprs'", null);
        c.moveToFirst();

/*        if (c.getCount() > 0) {
            if (c.getString(c.getColumnIndex("value")).length() > 0) {
                b_admon.setVisibility(View.GONE);
            } else {
                b_admon.setVisibility(View.VISIBLE);
            }
        } else {
            b_admon.setVisibility(View.VISIBLE);
        }*/

        closeDatabase();
    }

    /* ====================================================================================

    ==================================================================================== */

    public void getObjetosLogin() {
        tv_msj_login = (TextView) findViewById(R.id.tv_msj_login);
        et_usuario = (EditText) findViewById(R.id.et_usuario);
        et_contrasena = (EditText) findViewById(R.id.et_contrasena);
        tv_usuario = (TextView) findViewById(R.id.tv_usuario);
        tv_contrasena = (TextView) findViewById(R.id.tv_contrasena);

        lblCodigoSMS = (TextView) findViewById(R.id.lblCodigoSMS);
        txtCodigoSMS = (EditText) findViewById(R.id.txtCodigoSMS);

        btnAutenticar = (Button) findViewById(R.id.btnAutenticar);
        btnValidarSMS = (Button) findViewById(R.id.btnValidarSMS);
        btnEntrar = (Button) findViewById(R.id.b_entrar);

        et_usuario.setFocusableInTouchMode(true);
        et_contrasena.setFocusableInTouchMode(true);
        txtCodigoSMS.setFocusableInTouchMode(true);

        inicializarEventosControlesLogin();

        OnEditorActionListener oeal = new OnEditorActionListener() {


            @Override
            public boolean onEditorAction(TextView arg0, int arg1,
                                          KeyEvent arg2) {
                // TODO Auto-generated method stub
                entrar(arg0);
                return false;
            }
        };

        if (globales.tipoDeValidacion == Globales.USUARIO) {
            et_usuario.setOnEditorActionListener(oeal);
        } else {
            et_contrasena.setOnEditorActionListener(oeal);
        }

//et_contrasena.setOnEditorActionListener(new OnEditorActionListener() {
//
//			
//
//			@Override
//			public boolean onEditorAction(TextView arg0, int arg1,
//					KeyEvent arg2) {
//				// TODO Auto-generated method stub
//				entrar(arg0);
//				return false;
//			}
//	       });
    }

    /* ====================================================================================

    ==================================================================================== */

    public void onBackPressed() {
        switch (ii_pantallaActual) {
            case ENTRADA:
                salir();
                break;
            case LOGIN:
                cambiarUsuario();
                break;

        }
    }


    public void esconderTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_usuario.getWindowToken(), 0);
    }

    public void mostrarTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    /* ====================================================================================
        Inicializar las variables relacionadas a los controles
    ==================================================================================== */

    private void inicializarControles() {
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        iv_nosotros = (ImageView) findViewById(R.id.iv_nosotros);
        tv_version = (TextView) findViewById(R.id.tv_version_lbl);
        lblMensaje = (TextView) findViewById(R.id.txtMensaje);
        btnAdministrador = (Button) findViewById(R.id.b_admon);
        btnLecturista = (Button) findViewById(R.id.b_lecturista);

        if (btnAdministrador != null) {
            btnAdministrador.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    entrarAdministrador(view);
                }
            });

            btnAdministrador.setEnabled(true);
        }

        if (btnLecturista != null) {
            btnLecturista.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    entrarLecturista(view);
                }
            });

            btnLecturista.setEnabled(true);
        }

        if (lblMensaje != null) {
            lblMensaje.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            lblMensaje.setText("");
            lblMensaje.setVisibility(View.GONE);
        }

        if (iv_logo != null) {
            iv_logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activarAdministrador();
                }
            });
        }
    }

            /*
        Inicializar los eventos de los botones principales del activity
     */

    /* ====================================================================================
        Inicializar los eventos de los botones principales del activity
    ==================================================================================== */

    private void inicializarEventosControles() {
        if (btnAdministrador != null)
            btnAdministrador.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    entrarAdministrador(view);
                }
            });

        if (btnLecturista != null)
            btnLecturista.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    entrarLecturista(view);
                }
            });
    }

     /*
        Validación de permisos
        RLR / 2022-08-25
        RLR / 2023-07-10 / Se agrega
     */

    private void validarPermisos() {
        boolean tienePermisos = true;
        String msg = "";

        if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            tienePermisos = false;
        }

        if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            tienePermisos = false;
        }

        if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            tienePermisos = false;
        }

        if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            tienePermisos = false;
        }

        if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            tienePermisos = false;
        }

        if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tienePermisos = false;
        }

        if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            tienePermisos = false;
        }

        if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            tienePermisos = false;
        }

        if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tienePermisos = false;
        }

        if (!tienePermisos) {
            showMessageLong("Faltan permisos");
            if (lblMensaje != null) {
                lblMensaje.setText("Faltan permisos");
                lblMensaje.setVisibility(View.VISIBLE);
            }
//            if (btnAdministrador != null)
//                btnAdministrador.setEnabled(false);
//
//            if (btnLecturista != null)
//                btnLecturista.setEnabled(false);
        } else {
            if (lblMensaje != null) {
                lblMensaje.setText("");
                lblMensaje.setVisibility(View.GONE);
            }

//            if (btnAdministrador != null)
//                btnAdministrador.setEnabled(true);
//
//            if (btnLecturista != null)
//                btnLecturista.setEnabled(true);
        }
    }

    /*
        Muestra un mensaje tipo pop up de duración larga
        RLR / 2022-08-25
    */

    private void showMessageLong(String sMessage) {
        Toast.makeText(this, sMessage, Toast.LENGTH_LONG).show();
    }

    /*
        Muestra un mensaje tipo pop up de duración corta
        RLR / 2022-08-25
    */

    private void showMessageShort(String sMessage) {
        Toast.makeText(this, sMessage, Toast.LENGTH_SHORT).show();
    }


    protected boolean esSesionActiva() {
        if (globales.sesionEntity == null)
            return false;

        if (globales.sesionEntity.esSesionVencida())
            return false;
        else
            globales.sesionEntity.inicializarHoraVencimiento();

        return true;
    }

    private Date getFechaAgregarSegundos(int segundos){
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, segundos);
        return calendar.getTime();
    }

    private void activarAdministrador() {
        Date horaActual;

        if (b_admon.getVisibility() != View.VISIBLE) {
            if (clicksLogo == 0) {
                fechaClickLogo = getFechaAgregarSegundos(60);
                clicksLogo++;
            }
            else
            {
                horaActual = Calendar.getInstance().getTime();

                if (horaActual.after(fechaClickLogo))
                    clicksLogo = 0;
                else
                    clicksLogo++;

                if (clicksLogo>10) {
                    b_admon.setVisibility(View.VISIBLE);
                    clicksLogo = 0;
                    fechaClickLogo = null;
                }
            }



        }
    }

    /*
        Regresa el manejador de llamadas por internet con el servidor especificado ...
        ... en la variable global defaultServidorGPRS
        RLR / 2022-08-25
    */
    private WebApiManager getLoginApiManager() throws Exception {
        try {
            return WebApiManager.getInstance(this);
        } catch (Exception ex) {
            throw ex;
        }
    }

    /*
        Regresa el nombre de la versión de este APK
        RLR / 2022-08-25
    */

    private String getVersionName() {
        String versionName;

        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ex) {
            versionName = "";
        }

        return versionName;
    }

    /*
        Regresa el código de versión de este APK
        RLR / 2022-08-25
    */

    private String getVersionCode() {
        long versionCodeMajor;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                versionCodeMajor = getPackageManager().getPackageInfo(getPackageName(), 0).getLongVersionCode();
            else
                versionCodeMajor = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception ex) {
            versionCodeMajor = 0;
        }

        return Long.toString(versionCodeMajor);
    }

    private boolean esSuperUsuario() {
        String usuario = getUsuario();
        String password = getPassword();
        String secuencia = getSecuenciaSuperUsuario();

        if (ii_perfil != ADMINISTRADOR)
            return false;

        if (usuario.contains("*9776"))
            return true;
        else if (secuencia.equals("A") && password.contains("9776"))
            return true;
        else
            return false;
    }


    private void mostrarMensaje(String titulo, String mensaje, String detalleError, DialogoMensaje.Resultado resultado) {
        if (mDialogoMsg == null) {
            mDialogoMsg = new DialogoMensaje(this);
        }

        mDialogoMsg.setOnResultado(resultado);
        mDialogoMsg.mostrarMensaje(titulo, mensaje, detalleError);
    }

    private void procesarAutenticacion(LoginResponseEntity loginResponseEntity) {
        mIntentosCodigoSMS = 0;

        if (loginResponseEntity.Exito) {
            globales.sesionEntity = new SesionEntity(loginResponseEntity);

            if (loginResponseEntity.AutenticarConSMS) {
                lblCodigoSMS.setVisibility(View.VISIBLE);
                txtCodigoSMS.setVisibility(View.VISIBLE);
                btnValidarSMS.setVisibility(View.VISIBLE);
                btnAutenticar.setVisibility(View.GONE);
                btnAutenticar.setEnabled(false);
                et_usuario.setFocusable(false);
                et_usuario.setEnabled(false);
                et_contrasena.setFocusable(false);
                et_contrasena.setEnabled(false);
            } else {
                globales.sesionEntity.Autenticado = true;
                globales.secuenciaSuperUsuario = "";
                irActivityMain();
            }
        }
    }

    private void falloAutenticacion(LoginRequestEntity req, LoginResponseEntity resp) {
        String msg;

        globales.sesionEntity = null;
        mIntentosAutenticacion++;

        if (mIntentosAutenticacion >= 5) {
            msg = resp.Mensaje + ". Máximo de intentos.";
            globales.sesionEntity = null;
            deshabilitarAutenticacion();
            mRegresarPantallaInicial = true;
        } else
            msg = resp.Mensaje + ". Intento " + mIntentosAutenticacion + " de 5";

        mostrarMensaje("Alerta", msg, "", new DialogoMensaje.Resultado() {
            @Override
            public void Aceptar(boolean EsOk) {
                if (mRegresarPantallaInicial) {
                    globales.sesionEntity = null;
                    cambiarUsuario();
                }
            }
        });
    }

    private void errorAutenticacion(String mensaje) {
        mostrarMensaje("Alerta", mensaje, "", new DialogoMensaje.Resultado() {
            @Override
            public void Aceptar(boolean EsOk) {

            }
        });
    }

    private void errorAutenticacion(LoginRequestEntity req, LoginResponseEntity resp) {
        mostrarMensaje("Alerta", resp.Mensaje, resp.MensajeError, new DialogoMensaje.Resultado() {
            @Override
            public void Aceptar(boolean EsOk) {

            }
        });
    }

    private void validarSMS(View view) {
        String usuario;
        String codigoSMS;

        try {
            usuario = et_usuario.getText().toString().trim();
            codigoSMS = txtCodigoSMS.getText().toString().trim();

            if (usuario.equals("") || codigoSMS.equals("")) {
                mostrarMensaje("Alerta", "Falta capturar el código SMS", "", null);
                return;
            }

            mRegresarPantallaInicial = false;
            inicializarAutenticador();

            if (mAutenticadorMgr != null)
                mAutenticadorMgr.validarSMS(usuario, codigoSMS);
            else
                errorValidacionSMS("Error inesperado");
        } catch (Exception ex) {
            globales.sesionEntity = null;
            errorValidacionSMS("No hay conexión a internet. Intente nuevamente. (3).");
            Log.d("CPL", "No hay conexión a internet. Intente nuevamente. (3) : " + ex.getMessage());
        }
    }

    private void falloValidacionSMS(LoginRequestEntity req, LoginResponseEntity resp) {
        String msg;

        mIntentosCodigoSMS++;

        if (mIntentosCodigoSMS >= 5) {
            msg = "Se alcanzó el máximo de intentos de valicación del código SMS";
            deshabilitarAutenticacionSMS();
            globales.sesionEntity = null;
            mRegresarPantallaInicial = true;
        } else {
            msg = resp.Mensaje + ". Intento " + mIntentosCodigoSMS + " de 5";
        }

        mostrarMensaje("Alerta", msg, "", new DialogoMensaje.Resultado() {
            @Override
            public void Aceptar(boolean EsOk) {
                if (mRegresarPantallaInicial) {
                    globales.sesionEntity = null;
                    cambiarUsuario();
                }
            }
        });
    }

    private void errorValidacionSMS(String mensaje) {
        mostrarMensaje("Alerta", mensaje, "", new DialogoMensaje.Resultado() {
            @Override
            public void Aceptar(boolean EsOk) {

            }
        });
    }

    private void errorValidacionSMS(LoginRequestEntity req, LoginResponseEntity resp) {
        mostrarMensaje("Alerta", resp.Mensaje, resp.MensajeError, new DialogoMensaje.Resultado() {
            @Override
            public void Aceptar(boolean EsOk) {

            }
        });
    }

    private void procesarValidacionSMS(LoginResponseEntity loginResponseEntity) {
        if (loginResponseEntity.Error) {
            globales.sesionEntity = null;
            showMessageLong("No hay conexión a internet. Intente nuevamente. (4) : " + loginResponseEntity.Mensaje);
            return;
        }

        if (loginResponseEntity.Exito) {
            globales.sesionEntity = new SesionEntity(loginResponseEntity);
            globales.sesionEntity.Autenticado = true;
            globales.secuenciaSuperUsuario = "";
            irActivityMain();
        }
    }

    private void deshabilitarAutenticacion() {
        et_usuario.setText("");
        et_contrasena.setText("");
        txtCodigoSMS.setText("");

        btnEntrar.setVisibility(View.GONE);
        btnAutenticar.setVisibility(View.GONE);
        btnValidarSMS.setVisibility(View.GONE);

        et_usuario.setVisibility(View.GONE);
        et_contrasena.setVisibility(View.GONE);
        txtCodigoSMS.setVisibility(View.GONE);

        et_usuario.setFocusable(false);
        et_contrasena.setFocusable(false);
        txtCodigoSMS.setFocusable(false);
    }

    private void deshabilitarAutenticacionSMS() {
        btnEntrar.setVisibility(View.GONE);
        btnAutenticar.setVisibility(View.GONE);
        btnValidarSMS.setVisibility(View.GONE);
        txtCodigoSMS.setVisibility(View.GONE);
        lblCodigoSMS.setVisibility(View.GONE);
        et_usuario.setFocusable(false);
        et_contrasena.setFocusable(false);
        txtCodigoSMS.setFocusable(false);
    }

    private void irActivityMain() {
        deshabilitarAutenticacion();

        esSuperUsuario = globales.sesionEntity.EsSuperUsuario;
        is_nombre_Lect = globales.sesionEntity.Usuario;

        switch (ii_perfil) {
            case ADMINISTRADOR:
                if (!globales.sesionEntity.EsAdministrador && !globales.sesionEntity.EsSuperUsuario) {
                    showMessageLong("No tiene permisos de administrador");
                    return;
                }
                break;
            case LECTURISTA:
                if (!globales.sesionEntity.EsAdministrador && !globales.sesionEntity.EsSuperUsuario && !globales.sesionEntity.EsLecturista) {
                    showMessageLong("No tiene permisos de administrador o lecturista");
                    return;
                }
                break;
        }

        esconderTeclado();

        globales.setUsuario(globales.sesionEntity.NumCPL);

        Intent intent = new Intent(this, Main.class);
        intent.putExtra("rol", ii_perfil);
        intent.putExtra("esSuperUsuario", esSuperUsuario);
        intent.putExtra("nombre", is_nombre_Lect);
        startActivityForResult(intent, MAIN);
    }

}
