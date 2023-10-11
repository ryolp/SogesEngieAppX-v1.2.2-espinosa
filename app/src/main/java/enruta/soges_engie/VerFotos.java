package enruta.soges_engie;
 
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


public class VerFotos extends Activity{
    /** maintains the pager adapter*/
    private PagerAdapter mPagerAdapter;
    private List <String> fotos;
    
    final static int FOTOS=0;
    
    DBHelper dbHelper;
	SQLiteDatabase db;
	int canti_fotos, pos;
	int il_lect_act;
	String is_caseta;
    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
	
	private Button captureButton, backButton, otraButton;
	
	Lectura lectura;
	
	 String ls_terminacion;
	
	 Globales globales;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.fotos);
        this.setTitle("");
        globales = ((Globales) getApplicationContext());
        
        fotos= new ArrayList<String>();
        
        Bundle bu_params= getIntent().getExtras();
        il_lect_act= (int) bu_params.getLong("lect_act");
        
        String[] ls_args={String.valueOf(il_lect_act)};
        
        openDatabase();
        Cursor c=db.query("fotos", null, "cast (secuencial as integer)= cast(? as integer)", ls_args , null, null, null);
    	
        c.moveToFirst();
    	
        canti_fotos=c.getCount();
        
    	if (canti_fotos==0){
    		Toast.makeText(this, "No hay fotos que mostrar", Toast.LENGTH_SHORT).show();
    		finish();
    		return;
    	}
    	
    	for (int i=0; i<canti_fotos;i++){
    		fotos.add(c.getString(c.getColumnIndex("nombre")));
    		c.moveToNext();
    	}
    	
    	c.moveToFirst();
    	
        
        //initialsie the pager
    	
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        
        final TextView tv_infofotos=(TextView) findViewById(R.id.tv_infofotos);
        
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                // Check if this is the page you want.
            	pos=position;
            	tv_infofotos.setText((position +1 ) + " "+getString(R.string.de)+" " + canti_fotos);
            	tv_infofotos.bringToFront();
            }
        });
        
        tv_infofotos.setText(1 + " "+getString(R.string.de)+" " + canti_fotos);

        PageAdapter adapter = new PageAdapter(this, c);
        viewPager.setAdapter(adapter);
        
        c.close();
        
        c= db.rawQuery("Select serieMedidor from Ruta where cast (secuenciaReal as integer)= cast(? as integer)", ls_args);
        c.moveToFirst();
        is_caseta= c.getString(c.getColumnIndex("serieMedidor"));
        closeDatabase();
        
        captureButton = (Button) findViewById(R.id.btnCapturar);
        backButton = (Button) findViewById(R.id.camara_b_regresa);
        otraButton=(Button) findViewById(R.id.camara_b_otra);
        
        final VerFotos vf= this;
        
        try {
			lectura= new Lectura(this,Integer.parseInt(String.valueOf(il_lect_act) ));
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        ls_terminacion=lectura.terminacion;
        if (ls_terminacion==null){
        	ls_terminacion="-1";
        }
        
        globales.calidadOverride=globales.tdlg.cambiaCalidadSegunTabla("", "");

        otraButton.setOnClickListener(
        	    new View.OnClickListener() {
        	        public void onClick(View v) {
        	            // nueva imagen
        	        	Intent camara = new Intent(vf, CamaraActivity.class);
        				camara.putExtra("secuencial", il_lect_act);
        				camara.putExtra("caseta", is_caseta);
        				
        				camara.putExtra("terminacion", ls_terminacion);
        				//vengoDeFotos = true;
        				startActivityForResult(camara, FOTOS);
        	        		
        	        }
        	    }
        	);
        
        backButton.setOnClickListener(
        	    new View.OnClickListener() {
        	        public void onClick(View v) {
        	            // cerramos
        	        	 finish();
        	        		
        	        }
        	    }
        	);
        
        captureButton.setOnClickListener(
        	    new View.OnClickListener() {
        	        public void onClick(View v) {
        	            // Borramos la foto
        	        	openDatabase();
        	        	
        	        	db.execSQL("delete from fotos where nombre='"+fotos.get(pos)+"'");
        	        	
        	        	closeDatabase();
        	        	
        	        	//Tomamos otra
        	        	Intent camara = new Intent(vf, CamaraActivity.class);
        				camara.putExtra("secuencial", il_lect_act);
        				camara.putExtra("caseta", is_caseta);
        				//vengoDeFotos = true;
        				startActivityForResult(camara, FOTOS);
        	        	
        	        		
        	        }
        	    }
        	);
        
        
    }
    
    private void openDatabase(){
    	dbHelper= new DBHelper(this);
		
        db = dbHelper.getReadableDatabase();
    }
	
	 private void closeDatabase(){
	    	db.close();
	        dbHelper.close();
	    }
 
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 switch(requestCode){
			case FOTOS:
				finish();
				break;
		 }
	 }
	 
	 private void cerrar(){
		 finish();
	 }
    
}