package enruta.soges_engie;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import enruta.soges_engie.clases.AppSinGps;
import enruta.soges_engie.clases.EmergenciaMgr;
import enruta.soges_engie.clases.OperacionResponse;
import enruta.soges_engie.clases.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
//import android.graphics.drawable.AnimationDrawable;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import androidx.cardview.widget.CardView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBar;

public class TomaDeLecturas extends TomaDeLecturasPadre implements
        OnGestureListener {

    int ii_dondeEstaba = 0;
    Timer timer = new Timer();

    double porcentaje = 1.0;
    double porcentajeInfoCliente = 1.0;
    double factorPorcentaje = 0.05;

    int segundoCambiarFuente = 5;
    boolean permiteCambiarFuente = false;
    Timer cambiarFuenteTimer = new Timer();

    private GestureDetector gestureScanner;

    // EditText et_lectura/*, et_presion*/;
    // TextView tv_informacion, tv_lectura, tv_presion;
    TextView tv_caseta, tv_min, tv_max, tv_mensaje, tv_mensaje2, tv_respuesta/*
     * ,
     * tv_lectura
     */, tv_contador, tv_presion, tv_comentarios, tv_lectura,
            tv_anomalia, tv_contadorOpcional, tv_lecturaAnterior, tv_campo0, tv_campo1, tv_campo2, tv_campo3, tv_campo4,
            label_campo0, label_campo1, label_campo2, label_campo3, label_campo4, tv_advertencia;
    Button button1, button2, button3, button4, button5, button6, b_repetir_anom;
    ImageView iv_gps, iv_button3, iv_button4, iv_button5, iv_button6;
    MenuItem iv_campanaNegra,iv_campanaAmarilla;
//    AnimationDrawable iv_campanitaAnimation;
    View layout;

    TextView tv_nueva_datos_cliente,tv_nueva_direccion,tv_nueva_datos_sap1,tv_nueva_datos_sap2;
    TextView tv_sap_medidor,tv_sap_cuenta_contrato,tv_sap_interlocutor,tv_sap_notificacion;
    // String globales.is_lectura, globales.is_presion, globales.is_caseta,
    // globales.is_terminacion;

    LinearLayout ll_limites, ll_linearLayout1, ll_generica, ll_linearLayout2, cuadricula;
    RelativeLayout ll_layoutTipoDeOrden;

    CardView cv_button1, cv_button2, cv_button3, cv_button4, cv_button5, cv_button6;

    boolean filtrarComentarios = false;
    //boolean captureAnomalias=false;

    String is_lectAnt = "", is_comentarios, is_problemas;

    boolean esSuperUsuario = false;
//	String ultimaAnomaliaSeleccionada="";
//	String ultimaSubAnomaliaSeleccionada="";

    boolean preguntaSiBorraDatosComodin = false;

    float anomSize, lecturaSize, mensajeSize, casetaSize, cialSize, minSize, maxSize,
            nombreSize, comentariosSize, nisradSize, direccionSize,
            contadorOpcionalSize, tipoMedidorSize, sizeGenerico = 14, labelCuadriculaSize, respuestasSize;

    int modoCambiofuente = NINGUNO;
    long secuencialAntesDeInput = 0;

    boolean preguntarHabitado = false;
    boolean puedeVerDatosAlRegresar = false;

    private EmergenciaMgr mEmergenciaMgr = null;
    private AlertDialog mAlertEmergencia = null;
    private DialogoMensaje mDialogoMsg = null;

    private ActionBar actionBar;

    private final static int TOMAR_VIDEO = 52;

    @SuppressLint("NewApi")
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Bundle bu_params;

        esconderTeclado();
        boolean permiteTomarFoto = true;

        if (globales.sesionEntity == null) {
            cerrarActivity();
            return;
        }

        switch (requestCode) {
            case FOTOS:
// CE, 09/11/23, Vamos a
                rutinaDespuesDeTomarFotoDeLlegada(requestCode, resultCode);
/*
                if (puedeVerDatosAlRegresar && resultCode == Activity.RESULT_OK) {
                    globales.tll.getLecturaActual().verDatos = true;
                    setDatos(false);
                    openDatabase();
                    db.execSQL("update ruta set verDatos=1, fechaDeInicio='" + Main.obtieneFecha("ymdhis") + "' where secuenciaReal=" + globales.il_lect_act);
                    closeDatabase();
                    setStyleDatosVistos();
                    //por ahorita no...
//				if (preguntarHabitado){
//					preguntarHabitado=false;
//				}
                }
                globales.puedoCancelarFotos = false;
                puedeVerDatosAlRegresar = false;

                if (globales.estoyTomandoFotosConsecutivas) {
                    tomaFotosConsecutivas(globales.idMedidorUltimaLectura);
                } else {
                    tieneFotos();
                    //avanzarDespuesDeAnomalia();
                }
                if (regreseDe == ANOMALIA && globales.legacyCaptura) {
                    if (globales.tll.getLecturaActual().requiereLectura() == Anomalia.LECTURA_AUSENTE && !globales.tdlg.avanzarDespuesDeAnomalia(ultimaAnomaliaSeleccionada, ultimaSubAnomaliaSeleccionada, false)) {
                        capturar();
                    } else if (globales.tdlg.avanzarDespuesDeAnomalia(ultimaAnomaliaSeleccionada, ultimaSubAnomaliaSeleccionada, false)) {
                        avanzarDespuesDeAnomalia();
                    }
                } else if (regreseDe == LECTURA && globales.legacyCaptura) {

//				if (globales.is_terminacion.endsWith("2")){
                    capturar();
//				}
//				else{
//					globales.is_terminacion="_2";
////					tomarFoto(CamaraActivity.TEMPORAL, 1);
//				}

                    if (Build.VERSION.SDK_INT >= 11)
                        invalidateOptionsMenu();

                } else if (regreseDe == LECTURA && !globales.legacyCaptura) {

//				if (!globales.is_terminacion.endsWith("2")){
//					globales.is_terminacion="_2";
////					tomarFoto(CamaraActivity.TEMPORAL, 1);
//				}
//				else{
//					globales.is_terminacion="_1";
//				}


                }
                //regreseDe=FOTOS;
                voyATomarFoto = false;
*/
                break;
            case LECTURA:
                regreseDe = LECTURA;
                voyATomarFoto = false;
                if (resultCode == Activity.RESULT_OK) {
                    bu_params = data.getExtras();
                    globales.is_lectura = bu_params.getString("input");
// CE, 10/10/23, No necesitamos calcular ningun consumo
//                    globales.tdlg.setConsumo();
                    if (globales.is_lectura.equals("")) {
                        globales.BorrarTodasLosCamposEngie();
                        globales.tdlg.regresaDeBorrarLectura();
                    }
                    regresaDeBorrar();
                    if (globales.is_lectura.trim().length() > 0
                            || globales.tll.getLecturaActual().anomalias.size() > 0) {
                        globales.tll.getLecturaActual().sospechosa = String.valueOf(bu_params
                                .getInt("confirmada"));
                        globales.tll.getLecturaActual().guardarSospechosa();
                        int requiereLectura = globales.tll.getLecturaActual().requiereLectura();
                        if (!globales.is_lectura.equals("") ||
                                requiereLectura == Anomalia.LECTURA_AUSENTE)
                            setModoCaptura();
                        else
                            salirModoCaptura();
                    } else {
                        globales.modoCaptura = false;
                        salirModoCaptura();
                        permiteTomarFoto = false;
                    }

                    // borramos fotos temporales anteriores
                    openDatabase();
                    db.execSQL("delete from fotos where temporal="
                            + CamaraActivity.TEMPORAL);
                    db.execSQL("delete from fotos where temporal="
                            + CamaraActivity.ANOMALIA);
                    closeDatabase();

                    globales.is_terminacion = bu_params.getString("terminacion");
                    globales.tll.getLecturaActual().setTerminacion(
                            globales.is_terminacion);

                    if (globales.is_lectura.equals(""))
                        tv_lectura.setText("");
                    else
                        tv_lectura.setText(getString(R.string.lbl_tdl_indica_lectura) + globales.is_lectura);

                    if (globales.is_lectura.equals(""))
                        tv_anomalia.setText("");
                    else
                        tv_anomalia.setText(getString(R.string.lbl_tdl_indica_anomalia) + (globales.is_presion.length() > 3 ? "***" : globales.is_presion));

                    if ((bu_params.getBoolean("sospechosa") || globales.fotoForzada || globales.bModificar)
                            && permiteTomarFoto && globales.is_lectura.length() > 0) {

                        if (modo == Input.FOTOS) {
                            globales.ignorarContadorControlCalidad = true;
                        }

//					if (!globales.ignorarGeneracionCalidadOverride){
                        globales.calidadOverride = globales.tdlg.cambiaCalidadSegunTabla("", "");
//					}
//					else{
//						globales.ignorarGeneracionCalidadOverride=false;
//					}

//*******************************************************************************************************
// CE, 14/10/23, En el caso del SOGES, no existe el concepto de Control de Calidad como en el SISTOLE
                        tomarFoto(CamaraActivity.TEMPORAL, 1);
/*
                        if (globales.ignorarContadorControlCalidad) {
                            tomarFoto(CamaraActivity.TEMPORAL, 1);
                            globales.ignorarContadorControlCalidad = false;
                        } else {
                            if (contadorControlCalidadFotos == 0) {
                                tomarFoto(CamaraActivity.TEMPORAL, 1);
                                contadorControlCalidadFotos++;
                            } else {
                                contadorControlCalidadFotos++;
                                if (contadorControlCalidadFotos >= globales.controlCalidadFotos) {
                                    contadorControlCalidadFotos = 0;
                                }
                            }
                        }
*/
//*******************************************************************************************************
                    }
                    if (bu_params.getBoolean("sospechosa")) {
                        globales.tll.getLecturaActual().sospechosa = String.valueOf(bu_params.getInt("confirmada"));
                        globales.tll.getLecturaActual().guardarSospechosa();
                    }
                    if (!voyATomarFoto && globales.legacyCaptura) {
                        capturar();
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    setDatos(false);
                    if (globales.inputMandaCierre) {
                        globales.inputMandaCierre = false;
                        muere();
                        return;
                    }
                    bu_params = data.getExtras();
                    if (bu_params.getBoolean("sospechosa")) {
                        globales.tll.getLecturaActual().sospechosa = String.valueOf(bu_params
                                .getInt("confirmada"));
                        globales.tll.getLecturaActual().guardarSospechosa();
                    }
                    if (globales.bModificar) {
                        //establcemos el fondo de correccion
                        setFondoCorreccion();
                    } else {
                        layout.setBackgroundResource(0);
                    }
                    if (secuencialAntesDeInput != globales.tll.getLecturaActual().secuencia)
                        setDatos();
                }
                voyATomarFoto = false;
                if (!globales.bModificar && globales.tll.getLecturaActual().verDatos && !globales.modoCaptura) {
                    setStyleDatosVistos();
                }
// CE, 14/10/23, Vamos a mostrar o esconder los botones de acuerdo a las reglas de Engie
                if (globales.is_lectura.equals("")) {
                    tv_mensaje2.setText("¿Es Efectiva o No Efectiva?");
                    tv_mensaje2.setVisibility(View.VISIBLE);
                    estableceVisibilidadDeBotones(View.VISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, true);
                } else {
                    tv_mensaje2.setText("Presione FINALIZAR");
                    tv_mensaje2.setVisibility(View.VISIBLE);
                    estableceVisibilidadDeBotones(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.VISIBLE, false);
                }
                break;
            /*
             * case COMENTARIOS: if (resultCode == Activity.RESULT_OK){ bu_params
             * =data.getExtras(); is_comentarios=bu_params.getString("input");
             * setModoCaptura(); tv_comentarios.setText(is_comentarios); }
             *
             * break;
             */
            /*
             * case PRESION: if (resultCode == Activity.RESULT_OK){ bu_params
             * =data.getExtras(); globales.is_presion=bu_params.getString("input");
             * setModoCaptura(); tv_presion.setText("P:" + globales.is_presion); if
             * (globales.is_presion.trim().equals("")){
             * tv_presion.setVisibility(View.GONE); } else{
             * tv_presion.setVisibility(View.VISIBLE); } } break;
             */

            case ANOMALIA:
                regreseDe = ANOMALIA;
                voyATomarFoto = false;

                //Si se hicieron cambois en el nombre del usuario se verán
                setDatos(false);
                String ls_comentarios = "";
                boolean anomaliaCapturada = true;
                String ls_anomalia = "", ls_subAnomalia = "";

                Anomalia anom = null;

                if (resultCode == Activity.RESULT_OK) {
                    bu_params = data.getExtras();
                    globales.is_terminacion = "_1";
                    ls_anomalia = bu_params.getString("anomalia");
                    ls_subAnomalia = bu_params.getString("subAnomalia");

                    openDatabase();
                    db.execSQL("delete from fotos where temporal="
                            + CamaraActivity.TEMPORAL);

                    db.execSQL("delete from fotos where temporal="
                            + CamaraActivity.ANOMALIA);
                    closeDatabase();

                    if (!ls_subAnomalia.equals("")) {
                        anom = new Anomalia(this, ls_subAnomalia, true);
                    } else {
                        anom = new Anomalia(this, ls_anomalia, false);
                    }
                    if (preguntaSiBorrarEnAnomaliaAusentes && !globales.is_lectura.equals("")/*&& !globales.bModificar*/) {
                        if (anom.requiereLectura() == Anomalia.LECTURA_AUSENTE) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            AlertDialog alert;

                            //Si ingresé datos, estoy en modo captura y  me estoy moviendo deberia preguntar
                            builder.setMessage(R.string.str_pregunta_guardar_cambios_anomalia_ausente)
                                    .setCancelable(false).setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //recepcion();
                                            preguntaSiBorrarEnAnomaliaAusentes = false;
                                            onActivityResult(requestCode, resultCode, data);
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();

                                        }
                                    });

                            alert = builder.create();
                            alert.show();
                            return;
                        }
                    }

                    preguntaSiBorrarEnAnomaliaAusentes = true;

                    //Realizamos los cambios necesarios en la anomalia
                    globales.tdlg.cambiosAnomalia(bu_params.getString("anomalia"));

                    // Tomamos la anomalia y la subAnomalia en caso de requerir
                    globales.tll.getLecturaActual().setAnomalia(
                            bu_params.getString("anomalia"));

                    globales.tll.getLecturaActual().setSubAnomalia(
                            bu_params.getString("subAnomalia"));

//				globales.tll.getLecturaActual().setComentarios(
//						bu_params.getString("comentarios"));

                    ultimaAnomaliaSeleccionada = bu_params.getString("anomalia");
                    ultimaSubAnomaliaSeleccionada = bu_params.getString("subAnomalia");

                    //Aqui guardamos si debe repetir anomalia o no
//				String ls_anomalia=globales.tll.getLecturaActual().getAnomaliaAMostrar();
//				if (ls_anomalia.endsWith("A") || ls_anomalia.endsWith("AC") || ls_anomalia.endsWith("CA")|| ls_anomalia.endsWith("R")|| ls_anomalia.endsWith("Z"))
//					globales.anomaliaARepetir=globales.convertirAnomalias?
//							globales.tll.getLecturaActual().getUltimaAnomalia().is_conv:
//								globales.tll.getLecturaActual().getUltimaAnomalia().is_anomalia;
//				else
//					globales.anomaliaARepetir="";
                    //captureAnomalias=true;
                    preguntaSiBorraDatos = true;
                    muestraRespuestaSeleccionadaAutomatica(globales.tdlg.regresaDeAnomalias(bu_params.getString("anomalia")));

// CE, 14/10/23, Vamos a mostrar o esconder los botones de acuerdo a las reglas de Engie
                    if (ls_anomalia.equals("")) {
                        tv_mensaje2.setText("¿Es Efectiva o No Efectiva?");
                        tv_mensaje2.setVisibility(View.VISIBLE);
                        estableceVisibilidadDeBotones(View.VISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, true);
                    } else {
                        tv_mensaje2.setText("Presione FINALIZAR");
                        tv_mensaje2.setVisibility(View.VISIBLE);
                        estableceVisibilidadDeBotones(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.VISIBLE, false);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    anomaliaCapturada = false;
                }


                globales.is_presion = globales.tll.getLecturaActual().getAnomaliaAMostrar();

