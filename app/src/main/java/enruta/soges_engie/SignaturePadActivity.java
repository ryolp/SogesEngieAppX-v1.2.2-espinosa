package enruta.soges_engie;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class SignaturePadActivity extends Activity {

    Globales globales;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private SignaturePad mSignaturePad;
    private Button mClearButton;
    private Button mSaveButton;
    private Button mSaveButton2;
    private Button mSaveButton3;
    private Button mSaveButton4;
    private Button mSaveButton5;

    private ContentValues cv_datos;
    private long secuencial;
    private String is_terminacion = "-A", is_anomalia = "";
    private String ls_nombre, caseta;
    private int temporal, cantidad;
    private long idOrden;
    private SignaturePadActivity thisIsMe;

    private boolean mInicializado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
        setContentView(R.layout.activity_signature_pad);

        globales = ((Globales) getApplicationContext());

        Bundle bu_params = getIntent().getExtras();
        secuencial = bu_params.getInt("secuencial");
        caseta = bu_params.getString("caseta");
        is_terminacion = bu_params.getString("terminacion");
        ls_nombre = bu_params.getString("ls_nombre");
        temporal = bu_params.getInt("temporal");
        cantidad = bu_params.getInt("cantidad");
        idOrden = bu_params.getLong("idOrden");
        thisIsMe = this;

        inicializar();
    }

    private void inicializar() {
        try {
            mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
            mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
                @Override
                public void onStartSigning() {
//                    Toast.makeText(SignaturePadActivity.this, "OnStartSigning", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSigned() {
                    mSaveButton.setEnabled(true);
                    mSaveButton2.setEnabled(true);
                    mSaveButton3.setEnabled(true);
                    mSaveButton4.setEnabled(true);
                    mSaveButton5.setEnabled(true);
                    mClearButton.setEnabled(true);
                }

                @Override
                public void onClear() {
                    mSaveButton.setEnabled(false);
                    mSaveButton2.setEnabled(false);
                    mSaveButton3.setEnabled(false);
                    mSaveButton4.setEnabled(false);
                    mSaveButton5.setEnabled(false);
                    mClearButton.setEnabled(false);
                }
            });

            mSaveButton = (Button) findViewById(R.id.save_button);
            mSaveButton2 = (Button) findViewById(R.id.save_button2);
            mSaveButton3 = (Button) findViewById(R.id.save_button3);
            mSaveButton4 = (Button) findViewById(R.id.save_button4);
            mSaveButton5 = (Button) findViewById(R.id.save_button5);
            mClearButton = (Button) findViewById(R.id.clear_button);

            mClearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        mSignaturePad.clear();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });

            mSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        byte[] firmaAGuardar = out.toByteArray();
                        long idFoto;

                        globales.tll.getLecturaActual().is_EncuestaDeSatisfaccion = "1";
                        cv_datos = new ContentValues(4);
                        cv_datos.put("secuencial", secuencial);
                        cv_datos.put("nombre", ls_nombre);
                        cv_datos.put("foto", firmaAGuardar);
                        cv_datos.put("envio", TomaDeLecturas.NO_ENVIADA);
                        cv_datos.put("temporal", temporal);
                        cv_datos.put("idOrden", idOrden);

                        DBHelper dbHelper = new DBHelper(thisIsMe);
                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                        idFoto = db.insertOrThrow("fotos", null, cv_datos);
                        db.close();
                        dbHelper.close();
                        thisIsMe.finish();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                                           });

            mSaveButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            byte[] firmaAGuardar = out.toByteArray();
                            long idFoto;

                            globales.tll.getLecturaActual().is_EncuestaDeSatisfaccion = "2";
                            cv_datos = new ContentValues(4);
                            cv_datos.put("secuencial", secuencial);
                            cv_datos.put("nombre", ls_nombre);
                            cv_datos.put("foto", firmaAGuardar);
                            cv_datos.put("envio", TomaDeLecturas.NO_ENVIADA);
                            cv_datos.put("temporal", temporal);
                            cv_datos.put("idOrden", idOrden);

                            DBHelper dbHelper = new DBHelper(thisIsMe);
                            SQLiteDatabase db = dbHelper.getReadableDatabase();
                            idFoto = db.insertOrThrow("fotos", null, cv_datos);
                            db.close();
                            dbHelper.close();
                            thisIsMe.finish();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                });

                    mSaveButton3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                byte[] firmaAGuardar = out.toByteArray();
                                long idFoto;

                                globales.tll.getLecturaActual().is_EncuestaDeSatisfaccion = "3";
                                cv_datos = new ContentValues(4);
                                cv_datos.put("secuencial", secuencial);
                                cv_datos.put("nombre", ls_nombre);
                                cv_datos.put("foto", firmaAGuardar);
                                cv_datos.put("envio", TomaDeLecturas.NO_ENVIADA);
                                cv_datos.put("temporal", temporal);
                                cv_datos.put("idOrden", idOrden);

                                DBHelper dbHelper = new DBHelper(thisIsMe);
                                SQLiteDatabase db = dbHelper.getReadableDatabase();
                                idFoto = db.insertOrThrow("fotos", null, cv_datos);
                                db.close();
                                dbHelper.close();
                                thisIsMe.finish();
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                });

                                    mSaveButton4.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                    byte[] firmaAGuardar = out.toByteArray();
                                    long idFoto;

                                    globales.tll.getLecturaActual().is_EncuestaDeSatisfaccion = "4";
                                    cv_datos = new ContentValues(4);
                                    cv_datos.put("secuencial", secuencial);
                                    cv_datos.put("nombre", ls_nombre);
                                    cv_datos.put("foto", firmaAGuardar);
                                    cv_datos.put("envio", TomaDeLecturas.NO_ENVIADA);
                                    cv_datos.put("temporal", temporal);
                                    cv_datos.put("idOrden", idOrden);

                                    DBHelper dbHelper = new DBHelper(thisIsMe);
                                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                                    idFoto = db.insertOrThrow("fotos", null, cv_datos);
                                    db.close();
                                    dbHelper.close();
                                    thisIsMe.finish();
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                });

                            mSaveButton5.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                                        signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                        byte[] firmaAGuardar = out.toByteArray();
                                        long idFoto;

                                        globales.tll.getLecturaActual().is_EncuestaDeSatisfaccion = "5";
                                        cv_datos = new ContentValues(4);
                                        cv_datos.put("secuencial", secuencial);
                                        cv_datos.put("nombre", ls_nombre);
                                        cv_datos.put("foto", firmaAGuardar);
                                        cv_datos.put("envio", TomaDeLecturas.NO_ENVIADA);
                                        cv_datos.put("temporal", temporal);
                                        cv_datos.put("idOrden", idOrden);

                                        DBHelper dbHelper = new DBHelper(thisIsMe);
                                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                                        idFoto = db.insertOrThrow("fotos", null, cv_datos);
                                        db.close();
                                        dbHelper.close();
                                        thisIsMe.finish();
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                    }
                                }
                });

            mInicializado = true;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        mInicializado = false;
        try {
            mSignaturePad.clear();
            mSignaturePad.setOnSignedListener(null);
            mSignaturePad = null;
            thisIsMe.finish();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (!mInicializado)
            inicializar();

        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SignaturePadActivity.this, "Cannot write images to external storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public File getAlbumStorageDir(String albumName) throws Exception {
        try {
            // Get the directory for the user's public pictures directory.
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), albumName);
            if (!file.mkdirs()) {
                Log.e("SignaturePad", "Directory not created");
            }
            return file;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception("Error al obtener lugar donde guardar la firma", t);
        }
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    public boolean addJpgSignatureToGallery(Bitmap signature) throws Exception {
        boolean result = false;
        try {
            File photo = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signature, photo);
            scanMediaFile(photo);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable t) {
            throw new Exception("Error al guardar la imagen de la firma", t);
        }
        return result;
    }

    private void scanMediaFile(File photo) {
        try {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(photo);
            mediaScanIntent.setData(contentUri);
            SignaturePadActivity.this.sendBroadcast(mediaScanIntent);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public boolean addSvgSignatureToGallery(String signatureSvg) throws Exception {
        boolean result = false;

        try {
            File svgFile = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.svg", System.currentTimeMillis()));
            OutputStream stream = new FileOutputStream(svgFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(signatureSvg);
            writer.close();
            stream.flush();
            stream.close();
            scanMediaFile(svgFile);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable t) {
            throw new Exception("Error al guardar imagen", t);
        }
        return result;
    }

    /**
     * Checks if the app has permission to write to device storage
     * <p/>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity the activity from which permissions are checked
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions
                    (
                            activity,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
        }
    }
}
