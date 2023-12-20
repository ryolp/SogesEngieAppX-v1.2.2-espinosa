package enruta.soges_engie;

import androidx.appcompat.app.AppCompatActivity;

import enruta.soges_engie.R;
import enruta.soges_engie.Configuracion.Entry;
import enruta.soges_engie.Configuracion.XmlEditText;
import enruta.soges_engie.Configuracion.XmlSpinner;
import enruta.soges_engie.Configuracion.XmlTextView;
import android.database.sqlite.SQLiteDatabase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Vector;

public class ClienteYaPago extends AppCompatActivity {

    public final static int CLIENTE_YA_PAGO_MONTO = 13;
    public final static int CLIENTE_YA_PAGO_FECHA = 14;
    public final static int CLIENTE_YA_PAGO_AGENTE = 15;

    Vector<Entry> objetosAMostar;
    View view;

    DBHelper dbHelper;
    SQLiteDatabase db;
    Globales globales;

    String is_anomalia="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_ya_pago);

        view= findViewById(R.id.ll_cliente_ya_pago);

        globales = ((Globales) getApplicationContext());
        Button button= (Button) findViewById(R.id.btn_cyp_continuar);
        button.setVisibility(View.VISIBLE);

        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (guardar())
                    finish();
            }

        });
        llenarAgentes();
    }

    private void llenarAgentes(){
        Spinner spinner = (Spinner) findViewById(R.id.sv_cyp_agente);
        ArrayList<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("7 Eleven");
        spinnerArray.add("American Express");
        spinnerArray.add("Arteli");
        spinnerArray.add("App Engie");
        spinnerArray.add("BBVA-Bancomer CIE");
        spinnerArray.add("Caja Bienestar");
        spinnerArray.add("Caja Popular Gonzalo Vega");
        spinnerArray.add("Citibanamex");
        spinnerArray.add("Elektra");
        spinnerArray.add("HEB");
        spinnerArray.add("Mi Cuenta");
        spinnerArray.add("OXXO");
        spinnerArray.add("Soriana");
        spinnerArray.add("Super Q");
        spinnerArray.add("Terminal Bancaria");
        spinnerArray.add("Otro");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private boolean guardar(){
        Intent intent= new Intent();
        String texto="";
        String dbField="";

        EditText et_view = (EditText) view.findViewById(R.id.ev_cyp_monto);
        texto = et_view.getText().toString();
        dbField = String.valueOf(CLIENTE_YA_PAGO_MONTO);
        intent.putExtra(dbField, texto);

        DatePicker dp_view = (DatePicker) view.findViewById(R.id.dp_cyp_fecha);
        texto = String.valueOf(dp_view.getDayOfMonth()) + "/" + String.valueOf(dp_view.getMonth()+1)+ "/" + String.valueOf(dp_view.getYear());
        dbField = String.valueOf(CLIENTE_YA_PAGO_FECHA);
        intent.putExtra(dbField, texto);

        Spinner sp_view= (Spinner) view.findViewById(R.id.sv_cyp_agente);
        texto = String.valueOf(sp_view.getSelectedItem());
        dbField = String.valueOf(CLIENTE_YA_PAGO_AGENTE);
        intent.putExtra(dbField, texto);

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