//				tv_anomalia.setText(getString(R.string.lbl_tdl_indica_anomalia) + (globales.is_presion.length()>3?"***":globales.is_presion));
//
//				if (globales.tll.getLecturaActual().anomalias.size() != 0) {
//					// Tiene una anomalia
//					ls_comentarios = getString(R.string.str_anomalia)+": "  + globales.tll.getLecturaActual().getAnomaliaAMostrar();
//					if (globales.tll.getLecturaActual().subAnomalias.size() != 0) {
//						// Tiene una subanomalia
//						ls_comentarios += ", "
//								+ getString(R.string.str_subanomalia)+": " 
//								+ globales.tll.getLecturaActual().getSubAnomaliaAMostrar();
//					}
//					ls_comentarios += "\n";
//
//					// //Hay que verificar si la anomalia es ausente
//					// if
//					// (globales.tll.getLecturaActual().anomalia.ii_lectura==0
//					// || globales.tll.getLecturaActual().anomalia.ii_ausente==4
//					// ){
//					// //Si es ausente, tiene que borrar la lectura...
//					// globales.is_lectura="";
//					// tv_lectura.setText(getString(R.string.lbl_tdl_indica_lectura) +globales.is_lectura);
//					// }
//				}
//
//				tv_comentarios.setText(ls_comentarios
//						+ globales.tll.getLecturaActual().getComentarios());

                setDatos(false);
                //setModoCaptura();


                //Fotos tomadas con las anomalias se almacenan sin importar que se borren, just like the nokia does it
//				openDatabase();
//				db.execSQL("delete from fotos where temporal="
//						+ CamaraActivity.ANOMALIA);
//			closeDatabase();


                // Aqui manejamos el si requiere lectura o no
                //if (!bu_params.getString("anomalia").equals("")) {
                if (globales.tll.getLecturaActual().anomalias.size() != 0) {
//					if (globales.tll.getLecturaActual().subAnomalias.size() != 0) {
//						presentacionAnomalias();
                    // Hay que verificar si la anomalia es ausente

                    //} else if (globales.tll.getLecturaActual().anomalias.size() != 0) {


                    presentacionAnomalias(anomaliaCapturada, ls_anomalia, ls_subAnomalia);
                    //}
                } else {
                    // Hay que verificar si aun hay que seguir en modo de
                    // captura...
                    if (globales.is_lectura.equals("")) {
                        globales.modoCaptura = false;
                        salirModoCaptura();
                        if (!globales.bModificar && globales.tll.getLecturaActual().verDatos && !globales.modoCaptura) {
                            setStyleDatosVistos();
                        }
                    }
                    button1.setEnabled(true);
                    cv_button1.setEnabled(true);
//                    cv_button1.setBackgroundColor(R.color.LimeGreen);
                }

                if (anom != null) {
                    if (anom.requiereLectura() == Anomalia.LECTURA_AUSENTE && !voyATomarFoto && globales.legacyCaptura && !globales.tdlg.avanzarDespuesDeAnomalia(ultimaAnomaliaSeleccionada, ultimaSubAnomaliaSeleccionada, false)) {
                        capturar();

                    }
                }

                voyATomarFoto = false;


                break;

            case BUSCAR_MEDIDOR:
                if (resultCode == Activity.RESULT_OK) {
                    bu_params = data.getExtras();
                    try {
                        globales.strUltimaBusquedaRealizada = bu_params.getString("ultimabusqueda");
                        globales.tll.setSecuencialLectura(bu_params.getInt("secuencia"));

                        // Si estamos modificando y no tiene lectura o anomalia
                        // debemos romper el modo de modificacion
                        if (globales.bModificar) {
                            if ((globales.tll.getLecturaActual().getLectura()
                                    .equals("") && globales.tll.getLecturaActual()
                                    .getAnomalia().equals(""))) {
                                globales.bcerrar = false;
                                globales.bModificar = false;
                                // tv_indica_corr.setText("N");
                            }
                        }
                        globales.modoCaptura = false;
                        this.salirModoCaptura();
                        setDatos();

                        // Si selecciono del listado un medidor con lectura o
                        // anomalia, debe detectar que esta en modo de correccion
                        if (!globales.bModificar) {
                            if (!globales.tll.getLecturaActual().is_tipoLectura.trim().equals("")) {
                                globales.bModificar = true;
                                setModoModificacion(false);
                            }
                        }

                    } catch (Throwable e) {

                        e.printStackTrace();
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    bu_params = data.getExtras();
                    globales.strUltimaBusquedaRealizada = bu_params.getString("ultimabusqueda");
                    globales.moverPosicion = false;
                    preguntaSiBorraDatos = preguntaSiBorraDatosComodin;
                    if (globales.strUltimaBusquedaRealizada.equals("Campanita")) {
                        globales.strUltimaBusquedaRealizada = "";
                        openDatabase();
                        db.execSQL("update ruta set balance=''");
                        closeDatabase();
                    }
                }
                break;

            case REQUEST_ENABLE_BT:
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
                        .getDefaultAdapter();
                if (mBluetoothAdapter.isEnabled()) {
                    // Solo si esta encendido volvemos a llamar esta función
                    mandarAImprimir(ilec_lectura);
                }
                break;
            case NO_REGISTADOS:
                if (resultCode == Activity.RESULT_OK) {
                    bu_params = data.getExtras();
                    globales.tdlg.regresaDeCamposGenericos(bu_params, "noregistrados");

                    //Ya insertamos el valor, solo falta ir al registro...
                    globales.il_total = globales.tll.getUltimaSecuencia();
                    this.getUltLect();
                } else {
                    if (!globales.tll.hayPendientes()) {
                        muere();
                    }
                }
                break;
/*
            case CLIENTE_YA_PAGO:
                if (resultCode == Activity.RESULT_OK) {
                    bu_params = data.getExtras();
                    globales.tdlg.regresaDeCamposGenericos(bu_params, "cliente_ya_pago");
                } else {
                    if (!globales.tll.hayPendientes()) {
                        muere();
                    }
                }
                break;
*/
            case TRANSMISION:
                //Recibimos si fue extoso el result
                if (resultCode == Activity.RESULT_OK) {
                    Intent lrs = new Intent(this, trasmisionDatos.class);
                    lrs.putExtra("tipo", trasmisionDatos.RECEPCION);
                    startActivityForResult(lrs, RECEPCION);
                }
                break;

            case RECEPCION:
                //Actualizamos el secuencial Actual
//			globales.tll.getLecturaActual().corregirSecuenciaReal();
                globales.il_total = globales.tll.getUltimaSecuencia();
                setDatos(false);
                break;
        }

        if (Build.VERSION.SDK_INT >= 11)
            invalidateOptionsMenu();

    }

    public void setModoCaptura() {
        setModoCaptura(true);
    }

    public void setModoCaptura(boolean esconderBuscar) {
        globales.modoCaptura = true;

// CE, 14/10/23, Vamos a reemplazar este ModoCaptura por el nuevo metodo
        tv_mensaje2.setText("Presione FINALIZAR");
        tv_mensaje2.setVisibility(View.VISIBLE);
// CE, 14/12/23, Vamos a esconder Efectiva y NoEfectiva
//        estableceVisibilidadDeBotones(button1.getVisibility(),button2.getVisibility(),View.INVISIBLE,View.VISIBLE,View.INVISIBLE,View.VISIBLE,false);
        estableceVisibilidadDeBotones(View.INVISIBLE,View.INVISIBLE,View.INVISIBLE,View.VISIBLE,View.INVISIBLE,View.VISIBLE,false);
/*
        button4.setVisibility(View.INVISIBLE);
        iv_button4.setVisibility(View.INVISIBLE);
        iv_button6.setVisibility(View.INVISIBLE);
        button6.setText(R.string.guardar);
        button6.setEnabled(true);
        // button3.setEnabled(false);
        button3.setEnabled(!esconderBuscar);
*/
        button4.setText(R.string.reiniciar);
        button6.setText(R.string.guardar);
        tv_contador.bringToFront();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tomadelecturas);

        is_mensaje_direccion = getString(R.string.msj_lecturas_no_hay_mas);
        setTitle("");

        Toolbar mTopToolbar;
        mTopToolbar = (Toolbar) findViewById(R.id.toma_de_lecturas);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(getDrawable(R.drawable.soges_blanco));
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        mTopToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mostrarToolbar();

        //startListeningGPS();
        mHandler = new Handler();

        globales = ((Globales) getApplicationContext());
        inicializarVariables();

        Bundle bu_params = getIntent().getExtras();
        esSuperUsuario = bu_params.getBoolean("esSuperUsuario");

        globales.is_nombre_Lect = bu_params.getString("nombre");
        bHabilitarImpresion = bu_params.getBoolean("bHabilitarImpresion");

        porcentaje = globales.porcentaje_lectura;
        porcentajeInfoCliente = globales.porcentaje_info;

        // Obtenemos la impresora...
        getImpresora();

        // Filtrado y para empezar editando
        openDatabase();
        Cursor c = db.query("config", null, "key='modo'", null, null, null,
                null);

        if (c.getCount() > 0) {
            c.moveToFirst();

            // Modo correccion segun el su posicion en el arreglo
            if (c.getInt(c.getColumnIndex("value")) == 1) {
                globales.capsModoCorreccion = true;
                globales.bModificar = true;

            }

        }

        c.close();

        // ahora las lecturas forzadas, fotos forzadas y demas

        c = db.query("config", null, "key='modo_config'", null, null, null,
                null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            modo = Integer.parseInt(c.getString(c.getColumnIndex("value")));

            //this.modo = modo;
            switch (modo) {
                case Input.NORMAL:// Normal
                    break;

                case Input.SIN_FOTOS: // Sin fotos
                    globales.validar = false;
                    break;

                case Input.FOTOS: // Fotos
                    globales.fotoForzada = true;
                    break;

                case Input.FOTOS_CC: // Fotos Control de Calidad
                    globales.validar = false;
                    break;
            }
        }

        c.close();
        closeDatabase();
        try {
            globales.tll = new TodasLasLecturas(this);
            if (!globales.tll.hayMasLecturas())
                throw new Throwable();

            int ordenInconclusa = globales.tll.getOrdenInconclusa();

            if (ordenInconclusa != 0) {
                globales.tll.setSecuencialLectura(ordenInconclusa);
            }
        } catch (Throwable e) {
//			if (!globales.bModificar) {
//				Toast.makeText(this, R.string.msj_lecturas_no_hay_mas, Toast.LENGTH_LONG)
//						.show();
//				// finish();
//				muere();
//				return;
//			}

        }

        boolean obtenerSiguiente = true;
        gestureScanner = new GestureDetector(this, this);

        globales.il_total = globales.tll.getUltimaSecuencia();

        layout = (View) findViewById(R.id.relativeLayoutm);

        iniciaCampos();

        ViewTreeObserver vto = tv_lectura.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                setPorcentaje();
                ViewTreeObserver obs = tv_lectura.getViewTreeObserver();

                // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                // {
                // obs.removeOnGlobalLayoutListener(this);
                // } else {
                obs.removeGlobalOnLayoutListener(this);
                // }
            }
        });

        // if (obtenerSiguiente)
        // getSigLect();
        // else

//		if (globales.bModificar) {
//			try {
//				globales.tll.setSecuencialLectura(0);
//			} catch (Throwable e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			setModoModificacion();
//
//			if (!globales.bModificar) {
//				// Aqui entra porque me sacaron del modo de correccion (no hay
//				// nada que corregir), asi que
//				globales.capsModoCorreccion = false;
//				openDatabase();
//				db.execSQL("Update config set value='0' where key='modo'");
//				closeDatabase();
//				// getSigLect();
//			}
//		}

        permiteCerrar();
        if (globales.bcerrar) {
            Toast.makeText(this, "No hay más ordenes", Toast.LENGTH_SHORT)
                    .show();
//	bEsElFInal = true;
//	if (!ll_lectura.getLectura().equals(""))
//		mandarAImprimir(ll_lectura);
            // this.finish();
//			iniciarModoCorreccionCAPS();
            //muere();
            //Cuando no haya mas ordenes, lo mas indicado será que le pida si desea capturar una, de no ser asi, que salga de la aplicacion.
            mostrarVentanaDeNoRegistrados();
            return;
        }
