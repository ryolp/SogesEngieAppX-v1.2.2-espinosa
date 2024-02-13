package enruta.soges_engie;

import java.util.Vector;

import enruta.soges_engie.R;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ResumenGridAdapter extends BaseAdapter {
	Context mContext;
	Vector<EstructuraResumen> cursor;
	long height=0;
	double fontSize=22.0;

    public ResumenGridAdapter(Context c, Vector<EstructuraResumen> cursor, double fontSize) {
        mContext = c;
        this.cursor=cursor;
        this.fontSize=fontSize * 1.5;
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
    	int li_width, li_height;
    	
      //if (convertView == null) {  // if it's not recycled, initialize some attributes
        	layoutcuadro=new View (mContext);
        	//Ponemos el layout que queremos
        	layoutcuadro = inflater.inflate(R.layout.resumen, null);

        	int swidth;
        	WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
			
			Display display = wm.getDefaultDisplay();
			Point size = new Point();
			
        	try { 
				display.getSize(size); 
				swidth = size.x; 
				} catch (NoSuchMethodError e) {
					 swidth = display.getHeight(); 
					} 
        	
        	
        	//Foto y noticia
        	TextView cantidad = (TextView) layoutcuadro
					.findViewById(R.id.tv_cantidad);
        	
//        	double porcentaje= ((.20 * (swidth - 12)) /100);
//        	
//        	cantidad.setWidth((int) Math.round(porcentaje) );
        	
        	TextView descripcion = (TextView) layoutcuadro
					.findViewById(R.id.tv_descripcion);
        	
        	TextView tv_porcentaje = (TextView) layoutcuadro
					.findViewById(R.id.tv_porcentaje);
        	
//        	porcentaje= ((.80 *  (swidth - 12)) /100);
//        	
//        	descripcion.setWidth((int) Math.round(porcentaje));
        	
        	
        	EstructuraResumen resumen=cursor.elementAt(position);
        	
        	if (resumen.cantidad.equals("") && resumen.descripcion.equals("")){
        		cantidad.setBackgroundResource(android.R.color.transparent);
        		descripcion.setBackgroundResource(android.R.color.transparent);
        		tv_porcentaje.setVisibility(View.GONE);
        		
        	}
        		cantidad.setText(resumen.cantidad);
            	descripcion.setText(resumen.descripcion);	
            	cantidad.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)(fontSize));
            	descripcion.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)(fontSize));

				if (resumen.descripcion.equals("Desconexiones")) {
					descripcion.setBackgroundColor(Color.parseColor("#F39313"));
				}
				if (resumen.descripcion.equals("Remociones")) {
					descripcion.setBackgroundColor(Color.parseColor("#E73439"));
				}
				if (resumen.descripcion.equals("Reconexiones")) {
					descripcion.setBackgroundColor(Color.parseColor("#67AE6E"));
				}
				if (resumen.descripcion.equals("RX Express")) {
					descripcion.setBackgroundColor(Color.parseColor("#FDC300"));
				}
				if (resumen.descripcion.equals("Rec/Remos")) {
					descripcion.setBackgroundColor(Color.parseColor("#4BB0B9"));
				}

            	if (resumen.porcentaje.equals("")){
            		tv_porcentaje.setVisibility(View.GONE);
            	}
            	else{
            		tv_porcentaje.setText(resumen.porcentaje);
            		tv_porcentaje.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)(fontSize));
            	}
        	
        	
        	if (cantidad.getWidth()>descripcion.getWidth()){
        		height+=cantidad.getWidth();
        	}
        	else{
        		height+=descripcion.getWidth();
        	}
        	
        	return layoutcuadro;
//      }
//      else{
//    	  return convertView;
//      }
        
    }
    
   
	 
    
    
   
}