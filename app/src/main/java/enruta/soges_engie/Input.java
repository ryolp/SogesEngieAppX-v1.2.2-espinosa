package enruta.soges_engie;


import enruta.soges_engie.R;
import enruta.soges_engie.clases.Utils;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class Input extends TomaDeLecturasPadre {

    final static int LECTURA = 0;
    final static int PRESION = 1;
    final static int COMENTARIOS = 2;
    final static int MAC = 3;

    final static int NORMAL = 0;
    final static int SIN_FOTOS = 1;
    final static int FOTOS = 2;
    final static int FOTOS_CC = 3;

    final static int CAPTURA_Y_REGRESA = 0;
    final static int CAPTURA_Y_ANOMALIA = 1;
    final static int CAPTURA_Y_SIGUIENTE = 2;

    int tipoDeCaptura = CAPTURA_Y_REGRESA;

    final static int PANTALLA_CODIGOS = 4;
    final static int PANTALLA_COMENTARIOS = 5;

    final static String HEXATECLADO = "procentaje_hexateclado";
    final static String TECLADO = "porcentaje_teclado";
    final static String NINGUNO = "NINGUNO";

    int ii_tipo;
    EditText et_generico;
    Button b_aceptar;
    View i_teclado, i_hexateclado;
    TextView tv_label, tv_info, tv_medidor;
    RelativeLayout rl_contenedorLabel;

//	String is_lectAnt = "";

    long il_lect_act = 0, il_total, il_lect_max, il_lect_min;
    int il_lectConf = 0;

    String ls_lectura, ls_comentarios;
    boolean mostrado = false;
    LinearLayout tl_teclado;

    boolean sospechosa = false;
    boolean requiereFoto = false;
    boolean obligatorio = false;

    boolean validar = true, salir = true, esLecturaValida = true, pregunteConsumo = false;
    ;

    Lectura lectura;

    int modo = 0;

    double porcentaje = 1.0;
    double factorPorcentaje = 0.05;

    float[] sizeTeclasTeclado = new float[21];
    String tecladoActual = NINGUNO;

    Button botones[] = null;

    Sonidos sonidos;

    boolean capturando = false;
    String behavior = "";

    private DialogoMensaje mDialogoMsg = null;      // RL, 2023-09-16, Dialogo para mostrar mensajes al usuario

    /* ====================================================================================
        Creación del activity
    ==================================================================================== */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layoutgenerico);
        setTitle("");

        try {
            is_mensaje_direccion = getString(R.string.msj_lecturas_no_hay_mas);
            globales = ((Globales) getApplicationContext());

            Bundle bu_params = getIntent().getExtras();

            sonidos = new Sonidos(this);

            et_generico = (EditText) findViewById(R.id.et_generico);
            tv_label = (TextView) findViewById(R.id.tv_label);
            i_teclado = findViewById(R.id.i_teclado);
            ii_tipo = bu_params.getInt("tipo");
            tv_info = (TextView) findViewById(R.id.tv_info);
            tl_teclado = (LinearLayout) findViewById(R.id.tl_teclado);
            i_hexateclado = (View) findViewById(R.id.i_hexateclado);
            b_aceptar = (Button) findViewById(R.id.b_continuar);
            tv_medidor = (TextView) findViewById(R.id.tv_medidor);
            rl_contenedorLabel = (RelativeLayout) findViewById(R.id.ll_contendedorLabel);

//		final TextView tv = (TextView) findViewById(R.id.tv_label);

            switch (ii_tipo) {
                case LECTURA:

                    getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    ls_lectura = bu_params.getString("act");
                    il_lect_max = bu_params.getLong("max");
                    il_lect_min = bu_params.getLong("min");
                    validar = bu_params.getBoolean("validar");
                    modo = bu_params.getInt("modo");

                    tecladoActual = TECLADO;

                    porcentaje = globales.porcentaje_teclado;
                    et_generico.setKeyListener(null);

                    ViewTreeObserver vto = tv_label.getViewTreeObserver();
                    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

                        @Override
                        public void onGlobalLayout() {
                            try {
                                inicializarBototesTeclado();
                                shrinkTeclado();
                                setPorcentaje();
                                ViewTreeObserver obs = tv_label.getViewTreeObserver();

                                // if (Build.VERSION.SDK_INT >=
                                // Build.VERSION_CODES.JELLY_BEAN) {
                                // obs.removeOnGlobalLayoutListener(this);
                                // } else {

                                obs.removeGlobalOnLayoutListener(this);
                                // }
                            } catch (Throwable t) {
                                mostrarMensaje("Alerta", "Ocurrió un problema inesperado", t);
                            }
                        }

                    });

                    setDatosInicial();

                    et_generico
                            .setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if (!hasFocus) {
                                        esconderTeclado();
                                    }
                                }
                            });

                    et_generico.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            esconderTeclado();

                        }

                    });

                    esconderTeclado();

                    if (globales.habilitarPuntoDecimal) {
                        Button punto = (Button) findViewById(R.id.teclado_b_borrar);
                        punto.setTag(".");
                        punto.setText(".");
                    }