//			

        else
            setDatos();
        // c.close();
    }

    protected void mostrarToolbar() {
/*
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
*/
    }

    /*
     * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
     * menu; this adds items to the action bar if it is present.
     * //getMenuInflater().inflate(R.menu.toma_de_lecturas, menu); return true;
     * }
     */

    private void iniciaCampos() {
        // et_lectura=(EditText) findViewById(R.id.et_lectura);
        // et_presion=(EditText) findViewById(R.id.et_presion);

        // tv_informacion= (TextView) findViewById(R.id.tv_informacion);
        // et_lectura= (EditText) findViewById(R.id.et_lectura);
        tv_caseta = (TextView) findViewById(R.id.tv_caseta);
        tv_min = (TextView) findViewById(R.id.tv_min);
        tv_max = (TextView) findViewById(R.id.tv_max);
        tv_mensaje = (TextView) findViewById(R.id.tv_mensaje);
        tv_mensaje2 = (TextView) findViewById(R.id.tv_mensaje2);
        tv_respuesta = (TextView) findViewById(R.id.tv_respuesta);
        tv_contador = (TextView) findViewById(R.id.tv_contador);
//		tv_presion = (TextView) findViewById(R.id.tv_presion);
        tv_comentarios = (TextView) findViewById(R.id.tv_comentarios);
        tv_lectura = (TextView) findViewById(R.id.tv_lectura);
        tv_anomalia = (TextView) findViewById(R.id.tv_anomalia);
        tv_contadorOpcional = (TextView) findViewById(R.id.tv_contadorOpcional);
        tv_advertencia = (TextView) findViewById(R.id.tv_advertencia);
        tv_lecturaAnterior = (TextView) findViewById(R.id.tv_lecturaAnterior);

        tv_nueva_datos_cliente = (TextView) findViewById(R.id.tv_nueva_datos_cliente);
        tv_nueva_direccion = (TextView) findViewById(R.id.tv_nueva_direccion);
        tv_nueva_datos_sap1 = (TextView) findViewById(R.id.tv_nueva_datos_sap1);
        tv_nueva_datos_sap2 = (TextView) findViewById(R.id.tv_nueva_datos_sap2);

        tv_sap_medidor = (TextView) findViewById(R.id.tv_sap_medidor);
        tv_sap_cuenta_contrato = (TextView) findViewById(R.id.tv_sap_cuenta_contrato);
        tv_sap_interlocutor = (TextView) findViewById(R.id.tv_sap_interlocutor);
        tv_sap_notificacion = (TextView) findViewById(R.id.tv_sap_notificacion);

        cuadricula = (LinearLayout) findViewById(R.id.cuadricula);

        tv_campo0 = (TextView) findViewById(R.id.campo0);
        tv_campo1 = (TextView) findViewById(R.id.campo1);
        tv_campo2 = (TextView) findViewById(R.id.campo2);
        tv_campo3 = (TextView) findViewById(R.id.campo3);
        tv_campo4 = (TextView) findViewById(R.id.campo4);

        label_campo0 = (TextView) findViewById(R.id.label_campo0);
        label_campo1 = (TextView) findViewById(R.id.label_campo1);
        label_campo2 = (TextView) findViewById(R.id.label_campo2);
        label_campo3 = (TextView) findViewById(R.id.label_campo3);
        label_campo4 = (TextView) findViewById(R.id.label_campo4);

        iv_gps = (ImageView) findViewById(R.id.iv_gps);// GPS
        iv_button3 = (ImageView) findViewById(R.id.iv_button3);// Flechita
        iv_button4 = (ImageView) findViewById(R.id.iv_button4);// Flechita
        iv_button5 = (ImageView) findViewById(R.id.iv_button5);// Flechita
        iv_button6 = (ImageView) findViewById(R.id.iv_button6);// Flechita

        button1 = (Button) findViewById(R.id.button1);// Lectura
        button2 = (Button) findViewById(R.id.button3); // Presion
        button3 = (Button) findViewById(R.id.button2);// Comentarios
        button4 = (Button) findViewById(R.id.button4); // Anterior
        button5 = (Button) findViewById(R.id.button5);// Fotos
        button6 = (Button) findViewById(R.id.button6);// Siguiente

        cv_button1 = (CardView) findViewById(R.id.cv_button1);// Lectura
        cv_button2 = (CardView) findViewById(R.id.cv_button2); // Presion

        cv_button1.setBackgroundTintList(getResources().getColorStateList(R.color.button_backgroud));;
        cv_button2.setBackgroundTintList(getResources().getColorStateList(R.color.button_red));;

        b_repetir_anom = (Button) findViewById(R.id.b_repetir_anom);// Siguiente

        ll_limites = (LinearLayout) findViewById(R.id.ll_limites);
        ll_linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
        ll_linearLayout2 = (LinearLayout) findViewById(R.id.linearLayout2);
        ll_generica = (LinearLayout) findViewById(R.id.ll_generica);

        ll_layoutTipoDeOrden = (RelativeLayout) findViewById(R.id.ll_layoutTipoDeOrden);

// CE, 11/10/23, Vamos a ocultar los botones del viejo diseño
        ll_linearLayout1.setBackgroundResource(R.color.white);
        tv_contador.setVisibility(View.GONE);
        ll_generica.setVisibility(View.VISIBLE);
        tv_caseta.setVisibility(View.GONE);
        tv_lectura.setVisibility(View.GONE);
        tv_anomalia.setVisibility(View.GONE);

        OnGestureListener ogl = new OnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // TODO Auto-generated method stub
                return false;
            }

        };

        // tv_presion=(TextView) findViewById(R.id.tv_presion);
        final TomaDeLecturas parent = this;

        View.OnClickListener clicGPS = new View.OnClickListener() {
            public void onClick(View v) {
                mostrarUbicacionGPS();
            }
        };

        // Vamos a declarar los listeners ya que nos pueden servir mas adelante
        // en otros objetos
        View.OnClickListener clicLectura = new View.OnClickListener() {
            public void onClick(View v) {
                // Capturamos la lectura
                globales.setEstadoDeLaRepercusion(true,false);
                cancelaTimer();
                if (button1.isEnabled()) {
                    cancelaTimer();

                    Intent intent = new Intent(parent, Input.class);
                    intent.putExtra("tipo", Input.LECTURA);
                    intent.putExtra("min", globales.il_lect_min);
                    intent.putExtra("max", globales.il_lect_max);
                    intent.putExtra("act", globales.is_lectura);
                    intent.putExtra("validar", /*globales.validar*/true); //Siempre va a validar
                    intent.putExtra("modo", modo);
                    intent.putExtra("secuencia", globales.il_lect_act);

                    secuencialAntesDeInput = globales.tll.getLecturaActual().secuencia;
                    startActivityForResult(intent, LECTURA);
                }
            }
        };

        View.OnLongClickListener longClicLectura = new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                globales.setEstadoDeLaRepercusion(true,true);
                cancelaTimer();
                // TODO Auto-generated method stub
                if (button1.isEnabled()) {
                    cancelaTimer();
                    globales.is_lectura = "";
                    globales.BorrarTodasLosCamposEngie();
                    globales.tdlg.regresaDeBorrarLectura();

                    if (globales.tll.getLecturaActual().anomalias.size() == 0) {
                        globales.modoCaptura = false;
                        salirModoCaptura();
                        // borramos fotos temporales anteriores
                        openDatabase();

                        db.execSQL("delete from fotos where temporal="
                                + CamaraActivity.TEMPORAL);

                        closeDatabase();

                        //tv_lectura.setText(getString(R.string.lbl_tdl_indica_lectura) + globales.is_lectura);
                    }
                    globales.is_presion = globales.tll.getLecturaActual().getAnomaliaAMostrar();

                    if (globales.is_lectura.equals(""))
                        tv_lectura.setText("");
                    else
                        tv_lectura.setText(getString(R.string.lbl_tdl_indica_lectura) + globales.is_lectura);
                    if (globales.is_presion.equals(""))
                        tv_anomalia.setText("");
                    else
                        tv_anomalia.setText(getString(R.string.lbl_tdl_indica_anomalia) + globales.is_presion);

                    setDatos(false);
                    int requiereLectura = globales.tll.getLecturaActual().requiereLectura();
                    if (!globales.is_lectura.equals("") &&
                            (requiereLectura == Anomalia.LECTURA_AUSENTE))
                        setModoCaptura();
                    else {
                        salirModoCaptura();
                        if (!globales.bModificar && globales.tll.getLecturaActual().verDatos && !globales.modoCaptura) {
                            setStyleDatosVistos();
                        }
                    }
                    verficarSiPuedoDejarAusente();
                }
                return true;
            }
        };

        View.OnClickListener clicAnomalia = new View.OnClickListener() {

            public void onClick(View v) {
                // mensajeInput(PRESION);
                globales.setEstadoDeLaRepercusion(false,false);
                cancelaTimer();
                Intent anom = new Intent(parent, PantallaAnomaliasActivity.class);
                anom.putExtra("secuencial", globales.il_lect_act);
                anom.putExtra("lectura", globales.is_lectura);
                anom.putExtra("anomalia", globales.tll.getLecturaActual().getAnomaliasCapturadas());
                anom.putExtra("tipoAnomalia", globales.tll.getLecturaActual().is_tipoDeOrden);
                startActivityForResult(anom, ANOMALIA);
                // vengoDeAnomalias = true;
            }
            // tv_lectura.setText(getString(R.string.lbl_tdl_indica_lectura) +globales.is_lectura);
        };

        View.OnLongClickListener longClicAnomalia = new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                globales.setEstadoDeLaRepercusion(false,true);
                cancelaTimer();


                if (/*globales.tll.getLecturaActual().anomalias.size()>1 && */globales.tll.getLecturaActual().getAnomaliasABorrar().respuestas.size() > 1) {
                    //Muestra mensaje
                    anomaliasABorrar(globales.tll.getLecturaActual().getAnomaliasABorrar());
                } else if (globales.tll.getLecturaActual().getAnomaliasABorrar().respuestas.size() == 1) {
//					if (globales.tll.getLecturaActual().anomalias.get(0).is_activa.equals("I")){
//						// si es inactiva, ni la toques.
//						return true;
//					}
                    //Solo hay una, asi que la borramos
                    if (globales.tll.getLecturaActual().deleteAnomalia(globales.tll.getLecturaActual().getAnomaliasAIngresadas()))
                        Toast.makeText(parent, R.string.msj_anomalias_borrada, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(parent, R.string.msj_anomalias_error_borrado, Toast.LENGTH_LONG).show();

                }
                openDatabase();
                db.execSQL("delete from fotos where temporal="
                        + CamaraActivity.ANOMALIA);
                closeDatabase();

                globales.BorrarTodasLosCamposEngie();

                regresaDeBorrar();
                verficarSiPuedoDejarAusente();

                return true;
            }
        };

        View.OnClickListener clicMedidor = new View.OnClickListener() {
            public void onClick(View v) {
                // mensajeInput(PRESION);
                /*
                 * intent.putExtra("tipo", Input.COMENTARIOS);
                 * intent.putExtra("comentarios", is_comentarios);
                 * startActivityForResult(intent, COMENTARIOS);
                 */
                if (!button3.isEnabled())
                    return;
                cancelaTimer();
                //Intent intent = new Intent(parent, BuscarMedidor.class);
                buscarMedidor(BuscarMedidor.BUSCAR);
                globales.moverPosicion = true;
                globales.bEstabaModificando = globales.bModificar;
                globales.tll.guardarDondeEstaba();
            }
        };

        View.OnLongClickListener longClicMedidor = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                // mensajeInput(PRESION);
                /*
                 * intent.putExtra("tipo", Input.COMENTARIOS);
                 * intent.putExtra("comentarios", is_comentarios);
                 * startActivityForResult(intent, COMENTARIOS);
                 */
                if (!button3.isEnabled())
                    return true;
                cancelaTimer();
                //Intent intent = new Intent(parent, BuscarMedidor.class);
                buscarMedidor(BuscarMedidor.MOVER);
                return true;
            }
        };

        tv_anomalia.setClickable(true);
        tv_lectura.setClickable(true);
        tv_caseta.setClickable(true);

        tv_anomalia.setOnClickListener(clicAnomalia);
        tv_lectura.setOnClickListener(clicLectura);
        tv_caseta.setOnClickListener(clicMedidor);

// CE, 12/11/23, Vamos a quitar el funcionamiento del LongClick
//        tv_caseta.setOnLongClickListener(longClicMedidor);
//        tv_anomalia.setOnLongClickListener(longClicAnomalia);
//        tv_lectura.setOnLongClickListener(longClicLectura);

        iv_gps.setOnClickListener(clicGPS);

        button1.setOnClickListener(clicLectura);
        button2.setOnClickListener(clicAnomalia);
        button3.setOnClickListener(clicMedidor);
