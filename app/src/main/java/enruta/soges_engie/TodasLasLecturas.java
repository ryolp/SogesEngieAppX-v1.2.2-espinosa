package enruta.soges_engie;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class TodasLasLecturas {
	
	Vector <Lectura> tll;
	int li_lecturaActual=0;
	int li_distanciaActual=0;
	int siguienteMedidorACapturar=0;
	
	DBHelper dbHelper;
	
	SQLiteDatabase db;
	

	Context context;
	
	boolean validarEsferas=false;
	boolean seAcabanLosIntentos=true;
	
	final static int CORRECTA=0;
	final static int FUERA_DE_RANGO=-1;
	final static int VACIA=-2;
	final static int ESFERAS_INCORRECTAS=-3;
	final static int INTENTANDO=-4;
	final static int INTENTOS_ACABADOS=-5;
	final static int LECTURA_REPETIDA=-5;
	
	final static int SIN_LEER=0;
	final static int LEIDA=1;
	final static int TODAS_LAS_LECTURAS=2;

	//int ordenDeLectura=0;
	
	private String ls_filtro="";
	
	final static int ORDEN_ASCENDENTE=0;
	final static int ORDEN_DESENDENTE=1;
	
	int ii_dondeEstaba=0;
	
	boolean encontrado=false;
	
	int ii_numVueltas=0;
	
	String ls_groupBy="";

	int intentos=0, maxIntentos=6, intentosCorrectos=3, intentosSeguidos=0;
	String lecturaAnt="";
	
	//Determina el orden que deberá tener la siguiente lectura
	int is_orden=ORDEN_ASCENDENTE;
	
	Lectura lectura;
	
	boolean filtrando=false;
	
	Globales globales;

	TodasLasLecturas(Context context, int li_lecturaActual) {
		this.context=context;
		this.li_lecturaActual=li_lecturaActual;
		this.li_distanciaActual=0;
		globales= (Globales) context.getApplicationContext();
	}
	
	TodasLasLecturas(Context context) {
		this.context=context;
		globales= (Globales) context.getApplicationContext();
		try{
			this.medidorGuardadoACapturar(false, false);
			//primerMedidorACapturar(false, false);
		}
		catch(Throwable e){
			e.printStackTrace();
		}
		
		/*tll= new Vector<Lectura>();
		actualizaLecturas();*/

		//siguienteMedidorACapturar();
	}
	
	TodasLasLecturas(Context context, boolean obtenerPrimerMedidor) {
		this.context=context;
		globales= (Globales) context.getApplicationContext();
		if (obtenerPrimerMedidor){
			try{
				medidorGuardadoACapturar(false, false);
				primerMedidorACapturar(false, false);
				//ordenDeLectura= this.getSiguienteOrdenDeLectura();
			}
			catch(Throwable e){
				
			}
		}
		/*tll= new Vector<Lectura>();
		actualizaLecturas();*/
		//siguienteMedidorACapturar();
	}
	
	public void guardarDondeEstaba(){
		ii_dondeEstaba=li_lecturaActual;
	}
	
	public void regresarDondeEstaba(){
		try {
			setSecuencialLectura(ii_dondeEstaba);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void actualizaLecturas(){
		
		int li_numLec=0;
		Cursor c;
		openDatabase();
		
		
		c=db.rawQuery("Select count(*) canti from ruta", null);
		
		c.moveToFirst();
		li_numLec=c.getInt(c.getColumnIndex("canti"));
		
		closeDatabase();
		for(int i=0; i<1;i++){
			try {
					tll.addElement(new Lectura(context, i+1));
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void openDatabase(){
    	dbHelper=  new DBHelper(context);
        db = dbHelper.getReadableDatabase();
    }
	
	 private void closeDatabase(){
	    	db.close();
	        dbHelper.close();
	 }
	 
	 public void siguienteMedidorACapturar(boolean modificar, boolean salir) throws Throwable {
		 Cursor c;
		 openDatabase();
		 //Buscamos el primer medidor sin lectura
		 String ls_selectArgs="secuenciaReal, secuencia";
		 String ls_tables="Ruta";
		 encontrado=true;
		 if (modificar){
			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where  " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.LEIDAS) +
				 		"and  cast (secuenciaReal as Integer)> " +li_lecturaActual + " "
						 +getFiltro()+"" +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+"order by cast(secuenciaReal as Integer) asc  limit 1", null);
		 } else {
			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.AUSENTES) 		+
				 		"and  cast (secuenciaReal as Integer)> " +li_lecturaActual + " "
						 +getFiltro()+" " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" order by cast(secuenciaReal as Integer)  asc   limit 1", null);
		 }
		 if (c.getCount() > 0) {
			 c.moveToFirst();
			 li_lecturaActual=Integer.parseInt(c.getString(c.getColumnIndex(("secuenciaReal"))));
			 lectura=new Lectura(context, li_lecturaActual);
		 } else {
			 if (salir) {
				 li_lecturaActual=0;
				 lectura=null;
			 }
			 encontrado=false;
		 }
		 c.close();
		 closeDatabase();
		 
		 //Con esto quitamos las vueltas
		 ii_numVueltas=1;
		 
		//Vamos a dar una vuelta a ver si no se encuentra despues
		 if (ii_numVueltas==0 && !encontrado) {
			 ii_numVueltas++;
			 li_lecturaActual=0;
			 siguienteMedidorACapturar(modificar, salir);
		 } else {
			 ii_numVueltas=0;
		 }
	 }
	 
	 public void medidorGuardadoACapturar(boolean modificar, boolean salir) throws Throwable {
		 Cursor c = null;
		 openDatabase();
		 //Buscamos el primer medidor sin lectura
		 String ls_selectArgs="secuenciaReal, secuencia";
		 String ls_tables="Ruta";
		 encontrado=true;
		 int lli_lecturaActual=0;
		 
		 c=db.rawQuery("Select ultimoSeleccionado from encabezado", null);
		 if (c.getCount()>0){
			 c.moveToFirst();
			 lli_lecturaActual=c.getInt(c.getColumnIndex("ultimoSeleccionado"));
		 }
		 c.close();
		 c=null;
		 if (modificar){
		 } else {
			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.AUSENTES) 		+
				 		"and  cast (secuenciaReal as Integer)= " +lli_lecturaActual + " "
						 +getFiltro()+" " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" order by cast(secuenciaReal as Integer)  asc   limit 1", null);
		 }
		 if (c!=null) {
			 if(c.getCount() > 0) {
				 c.moveToFirst();
				 li_lecturaActual=Integer.parseInt(c.getString(c.getColumnIndex(("secuenciaReal"))));
				 lectura=new Lectura(context, li_lecturaActual);
			 }
			 else{
				 li_lecturaActual=0;
				 lectura=null;
				 encontrado=false;
			 }
		 } else {
			 if (salir){
				 li_lecturaActual=0;
				 lectura=null;
			 }
			 encontrado=false;
		 }
		 c.close();
		 closeDatabase();
		//Vamos a dar una vuelta a ver si no se encuentra despues
		 if (!encontrado){
			primerMedidorACapturar(modificar, salir);
		 }
	 }
	 
	 public int getOrdenInconclusa() throws Throwable{
		 Cursor c = null;
		 openDatabase();
		 //Buscamos el primer medidor sin lectura
		 String ls_selectArgs="secuenciaReal, secuencia";
		 String ls_tables="Ruta";
		 encontrado=true;
		 int lli_lecturaActual=0;
		 
		 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.AUSENTES) 		+
			 		"and  verDatos=1", null);

		 if (c!=null){
			 if(c.getCount()>0){
				 c.moveToFirst();
//				 li_lecturaActual=Integer.parseInt(c.getString(c.getColumnIndex(("secuenciaReal"))));
//				 lectura=new Lectura(context, li_lecturaActual);
				 lli_lecturaActual= Integer.parseInt(c.getString(c.getColumnIndex(("secuenciaReal"))));
				 c.close();
				 closeDatabase();
			 }
		 }
		 c.close();
		 closeDatabase();
		 return lli_lecturaActual;
	 }
	 
	 public void siguienteMedidorIndistinto() throws Throwable {
		 Cursor c;
		 openDatabase();
		 //Buscamos el primer medidor sin lectura
		 String ls_selectArgs="secuenciaReal, secuencia";
		 String ls_tables="Ruta";
		 encontrado=true;
			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where  " +
				 		" cast (secuenciaReal as Integer)> " +li_lecturaActual + " "
						 +getFiltro()+"order by cast(secuenciaReal as Integer) asc " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" limit 1", null);

		 if(c.getCount()>0){
			 c.moveToFirst();
			 li_lecturaActual=Integer.parseInt(c.getString(c.getColumnIndex(("secuenciaReal"))));
			 lectura=new Lectura(context, li_lecturaActual);
		 } else {
			 encontrado=false;
		 }
		 c.close();
		 closeDatabase();
		 
		 //Con esto quitamos las vueltas
		 ii_numVueltas=1;
		 
		//Vamos a dar una vuelta a ver si no se encuentra despues
		 if (ii_numVueltas==0 && !encontrado){
			 ii_numVueltas++;
			 li_lecturaActual=0;
			 siguienteMedidorIndistinto();
		 } else {
			 ii_numVueltas=0;
		 }
	 }

	public void siguienteMedidorIndistintoPorDistancia() throws Throwable {
		Cursor c;
		openDatabase();
		//Buscamos el primer medidor sin lectura
		String ls_selectArgs="secuenciaReal, secuencia";
		String ls_tables="Ruta";
		encontrado=true;

		c=db.rawQuery("Select "+ls_selectArgs + ",diametro_toma from "+  ls_tables +" where  " +
				" cast (diametro_toma as Integer) >= " +li_distanciaActual + " "
				+getFiltro()+"order by cast(diametro_toma as Integer) asc " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" limit 1", null);

		if(c.getCount()>0){
			c.moveToFirst();
			li_distanciaActual = Integer.parseInt(c.getString(c.getColumnIndex(("diametro_toma"))));
			li_lecturaActual = Integer.parseInt(c.getString(c.getColumnIndex(("secuenciaReal"))));
			lectura=new Lectura(context, li_lecturaActual);
			SQLiteDatabase db1 = dbHelper.getReadableDatabase();
			db1.execSQL("update ruta set diametro_toma = '-1' where secuenciaReal = " + li_lecturaActual);
			db1.close();
		}
		else{
			encontrado=false;
		}
		c.close();
		closeDatabase();

		//Con esto quitamos las vueltas
		ii_numVueltas=1;

		//Vamos a dar una vuelta a ver si no se encuentra despues
		if (ii_numVueltas==0 && !encontrado){
			ii_numVueltas++;
			li_lecturaActual=0;
			siguienteMedidorIndistintoPorDistancia();
		}
		else{
			ii_numVueltas=0;
		}
	}

	 public Lectura siguienteObjetoMedidor(int li_lecturaActual,  int direccion,  boolean vuelta) throws Throwable {
			
		 Cursor c;
		 openDatabase();
		 
		 //Buscamos el primer medidor sin lectura
		 String ls_selectArgs="secuenciaReal, secuencia";
		 String ls_tables="Ruta";
		 boolean encontrado=true;
		 Lectura lectura = null;
		 
		 
		 if (direccion==TomaDeLecturas.ASC){
			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where  " +
				 		" cast (secuenciaReal as Integer)> " +li_lecturaActual + " "
				 
						 +getFiltro()+"order by cast(secuenciaReal as Integer) asc " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" limit 1", null);
			 
		 }else {
				 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.AUSENTES) +
					 		"and  cast (secuenciaReal as Integer)< " +li_lecturaActual + " "
							 +getFiltro()+" order by cast(secuenciaReal as Integer) desc " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" limit 1", null);
			 }
			 
		 
		 
		 if(c.getCount()>0){
			 c.moveToFirst();
			 lectura=new Lectura(context, li_lecturaActual);
		 }
		 else{
			
			 encontrado=false;
				 
		 }
		 c.close();
		 closeDatabase();
		 
		 
		//Vamos a dar una vuelta a ver si no se encuentra despues
		 if (!encontrado){
			 if (vuelta)
				lectura = siguienteObjetoMedidor(li_lecturaActual,direccion,  false);
			 else
				 throw new Throwable ("No encontrado");
			
		 }
		 
		 return lectura;
		 

		 
 
	 }
	 
	boolean hayMasMedidoresIguales(String serieMedidor){
		
		int canti=0;
		 Cursor c;
		 openDatabase();
		 
		 c=db.rawQuery("Select count(*) canti from ruta where serieMedidor='"+ serieMedidor+"'", null);
		 
		 c.moveToFirst();
		
		
		 if (c.getCount()>0){
			 canti=c.getInt(c.getColumnIndex("canti"));
		 }
		 
		 c.close();
		 closeDatabase();
		 
		 return canti>1;
	}
	
	Vector<Lectura> getMedidoresIgualesConFoto(String serieMedidor){
		openDatabase();
		Cursor c;
		Vector <Lectura> lecturas= new Vector();
		
		c=db.rawQuery("Select "+("secuenciaReal")+ "from ruta where serieMedidor='"+ serieMedidor+"' and fotoAlFinal=" + 1 , null);
		c.moveToFirst();
		if (c.getCount()>0){
			try{
				for(int i=0;i< c.getCount();i++){
					Lectura lectura= new Lectura(context,Integer.parseInt( c.getString(c.getColumnIndex(("secuenciaReal")))));
					lecturas.add(lectura);
					c.moveToNext();
				}
			}catch(Throwable e){
				
			}
			
			
		}
		

		
		c.close();
		 closeDatabase();
		 
		 return lecturas;
		
	}
	 
	
	Vector<Lectura> getMedidoresIguales(String serieMedidor){
		openDatabase();
		Cursor c;
		Vector <Lectura> lecturas= new Vector();
		
		c=db.rawQuery("Select secuenciaReal from ruta where serieMedidor='"+ serieMedidor+"'"  , null);
		c.moveToFirst();
		if (c.getCount()>0){
			try{
				for(int i=0;i< c.getCount();i++){
					Lectura lectura= new Lectura(context,Integer.parseInt( c.getString(c.getColumnIndex(("secuenciaReal")))));
					lecturas.add(lectura);
					c.moveToNext();
				}
			}catch(Throwable e){
				
			}
			
			
		}
		

		
		c.close();
		 closeDatabase();
		 
		 return lecturas;
		
	}
	 
	
	public int getSiguienteOrdenDeLectura(){
		 Cursor c;
		 openDatabase();
		 int siguienteSecuencial=0;
		 
		 c= db.rawQuery("Select max(cast(ordenDeLectura as Integer)) ordenDeLectura from ruta", null);
		 
		 if (c.getCount()>0){
			 c.moveToFirst();
			 String secuencialActual= c.getString(c.getColumnIndex("ordenDeLectura"));
			 
			 if (secuencialActual==null)
				 secuencialActual="0";
			 else
			 if (secuencialActual.equals(""))
				 secuencialActual="0";
			 
			 siguienteSecuencial= Integer.parseInt(secuencialActual) ;
		 }
		 siguienteSecuencial++;
		 c.close();
		 closeDatabase();
		 
		 return siguienteSecuencial;

	}
	 
	 
	 
	 
	 public void siguienteMedidorACapturarSinVuelta(boolean modificar, boolean salir) throws Throwable {
		 ii_numVueltas=99;
		 siguienteMedidorACapturar( modificar, salir);
	 }
	 
	 
	 public void setSecuencialLectura(int secuencia) throws Throwable {
			
		 Cursor c;
		 openDatabase();
		 //Buscamos el primer medidor sin lectura
		 String ls_selectArgs="secuenciaReal, secuencia";
		 String ls_tables="Ruta";
		 encontrado=true;
		 
		 

			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where "+
				 		" cast (secuenciaReal as Integer)=" +secuencia + " ", null);
			 
		 
		 if(c.getCount()>0){
			 c.moveToFirst();
			 li_lecturaActual=Integer.parseInt(c.getString(c.getColumnIndex(("secuenciaReal"))));
			 lectura=new Lectura(context, li_lecturaActual);
		 }
		 else{
			 encontrado=false;
			// Toast.makeText(context, "Ocurrio un error al momento de traer la lectura seleccionada", Toast.LENGTH_SHORT).show();
				 
		 }
		 c.close();
		 closeDatabase();

		 
 
	 }
	 
	 
	 public void primerMedidorACapturar(boolean modificar, boolean salir) throws Throwable {
		 Cursor c;
		 
		 //Buscamos el primer medidor sin lectura
		 openDatabase();
		 String ls_selectArgs="secuenciaReal, secuencia";
		 String ls_tables="Ruta";
		 encontrado=true;
		
		 if (modificar){
			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where  " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.LEIDAS) 
						 +getFiltro()+"order by cast(secuenciaReal as Integer) asc " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" limit 1", null);
		 }
		 else{
			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.AUSENTES) 		
						 +getFiltro()+"order by cast("+("secuenciaReal" )+" as Integer) asc " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" limit 1", null);
		 }

		 if(c.getCount()>0){
			 c.moveToFirst();
			 li_lecturaActual=Integer.parseInt(c.getString(c.getColumnIndex(("secuenciaReal"))));
			 lectura=new Lectura(context, li_lecturaActual);
		 }
		 else{
			 if (salir){
				 li_lecturaActual=0;
				 lectura=null;
			 }
			 encontrado=false;

		 }
		 c.close();
		 closeDatabase();
		 
 
	 }
	 
	 
	 public void ultimoMedidorACapturar(boolean modificar, boolean salir) throws Throwable {
		 Cursor c;
		 openDatabase();
		 //Buscamos el primer medidor sin lectura
		 String ls_selectArgs="secuenciaReal, secuencia";
		 String ls_tables="Ruta";
		 
		 encontrado=true;
		 if (modificar){
			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where  " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.LEIDAS) 
						 +getFiltro()+" order by cast(secuenciaReal as Integer) desc " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" limit 1", null);
		 }
		 else
		 {
			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.AUSENTES) 		
						 +getFiltro()+" order by cast(secuenciaReal as Integer) desc " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" limit 1", null);
		 }
		 if(c.getCount()>0){
			 c.moveToFirst();
			 li_lecturaActual=Integer.parseInt(c.getString(c.getColumnIndex(("secuenciaReal"))));
			 lectura=new Lectura(context, li_lecturaActual);
		 }
		 else{
			 if (salir){
				 li_lecturaActual=0;
				 lectura=null;
			 }
			 encontrado=false;

		 }
		 c.close();
		 closeDatabase();
		 
 
	 }
	 
	 public void anteriorMedidorACapturar(boolean modificar, boolean salir) throws Throwable{
		 Cursor c;
		 openDatabase();
		 //Buscamos el primer medidor sin lectura
		 
		//Buscamos el primer medidor sin lectura
		 String ls_selectArgs="secuenciaReal, secuencia";
		 String ls_tables="Ruta";
		 
		 encontrado=true;
		 if (modificar){
			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where  " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.LEIDAS) +
				 		"and  cast (secuenciaReal as Integer)< " +li_lecturaActual + " "
						 +getFiltro()+" order by cast(secuenciaReal as Integer) desc " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" limit 1", null);
		 }
		 else{
			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.AUSENTES) +	
				 		"and  cast (secuenciaReal as Integer)< " +li_lecturaActual + " "
						 +getFiltro()+" order by cast(secuenciaReal as Integer) desc " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" limit 1", null);
		 }
		 
		 if(c.getCount()>0){
			 c.moveToFirst();
			 li_lecturaActual=Integer.parseInt(c.getString(c.getColumnIndex(("secuenciaReal"))));
			 lectura=new Lectura(context, li_lecturaActual);
		 }
		 else{
			 
			 if (salir){
				 li_lecturaActual=0;
				 lectura=null;
			 }
			 
			 encontrado=false;

		 }
		 c.close();
		 closeDatabase();
		 
		//Con esto quitamos las vueltas
		 ii_numVueltas=1;
		 
		 //Vamos a dar una vuelta a ver si no se encuentra despues
		 if (ii_numVueltas==0 && !encontrado){
			 li_lecturaActual=getNumRecords() + 1;
			 ii_numVueltas++;
			 anteriorMedidorACapturar(modificar, salir);
		 }
		 else
			 ii_numVueltas=0;
		 
		 
	 }
	 
	 public void guardarLectura(String lectura){
		 this.lectura.setLectura(lectura);
		 this.lectura.ordenDeLectura=String.valueOf(this.getSiguienteOrdenDeLectura());
		 this.lectura.guardar(getSiguienteOrdenDeLectura());
		 
		 //ordenDeLectura++;
		 
	 }
	 
