package enruta.soges_engie;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
 
public class PantallaAnomaliasTabsPagerAdapter extends FragmentPagerAdapter {
	

	final static int RECIENTES=2;
	final static int MAS_USADAS=1;
	final static int TODAS=0;
 
    public PantallaAnomaliasTabsPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }
 
    @Override
    public Fragment getItem(int index) {
 
    	if (index>=0 && index <=3){
    		Bundle bundle = new Bundle();
        	bundle.putInt("tipo", index);
        	Fragment fragmento=new PantallaAnomaliasFragment();
        	fragmento.setArguments(bundle);
        	return fragmento;
    	}
    	

        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 1;
    }
    
   
 
}