// CE, 12/11/23, Vamos a quitar el funcionamiento del LongClick
//        button3.setOnLongClickListener(longClicMedidor);

        button3.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(final View view, MotionEvent event) {
                // TODO Auto-generated method stub
                if (!globales.modoCaptura) {


                    if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {

                        /*
                         * Handler handler = new Handler();
                         * handler.postDelayed(new Runnable() { public void
                         * run() {
                         */
                        ((Button) view).setText(R.string.lbl_tdl_mover);
                        /*
                         * } },
                         * android.view.ViewConfiguration.getLongPressTimeout
                         * ());
                         */
                    } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                        // button6.setText(R.string.m_str_siguiente);
                        button3.setText(R.string.str_buscar);

                    }
                }

                return false;
            }

        });

        button4.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(final View view, MotionEvent event) {
                // TODO Auto-generated method stub

                if (!globales.modoCaptura) {
                    if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {

                        /*
                         * Handler handler = new Handler();
                         * handler.postDelayed(new Runnable() { public void
                         * run() {
                         */
                        ((Button) view).setText(R.string.lbl_tdl_primera);
                        /*
                         * } },
                         * android.view.ViewConfiguration.getLongPressTimeout
                         * ());
                         */
                    } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                        // button6.setText(R.string.m_str_siguiente);
                        button4.setText(R.string.m_str_anterior);
                        iv_button4.setVisibility(View.VISIBLE);
                    }
                }

                return false;
            }

        });

        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!globales.modoCaptura) {
                    permiteCerrar();
                    globales.moverPosicion = false;
                    cancelaTimer();
                    if (globales.bModificar)
                        is_mensaje_direccion = getString(R.string.msj_tdl_no_mas_lecturas_ingr_antes);
                    else
                        is_mensaje_direccion = getString(R.string.msj_tdl_no_mas_lecturas_antes);
                    getAntLect();
                    // enviarAvance();
                } else {
                    procesoDeReiniciar();
                }
            }
        });

        button6.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(final View view, MotionEvent event) {
                // TODO Auto-generated method stub
                if (!globales.modoCaptura) {
                    if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                        /*
                         * Handler handler = new Handler();
                         * handler.postDelayed(new Runnable() { public void
                         * run() {
                         */
                        ((Button) view).setText(R.string.lbl_tdl_ultima);
                        /*
                         * } },
                         * android.view.ViewConfiguration.getLongPressTimeout
                         * ());
                         */

                    } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                        button6.setText(R.string.m_str_siguiente);
                        iv_button6.setVisibility(View.VISIBLE);
                    }
                }


                return false;
            }

        });
        button6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!globales.modoCaptura) {
                    globales.moverPosicion = false;
                    cancelaTimer();
                    permiteCerrar();
                    if (globales.bModificar)
                        is_mensaje_direccion = getString(R.string.msj_tdl_no_mas_lecturas_ingr_despues);
                    else
                        is_mensaje_direccion = getString(R.string.msj_tdl_no_mas_lecturas_despues);
                    getSigLect();
                    // enviarAvance();
                } else {
                    capturar();
                }
            }
        });

        button4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                if (button6.getText().equals("Finalizar")) {
                    return true;
                } else {
                    cancelaTimer();
                    if (!globales.modoCaptura) {
                        permiteCerrar();
                        getPrimLect();
                        // enviarAvance();
                    } else {
                        capturar();
                    }
                }
                return true;
            }
        });
        button6.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View arg0) {
                if (button6.getText().equals("Finalizar")) {
                    return true;
                } else {
                    cancelaTimer();
                    if (!globales.modoCaptura) {
                        permiteCerrar();
                        getUltLect();
                    }
                    // enviarAvance();
                    return true;
                }
            }
        });

        button6.setWidth(button2.getWidth());
        button5.setWidth(button3.getWidth());
        button4.setWidth(button1.getWidth());
    }

    public void buscarMedidor(final int tipo) {

        if (preguntaSiBorraDatos /*&& !globales.bModificar*/) {
            if (!globales.tll.getLecturaActual().getAnomaliasCapturadas().equals("") &&
                    !globales.modoCaptura && !globales.tdlg.esSegundaVisita(globales.tll.getLecturaActual().getAnomaliasCapturadas(), globales.tll.getLecturaActual().is_subAnomalia)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog alert;

                //Si ingresé datos, estoy en modo captura y  me estoy moviendo deberia preguntar
                builder.setMessage(R.string.str_pregunta_guardar_cambios)
                        .setCancelable(false).setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //recepcion();
                                preguntaSiBorraDatos = false;
                                preguntaSiBorraDatosComodin = true;
                                buscarMedidor(tipo);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                            }
                        });

                alert = builder.create();
                alert.show();
                return;
            }
        }
        if (TextUtils.isEmpty(globales.strUltimaBusquedaRealizada))
            globales.strUltimaBusquedaRealizada = "";
        Intent intent = new Intent(this, BuscarMedidorActivity.class);
        intent.putExtra("modificar", globales.bModificar);
        intent.putExtra("tipoDeBusqueda", tipo);
        intent.putExtra("ultimabusqueda", globales.strUltimaBusquedaRealizada);
        startActivityForResult(intent, BUSCAR_MEDIDOR);
    }

    public void buscarMedidorCampanita(final int tipo) {
        globales.strUltimaBusquedaRealizada = "Campanita";
        Intent intent = new Intent(this, BuscarMedidorActivity.class);
        intent.putExtra("modificar", globales.bModificar);
        intent.putExtra("tipoDeBusqueda", tipo);
        intent.putExtra("ultimabusqueda", globales.strUltimaBusquedaRealizada);
        startActivityForResult(intent, BUSCAR_MEDIDOR);
    }

    public void enviarAvance() {
        Intent lrs = new Intent(this, trasmisionDatos.class);
        lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
        lrs.putExtra("transmiteFotos", true);
        lrs.putExtra("transmiteVideos", true);
        startActivityForResult(lrs, TRANSMISION);
    }

    protected void setDatos() {
        setDatos(true);
    }

    @SuppressLint("NewApi")
    protected void setDatos(boolean reiniciaValores) {
        try {
            if (reiniciaValores) {
                if (globales.tll.getNumRecords() > 0) {
                    if (!globales.tll.hayMasLecturas()
                            || globales.tll.getLecturaActual() == null) {
                        if (!globales.bModificar) {
                            if (globales.permiteDarVuelta && !globales.bcerrar) {
                                irALaPrimeraSinEjecutarAlTerminar();
                                globales.permiteDarVuelta = false;
                                return;
                            } else {
                                if (!globales.permiteDarVuelta) {
                                    Toast.makeText(this, is_mensaje_direccion,
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        } else {
                            if (globales.sonLecturasConsecutivas
                                    && globales.estoyCapturando) {
                                globales.bcerrar = false;
                                globales.bModificar = false;
                                // tv_indica_corr.setText("N");
                                globales.capsModoCorreccion = false;
                                layout.setBackgroundResource(0);
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
                                && /* !estoyTomandoFotosConsecutivas */!globales.sonLecturasConsecutivas && !globales.capsModoCorreccion) {
                            // finish();
                            //muere();
                            //Llegamos a la ultima lectura...  hay que ir al principio y empezar modo correccion

                            /**
                             * ESTO TENIA ANTES OJO!
                             */
                            //						iniciarModoCorreccionCAPS();
//						globales.permiteDarVuelta = false;

//						if (globales.bcerrar){
                            Toast.makeText(this, "No hay más ordenes", Toast.LENGTH_SHORT)
                                    .show();
//					bEsElFInal = true;
//					if (!ll_lectura.getLectura().equals(""))
//						mandarAImprimir(ll_lectura);
                            // this.finish();
//							iniciarModoCorreccionCAPS();
//					muere();
                            mostrarVentanaDeNoRegistrados();
//						}


//						globales.capsModoCorreccion = true;
//						globales.bModificar = true;
//						layout.setBackgroundResource(R.drawable.correccion_pattern);
//						getPrimLect();
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
                    return;
                }
            }

            button1.setEnabled(true);
            button2.setEnabled(true);
            cv_button1.setEnabled(true);
            cv_button2.setEnabled(true);
            ll_linearLayout2.setVisibility(View.VISIBLE);
            tv_mensaje.setVisibility(View.GONE);
            tv_mensaje2.setVisibility(View.VISIBLE);
            tv_respuesta.setVisibility(View.GONE);

//            iv_campanaNegra.setVisible(!globales.bPrenderCampana);
//            iv_campanaAmarilla.setVisible(globales.bPrenderCampana);

            is_mensaje_direccion = getString(R.string.msj_lecturas_no_hay_mas);

            String ls_comentarios = "";

            globales.permiteDarVuelta = false;
            if (!globales.bModificar) {
                layout.setBackgroundResource(0);
                openDatabase();
//			Cursor c=db.rawQuery("select ultimoSeleccionado from encabezado", null);
//			
//			if (c.getCount()==0){
//				db.execSQL("insert into encabezado (ultimoSeleccionado) values ("+globales.tll.getLecturaActual().secuenciaReal+")");
//			}else{

                db.execSQL("update encabezado set ultimoSeleccionado=" + globales.tll.getLecturaActual().secuenciaReal);
//			}
//			c.close();

                closeDatabase();
            }


            // button1.setEnabled(true);


            globales.il_lect_max = globales.tdlg.getLecturaMaxima();
            globales.il_lect_min = globales.tdlg.getLecturaMinima();
            globales.il_lect_act = globales.tll.getLecturaActual().secuenciaReal;
            //is_comentarios = globales.tll.getLecturaActual().getDireccion();
            globales.is_caseta = globales.tll.getLecturaActual().poliza;

            if (reiniciaValores) {
                globales.is_lectura = globales.tll.getLecturaActual().getLectura();
                globales.is_presion = globales.tll.getLecturaActual().getAnomalia();
                globales.is_terminacion = /*globales.tll.getLecturaActual().terminacion*/ "_1";
                globales.mensaje = globales.tll.getLecturaActual().ls_mensaje;
                ultimaAnomaliaSeleccionada = "";
                ultimaSubAnomaliaSeleccionada = "";
                preguntaSiBorraDatos = false;
                globales.ignorarContadorControlCalidad = false;
                preguntaSiBorraDatosComodin = false;
                globales.ignorarGeneracionCalidadOverride = false;

                //voyATomarFoto=false;
                regreseDe = NINGUNA;
                //captureAnomalias=false;
                //globales.mostrarDatosCompletos=globales.tll.getLecturaActual().verDatos;


                button1.setEnabled(false);
                button2.setEnabled(false);
                cv_button1.setEnabled(false);
                cv_button2.setEnabled(false);

                preguntarHabitado = false;//globales.tll.getLecturaActual().verDatos;
            }

            preguntaRepiteAnomalia();

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
            is_comentarios = is_comentarios != null ? is_comentarios : "";

            // Voy a verificar lo de las lecturas consecutivas
            if (globales.tll.hayMasMedidoresIguales(globales.is_caseta)
                    && !globales.is_caseta.trim().equals("0"))
                globales.sonLecturasConsecutivas = true;
            else
                globales.sonLecturasConsecutivas = false;

            globales.requiereGPS = globales.tll.getLecturaActual().requiereGPS;

            enciendeGPS();

// CE, 01/10/2023, Vamos a cambiar los colores dependiendo del TipoDeOrden
//            tv_caseta.setText(getString(R.string.lbl_tdl_indica_medidor) + globales.is_caseta);
            String strTipoDeMaterial = "";
            if (globales.tll.getLecturaActual().getTipoDeOrden().equals("DESCONEXIÓN")) {
                if (globales.tll.getLecturaActual().is_idMaterialSolicitado.equals("2"))
                    strTipoDeMaterial = " (EX)";
                else
                    strTipoDeMaterial = " (JC)";
            }

            tv_caseta.setText(globales.tll.getLecturaActual().getTipoDeOrden() + strTipoDeMaterial);

            //tv_nombre.setText(globales.tll.getLecturaActual().getNombreCliente());
            //globales.tdlg.getInformacionDelMedidor(ll_generica, globales.tll.getLecturaActual(), sizeGenerico);
            preparaDatosGenericos();
            setDatoEnCardViews();
            tv_nueva_datos_sap2.setText(globales.tll.getLecturaActual().getTipoDeOrden() + strTipoDeMaterial);

            String strContadorOrdenes = "";
            if (globales.bPrenderCampana)
                strContadorOrdenes = "TIENES UNA ACTUALIZACION\n\n";
            strContadorOrdenes += (globales.mostrarRowIdSecuencia ? globales.tll.getLecturaActual().secuenciaReal : globales.il_lect_act) + " de " + globales.il_total;
            tv_contador.setText(strContadorOrdenes);

            tv_contadorOpcional.setText(tv_contador.getText().toString());
            tv_min.setText(String.valueOf(globales.il_lect_min));
            tv_max.setText(String.valueOf(globales.il_lect_max));
            tv_lecturaAnterior.setText(String.valueOf(globales.tll.getLecturaActual().lecturaAnterior));

            // Queremos que los comentarios sean de la siguiente manera Anomalia: ,
            // SubAnomalia \n(todo lo demas)
//		if (!globales.tll.getLecturaActual().getAnomaliaAMostrar().equals("")) {
//			// Tiene una anomalia
//			ls_comentarios =getString(R.string.str_anomalia)+": "  + globales.is_presion;
//			if (!globales.tll.getLecturaActual().getSubAnomaliaAMostrar().equals("")) {
//				// Tiene una subanomalia
//				ls_comentarios += ", " + getString(R.string.str_subanomalia)+": " 
//						+ globales.tll.getLecturaActual().getSubAnomaliaAMostrar();
//			}
//			ls_comentarios += "\n";
//
//		}
//		
//		tv_comentarios.setText(ls_comentarios
//				+ globales.tll.getLecturaActual().getComentarios());

            tv_comentarios.setVisibility(View.GONE);

            if (globales.is_lectura.equals(""))
                tv_lectura.setText("");
            else
                tv_lectura.setText(getString(R.string.lbl_tdl_indica_lectura) + globales.is_lectura);
            if (globales.is_presion.equals(""))
                tv_anomalia.setText("");
            else
                tv_anomalia.setText(getString(R.string.lbl_tdl_indica_anomalia) + (globales.is_presion.length() > 3 ? "***" : globales.is_presion));

            if (globales.mostrarCuadriculatdl) {
                cuadricula.setVisibility(View.VISIBLE);
                //llenar los campos
                tv_campo0.setText(globales.tdlg.obtenerContenidoDeEtiqueta("campo0"));
                tv_campo1.setText(globales.tdlg.obtenerContenidoDeEtiqueta("campo1"));
                tv_campo2.setText(globales.tdlg.obtenerContenidoDeEtiqueta("campo2"));
                tv_campo3.setText(globales.tdlg.obtenerContenidoDeEtiqueta("campo3"));
                tv_campo4.setText(globales.tdlg.obtenerContenidoDeEtiqueta("campo4"));
            } else {
                cuadricula.setVisibility(View.GONE);
            }

            // if (strEsDemanda.equals("5")) {nNumColorAviso = 8; strTextoEspecial =
            // " - " + tipoMedidor.trim(); }
            // if (strEsDemanda.equals("6") || strEsDemanda.equals("7"))
            // {nNumColorAviso = 9; strTextoEspecial = " - " + tipoMedidor.trim(); }
            //

//*******************************************************************************************
// CE, 01/10/2023, Vamos a cambiar los colores dependiendo del TipoDeOrden
/*            if (globales.tll.getLecturaActual().is_tarifa.endsWith("5")) {
                // ll_linearLayout1.setBackgroundColor(R.color.Blue);
                ll_linearLayout1.setBackgroundResource(R.color.Blue);
            } else if (globales.tll.getLecturaActual().is_tarifa.endsWith("6")
                    || globales.tll.getLecturaActual().is_tarifa.endsWith("7")) {
                ll_linearLayout1.setBackgroundResource(R.color.Red);
            } else {
                // ll_linearLayout1.setBackgroundResource(R.color.SteelBlue);
                ll_linearLayout1.setBackgroundResource(R.color.green);
            }*/
// CE, 11/10/2023, Vamos a esconder los elementos del diseño viejo
/*
            if (globales.tll.getLecturaActual().getTipoDeOrden().equals("DESCONEXION")) {
                ll_linearLayout1.setBackgroundResource(R.color.DarkOrange);
            } else if (globales.tll.getLecturaActual().getTipoDeOrden().equals("RECONEXION")) {
                ll_linearLayout1.setBackgroundResource(R.color.green);
            } else if (globales.tll.getLecturaActual().getTipoDeOrden().equals("REMOCION")) {
                ll_linearLayout1.setBackgroundResource(R.color.Red);
            } else {
                ll_linearLayout1.setBackgroundResource(R.color.Blue);
            }
*/
//*******************************************************************************************
            /*
             * int secuencial=(int) globales.il_lect_act; button5.setEnabled(false);
             * Cursor c; //Por ahora no tenemos un objeto de donde tomar las fotos
             * asi que haremos esto...
             *
             * openDatabase();
             *
             *
             *
             * c=db.rawQuery(
             * "Select count(*) canti from fotos where cast(secuencial as Integer)="
             * + secuencial,null);
             *
             * c.moveToFirst();
             *
             * if (c.getInt(c.getColumnIndex("canti"))==0){
             * button5.setEnabled(false); } else{ button5.setEnabled(true); }
             *
             * closeDatabase();
             */

            tieneFotos();

            // button5.setEnabled(false);
            // tv_contador.bringToFront();

            if (esSuperUsuario) {
                ll_limites.setVisibility(View.VISIBLE);
            }


            if (!globales.modoCaptura) {
                salirModoCaptura();
            }

            if (!globales.bModificar && globales.tll.getLecturaActual().verDatos && reiniciaValores && !globales.modoCaptura) {
                setStyleDatosVistos();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            Utils.showMessageLong(this, "Error inesperado. " + t.getMessage());
        }

        if (Build.VERSION.SDK_INT >= 11)

            invalidateOptionsMenu();

        // button6.setEnabled(true);

        try {
            timer.cancel();
        } catch (
                Throwable e) {

        }

        timer.purge();
        timer = new

                Timer();
        timer.schedule(new

                               TimerTask() {

                                   @Override
                                   public void run() {
                                       // TODO Auto-generated method stub
                                       mHandler.post(new Runnable() {
                                           public void run() {
                                               //button6.setEnabled(true);
                                           }
                                       });

                                   }

                               }, 500);

        FormatoDeEtiquetas fde = globales.tdlg.getMensajedeRespuesta();
        if (fde != null) {
            tv_respuesta.setText(fde.texto);
            tv_respuesta.setBackgroundResource(fde.color);
            tv_respuesta.setVisibility(View.VISIBLE);
        }
        String advertencia = globales.tdlg.getMensajedeAdvertencia();
        if (advertencia.equals("")) {
            tv_advertencia.setVisibility(View.GONE);
        } else {
            tv_advertencia.setText(advertencia);
            tv_advertencia.setVisibility(View.VISIBLE);
        }
        this.me = globales.tdlg.getMensaje();
        if (globales.mensaje.equals("") && me != null)
            activaAvisoEspecial(me);
        else if (!globales.mensaje.equals("")) {
            muestraRespuestaSeleccionada(me);
        }
/*
// CE, 09/10/23, En Engie no vamos a usar esta funcionalidad
        //Bloquear botones si es la orden esta pagada o forzada
        if (globales.tll.getLecturaActual().is_estadoDeLaOrden.equals("EO012") || globales.tll.getLecturaActual().is_estadoDeLaOrden.equals("EO005")) {
            button1.setEnabled(false);
            button2.setEnabled(false);
        }
*/
    }

    protected void verficarSiPuedoDejarAusente() {
        if (globales.bModificar) {
            if (globales.is_lectura.equals("") && (globales.is_presion.equals("") || globales.is_presion.endsWith("*"))) {
                setModoCaptura();
            }
        } else {
            if (globales.is_lectura.equals("") && (globales.is_presion.equals(""))) {
                if (!globales.bModificar && globales.tll.getLecturaActual().verDatos && !globales.modoCaptura) {
                    setStyleDatosVistos();
                }
            }
        }


    }

    protected void iniciarModoCorreccionCAPS() {
        // TODO Auto-generated method stub
        //setModoModificacion(false);
        //layout.setBackgroundResource(R.drawable.correccion_pattern);
        setFondoCorreccion();
        super.iniciarModoCorreccionCAPS();

    }

    private void setFondoCorreccion() {
        layout.setBackgroundResource(R.drawable.correccion_pattern);
    }

    public void salirModoCaptura() {
        globales.modoCaptura = false;

// CE, 14/10/23, Vamos a reemplazar este ModoCaptura por el nuevo metodo
        estableceVisibilidadDeBotones(View.INVISIBLE,View.INVISIBLE,View.VISIBLE,View.VISIBLE,View.INVISIBLE,View.VISIBLE,true);
/*
        button4.setVisibility(View.VISIBLE);
        iv_button4.setVisibility(View.VISIBLE);
        iv_button6.setVisibility(View.VISIBLE);
        button4.setEnabled(true);
        button6.setText(R.string.m_str_siguiente);
        button3.setEnabled(true);
        button6.setEnabled(true);
        button1.setEnabled(true);
*/
        button4.setText(R.string.m_str_anterior);
        button6.setText(R.string.m_str_siguiente);
        tv_contador.bringToFront();
    }

    public void capturar() {

        // int respuesta= globales.tll.capturaLectura(globales.is_lectura,
        // globales.tll.getLecturaActual().getAnomalia());


        int respuesta = 1;

        globales.estoyCapturando = true;

        Lectura ll_lectura = globales.tll.getLecturaActual();

        if (respuesta > 0) {
            // Siguiente lectura
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

                // Temporalmente bloqueamos el boton de grabar/siguiente, ya que
                // nos reportan que se guardan las lecturas consecutivamente
                //button6.setEnabled(false);

                globales.requiereLectura = false;
//				if (globales.bModificar)
//					globales.tll.getLecturaActual().intentos++;

                globales.il_ultimoSegReg = globales.il_lect_act;

//*************************************************************************************
// CE, 08/10/23, Vamos a establecer los cambios que necesitamos para los CamposEngie
                if (globales.is_presion.equals(""))
                    ll_lectura.is_Repercusion = "A";
                else
                    ll_lectura.is_Repercusion = globales.is_presion;
                if (!globales.tll.getLecturaActual().is_idMaterialUtilizado.equals(""))
                    globales.tll.getLecturaActual().is_idMaterialUtilizado += ", ";
                globales.tll.getLecturaActual().is_idMaterialUtilizado += globales.getMaterialUtilizado();
                globales.tll.getLecturaActual().is_LecturaReal = globales.is_lectura;
//*************************************************************************************

//				if (captureAnomalias){
//					globales.tdlg.anomaliasARepetir();
//					globales.tdlg.subAnomaliasARepetir();
//					//captureAnomalias=false;
//				}


                //globales.tdlg.cambiosAnomaliaAntesDeGuardar(globales.is_lectura);

                globales.tdlg.anomaliasARepetir();
                globales.tdlg.subAnomaliasARepetir();

                globales.tll.getLecturaActual().ls_mensaje = globales.mensaje;
                globales.tll.guardarLectura(globales.is_lectura);
                globales.tll.getLecturaActual().setPuntoGPS(globales.location);
                globales.modoCaptura = false;
                salirModoCaptura();

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

                if (sonLecturasConsecutivas)
                    asignaAnomaliaConsecutiva(globales.idMedidorUltimaLectura,
                            globales.tll.getLecturaActual().getUltimaAnomalia());

                //Desactivamos el mensaje, la verdad no nos interesa de momento
                preguntaSiBorraDatos = false;
                preguntaSiBorraDatosComodin = false;
                switch (globales.ii_orden) {
                    case ASC:
                        getSigLect();
                        break;
                    case DESC:
                        getAntLect();
                        break;
                }
                //Lo volvemos a mostrar si es necesario
                //preguntaSiBorraDatos=true;

                if ((globales.sonLecturasConsecutivas && !globales.idMedidorUltimaLectura
                        .equals(globales.is_caseta))
                        || (!globales.tll.hayMasLecturas() && sonLecturasConsecutivas)) {
                    tomaFotosConsecutivas(globales.idMedidorUltimaLectura);

                    // Salimos de correccion
                    if (globales.bModificar) {
                        globales.bcerrar = false;
                        globales.bModificar = false;
                        // tv_indica_corr.setText("N");
                        globales.capsModoCorreccion = false;
                        layout.setBackgroundResource(0);
                        // item.setIcon(R.drawable.ic_action_correccion);
                        getSigLect();
                    }
                }

                if (!ll_lectura.getLectura().equals(""))
                    mandarAImprimir(ll_lectura);

                globales.moverPosicion = false;

//				 Intent lrs = new Intent(this, trasmisionDatos.class);
//			 		lrs.putExtra("tipo", trasmisionDatos.TRANSMISION);
//			 		lrs.putExtra("transmiteFotos", true);
//			 		startActivity(lrs);

                enviarAvance();

            } catch (Throwable e) {

                // En caso de que sea la ultima lectura
                if (globales.sonLecturasConsecutivas && globales.bModificar) {

                    // Salimos de correccion
                    if (globales.bModificar) {
                        globales.bcerrar = false;
                        globales.bModificar = false;
                        // tv_indica_corr.setText("N");
                        globales.capsModoCorreccion = false;
                        layout.setBackgroundResource(0);
                        // item.setIcon(R.drawable.ic_action_correccion);
                        getSigLect();
                    }
                } else {
                    // Ya no hay lecturas
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    bEsElFInal = true;
                    if (!ll_lectura.getLectura().equals(""))
                        mandarAImprimir(ll_lectura);
                    // this.finish();
                    muere();
                }

            }

            globales.estoyCapturando = false;

        } else {
            // Mostramos porque
            switch (respuesta) {
                case TodasLasLecturas.FUERA_DE_RANGO:
                    Toast.makeText(this, "Lectura fuera de rango, Verifique.",
                            Toast.LENGTH_SHORT).show();
                    break;
                case TodasLasLecturas.ESFERAS_INCORRECTAS:
                    Toast.makeText(this, "No concuerda el número de esferas.",
                            Toast.LENGTH_SHORT).show();
                    break;
                case TodasLasLecturas.INTENTOS_ACABADOS:
                    Toast.makeText(this, "Se han agotado el número de intentos.",
                            Toast.LENGTH_SHORT).show();
                    break;
                case TodasLasLecturas.VACIA:
                    Toast.makeText(this, "La lectura no puede quedar vacia.",
                            Toast.LENGTH_SHORT).show();
                    break;
                case TodasLasLecturas.INTENTANDO:
                    Toast.makeText(this, "Verifique la lectura.",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        globales.estoyCapturando = false;
        globales.location = null;

    }


//	private int validaLectura(String ls_lectAct) {
//		long ll_lectAct = Long.parseLong(ls_lectAct);
//
//		if (is_lectAnt.equals("")) {
//			if (globales.il_lect_max < ll_lectAct
//					|| globales.il_lect_min > ll_lectAct) {
//				is_lectAnt = ls_lectAct;
//				return FUERA_DE_RANGO;
//			}
//		} else {
//
//			if (!is_lectAnt.equals(ls_lectAct)) {
//				is_lectAnt = ls_lectAct;
//				return VERIFIQUE;
//			}
//
//		}
//
//		is_lectAnt = "";
//		return CORRECTA;
//	}

//	public void mensajeComentarios(View views) {
//		AlertDialog alert = null;
//		LayoutInflater inflater = this.getLayoutInflater();
//
//		final View view = inflater.inflate(R.layout.comentarios, null);
//		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//		final EditText et_comentario = (EditText) view
//				.findViewById(R.id.et_comentarios);
//		final EditText et_problemas = (EditText) view
//				.findViewById(R.id.et_problemas);
//
//		et_comentario.setText(is_comentarios != null ? is_comentarios : "");
//		et_problemas.setText(is_problemas != null ? is_problemas : "");
//
//		final TomaDeLecturas slda = this;
//		builder.setView(view)
//				.setCancelable(false)
//				.setNegativeButton(R.string.cancelar,
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog, int id) {
//								dialog.cancel();
//								esconderTeclado(et_comentario);
//							}
//
//						})
//				.setPositiveButton(R.string.continuar,
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog, int id) {
//
//								is_comentarios = et_comentario.getText()
//										.toString();
//								is_problemas = et_problemas.getText()
//										.toString();
//
//								openDatabase();
//								ContentValues cv_datos = new ContentValues(2);
//								String whereClause = "secuencial=?";
//								String[] whereArgs = { String
//										.valueOf(globales.il_lect_act) };
//
//								cv_datos.put("comentarios",
//										is_comentarios.trim());
//								cv_datos.put("problemas", is_problemas.trim());
//
//								db.update("lecturas", cv_datos, whereClause,
//										whereArgs);
//								closeDatabase();
//								dialog.dismiss();
//								esconderTeclado(et_comentario);
//							}
//
//						});
//
//		alert = builder.create();
//		mostrarTeclado(et_comentario);
//		alert.show();
//	}

//	public void mensajeInput(final int tipo) {
//		AlertDialog alert = null;
//		LayoutInflater inflater = this.getLayoutInflater();
//
//		final View view = inflater.inflate(R.layout.layoutgenerico, null);
//		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//		final EditText et_comentario = (EditText) view
//				.findViewById(R.id.et_generico);
//		final TextView tv_label = (TextView) view.findViewById(R.id.tv_label);
//
//		switch (tipo) {
//		case LECTURA:
//			tv_label.setText(R.string.str_lectura);
//			if (is_lectAnt.equals(""))
//				et_comentario.setText(globales.is_lectura.trim());
//			et_comentario.setInputType(InputType.TYPE_CLASS_NUMBER);
//			break;
//		case PRESION:
//			tv_label.setText(R.string.str_presion);
//			et_comentario.setText(globales.is_presion.trim());
//			et_comentario.setInputType(InputType.TYPE_CLASS_TEXT);
//			break;
//		default:
//			return;
//		}
//
//		final TomaDeLecturas slda = this;
//		builder.setView(view)
//				.setCancelable(false)
//				.setNegativeButton(R.string.cancelar,
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog, int id) {
//								esconderTeclado(et_comentario);
//								dialog.cancel();
//							}
//
//						})
//				.setPositiveButton(R.string.continuar,
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog, int id) {
//								String ls_etiqueta;
//
//								String ls_comentario = et_comentario.getText()
//										.toString();
//								switch (tipo) {
//
//								case LECTURA:
//									if (ls_comentario.equals("")) {
//										return;
//									}
//									if (validaLectura(ls_comentario) != CORRECTA) {
//										mensajeErrorInput(tipo);
//										return;
//									}
//									globales.is_lectura = ls_comentario;
//
//									break;
//								case PRESION:
//									globales.is_presion = ls_comentario;
//									break;
//								default:
//									return;
//								}
//								ls_etiqueta = "L: " + globales.is_lectura;
//								ls_etiqueta += "\t P:" + globales.is_presion;
//								// tv_lectura.setText(ls_etiqueta);
//
//								openDatabase();
//								ContentValues cv_datos = new ContentValues(3);
//								String whereClause = "secuencial=?";
//								String[] whereArgs = { String
//										.valueOf(globales.il_lect_act) };
//
//								cv_datos.put(tipo == LECTURA ? "lectact"
//										: "presion", ls_comentario.trim());
//
//								if (tipo == LECTURA) {
//									cv_datos.put("horadelectura",
//											Main.obtieneFecha());
//								}
//								cv_datos.put("envio", NO_ENVIADA);
//
//								if (tipo == LECTURA)
//									tomarFoto(CamaraActivity.TEMPORAL);
//
//								db.update("lecturas", cv_datos, whereClause,
//										whereArgs);
//								closeDatabase();
//								esconderTeclado(et_comentario);
//								dialog.dismiss();
//							}
//
//						});
//
//		alert = builder.create();
//		mostrarTeclado(et_comentario);
//		alert.show();
//
//	}

//	public void mensajeErrorInput(final int tipo) {
//		final TomaDeLecturas slda = this;
//		String ls_mensaje = "";
//		switch (tipo) {
//		case LECTURA:
//			ls_mensaje = "Verifique lectura";
//			break;
//		}
//		AlertDialog.Builder message = new AlertDialog.Builder(slda);
//		message.setMessage(ls_mensaje)
//				.setCancelable(false)
//				.setPositiveButton(R.string.aceptar,
//						new DialogInterface.OnClickListener() {
//
//							public void onClick(DialogInterface dialog,
//									int which) {
//								mensajeInput(tipo);
//
//							}
//
//						});
//		AlertDialog alerta = message.create();
//		alerta.show();
//	}

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toma_de_lecturas, menu);
        manejaEstadosDelMenu(menu);
        return true;
    }

    @SuppressLint("NewApi")
    public boolean onOptionsItemSelected(final MenuItem item) {
        Intent lrs;
        switch (item.getItemId()) {
            /*
             * case R.id.m_anterior: globales.bcerrar=false; getAntLect(); break;
             */
            case R.id.m_correccion:
                if (preguntaSiBorraDatos /*&& !globales.bModificar*/) {
                    if (!globales.tll.getLecturaActual().getAnomaliasCapturadas().equals("") &&
                            !globales.modoCaptura && !globales.tdlg.esSegundaVisita(globales.tll.getLecturaActual().getAnomaliasCapturadas(), globales.tll.getLecturaActual().is_subAnomalia)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        AlertDialog alert;

                        //Si ingresé datos, estoy en modo captura y  me estoy moviendo deberia preguntar
                        builder.setMessage(R.string.str_pregunta_guardar_cambios)
                                .setCancelable(false).setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //recepcion();
                                        preguntaSiBorraDatos = false;
                                        // getSigLect();
                                        onOptionsItemSelected(item);
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();

                                    }
                                });

                        alert = builder.create();
                        alert.show();
                        return true;
                    }

                }
                TextView tv_indica_corr = (TextView) findViewById(R.id.tv_indica_corr);
                if (globales.bModificar) {
                    globales.bcerrar = false;
                    globales.bModificar = false;
                    tv_indica_corr.setText("N");
                    globales.capsModoCorreccion = false;
                    layout.setBackgroundResource(0);
                    // item.setIcon(R.drawable.ic_action_correccion);
                    globales.modoCaptura = false;
                    salirModoCaptura();
                    getSigLect();
                } else {
                    // String ls_filtrado= formaCadenaFiltrado();
                    globales.bModificar = true;
                    /*
                     * String[]
                     * ls_selectionArgs={String.valueOf(globales.il_lect_act)};
                     * openDatabase();
                     *
                     * String ls_comentarios="";
                     *
                     * if (filtrarComentarios) ls_comentarios=
                     * " and (lectact<>'' or (comentarios<>'' and comentarios is not null)) "
                     * ; else ls_comentarios=" and lectact<>'' ";
                     *
                     *
                     * Cursor c= db.query("lecturas", null,
                     * "cast(secuencial as integer)< cast (? as integer) " +
                     * ls_comentarios + ls_filtrado, ls_selectionArgs, null, null,
                     * "cast (secuencial as Integer) desc", "1"); c.moveToFirst();
                     */
                    setModoModificacion();
                    globales.bcerrar = false;
                    /*
                     * if (globales.bModificar)
                     * item.setIcon(R.drawable.ic_action_salir_correccion); else
                     * item.setIcon(R.drawable.ic_action_correccion);
                     */
                    setDatos();
                    // closeDatabase();
                }
                break;
            case R.id.m_orden:
                switch (globales.ii_orden) {
                    case ASC:
                        // item.setIcon(R.drawable.ic_action_ascendente);
                        globales.ii_orden = DESC;
                        break;
                    case DESC:
                        // item.setIcon(R.drawable.ic_action_descendente);
                        globales.ii_orden = ASC;
                        break;
                }
                break;
            /*
             * case R.id.m_siguiente: globales.bcerrar=false; getSigLect(); break;
             */
/*
            case R.id.m_cliente_ya_pago:
                mostrarVentanaDeClienteYaPago();
                break;
*/
            case R.id.m_noRegistrados:

//			if (globales.il_ultimoSegReg == 0) {
//				Toast.makeText(this, R.string.msj_tdl_ultima,
//						Toast.LENGTH_SHORT).show();
//				return true;
//			}
//
//			Intent intent = new Intent(this, NoRegistrados.class);
//			intent.putExtra("il_ultimoSegReg", globales.il_ultimoSegReg);
//			// vengoDeFotos = true;
//			startActivityForResult(intent, NO_REGISTADOS);
                mostrarVentanaDeNoRegistrados();
                break;
            case R.id.m_cambiarFuente:
                final TomaDeLecturas main = this;
                AlertDialog.Builder builder;
                // String ls_opciones[]={"Ninguno", "Info. del Cliente", "Detalle"};
                String ls_opciones[] = {getString(R.string.lbl_informacion), getString(R.string.lbl_area_de_captura)};
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.msj_tdl_que_ajustar)
                        .setItems(ls_opciones,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        boolean ejecutar = true;
                                        switch (which) {
                                            // case NINGUNO: //Ninguno
                                            // modoCambiofuente= NINGUNO;
                                            // break;
                                            case INFO_CLIENTE: // Info del Cliente
                                                modoCambiofuente = INFO_CLIENTE;
                                                break;
                                            case DETALLE: // detalle
                                                modoCambiofuente = DETALLE;
                                                break;
                                        }
                                        empezarACambiarFuente();
                                    }
                                })
                        .setNegativeButton(R.string.cancelar,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        modoCambiofuente = NINGUNO;
                                    }
                                });
                builder.show();
            case R.id.m_font_areaCaptura:
                modoCambiofuente = DETALLE;
                empezarACambiarFuente();
                break;
            case R.id.m_font_informacion:
                modoCambiofuente = INFO_CLIENTE;
                empezarACambiarFuente();
                break;
            case R.id.m_Impresion:
                bHabilitarImpresion = !bHabilitarImpresion;
            case R.id.m_SolicitarAyuda:
                solicitarEmergencia();
                break;
            case R.id.m_CampanaNegra:
                if (globales.bPrenderCampana)
                    item.setIcon(R.drawable.campana_negra);
//                else
//                    item.setIcon(R.drawable.campana_amarilla);
                globales.bPrenderCampana = false;
                buscarMedidorCampanita(BuscarMedidor.BUSCAR);
                break;
            case R.id.m_MostrarUbicacionGPS:
// CE, 05/11/23, Ahora vamos a mostrar el Mapa de todas las ordenes
//                mostrarUbicacionGPS();
                verMapaDeTodos();
                break;
//            case R.id.m_CapturarVideo:
//                capturarVideo();
//                break;
        }

        if (Build.VERSION.SDK_INT >= 11)
            invalidateOptionsMenu();
        return true;
    }

    private void verMapaDeTodos() {
        String miLatitud = "";
        String miLongitud = "";
        String uri = "";
        try {
            miLatitud = "25.696515021213962";
            miLongitud = "-100.34119561673539";
            if (miLatitud.trim().equals("") || miLongitud.trim().equals(""))
                return;
            TodasLasLecturas tll;
            tll = new TodasLasLecturas(this.getApplicationContext(), 0);// Se buscaran las lecturas desde el principio
            uri = tll.getTodosLosPuntosGPS();
            if (uri.equals("")) {
                Toast.makeText(this, "No hay datos que mostrar en el Mapa", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage("com.google.android.apps.maps");
                this.startActivity(intent);
            }
        } catch (AppSinGps t) {
            mostrarMensaje("Info", "No se tiene una ubicación actual para poder hacer la búsqueda");
        }catch (Throwable t) {
            Utils.showMessageLong(this, t.getMessage());
        }
    }

    private void mostrarUbicacionGPS() {
        Lectura lectura;
        String miLatitud = "";
        String miLongitud = "";
        String serieMedidor = "";
        String uri = "";

        try {
            if (globales == null)
                return;

            if (globales.tll == null)
                return;

            lectura = globales.tll.getLecturaActual();

            if (lectura == null)
                return;

// CE, 01/10/23, Vamos a poner fijo la Geoposicion hasta que nos llegue del servidor
            miLatitud = lectura.getMiLatitud();
            miLongitud = lectura.getMiLongitud();
//            miLatitud = "25.696515021213962";
//            miLongitud = "-100.34119561673539";

            if (miLatitud.trim().equals("") || miLongitud.trim().equals(""))
                return;

            serieMedidor = lectura.getSerieMedidor();

            //uri = "https://maps.google.com/maps/@?api=1&map_action=map&center=" + miLatitud + "," + miLongitud + "&zoom=20";

            uri = "geo:" + miLatitud + "," + miLongitud + "?q=" + miLatitud + "," + miLongitud + "(" + serieMedidor + ")&z=24";

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            this.startActivity(intent);
        } catch (Throwable t) {
            Utils.showMessageLong(this, t.getMessage());
        }
    }

    public void setModoModificacion() {
        setModoModificacion(true);
    }

    public void setModoModificacion(boolean prepararModificacion) {
        TextView tv_indica_corr = (TextView) findViewById(R.id.tv_indica_corr);
        try {
            if (prepararModificacion)
                globales.tll.prepararModificar();
            /*
             * if (globales.tll.hayMasLecturas()) throw new Throwable();
             */

            if (!globales.tll.hayMasLecturas())
                throw new Throwable();
            setFondoCorreccion();
            tv_indica_corr.setText("C");
        } catch (Throwable e) {
            globales.bModificar = false;
        }

    }

    public void esconderTeclado(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void esSospechosa() {
        openDatabase();
        ContentValues cv_datos = new ContentValues(1);
        String whereClause = "secuenciaReal=?";
        String[] whereArgs = {String.valueOf(globales.il_lect_act)};

        cv_datos.put("sospechosa", SOSPECHOSA);

        int i = db.update("lecturas", cv_datos, whereClause, whereArgs);
        closeDatabase();
    }

    public void verFotos(View view) {
        // globales.il_lect_act
        cancelaTimer();
        regreseDe = FOTOS;
        Intent intent = new Intent(this, VerFotos.class);
        intent.putExtra("lect_act", globales.il_lect_act);
        startActivity(intent);
    }

    public boolean onTouchEvent(MotionEvent me) {
        return gestureScanner.onTouchEvent(me);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        // TODO Auto-generated method stub

        if (globales.sesionEntity == null) {
            cerrarActivity();
        }

        if (!globales.modoCaptura /* || globales.is_caseta.contains("CF") */) {
            permiteCerrar();
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    // globales.modoCaptura=false;
                    if (button6.getVisibility() == View.VISIBLE) {
                        globales.moverPosicion = false;
                        getSigLect();
                    }
                    // Left to right swipe
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    // globales.modoCaptura=false;
                    if (button4.getVisibility() == View.VISIBLE) {
                        globales.moverPosicion = false;
                        getAntLect();
                    }
                }
            } catch (Exception e) {
                // nothing
            }
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return gestureScanner.onTouchEvent(ev);
    }

    public void mostrarTeclado(View v) {
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(v, 0);
    }

    public String formaCadenaFiltrado() {
        String ls_cadena = "";
        TextView tv_indica_filtro = (TextView) findViewById(R.id.tv_indica_filtro);
        openDatabase();
        String ls_tmp;
        Cursor c;

        c = db.query("config", null, "key='ciudad'", null, null, null, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            ls_tmp = c.getString(c.getColumnIndex("value"));
            if (ls_tmp.trim().length() > 0) {
                ls_cadena += " and upper(municipio) like '%"
                        + ls_tmp.toUpperCase() + "%' ";
            }

        }

        c.close();

        c = db.query("config", null, "key='medidor'", null, null, null, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            ls_tmp = c.getString(c.getColumnIndex("value"));
            if (ls_tmp.trim().length() > 0) {
                ls_cadena += " and upper(caseta) like '%"
                        + ls_tmp.toUpperCase() + "%' ";
            }

        }

        c.close();

        c = db.query("config", null, "key='cliente'", null, null, null, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            ls_tmp = c.getString(c.getColumnIndex("value"));
            if (ls_tmp.trim().length() > 0) {
                ls_cadena += " and upper(nombre) like '%"
                        + ls_tmp.toUpperCase() + "%' ";
            }

        }

        c.close();

        c = db.query("config", null, "key='direccion'", null, null, null, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            ls_tmp = c.getString(c.getColumnIndex("value"));
            if (ls_tmp.trim().length() > 0) {
                ls_cadena += " and upper(direccion) like '%"
                        + ls_tmp.toUpperCase() + "%' ";
            }

        }

        c.close();

        if (ls_cadena.length() == 0)
            tv_indica_filtro.setVisibility(View.GONE);

        c = db.query("config", null, "key='brincarc'", null, null, null, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            if (c.getInt(c.getColumnIndex("value")) == 1
                    && !globales.bModificar)
                ls_cadena += " and (comentarios='' or comentarios is null) ";
            filtrarComentarios = true;
        }
        c.close();
        closeDatabase();
        return ls_cadena;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        // En este metodo se cambian las opciones del menu
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toma_de_lecturas, menu);

        /*
         * MenuItem mi_correcion=menu.findItem(R.id.m_correccion); MenuItem
         * mi_orden= menu.findItem(R.id.m_orden);
         * mi_correcion.setTitle(globales.
         * bModificar?R.string.m_str_salirCorreccion:R.string.m_str_correccion);
         *
         * if (globales.modoCaptura) mi_correcion.setVisible(false); else
         * mi_correcion.setVisible(true) ;
         *
         * if (globales.bModificar){
         * mi_correcion.setIcon(R.drawable.ic_action_salir_correccion); } else{
         * mi_correcion.setIcon(R.drawable.ic_action_correccion); }
         *
         *
         * if(ii_orden==DESC) mi_orden.setIcon(R.drawable.ic_action_ascendente);
         * else mi_orden.setIcon(R.drawable.ic_action_descendente);
         *
         * mi_orden.setTitle(ii_orden==ASC?R.string.m_str_desendente:R.string.
         * m_str_ascendente);
         */
        manejaEstadosDelMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    public void manejaEstadosDelMenu(Menu menu) {
        MenuItem mi_correcion = menu.findItem(R.id.m_correccion);
        MenuItem mi_noRegistrados = menu.findItem(R.id.m_noRegistrados);
//        MenuItem mi_cliente_ya_pago = menu.findItem(R.id.m_cliente_ya_pago);
        MenuItem mi_orden = menu.findItem(R.id.m_orden);
        MenuItem mi_impresion = menu.findItem(R.id.m_Impresion);
        MenuItem mi_gps = menu.findItem(R.id.m_gps);

        iv_campanaNegra = menu.findItem(R.id.m_CampanaNegra);

        mi_correcion
                .setTitle(globales.bModificar ? R.string.m_str_salirCorreccion
                        : R.string.m_str_correccion);
        mi_impresion
                .setTitle(bHabilitarImpresion ? R.string.m_str_desHabilita_imp
                        : R.string.m_str_habilita_Impresion);

//		if (globales.modoCaptura)
        mi_correcion.setVisible(false);
//		else
//			mi_correcion.setVisible(true);

        if (!globales.capsModoCorreccion) {
            if (globales.bModificar) {
                mi_correcion.setIcon(R.drawable.ic_action_salir_correccion);
                mi_noRegistrados.setVisible(false);
                mi_orden.setVisible(false);
            } else {
                mi_correcion.setIcon(R.drawable.ic_action_correccion);
                if (globales.mostrarNoRegistrados) {
                    mi_noRegistrados.setVisible(true);
                } else {
                    mi_noRegistrados.setVisible(false);
                }
                mi_orden.setVisible(false);
            }
        } else {
            mi_correcion.setVisible(false);
            mi_correcion.setIcon(R.drawable.ic_action_correccion);
            if (globales.mostrarNoRegistrados) {
                mi_noRegistrados.setVisible(true);
            } else {
                mi_noRegistrados.setVisible(false);
            }
        }
        if (globales.mostrarImpresion) {
            if (bHabilitarImpresion) {
                mi_impresion.setIcon(R.drawable.ic_deshabilita_impr);
            } else {
                mi_impresion.setIcon(R.drawable.ic_habilita_impresion);
            }
        } else {
            mi_impresion.setVisible(false);
        }
        if (globales.bPrenderCampana)
            iv_campanaNegra.setIcon(R.drawable.campana_amarilla);
        else
            iv_campanaNegra.setIcon(R.drawable.campana_negra);

        if (globales.ii_orden == DESC)
            mi_orden.setIcon(R.drawable.ic_action_ascendente);
        else
            mi_orden.setIcon(R.drawable.ic_action_descendente);

        mi_orden.setTitle(globales.ii_orden == ASC ? R.string.m_str_desendente
                : R.string.m_str_ascendente);

        if (globales.gpsEncendido) {
            mi_gps.setVisible(false);
        } else {
            mi_gps.setVisible(false);
        }
    }

    public void presentacionAnomalias(boolean anomaliaCapturada, String anomalia, String subAnomalia) {
        // if (anomalia.ii_lectura==1 || anomalia.ii_ausente==0 ){
        // requiereLectura=true;
        // button1.setEnabled(true);
        // }
        // else{
        // requiereLectura=false;
        // button1.setEnabled(false);
        // //if (globales.tll.getLecturaActual().anomalia.ii_lectura==0 ||
        // globales.tll.getLecturaActual().anomalia.ii_ausente==4 ){
        // //Si es ausente, tiene que borrar la lectura...
        // globales.is_lectura="";
        // tv_lectura.setText(getString(R.string.lbl_tdl_indica_lectura) +globales.is_lectura);
        // //}
        // }

        int requiereLectura = globales.tll.getLecturaActual().requiereLectura();
        if (requiereLectura == Anomalia.LECTURA_AUSENTE) {
            globales.requiereLectura = false;
            button1.setEnabled(false);
            cv_button1.setEnabled(false);
            // if (globales.tll.getLecturaActual().anomalia.ii_lectura==0 ||
            // globales.tll.getLecturaActual().anomalia.ii_ausente==4 ){
            // Si es ausente, tiene que borrar la lectura...
            globales.is_lectura = "";
            if (globales.is_lectura.equals(""))
                tv_lectura.setText("");
            else
                tv_lectura.setText(getString(R.string.lbl_tdl_indica_lectura) + globales.is_lectura);
            // }

            setModoCaptura();

        } else if (requiereLectura != Anomalia.SIN_ANOMALIA) {
            //globales.requiereLectura = true;
            button1.setEnabled(true);
            cv_button1.setEnabled(true);
            //Requiere lectura... verificamos que la lectura no este vacia
            if (globales.is_lectura.equals("")) {
                salirModoCaptura();
            }

            if (requiereLectura != Anomalia.LECTURA_OPCIONAL) {
                globales.requiereLectura = false;

            }
        }


        //

        // Manejamos si la anomalia que presento es ausente.. hay que recordar
        // que no lleva lectura
        // if (globales.tll.getLecturaActual().anomalia.ii_lectura==0 ||
        // globales.tll.getLecturaActual().anomalia.ii_ausente==4 ){
        // //Si es ausente, tiene que borrar la lectura...
        // globales.is_lectura="";
        // tv_lectura.setText(getString(R.string.lbl_tdl_indica_lectura) +globales.is_lectura);
        // }
        // Manejamos la foto
        if (globales.tll.getLecturaActual().requiereFotoAnomalia() != 0 && anomaliaCapturada) {
            globales.calidadOverride = globales.tdlg.cambiaCalidadSegunTabla(anomalia, subAnomalia);
            tomarFoto(CamaraActivity.ANOMALIA, globales.tll.getLecturaActual().requiereFotoAnomalia(), anomalia);
        } else {
            avanzarDespuesDeAnomalia();
        }
    }

    public void esconderTeclado() {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(tv_lectura.getWindowToken(), 0);
    }

    private void tieneFotos() {
        int secuencial = (int) globales.il_lect_act;
        button5.setEnabled(false);
        Cursor c; // Por ahora no tenemos un objeto de donde tomar las fotos asi
        // que haremos esto...

        openDatabase();

        c = db.rawQuery(
                "Select count(*) canti from fotos where cast(secuencial as Integer)="
                        + secuencial, null);

        c.moveToFirst();

        if (c.getInt(c.getColumnIndex("canti")) == 0) {
            button5.setEnabled(false);
        } else {
            button5.setEnabled(true);
        }

        closeDatabase();

        // button5.setEnabled(false);

        tv_contador.bringToFront();
    }

    @Override
    public void onBackPressed() {
        if (preguntaSiBorraDatos /*&& !globales.bModificar*/) {
            if (!globales.tll.getLecturaActual().getAnomaliasCapturadas().equals("") &&
                    !globales.modoCaptura && !globales.tdlg.esSegundaVisita(globales.tll.getLecturaActual().getAnomaliasCapturadas(), globales.tll.getLecturaActual().is_subAnomalia)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog alert;

                //Si ingresé datos, estoy en modo captura y  me estoy moviendo deberia preguntar
                builder.setMessage(R.string.str_pregunta_guardar_cambios)
                        .setCancelable(false).setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //recepcion();
                                preguntaSiBorraDatos = false;
                                // getSigLect();
                                muere();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                alert = builder.create();
                alert.show();
                return;
            }
        }
        if (!globales.modoCaptura) {
            muere();
            // finish();
        }
    }

    public void muere() {
        Intent resultado = new Intent();
        resultado.putExtra("bHabilitarImpresion", this.bHabilitarImpresion);
        setResult(Activity.RESULT_OK, resultado);
        //locationManager.removeUpdates(locationListener);
        finish();
    }

    public void cerrarActivity() {
        Intent resultado = new Intent();
        resultado.putExtra("bHabilitarImpresion", this.bHabilitarImpresion);
        setResult(Activity.RESULT_CANCELED, resultado);
        //locationManager.removeUpdates(locationListener);
        finish();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (!permiteCambiarFuente) {
                    return super.dispatchKeyEvent(event);
                }
                reiniciaTimer();
                if (action == KeyEvent.ACTION_UP) {
                    switch (modoCambiofuente) {
                        case DETALLE:
                            porcentaje += factorPorcentaje;
                            break;
                        case INFO_CLIENTE:
                            porcentajeInfoCliente += factorPorcentaje;
                            break;
                    }
                    // porcentaje= getFloatValue("porcentaje", porcentaje);
                    setSizes();
                    openDatabase();
                    guardaValor("porcentaje_lectura", porcentaje);
                    guardaValor("porcentaje_info", porcentajeInfoCliente);
                    closeDatabase();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (!permiteCambiarFuente) {
                    return super.dispatchKeyEvent(event);
                }
                reiniciaTimer();
                if (action == KeyEvent.ACTION_DOWN) {
                    // TODO
                    switch (modoCambiofuente) {
                        case DETALLE:
                            if ((porcentaje - factorPorcentaje) >= .05f) {
                                porcentaje -= factorPorcentaje;
                                // porcentaje= getFloatValue("porcentaje", porcentaje);
                            }
                            break;
                        case INFO_CLIENTE:
                            if ((porcentajeInfoCliente - factorPorcentaje) >= .05f) {
                                porcentajeInfoCliente -= factorPorcentaje;
                                // porcentaje= getFloatValue("porcentaje", porcentaje);
                            }
                            break;
                    }
                    setSizes();
                    openDatabase();
                    guardaValor("porcentaje_lectura", porcentaje);
                    guardaValor("porcentaje_info", porcentajeInfoCliente);
                    closeDatabase();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    public void setSizes() {
        tv_caseta.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentaje * casetaSize));
        tv_lectura.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentaje * lecturaSize));
        tv_mensaje.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentaje * mensajeSize));
        tv_mensaje2.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentaje * mensajeSize));
        tv_anomalia.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentaje * anomSize));
        tv_respuesta.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentaje * respuestasSize));

        tv_nueva_datos_sap1.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * minSize));
        tv_nueva_datos_sap2.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * minSize));

        tv_min.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * minSize));
        tv_max.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * maxSize));
        this.tv_lecturaAnterior.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * maxSize));
        tv_comentarios.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * comentariosSize));
