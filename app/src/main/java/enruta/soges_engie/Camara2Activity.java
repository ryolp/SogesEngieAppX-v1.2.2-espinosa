package enruta.soges_engie;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;

import androidx.camera.video.Recorder;

import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import enruta.soges_engie.entities.DatosEnvioEntity;
import enruta.soges_engie.entities.SubirFotoRequest;
import enruta.soges_engie.entities.SubirFotoResponse;
import enruta.soges_engie.services.WebApiManager;

public class Camara2Activity extends AppCompatActivity {
    // RL, 2023-10-04, Declaraciones para el uso de la cámara

    private static final int CAMARA_TRASERA = 1;
    private static final int CAMARA_FRONTAL = 2;

    private Globales mGlobales;

    private ListenableFuture<ProcessCameraProvider> mCameraProviderListenableFuture;
    private PreviewView mPreviewView;
    private ImageCapture mImageCapture;
    private VideoCapture<Recorder> mVideoCapture = null;
    private ProcessCameraProvider mCameraProvider = null;
    private PendingRecording mPendingRecording = null;
    private Recording mRecording = null;
    private Recorder mRecorder = null;
    //private VideoCapture<Recorder> videoCapture2;
    private ExecutorService mExecutorService;
    private boolean mGrabandoVideo = false;
    private boolean mCancelandoGrabacion = false;
    private int camaraOrigen = CAMARA_TRASERA;

    // RL, 2023-10-04, variables de los controles
    private Button btnCapturarFoto;
    private Button btnCapturarVideo;
    private ImageButton btnCambiarCamara;
    private Button btnRegresar;
    private ImageButton btnFirmar;
    private ImageButton btnFlash;
    private ImageButton btnSubirResolucion;
    private ImageButton btnBajarResolucion;
    private TextView txtIndicador;
    private TextView lblMedidor;
    private TextView txtMedidor;
    private ImageView imgGrabando;

    // RL, 2023-10-04, diálogo para mostrar mensajes de error
    private DialogoMensaje mDialogoMsg = null;

    // RL, 2023-10-16, Funcionalidad del timer

    private CountDownTimer mTimer = null;
    private final long INTERVALO_CONTADOR = 1000;
    private boolean mForzarDetenerVideo = true;
    private long mContador = 0;
    private int mDuracionVideoMseg = 61;

    // RL, 2023-10-16, medio para enviar mensaje de un Thread al Thread principal
    private Handler mHandler;

    // RL, 2023-09-14, Para saber el tipo de la foto: Lectura o de Empleado (Check-Seguridad)

    private int mTipoFoto = 0;
    private int mIdFoto = 0;
    private long mIdOrden = 0;
    private int temporal;
    private long secuencial;
    private String ls_nombre;
    private String caseta;
    private String is_terminacion = "-A";
    private int cantidad;

    private Thread mEnviarProceso = null;

    // RL, 2023-10-24, Campos para guardar la referencia del video en la base de datos

