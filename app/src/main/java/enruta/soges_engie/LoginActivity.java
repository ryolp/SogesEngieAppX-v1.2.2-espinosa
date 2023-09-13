package enruta.soges_engie;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import enruta.soges_engie.clases.AutenticadorMgr;
import enruta.soges_engie.clases.Utils;
import enruta.soges_engie.entities.LoginRequestEntity;
import enruta.soges_engie.entities.LoginResponseEntity;
import enruta.soges_engie.entities.SesionEntity;

/* ====================================================================================
    LoginActivity:

    Este activity tiene la función de realizar la autenticación del usuario,
    ... incluyendo el dobloe factor de autenticación con SMS.
==================================================================================== */

public class LoginActivity extends Activity {

    // Constantes

    public final static int ADMINISTRADOR = 1;
    public final static int LECTURISTA = 2;
    public final static int SUPERUSUARIO = 3;

    // Variables

    private Globales mGlobales;

    private TextView tv_msj_login, tv_usuario, tv_contrasena, tv_version;
    private EditText et_usuario, et_contrasena;

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    // RL, 2022-07-14, Campos para validación SMS

    private TextView lblMensaje;
    private TextView lblCodigoSMS;
    private EditText txtCodigoSMS;
    private Button btnEntrar;
    private Button btnAutenticar;
    private Button btnValidarSMS;
    private int mIntentosAutenticacion = 0;
    private int mIntentosCodigoSMS = 0;

    // RL, 2023-07-10, Migración del Cortrex

    private DialogoMensaje mDialogoMsg = null;
    private AutenticadorMgr mAutenticadorMgr = null;

    private int mOpcionLogin = 0;

    /* ====================================================================================
        Creación del activity
    ==================================================================================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mGlobales = ((Globales) getApplicationContext());

        //obtenerParametros();
        //inicializarControles();
    }

    /* ====================================================================================
        Obtener los parámetros que recibe el Activity
    ==================================================================================== */

    private void obtenerParametros() {
        Bundle bu_params = getIntent().getExtras();
        mOpcionLogin = bu_params.getInt("opcionLogin", 0);
    }


    /* ====================================================================================
        Inicializar de los controles
    ==================================================================================== */

    private void inicializarControles() {
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

        tv_usuario.setVisibility(View.VISIBLE);
        et_usuario.setVisibility(View.VISIBLE);
        et_usuario.setInputType(InputType.TYPE_CLASS_TEXT);
        et_usuario.setFilters(new InputFilter[]{});
        btnAutenticar.setVisibility(View.VISIBLE);

        tv_contrasena.setVisibility(View.VISIBLE);
        et_contrasena.setVisibility(View.VISIBLE);
        et_contrasena.setInputType(InputType.TYPE_CLASS_TEXT);
        et_contrasena.setFilters(new InputFilter[]{});

        lblCodigoSMS.setVisibility(View.GONE);
        txtCodigoSMS.setVisibility(View.GONE);
        btnValidarSMS.setVisibility(View.GONE);
        btnEntrar.setVisibility(View.GONE);
        btnEntrar.setEnabled(false);

        et_usuario.setFocusableInTouchMode(true);
        et_usuario.setFocusable(true);
        et_usuario.requestFocus();

        inicializarEventosControles();
    }

    /* ====================================================================================
        Inicializar los eventos de los controles
    ==================================================================================== */