//			 mensajeOK( "Favor de ingresar la lectura del medidor", "Teclado");

                    break;
                case PRESION:
                case COMENTARIOS:
                    String ls_label;
                    // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    try {
                        // No me importa mucho si se pone la etiqueta o no asi que
                        // ponemos un try... catch si hay algun error
                        ls_label = bu_params.getString("label");
                        if (ls_label.length() > 0) {
                            tv_label.setText(ls_label);
                            tv_label.setVisibility(View.VISIBLE);
                        }
                    } catch (Throwable e) {

                    }
                    ls_comentarios = bu_params.getString("comentarios");
                    et_generico.setText(ls_comentarios);
                    i_teclado.setVisibility(View.GONE);
                    b_aceptar.setVisibility(View.VISIBLE);
                    et_generico
                            .setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if (!hasFocus) {
                                        mostrarTeclado();
                                    }
                                }
                            });

                    et_generico.setOnEditorActionListener(new OnEditorActionListener() {

                        @Override
                        public boolean onEditorAction(TextView arg0, int arg1,
                                                      KeyEvent arg2) {
                            // TODO Auto-generated method stub
                            captura();
                            return false;
                        }
                    });

                    et_generico.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                    try {
                        behavior = bu_params.getString("behavior");
                        ComentariosInputBehavior cib = globales.tdlg.getAvisoMensajeInput(behavior);
                        et_generico.setInputType(cib.tipo);
                        et_generico.setText(cib.texto);
                        obligatorio = cib.obligatorio;
                        et_generico.setFilters(new InputFilter[]{new InputFilter.LengthFilter(cib.longitud)});
                        tv_label.setText(tv_label.getText().toString() + "\n\n" + cib.mensaje);

                    } catch (Throwable e) {
                        et_generico.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                    }

                    b_aceptar.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            captura();
                        }

                    });

                    mostrarTeclado();
                    break;

                case MAC:
                    tecladoActual = HEXATECLADO;
                    porcentaje = globales.porcentaje_hexateclado;
                    getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    ls_comentarios = bu_params.getString("comentarios");
                    et_generico.setText(quitaDosPuntos(ls_comentarios));

                    i_teclado.setVisibility(View.GONE);
                    i_hexateclado.setVisibility(View.VISIBLE);

                    vto = tv_label.getViewTreeObserver();
                    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

                        @Override
                        public void onGlobalLayout() {
                            try {
                                inicializarBototesTeclado();
                                shrinkTeclado();
                                setPorcentaje();
                                ViewTreeObserver obs = tv_label.getViewTreeObserver();

                                // if (Build.VERSION.SDK_INT >=
                                // Build.VERSION_CODES.JELLY_BEAN) {
                                // obs.removeOnGlobalLayoutListener(this);
                                // } else {
                                obs.removeGlobalOnLayoutListener(this);
                                // }
                            } catch (Throwable t) {
                                mostrarMensaje("Alerta", "Ocurrió un problema inesperado", t);
                            }
                        }

                    });

                    tv_info.setVisibility(View.GONE);

                    et_generico
                            .setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if (!hasFocus) {
                                        esconderTeclado();
                                    }
                                }
                            });

                    et_generico.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            esconderTeclado();

                        }

                    });

                    esconderTeclado();
                    break;
            }
            toFin();
            // mostrarTeclado();

            mHandler = new Handler();
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Ocurrió un problema inesperado", t);
        }
    }

    /*
     * public void esconderTeclado() { InputMethodManager mgr =
     * (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
     * mgr.hideSoftInputFromWindow(et_generico.getWindowToken(), 0); }
     */

    public void mostrarTeclado() {
        /*
         * InputMethodManager imm = (InputMethodManager)
         * getSystemService(Context.INPUT_METHOD_SERVICE);
         * imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
         */
    }

    public void esconderTeclado() {

        final View activityRootView = findViewById(R.id.rl_inputLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        // r will be populated with the coordinates of your view
                        // that area still visible.
                        activityRootView.getWindowVisibleDisplayFrame(r);

                        int heightDiff = activityRootView.getRootView()
                                .getHeight() - (r.bottom - r.top);
                        if (heightDiff > 100) { // if more than 100 pixels, its
                            // probably a keyboard...
                            // Hay teclado, lo escondemos
                            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            mgr.hideSoftInputFromWindow(
                                    et_generico.getWindowToken(), 0);
                        }

                    }
                });
    }

    public void clickTeclado(View view) {
        // Esta funcion permite habilitar un teclado alternativo al de android,
        // para facilitar la toma de lecturas
        // Como parametro de entrada es una vista (el boton presionado), sin
        // embargo lo que en verdad nos interesa es la propiedad "TAG"
        // En dicha propiedad se encuentra almacenada la operacion a realizar
        // Del 0-9 Se escribiran los numeros
        // -1 Borrar
        String ls_opcion = "";
        int li_selectEnd, li_selectIni, li_opcion;
        String ls_antes = "", ls_completa = "";
        ls_opcion = (String) view.getTag();
        // li_opcion = Integer.parseInt((String) view.getTag());
        /*
         * switch (li_opcion) { case 1: ls_opcion = "1"; break; case 2:
         * ls_opcion = "2"; break; case 3: ls_opcion = "3"; break; case 4:
         * ls_opcion = "4"; break; case 5: ls_opcion = "5"; break; case 6:
         * ls_opcion = "6"; break; case 7: ls_opcion = "7"; break; case 8:
         * ls_opcion = "8"; break; case 9: ls_opcion = "9"; break; case 0:
         * ls_opcion = "0"; break;
         *
         * }
         */
        try {
            li_opcion = Integer.parseInt((String) view.getTag());
            if (li_opcion == -1) {
                ls_opcion = "";
            }
        } catch (Throwable e) {
            li_opcion = 1;
        }

        // Agregamos el texto dependiendo de donde este el puntero
        li_selectEnd = et_generico.getSelectionEnd();
        li_selectIni = et_generico.getSelectionStart();
        if (li_opcion == -1 && et_generico.getSelectionStart() > 0)
            li_selectIni--;
        ls_antes = et_generico.getText().toString().substring(0, li_selectIni);

        ls_completa = ls_antes
                + ls_opcion
                + et_generico.getText().toString()
                .substring(et_generico.getSelectionEnd());
        et_generico.setText(ls_completa);
        // devlvemos el puntero a su lugar
        if (et_generico.getText().toString().length() > 0 && li_opcion >= 0)
            li_selectEnd++;
        else if (li_selectEnd > 0 && li_opcion == -1)
            li_selectEnd--;
        Selection.setSelection((Editable) et_generico.getText(), li_selectEnd);
        et_generico.requestFocus();
    }

    public void toFin() {
        int li_selectEnd = et_generico.getText().toString().length();
        Selection.setSelection((Editable) et_generico.getText(), li_selectEnd);

    }

