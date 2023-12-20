package enruta.soges_engie;

import static enruta.soges_engie.Main.FOTO_PROBAR_VIDEO;

import enruta.soges_engie.services.WebApiManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Calendar;
import java.util.Date;

public class CPL extends AppCompatActivity {

    public final static int NINGUNO = 0;
    public final static int ADMINISTRADOR = 1;
    public final static int LECTURISTA = 2;
    public final static int SUPERUSUARIO = 3;

    public final static int ENTRADA = 1;
    public final static int LOGIN = 2;
    public final static int MAIN = 3;

    public final static int CAMBIAR_USUARIO = 1;

    public int ii_perfil = NINGUNO;

    private boolean esSuperUsuario = false;

    private String is_nombre_Lect = "";

    private TextView tv_version;

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private Globales globales;
    private ImageView iv_logo;

    private Button b_admon;

    // RL, 2022-07-14, Campos para validación SMS

//    private ImageView iv_nosotros;
    private TextView lblMensaje;
    private int clicksLogo = 0;
    private Date fechaClickLogo = null;

    // RL. 2023-07-25, quitar la referencia de eventos de los botones del layout...
    // ... y ponerlos en código como sugiere Android.

    private Button btnAdministrador;
    private Button btnLecturista;

    // RL, 2023-07-10, Migración del Cortrex

    private DialogoMensaje mDialogoMsg = null;

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cpl);

        clicksLogo = 0;

        globales = ((Globales) getApplicationContext());

        esconderAdministrador();

        inicializarControles();

        showAppVersion();

        estableceVariablesDePaises();
    }

    private void showAppVersion() {
        String version;

        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode + ", " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
//CE, 11/10/23, Por diseño de la pantalla, vamos a quitar el numero de version
//            tv_version.setText(version);

        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
            case Globales.ENGIE:
                globales.tdlg = new TomaDeLecturasEngie(this);
                break;
//		 case Globales.COLOMBIA:
//			 globales.tdlg= new TomaDeLecturasColombia(this);
//			 break;
        }
// CE, 11/10/23, Vamos a trabajar en el Rediseño
//        iv_logo.setImageResource(globales.logo);

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
    }

//    public void salir() {
//        if (globales != null) {
//            globales.sesionEntity = null;
//        }
//        finish();
//    }


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

    private void hacerAutenticacion(int opcionLogin) {
        if (!esSesionActiva()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("opcionLogin", opcionLogin);
            intent.putExtra("ultimousuario", getStringValue("ultimousuario"));
            startActivityForResult(intent, LOGIN);
        }
        else {
            switch (opcionLogin) {
                case ADMINISTRADOR:
                    entrarAdministrador();
                    break;
                case LECTURISTA:
                    entrarLecturista();
                    break;
            }
        }
    }

    /* ====================================================================================

    ==================================================================================== */

    public void entrarAdministrador() {
        if (!esSesionActiva() && !globales.esSuperUsuario)
            return;

        ii_perfil = ADMINISTRADOR;
        globales.secuenciaSuperUsuario = "";
        irActivityMain();
    }

    /* ====================================================================================

    ==================================================================================== */

    public void entrarLecturista() {
        if (!esSesionActiva())
            return;

        ii_perfil = LECTURISTA;
        globales.secuenciaSuperUsuario = "";
        irActivityMain();
        return;
    }

    /* ====================================================================================

    ==================================================================================== */

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bu_params = null;
        boolean cancelo = false;

        if (data != null) {
            bu_params = data.getExtras();
        }
        switch (requestCode) {
            case MAIN:
                if (resultCode == Activity.RESULT_CANCELED) {
                    cancelo = true;
//                    finish(); //Cancelo con el back
                } else if (resultCode == Activity.RESULT_OK) {
                    cancelo = false;
//                    //Cuando cambia de usuario...
//                    if (bu_params.getInt("opcion") == CAMBIAR_USUARIO) {
//                        cambiarUsuario();
//                    }
                }
                break;
            case LOGIN:
                procesarLogin(bu_params);
                openDatabase();
                db.execSQL("update ruta set verDatos=0, fechaDeInicio='' where lectura='' AND anomalia=''");
                closeDatabase();
                break;
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

    /* ====================================================================================

    ==================================================================================== */

    private void procesarLogin(Bundle bu_params) {
        int opcionLogin;
        int permisoUsuario;

        if (bu_params == null)
            return;

        opcionLogin = bu_params.getInt("opcionLogin");
        permisoUsuario = bu_params.getInt("permisoUsuario");

        if (globales.sesionEntity != null)
            globales.sesionEntity.Autenticado = true;

        globales.secuenciaSuperUsuario = "";

        switch (opcionLogin) {
            case SUPERUSUARIO:
                globales.esSuperUsuario = true;
                entrarAdministrador();
                break;
            case ADMINISTRADOR:
                globales.esSuperUsuario = false;
                entrarAdministrador();
                break;
            case LECTURISTA:
                globales.esSuperUsuario = false;
                entrarLecturista();
                break;
        }
    }


    /* ====================================================================================

    ==================================================================================== */

//    public void cambiarUsuario() {
//        setContentView(R.layout.cpl);
//        esSuperUsuario = false;
//        ii_perfil = NINGUNO;
//        TextView tv_version = (TextView) findViewById(R.id.tv_version_lbl);
//
//        try {
//            tv_version.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode + ", " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
//
//        } catch (NameNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        iv_logo = (ImageView) findViewById(R.id.iv_logo);
//        iv_logo.setImageResource(globales.logo);
//        globales.anomaliaARepetir = "";
//        globales.subAnomaliaARepetir = "";
//
//
//        esconderAdministrador();
//    }

    /* ====================================================================================

    ==================================================================================== */

    private void esconderAdministrador() {
        // TODO Auto-generated method stub
        openDatabase();
        b_admon = (Button) findViewById(R.id.b_admon);
        Cursor c = db.rawQuery(
                "Select value from config where key='server_gprs'", null);
        c.moveToFirst();
        c.close();

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

//    public void onBackPressed() {
//        switch (ii_pantallaActual) {
//            case ENTRADA:
//                salir();
//                break;
////            case LOGIN:
////                cambiarUsuario();
////                break;
//
//        }
//    }

    /* ====================================================================================
        Inicializar las variables relacionadas a los controles
    ==================================================================================== */

    private void inicializarControles() {
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
//        iv_nosotros = (ImageView) findViewById(R.id.iv_nosotros);
        tv_version = (TextView) findViewById(R.id.tv_version_lbl);
        lblMensaje = (TextView) findViewById(R.id.txtMensaje);
        btnAdministrador = (Button) findViewById(R.id.b_admon);
        btnLecturista = (Button) findViewById(R.id.b_lecturista);

        if (btnAdministrador != null) {
            btnAdministrador.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hacerAutenticacion(ADMINISTRADOR);
                }
            });

            btnAdministrador.setEnabled(true);
        }

        if (btnLecturista != null) {
            btnLecturista.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hacerAutenticacion(LECTURISTA);
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
                    hacerAutenticacion(ADMINISTRADOR);
                }
            });

        if (btnLecturista != null)
            btnLecturista.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hacerAutenticacion(LECTURISTA);
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

        if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
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

        if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            tienePermisos = false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                tienePermisos = false;
            }

            if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                tienePermisos = false;
            }

            if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                tienePermisos = false;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                tienePermisos = false;
            }

            if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                tienePermisos = false;
            }

            if (ActivityCompat.checkSelfPermission(CPL.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                tienePermisos = false;
            }
        }

