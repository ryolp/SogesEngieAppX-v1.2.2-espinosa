package enruta.soges_engie;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class PageAdapter extends PagerAdapter {
	Context context;
	private Cursor c;
	List <Bitmap> lista;
	PageAdapter(Context context, Cursor c){
		this.context=context;
		this.c=c;
		lista= new ArrayList<Bitmap>();
		for (int i=0; i<c.getCount();i++){
			c.moveToPosition(i);
			ByteArrayInputStream imageStream = new ByteArrayInputStream(c.getBlob(c.getColumnIndex("foto")));
			//Bitmap theImage =rotateImage(imageStream); ya no debe girar
			Bitmap theImage = resizeImage(imageStream );
			lista.add(theImage);
			
		}
	}
	@Override
	public int getCount() {
		return c.getCount();
	}
	 
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((ImageView) object);
	}
	 
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ImageView imageView = new ImageView(context);
		int padding = /*context.getResources().getDimensionPixelSize(R.dimen.padding_medium)*/0;
		imageView.setPadding(padding, padding, padding, padding);
		imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		
		//c.moveToPosition(position);
		//ByteArrayInputStream imageStream = new ByteArrayInputStream(c.getBlob(c.getColumnIndex("foto")));
		
		//BitmapFactory.Options options=new BitmapFactory.Options();
		//options.inSampleSize = 4;
		//Bitmap theImage = BitmapFactory.decodeStream(imageStream, null, options);
		//Bitmap theImage = BitmapFactory.decodeStream(imageStream);
		//Bitmap theImage =rotateImage(lista.get(position));
		Bitmap theImage=lista.get(position);
		imageView.setImageBitmap(theImage);
		((ViewPager) container).addView(imageView, 0);
		return imageView;
	}
	 
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((ImageView) object);
	
	}
	
	
//	@SuppressLint("NewApi")
//	public Bitmap rotateImage(ByteArrayInputStream imageStream ){
//		Bitmap theImage = BitmapFactory.decodeStream(imageStream);
//	
//		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//		
//		Display display = wm.getDefaultDisplay();
//		Point size = new Point();
//	
//	
//		int swidth ;
//		int sheight;
//		 int width = theImage.getWidth();
//	     int height = theImage.getHeight();
//		
//		try { 
//			display.getSize(size); 
//			swidth = size.y; 
//			} catch (NoSuchMethodError e) {
//				 swidth = display.getHeight(); 
//				} 
//		
//		sheight= (height * swidth) / width;
//		
//		
//	    
//	     int newWidth = swidth -10 ;
//	     int newHeight = sheight -10;
//	
//	     // calculate the scale - in this case = 0.4f
//	     float scaleWidth = ((float) newWidth) / width;
//	     float scaleHeight = ((float) newHeight) / height;
//	
//	     // createa matrix for the manipulation
//	     Matrix matrix = new Matrix();
//	     // resize the bit map
//	     matrix.postScale(scaleWidth, scaleHeight);
//	// rotate the Bitmap
//    matrix.postRotate(90);
//
//
//
//	     // recreate the new Bitmap
//	     Bitmap resizedBitmap = Bitmap.createBitmap(theImage, 0, 0,
//	                       width, height, matrix, true);
//	     
//	     return resizedBitmap;
//	}
	
	@SuppressLint("NewApi")
	public Bitmap rotateImage(ByteArrayInputStream imageStream ){
		Bitmap theImage = BitmapFactory.decodeStream(imageStream);
	
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
	
		 int width = theImage.getWidth();
	     int height = theImage.getHeight();

	
	     // createa matrix for the manipulation
	     Matrix matrix = new Matrix();
	     // resize the bit map
	     matrix.postScale(width, height);
	     // rotate the Bitmap
	  // rotate the Bitmap
	     matrix.postRotate(90);



	     // recreate the new Bitmap
	     Bitmap resizedBitmap = Bitmap.createBitmap(theImage, 0, 0,
	                       width, height, matrix, true);
	     
	     return resizedBitmap;
	}
	
	@SuppressLint("NewApi")
	public Bitmap resizeImage(ByteArrayInputStream imageStream ){
		Bitmap theImage = BitmapFactory.decodeStream(imageStream);
	
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
	
	
		int swidth ;
		int sheight;
		 int width = theImage.getWidth();
	     int height = theImage.getHeight();
		
		try { 
			display.getSize(size); 
			swidth = size.y; 
			} catch (NoSuchMethodError e) {
				 swidth = display.getHeight(); 
				} 
		
		sheight= (height * swidth) / width;
		
		
	    
	     int newWidth = swidth -10 ;
	     int newHeight = sheight -10;
	
	     // calculate the scale - in this case = 0.4f
	     float scaleWidth = ((float) newWidth) / width;
	     float scaleHeight = ((float) newHeight) / height;
	
	     // createa matrix for the manipulation
	     Matrix matrix = new Matrix();
	     // resize the bit map
	     matrix.postScale(scaleWidth, scaleHeight);



	     // recreate the new Bitmap
	     Bitmap resizedBitmap = Bitmap.createBitmap(theImage, 0, 0,
	                       width, height, matrix, true);
	     
	     return resizedBitmap;
	}
	
	
}