//        tv_advertencia.setTextSize(TypedValue.COMPLEX_UNIT_PX,
//                (float) (porcentajeInfoCliente * comentariosSize));
        tv_campo0.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * comentariosSize));
        tv_campo1.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * comentariosSize));
        tv_campo2.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * comentariosSize));
        tv_campo3.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * comentariosSize));
        tv_campo4.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * comentariosSize));
        label_campo0.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * labelCuadriculaSize));
        label_campo1.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * labelCuadriculaSize));
        label_campo2.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * labelCuadriculaSize));
        label_campo3.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * labelCuadriculaSize));
        label_campo4.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                (float) (porcentajeInfoCliente * labelCuadriculaSize));
//        tv_contadorOpcional.setTextSize(TypedValue.COMPLEX_UNIT_PX,
//                (float) (porcentajeInfoCliente * contadorOpcionalSize));
        sizeGenerico = (float) (porcentajeInfoCliente * comentariosSize);

        //Esto se genera genericamente, asi que hay que rehacerlo
        //globales.tdlg.getInformacionDelMedidor(ll_generica, globales.tll.getLecturaActual(), sizeGenerico);
        try {
            preparaDatosGenericos();
            setDatoEnCardViews();
        } catch (Throwable t) {
            t.printStackTrace();
            Utils.showMessageLong(this, "Error inesperado en setSizes. " + t.getMessage());
        }
    }

    public void setPorcentaje() {
        // openDatabase();
        // db.execSQL("delete from config where key='porcentaje' ");
        // closeDatabase();

        porcentaje = getDoubleValue("porcentaje_lectura", porcentaje);
        porcentajeInfoCliente = getDoubleValue("porcentaje_info", porcentajeInfoCliente);

        // porcentaje=1.0f;
        anomSize = tv_anomalia.getTextSize();
        lecturaSize = tv_lectura.getTextSize();
        mensajeSize = tv_mensaje2.getTextSize();
        casetaSize = tv_caseta.getTextSize();

        minSize = tv_min.getTextSize();
        maxSize = tv_max.getTextSize();
        respuestasSize = tv_respuesta.getTextSize();
        comentariosSize = tv_comentarios.getTextSize();
        labelCuadriculaSize = label_campo0.getTextSize();
        contadorOpcionalSize = tv_contadorOpcional.getTextSize();
        //tipoMedidorSize = tv_tipoMedidor.getTextSize();
        setSizes();
    }

    public int getIntValue(String key, int value) {
        openDatabase();
        Cursor c = db.rawQuery("Select * from config where key='" + key + "'",
                null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            value = c.getInt(c.getColumnIndex("value"));
        } else {
            db.execSQL("Insert into config (key, value) values ('" + key
                    + "', " + value + ")");
        }
        c.close();
        closeDatabase();
        return value;
    }

    public double getDoubleValue(String key, double value) {
        openDatabase();
        Cursor c = db.rawQuery("Select * from config where key='" + key + "'",
                null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            value = c.getDouble(c.getColumnIndex("value"));
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
        db.execSQL("Update config set value=" + value + " where key='" + key + "'");
        // closeDatabase();
    }

    void empezarACambiarFuente() {
        Toast.makeText(
                this,
                R.string.msj_tdl_config_fuente,
                Toast.LENGTH_SHORT).show();
        reiniciaTimer();
    }

    public void reiniciaTimer() {
        final Context contx = this;
        permiteCambiarFuente = true;
        try {
            cambiarFuenteTimer.cancel();
        } catch (Throwable e) {
        }
        cambiarFuenteTimer.purge();
        cambiarFuenteTimer = new Timer();
        cambiarFuenteTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mHandler.post(new Runnable() {
                    public void run() {
                        permiteCambiarFuente = false;
                        modoCambiofuente = NINGUNO;
                        Toast.makeText(contx,
                                R.string.msj_tdl_fin_config_fuente,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }, segundoCambiarFuente * 1000);
    }

    public void cancelaTimer() {
        permiteCambiarFuente = false;
        try {
            cambiarFuenteTimer.cancel();
        } catch (Throwable e) {
        }
        cambiarFuenteTimer.purge();
    }

    public void inicializarVariables() {
        globales.il_ultimoSegReg = 0; //Ultimo medidor guardado
        globales.idMedidorUltimaLectura = "";
        globales.bModificar = false;
        globales.bcerrar = true;
        globales.moverPosicion = false;
        globales.bEstabaModificando = false;
        globales.capsModoCorreccion = false;
        globales.permiteDarVuelta = false;
        globales.sonLecturasConsecutivas = false;
        globales.estoyTomandoFotosConsecutivas = false;
        globales.il_lect_act = 0;
        globales.ii_orden = TomaDeLecturasPadre.ASC;
        globales.lecturasConFotosPendientes = null;
        globales.ii_foto_cons_act = 0;
        globales.estoyCapturando = false;
        //inputMandaCierre=false;
        globales.requiereLectura = false;
        globales.modoCaptura = false;
        globales.fotoForzada = false; // Siempre tomará foto despues de una lectura
        globales.validar = true; // No se validará la lectura
        globales.location = null;//Variable donde se indica todo del gps
    }

    @Override
    protected void capturaDespuesDelPuntoGPS() {
        // TODO Auto-generated method stub
        //Una vez que se salga verificamos que paso...
        //if (globales.location!=null){
        capturar();
        //	}
    }

    @Override
    protected void rutinaDespuesDeTomarFotoDeLlegada(final int requestCode, final int resultCode) {
        if (puedeVerDatosAlRegresar && resultCode == Activity.RESULT_OK) {
            globales.tll.getLecturaActual().verDatos = true;
            setDatos(false);
            openDatabase();
            db.execSQL("update ruta set verDatos=1, fechaDeInicio='" + Main.obtieneFecha("ymdhis") + "' where secuenciaReal=" + globales.il_lect_act);
            closeDatabase();
            setStyleDatosVistos();
            //por ahorita no...
//				if (preguntarHabitado){
//					preguntarHabitado=false;
//				}
        }
        globales.puedoCancelarFotos = false;
        puedeVerDatosAlRegresar = false;
        if (globales.estoyTomandoFotosConsecutivas) {
            tomaFotosConsecutivas(globales.idMedidorUltimaLectura);
        } else {
            tieneFotos();
            //avanzarDespuesDeAnomalia();
        }
        if (regreseDe == ANOMALIA && globales.legacyCaptura) {
            if (globales.tll.getLecturaActual().requiereLectura() == Anomalia.LECTURA_AUSENTE && !globales.tdlg.avanzarDespuesDeAnomalia(ultimaAnomaliaSeleccionada, ultimaSubAnomaliaSeleccionada, false)) {
                capturar();
            } else if (globales.tdlg.avanzarDespuesDeAnomalia(ultimaAnomaliaSeleccionada, ultimaSubAnomaliaSeleccionada, false)) {
                avanzarDespuesDeAnomalia();
            }
        } else if (regreseDe == LECTURA && globales.legacyCaptura) {

//				if (globales.is_terminacion.endsWith("2")){
            capturar();
//				}
//				else{
//					globales.is_terminacion="_2";
////					tomarFoto(CamaraActivity.TEMPORAL, 1);
//				}

            if (Build.VERSION.SDK_INT >= 11)
                invalidateOptionsMenu();

        } else if (regreseDe == LECTURA && !globales.legacyCaptura) {

//				if (!globales.is_terminacion.endsWith("2")){
//					globales.is_terminacion="_2";
////					tomarFoto(CamaraActivity.TEMPORAL, 1);
//				}
//				else{
//					globales.is_terminacion="_1";
//				}
        }
        //regreseDe=FOTOS;
        voyATomarFoto = false;
    }

    public void setDatoEnCardViews() throws Exception {
        Lectura lectura;
        lectura = globales.tll.getLecturaActual();
        if (lectura != null) {

            tv_sap_medidor.setText(globales.tdlg.getDatosSAP(lectura,1));
            tv_sap_cuenta_contrato.setText(globales.tdlg.getDatosSAP(lectura,2));;
            tv_sap_interlocutor.setText(globales.tdlg.getDatosSAP(lectura,3));;
            tv_sap_notificacion.setText(globales.tdlg.getDatosSAP(lectura,4));;

            tv_nueva_datos_cliente.setText(globales.tdlg.getDatosDelCliente(lectura));
            tv_nueva_direccion.setText(globales.tdlg.getDatosDireccion(lectura));

            tv_nueva_datos_sap2.setText(globales.tll.getLecturaActual().getTipoDeOrden());
            if (globales.tll.getLecturaActual().getTipoDeOrden().equals("DESCONEXIÓN")) {
                ll_layoutTipoDeOrden.setBackgroundResource(R.color.EngieDx);
            } else if (globales.tll.getLecturaActual().getTipoDeOrden().equals("RECONEXIÓN")) {
                ll_layoutTipoDeOrden.setBackgroundResource(R.color.EngieRx);
            } else if (globales.tll.getLecturaActual().getTipoDeOrden().equals("REMOCIÓN")) {
                ll_layoutTipoDeOrden.setBackgroundResource(R.color.EngieRm);
            } else {
                ll_layoutTipoDeOrden.setBackgroundResource(R.color.EngieRr);
            }
        }
    }

    private void preparaDatosGenericos() throws Exception {
        Lectura lectura;
        ll_generica.removeAllViews();
//***************************************************************************************
// CE, 22/10/23, Ahora solamente vamos a mostrar TextoLibreSAP en la lista de los campos
        if (!globales.tll.getLecturaActual().is_TextoLibreSAP.equals(""))
            agregarCampo(globales.tll.getLecturaActual().is_TextoLibreSAP);
/*
        lectura = globales.tll.getLecturaActual();

        if (lectura != null) {
            Vector<String> datos = globales.tdlg.getInformacionDelMedidor(lectura);

            if (datos != null) {
                for (String dato : datos) {
                    agregarCampo(dato);
                }
            }
        }
*/
//***************************************************************************************
    }

    private void agregarCampo(String texto) {
        LayoutParams layout_params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        TextView tv_view = new TextView(this);
        tv_view.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeGenerico);
        tv_view.setTextIsSelectable(true);
        tv_view.setText(texto);
        ll_generica.addView(tv_view, layout_params);
    }

    public void activaAvisoEspecial(final MensajeEspecial me) {
        if (me != null) {
            tv_mensaje.setText(me.descripcion);
            tv_mensaje2.setVisibility(View.GONE);
            tv_mensaje.setVisibility(View.VISIBLE);
            tv_mensaje.setBackgroundResource(me.color);

            ll_linearLayout2.setVisibility(View.GONE);

            button1.setEnabled(false);
            button2.setEnabled(false);
            cv_button1.setEnabled(false);
            cv_button2.setEnabled(false);

//			tv_caseta.setVisibility(View.GONE);

            b_repetir_anom.setVisibility(View.GONE);
            switch (me.tipo) {
                case MensajeEspecial.MENSAJE_SI_NO:
                    tv_mensaje.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            preguntaSiNo(me);
                        }
                    });
                    break;
                case MensajeEspecial.OPCION_MULTIPLE:
                    tv_mensaje.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            preguntaOpcionMultiple(me);
                        }
                    });
                    break;
                case MensajeEspecial.SIN_MENSAJE_ESPECIAL:
                    tv_mensaje.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            regresaDeMensaje(me, 0);
                        }
                    });
            }
        }
    }

    public void guardarRespuestaDialog(int respuesta) {

        //Guardamos
        globales.mensaje = me.regresaValor(respuesta);
        //Escondemos mensaje
        tv_mensaje.setVisibility(View.GONE);
        tv_mensaje2.setVisibility(View.VISIBLE);
        //Restablecemos botones
        button1.setEnabled(true);
        button2.setEnabled(true);
        cv_button1.setEnabled(true);
        cv_button2.setEnabled(true);
        ll_linearLayout2.setVisibility(View.VISIBLE);
        muestraRespuestaSeleccionada(me);
    }

    public void muestraRespuestaSeleccionada(final MensajeEspecial me) {
        tv_respuesta.setText(me.regresaDescripcion(globales.mensaje));
        tv_respuesta.setVisibility(View.VISIBLE);

        //Hay que dotarlo de opciones del anterior

        switch (me.tipo) {
            case MensajeEspecial.MENSAJE_SI_NO:
                tv_respuesta.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        preguntaSiNo(me);
                    }

                });
                break;

            case MensajeEspecial.OPCION_MULTIPLE:
                tv_respuesta.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        preguntaOpcionMultiple(me);
                    }

                });

                break;

        }
    }

    public void muestraRespuestaSeleccionadaAutomatica(final MensajeEspecial me) {
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

//	public void preguntaSiNo(MensajeEspecial me){
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(me.descripcion)
//		.setPositiveButton(R.string.No,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int id) {
//								guardarRespuestaDialog(MensajeEspecial.NO);
//							}
//						})
//				.setNegativeButton(R.string.Si,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int id) {
//								guardarRespuestaDialog(MensajeEspecial.SI);
//							}
//						});
//		builder.show();
//	}
//	
//	public void preguntaOpcionMultiple(MensajeEspecial me){
//		
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(me.descripcion).setItems(me.getArregloDeRespuestas(), 
//				new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int id) {
//								guardarRespuestaDialog(id);
//							}
//						});
//		builder.show();
//	}

    public void anomaliasABorrar(final MensajeEspecial me) {

        final TomaDeLecturas tdl = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(me.descripcion).setItems(me.getArregloDeRespuestas(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        if (globales.tll.getLecturaActual().deleteAnomalia(me.regresaValor(id)))
                            Toast.makeText(tdl, R.string.msj_anomalias_borrada, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(tdl, R.string.msj_anomalias_error_borrado, Toast.LENGTH_LONG).show();

                        regresaDeBorrar();
                    }
                });
        builder.show();
    }

    public void regresaDeBorrar() {
        String ls_comentarios = "";
        globales.is_presion = globales.tll.getLecturaActual().getAnomaliaAMostrar();
        if (globales.is_lectura.length() == 0 && globales.tll.getLecturaActual().anomalias.size() == 0) {
            globales.modoCaptura = false;
            salirModoCaptura();
            // borramos fotos temporales anteriores
            openDatabase();

            db.execSQL("delete from fotos where temporal="
                    + CamaraActivity.TEMPORAL);

            closeDatabase();
        } else if (globales.is_lectura.length() == 0 && globales.tll.getLecturaActual().anomalias.size() > 0) {
            presentacionAnomalias(false, "", "");
        } else {
            globales.tll.getLecturaActual().borrarLecturasAusentes();
            globales.is_presion = globales.tll.getLecturaActual().getAnomaliaAMostrar();
            setModoCaptura();
        }

        //No tiene caso poner esto
//		tv_anomalia.setText(getString(R.string.lbl_tdl_indica_anomalia) + (globales.is_presion.length()>3?"***":globales.is_presion));
//		if (globales.tll.getLecturaActual().anomalias.size() != 0) {
//			// Tiene una anomalia
//			ls_comentarios = getString(R.string.str_anomalia)+": "  + globales.tll.getLecturaActual().getAnomaliaAMostrar();
//			if (globales.tll.getLecturaActual().subAnomalias.size() != 0) {
//				// Tiene una subanomalia
//				ls_comentarios += ", "
//						+ getString(R.string.str_subanomalia)+": " 
//						+ globales.tll.getLecturaActual().getSubAnomaliaAMostrar();
//			}
//			ls_comentarios += "\n";


        // //Hay que verificar si la anomalia es ausente
        // if
        // (globales.tll.getLecturaActual().anomalia.ii_lectura==0
        // || globales.tll.getLecturaActual().anomalia.ii_ausente==4
        // ){
        // //Si es ausente, tiene que borrar la lectura...
        // globales.is_lectura="";
        // tv_lectura.setText(getString(R.string.lbl_tdl_indica_lectura) +globales.is_lectura);
        // }
        //	}

        setDatos(false);
//		tv_comentarios.setText(ls_comentarios
//				+ globales.tll.getLecturaActual().getComentarios());
    }

    public void preguntaRepiteAnomalia() {
        if (!globales.repiteAnomalias)
            return;

        if (globales.anomaliaARepetir.equals("") || globales.bModificar || !globales.tdlg.puedoRepetirAnomalia()) {
            b_repetir_anom.setVisibility(View.GONE);
            return;
        }
        if (globales.tll.getLecturaActual().getAnomaliasCapturadas().contains(globales.anomaliaARepetir)) {
            b_repetir_anom.setVisibility(View.GONE);
            return;
        }


        b_repetir_anom.setText(getString(R.string.lbl_tdl_repetirAnomalia) + " (" + globales.anomaliaARepetir + ")");
        b_repetir_anom.setVisibility(View.VISIBLE);

        b_repetir_anom.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Hay que agregar la anomalia...
                globales.tdlg.cambiosAnomalia(globales.anomaliaARepetir);
                globales.tll.getLecturaActual().setAnomalia(globales.anomaliaARepetir);
                globales.tll.getLecturaActual().setSubAnomalia(globales.subAnomaliaARepetir);
                globales.tdlg.repetirAnomalias();
                if (globales.tdlg.esSegundaVisita(globales.anomaliaARepetir, globales.subAnomaliaARepetir)) {
                    //grabamos
                    capturar();
                } else {
                    b_repetir_anom.setVisibility(View.GONE);
                    globales.is_presion = globales.tll.getLecturaActual().getAnomalia();
                    preguntaSiBorraDatos = true;
                    setDatos(false);
                    presentacionAnomalias(false, globales.anomaliaARepetir.substring(globales.anomaliaARepetir.length() - 1, globales.anomaliaARepetir.length()), "");
                }


            }

        });
    }

    @Override
    protected void regresaDeMensaje(MensajeEspecial me, int respuesta) {

        // TODO Auto-generated method stub
        regreseDe = NINGUNA;
        if (me.respondeA == TomaDeLecturasGenerica.PREGUNTAS_UBICACION_VACIA) {
            globales.is_presion = globales.tll.getLecturaActual().getAnomaliaAMostrar();
            setDatos(false);
            preguntaSiBorraDatos = true;
            presentacionAnomalias(true, me.regresaValor(respuesta).substring(0, 1), me.regresaValor(respuesta));
        } else if (me.respondeA == TomaDeLecturasGenerica.ANOMALIA_SEIS) {
            globales.is_presion = globales.tll.getLecturaActual().getAnomaliaAMostrar();
            setDatos(false);
            presentacionAnomalias(true, me.regresaValor(respuesta), "");
        } else if (me.respondeA == TomaDeLecturasGenerica.VER_DATOS) {
            // TODO Auto-generated method stub
//**********************************************************************************************
// CE, 04/10/2023, Vamos a preguntar si esta el Cliente Presente solamente en las Rx y RecRemo
            if ((globales.tll.getLecturaActual().getTipoDeOrden().equals("RECONEXIÓN")) ||
                    (globales.tll.getLecturaActual().getTipoDeOrden().equals("REC/REMO"))) {
                preguntaSiNo(globales.tdlg.mj_habitado);
            } else {
                puedeVerDatosAlRegresar = true;
                globales.puedoCancelarFotos = true;
                openDatabase();
                db.execSQL("update ruta set habitado=0 where secuenciaReal=" + globales.il_lect_act);
                closeDatabase();
                this.tomarFoto(99, 1);
            }
//**********************************************************************************************

//		puedeVerDatosAlRegresar=true;
//		globales.puedoCancelarFotos=true;
//		globales.tll.getLecturaActual().verDatos=true;
//		setDatos(false);
//		
//		openDatabase();
//		db.execSQL("update ruta set verDatos=1 where secuenciaReal="+globales.il_lect_act);
//		closeDatabase();
//		
////		button4.setEnabled(false);
////		button6.setEnabled(false);
////		button3.setEnabled(false);
////		tv_caseta.setVisibility(View.VISIBLE);
//		setStyleDatosVistos();
//		this.tomarFoto(99, 1);

// *************************************************************
// CE, 01/10/23, Vamos a quitar la segunda pregunta
/*        } else if (me.respondeA == TomaDeLecturasGenerica.PREGUNTAS_ESTA_HABITADO) {
            preguntaSiNo(globales.tdlg.mj_registro);
            openDatabase();
            db.execSQL("update ruta set habitado=" + respuesta + " where secuenciaReal=" + globales.il_lect_act);
            closeDatabase();
        } else if (me.respondeA == TomaDeLecturasGenerica.PREGUNTAS_TIENE_REGISTRO) {
            puedeVerDatosAlRegresar = true;
            globales.puedoCancelarFotos = true;
            openDatabase();
            db.execSQL("update ruta set registro=" + respuesta + " where secuenciaReal=" + globales.il_lect_act);
            closeDatabase();
            this.tomarFoto(99, 1);*/
        } else if (me.respondeA == TomaDeLecturasGenerica.PREGUNTAS_ESTA_HABITADO) {
            puedeVerDatosAlRegresar = true;
            globales.puedoCancelarFotos = true;
            globales.tll.getLecturaActual().is_habitado = "" + (respuesta+1);
            if (globales.tll.getLecturaActual().getTipoDeOrden().equals("RECONEXIÓN")) {
                if (respuesta==0)
                    globales.setEstadoDeLaRepercusion(Globales.ENTRO_EFECTIVA_SIN_DATOS_RECONEXION_CLIENTE_PRESENTE_ANTES);
                else
                    globales.setEstadoDeLaRepercusion(Globales.ENTRO_EFECTIVA_SIN_DATOS_RECONEXION_LITRAJE_ANTES);
            } else if (globales.tll.getLecturaActual().getTipoDeOrden().equals("REC/REMO")) {
                if (respuesta==0)
                    globales.setEstadoDeLaRepercusion(Globales.ENTRO_EFECTIVA_SIN_DATOS_REC_REMO_CLIENTE_PRESENTE_ANTES);
                else
                    globales.setEstadoDeLaRepercusion(Globales.ENTRO_EFECTIVA_SIN_DATOS_REC_REMO_LITRAJE_ANTES);
            }
            openDatabase();
            db.execSQL("update ruta set habitado=" + (respuesta+1) + " where secuenciaReal=" + globales.il_lect_act);
            closeDatabase();
            this.tomarFoto(99, 1);
// *************************************************************
        } else {
            preguntaSiBorraDatos = true;
            guardarRespuestaDialog(respuesta);
        }

        preguntaRepiteAnomalia();

    }

    public void setStyleDatosVistos() {
        if (!globales.puedoVerLosDatos) {
//		button4.setEnabled(false);
//		button6.setEnabled(false);
//		button3.setEnabled(false);

// CE, 14/10/23, Vamos a usar un nuevo metodo de Engie
            tv_mensaje2.setText("¿Es Efectiva o No Efectiva?");
            tv_mensaje2.setVisibility(View.VISIBLE);
            estableceVisibilidadDeBotones(View.VISIBLE,View.VISIBLE,View.INVISIBLE,View.INVISIBLE,View.INVISIBLE,View.INVISIBLE,true);
//            button1.setEnabled(true);
//            button2.setEnabled(true);
        }
// CE, 11/10/23, Vamos a esconder los botones del viejo diseño
//        tv_caseta.setVisibility(View.VISIBLE);
        tv_caseta.setVisibility(View.GONE);
    }


    protected void avanzarDespuesDeAnomalia() {
        if (globales.tdlg.avanzarDespuesDeAnomalia(ultimaAnomaliaSeleccionada, ultimaSubAnomaliaSeleccionada, true)) {
            salirModoCaptura();
            //if (captureAnomalias){
            globales.tdlg.anomaliasARepetir();
            globales.tdlg.subAnomaliasARepetir();
            //captureAnomalias=false;
            //}
            preguntaSiBorraDatos = false;
            switch (globales.ii_orden) {
                case ASC:
                    getSigLect();
                    break;
                case DESC:
                    getAntLect();
                    break;
            }
            //preguntaSiBorraDatos=true;
        }
    }

    void mostrarVentanaDeNoRegistrados() {
        if (globales.tdlg == null)
            return;
        Intent intent = new Intent(this, InputCamposGenerico.class);
        intent.putExtra("campos", globales.tdlg.getCamposGenerico("noregistrados"));
        intent.putExtra("label", "");
        intent.putExtra("anomalia", "noregistrados");
        intent.putExtra("titulo", "Nuevo Punto");
        intent.putExtra("boton", "Grabar");
        intent.putExtra("puedoCerrar", true);
        startActivityForResult(intent, NO_REGISTADOS);
    }
