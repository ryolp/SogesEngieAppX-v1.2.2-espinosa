package enruta.soges_engie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import enruta.soges_engie.clases.Utils;
import enruta.soges_engie.entities.DatosEnvioEntity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CamaraActivity extends AppCompatActivity {

    public static int TEMPORAL = 1;
    public static int PERMANENTE = 0;
    public static int ANOMALIA = 2;

    public final static int TIPO_FOTO_ORDEN = 0;
    public final static int TIPO_FOTO_EMPLEADO = 1;

    /*Estados del Flash*/

    public final static int SIN_FLASH = 0;
    public final static int CON_FLASH = 1;
    public final static int AUTO = 3;

    CamaraActivity ca;

    private Camera mCamera;
    private CamaraPreview mPreview;
    private TextView tv_indicador;
    private Button captureButton, backButton, otraButton;
    private FrameLayout fotoPreview, cPreview;
    private long secuencial;
    private String is_terminacion = "-A";
    private ContentValues mCv_datos;
    private boolean otraFoto = false;
    private String ls_nombre, caseta;
    private byte[] foto;
    private int temporal;
    static String mensajeDeErrorCamera = "";
    private Globales globales;
    private ImageButton ib_flash;
    private boolean tieneFlash = true;
    private boolean tieneZoom = true;
    private boolean tieneCamaraFrontal = false;
    private AlertDialog alert;
    /**
     * Cantidad de fotos
     */
    int cantidad;
    int fotosTomadas = 0;
    Handler mHandler = new Handler();

    String flashMode = Camera.Parameters.FLASH_MODE_OFF;
    int zoomMode = 0;
    String camaraFrontalMode = Camera.Parameters.FLASH_MODE_OFF;

    // RL, 2023-09-14, Botones para aumentar y disminuir resolución, cambiar de cámara y firmar.

    protected ImageButton btnBajarResolucion;
    protected ImageButton btnSubirResolucion;
    protected ImageButton btnCambiarCamara;
    protected ImageButton btnFirmar;

    // RL, 2023-09-14, Etiqueta para mostrar la serie del medidor o su código de barras

    protected TextView lblNumMedidor;
    protected TextView txtNumMedidor;

    // RL, 2023-09-14, diálogo para mostrar mensajes de error
    private DialogoMensaje mDialogoMsg = null;

    // RL, 2023-09-14, Para saber el tipo de la foto: Lectura o de Empleado (Check-Seguridad)

    private int mTipoFoto = 0;
    private int mIdFoto = 0;
    private long mIdOrden = 0;

    /**
     * Inicialización del Activity
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camaralayout);
        globales = ((Globales) getApplicationContext());
        Bundle bu_params = getIntent().getExtras();

        secuencial = bu_params.getInt("secuencial");
        caseta = bu_params.getString("caseta");
        is_terminacion = bu_params.getString("terminacion");
//        try{
//        	if (!bu_params.getString("anomalia").equals("")){
//            	is_terminacion="_"+bu_params.getString("anomalia");
//            }
//        }
//        catch(Throwable e){
//        	
//        }

        temporal = bu_params.getInt("temporal");
        cantidad = bu_params.getInt("cantidad");
        mTipoFoto = bu_params.getInt("TipoFoto");
        mIdOrden = bu_params.getLong("idOrden");

        ca = this;

        tv_indicador = (TextView) findViewById(R.id.tv_indicador);
        captureButton = (Button) findViewById(R.id.btnCapturar);
        backButton = (Button) findViewById(R.id.camara_b_regresa);
        otraButton = (Button) findViewById(R.id.camara_b_otra);
        ib_flash = (ImageButton) findViewById(R.id.ib_flash);

        btnBajarResolucion = (ImageButton) findViewById(R.id.ib_bajarResolucion);
        btnSubirResolucion = (ImageButton) findViewById(R.id.ib_subirResolucion);
        btnCambiarCamara = (ImageButton) findViewById(R.id.ib_cambiarCamara);
        btnFirmar = (ImageButton) findViewById(R.id.ib_firmar);

        lblNumMedidor = (TextView) findViewById(R.id.lblMedidor);
        txtNumMedidor = (TextView) findViewById(R.id.txtMedidor);

        fotoPreview = (FrameLayout) findViewById(R.id.camera_preview_foto);
        cPreview = (FrameLayout) findViewById(R.id.camera_preview);

        tieneFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!tieneFlash) {
            ib_flash.setVisibility(View.GONE);
        } else {
            displayFlashMode();
        }

        if (!tieneZoom) {
            btnBajarResolucion.setVisibility(View.GONE);
            btnSubirResolucion.setVisibility(View.GONE);
        }
        if (is_terminacion.equals("Check")) {
            btnBajarResolucion.setVisibility(View.GONE);
            btnSubirResolucion.setVisibility(View.GONE);
            btnFirmar.setVisibility(View.GONE);
        }


        if (!globales.tomaMultiplesFotos && cantidad > 1) {
            cantidad = 1;
        }

        iniciaCamara();

        mostrarInformacion();
        inicializarEventosBotones();
    }

    /**
     * Inicializa los eventos de los botones
     */
    private void inicializarEventosBotones() {
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @SuppressLint("NewApi")
                    public void onClick(View v) {
                        // get an image from the camera
                        if (!otraFoto) {
                            try {
                                ca.mensajeEspere();
                                captureButton.setEnabled(false);
                                mCamera.autoFocus(mAutoFocusCallback);

                                //mCamera.takePicture(null, null, mPicture);
                                otraFoto = true;
                                //tv_indicador.setVisibility(View.GONE);

                                ib_flash.setVisibility(View.GONE);
                                btnFirmar.setVisibility(View.GONE);
                                btnSubirResolucion.setVisibility(View.GONE);
                                btnBajarResolucion.setVisibility(View.GONE);
                                btnCambiarCamara.setVisibility(View.GONE);
                                txtNumMedidor.setVisibility(View.GONE);
                                lblNumMedidor.setVisibility(View.GONE);
                            } catch (Throwable t) {
                                mostrarMensaje("Error", "Ocurrió un error inesperado en la cámara. Contactar a soporte.", t, null);
                            }
                        } else {
                            try {
                                iniciaCamara();
                                mostrarInformacion();
                                cPreview.setVisibility(View.VISIBLE);
                                fotoPreview.setVisibility(View.GONE);
                                otraFoto = false;

                                if (tieneFlash) {
                                    ib_flash.setVisibility(View.VISIBLE);
                                } else {
                                    ib_flash.setVisibility(View.GONE);
                                }

                                if (tieneZoom) {
                                    btnBajarResolucion.setVisibility(View.VISIBLE);
                                    btnSubirResolucion.setVisibility(View.VISIBLE);
                                } else {
                                    btnBajarResolucion.setVisibility(View.GONE);
                                    btnSubirResolucion.setVisibility(View.GONE);
                                }
                                if (is_terminacion.equals("Check")) {
                                    btnCambiarCamara.setVisibility(View.GONE);
                                    btnBajarResolucion.setVisibility(View.GONE);
                                    btnSubirResolucion.setVisibility(View.GONE);
                                } else btnCambiarCamara.setVisibility(View.VISIBLE);
                            } catch (Throwable t) {
                                mostrarMensaje("Error", "Ocurrió un error inesperado en la cámara. Contactar a soporte.", t, null);
                            }
                        }
                    }
                }
        );

        backButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        // get an image from the camera
                        fotosTomadas++;
                        if (fotosTomadas >= cantidad) {
                            regresar();
                        } else {
                            try {
                                otraFoto = false;
                                guardarFotoBD();
                                mostrarInformacion();

                                if (tieneFlash) {
                                    ib_flash.setVisibility(View.VISIBLE);
                                } else ib_flash.setVisibility(View.GONE);
                                if (tieneZoom) {
                                    btnBajarResolucion.setVisibility(View.VISIBLE);
                                    btnSubirResolucion.setVisibility(View.VISIBLE);
                                } else {
                                    btnBajarResolucion.setVisibility(View.GONE);
                                    btnSubirResolucion.setVisibility(View.GONE);
                                }
                                if (is_terminacion.equals("Check")) {
                                    btnCambiarCamara.setVisibility(View.GONE);
                                    btnBajarResolucion.setVisibility(View.GONE);
                                    btnSubirResolucion.setVisibility(View.GONE);
                                } else btnCambiarCamara.setVisibility(View.VISIBLE);

                                iniciaCamara();
                                cPreview.setVisibility(View.VISIBLE);
                                fotoPreview.setVisibility(View.GONE);
                            } catch (Throwable t) {
                                mostrarMensaje("Error", "Ocurrió un error inesperado en la cámara. Contactar a soporte.", t, null);
                            }
                        }
                    }
                }
        );

        otraButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        // get an image from the camera

                        try {
                            otraFoto = false;
                            mostrarInformacion();

                            if (tieneFlash) {
                                ib_flash.setVisibility(View.VISIBLE);
                            } else ib_flash.setVisibility(View.GONE);
                            if (tieneZoom) {
                                btnBajarResolucion.setVisibility(View.VISIBLE);
                                btnSubirResolucion.setVisibility(View.VISIBLE);
                            } else {
                                btnBajarResolucion.setVisibility(View.GONE);
                                btnSubirResolucion.setVisibility(View.GONE);
                            }
                            if (is_terminacion.equals("Check")) {
                                btnCambiarCamara.setVisibility(View.GONE);
                                btnBajarResolucion.setVisibility(View.GONE);
                                btnSubirResolucion.setVisibility(View.GONE);
                            } else btnCambiarCamara.setVisibility(View.VISIBLE);

                            guardarFotoBD();
                            iniciaCamara();
                            cPreview.setVisibility(View.VISIBLE);
                            fotoPreview.setVisibility(View.GONE);
                        } catch (Throwable t) {
                            mostrarMensaje("Error", "Ocurrió un error inesperado en la cámara. Contactar a soporte.", t, null);
                        }
                    }
                }
        );

        btnBajarResolucion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hacerBajarResolucion();
            }
        });

        btnSubirResolucion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hacerSubirResolucion();
            }
        });

        btnCambiarCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hacerCambiarCamara();
            }
        });

        btnFirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hacerFirmar();
            }
        });
    }

    /**
     * Inicialización de la cámara
     */

    public void iniciaCamara() {

        mensajeEspere();


        // Create an instance of Camera
        captureButton.setText("Captura");
        backButton.setVisibility(View.GONE);
        otraButton.setVisibility(View.GONE);
        captureButton.setEnabled(true);
        if (mCamera == null) {
            try {
                mCamera = getCameraInstance(globales.camaraFrontal);

                //Si no pudimos habrir la camara, mandamos un lindo mensajito...
                if (mCamera == null) {
                    Toast.makeText(this, String.format(getString(R.string.msj_error_descripcion), getString(R.string.msj_camara_obtener)) + mensajeDeErrorCamera, Toast.LENGTH_LONG).show();
                    tieneCamaraFrontal = false;
                    ca.alert.dismiss();
                    this.finish();
                    return;
                }

                Camera.Parameters cp = mCamera.getParameters();

//    		 
//    		 if (Build.VERSION.SDK_INT>=8)
//    			 setDisplayOrientation(mCamera,180);
//    		 else
//    		 {
//        		 cp.set("orientation", "portrait");
//        		 cp.set("rotation", 180);
//    		 }


                DBHelper dbHelper = DBHelper.getInstance(this);
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                cp.getSupportedPictureSizes();


                Cursor c = db.rawQuery("Select cast(value as integer) value from config where key='tam_fotos'", null);

                if (c.getCount() > 0) {
                    int n, m;
                    c.moveToFirst();
                    n = c.getColumnIndex("value");
                    m = c.getInt(n);
                    if (tieneCamaraFrontal)
                        m = seleccionarResolucionModerada(cp, 640, 480);

                    Size size = cp.getSupportedPictureSizes().get(m);
                    cp.setPictureSize(size.width, size.height);
                    //cp.setJpegQuality(70);
                    cp.setJpegQuality(/*globales.calidadDeLaFoto*/globales.calidadOverride);
                }

                c.close();
                db.close();
                dbHelper.close();

                if (!tieneCamaraFrontal) {
                    if (tieneFlash) {
                        cp.setFlashMode(flashMode);
                    }
                    tieneZoom = cp.isZoomSupported();
                    if (tieneZoom) {
                        cp.setZoom(globales.zoom);
                    }
                    //cp.setPictureSize(1633, 1225);
                }

                //cp.setPictureSize(1633, 1225);

                mCamera.setParameters(cp);
            } catch (Throwable t) {
                mostrarMensaje("Error", "Ocurrió un error inesperado en la cámara. Contactar a soporte.", t, null);
            }
        } else {
            try {
                if (!tieneCamaraFrontal) {
                    Camera.Parameters cp = mCamera.getParameters();
                    if (tieneFlash) {
                        cp.setFlashMode(flashMode);
                        mCamera.setParameters(cp);
                    }
                    tieneZoom = cp.isZoomSupported();
                    if (tieneZoom) {
                        cp.setZoom(globales.zoom);
                        mCamera.setParameters(cp);
                    }
                }
                mCamera.reconnect();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                mCamera = null;
                ca.alert.dismiss();
            }
        }

        if (mCamera == null) {
            globales.camaraFrontal = 0;
            tieneCamaraFrontal = false;
            ca.alert.dismiss();
            this.finish();
        }

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CamaraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeAllViews();
        preview.addView(mPreview);
        ca.alert.dismiss();
    }

    /**
     * Cambio de la resolución
     */

    protected int seleccionarResolucionModerada(Camera.Parameters cp, int width, int height) {
        List<Size> resolutions = cp.getSupportedPictureSizes();
        long resolution;
        Camera.Size size;
        long resDeseada = (long) width * (long) height;
        int count;
        long resMin = -1;
        int idxMin = -1;

        count = resolutions.size();

        for (int i = 0; i < count; i++) {

            size = resolutions.get(i);

            if (size.width == 640 && size.height == 480) {
                return i;
            }

            resolution = (long) size.height * (long) size.width;

            if (resMin == -1) {
                resMin = resolution;
                idxMin = i;
            } else if (resolution < resMin) {
                resMin = resolution;
                idxMin = i;
            }
        }

        return idxMin;
    }

    protected void setDisplayOrientation(Camera camera, int angle) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[]{angle});
        } catch (Throwable t) {
            mostrarMensaje("Error", "Ocurrió un error inesperado en la cámara. Contactar a soporte.", t, null);
        }
    }


    public static Camera getCameraInstance(int numCamara) {
        Camera c = null;
        try {
            c = Camera.open(numCamara); // attempt to get a Camera instance
            c.setDisplayOrientation(90);
        } catch (Throwable t) {
            // Camera is not available (in use or does not exist)

            mensajeDeErrorCamera = t.getMessage();
        }
        return c; // returns null if camera is unavailable
    }

    private PictureCallback mPicture = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                captureButton.setText(R.string.msj_camara_volverATomar);
                backButton.setVisibility(View.VISIBLE);
                otraButton.setVisibility(View.VISIBLE);
                captureButton.setEnabled(true);
                ca.alert.dismiss();
                //alert.dismiss();
                //captureButton.setText("Cámara");
                if (data == null) {
              /*  Log.d(TAG, "Error creating media file, check storage permissions: " +
                    e.getMessage());*/


                    return;
                } else {

                    if (Build.VERSION.SDK_INT >= 11)
                        mCamera.stopPreview();

                    guardarFotoTmp(data);
                    muestraPreview();
                }
            } catch (Throwable t) {
                mostrarMensaje("Error", "Ocurrió un error inesperado en la cámara. Contactar a soporte.", t, null);
            }
        }
    };
    
  /*  public void muestraPreview(){
    	CamaraPreview cp= new CamaraPreview(this, mCamera);
    }*/

    private void guardarFotoTmp(byte[] foto) throws Exception {
        try {
            DBHelper dbHelper = DBHelper.getInstance(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String ls_unicom, ls_nisrad;
            DatosEnvioEntity infoFoto = new DatosEnvioEntity();

//    	Cursor c= db.rawQuery("Select registro from encabezado", null);
//    	
//    	c.moveToFirst();
//    	ls_unicom= new String (c.getBlob(c.getColumnIndex("registro")), getResources().getInteger(R.integer.POS_DATOS_UNICOM), getResources().getInteger(R.integer.LONG_CAMPO_UNICOM));
//    	
//    	ls_nombre= ls_unicom.substring(0, 4)+"_" +ls_unicom.substring(4, 6)+"_"+ls_unicom.substring(6,10)+"_"+ls_unicom.substring(10, 12);
//    	c.close();
//    	
//    	//Quiero su nis_rad
//    	
//    	c= db.rawQuery("Select nisRad from ruta where cast(secuencia as Integer) ="+secuencial, null);
//    	c.moveToFirst();
//    	
//    	ls_nombre+="_"+ Main.rellenaString(c.getString(c.getColumnIndex("nisRad")), "0", getResources().getInteger(R.integer.LONG_CAMPO_POLIZA), true);
//
//    	c.close();
//    	db.close();
//    	dbHelper.close();
//    	//ls_nombre=caseta+ "_"+ secuencial + "_" + Main.obtieneFecha()+".jpg";
//    	
//    	ls_nombre+="_"+ Main.obtieneFecha("ymd_his");
//    	//Hay que preguntar por la terminacion
//    	ls_nombre+= "_" + is_terminacion +".jpg";

            infoFoto = getInfoFoto();

            ls_nombre = infoFoto.nombreArchivo;

            // RL, 2023-09, ls_nombre = globales.tdlg.getNombreFoto(globales, db, secuencial, is_terminacion);

            db.close();
            dbHelper.close();

            mCv_datos = new ContentValues(4);

            ByteArrayInputStream imageStream = new ByteArrayInputStream(foto);
            Bitmap theImage = rotateImage(imageStream);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            theImage.compress(Bitmap.CompressFormat.JPEG, 100, out);

            byte[] fotoAGuardar = out.toByteArray();


            mCv_datos.put("secuencial", secuencial);
            mCv_datos.put("nombre", ls_nombre);
            mCv_datos.put("foto", fotoAGuardar);
            mCv_datos.put("envio", TomaDeLecturas.NO_ENVIADA);
            mCv_datos.put("temporal", temporal);
            mCv_datos.put("idOrden", mIdOrden);
//        mCv_datos.put("idLectura", infoFoto.idLectura);
//        mCv_datos.put("idEmpleado", infoFoto.idEmpleado);
//        mCv_datos.put("idArchivo", infoFoto.idArchivo);
//        mCv_datos.put("NumId", infoFoto.NumId);

            this.foto = foto;
        } catch (Throwable t) {
            throw new Exception("Error al guardar foto", t);
        }
    }

    @Override
    public void onBackPressed() {
        if (globales.puedoCancelarFotos && !otraFoto) {

            try {
                mCamera.stopPreview();
                mPreview.setCamera(null);

                globales.camaraFrontal = 0;
                tieneCamaraFrontal = false;
                if (mCamera != null) {
                    globales.camaraFrontal = findBackCamera();
                    mCamera.release();
                    mCamera = null;
                }

                finish();
            } catch (Throwable t) {
                mostrarMensaje("Error", "Ocurrió un error inesperado en la cámara. Contactar a soporte.", t, null);
            }
        }
    }


    public void regresar() {
        try {
            guardarFotoBD();
            globales.camaraFrontal = 0;
            tieneCamaraFrontal = false;
// CE, 10/10/22, Estoy cerrando la camara aqui para evitar un problema al volverla a abrir
            if (mCamera != null) {
                globales.camaraFrontal = findBackCamera();
                mCamera.release();
                mCamera = null;
            }

            Intent resultado = new Intent();

            resultado.putExtra("idFoto", mIdFoto);

            setResult(Activity.RESULT_OK);
        } catch (Throwable t) {
            mostrarMensaje("Error", "Ocurrió un error inesperado en la cámara. Contactar a soporte.", t, null);
        }
        this.finish();
    }

    public void guardarFotoBD() throws Exception {
        long id;

        try {
            DBHelper dbHelper = DBHelper.getInstance(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();


            id = db.insertOrThrow("fotos", null, mCv_datos);

            //Guardar las fotos en la memoria del telefono, si me piden esto despues lo habilito pero por mientras vamos a quitarlo, ya que no tenemos control de esto.
    	/*File pictureFile = getOutputMediaFile(1, ls_nombre);
        if (pictureFile == null){
            //Log.d(TAG, "Error creating media file, check storage permissions: " +
              //  e.getMessage());
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(foto);
            fos.close();
        } catch (Throwable e) {
            
        }*/

            db.close();
            dbHelper.close();

            if (id == 0)
                throw new Exception("Error al guardar foto");
        } catch (Throwable t) {
            throw new Exception("Error al guardar foto", t);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            tieneCamaraFrontal = false;
            if (mCamera != null) {
                globales.camaraFrontal = findBackCamera();
                mCamera.release();
                mCamera = null;
            }
        } catch (Throwable t) {
            mostrarMensaje("Error", "Ocurrió un error inesperado en la cámara. Contactar a soporte.", t, null);
        }
        super.onDestroy();

    }

    private static File getOutputMediaFile(int type, String ls_nombre) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                // Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    ls_nombre);
        } else if (type == 2) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            try {
                camera.takePicture(null, null, mPicture);
            } catch (Throwable t) {
                mostrarMensaje("Error", "Ocurrió un error inesperado en la cámara. Contactar a soporte.", t, null);
            }
        }
    };

    private DatosEnvioEntity getInfoFoto() throws Exception {
        try {
            DatosEnvioEntity infoFoto = new DatosEnvioEntity();
            DBHelper dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String ls_nombre = "";

            if (is_terminacion.equals("Check")) {
                ls_nombre = Main.rellenaString(is_terminacion, "x", 10, true) + "-";
                ls_nombre += Main.rellenaString(caseta, "0", 20, true) + "-";
                ls_nombre += Main.obtieneFecha("ymd");
                ls_nombre += Main.obtieneFecha("his");
                ls_nombre += ".JPG";

                infoFoto = globales.tdlg.getInfoFoto(globales, db);

                infoFoto.nombreArchivo = ls_nombre;
            } else if (is_terminacion.equals("NoReg")) {
                ls_nombre = Main.rellenaString(is_terminacion, "x", 10, true) + "-";
                ls_nombre += Main.rellenaString(String.valueOf(secuencial), "0", 10, true) + "-";
                ls_nombre += Main.rellenaString(caseta, "0", 5, true) + "-";
                ls_nombre += Main.obtieneFecha("ymd");
                ls_nombre += Main.obtieneFecha("his");
                ls_nombre += ".JPG";

                infoFoto = globales.tdlg.getInfoFoto(globales, db);
                infoFoto.NumId = Utils.convToInt(caseta);

                infoFoto.nombreArchivo = ls_nombre;
            } else {
                infoFoto = globales.tdlg.getInfoFoto(globales, db, secuencial, is_terminacion);
                infoFoto.Lectura = globales.is_lectura;
            }

            return infoFoto;
        } catch (Throwable t) {
            throw new Exception("Error al obtener información para la foto");
        }
    }

    public void muestraPreview() throws Exception {
        try {
//            DatosEnvioEntity infoFoto = new DatosEnvioEntity();
//            DBHelper dbHelper = new DBHelper(this);
//            SQLiteDatabase db = dbHelper.getReadableDatabase();

            ImageView imageView = new ImageView(this);
            int padding = /*context.getResources().getDimensionPixelSize(R.dimen.padding_medium)*/0;
            imageView.setPadding(padding, padding, padding, padding);
            //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            ByteArrayInputStream imageStream = new ByteArrayInputStream(foto);
            Bitmap theImage = rotateImage(imageStream);
            //theImage =resizeImage(imageStream);

//            infoFoto = getInfoFoto();
//
//            agregarInfoImagen(theImage, infoFoto);

            imageView.setImageBitmap(theImage);

            fotoPreview.removeAllViews();
            fotoPreview.addView(imageView);

            fotoPreview.setVisibility(View.VISIBLE);
            cPreview.setVisibility(View.GONE);

            mCamera.release();
            mCamera = null;
        } catch (Throwable t) {
            throw new Exception("Error en inesperado en Cámara al mostrar preview", t);
        }
    }

