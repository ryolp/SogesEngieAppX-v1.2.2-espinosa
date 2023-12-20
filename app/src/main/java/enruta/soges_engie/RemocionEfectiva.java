package enruta.soges_engie;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Vector;

import enruta.soges_engie.Configuracion.Entry;

public class RemocionEfectiva extends AppCompatActivity {

    public final static int CLIENTE_YA_PAGO_MONTO = 13;
    public final static int CLIENTE_YA_PAGO_FECHA = 14;
    public final static int CLIENTE_YA_PAGO_AGENTE = 15;

    public final static int REMOCION_MARCA_MEDIDOR = 16;
    public final static int REMOCION_SERIE_MEDIDOR = 17;
    public final static int REMOCION_TUBERIA = 18;
    public final static int REMOCION_OBSERVACIONES = 19;

    Vector<Entry> objetosAMostar;
    View view;
    Spinner spinnerSiNo;
    RelativeLayout rv_remo_combo_marcas;
    RelativeLayout rv_remo_combo_serie;
    RelativeLayout rv_remo_combo_tuberia;

    DBHelper dbHelper;
    SQLiteDatabase db;
    Globales globales;

    String is_anomalia="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remocion_efectiva);

        view= findViewById(R.id.ll_remocion_efectiva);

        globales = ((Globales) getApplicationContext());
        Button button= (Button) findViewById(R.id.btn_remo_continuar);
        button.setVisibility(View.VISIBLE);

        button.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (guardar())
                    finish();
            }
        });
        llenarSiNo();
        llenarMarcas();
        llenarTuberia();
    }

    private void llenarSiNo(){
        spinnerSiNo = (Spinner) findViewById(R.id.sv_si_o_no);
        rv_remo_combo_marcas = (RelativeLayout) findViewById(R.id.rv_remo_combo_marcas);
        rv_remo_combo_serie = (RelativeLayout) findViewById(R.id.rv_remo_combo_serie);
        rv_remo_combo_tuberia = (RelativeLayout) findViewById(R.id.rv_remo_combo_tuberia);
        ArrayList<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("SI");
        spinnerArray.add("NO");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSiNo.setAdapter(adapter);

        spinnerSiNo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
                if (spinnerSiNo.getItemAtPosition(pos).toString().equals("SI")) {
                    rv_remo_combo_marcas.setVisibility(View.VISIBLE);
                    rv_remo_combo_serie.setVisibility(View.GONE);
                    rv_remo_combo_tuberia.setVisibility(View.GONE);
                } else {
                    rv_remo_combo_marcas.setVisibility(View.GONE);
                    rv_remo_combo_serie.setVisibility(View.GONE);
                    rv_remo_combo_tuberia.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void llenarMarcas(){
        Spinner spinner = (Spinner) findViewById(R.id.sv_remo_marcas);
        ArrayList<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("HP Meter Italian");
        spinnerArray.add("ITRON");
        spinnerArray.add("ELSTER");
        spinnerArray.add("AMCO ELSTER");
        spinnerArray.add("GALLUS");
        spinnerArray.add("KROMSCHRODER");
        spinnerArray.add("GOLDSTAR");
        spinnerArray.add("BK");
        spinnerArray.add("YASAKI");
        spinnerArray.add("OTRA");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void llenarTuberia(){
        Spinner spinner = (Spinner) findViewById(R.id.sv_remo_tuberia);
        ArrayList<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("Regulador");
        spinnerArray.add("Yugo");
        spinnerArray.add("Tramo de Tubería");
        spinnerArray.add("Corte de Banqueta");
        spinnerArray.add("Otro");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private boolean guardar(){
        Intent intent= new Intent();
        String texto="";
        String dbField="";

        if (spinnerSiNo.getSelectedItem().toString().equals("SI")) {
            Spinner sp_view = (Spinner) view.findViewById(R.id.sv_remo_marcas);
            texto = String.valueOf(sp_view.getSelectedItem());
            dbField = String.valueOf(REMOCION_MARCA_MEDIDOR);
            intent.putExtra(dbField, texto);

//            EditText et_view = (EditText) view.findViewById(R.id.ev_remo_serie);
//            texto += " - " + et_view.getText().toString();
            dbField = String.valueOf(REMOCION_SERIE_MEDIDOR);
            intent.putExtra(dbField, "");

            dbField = String.valueOf(REMOCION_TUBERIA);
            intent.putExtra(dbField, "");
        } else {
            dbField = String.valueOf(REMOCION_MARCA_MEDIDOR);
            intent.putExtra(dbField, "");

            dbField = String.valueOf(REMOCION_SERIE_MEDIDOR);
            intent.putExtra(dbField, "");

            Spinner sp_view2 = (Spinner) view.findViewById(R.id.sv_remo_tuberia);
            texto += " - " + String.valueOf(sp_view2.getSelectedItem());
            dbField = String.valueOf(REMOCION_TUBERIA);
            intent.putExtra(dbField, String.valueOf(sp_view2.getSelectedItem()));
        }

        EditText et_view2 = (EditText) view.findViewById(R.id.ev_remo_observaciones);
//        texto += " - " + et_view2.getText().toString();
        texto = et_view2.getText().toString();

        dbField = String.valueOf(REMOCION_OBSERVACIONES);
        intent.putExtra("input", texto);

        setResult(Activity.RESULT_OK, intent);
        return true;
    }

    private void openDatabase(){
        dbHelper= new DBHelper(this);
        db = dbHelper.getReadableDatabase();
    }

    private void closeDatabase(){
        db.close();
        dbHelper.close();
    }

    public void setListener(){
        //Si es el ultimo le agregamos una accion
/*        EditText et_view=(EditText) view.findViewWithTag(ultimoEditView);
        et_view.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView arg0, int arg1,
                                          KeyEvent arg2) {
                // TODO Auto-generated method stub
                if (guardar())
                    finish();
                return false;
            }
        });
*/
    }
}