/*
    void mostrarVentanaDeClienteYaPago() {
        if (globales.tdlg == null)
            return;
        Intent intent = new Intent(this, InputCamposGenerico.class);
        intent.putExtra("campos", globales.tdlg.getCamposGenerico("cliente_ya_pago"));
        intent.putExtra("label", "");
        intent.putExtra("anomalia", "cliente_ya_pago");
        intent.putExtra("titulo", "Cliente Ya Pagó");
        intent.putExtra("boton", "Continuar");
        intent.putExtra("puedoCerrar", true);
        startActivityForResult(intent, CLIENTE_YA_PAGO);
    }
*/
    private void solicitarEmergencia() {
        enviarSolicitudEmergencia(EmergenciaMgr.EMERGENCIA_PRELIMINAR);
    }

    protected void confirmarEmergencia() {
        if (mAlertEmergencia == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Confirmar ayuda");
            builder.setMessage("¿Está seguro de la ayuda?");
            builder.setCancelable(false);

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    enviarSolicitudEmergencia(EmergenciaMgr.EMERGENCIA_CONFIRMADA);
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    enviarSolicitudEmergencia(EmergenciaMgr.EMERGENCIA_CANCELADA);
                    dialog.dismiss();
                    mEmergenciaMgr = null;
                }
            });

            mAlertEmergencia = builder.create();
        }
        mAlertEmergencia.show();
    }

    protected void enviarSolicitudEmergencia(int solicitudEmergencia) {
        if (globales == null) {
            Utils.showMessageLong(this, "Error al solicitar ayuda. Contacte soporte técnico.");
            return;
        }

        if (globales.sesionEntity == null) {
            Utils.showMessageLong(this, "No se ha autenticado en la aplicación");
            return;
        }

        if (globales.sesionEntity.empleado == null) {
            Utils.showMessageLong(this, "No se ha autenticado en la aplicación");
            return;
        }

        if (mEmergenciaMgr == null) {
            mEmergenciaMgr = new EmergenciaMgr(this);

            mEmergenciaMgr.setEmergenciaCallback(new EmergenciaMgr.EmergenciaCallback() {
                @Override
                public void enExito(OperacionResponse resp, int solicitudEmergenciaResultado) {
                    if (solicitudEmergenciaResultado == EmergenciaMgr.EMERGENCIA_PRELIMINAR)
                        confirmarEmergencia();
                }

                @Override
                public void enFallo(OperacionResponse resp) {
                    Utils.showMessageLong(TomaDeLecturas.this, resp.MensajeError);
                }
            });
        }

        switch (solicitudEmergencia) {
            case EmergenciaMgr.EMERGENCIA_PRELIMINAR:
                Utils.showMessageLong(TomaDeLecturas.this, "Fue enviada la solicitud de emergencia");
                break;
            case EmergenciaMgr.EMERGENCIA_CONFIRMADA:
                Utils.showMessageLong(TomaDeLecturas.this, "Fue enviada la confirmación de emergencia");
                break;
            case EmergenciaMgr.EMERGENCIA_CANCELADA:
                Utils.showMessageLong(TomaDeLecturas.this, "Fue enviada la cancelación de emergencia");
                break;
        }

        mEmergenciaMgr.enviarSolicitudEmergencia(globales.sesionEntity, globales.location, solicitudEmergencia);
    }

    private void capturarVideo() {
        Lectura lectura;
        long idOrden;

        if (globales == null)
            return;

        if (globales.tll == null)
            return;

        lectura = globales.tll.getLecturaActual();

        if (lectura == null)
            return;

        idOrden = lectura.is_idOrden;

        Intent video = new Intent(this, Camara2Activity.class);
        video.putExtra("idOrden", idOrden);
        startActivityForResult(video, TOMAR_VIDEO);
    }

    private void estableceVisibilidadDeBotones(int b1, int b2, int b3, int b4, int b5, int b6, boolean bMostrarFlecha){
        button1.setEnabled(b1==View.VISIBLE);
        button2.setEnabled(b2==View.VISIBLE);
        button3.setEnabled(b3==View.VISIBLE);
        button4.setEnabled(b4==View.VISIBLE);
        button5.setEnabled(b5==View.VISIBLE);
        button6.setEnabled(b6==View.VISIBLE);
        button1.setVisibility(b1);
        button2.setVisibility(b2);
        button3.setVisibility(b3);
        button4.setVisibility(b4);
        button5.setVisibility(b5);
        button6.setVisibility(b6);

        cv_button1.setEnabled(b1==View.VISIBLE);
        cv_button2.setEnabled(b2==View.VISIBLE);

        iv_button3.setVisibility(b3);
        iv_button4.setVisibility(b4);
        iv_button5.setVisibility(b5);
        if (bMostrarFlecha) {
            iv_button4.setVisibility(b6);
            iv_button6.setVisibility(b6);
        } else {
            iv_button4.setVisibility(View.INVISIBLE);
            iv_button6.setVisibility(View.INVISIBLE);
        }
    }

    private void procesoDeReiniciar() {
        globales.setEstadoDeLaRepercusion(true,true);
        cancelaTimer();
        // TODO Auto-generated method stub
// CE, 13/12/23, Aqui debemos revisar si es Efectiva o No Efectiva
        boolean bEsEfectiva = (globales.tll.getLecturaActual().anomalias.size() == 0);
        if (bEsEfectiva) {
            cancelaTimer();
            globales.is_lectura = "";
            globales.BorrarTodasLosCamposEngie();
            globales.tdlg.regresaDeBorrarLectura();

            if (globales.tll.getLecturaActual().anomalias.size() == 0) {
                globales.modoCaptura = false;
                salirModoCaptura();
                // borramos fotos temporales anteriores
                openDatabase();

                db.execSQL("delete from fotos where temporal="
                        + CamaraActivity.TEMPORAL);

                closeDatabase();

                //tv_lectura.setText(getString(R.string.lbl_tdl_indica_lectura) + globales.is_lectura);
            }
            globales.is_presion = globales.tll.getLecturaActual().getAnomaliaAMostrar();

            if (globales.is_lectura.equals(""))
                tv_lectura.setText("");
            else
                tv_lectura.setText(getString(R.string.lbl_tdl_indica_lectura) + globales.is_lectura);
            if (globales.is_presion.equals(""))
                tv_anomalia.setText("");
            else
                tv_anomalia.setText(getString(R.string.lbl_tdl_indica_anomalia) + globales.is_presion);

            setDatos(false);
            int requiereLectura = globales.tll.getLecturaActual().requiereLectura();
            if (!globales.is_lectura.equals("") &&
                    (requiereLectura == Anomalia.LECTURA_AUSENTE))
                setModoCaptura();
            else {
                salirModoCaptura();
                if (!globales.bModificar && globales.tll.getLecturaActual().verDatos && !globales.modoCaptura) {
                    setStyleDatosVistos();
                }
            }
            verficarSiPuedoDejarAusente();
        } else {
            globales.setEstadoDeLaRepercusion(false,true);
            cancelaTimer();
            if (/*globales.tll.getLecturaActual().anomalias.size()>1 && */globales.tll.getLecturaActual().getAnomaliasABorrar().respuestas.size() > 1) {
                //Muestra mensaje
                anomaliasABorrar(globales.tll.getLecturaActual().getAnomaliasABorrar());
            } else if (globales.tll.getLecturaActual().getAnomaliasABorrar().respuestas.size() == 1) {
                if (globales.tll.getLecturaActual().deleteAnomalia(globales.tll.getLecturaActual().getAnomaliasAIngresadas()))
                    Toast.makeText(this, R.string.msj_anomalias_borrada, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, R.string.msj_anomalias_error_borrado, Toast.LENGTH_LONG).show();
            }
            openDatabase();
            db.execSQL("delete from fotos where temporal="
                    + CamaraActivity.ANOMALIA);
            closeDatabase();
            globales.BorrarTodasLosCamposEngie();
            regresaDeBorrar();
            verficarSiPuedoDejarAusente();
        }
    }

    private void mostrarMensaje(String titulo, String mensaje) {
        if (mDialogoMsg == null) {
            mDialogoMsg = new DialogoMensaje(this);
        }

        mDialogoMsg.mostrarMensaje(titulo, mensaje, "");
    }
}
