package enruta.soges_engie;

import java.util.Vector;

import enruta.soges_engie.R;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BuscarMedidorGridAdapter extends BaseAdapter {
    Context mContext;
    Vector<Lectura> cursor;
    long height = 0;
    String textoBuscado;
    int tipoDeBusqueda = BuscarMedidorTabsPagerAdapter.MEDIDOR;
    int totalMedidores = 0;
    Globales globales;

    Vector<String> ivs_cursor;

    public BuscarMedidorGridAdapter(Context c, Vector<Lectura> cursor, int tipoDeBusqueda, String textoBuscado, int totalMedidores) {
        mContext = c;
        this.tipoDeBusqueda = tipoDeBusqueda;
        this.cursor = cursor;
        this.textoBuscado = textoBuscado;
        this.totalMedidores = totalMedidores;
        globales = (Globales) c.getApplicationContext();
    }

    public BuscarMedidorGridAdapter(Context c, Vector<String> cursor, int tipoDeBusqueda, String textoBuscado) {
        mContext = c;
        this.tipoDeBusqueda = tipoDeBusqueda;
        ivs_cursor = cursor;
        this.textoBuscado = textoBuscado;
        this.totalMedidores = totalMedidores;
        globales = (Globales) c.getApplicationContext();
    }


    public int getCount() {
        if (tipoDeBusqueda != BuscarMedidorTabsPagerAdapter.CALLES) {
            return this.cursor.size();
        } else {
            return ivs_cursor.size();
        }
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

        View layoutcuadro = null;

        try {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int li_width, li_height;


//      if (convertView == null) {  // if it's not recycled, initialize some attributes
            layoutcuadro = new View(mContext);
            //Ponemos el layout que queremos
            layoutcuadro = inflater.inflate(R.layout.preview_buscar_medidor, null);

            int swidth;
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

            LinearLayout ll_indicadores = (LinearLayout) layoutcuadro
                    .findViewById(R.id.ll_indicadores);

            Display display = wm.getDefaultDisplay();
            Point size = new Point();

            try {
                display.getSize(size);
                swidth = size.x;
            } catch (NoSuchMethodError e) {
                swidth = display.getHeight();
            }

            if (tipoDeBusqueda == BuscarMedidorTabsPagerAdapter.CALLES) {
                ll_indicadores.setVisibility(View.GONE);
            }

            //Foto y noticia
            TextView informacion = (TextView) layoutcuadro
                    .findViewById(R.id.tv_info);

//        	double porcentaje= ((.20 * (swidth - 12)) /100);
//        	
//        	cantidad.setWidth((int) Math.round(porcentaje) );

            TextView leido_lectura = (TextView) layoutcuadro
                    .findViewById(R.id.tv_leido_lectura);

            TextView leido_anomalia = (TextView) layoutcuadro
                    .findViewById(R.id.tv_leido_anomalia);

//        	porcentaje= ((.80 *  (swidth - 12)) /100);
//        	
//        	descripcion.setWidth((int) Math.round(porcentaje));

            //leido.setText(lectura.formatedInfoReadMetter());
            if (tipoDeBusqueda != BuscarMedidorTabsPagerAdapter.CALLES) {
                Lectura lectura = cursor.elementAt(position);
                informacion.setText(lectura.getInfoPreview(tipoDeBusqueda, textoBuscado, totalMedidores));

                leido_lectura.setTextColor(mContext.getResources().getColor(lectura.colorInfoReadMetter(Lectura.LEIDA_LECTURA)));
                leido_anomalia.setTextColor(mContext.getResources().getColor(lectura.colorInfoReadMetter(Lectura.LEIDA_ANOMALIA)));

                if (globales.strUltimaBusquedaRealizada.equals("Campanita")) {
                    layoutcuadro.setBackgroundResource(R.color.white);
                    if (lectura.is_balance.equals("1"))
                        layoutcuadro.setBackgroundResource(R.color.Green);
                    if (lectura.is_balance.equals("2"))
                        layoutcuadro.setBackgroundResource(R.color.Red);
                    if (lectura.is_balance.equals("3"))
                        layoutcuadro.setBackgroundResource(R.color.Yellow);
                } else {
                    if (lectura.getTipoDeOrden().equals("DESCONEXIÓN"))
                        layoutcuadro.setBackgroundResource(R.color.EngieDx);
                    if (lectura.getTipoDeOrden().equals("RECONEXIÓN"))
                        layoutcuadro.setBackgroundResource(R.color.EngieRx);
                    if (lectura.getTipoDeOrden().equals("RX.EXPRESS"))
                        layoutcuadro.setBackgroundResource(R.color.EngieEx);
                    if (lectura.getTipoDeOrden().equals("REMOCIÓN"))
                        layoutcuadro.setBackgroundResource(R.color.EngieRm);
                    if (lectura.getTipoDeOrden().equals("REC/REMO"))
                        layoutcuadro.setBackgroundResource(R.color.EngieRr);
                }
            } else {
                String lectura = ivs_cursor.elementAt(position);
                informacion.setTextSize(30f);
                informacion.setText(Html.fromHtml(Lectura.marcarTexto(lectura.substring(lectura.indexOf("*") + 1), textoBuscado, false)));
                if (position % 2 == 0)
                    layoutcuadro.setBackgroundResource(R.color.LightGray);
            }
        } catch (Throwable t) {
            Log.d("SOGES", "Error al buscar medidores", t);
        }
// CE, 19/10/23, Vamos a mostrar el color de fondo dependiendo del TipoDeOrden
//        	if (position%2==0)
//        		layoutcuadro.setBackgroundResource(R.color.LightGray);
        return layoutcuadro;
//      }
//      else{
//    	  return convertView;
//      }
    }

    public int getSecuencia(int pos) {
        if (tipoDeBusqueda != BuscarMedidorTabsPagerAdapter.CALLES) {
            return globales.mostrarRowIdSecuencia ? cursor.elementAt(pos).secuenciaReal : cursor.elementAt(pos).secuencia;
        } else {
            return Integer.parseInt(ivs_cursor.elementAt(pos).substring(0, ivs_cursor.elementAt(pos).indexOf("*")));
        }
    }
}