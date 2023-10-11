package enruta.soges_engie;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.UseCase;
import androidx.camera.core.impl.ImageCaptureConfig;

import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoRecordEvent;

import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import enruta.soges_engie.clases.Utils;
import enruta.soges_engie.entities.DatosEnvioEntity;
import enruta.soges_engie.entities.SubirFotoRequest;
import enruta.soges_engie.entities.SubirFotoResponse;
import enruta.soges_engie.services.WebApiManager;

public class Camara2Activity extends AppCompatActivity {
    // RL, 2023-10-04, Declaraciones para el uso de la cámara

    private static final int CAMARA_TRASERA = 1;
    private static final int CAMARA_FRONTAL = 2;

    private Globales mGlobales;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private Recording recording = null;
    private Recorder recorder = null;
    //private VideoCapture<Recorder> videoCapture2;
    private ExecutorService service;
    private boolean grabandoVideo = false;
    private int camaraOrigen = CAMARA_TRASERA;

    // RL, 2023-10-04, variables de los controles
    private Button btnCapturarFoto;
    private Button btnCapturarVideo;
    private ImageButton btnCambiarCamara;
    private Button btnRegresar;

    // RL, 2023-10-04, diálogo para mostrar mensajes de error
    private DialogoMensaje mDialogoMsg = null;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara2);

        mGlobales = ((Globales) getApplicationContext());

        service = Executors.newSingleThreadExecutor();

        Bundle bu_params = getIntent().getExtras();



        inicializarControles();
        inicializarEventosControles();
        obtenerParametros();
    }

    void obtenerParametros() {
        Bundle bu_params = getIntent().getExtras();

        secuencial = bu_params.getInt("secuencial");
        caseta = bu_params.getString("caseta");
        is_terminacion = bu_params.getString("terminacion");
        temporal = bu_params.getInt("temporal");
        cantidad = bu_params.getInt("cantidad");
        mTipoFoto = bu_params.getInt("TipoFoto");
        mIdOrden = bu_params.getLong("idOrden");
    }

    void inicializarControles() {
        btnCapturarFoto = (Button) findViewById(R.id.btnCapturarFoto);
        btnCapturarVideo = (Button) findViewById(R.id.btnCapturarVideo);
        btnCambiarCamara = (ImageButton) findViewById(R.id.btnCambiarCamara);
        btnRegresar = (Button) findViewById(R.id.btnRegresar);

        previewView = (PreviewView) findViewById(R.id.previewView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
    }

    void inicializarEventosControles() {
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

        iniciarCamara();


    }

    private void iniciarCamara() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, getExecutor());
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector;

        cameraProvider.unbindAll();

//        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
//                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
//                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
//                .build();

        if (camaraOrigen == CAMARA_TRASERA) {
            // Camera selector use case
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();
        }
        else
        {
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();
        }

        // Preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // Video capture use case


        //Recorder recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.SD)).build();

        //videoCapture = VideoCapture.withOutput();

        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .setTargetResolution(new Size(640, 480))
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture, videoCapture);
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

        ContentValues contentValues = new ContentValues();

        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
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
    }




    @SuppressLint("RestrictedApi")
    private void capturarVideo() {
        if (!grabandoVideo) {
            grabandoVideo = true;
            btnCapturarVideo.setText("Grabando video");
            grabarVideo();
        } else {
            grabandoVideo = false;
            btnCapturarVideo.setText("Grabar video");
            videoCapture.stopRecording();
        }
    }

    @SuppressLint("RestrictedApi")
    private void grabarVideo()
    {
        if (videoCapture != null) {
            long timeStamp = System.currentTimeMillis();
            ContentValues contentValues = new ContentValues();

            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

//            File videoDir = new File(Environment.getExternalStorageDirectory() + "/CameraXVideos");
//
//            if (!videoDir.exists())
//                videoDir.mkdir();
//
//            Date date = new Date();
//            String timeStamp = String.valueOf(date.getTime());
//            String videoFilePath = videoDir.getAbsolutePath() + "/" + timeStamp + ".mp4";

            //File vidFile = new File(videoFilePath);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            videoCapture.startRecording(
                    new VideoCapture.OutputFileOptions.Builder(
                            getContentResolver(),
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            contentValues).build(),
                    getExecutor(),
                    new VideoCapture.OnVideoSavedCallback() {
                        @Override
                        public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                            Toast.makeText(Camara2Activity.this, "Video guardado.", Toast.LENGTH_LONG).show();
                            enviarVideo(outputFileResults.getSavedUri());
                        }

                        @Override
                        public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                            Toast.makeText(Camara2Activity.this, "Error al guardar vodep: " + message, Toast.LENGTH_LONG).show();
                        }
                    }
            );
        }
    }

    @SuppressLint("RestrictedApi")
    private void grabarVideo2()
    {
        if (videoCapture != null) {
            long timeStamp = System.currentTimeMillis();
            ContentValues contentValues = new ContentValues();

            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

//            File videoDir = new File(Environment.getExternalStorageDirectory() + "/CameraXVideos");
//
//            if (!videoDir.exists())
//                videoDir.mkdir();
//
//            Date date = new Date();
//            String timeStamp = String.valueOf(date.getTime());
//            String videoFilePath = videoDir.getAbsolutePath() + "/" + timeStamp + ".mp4";

            //File vidFile = new File(videoFilePath);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return;
            }



            videoCapture.startRecording(
                    new VideoCapture.OutputFileOptions.Builder(
                            getContentResolver(),
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            contentValues).build(),
                    getExecutor(),
                    new VideoCapture.OnVideoSavedCallback() {
                        @Override
                        public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                            Toast.makeText(Camara2Activity.this, "Video guardado.", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                            Toast.makeText(Camara2Activity.this, "Error al guardar vodep: " + message, Toast.LENGTH_LONG).show();
                        }
                    }
            );
        }
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
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };

        mEnviarProceso.start();

        return 0;
    }
}