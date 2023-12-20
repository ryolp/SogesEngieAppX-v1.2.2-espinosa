package enruta.soges_engie;

import android.app.Activity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class PantallaAnomaliasActivity extends Activity {
    DBHelper dbHelper;
    SQLiteDatabase db;
    ListView lv_lista;
    TextView tv_mensaje;
    RelativeLayout rl_busquedaManual;
    String tipoAnomalia="";
    int tipo;

    ImageButton b_clearText;

    boolean tieneSubanomalia = false;
    boolean tieneMensaje = false;

    String is_desc = "";
    String is_anomaliaSelec="";
    String is_subAnomSelect="";
    String is_comentarios="";
    Cursor c;

    EditText li_anomalia;
    TextView tv_label;

    Globales globales;
    String anomaliaTraducida;
    String is_anomalia;
    int ii_secuencial;
    String is_lectura="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anomalias_fragment);
        globales = ((Globales) getApplicationContext());

        setTitle("Repercusiones No Efectivas");

        //Verificamos si la entrada de anomalias debe ser de numeros o tambien letras
        anomaliaTraducida = globales.traducirAnomalia();

        Bundle bu_params = getIntent().getExtras();
        ii_secuencial=bu_params.getInt("secuencial");
        try{
            is_lectura=bu_params.getString("lectura");
        }catch(Throwable e){

        }
        is_anomalia=bu_params.getString("anomalia");

        tipo = bu_params.getInt("tipo");
        tipoAnomalia = globales.tll.getLecturaActual().is_tipoDeOrden;

        if (tipoAnomalia.equals("TO003")){   // Reconexion
            tipoAnomalia="I";
        }
        else if (tipoAnomalia.equals("TO004")){   // Rec-Remo
            tipoAnomalia="R";
        }
        else{
            tipoAnomalia="M";
        }
        b_clearText=(ImageButton) findViewById(R.id.im_clearText);
        lv_lista = (ListView) findViewById(R.id.anom_lv_lista);
        tv_label = (TextView) findViewById(R.id.tv_label);
        tv_mensaje= (TextView) findViewById(R.id.tv_mensaje);
        rl_busquedaManual = (RelativeLayout) findViewById(R.id.rl_busquedaManual);

        li_anomalia = (EditText) findViewById(R.id.anom_et_anomalia);
        li_anomalia.setText(bu_params.getString("anomalia"));

        b_clearText.setOnClickListener(new OnClickListener(){
           @Override
            public void onClick(View view) {
                //Borramos el texto
                li_anomalia.getText().clear();
            }
        });
//Hay que borrarse los tabs, asi que se tiene que volver a llamar a toda la rutina de inicializacion
        reinicializaTAB();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bu_params;
        switch(requestCode){
            case TomaDeLecturas.COMENTARIOS:
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
                    if (tieneSubanomalia){
                        tieneSubanomalia=false;
                        selectAnomalia(is_anomaliaSelec);
                    }
                    else{
                        reinicializaTAB();
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
                    if (tieneSubanomalia){
                        tieneSubanomalia=false;
                        selectAnomalia(is_anomaliaSelec);
                    }
                    else{
                        reinicializaTAB();
                    }
                }
                break;
        }
    }

    @SuppressLint("NewApi")
    public void selectAnomalia(String ls_anomalia) {
        ls_anomalia = ls_anomalia.toUpperCase();
        String[] args = { ls_anomalia };
        String query="";
        // Buscamos si la anomalia tiene una sub anomalia

        if (!tieneSubanomalia){
            if (!globales.tdlg.esAnomaliaCompatible(ls_anomalia, is_anomalia) ){
                mensajeOK(this.getString(R.string.msj_anomalias_no_compatible));
                return;
            }
        }
        String ls_mensaje = globales.tdlg.validaAnomalia(is_anomaliaSelec);
        if (!ls_mensaje.equals("")){
            mensajeOK(ls_mensaje);
            return;
        }
        openDatabase();
        li_anomalia.setText("");
        c.close();
        if (!tieneSubanomalia) {
            query="select mens , capt from anomalia where " + anomaliaTraducida + "='"
                    + is_anomaliaSelec +"'";
        }
        else{
            query="select mens, capt from anomalia where substr(desc, 1, " + globales.longitudCodigoSubAnomalia + ")='"
                    + is_subAnomSelect+"'";
        }
        c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.getInt(c.getColumnIndex("mens")) == 1 || c.getInt(c.getColumnIndex("capt")) == 1 ) {
            tieneMensaje = true;
        }
        c.close();
        c = db.rawQuery(
                "select rowid _id, " + anomaliaTraducida + " anom, desc desc from anomalia where " + anomaliaTraducida + "=? and subanomalia='S' and activa='A'",
                args);
        if (c.getCount() > 0 && !tieneSubanomalia && tieneMensaje) {
            // c.moveToFirst();
            tieneSubanomalia = true;
            tv_label.setText(ls_anomalia + " - " + is_desc);
            tv_label.setVisibility(View.VISIBLE);

            String[] columns = new String[] { "desc" };
            ListAdapter adapter;
            adapter = new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_1, c, columns,
                    new int[] { android.R.id.text1 });
            lv_lista.setAdapter(adapter);
        } else {
            if (tieneMensaje){
                //Hay que saber cual es la que se va a abrir, input o inputCampos generico
                //Para eso le preguntaremos a generica si tiene varios campos
                int [] campos = globales.tdlg.getCamposGenerico(is_anomaliaSelec);
                if (!tieneSubanomalia) {
                    c = db.rawQuery(
                            "select mens, desc, capt, " + anomaliaTraducida + " anomalia from anomalia where " + anomaliaTraducida + " ='"
                                    + is_anomaliaSelec+"'", null);
                }
                else{
                    c = db.rawQuery(
                            "select mens, desc, capt, " + anomaliaTraducida + " anomalia from anomalia where substr(desc, 1, " + globales.longitudCodigoSubAnomalia + ")='"
                                    + is_subAnomSelect+"'", null);
                }
                c.moveToFirst();
                String ls_indicadorMensaje = globales.tdlg.remplazaValorDeArchivo(TomaDeLecturasGenerica.MENSAJE, !tieneSubanomalia? is_anomaliaSelec:is_subAnomSelect,  String.valueOf(c.getInt(c.getColumnIndex("mens"))));
                if (campos==null && (ls_indicadorMensaje.equals("1") || c.getInt(c.getColumnIndex("capt"))==1)){
                    // Abrimos input
                    Intent intent = new Intent(this, Input.class);
                    intent.putExtra("tipo", Input.COMENTARIOS);
                    intent.putExtra("comentarios", "");
                    intent.putExtra("anomaliaquepidelectura", c.getString(c.getColumnIndex("anomalia")));

                    String strEscribaSusComentarios = "";
                    if (c.getString(c.getColumnIndex("anomalia")).equals("E"))
                        strEscribaSusComentarios="\nESCRIBA LA LECTURA DEL MEDIDOR: \n\n";
                    else
                        strEscribaSusComentarios="\nESCRIBA SUS COMENTARIOS PARA LA REPERCUSION: \n\n" + c.getString(c.getColumnIndex("desc")) + "\n\n";
                    // Con esto generamos la etiqueta que tendra el input
                    intent.putExtra("label",strEscribaSusComentarios	+ "");

                    //Aqui mandamos el comportamiento de input, en otras palabras, le daremos la anomalia para que pueda configurarlo como se le de la gana
                    intent.putExtra("behavior", is_anomaliaSelec);
                    // Tambien debo mandar que etiqueta quiero tener
                    startActivityForResult(intent, TomaDeLecturas.COMENTARIOS);
                }
                else if (campos!=null && (c.getInt(c.getColumnIndex("mens"))==1 || c.getInt(c.getColumnIndex("capt"))==1)){
/*
                    //Tiene mas datos a guardar
                    Intent intent = new Intent(this, InputCamposGenerico.class);
                    intent.putExtra("campos",campos);
                    intent.putExtra("label", c.getString(c.getColumnIndex("anomalia")) + " - "
                            + c.getString(c.getColumnIndex("desc"))
                            + "\n");
                    intent.putExtra("anomalia", c.getString(c.getColumnIndex("anomalia")));
                    startActivityForResult(intent, TomaDeLecturas.INPUT_CAMPOS_GENERICO);
 */
                    Intent intent = new Intent(this, ClienteYaPago.class);
                    intent.putExtra("campos",campos);
                    intent.putExtra("label", c.getString(c.getColumnIndex("anomalia")) + " - "
                            + c.getString(c.getColumnIndex("desc"))
                            + "\n");
                    intent.putExtra("anomalia", c.getString(c.getColumnIndex("anomalia")));
                    startActivityForResult(intent, TomaDeLecturas.INPUT_CLIENTE_YA_PAGO);
                }else{
                    mandarAnomalia();
                }
            }else{
                globales.tdlg.RealizarModificacionesDeAnomalia(is_subAnomSelect);
                mandarAnomalia();
            }
        }
        closeDatabase();
    }

    public void mandarAnomalia(){
        Intent intent= new Intent();
        int longAnomalia;
        globales.tdlg.RealizarModificacionesDeAnomalia(is_subAnomSelect);
        if (globales.convertirAnomalias)
            longAnomalia=this.getResources().getInteger(R.integer.ANOM_LONG_CONV);
        else
            longAnomalia=globales.tlc.getLongCampo("anomalia");

        String ls_anomalia= is_anomaliaSelec;

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

    public void getAnomaliaEditText(View view) {
        boolean existe = false;
        esconderTeclado();
        if (li_anomalia.getText().toString().equals("")){
            mensajeOK(this.getString(R.string.msj_anomalias_no_valida));
            return;
        }
        // Hay que validar...
        if (tieneSubanomalia
                && li_anomalia.getText().toString().trim().length() < globales.longitudCodigoSubAnomalia) {
            mensajeOK(String.format(getString(R.string.msj_anomalias_validacion_subavisos), String.valueOf(globales.longitudCodigoSubAnomalia)));
            return;
        }
        else if (!tieneSubanomalia && li_anomalia.getText().toString().trim().equals("0")) {
            borrarAnomalia();
            return;
        }
        openDatabase();
        // Mas validaciones
        if (!tieneSubanomalia) {
            String query = "Select rowid _id, " + anomaliaTraducida + " anom , desc desc from anomalia where subanomalia<>'S' and subanomalia<>'A' and activa='A' and " + anomaliaTraducida + "=";
            c = db.rawQuery(query + "'" + li_anomalia.getText().toString().trim().toUpperCase() + "'",
                    null);
            is_anomaliaSelec = li_anomalia.getText().toString().trim();
        }
        else {
            String query = "Select rowid _id, " + anomaliaTraducida + " anom , desc desc from anomalia where subanomalia='S' and subanomalia<>'A' and activa='A' and " + anomaliaTraducida + "='";
            is_subAnomSelect = li_anomalia.getText().toString().trim().toUpperCase();
            c = db.rawQuery(query + is_anomaliaSelec
                    + "' and substr(desc, 1, " + globales.longitudCodigoSubAnomalia + ")  ='"
                    + is_subAnomSelect + "'", null);
        }
        if (c.getCount() > 0) {
            existe = true;
            c.moveToFirst();
            is_desc = c.getString(c.getColumnIndex("desc"));
        }
        c.close();
        closeDatabase();
        if (existe) {
            selectAnomalia(li_anomalia.getText().toString());
            li_anomalia.setText("");
        } else {
            mensajeOK(this.getString(R.string.msj_anomalias_no_valida));
        }
    }

    public void borrarAnomalia(){
        Intent intent= new Intent();
        intent.putExtra("anomalia", "");
        intent.putExtra("subAnomalia", "");
        intent.putExtra("comentarios", "");
        setResult(Activity.RESULT_OK, intent);
        this.finish();
    }

    public void esconderTeclado() {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(li_anomalia.getWindowToken(), 0);
    }

    public void openDatabase(){
        dbHelper= new DBHelper(this);
        db = dbHelper.getReadableDatabase();
    }

    public void closeDatabase(){
        db.close();
        dbHelper.close();
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

    public void reinicializaTAB(){
        tv_label.setVisibility(View.GONE);
        tieneSubanomalia=false;
        tieneMensaje=false;
        openDatabase();
        String ls_filtro = getFiltro();
        lv_lista.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (!tieneSubanomalia) {
                    TextView tv_anomalia = (TextView) view
                            .findViewById(android.R.id.text1);
                    is_desc = ((TextView) view
                            .findViewById(android.R.id.text2)).getText()
                            .toString();
                    String ls_anomalia = tv_anomalia.getText().toString();
                    is_anomaliaSelec = ls_anomalia;
                    selectAnomalia(ls_anomalia);
                } else {
                    TextView tv_anomalia = (TextView) view
                            .findViewById(android.R.id.text1);
                    String ls_anomalia = tv_anomalia.getText().toString()
                            .substring(0, globales.longitudCodigoSubAnomalia);
                    is_subAnomSelect = ls_anomalia;
                    selectAnomalia(ls_anomalia);
                }
            }
        });

        c = db.rawQuery(
                "Select rowid _id, " + anomaliaTraducida + " anom , desc desc from anomalia where subanomalia<>'S' and subanomalia<>'A' and activa='A' and tipo='" + tipoAnomalia + "' "
                        + ls_filtro, null);
        c.moveToFirst();
        String[] columns = new String[] { "anom", "desc" };
        ListAdapter adapter;
        adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, c, columns, new int[] {
                android.R.id.text1, android.R.id.text2 });
        lv_lista.setAdapter(adapter);
        li_anomalia.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView arg0, int arg1,
                                          KeyEvent arg2) {
                // Si le damos al teclado mostramos
                getAnomaliaEditText(arg0);
                return false;
            }
        });

        if (globales.convertirAnomalias)
            li_anomalia.setInputType(/*InputType.TYPE_CLASS_TEXT|*/InputType.TYPE_TEXT_FLAG_CAP_WORDS| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        else
            li_anomalia.setInputType(InputType.TYPE_CLASS_NUMBER);
        closeDatabase();
    }
}