//	private int validaLectura(String ls_lectAct) {
//
//		if (ls_lectAct.length() > lectura.numerodeesferas) {
//			Toast.makeText(this, R.string.msj_validacion_esferas,
//					Toast.LENGTH_SHORT).show();
//			sonidos.playSoundMedia(Sonidos.URGENT);
//			return TomaDeLecturas.VERIFIQUE;
//		}
//
//		if (ls_lectAct.equals("")) {
//			Toast.makeText(this,R.string.msj_validacion_no_hay_lectura,
//					Toast.LENGTH_SHORT).show();
//			sonidos.playSoundMedia(Sonidos.URGENT);
//			return TomaDeLecturas.VERIFIQUE;
//		}
//
//		long ll_lectAct = Long.parseLong(ls_lectAct);
//
//		if (is_lectAnt.equals("")) {
//			if (il_lect_max < ll_lectAct || il_lect_min > ll_lectAct
//					|| lectura.confirmarLectura()) {
//				is_lectAnt = ls_lectAct;
//				boolean seEquivoco = false;
//
//				if (il_lect_max < ll_lectAct || il_lect_min > ll_lectAct) {
//					seEquivoco = true;
//				}
//
//				if (lectura.is_supervisionLectura.equals("1")) {
//					if (seEquivoco)
//						globales.is_terminacion = "Y1";
//					else
//						globales.is_terminacion = "-S";
//				}
//
//				if (lectura.is_reclamacionLectura.equals("1")) {
//					if (seEquivoco)
//						globales.is_terminacion = "X1";
//					else
//						globales.is_terminacion = "-R";
//				}
//
//				sonidos.playSoundMedia(Sonidos.URGENT);
//
//				return TomaDeLecturas.FUERA_DE_RANGO;
//			}
//		} else {
//
//			if (!is_lectAnt.equals(ls_lectAct)) {
//				is_lectAnt = ls_lectAct;
//				sonidos.playSoundMedia(Sonidos.URGENT);
//				return TomaDeLecturas.VERIFIQUE;
//			}
//
//		}
//
//		if (is_lectAnt.equals("")) {
//			sonidos.playSoundMedia(Sonidos.BEEP);
//		}
//		is_lectAnt = "";
//		return TomaDeLecturas.CORRECTA;
//	}

    public void capturaLectura(View view) {
        try {
            switch (ii_tipo) {
                case LECTURA:
                    tipoDeCaptura = CAPTURA_Y_REGRESA;
                    esLecturaValida = false;
                    if (!pregunteConsumo) {
                        if (validar /*|| modoDeCaptura()*/) {
                            String ls_respuesta = globales.tdlg.validaLectura(et_generico.getText().toString());

                            if (!ls_respuesta.equals("")) {
                                int li_sospecha = Integer.parseInt(ls_respuesta.substring(0, ls_respuesta.indexOf("|")));
                                ls_respuesta = ls_respuesta.substring(ls_respuesta.indexOf("|") + 1);
                                et_generico.setText("");
                                Toast.makeText(this, ls_respuesta,
                                        Toast.LENGTH_SHORT).show();
                                if (li_sospecha == TomaDeLecturasGenerica.SOSPECHOSA) {
                                    sospechosa = true;
                                    il_lectConf++;
                                }

                                if (globales.sonidos)
                                    sonidos.playSoundMedia(globales.sonidoIncorrecta);
                                return;
                            }

                        }

                        esLecturaValida = true;
//				globales.is_lectura = et_generico.getText().toString();
                        MensajeEspecial me = globales.tdlg.mensajeDeConsumo(et_generico.getText().toString());
                        if (me != null) {
                            muestraRespuestaSeleccionada(me);
                            esLecturaValida = false;
                            return;
                        }

                        if (globales.sonidos) {
                            if (!sospechosa) {
                                sonidos.playSoundMedia(globales.sonidoCorrecta);
                            } else {
                                sonidos.playSoundMedia(globales.sonidoConfirmada);
                            }
                        }
                    } else {
                        esLecturaValida = true;
                    }
                    sospechosa = true;
//			if (salir)
//				captura();

                    Intent intent = new Intent(this, Input.class);
                    intent.putExtra("tipo", Input.COMENTARIOS);
                    intent.putExtra("comentarios", "");
                    String is_desc = "";

// CE, 10/10/23, Ya nos vamos a buscar la descricpion de ningun codigo
//                    openDatabase();
//                    Cursor c = db.rawQuery("Select desc from codigosEjecucion where anomalia='" + this.et_generico.getText().toString() + "'", null);
//                    c.moveToFirst();
//                    is_desc = Utils.getString(c, "desc", "");
//                    closeDatabase();

//********************************************************************
// CE, 01/10/23, Vamos a mostrar textos diferentes dependiendo de la Operacion
                    String is_NuevoMensajePorMostrar = "";
                    if (globales.tll.getLecturaActual().getTipoDeOrden().equals("DESCONEXION"))
                        is_NuevoMensajePorMostrar = "ESCRIBA SUS OBSERVACIONES";
                    else if (globales.tll.getLecturaActual().getTipoDeOrden().equals("REMOCION"))
                        is_NuevoMensajePorMostrar = "ESCRIBA SUS OBSERVACIONES";
                    else if (globales.tll.getLecturaActual().getTipoDeOrden().equals("RECONEXION"))
                        is_NuevoMensajePorMostrar = "ESCRIBA EL NOMBRE DEL CLIENTE";
                    else if (globales.tll.getLecturaActual().getTipoDeOrden().equals("REC/REMO"))
                        is_NuevoMensajePorMostrar = "ESCRIBA EL NUMERO DE MEDIDOR INSTALADO";
                    else
                        is_NuevoMensajePorMostrar = "ESCRIBA SUS OBSERVACIONES";
//                        is_NuevoMensajePorMostrar = this.et_generico.getText().toString() + " - " + is_desc + "";
//********************************************************************

                    // Con esto generamos la etiqueta que tendra el input
                    intent.putExtra("label",
                            ""
                                    + is_NuevoMensajePorMostrar
                                    + "");

//			String codigoAnomalia="";
//			if (globales.convertirAnomalias)
//				codigoAnomalia=pa_papa.is_anomalia.is_conv;
//			else
//				codigoAnomalia=anom.is_anomalia;

                    //Aqui mandamos el comportamiento de input, en otras palabras, le daremos la anomalia para que pueda configurarlo como se le de la gana
//			intent.putExtra("behavior", pa_papa.is_anomaliaSelec);
                    // Tambien debo mandar que etiqueta quiero tener
                    startActivityForResult(intent, PANTALLA_COMENTARIOS);


                    break;

                case MAC:

                    if (et_generico.getText().toString().length() != 12) {
                        Toast.makeText(
                                this,
                                R.string.msj_validacion_long_mac,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // captura();
                    Intent resultado = new Intent();
                    resultado.putExtra("input", agregaDosPuntos());
                    setResult(Activity.RESULT_OK, resultado);
                    finish();
                    break;
            }
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Ocurrió un problema inesperado.", t);
        }
    }

    public void captura() {

        capturando = true;
        String input = et_generico.getText().toString();
        Intent resultado = new Intent();
        resultado.putExtra("input", input);
        if (behavior != null) {
            if (!behavior.equals("")) {
                if (obligatorio && input.equals("")) {
                    capturando = false;
                    Toast.makeText(this, R.string.msj_campo_vacio_no_persnalizable, Toast.LENGTH_LONG).show();
                    return;

                }

                String ls_mensaje = globales.tdlg.validaCamposGenericos(behavior, resultado.getExtras());
                if (!ls_mensaje.equals("")) {
                    Toast.makeText(this, ls_mensaje, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        resultado.putExtra("tipo", ii_tipo);
        resultado.putExtra("sospechosa", sospechosa);
        resultado.putExtra("confirmada", il_lectConf);
        resultado.putExtra("terminacion", globales.is_terminacion);
        setResult(Activity.RESULT_OK, resultado);
        finish();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (tecladoActual.equals(NINGUNO)) {
                    return super.dispatchKeyEvent(event);
                }
                if (action == KeyEvent.ACTION_UP) {
                    porcentaje += factorPorcentaje;

                    // porcentaje= getFloatValue("porcentaje", porcentaje);

                    setSizes();
                    openDatabase();
                    // Hay que utilizar el teclado utilizado
                    guardaValor(tecladoActual, porcentaje);

                    closeDatabase();
                }

                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (tecladoActual.equals(NINGUNO)) {
                    return super.dispatchKeyEvent(event);
                }
                if (action == KeyEvent.ACTION_DOWN) {
                    // TODO

                    if ((porcentaje - factorPorcentaje) >= .05f) {
                        porcentaje -= factorPorcentaje;
                        // porcentaje= getFloatValue("porcentaje", porcentaje);
                    }

                    setSizes();

                    openDatabase();
                    guardaValor(tecladoActual, porcentaje);
                    closeDatabase();
                }
                return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.m_borrar_lect:
                et_generico.setText("");
                captura();
                break;
        }

        return true;

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // Solo si es una lectura
        if (ii_tipo == LECTURA)
            getMenuInflater().inflate(R.menu.m_input, menu);
        if (globales.habilitarPuntoDecimal) {
// CE, 01/10/2023
//            MenuItem mi_backspace = menu.findItem(R.id.m_backspace);
//            mi_backspace.setVisible(true);
        }
        return true;
    }

    public boolean modoDeCaptura() {
        // Vamos a hacer caso a lo siguiente
        // 0 Normal
        // 1 Sin Fotos
        // 2 Fotos
        // 3 Fotos control de calidad

        // if (modo==NORMAL || modo==SIN_FOTOS || modo==FOTOS)
        // return false;
        //
//		if ((modo == FOTOS_CC || modo == NORMAL || modo == FOTOS)
//				&& lectura.confirmarLectura()) {
//			return true;
//		}
//		return false;

        return false;

    }

    @SuppressLint("NewApi")
    public void shrinkTeclado() {
        int displayHeight, tecladoHeight, infoHeight, genericoHeight;
        long totalHeight = 0;
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Point size = new Point();
            display.getSize(size);
            displayHeight = size.y;
        } else {
            displayHeight = display.getHeight();
        }

        tecladoHeight = i_teclado.getHeight();
        genericoHeight = et_generico.getHeight();
        infoHeight = tv_info.getHeight();

        totalHeight = tecladoHeight + infoHeight + genericoHeight;
        if (totalHeight > displayHeight) {// while(totalHeight>displayHeight){
            shrinkBotones();
            tecladoHeight = i_teclado.getHeight();
            totalHeight = tecladoHeight + infoHeight + genericoHeight;
        }

    }

    public void shrinkBotones() {
        Button botones[] = {(Button) findViewById(R.id.teclado_b_1),
                (Button) findViewById(R.id.teclado_b_2),
                (Button) findViewById(R.id.teclado_b_3),
                (Button) findViewById(R.id.teclado_b_4),
                (Button) findViewById(R.id.teclado_b_5),
                (Button) findViewById(R.id.teclado_b_6),
                (Button) findViewById(R.id.teclado_b_7),
                (Button) findViewById(R.id.teclado_b_8),
                (Button) findViewById(R.id.teclado_b_9),
                (Button) findViewById(R.id.teclado_b_0),
                (Button) findViewById(R.id.teclado_b_A),
                (Button) findViewById(R.id.teclado_b_B),
                (Button) findViewById(R.id.teclado_b_C),
                (Button) findViewById(R.id.teclado_b_D),
                (Button) findViewById(R.id.teclado_b_E),
                (Button) findViewById(R.id.teclado_b_F),
                (Button) findViewById(R.id.teclado_b_borrar),
                (Button) findViewById(R.id.pcl_b_capturar),
                (Button) findViewById(R.id.teclado_capturayanomalia),
                (Button) findViewById(R.id.teclado_capturaysalir),
                (Button) findViewById(R.id.teclado_capturaysiguiente),
                (Button) findViewById(R.id.teclado_b_punto)};

        for (Button boton : botones) {
            if (boton != null) {
                boton.setTextSize((float) ((float) boton.getTextSize() - 0.5));
                boton.append("\u200b");
            }

        }
    }

    public String agregaDosPuntos() {
        String ls_mac = et_generico.getText().toString();
        String ls_final = "";

        for (int i = 0; i < ls_mac.length(); i += 2) {
            try {
                ls_final += ls_mac.substring(i, i + 2) + ":";
            } catch (Throwable e) {
            }

        }

        if (ls_final.endsWith(":"))
            ls_final = ls_final.substring(0, ls_final.length() - 1);

        return ls_final;
    }

    public String quitaDosPuntos(String ls_mac) {
        String ls_final = "";

        for (int i = 0; i < ls_mac.length(); i++) {
            if (!ls_mac.substring(i, i + 1).equals(":")) {
                ls_final += ls_mac.substring(i, i + 1);
            }

        }
        return ls_final;

    }

    public void setSizes() {

        int i = 0;

        for (Button boton : botones) {
            if (boton != null) {
                boton.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                        (float) (porcentaje * sizeTeclasTeclado[i]));
                //boton.append("\u200b");
            }
            i++;

        }
    }

    public void setPorcentaje() throws Exception {
        // openDatabase();
        // db.execSQL("delete from config where key='porcentaje' ");
        // closeDatabase();

        porcentaje = getDoubleValue(tecladoActual, porcentaje);

        int i = 0;

        for (Button boton : botones) {
            if (boton != null) {
                sizeTeclasTeclado[i] = boton.getTextSize();
            }
            i++;

        }

        setSizes();
    }

    public int getIntValue(String key, int value) throws Exception {
        openDatabase();

        Cursor c = db.rawQuery("Select * from config where key='" + key + "'",
                null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            value = Utils.getInt(c, "value", 0);
        } else {
            db.execSQL("Insert into config (key, value) values ('" + key
                    + "', " + value + ")");
        }
        c.close();

        closeDatabase();

        return value;
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

    public void guardaValor(String key, double value) {
        // openDatabase();
        db.execSQL("Update config  set value=" + value + " where  key='" + key
                + "'");

        // closeDatabase();
    }

    public void inicializarBototesTeclado() {

        if (tecladoActual.equals(TECLADO)) {
            Button botones[] = {(Button) findViewById(R.id.teclado_b_1),
                    (Button) findViewById(R.id.teclado_b_2),
                    (Button) findViewById(R.id.teclado_b_3),
                    (Button) findViewById(R.id.teclado_b_4),
                    (Button) findViewById(R.id.teclado_b_5),
                    (Button) findViewById(R.id.teclado_b_6),
                    (Button) findViewById(R.id.teclado_b_7),
                    (Button) findViewById(R.id.teclado_b_8),
                    (Button) findViewById(R.id.teclado_b_9),
                    (Button) findViewById(R.id.teclado_b_0),
                    (Button) findViewById(R.id.teclado_b_A),
                    (Button) findViewById(R.id.teclado_b_B),
                    (Button) findViewById(R.id.teclado_b_C),
                    (Button) findViewById(R.id.teclado_b_D),
                    (Button) findViewById(R.id.teclado_b_E),
                    (Button) findViewById(R.id.teclado_b_F),
                    (Button) findViewById(R.id.teclado_b_borrar),
                    (Button) findViewById(R.id.teclado_capturayanomalia),
                    (Button) findViewById(R.id.teclado_capturaysalir),
                    (Button) findViewById(R.id.teclado_capturaysiguiente),
                    (Button) findViewById(R.id.teclado_b_punto)
            };
            this.botones = botones;
        } else if (tecladoActual.equals(HEXATECLADO)) {
            Button botones[] = {
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_1),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_2),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_3),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_4),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_5),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_6),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_7),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_8),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_9),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_0),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_A),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_B),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_C),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_D),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_E),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_F),
                    (Button) i_hexateclado.findViewById(R.id.teclado_b_borrar),
                    (Button) i_hexateclado.findViewById(R.id.pcl_b_capturar)};
            this.botones = botones;

        }

    }

    @Override
    public void finish() {
        // Hago esto porque no funciono el backpressed
        if (!capturando && ii_tipo == LECTURA) {
            Intent resultado = new Intent();
            resultado.putExtra("sospechosa", sospechosa);
            resultado.putExtra("confirmada", il_lectConf);
            setResult(Activity.RESULT_CANCELED, resultado);
        }

        super.finish();
        return;
    }

    // public void onBackPressed() {
    // Intent resultado =new Intent();
    // resultado.putExtra("sospechosa", sospechosa);
    // resultado.putExtra("confirmada", il_lectConf);
    // setResult(Activity.RESULT_CANCELED, resultado);
    // finish();
    // return;
    // }

    public void capturarYSiguiente(View view) {

        if (globales.is_caseta.contains("CF") && globales.is_lectura.equals("")
                && globales.is_presion.equals("") && !globales.bModificar) {

            switch (globales.ii_orden) {
                case ASC:
                    getSigLect();
                    break;
                case DESC:
                    globales.tll.guardarLectura("0");
                    getAntLect();

                    break;
            }
        } else {

            capturaLectura();
            tipoDeCaptura = CAPTURA_Y_SIGUIENTE;
            if (!esLecturaValida)
                return;


            if ((sospechosa || globales.fotoForzada || globales.bModificar))
                tomarFoto(CamaraActivity.TEMPORAL, 1);
            else
                guardar();
        }


    }

    public void capturaLectura() {
        salir = (pregunteConsumo && tipoDeCaptura == CAPTURA_Y_REGRESA); //La igualamos, porque si pregunto el consumo, no hay manera de saber si debe salir o no.
        capturaLectura(tv_label);
        salir = true;
    }

    public void guardar() {

        capturando = true;
        // int respuesta= globales.tll.capturaLectura(is_lectura,
        // globales.tll.getLecturaActual().getAnomalia());
        int respuesta = 1;
        Lectura ll_lectura = globales.tll.getLecturaActual();
        globales.estoyCapturando = true;
        try {

            if (globales.is_lectura.equals("") && globales.requiereLectura) {
                Toast.makeText(this,
                        R.string.lbl_tdl_requiere_lectura,
                        Toast.LENGTH_LONG).show();
                return;
            }

            //Verificamos si la localizacion es nula...
            if (deboTomarPuntoGPS()) {
                //No podemos permitir que sea nula sin haber tenido el consentimiento del usuario, ok?...
                //Nos tendremos que salir de esta rutina e iniciar la rutina de espere gps
                esperarGPS();
                return;
            }

            seguirConLaCapturaSinPunto = false;


            // Siguiente lectura

//			if (globales.bModificar)
//				globales.tll.getLecturaActual().intentos++;

            globales.il_ultimoSegReg = il_lect_act;

            globales.tll.guardarLectura(globales.is_lectura);
            globales.tll.getLecturaActual().setPuntoGPS(globales.location);
            globales.modoCaptura = false;
            // salirModoCaptura();

            // Ahora hay que poner que ya no hay temporales
            // borramos fotos temporales anteriores
            openDatabase();

            db.execSQL("update fotos set temporal=0 where temporal="
                    + CamaraActivity.TEMPORAL + " or temporal="
                    + CamaraActivity.ANOMALIA);

            closeDatabase();

            globales.idMedidorUltimaLectura = globales.is_caseta;

            if (!globales.capsModoCorreccion
                    && !(globales.sonLecturasConsecutivas && globales.bModificar))
                globales.bModificar = false;

            globales.permiteDarVuelta = true;

            permiteCerrar();

            boolean sonLecturasConsecutivas = this.globales.sonLecturasConsecutivas;

            if (globales.sonLecturasConsecutivas)
                asignaAnomaliaConsecutiva(globales.idMedidorUltimaLectura,
                        globales.tll.getLecturaActual().getUltimaAnomalia());

            switch (globales.ii_orden) {
                case ASC:
                    getSigLect();
                    break;
                case DESC:
                    getAntLect();
                    break;
            }

            if ((globales.sonLecturasConsecutivas && !globales.idMedidorUltimaLectura
                    .equals(globales.is_caseta))
                    || (!globales.tll.hayMasLecturas() && sonLecturasConsecutivas)) {
                tomaFotosConsecutivas(globales.idMedidorUltimaLectura);

                // Salimos de correccion
                if (globales.bModificar) {
                    globales.bcerrar = false;
                    globales.bModificar = false;
                    globales.capsModoCorreccion = false;
                    // layout.setBackgroundResource(0);
                    getSigLect();
                }
            }

            if (!ll_lectura.getLectura().equals(""))
                mandarAImprimir(ll_lectura);

            globales.moverPosicion = false;

        } catch (Throwable e) {

            // En caso de que sea la ultima lectura
            if (globales.sonLecturasConsecutivas && globales.bModificar) {

                // Salimos de correccion
                if (globales.bModificar) {
                    globales.bcerrar = false;
                    globales.bModificar = false;
                    globales.capsModoCorreccion = false;
                    getSigLect();
                }
            } else {
                // Ya no hay lecturas
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                bEsElFInal = true;
                if (!ll_lectura.getLectura().equals(""))
                    mandarAImprimir(ll_lectura);
                // this.finish();
                // Si es la ultima deberia salir

                muere();
            }
        }

        pregunteConsumo = false;
        globales.estoyCapturando = false;

        // }
        // else{
        // //Mostramos porque
        // switch(respuesta){
        // case TodasLasLecturas.FUERA_DE_RANGO:
        // Toast.makeText(this, "Lectura fuera de rango, Verifique.",
        // Toast.LENGTH_SHORT).show();
        // break;
        // case TodasLasLecturas.ESFERAS_INCORRECTAS:
        // Toast.makeText(this, "No concuerda el número de esferas.",
        // Toast.LENGTH_SHORT).show();
        // break;
        // case TodasLasLecturas.INTENTOS_ACABADOS:
        // Toast.makeText(this, "Se han agotado el número de intentos.",
        // Toast.LENGTH_SHORT).show();
        // break;
        // case TodasLasLecturas.VACIA:
        // Toast.makeText(this, "La lectura no puede quedar vacia.",
        // Toast.LENGTH_SHORT).show();
        // break;
        // case TodasLasLecturas.INTENTANDO:
        // Toast.makeText(this, "Verifique la lectura.",
        // Toast.LENGTH_SHORT).show();
        // break;
        // }
        // }

        salir = true;
        capturando = false;
        globales.location = null;
        // estoyCapturando=false;

    }

    @Override
    protected void setDatos() {

        if (globales.tll.hayMasMedidoresIguales(globales.is_caseta)
                && !globales.is_caseta.trim().equals("0"))
            globales.sonLecturasConsecutivas = true;
        else
            globales.sonLecturasConsecutivas = false;

        if (globales.tll.getNumRecords() > 0) {
            if (!globales.tll.hayMasLecturas()
                    || globales.tll.getLecturaActual() == null) {
                if (!globales.bModificar) {
                    if (globales.permiteDarVuelta && !globales.bcerrar) {
                        irALaPrimeraSinEjecutarAlTerminar();
                        globales.permiteDarVuelta = false;
                        return;
                    } else {
                        if (!globales.permiteDarVuelta)
                            Toast.makeText(this, is_mensaje_direccion,
                                    Toast.LENGTH_SHORT).show();
                    }

                } else {
                    if (globales.sonLecturasConsecutivas && globales.estoyCapturando) {
                        globales.bcerrar = false;
                        globales.bModificar = false;
                        // tv_indica_corr.setText("N");
                        globales.capsModoCorreccion = false;

                        // item.setIcon(R.drawable.ic_action_correccion);
                        globales.permiteDarVuelta = false;
                        getSigLect();
                        return;
                    } else {


                        if (globales.permiteDarVuelta) {
                            irALaPrimeraSinEjecutarAlTerminar();
                            globales.permiteDarVuelta = false;
                            return;
                        } else {
                            Toast.makeText(this, is_mensaje_direccion,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    // globales.bModificar=false;
                }

                // Si estoy tomando fotos consecutivas, no puedo cerrar ya que
                // tengo una actividad hijo que depende de esta...
                if (globales.bcerrar
                        && /* !estoyTomandoFotosConsecutivas */!globales.sonLecturasConsecutivas) {
                    // finish();
                    //muere();
                    iniciarModoCorreccionCAPS();
                    globales.permiteDarVuelta = false;
                    return;
                } else if (globales.bcerrar && globales.sonLecturasConsecutivas) {
                    // Puede darse el caso de que sea la ultima lectura
                    // consecutiva, se decide cerrar, pero el algoritmo
                    // avanza... no debe cerrar
                } else
                    globales.bcerrar = true;
            }
        } else {
            Toast.makeText(this, R.string.msj_lecturas_no_hay_lecturas_cargadas, Toast.LENGTH_SHORT)
                    .show();
            // finish();
            muere();
        }
        is_mensaje_direccion = getString(R.string.msj_lecturas_no_hay_mas);
        // TODO Auto-generated method stub
        globales.permiteDarVuelta = false;
        sospechosa = false;
        il_lectConf = 0;
        globales.is_terminacion = "-1";

        globales.il_lect_max = globales.tll.getLecturaActual().consAnoAnt;
        globales.il_lect_min = globales.tll.getLecturaActual().consBimAnt;
        globales.il_lect_act = globales.tll.getLecturaActual().secuenciaReal;
        //is_comentarios = globales.tll.getLecturaActual().getDireccion();
        globales.is_caseta = globales.tll.getLecturaActual().poliza;

        globales.is_lectura = globales.tll.getLecturaActual().getLectura();
        globales.is_presion = globales.tll.getLecturaActual().getAnomalia();
        globales.is_terminacion = globales.tll.getLecturaActual().terminacion;

        ls_lectura = Long.toString(globales.il_lect_act);
        il_lect_max = globales.il_lect_max;
        il_lect_min = globales.il_lect_min;

        enciendeGPS();
        // if (globales.is_caseta.contains("CF")){
        // setModoCaptura(false);
        // globales.is_lectura="0";
        // tv_caseta.setBackgroundResource(R.color.SteelBlue);
        //
        // }

        globales.is_lectura = globales.is_lectura == null ? ""
                : globales.is_lectura;
        globales.is_presion = globales.is_presion == null ? ""
                : globales.is_presion;


        // Voy a verificar lo de las lecturas consecutivas
        if (globales.tll.hayMasMedidoresIguales(globales.is_caseta)
                && !globales.is_caseta.trim().equals("0"))
            globales.sonLecturasConsecutivas = true;
        else
            globales.sonLecturasConsecutivas = false;

        setDatosInicial();
    }

    private void setDatosInicial() {
        if (globales.tdlg == null)
            return;

        try {
            lectura = globales.tll.getLecturaActual();
        } catch (Throwable e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        globales.sonLecturasConsecutivas = false;
//		tv_info.setVisibility(View.VISIBLE);
//		tv_info.setText("Medidor con " + lectura.numerodeesferas
//				+ " esferas.");

// CE, 10/10/23, Aqui debemos mostrar la LecturaReal
        et_generico.setText(globales.is_lectura);
        et_generico.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        //if (lectura.numerodeesferasReal.equals(""))
//			et_generico.setHint(lectura.numerodeesferas
//					+ " " + getString(R.string.lbl_esferas));
//		else
//			et_generico.setHint(lectura.numerodeesferasReal
//					+ " " + getString(R.string.lbl_esferas));
        tv_medidor.setText(getString(R.string.lbl_tdl_indica_medidor) + globales.is_caseta);

        tv_medidor.setBackgroundResource(R.color.green);
        tv_medidor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
        tv_medidor.setTextColor(this.getResources().getColor(R.color.White));
        tv_medidor.setGravity(Gravity.CENTER_HORIZONTAL);
        rl_contenedorLabel.setVisibility(View.VISIBLE);
        rl_contenedorLabel.setBackgroundResource(R.color.green);

        tv_medidor.setClickable(true);
        final Input input = this;
        tv_medidor.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(input, PantallaCodigos.class);

                startActivityForResult(intent, PANTALLA_CODIGOS);
            }

        });

        globales.tdlg.is_lectAnt = "";
    }

    @Override
    void muere() {
        // TODO Auto-generated method stub
        globales.inputMandaCierre = true;
        finish();
    }

    //Capturar y anomalia
    public void capturarYAnomalia(View view) {
        // mensajeInput(PRESION);

        globales.is_lectura = et_generico.getText().toString();
        //Primero validamos la lectura...
        //Si la lectura es VACIA tal vez sea una anomalia sin lectura hay que poder capturarla, asi que hay que validar eso

        if (globales.is_lectura.length() != 0) {
            //Validamos la lectura
            capturaLectura();
            tipoDeCaptura = CAPTURA_Y_ANOMALIA;
            if (!esLecturaValida)
                return;
        }
        Intent anom = new Intent(this, PantallaAnomalias.class);
        anom.putExtra("secuencial", globales.il_lect_act);
        anom.putExtra("lectura", globales.is_lectura);
        anom.putExtra("anomalia", globales.is_presion);

        startActivityForResult(anom, ANOMALIA);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bu_params = null;
        switch (requestCode) {
            case TomaDeLecturasPadre.FOTOS:
                if (globales.estoyTomandoFotosConsecutivas) {
                    tomaFotosConsecutivas(globales.idMedidorUltimaLectura);
                } else {

                    guardar();
                }
                break;
            case ANOMALIA:
                if (resultCode == Activity.RESULT_OK) {
                    String ls_comentarios = "";
                    bu_params = data.getExtras();

                    // Tomamos la anomalia y la subAnomalia en caso de requerir
                    globales.tll.getLecturaActual().setAnomalia(
                            bu_params.getString("anomalia"));
                    globales.tll.getLecturaActual().setSubAnomalia(
                            bu_params.getString("subAnomalia"));
                    globales.tll.getLecturaActual().setComentarios(
                            bu_params.getString("comentarios"));

                    globales.is_presion = bu_params.getString("anomalia");


                    // Aqui manejamos el si requiere lectura o no
                    if (!bu_params.getString("anomalia").equals("")) {
                        if (globales.tll.getLecturaActual().subAnomalias.size() > 0) {
                            presentacionAnomalias();
                            // Hay que verificar si la anomalia es ausente

                        } else if (globales.tll.getLecturaActual().anomalias.size() > 0) {

                            presentacionAnomalias();
                        }
                    }


                    openDatabase();
                    db.execSQL("delete from fotos where temporal="
                            + CamaraActivity.ANOMALIA);
                    closeDatabase();

                    if (requiereFoto) {
                        tomarFoto(CamaraActivity.ANOMALIA, 1);
                        requiereFoto = false;
                    } else if (sospechosa || globales.fotoForzada || globales.bModificar) {
                        tomarFoto(CamaraActivity.TEMPORAL, 1);
                    } else {
                        guardar();
                    }

                    //guardar();


                } else {
                    captura();
                }
                break;

            case PANTALLA_CODIGOS:
                if (resultCode == Activity.RESULT_OK) {
                    bu_params = data.getExtras();
                    globales.ignorarContadorControlCalidad = true;
                    globales.fotoForzada = true;
                    et_generico.setText(bu_params.getString("anomalia"));
                    captura();
                }

                break;

            case PANTALLA_COMENTARIOS:
                if (resultCode == Activity.RESULT_OK) {
                    bu_params = data.getExtras();
                    globales.tll.getLecturaActual().setComentarios(bu_params.getString("input"));
                    captura();
                }

                break;
        }
    }

    public void presentacionAnomalias() {

        if (globales.tll.getLecturaActual().requiereLectura() == Anomalia.LECTURA_AUSENTE) {
            globales.requiereLectura = false;
            // if (globales.tll.getLecturaActual().anomalia.ii_lectura==0 ||
            // globales.tll.getLecturaActual().anomalia.ii_ausente==4 ){
            // Si es ausente, tiene que borrar la lectura...
            globales.is_lectura = "";
            // }

        } else {
            globales.requiereLectura = true;
        }


        if (globales.tll.getLecturaActual().requiereFotoAnomalia() == 1)
            requiereFoto = true;
//			tomarFoto(CamaraActivity.ANOMALIA);
    }

    public void getSigLect(View view) {
        permiteCerrar();
        if (globales.bModificar)
            is_mensaje_direccion = getString(R.string.msj_tdl_no_mas_lecturas_ingr_despues);
        else
            is_mensaje_direccion = getString(R.string.msj_tdl_no_mas_lecturas_despues);
        getSigLect();

    }

    public void getAntLect(View view) {
        permiteCerrar();
        if (globales.bModificar)
            is_mensaje_direccion = getString(R.string.msj_tdl_no_mas_lecturas_ingr_antes);
        else
            is_mensaje_direccion = getString(R.string.msj_tdl_no_mas_lecturas_antes);
        getAntLect();
    }

    @Override
    protected void capturaDespuesDelPuntoGPS() {
        // TODO Auto-generated method stub

        guardar();


    }


    public void muestraRespuestaSeleccionada(final MensajeEspecial me) {
        if (me != null) {


            switch (me.tipo) {
                case MensajeEspecial.MENSAJE_SI_NO:

                    preguntaSiNo(me);

                    break;

                case MensajeEspecial.OPCION_MULTIPLE:

                    preguntaOpcionMultiple(me);

                    break;

            }

        }
    }


    public void regresaDeMensaje(MensajeEspecial me, int respuesta) {
        if (me.respondeA == TomaDeLecturasGenerica.PREGUNTAS_CONSUMO_CERO || me.respondeA == TomaDeLecturasGenerica.PREGUNTAS_SIGUE_CORTADO) {
            //Ya no validamos, ya lo hicimos
            pregunteConsumo = true;
            globales.ignorarContadorControlCalidad = true;

            globales.calidadOverride = globales.tdlg.cambiaCalidadSegunTabla(me.regresaValor(respuesta).substring(0, 1), me.regresaValor(respuesta));
//			globales.ignorarGeneracionCalidadOverride=true;
            //regresamos a la funcion
            switch (tipoDeCaptura) {
                case CAPTURA_Y_REGRESA:
                    capturaLectura();
                    break;
                case CAPTURA_Y_SIGUIENTE:
                    capturarYSiguiente(tv_label);
                    break;
                case CAPTURA_Y_ANOMALIA:
                    capturarYAnomalia(tv_label);
                    break;
            }

        }
    }

    @Override
    protected void onResume() {
        //Ahora si abrimos
        if (globales.tdlg == null) {
            super.onResume();
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            System.exit(0);
            return;
        }
        super.onResume();
    }

    /* ====================================================================================
        Diálogo para mostrar un mensaje.
        RLR / 2023-09-15
    ==================================================================================== */

    private void mostrarMensaje(String titulo, String mensaje, Throwable t) {
        if (mDialogoMsg == null) {
            mDialogoMsg = new DialogoMensaje(this);
        }

        mDialogoMsg.mostrarMensaje(titulo, mensaje, t.getMessage());
    }
}