//        if (!HandleAndroidPermissions.hasPermissions(context, *PERMISSIONS)) {
//            ActivityCompat.requestPermissions(
//                    requireActivity(),
//                    PERMISSIONS,
//                    101
//            );
//        } else {
//            captureImage()
//        }

        if (!tienePermisos) {
            showMessageLong("Faltan permisos");
            if (lblMensaje != null) {
                lblMensaje.setText("Faltan permisos");
                lblMensaje.setVisibility(View.VISIBLE);
            }
            if (btnAdministrador != null)
                btnAdministrador.setEnabled(false);

            if (btnLecturista != null)
                btnLecturista.setEnabled(false);
        } else {
            if (lblMensaje != null) {
                lblMensaje.setText("");
                lblMensaje.setVisibility(View.GONE);
            }

            if (btnAdministrador != null)
                btnAdministrador.setEnabled(true);

            if (btnLecturista != null)
                btnLecturista.setEnabled(true);
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

    private Date getFechaAgregarSegundos(int segundos) {
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
            } else {
                horaActual = Calendar.getInstance().getTime();

                if (horaActual.after(fechaClickLogo))
                    clicksLogo = 0;
                else
                    clicksLogo++;

                if (clicksLogo > 10) {
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

    private void irActivityMain() {
        if (globales == null)
            return;

        if (globales.esSuperUsuario)
        {
            Intent intent = new Intent(this, Main.class);
            intent.putExtra("rol", ii_perfil);
            intent.putExtra("esSuperUsuario", globales.esSuperUsuario);
            intent.putExtra("nombre", is_nombre_Lect);
            startActivityForResult(intent, MAIN);
            return;
        }

        if (globales.sesionEntity == null) {
            mostrarMensaje("Aviso", "Sesión finalizada. Favor de autenticarse otra vez.");
            return;
        }

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

        globales.setUsuario(globales.sesionEntity.NumCPL);

// CE, 11/12/23, Vamos a quitar esta linea porque esta marcando error
//        setStringValue("ultimousuario", globales.sesionEntity.Usuario);

        Intent intent = new Intent(this, Main.class);
        intent.putExtra("rol", ii_perfil);
        intent.putExtra("esSuperUsuario", esSuperUsuario);
        intent.putExtra("nombre", is_nombre_Lect);
        startActivityForResult(intent, MAIN);
    }

    public void setStringValue(String key, String value) {
        // openDatabase();
        db.execSQL("Update config set value='" + value + "' where key='" + key + "'");
        // closeDatabase();
    }

    public String getStringValue(String key) {
        String value = "";
        openDatabase();
        Cursor c = db.rawQuery("Select * from config where key='" + key + "'",
                null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            value = c.getString(c.getColumnIndex("value"));
        } else {
            db.execSQL("Insert into config (key, value) values ('" + key + "', '" + value + "')");
        }
        c.close();
        closeDatabase();
        return value;
    }
}
