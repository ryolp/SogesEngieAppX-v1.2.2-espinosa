package enruta.soges_engie;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Vector;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import enruta.soges_engie.clases.Utils;

public class Resumen extends Fragment {
	
	View rootView;
	
	DBHelper dbHelper;
	SQLiteDatabase db;
	TextView tv_resumen;
	Main ma_papa;
		 
	   @Override
	   public View onCreateView(LayoutInflater inflater, ViewGroup container,
	          Bundle savedInstanceState) {
	 
	       rootView = inflater.inflate(R.layout.entrada, container, false);
	       ma_papa=(Main) getActivity();
	       actualizaResumen();
	       return rootView;
	   }
	   
		public void actualizaResumen(){
			long ll_total;
			long ll_fotos;
			long ll_videos;
			long ll_restantes;
			long ll_porEnviar;
			long ll_EngieDesconexiones;
			long ll_EngieReconexiones;
			long ll_EngieRemociones;
			long ll_EngieRecRemos;
			long ll_EngieDesconexionesPendientes;
			long ll_EngieReconexionesPendientes;
			long ll_EngieRemocionesPendientes;
			long ll_EngieRecRemosPendientes;
			long ll_EngieDesconexionesEfectivas;
			long ll_EngieReconexionesEfectivas;
			long ll_EngieRemocionesEfectivas;
			long ll_EngieRecRemosEfectivas;

	    	if (ma_papa.globales.tdlg==null)
				return;
		 
	    	final GridView gv_resumen= (GridView) rootView.findViewById(R.id.gv_resumen);
	    	tv_resumen= (TextView) rootView.findViewById(R.id.tv_resumen);
	    	Vector <EstructuraResumen>resumen= new Vector<EstructuraResumen>();
	    	
	    	Cursor c;
	    	openDatabase();
	    	c= db.rawQuery("Select count(*) canti from Ruta", null);
	    	c.moveToFirst();
	    	ll_total=c.getLong(c.getColumnIndex("canti"));
	    	if (ll_total>0){
				c=db.rawQuery("Select count(*) canti from ruta where lectura='' AND anomalia=''", null);
				c.moveToFirst();
				ll_restantes=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where envio=" + TomaDeLecturas.NO_ENVIADA, null);
				c.moveToFirst();
				ll_porEnviar=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where trim(tipoDeOrden)='TO002'", null);
				c.moveToFirst();
				ll_EngieDesconexiones=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where trim(tipoDeOrden)='TO002' and trim(tipoLectura)=''", null);
				c.moveToFirst();
				ll_EngieDesconexionesPendientes=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where trim(tipoDeOrden)='TO002' and trim(anomalia)='A'", null);
				c.moveToFirst();
				ll_EngieDesconexionesEfectivas=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where trim(tipoDeOrden)='TO003'", null);
				c.moveToFirst();
				ll_EngieReconexiones=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where trim(tipoDeOrden)='TO003' and trim(tipoLectura)=''", null);
				c.moveToFirst();
				ll_EngieReconexionesPendientes=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where trim(tipoDeOrden)='TO003' and trim(repercusion)='A'", null);
				c.moveToFirst();
				ll_EngieReconexionesEfectivas=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where trim(tipoDeOrden)='TO004'", null);
				c.moveToFirst();
				ll_EngieRecRemos=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where trim(tipoDeOrden)='TO004' and trim(tipoLectura)=''", null);
				c.moveToFirst();
				ll_EngieRecRemosPendientes=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where trim(tipoDeOrden)='TO004' and trim(anomalia)='A'", null);
				c.moveToFirst();
				ll_EngieRecRemosEfectivas=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where trim(tipoDeOrden)='TO005'", null);
				c.moveToFirst();
				ll_EngieRemociones=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where trim(tipoDeOrden)='TO005' and trim(tipoLectura)=''", null);
				c.moveToFirst();
				ll_EngieRemocionesPendientes=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c=db.rawQuery("Select count(*) canti from ruta where trim(tipoDeOrden)='TO005' and trim(anomalia)='A'", null);
				c.moveToFirst();
				ll_EngieRemocionesEfectivas=c.getLong(c.getColumnIndex("canti"));
				c.close();

				c = db.rawQuery("Select count(*) canti from fotos", null);
				c.moveToFirst();
				ll_fotos = c.getLong(c.getColumnIndex("canti"));
				c.close();

				c = db.rawQuery("Select count(*) canti from videos", null);
				c.moveToFirst();
				ll_videos = c.getLong(c.getColumnIndex("canti"));
				c.close();

				float porcentaje=0;
				DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
				otherSymbols.setDecimalSeparator('.');
				otherSymbols.setGroupingSeparator(',');
				DecimalFormat formatter = new DecimalFormat("##0.00", otherSymbols);
	        	 
				for(String s:ma_papa.log){
					String[]sa=s.split("\t");
					resumen.add(new EstructuraResumen(sa[0],sa[1]));
				}

				porcentaje = (((float)ll_total*100) /(float)ll_total);
				resumen.add(new EstructuraResumen(getString(R.string.msj_main_total_lecturas),String.valueOf(ll_total),  formatter.format(porcentaje) +"%"));
				porcentaje = (((float)(ll_total - ll_restantes)*100) /(float)ll_total);
				resumen.add(new EstructuraResumen(getString(R.string.msj_main_lecturas_realizadas),String.valueOf(ll_total - ll_restantes),  formatter.format(porcentaje) +"%"));
				porcentaje = (((float)(ll_restantes)*100) /(float)ll_total);
				resumen.add(new EstructuraResumen(getString(R.string.msj_main_lecturas_restantes),String.valueOf(ll_restantes),  formatter.format(porcentaje) +"%"));
				porcentaje = (((float)ll_porEnviar*100) /(float)ll_total);
				resumen.add(new EstructuraResumen(getString(R.string.msj_main_medidores_con_lectura), String.valueOf(ll_porEnviar),  formatter.format(porcentaje) +"%"));
				porcentaje = (((float)ll_fotos*100) /(float)ll_total);
				resumen.add(new EstructuraResumen(getString(R.string.msj_main_fotos_tomadas), String.valueOf(ll_fotos),  formatter.format(porcentaje) +"%"));
				porcentaje = (((float)ll_videos*100) /(float)ll_total);
				resumen.add(new EstructuraResumen(getString(R.string.msj_main_videos_tomadas), String.valueOf(ll_videos),  formatter.format(porcentaje) +"%"));

				resumen.add(new EstructuraResumen("", ""));

				if (ll_EngieDesconexiones == ll_EngieDesconexionesPendientes)
					porcentaje = (float)100.0;
				else
					porcentaje = (((float)ll_EngieDesconexionesEfectivas*100 / (float)(ll_EngieDesconexiones - ll_EngieDesconexionesPendientes)));
				resumen.add(new EstructuraResumen(getString(R.string.msj_main_engie_desconexiones), String.valueOf(ll_EngieDesconexionesPendientes), String.format(Locale.US, "%.0f", porcentaje) + "%   "));
				if (ll_EngieReconexiones == ll_EngieReconexionesPendientes)
					porcentaje = (float)100.0;
				else
					porcentaje = (((float)ll_EngieReconexionesEfectivas*100 / (float)(ll_EngieReconexiones - ll_EngieReconexionesPendientes)));
				resumen.add(new EstructuraResumen(getString(R.string.msj_main_engie_reconexiones), String.valueOf(ll_EngieReconexionesPendientes), String.format(Locale.US, "%.0f", porcentaje) + "%   "));
				if (ll_EngieRemociones == ll_EngieRemocionesPendientes)
					porcentaje = (float)100.0;
				else
					porcentaje = (((float)ll_EngieRemocionesEfectivas*100 / (float)(ll_EngieRemociones - ll_EngieRemocionesPendientes)));
				resumen.add(new EstructuraResumen(getString(R.string.msj_main_engie_remociones), String.valueOf(ll_EngieRemocionesPendientes), String.format(Locale.US, "%.0f", porcentaje) + "%   "));
				if (ll_EngieRecRemos == ll_EngieRecRemosPendientes)
					porcentaje = (float)100.0;
				else
					porcentaje = (((float)ll_EngieRecRemosEfectivas*100) / (float)(ll_EngieRecRemos - ll_EngieRecRemosPendientes));
				resumen.add(new EstructuraResumen(getString(R.string.msj_main_engie_recremos), String.valueOf(ll_EngieRecRemosPendientes), String.format(Locale.US, "%.0f", porcentaje) + "%   "));
				resumen.add(new EstructuraResumen("", "")); //Agregamos una linea mas

				final ResumenGridAdapter adapter = new ResumenGridAdapter(getActivity(), resumen, ma_papa.infoFontSize * ma_papa.porcentaje2);

				gv_resumen.setAdapter(adapter);

				tv_resumen.setVisibility(View.GONE);
				gv_resumen.setVisibility(View.VISIBLE);
			} else {
	    		tv_resumen.setVisibility(View.VISIBLE);
	        	gv_resumen.setVisibility(View.GONE);
	    	}
	    	closeDatabase();
	    }
	    
	    private void openDatabase(){
	    	dbHelper= new DBHelper(getActivity());
	        db = dbHelper.getReadableDatabase();
	    }
		
		private void closeDatabase(){
			db.close();
			dbHelper.close();
		}
}