//	@SuppressLint("NewApi")
//	public Bitmap rotateImage(ByteArrayInputStream imageStream ){
//		Bitmap theImage = BitmapFactory.decodeStream(imageStream);
//	
//		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
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
//	     // rotate the Bitmap
//	     matrix.postRotate(90);
//
//	     // recreate the new Bitmap
//	     Bitmap resizedBitmap = Bitmap.createBitmap(theImage, 0, 0,
//	                       width, height, matrix, true);
//	     
//	     return resizedBitmap;
//	}

    @SuppressLint("NewApi")
    public Bitmap rotateImage(ByteArrayInputStream imageStream) throws Exception {
        try {
            Bitmap theImage = BitmapFactory.decodeStream(imageStream);

            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            Display display = wm.getDefaultDisplay();
            Point size = new Point();

            // createa matrix for the manipulation

            Matrix matrix = new Matrix();

            // rotate the Bitmap

            int numRotation = 0;
            if (tieneCamaraFrontal) {
                numRotation = 270;
            } else {
                numRotation = 90;
            }
            matrix.postRotate(numRotation);

            // recreate the new Bitmap

            Bitmap resizedBitmap = Bitmap.createBitmap(theImage, 0, 0,
                    theImage.getWidth(), theImage.getHeight(), matrix, true);

            return resizedBitmap;
        } catch (Throwable t) {
            throw new Exception("Error al rotar imagen", t);
        }
    }

    @SuppressLint("NewApi")
    public Bitmap resizeImage(ByteArrayInputStream imageStream) throws Exception {
        try {
            Bitmap theImage = BitmapFactory.decodeStream(imageStream);

            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            Display display = wm.getDefaultDisplay();
            Point size = new Point();


            int swidth;
            int sheight;
            int width = theImage.getWidth();
            int height = theImage.getHeight();

            try {
                display.getSize(size);
                swidth = size.y;
            } catch (NoSuchMethodError e) {
                swidth = display.getHeight();
            }

            sheight = (height * swidth) / width;

            int newWidth = swidth - 10;
            int newHeight = sheight - 10;

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
        } catch (Throwable t) {
            throw new Exception("Error al cambiar tamaño de imagen", t);
        }
    }


    public void mensajeEspere() {
        final LayoutInflater inflater = this.getLayoutInflater();

        final View view = inflater.inflate(R.layout.wait_messagebox, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(view);
        builder.setCancelable(false);

        alert = builder.create();
        alert.show();
    }

    public void mostrarInformacion() {
        if (cantidad > 1) {
            //Toast.makeText(this,String.format(getString(R.string.msj_fotos_cantidad_a_tomar), cantidad), Toast.LENGTH_LONG).show();
            tv_indicador.setVisibility(View.VISIBLE);
            tv_indicador.setText((fotosTomadas + 1) + " " + getString(R.string.de) + " " + cantidad + " " + getString(R.string.msj_fotos));
        } else
            tv_indicador.setVisibility(View.GONE);

//        txtNumMedidor.setText(caseta);
//        txtNumMedidor.setVisibility(View.VISIBLE);
//        lblNumMedidor.setVisibility(View.VISIBLE);
    }

    public void flashMode(View view) throws Exception  {
        try {
            //Verificamos primero el modo actual, y luego cambiamos al siguiente...como un carrusel
            switch (globales.flash) {

                case SIN_FLASH:
                    globales.flash = CON_FLASH;
                    break;
                case CON_FLASH:
                    globales.flash = AUTO;
                    break;
                case AUTO:
                    globales.flash = SIN_FLASH;
                    break;
            }

            displayFlashMode();
            //tenemos que detener la camara y volverla a iniciar
            if (Build.VERSION.SDK_INT >= 11)
                mCamera.stopPreview();

            iniciaCamara();
        } catch (Throwable t) {
            throw new Exception("Error en flashMode", t);
        }
    }

    public void zoomMode(boolean bSubir) {
        try {
            //Verificamos primero el modo actual, y luego cambiamos al siguiente...como un carrusel
            Camera.Parameters cp = mCamera.getParameters();
            if (bSubir) {
                globales.zoom = globales.zoom + 10;
                if (globales.zoom > cp.getMaxZoom())
                    globales.zoom = cp.getMaxZoom();
            } else {
                globales.zoom = globales.zoom - 10;
                if (globales.zoom < 0)
                    globales.zoom = 0;
            }
            cp.setZoom(globales.zoom);
            if (!tieneCamaraFrontal) {
                mCamera.setParameters(cp);
            }
        } catch (Throwable t) {
            mostrarMensaje("Error", "Ocurrió un error inesperado en la cámara. Contactar a soporte.", t, null);
        }
    }

    public void camaraFrontalMode() {
        try {
            //tenemos que detener la camara y volverla a iniciar
            if (!tieneCamaraFrontal) {
                tieneCamaraFrontal = true;
                globales.camaraFrontal = findFrontCamera();
            } else {
                tieneCamaraFrontal = false;
                globales.camaraFrontal = findBackCamera();
            }
            if (Build.VERSION.SDK_INT >= 11)
                mCamera.stopPreview();
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            iniciaCamara();
        } catch (Throwable t) {
            mostrarMensaje("Error", "Ocurrió un error inesperado al usar la cámara frontal. Contactar a soporte.", t, null);
        }
    }

    private int findFrontCamera() throws Exception {
        try {
            int cameraCount = 0;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras();
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    return camIdx;
                }
            }
            return -1;
        } catch (Throwable t) {
            throw new Exception("Error al buscar cámara frontal", t);
        }
    }

    private int findBackCamera() throws Exception {
        try {
            int cameraCount = 0;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras();
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    return camIdx;
                }
            }
            return -1;
        } catch (Throwable t) {
            throw new Exception("Error al buscar cámara trasera", t);
        }
    }

    public void displayFlashMode() {
        switch (globales.flash) {

            case SIN_FLASH:
                flashMode = Camera.Parameters.FLASH_MODE_OFF;
                ib_flash.setImageResource(R.drawable.ic_sin_flash);
                break;
            case CON_FLASH:
                flashMode = Camera.Parameters.FLASH_MODE_ON;
                ib_flash.setImageResource(R.drawable.ic_con_flash);
                break;
            case AUTO:
                flashMode = Camera.Parameters.FLASH_MODE_AUTO;
                ib_flash.setImageResource(R.drawable.ic_auto);
                break;
        }
    }

    protected void hacerBajarResolucion() {
        // Sustituir por el código que permita bajar la resolución
        zoomMode(false);
//        Utils.showMessageLong(this, "Bajar resolución");
    }

    protected void hacerSubirResolucion() {
        // Sustituir por el código que permita subir  la resolución
        zoomMode(true);
//        Utils.showMessageLong(this, "Subir resolución");
    }

    protected void hacerCambiarCamara() {
        // Sustituir por el código que permita intercambiar las cámaras
        camaraFrontalMode();
//        Utils.showMessageLong(this, "Cambiar cámara");
    }


    /*
        Función para iniciar el activity para que el cliente firme.
        RL, 2023-01-02, Se regresa estructura con información adicional de la foto.
     */

    protected void hacerFirmar() {
        try {
            DatosEnvioEntity infoFoto;

            // Sustituir por el código que permita llamar el Activity para firmar

            DBHelper dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            infoFoto = globales.tdlg.getInfoFoto(globales, db, secuencial, is_terminacion);
            db.close();
            dbHelper.close();

            Intent padParaFirmar = new Intent(this, SignaturePadActivity.class);
            padParaFirmar.putExtra("secuencial", secuencial);
            padParaFirmar.putExtra("caseta", caseta);
            padParaFirmar.putExtra("terminacion", is_terminacion);
            padParaFirmar.putExtra("temporal", temporal);
            padParaFirmar.putExtra("cantidad", cantidad);
            //padParaFirmar.putExtra("anomalia", is_anomalia);
            padParaFirmar.putExtra("ls_nombre", infoFoto.nombreArchivo);
            padParaFirmar.putExtra("idOrden", infoFoto.idOrden);
            // vengoDeFotos = true;
            startActivityForResult(padParaFirmar, 1);
//        startActivity(padParaFirmar);

            //        Utils.showMessageLong(this, "Firmar");
        } catch (Throwable t) {
            mostrarMensaje("Error", "Ocurrió un error inesperado al abrir ventana para firmar. Contactar a soporte.", t, null);
        }
    }

    private void liberarCamara() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        liberarCamara();                 // Liberar la cámara cuando el usuario cambia de aplicación.
    }

    @Override
    protected void onResume() {
        iniciaCamara();
        //Ahora si abrimos
        if (globales.tdlg == null) {
            super.onResume();

            try {
	            Intent i = getBaseContext().getPackageManager()
	                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
	            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(i);
	            System.exit(0);
            } catch (Throwable t) {

            }
            return;
        }
        super.onResume();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bu_params;
//        switch (requestCode) {
//            case FIRMA:
        if (data == null) {
//                    mensajeOK(getString(R.string.msj_main_operacion_cancelada));
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
//                    if (bu_params.getString("mensaje").length() > 0)
//                        mensajeOK(bu_params.getString("mensaje"));
        } else {
//                    mensajeOK(getString(R.string.msj_main_operacion_cancelada));
        }
//                break;
//        }
    }


    /* -------------------------------------------------------------------------------------------
        Muestra el diálogo o ventana para mostrar mensajes diversos o de error.
        El detalle del error está oculto hasta que se hace click en el mensaje.
    ------------------------------------------------------------------------------------------- */

    private void mostrarMensaje(String titulo, String mensaje, String detalleError, DialogoMensaje.Resultado resultado) {
        if (mDialogoMsg == null) {
            mDialogoMsg = new DialogoMensaje(this);
        }

        mDialogoMsg.setOnResultado(resultado);
        mDialogoMsg.mostrarMensaje(titulo, mensaje, detalleError);
    }

    private void mostrarMensaje(String titulo, String mensaje, Throwable t, DialogoMensaje.Resultado resultado) {
        String msg;

        if (mDialogoMsg == null) {
            mDialogoMsg = new DialogoMensaje(this);
        }

        mDialogoMsg.setOnResultado(resultado);
        mDialogoMsg.mostrarMensaje(titulo, mensaje, t.getMessage());
    }

    private void mostrarMensaje(String titulo, String mensaje) {
        mostrarMensaje(titulo, mensaje, "", null);
    }
}