    private ContentValues mCv_datos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara2);

        mGlobales = ((Globales) getApplicationContext());

        mExecutorService = Executors.newSingleThreadExecutor();

        mDuracionVideoMseg = mGlobales.getDuracionVideoSeg() * 1000;

        if (mDuracionVideoMseg <= 0)
            finalizarActivity();
        else {
            Bundle bu_params = getIntent().getExtras();

            inicializarControles();
            inicializarEventosControles();
            obtenerParametros();
        }
    }

    @Override
    public void onBackPressed() {

    }

    void obtenerParametros() {
        Bundle bu_params = getIntent().getExtras();

        if (bu_params == null) return;

        try {
            secuencial = bu_params.getInt("secuencial");
            caseta = bu_params.getString("caseta");
            is_terminacion = bu_params.getString("terminacion");
            temporal = bu_params.getInt("temporal");
            cantidad = bu_params.getInt("cantidad");
            mTipoFoto = bu_params.getInt("TipoFoto");
            mIdOrden = bu_params.getLong("idOrden");
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Problema inesperado al obtener parámetros", t.getMessage(), null);
        }
    }

    void inicializarControles() {
        try {
            btnCapturarFoto = findViewById(R.id.btnCapturarFoto);
            btnCapturarVideo = findViewById(R.id.btnCapturarVideo);
            btnCambiarCamara = findViewById(R.id.btnCambiarCamara);
            btnRegresar = findViewById(R.id.btnRegresar);

            btnFirmar = findViewById(R.id.ib_firmar);
            btnFlash = findViewById(R.id.ib_flash);
            btnSubirResolucion = findViewById(R.id.ib_subirResolucion);
            btnBajarResolucion = findViewById(R.id.ib_bajarResolucion);

            imgGrabando = findViewById(R.id.imgGrabando);

            btnRegresar.setVisibility(View.INVISIBLE);
            imgGrabando.setVisibility(View.INVISIBLE);


            txtIndicador = findViewById(R.id.tv_indicador);
            lblMedidor = findViewById(R.id.lblMedidor);
            txtMedidor = findViewById(R.id.txtMedidor);

            mPreviewView = findViewById(R.id.previewView);

            txtIndicador.setVisibility(View.GONE);

            mPreviewView = (PreviewView) findViewById(R.id.previewView);
            lblMedidor = findViewById(R.id.lblMedidor);
            lblMedidor.setVisibility(View.GONE);

            txtMedidor = findViewById(R.id.txtMedidor);
            txtMedidor.setVisibility(View.GONE);
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Problema inesperado al registrar controles", t.getMessage(), null);
        }
    }

    void inicializarEventosControles() {
        try {
            btnCapturarFoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    capturarFoto();
                }
            });

            btnCapturarVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    capturarVideo();
                }
            });

            btnRegresar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finalizarActivity();
                }
            });

            btnCambiarCamara.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cambiarCamara();
                }
            });

            // iniciarCamara();
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Problema inesperado al iniciar eventos de los controles", t.getMessage(), null);
        }
    }

    private void iniciarCamara() {
        try {
            if (mRecording != null) {
                mRecording.stop();
                mRecording = null;
            }

            ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);

            cameraProviderListenableFuture.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        mCameraProvider = cameraProviderListenableFuture.get();
                        startCameraX(mCameraProvider);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }, getExecutor());
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Problema al inicializar la cámara (1)", t.getMessage(), null);
        }
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector;
        Preview preview;

        try {

            if (camaraOrigen == CAMARA_TRASERA) {
                // Camera selector use case
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
            } else {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
            }

            // Preview use case

            preview = new Preview.Builder().build();
            preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

            // Image capture use case

            mImageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

            // Video capture use case

            QualitySelector qualitySelector = QualitySelector.from(Quality.SD, FallbackStrategy.lowerQualityOrHigherThan(Quality.SD));

            mRecorder = new Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .build();

            mVideoCapture = VideoCapture.withOutput(mRecorder);
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Problema inesperado al inicializar la cámara (2)", t.getMessage(), null);
            return;
        }

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, mImageCapture, mVideoCapture);

        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Problema inesperado al inicializar la cámara (3)", t.getMessage(), null);
        }
    }

    private void cambiarCamara() {
        if (camaraOrigen == CAMARA_TRASERA)
            camaraOrigen = CAMARA_FRONTAL;
        else
            camaraOrigen = CAMARA_TRASERA;

        iniciarCamara();
    }

    private void capturarFoto() {

        long timeStamp = System.currentTimeMillis();
//        String photoFilePath = photoDir.getAbsolutePath() + "/" + timeStamp + ".jpg";
//
//        File photoFile = new File(photoFilePath);

        try {
            ContentValues contentValues = new ContentValues();

            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SOGES");
            }

            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();

            mImageCapture.takePicture(outputFileOptions, getExecutor(),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Toast.makeText(Camara2Activity.this, "Fotografía guardada.", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Toast.makeText(Camara2Activity.this, "Error al guardar fotografía: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Problema inesperado al tomar foto", t.getMessage(), null);
        }
    }


    @SuppressLint("RestrictedApi")
    private void capturarVideo() {
/*
        if (!grabandoVideo) {
            grabandoVideo = true;
            btnCapturarVideo.setText("Detener grabación");
            grabarVideo();
        } else {
            grabandoVideo = false;
            btnCapturarVideo.setText("Grabar video");
            videoCapture.stopRecording();
            btnRegresar.setVisibility(View.VISIBLE);
        }
*/
        if (!mGrabandoVideo) {
            iniciarGrabarVideo();
        } else {
            finalizarGrabacionDeVideo();
        }
    }

    @SuppressLint("RestrictedApi")
    private void iniciarGrabarVideo() {
        if (mVideoCapture == null) return;

        try {
            mCancelandoGrabacion = false;

            long timeStamp = System.currentTimeMillis();
            ContentValues contentValues = new ContentValues();

            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Movies/SOGES");
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions.Builder(getContentResolver(),
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI).build();

            mPendingRecording = mVideoCapture.getOutput().prepareRecording(this, mediaStoreOutputOptions);

            mRecording = mPendingRecording.start(getExecutor(), new Consumer<VideoRecordEvent>() {
                @Override
                public void accept(VideoRecordEvent videoRecordEvent) {
                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                        grabandoVideo();
                    } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                        VideoRecordEvent.Finalize videoRecordFinalize = (VideoRecordEvent.Finalize) videoRecordEvent;

                        if (videoRecordFinalize.hasError()) {
                            if (mRecording != null) mRecording.stop();
                            mRecording = null;
                            grabacionDeVideoFinalizada();
                            return;
                        }

                        if (!mCancelandoGrabacion) {
                            guardarVideo(videoRecordFinalize.getOutputResults().getOutputUri());
                            grabacionDeVideoFinalizada();
                        } else
                            borrarVideo(videoRecordFinalize.getOutputResults().getOutputUri());
                    }
                }
            });
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Problema inesperado al iniciar la grabación", t.getMessage(), null);
        }
    }

    private void grabandoVideo() {
        try {
            mGrabandoVideo = true;
            btnCapturarVideo.setText("Detener grabación");

            mContador = 0;

            habilitarBotones(false);

            txtIndicador.setVisibility(View.VISIBLE);
            imgGrabando.setVisibility(View.VISIBLE);

            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(500); //You can manage the blinking time with this parameter
            anim.setStartOffset(0);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);

            imgGrabando.setAnimation(anim);

            // Crear el handler que utilizará el thread de envío del video para que notifique
            // ... al thread principal del resultado.

            mHandler = new Handler();

            mTimer = new CountDownTimer(mDuracionVideoMseg, INTERVALO_CONTADOR) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mContador = mContador + INTERVALO_CONTADOR;

                    txtIndicador.setText(formatearHMS(mContador));
                    // txtIndicador.setText(formatearHMS(millisUntilFinished));
                }

                @Override
                public void onFinish() {
                    // txtIndicador.setVisibility(View.GONE);
                    if (mForzarDetenerVideo) {
                        btnCapturarVideo.setEnabled(false);
                        finalizarGrabacionDeVideo();
                    } else
                        btnCapturarVideo.setEnabled(true);
                }
            }.start();
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Problema inesperado durante la grabación", t.getMessage(), null);
        }
    }

    @SuppressLint("RestrictedApi")
    private void finalizarGrabacionDeVideo() {
        if (mRecording == null) return;

        try {
            mRecording.stop();
            mRecording = null;
        } catch (Throwable t) {
            mostrarMensaje("Alerta", "Problema inesperado al finalizar grabación", t.getMessage(), null);
        }
    }

    @SuppressLint("RestrictedApi")
    private void grabacionDeVideoFinalizada() {
        mGrabandoVideo = false;
        btnCapturarVideo.setText("Grabar video");
        imgGrabando.setVisibility(View.GONE);
        txtIndicador.setVisibility(View.GONE);
        mCancelandoGrabacion = false;
    }

    private void cancelarGrabacionVideo() {
        mCancelandoGrabacion = true;
        if (mRecording != null) {
            mRecording.stop();
            mRecording = null;
        }

        if (mTimer != null) {
            mTimer.cancel();
        }

        mCameraProvider.unbindAll();
        mGrabandoVideo = false;
        imgGrabando.setVisibility(View.GONE);
        txtIndicador.setVisibility(View.GONE);
        btnCapturarVideo.setText("Grabar video");
        btnCapturarVideo.setEnabled(true);
        btnCapturarVideo.setVisibility(View.VISIBLE);
        btnRegresar.setVisibility(View.GONE);

        habilitarBotones(true);
    }

    private void borrarVideo(Uri uri) {
        try {
            String archivo = uri.getPath();
            File file = new File(archivo);

            if (!file.delete()) {
                mostrarMensaje("Info", "No se pudo borrar video");
            }
        } catch (Throwable t) {
            mostrarMensaje("Info", "No se pudo borrar video", t.getMessage(), null);
        }
    }

    private void habilitarBotones(boolean habilitar) {
        btnCapturarVideo.setEnabled(habilitar);
        btnRegresar.setEnabled(habilitar);
        btnCambiarCamara.setEnabled(habilitar);
        btnFirmar.setEnabled(habilitar);
        btnSubirResolucion.setEnabled(habilitar);
        btnBajarResolucion.setEnabled(habilitar);
        btnFlash.setEnabled(habilitar);
    }

    private String formatearHMS(long nMillis) {
        // Used for formatting digit to be in 2 digits only
        NumberFormat f = new DecimalFormat("00");

        long hour = (nMillis / 3600000) % 24;
        long min = (nMillis / 60000) % 60;
        long sec = (nMillis / 1000) % 60;

        if (hour > 0)
            return f.format(hour) + ":" + f.format(min) + ":" + f.format(sec);
        else
            return f.format(min) + ":" + f.format(sec);
    }

    private void finalizarActivity() {
        setResult(Activity.RESULT_OK);
        this.finish();
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

    private void mostrarMensajeEnThreadUI(String mensaje) {
        mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(Camara2Activity.this, mensaje, Toast.LENGTH_LONG).show();
                //mostrarMensaje(titulo, mensaje, "", null);
            }
        });
    }

    /* -------------------------------------------------------------------------------------------
        Envía al webapi el video creado.
    ------------------------------------------------------------------------------------------- */

    private void enviarVideo(Uri uri) {
        String nombre;
        InputStream is;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] datos;
        String idOrdenStr;
        String fechaStr;

        try {
            is = getContentResolver().openInputStream(uri);

            datos = new byte[is.available()];
            is.read(datos);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        btnCapturarVideo.setEnabled(false);

        DatosEnvioEntity datosEnvio = new DatosEnvioEntity();

        idOrdenStr = Main.rellenaString(String.valueOf(mIdOrden), "0", 12, true);
        fechaStr = Main.obtieneFecha("ymdhis");

        datosEnvio.idOrden = mIdOrden;
        datosEnvio.Carpeta = fechaStr;
        datosEnvio.nombreArchivo = "video-" + idOrdenStr + "-" + fechaStr + ".mp4";

        try {
            enviarVideo(datosEnvio, datos);
        } catch (Throwable e) {
            e.printStackTrace();
        }


//        String[] projection = new String[] {
//                MediaStore.MediaColumns.DISPLAY_NAME,
//                MediaStore.MediaColumns.MIME_TYPE
//        };
//
//
//        String selection = MediaStore.Video.Media.DISPLAY_NAME + "= ?";
//
//        String[] selectionArgs = new String[] {
//                String.valueOf(uri)
//        };
//
//        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";
//
//        Cursor cursor = getApplicationContext().getContentResolver().query(
//                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//                projection,
//                selection,
//                selectionArgs,
//                sortOrder
//        );
//
//        try {
//            while (cursor.moveToNext()) {
//                nombre = Utils.getString(cursor, MediaStore.Video.Media.DISPLAY_NAME, "");
//                // Use an ID column from the projection to get
//                // a URI representing the media item itself.
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private int enviarVideo(DatosEnvioEntity datosEnvio, byte[] foto) throws Throwable {

        mEnviarProceso = new Thread() {
            int cantidad;

            public void run() {

                byte[] response = null;
                String ls_urlConArchivo;
                SubirFotoResponse respMedia;

                try {
                    if (foto != null) {
                        mostrarMensajeEnThreadUI("Se está transmitiendo el video. Espere...");

                        if (datosEnvio.Carpeta.equals("")) {
                            ls_urlConArchivo = "/" + datosEnvio.nombreArchivo;
                        } else {
                            ls_urlConArchivo = "/" + datosEnvio.Carpeta + "/" + datosEnvio.nombreArchivo;
                        }

                        SubirFotoRequest req = new SubirFotoRequest();

                        req.carpeta = "/" + datosEnvio.Carpeta;
                        req.ruta = ls_urlConArchivo;
                        req.nombre = datosEnvio.nombreArchivo;
                        req.idOrden = datosEnvio.idOrden;

                        respMedia = WebApiManager.getInstance(mGlobales.getApplicationContext()).subirVideo(req, foto);

                        if (respMedia == null)
                            throw new Exception("Error al enviar la video");

                        if (respMedia.NumError > 0)
                            throw new Exception("Error al enviar la video. " + respMedia.Mensaje);

                        regresarResultadoEnvio(true, null);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    regresarResultadoEnvio(false, t);
                }
            }
        };

        mEnviarProceso.start();

        return 0;
    }

    private void regresarResultadoEnvio(boolean exito, Throwable t) {
        mHandler.post(new Runnable() {
            public void run() {
                if (exito)
                    Toast.makeText(Camara2Activity.this, "Video guardado.", Toast.LENGTH_LONG).show();
                else
                    mostrarMensaje("Error", "No se pudo enviar el video", t, null);
                btnRegresar.setEnabled(true);
                btnRegresar.setVisibility(View.VISIBLE);
            }
        });
    }

    /* -------------------------------------------------------------------------------------------
        Envía al webapi el video creado.
    ------------------------------------------------------------------------------------------- */

    private void guardarVideo(Uri uri) {
        String idOrdenStr;
        String fechaStr;

        try {
            btnCapturarVideo.setEnabled(false);

            DatosEnvioEntity datosEnvio = new DatosEnvioEntity();

            idOrdenStr = Main.rellenaString(String.valueOf(mIdOrden), "0", 12, true);
            fechaStr = Main.obtieneFecha("ymdhis");

            datosEnvio.idOrden = mIdOrden;
            datosEnvio.Carpeta = fechaStr;
            datosEnvio.nombreArchivo = "video-" + idOrdenStr + "-" + fechaStr + ".mp4";

            guardarVideo(datosEnvio, uri);
            //enviarVideo(uri);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            btnRegresar.setEnabled(true);
            btnRegresar.setVisibility(View.VISIBLE);
        }
    }

    private void guardarVideo(DatosEnvioEntity datosEnvio, Uri uri) throws Exception {
        try {
            DBHelper dbHelper = DBHelper.getInstance(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            long id;

            mCv_datos = new ContentValues(4);

            mCv_datos.put("carpeta", datosEnvio.Carpeta);
            mCv_datos.put("nombre", datosEnvio.nombreArchivo);
            mCv_datos.put("idOrden", datosEnvio.idOrden);
            mCv_datos.put("uri", uri.toString());
            mCv_datos.put("envio", 1);

            id = db.insertOrThrow("videos", null, mCv_datos);

            db.close();
            dbHelper.close();

            if (id == 0)
                throw new Exception("Error al guardar video");
        } catch (Throwable t) {
            throw new Exception("Error al guardar referencia de video en base de datos", t);
        }
    }

    protected void onPause() {
        cancelarGrabacionVideo();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mDuracionVideoMseg > 0) {
            if (!mGrabandoVideo) {
                iniciarCamara();
            }
        }
        else {
            finalizarActivity();
            return;
        }
        super.onResume();
    }
}