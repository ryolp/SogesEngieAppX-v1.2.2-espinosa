package enruta.soges_engie;

import java.util.Vector;

import enruta.soges_engie.R;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class PantallaAnomaliasGridAdapter extends BaseAdapter {
	Context mContext;
	Vector<String> cursor;
	long height=0;
	int sWidth=0, sHeight=0;
	

    public PantallaAnomaliasGridAdapter(Context c, Vector<String> cursor, int fragmentWidth, int fragmentHeight) {
        mContext = c;
        this.cursor=cursor;
        sWidth=fragmentWidth/3;
        sHeight=fragmentHeight/4;
    }

    public int getCount() {
        return this.cursor.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
         
         //return cursor.elementAt(position).id;
    	return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    @SuppressLint("NewApi")
	public View getView(int position, View convertView, ViewGroup parent) {
    	//TextView agregarCategoria;
    	//Inflamos la vista
    	LayoutInflater inflater = (LayoutInflater) mContext
    			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View layoutcuadro;
//    	int li_width, li_height;
    	
//      if (convertView == null) {  // if it's not recycled, initialize some attributes
        	layoutcuadro=new View (mContext);
        	//Ponemos el layout que queremos
        	layoutcuadro = inflater.inflate(R.layout.anomalias_grid_preview, null);

//        	int swidth=0, sHeight = 0;
//        	WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
//			
//			Display display = wm.getDefaultDisplay();
//			Point size = new Point();
//			
//        	try { 
//				display.getSize(size); 
//				swidth = size.x; 
//				sHeight= size.y;
//				} catch (NoSuchMethodError e) {
//					 swidth = display.getWidth(); 
//					 sHeight =display.getHeight();
//					} 
//        	
//
//        	
//        	swidth /=3;
//        	sHeight /=5;
        	
        	
        	//Foto y noticia
        	TextView informacion = (TextView) layoutcuadro
					.findViewById(R.id.tv_informacion);

//        	informacion.setWidth(swidth);
//        	informacion.setWidth(sHeight);
        	
        	GridView.LayoutParams params= new GridView.LayoutParams(sWidth,sHeight);
        	layoutcuadro.setLayoutParams(params);
        	
        	
        	
        	String anomalia=cursor.elementAt(position);
        	
        	
        	informacion.setText(anomalia);
        	//leido.setText(lectura.formatedInfoReadMetter());
//        	leido_lectura.setTextColor(mContext.getResources().getColor(lectura.colorInfoReadMetter(Lectura.LEIDA_LECTURA)));
//        	leido_anomalia.setTextColor(mContext.getResources().getColor(lectura.colorInfoReadMetter(Lectura.LEIDA_ANOMALIA)));
        	
        	if (position%2==0)
        		layoutcuadro.setBackgroundResource(R.color.LightGray);
        	
        	
        	return layoutcuadro;
//      }
//      else{
//    	  return convertView;
//      }
        
    }
    
    public String getSecuencia(int pos){
    	return cursor.elementAt(pos);
    	
    }
    
    
	 
    
    
   
}