//	 public int capturaLectura(String lectura, Anomalia anomalia){
//		 int retorno=CORRECTA;
//		 lectura=lectura.trim();
//		 if (anomalia!=null){
//			 if (lectura.equals("") && anomalia.ii_ausente==0){
//				 //Requiere lectura
//				 return VACIA;
//			 }
//		 }
//		 else
//		 {
//			 if (lectura.equals("")){
//				 //Requiere lectura
//				 return VACIA;
//			 }
//			 
//		 }
//		 
//		 if (lectura.length()==tll.elementAt(li_lecturaActual).numerodeesferas && validarEsferas){
//			 return ESFERAS_INCORRECTAS;
//		 }
//			 
//		 intentos=tll.elementAt(li_lecturaActual).intentos;
//		 intentos++;
//		 if (!lectura.equals("")){
//			 if ((Long.parseLong(lectura)<tll.elementAt(li_lecturaActual).consBimAnt || Long.parseLong(lectura)>tll.elementAt(li_lecturaActual).consAnoAnt)
//					 && intentos==0){
//				 //Consumo fuera de rango
//				 retorno=FUERA_DE_RANGO;
//				
//				 lecturaAnt=lectura;
//				 
//				 intentosCorrectos++;
//			 }
//			 
//			 else if(intentos>0){
//				 retorno=INTENTANDO;
//				 lecturaAnt=lectura;
//
//				// if (intentos>=maxIntentos)		{ para poner una validacion de que sea opcional la falta de intentos
//				 if (intentos>=maxIntentos && seAcabanLosIntentos ){
//					 return INTENTOS_ACABADOS;
//				 }
//					if (lecturaAnt==lectura)	{
//						intentosCorrectos++;
//						
//					}
//					else{
//						intentosCorrectos=0;
//					}
//									
//					if ((intentos>=maxIntentos) || (intentosSeguidos==intentosCorrectos)	)		{
//						//Se acabaron los intentos, guardamos y siguiente lectura
//						lecturaAnt="";
//						intentos=0;
//						intentosCorrectos=0;
//						retorno=CORRECTA;
//					} 
//	 
//				// }
//			 }
//			 
//			 else{
//				 retorno=CORRECTA;
//			 }
//			 
//		 
//		 }
//		 
//		 tll.elementAt(li_lecturaActual).setIntento(intentos, lectura, retorno==CORRECTA);
//
//	 return retorno;
//	 }
	 
	 
	public Lectura getLecturaActual() {
		return  lectura;
	}
	
	public int getNumRecords(){
		Cursor c;
		int li_canti;
		openDatabase();
		
		c= db.rawQuery("Select count(*) canti from Ruta", null);
		c.moveToFirst();
		
		li_canti= c.getInt(0); //Solo tiene una columna, solo pongo la primaera
		c.close();
		closeDatabase();
		return li_canti;
	}
	
	public int getUltimaSecuencia(){
		Cursor c;
		int li_canti;
		openDatabase();
		
		c= db.rawQuery("Select max(secuenciaReal) canti from Ruta", null);
		c.moveToFirst();
		
		li_canti= c.getInt(0); //Solo tiene una columna, solo pongo la primaera
		c.close();
		closeDatabase();
		return li_canti;
	}
	
	public int getNumRecordsFiltro(){
		Cursor c;
		int li_canti;
		openDatabase();
		
		c= db.rawQuery("Select count(*) canti from Ruta where 1=1 "+ ls_filtro + (ls_groupBy.length()>0?" group by " + ls_groupBy: ""), null);
		c.moveToFirst();
		
		li_canti= c.getInt(0); //Solo tiene una columna, solo pongo la primaera
		c.close();
		closeDatabase();
		return li_canti;
	}
	
	public int getNumLectsPendientes(){
		int contador=0;
		
		for (int i =li_lecturaActual ; i<tll.size();i++){
			 if(tll.elementAt(i).sigoYo){
				 contador++;
			 }

		 }
		
		return contador;
	}
	
	public int getLecturasCapturadas(){
		
		return getNumRecords()-getLecturasCapturadas();
	}
	
	public void setFiltro(String ls_filtro){
		this.ls_filtro= " " + ls_filtro +" ";
	}
	 
	/**
	 * De haber un filtro definido, regresa dicha sentencia
	 * @return
	 */
	private String getFiltro(){
		
		String ls_cadena= formaCadenaFiltrado();
		
		if (ls_cadena.length()>0)
			 filtrando=true;
		 else
			 filtrando=false;
		
		return ls_filtro + ls_cadena;
	}
	
	/**
	 * De haber un filtro definido, regresa dicha sentencia
	 * @return
	 */
	private String getGroupBy(){
		
		
		
		return ls_groupBy ;
	}
	
	/**
	 * Maneja el orden que tendrá la lectura
	 * @return
	 */
	public String getOrden(){
		if (is_orden==ORDEN_ASCENDENTE){
			return " order by secuenciaReal asc ";
		}
		
		return " order by secuenciaReal desc ";
	}
	
	public boolean hayMasLecturas(){
		return li_lecturaActual!=0 && encontrado;
	}
	
	public void prepararModificar() throws Throwable{
		 Cursor c;
		 openDatabase();
		 String ls_selectArgs="secuenciaReal, secuencia";
		 String ls_tables="Ruta";
		 
		 encontrado=true;
		
			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where  " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.LEIDAS)  +
				 		"and  cast (secuenciaReal as Integer)< " +li_lecturaActual + " "
						 +getFiltro()+" order by cast(secuenciaReal as Integer) desc " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" limit 1", null);
		
		 
		 if(c.getCount()>0){
			 c.moveToFirst();
			 li_lecturaActual=Integer.parseInt(c.getString(c.getColumnIndex(("secuenciaReal"))));
			 lectura=new Lectura(context, li_lecturaActual);
		 }
		 else{
			 c.close();
			 
			 if (!db.isOpen()){
				 closeDatabase();
				 openDatabase();
			 }
			 

			 c=db.rawQuery("Select "+ls_selectArgs + " from "+  ls_tables +" where  " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.LEIDAS) +
				 		"and  cast (secuenciaReal as Integer)>= " +li_lecturaActual + " "
						 +getFiltro()+" order by cast(secuenciaReal as Integer) desc " +(ls_groupBy.length()>0?" group by " + ls_groupBy: "")+" limit 1", null);
			 if(c.getCount()>0){
				 c.moveToFirst();
				 li_lecturaActual=Integer.parseInt(c.getString(c.getColumnIndex(("secuenciaReal"))));
				 lectura=new Lectura(context, li_lecturaActual);
			 }
			 else
				 encontrado=false;
		 }
		 
			

		 c.close();
		 closeDatabase();
		 
	 }
	
	public String formaCadenaFiltrado(){
		String ls_cadena="";

		 openDatabase();
		 String ls_tmp;
		 Cursor c;
		 String ls_selected;
		 
		
		 
		 
		 c=db.query("config", null, "key='ciudad'", null, null, null, null);
		 
		 if (c.getCount()>0){
			 c.moveToFirst();
			 ls_selected=c.getString(c.getColumnIndex("selected"));
			 
			 if (ls_selected!=null)
				 if(ls_selected.equals("1"))
				 {
					 ls_tmp=c.getString(c.getColumnIndex("value"));
					 if (ls_tmp.trim().length()>0){
						 ls_cadena+= " and upper(municipio) like '%"+ls_tmp.toUpperCase()+"%' ";
					 }
				 
				 
				 
				 }
			 	
		 }
		 
		 c.close();
		 
		 c=db.query("config", null, "key='medidor'", null, null, null, null);
		 
		 if (c.getCount()>0){
			 c.moveToFirst();
			 
			 ls_selected=c.getString(c.getColumnIndex("selected"));
			 
			 if (ls_selected!=null)
				 if(ls_selected.equals("1"))
				 {
					 ls_tmp=c.getString(c.getColumnIndex("value"));
					 if (ls_tmp.trim().length()>0){
						 ls_cadena+= " and upper(serieMedidor) like '%"+ls_tmp.toUpperCase()+"%' ";
					 }
				 }
			 	
		 }
		 
		 c.close();
		 
		 c=db.query("config", null, "key='cliente'", null, null, null, null);
		 
		 if (c.getCount()>0){
			 c.moveToFirst();
			 ls_selected=c.getString(c.getColumnIndex("selected"));
			 
			 if (ls_selected!=null)
				 if(ls_selected.equals("1"))
				 {
					 ls_tmp=c.getString(c.getColumnIndex("value"));
					 if (ls_tmp.trim().length()>0){
						 ls_cadena+= " and upper(cliente) like '%"+ls_tmp.toUpperCase()+"%' ";
					 }
				 }
			 	
		 }
		 
		 c.close();
		 
		 c=db.query("config", null, "key='direccion'", null, null, null, null);
		 
		 if (c.getCount()>0){
			 c.moveToFirst();
			 
			 ls_selected=c.getString(c.getColumnIndex("selected"));
			 
			 if (ls_selected!=null)
				 if(ls_selected.equals("1"))
				 {
					 ls_tmp=c.getString(c.getColumnIndex("value"));
					 if (ls_tmp.trim().length()>0){
						 
							
						 
						 ls_cadena+= " and upper( trim(numEdificio) ||'  '|| trim(colonia) || '  ' || trim(comoLlegar1)  || '  '" +
						 		" || trim(dondeEsta)) like '%"+ls_tmp.toUpperCase()+"%' ";
					 }
				 }
			 }
			 
		 c.close();
		
		 
		 
		 closeDatabase();
		 
		 
		 
			 
		 
		 return ls_cadena;
		
	}
	
	public void forzarLecturas(){
		openDatabase();
		//Buscamos todas las lecturas cuya lectura y anomalia sean igual a vacio
		Cursor c=db.rawQuery("Select secuenciaReal from Ruta where "+globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.AUSENTES), null);
		c.moveToFirst();
		int canti= c.getCount();
		
		ContentValues cv_params = new ContentValues();
		cv_params.put("estadoDeLaOrden", "EO012");
		cv_params.put("fecha", Main.obtieneFecha(globales.tlc.getRellenoCampo("fecha")));
		cv_params.put("hora", Main.obtieneFecha(globales.tlc.getRellenoCampo("hora")));
		cv_params.put("tipoLectura", "4");
		cv_params.put("lectura", "0");
		cv_params.put("repercusion", "N");
		cv_params.put("anomalia", "18");
		cv_params.put("comentarios", "Cierre Forzado");
		cv_params.put("envio", 1);
		cv_params.put("registro", 0);
		db.update("ruta", cv_params,
				globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.AUSENTES), null);
		closeDatabase();
		c.close();
	}
	
	
	public boolean  hayPendientes(){
		openDatabase();
		
		//Buscamos todas las lecturas cuya lectura y anomalia sean igual a vacio
		Cursor c=db.rawQuery("Select secuencia from Ruta where " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.AUSENTES) +" limit 1", null);
		c.moveToFirst();
		int canti= c.getCount();
		c.close();
		closeDatabase();
		
		
		return canti>0;
		
		
	}
	
	public void moverPosicionMedidorActual(int pos){
		li_lecturaActual=pos;
	}
	
	/**
	 * Esta funcion regresa todas las calles existentes en el sistema
	 * @param tipo Tipo de filtro, Se encuentran estaticamente en esta clase y pueden ser las siguientes:
	 * 0 - No leidas
	 * 1 - Leidas
	 * 2 - Todas las lecturas
	 * @param filtro filtras las calles
	 * 
	 */
	public Vector <String> getTodasLasCalles(int tipo, String filtro){
		openDatabase();
		Vector <String> lvs_vector = new Vector <String>();
		Cursor c;
		String select="min(cast(secuenciaReal as Integer))"+"||'*'||direccion  || '<br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' || CASE  WHEN cast(numEdificio as Integer)%2=0 THEN ' PARES' ELSE ' NONES' END || ' ('|| count(*) ||')' nombre";
		if (!filtro.trim().equals("")){
			filtro=" " + (tipo==TODAS_LAS_LECTURAS?" where ": " and ")+" upper(direccion) like '%"+filtro.toUpperCase()+"%' ";
		}
		if (tipo== LEIDA){
			 c=db.rawQuery("Select "+select+" from ruta where  " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.LEIDAS) 			 		
						 +getFiltro()+ filtro +" group by direccion, cast(numEdificio as Integer)%2 order by cast(secuenciaReal as Integer)  asc", null);
		 }
		 else if (tipo== SIN_LEER) {
			 c=db.rawQuery("Select  "+select+"  from ruta where  " + globales.tdlg.getFiltroDeLecturas(TomaDeLecturasGenerica.AUSENTES) 			 		
						 +getFiltro()+ filtro + " group by direccion, cast(numEdificio as Integer)%2 order by cast(secuenciaReal as Integer)  asc", null);
		 }
		 else{
			 c=db.rawQuery("Select "+select+"  from ruta " +filtro
		 +" group by direccion, cast(numEdificio as Integer)%2 order by cast(secuenciaReal as Integer)  asc", null);
		 }
		c.moveToFirst();
		for (int i=0; i<c.getCount();i++){
			lvs_vector.add(c.getString(c.getColumnIndex("nombre")));
			c.moveToNext();
		}
		c.close();
		closeDatabase();
		return lvs_vector;
	}

	public String getTodosLosPuntosGPS(){
		openDatabase();
		String lvs_vector = "";
		String strOrigen = "";
		String strDestino = "";
		String strDondeEstoy = "";
		String strDestinoAnterior = "";
		String strWaypoints = "";
		Cursor c;
		c=db.rawQuery("Select * from ruta where anomalia='' and lectura='' order by cast(secuenciaReal as Integer) asc", null);
		c.moveToFirst();
// CE, 09/10/23, Solamente vamos a mostrar 20 puntos
//		for (int i=0; i<c.getCount(); i++){
		int nNumPuntosMax = 20;
		if (nNumPuntosMax > c.getCount())
			nNumPuntosMax = c.getCount();
		for (int i=0; i<nNumPuntosMax; i++){
			String strMedidor = "";
			strMedidor = c.getString(c.getColumnIndex("serieMedidor"));
			if ((c.getString(c.getColumnIndex("miLatitud")).equals("")) || c.getString(c.getColumnIndex("miLongitud")).equals("")) {

			} else {
				if (lvs_vector.equals("")) {
					lvs_vector = "https://maps.google.com/maps?saddr=";
					strOrigen = c.getString(c.getColumnIndex("miLatitud"))+","+c.getString(c.getColumnIndex("miLongitud"));
				} else {
//					if (strSegunda.equals("")) {
//						strSegunda = c.getString(c.getColumnIndex("miLatitud"))+","+c.getString(c.getColumnIndex("miLongitud"));
//					} else {
						strDestino = c.getString(c.getColumnIndex("miLatitud")) + "," + c.getString(c.getColumnIndex("miLongitud"));
						if (!strDestinoAnterior.equals(""))
							strWaypoints += "+to:" + strDestinoAnterior;
						strDestinoAnterior = strDestino;
//					}
				}
			}
			c.moveToNext();
		}
		if (!lvs_vector.equals("")) {
			strDondeEstoy = globales.location.getLatitude() + "," + globales.location.getLongitude();
			lvs_vector = lvs_vector + strDondeEstoy + "&daddr=" + strOrigen + strWaypoints + "+to:" + strDestino;
		}
		c.close();
		closeDatabase();
		return lvs_vector;
	}
}
