package enruta.soges_engie;

import java.util.HashMap;

import enruta.soges_engie.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;




public class Sonidos {
	
	SoundPool soundPool;
	HashMap<Integer, Integer> soundPoolMap;

	float volume = .5f; //Establecemos el volumen
	
	final static int NINGUNO=0;
	final static int BEEP=1;
	final static int URGENT=2;
	
	
	Context context;
	
	Sonidos(Context context){
		this.context = context;
		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		soundPoolMap = new HashMap(2);   
		soundPoolMap.put( R.raw.beep1a, soundPool.load(context, R.raw.beep1a, 1) );
		soundPoolMap.put( R.raw.urgent, soundPool.load(context, R.raw.urgent, 2) );
	}
	
	void playSound(int soundID) {

		if(soundPool == null || soundPoolMap == null){
			return;
		}
		
		soundPool.play(soundPoolMap.get(soundID), volume, volume, 1, 0, 1f);
		
		}
	
	void playSoundMedia(int soundID){
		int sonido;
		switch (soundID){
		case BEEP:
			sonido=R.raw.beep1a;
			break;
		case URGENT:
			sonido=R.raw.urgent;
			break;
		default:
			return;
		}
		MediaPlayer mp = MediaPlayer.create(context, sonido); 
		mp.start();
	}

}
