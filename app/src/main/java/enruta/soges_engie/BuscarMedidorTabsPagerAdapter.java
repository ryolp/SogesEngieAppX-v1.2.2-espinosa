package enruta.soges_engie;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
 
public class BuscarMedidorTabsPagerAdapter extends FragmentPagerAdapter {
	

	final static int MEDIDOR=0;
	final static int DIRECCION=1;
	final static int NUMERO=2;
	final static int CALLES=3;
	
	boolean remplazaDireccion=false;
 
    public BuscarMedidorTabsPagerAdapter(FragmentManager fragmentManager, boolean remplazaDireccion) {
    	super(fragmentManager);
    	this.remplazaDireccion=remplazaDireccion;
        
    }
 
    @Override
    public Fragment getItem(int index) {
 
    	if (index>=0 && index <=3){
    		Bundle bundle = new Bundle();
    		bundle.putInt("tipo", remplazaDireccion && index==DIRECCION?CALLES: index);
        	Fragment fragmento=new BuscarMedidorFragment();
        	fragmento.setArguments(bundle);
        	return fragmento;
    	}
    	
    	
//        switch (index) {
//        case MEDIDOR:
//            // Fragmento de por medidor
//           return  ;
//        case DIRECCION:
//            //Fragmento de Direccion
//            return new BuscarMedidorFragment();
//        case NUMERO:
//        	//Fragmento de Numero de casa
//        	//Fragmento de Direccion
//            return new BuscarMedidorFragment();
//        }
    	
    	
// 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }
    
   
 
}