    private void inicializarEventosControles() {
        btnAutenticar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autenticar(view);
            }
        });

        btnValidarSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarSMS(view);
            }
        });

        et_usuario.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String valor;

                valor = et_usuario.getText().toString().trim();

                if (valor.equals(""))
                    showMessageShort("Falta capturar el usuario");
                else if (esSuperUsuario())
                    autenticar(btnAutenticar);
                else
                    et_contrasena.requestFocus();
                return false;
            }
        });

        et_contrasena.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String password;
                String secuenciaSuperUsuario;

                password = getPassword();
                secuenciaSuperUsuario = getSecuenciaSuperUsuario();

                if (!secuenciaSuperUsuario.equals("A"))
                {
                    if (password.equals(""))
                        showMessageShort("Falta capturar la contraseña");
                    else
                        autenticar(btnValidarSMS);
                }
                else
                    regresar(SUPERUSUARIO);

                return false;
            }
        });

        txtCodigoSMS.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String valor;

                valor = txtCodigoSMS.getText().toString().trim();

                if (valor.equals(""))
                    showMessageShort("Falta capturar el código SMS");
                else
                    validarSMS(txtCodigoSMS);
                return false;
            }
        });
    }

    /* ====================================================================================
        Inicializar los eventos de los controles
    ==================================================================================== */

    private String getSecuenciaSuperUsuario() {
        if (mGlobales == null)
            return "";

        if (mGlobales.secuenciaSuperUsuario == null)
            return "";

        return mGlobales.secuenciaSuperUsuario;
    }

    /* ====================================================================================
        REgresar si el usuario es SUPER USUARIO
    ==================================================================================== */

    private boolean esSuperUsuario() {
        String usuario = getUsuario();
        String password = getPassword();
        String secuencia = getSecuenciaSuperUsuario();

        if (mOpcionLogin != ADMINISTRADOR)
            return false;

        if (usuario.contains("*9776"))
            return true;
        else if (secuencia.equals("A") && password.contains("9776"))
            return true;
        else
            return false;
    }

    /* ====================================================================================
        Regresar el string del campo de usuario
    ==================================================================================== */

    private String getUsuario() {
        if (et_usuario != null)
            return et_usuario.getText().toString().trim();
        else
            return "";
    }

    /* ====================================================================================
        Regresar el string del campo de password
    ==================================================================================== */

    private String getPassword() {
        if (et_contrasena != null)
            return et_contrasena.getText().toString().trim();
        else
            return "";
    }

    /* ====================================================================================
        Inicializar los eventos de los controles
    ==================================================================================== */

    private void inicializarEventosControlesLogin() {
        btnAutenticar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autenticar(view);
            }
        });

        btnValidarSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarSMS(view);
            }
        });

        et_usuario.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String valor;

                valor = et_usuario.getText().toString().trim();

                if (valor.equals(""))
                    showMessageShort("Falta capturar el usuario");
                else if (esSuperUsuario())
                    autenticar(btnAutenticar);
                else
                    et_contrasena.requestFocus();
                return false;
            }
        });

        et_contrasena.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String password;
                String secuenciaSuperUsuario;

                password = getPassword();
                secuenciaSuperUsuario = getSecuenciaSuperUsuario();

                if (!secuenciaSuperUsuario.equals("A")) {
                    if (password.equals(""))
                        showMessageShort("Falta capturar la contraseña");
                    else
                        autenticar(btnValidarSMS);
                } else
                    regresar(ADMINISTRADOR);

                return false;
            }
        });

        txtCodigoSMS.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String valor;

                valor = txtCodigoSMS.getText().toString().trim();

                if (valor.equals(""))
                    showMessageShort("Falta capturar el código SMS");
                else
                    validarSMS(txtCodigoSMS);
                return false;
            }
        });
    }

    /* ====================================================================================
        Inicializar las APIs de conectividad para el Login y validación del SMS
    ==================================================================================== */

    private void inicializarAutenticador() {
        if (mAutenticadorMgr == null) {
            mAutenticadorMgr = new AutenticadorMgr(this);

            mAutenticadorMgr.setAutenticadorCallback(new AutenticadorMgr.AutenticadorCallback() {
                @Override
                public void enExitoAutenticacion(LoginRequestEntity request, LoginResponseEntity resp) {
                    exitoAutenticacion(resp);
                }

                @Override
                public void enFalloAutenticacion(LoginRequestEntity request, LoginResponseEntity resp) {
                    falloAutenticacion(request, resp);
                }

                @Override
                public void enErrorAutenticacion(LoginRequestEntity request, LoginResponseEntity resp) {
                    errorAutenticacion(request, resp);
                }

                @Override
                public void enExitoValidarSMS(LoginRequestEntity request, LoginResponseEntity resp) {
                    exitoValidacionSMS(resp);
                }

                @Override
                public void enFalloValidarSMS(LoginRequestEntity request, LoginResponseEntity resp) {
                    falloValidacionSMS(request, resp);
                }

                @Override
                public void enErrorValidarSMS(LoginRequestEntity request, LoginResponseEntity resp) {
                    errorValidacionSMS(request, resp);
                }
            });
        }
    }

    /* ====================================================================================
        Realizar el proceso de autenticación
        RLR / 2022-08-25
    ==================================================================================== */

    private void autenticar(View view) {
        String usuario = "";
        String password = "";

        usuario = "";
        password = "";
        try {
            usuario = getUsuario();
            password = getPassword();

            if (esSuperUsuario()) {
                mGlobales.esSuperUsuario = true;
                regresar(SUPERUSUARIO);
                return;
            } else if (usuario.equals("") || password.equals("")) {
                mostrarMensaje("Alerta", "Falta capturar el usuario o la contraseña", "", null);
                return;
            }

            mGlobales.esSuperUsuario = false;

            inicializarAutenticador();

            showMessageShort("Autenticando");

            if (mAutenticadorMgr != null)
                mAutenticadorMgr.autenticar(usuario, password);
            else
                errorAutenticacion("Error inesperado");
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "No hay conexión a internet. Intente nuevamente. (3)", t.getMessage(), null);
            Log.d("CPL", "No hay conexión a internet. Intente nuevamente. (3):" + t.getMessage());
        }
    }

    /* ====================================================================================

        RLR / 2023-09-13
    ==================================================================================== */

    private void exitoAutenticacion(LoginResponseEntity loginResponseEntity) {
        mIntentosCodigoSMS = 0;

        if (loginResponseEntity.Exito) {
            mGlobales.sesionEntity = new SesionEntity(loginResponseEntity);

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
                mGlobales.sesionEntity.Autenticado = true;
                mGlobales.secuenciaSuperUsuario = "";
                regresar(mOpcionLogin);
            }
        }
    }

    /* ====================================================================================

        RLR / 2023-09-13
    ==================================================================================== */

    private void falloAutenticacion(LoginRequestEntity req, LoginResponseEntity resp) {
        String msg;

        mGlobales.sesionEntity = null;
        mIntentosAutenticacion++;

        if (mIntentosAutenticacion >= mGlobales.maxIntentosAutenticacion) {
            msg = resp.Mensaje + ". Máximo de intentos.";

            mostrarMensaje("Alerta", msg, "", new DialogoMensaje.Resultado() {
                @Override
                public void Aceptar(boolean EsOk) {
                    mGlobales.sesionEntity = null;
                    cancelar();
                }
            });
        } else {
            msg = resp.Mensaje + ". Intento " + Utils.convToStr(mIntentosAutenticacion)
                    + " de " + Utils.convToStr(mGlobales.maxIntentosAutenticacion);
            mostrarMensaje("Alerta", msg);
        }
    }

    /* ====================================================================================

    ==================================================================================== */

    private void errorAutenticacion(String mensaje) {
        mostrarMensaje("Alerta", mensaje, "", new DialogoMensaje.Resultado() {
            @Override
            public void Aceptar(boolean EsOk) {
                cancelar();
            }
        });
    }

    /* ====================================================================================

    ==================================================================================== */

    private void errorAutenticacion(LoginRequestEntity req, LoginResponseEntity resp) {
        mostrarMensaje("Alerta", resp.Mensaje, resp.MensajeError, new DialogoMensaje.Resultado() {
            @Override
            public void Aceptar(boolean EsOk) {
                cancelar();
            }
        });
    }

    /* ====================================================================================

    ==================================================================================== */

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

            inicializarAutenticador();

            if (mAutenticadorMgr != null)
                mAutenticadorMgr.validarSMS(usuario, codigoSMS);
            else
                errorValidacionSMS("Error inesperado");
        } catch (Exception ex) {
            mGlobales.sesionEntity = null;
            errorValidacionSMS("No hay conexión a internet. Intente nuevamente. (3).");
            Log.d("CPL", "No hay conexión a internet. Intente nuevamente. (3) : " + ex.getMessage());
        }
    }

    /* ====================================================================================

    ==================================================================================== */

    private void falloValidacionSMS(LoginRequestEntity req, LoginResponseEntity resp) {
        String msg;

        mIntentosCodigoSMS++;

        if (mIntentosCodigoSMS >= 3) {
            msg = "Se alcanzó el máximo de intentos de valicación del código SMS. Usuario bloqueado.";

            mostrarMensaje("Alerta", msg, "", new DialogoMensaje.Resultado() {
                @Override
                public void Aceptar(boolean EsOk) {
                    mGlobales.sesionEntity = null;
                    cancelar();
                }
            });
        } else {
            msg = resp.Mensaje + ". Intento " + Utils.convToStr(mIntentosCodigoSMS)
                    + " de " + Utils.convToStr(mGlobales.maxIntentosAutenticacion);
            mostrarMensaje("Alerta", msg);
        }
    }

    /* ====================================================================================

    ==================================================================================== */

    private void errorValidacionSMS(String mensaje) {
        mostrarMensaje("Alerta", mensaje, "", new DialogoMensaje.Resultado() {
            @Override
            public void Aceptar(boolean EsOk) {
                cancelar();
            }
        });
    }

    /* ====================================================================================

    ==================================================================================== */

    private void errorValidacionSMS(LoginRequestEntity req, LoginResponseEntity resp) {
        mostrarMensaje("Alerta", resp.Mensaje, resp.MensajeError, new DialogoMensaje.Resultado() {
            @Override
            public void Aceptar(boolean EsOk) {
                cancelar();
            }
        });
    }

    /* ====================================================================================

    ==================================================================================== */

    private void exitoValidacionSMS(LoginResponseEntity loginResponseEntity) {
        if (loginResponseEntity.Error) {
            mGlobales.sesionEntity = null;
            showMessageLong("No hay conexión a internet. Intente nuevamente. (4) : " + loginResponseEntity.Mensaje);
            return;
        }

        if (loginResponseEntity.Exito) {
            mGlobales.sesionEntity = new SesionEntity(loginResponseEntity);
            mGlobales.sesionEntity.Autenticado = true;
            mGlobales.secuenciaSuperUsuario = "";
            regresar(mOpcionLogin);
        }
    }

    /* ====================================================================================

    ==================================================================================== */

    @Override
    public void onDestroy() {
        super.onDestroy();
        limpiarVariables();
    }

    /* ====================================================================================

    ==================================================================================== */

    @Override
    public void onResume() {
        super.onResume();

        obtenerParametros();
        inicializarControles();
    }

    /* ====================================================================================

    ==================================================================================== */

    public void regresar(int opcionLogin) {
        Intent resultado = new Intent();

        resultado.putExtra("opcionLogin", opcionLogin);

        setResult(Activity.RESULT_OK, resultado);

        finish();
    }

    /* ====================================================================================

    ==================================================================================== */

    private void cancelar() {
        setResult(Activity.RESULT_CANCELED);
        this.finish();
    }

    /* ====================================================================================

    ==================================================================================== */

    public void onBackPressed() {
        cancelar();
    }

    /* ====================================================================================

    ==================================================================================== */

    protected void limpiarVariables() {
        if (mGlobales != null) {
            mGlobales.sesionEntity = null;
        }
    }

    /* ====================================================================================
        Diálogo para mostrar un mensaje.
        RLR / 2022-08-25
    ==================================================================================== */

    private void mostrarMensaje(String titulo, String mensaje, String detalleError, DialogoMensaje.Resultado resultado) {
        if (mDialogoMsg == null) {
            mDialogoMsg = new DialogoMensaje(this);
        }

        mDialogoMsg.setOnResultado(resultado);
        mDialogoMsg.mostrarMensaje(titulo, mensaje, detalleError);
    }

    /* ====================================================================================
        Diálogo para mostrar un mensaje.
        RLR / 2022-08-25
    ==================================================================================== */

    private void mostrarMensaje(String titulo, String mensaje) {
        if (mDialogoMsg == null) {
            mDialogoMsg = new DialogoMensaje(this);
        }

        mDialogoMsg.mostrarMensaje(titulo, mensaje, "");
    }


    /* ====================================================================================
        Muestra un mensaje tipo pop up de duración larga
        RLR / 2022-08-25
    ==================================================================================== */

    private void showMessageLong(String sMessage) {
        Toast.makeText(this, sMessage, Toast.LENGTH_LONG).show();
    }

    /* ====================================================================================
        Muestra un mensaje tipo pop up de duración corta
        RLR / 2022-08-25
    ==================================================================================== */

    private void showMessageShort(String sMessage) {
        Toast.makeText(this, sMessage, Toast.LENGTH_SHORT).show();
    }


    /* ====================================================================================
        Habilita los controles relacionados con la autenticación con SMS
        RLR / 2022-08-25
    ==================================================================================== */

    private void habilitarControlesAutenticacionSMS() {
        tv_usuario.setVisibility(View.VISIBLE);
        et_usuario.setVisibility(View.VISIBLE);
        et_usuario.setInputType(InputType.TYPE_CLASS_TEXT);
        et_usuario.setFilters(new InputFilter[]{});
        btnAutenticar.setVisibility(View.VISIBLE);

        tv_contrasena.setVisibility(View.VISIBLE);
        et_contrasena.setVisibility(View.VISIBLE);
        et_contrasena.setInputType(InputType.TYPE_CLASS_TEXT);
        et_contrasena.setFilters(new InputFilter[]{});

        lblCodigoSMS.setVisibility(View.GONE);
        txtCodigoSMS.setVisibility(View.GONE);
        btnValidarSMS.setVisibility(View.GONE);
        btnEntrar.setVisibility(View.GONE);
        btnEntrar.setEnabled(false);

        et_usuario.setFocusableInTouchMode(true);
        et_usuario.setFocusable(true);
        et_usuario.requestFocus();
    }

    /* ====================================================================================
        Deshabilita los controles relacionados con la autenticación con SMS
        RLR / 2022-08-25
    ==================================================================================== */

    private void deshabilitarControlesAutenticacionSMS() {
        lblCodigoSMS.setVisibility(View.GONE);
        txtCodigoSMS.setVisibility(View.GONE);
        btnValidarSMS.setVisibility(View.GONE);
        btnAutenticar.setVisibility(View.GONE);
    }
}