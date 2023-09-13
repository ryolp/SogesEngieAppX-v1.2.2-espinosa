package enruta.soges_engie.clases;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class FotosMgr extends BaseMgr {
    private byte[] imagen = null;
    private SQLiteDatabase mDb;
    private final long MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB

    public byte[] obtenerFoto(SQLiteDatabase db, String nombreFoto, long imageSize) throws Exception
    {
        mDb = db;

        try {
            if (imageSize <= MAX_IMAGE_SIZE)
                return obtenerFoto1(nombreFoto);        // Para guardar fotos menores a 2MB
            else
                return obtenerFoto2(nombreFoto, imageSize);        // Para guardar fotos mayores a 2MB, hacerlo por bloques
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    private byte[] obtenerFoto1(String nombreFotoPadre) throws Exception
    {
        String query;
        Cursor cFoto = null;
        String nombreFoto;
        File archivoFoto = null;
        byte[] imagen = null;
        FileOutputStream fsFoto = null;

        try {
            query = "Select nombre, foto from fotos where nombre = '" + nombreFotoPadre + "'";
            cFoto = mDb.rawQuery(query, null);

            while (cFoto.moveToNext()) {

                nombreFoto = getString(cFoto, "nombre", "");

                imagen = getBlob(cFoto, "foto");
            }
        } catch (Exception e) {
            throw new Exception("Error al obtener foto :" + e.getMessage());
        } finally {
            if (cFoto != null) {
                try {
                    cFoto.close();
                } catch (Exception e) {
                }
            }

            return imagen;
        }
    }

    private byte[] obtenerFoto2(String nombreFoto, long imageSize) throws Exception
    {
        String query;
        Cursor cFoto = null;
        File archivoFoto = null;
        byte[] image = null;
        ByteArrayOutputStream outputStream;
        FileOutputStream fsFoto = null;
        long idx = 0;
        long actualImageSize;
        long sizeToCopy;
        final long IMAGE_BLOCK_SIZE = 1 * 1024 * 1024; // 1MB

        try {
            actualImageSize = imageSize;

            outputStream = new ByteArrayOutputStream( );

            // Obtener la imagen del campo blob en bloques de 1MB

            while (actualImageSize > 0) {
                if (actualImageSize > IMAGE_BLOCK_SIZE)
                    sizeToCopy = IMAGE_BLOCK_SIZE;
                else
                    sizeToCopy = actualImageSize;

                query = "Select nombre, substr(foto," + String.valueOf(idx) + ","+ String.valueOf(sizeToCopy)+"  ) fotoParcial from fotos where nombre = '" + nombreFoto + "'";
                cFoto = mDb.rawQuery(query, null);

                while (cFoto.moveToNext()) {

                    nombreFoto = getString(cFoto, "nombre", "");

                    outputStream.write(getBlob(cFoto, "fotoParcial"));
                }

                cFoto.close();
                cFoto = null;

                idx += sizeToCopy;
                actualImageSize -= sizeToCopy;
            }

            // Si se pudo obtener todos los bytes de la foto, se escribe en el almacenamiento SD

            if (idx == imageSize) {
                image = outputStream.toByteArray();

                return image;
            }
            else
                throw new Exception("Error al obtener la foto para imagenes mayores a 2MB");
        } catch (Exception e) {
            throw new Exception("Error al obtener la foto para imagenes mayores a 2MB : "+e.getMessage());
        } finally {
            if (cFoto != null) {
                try {
                    cFoto.close();
                } catch (Exception e) {
                }

                cFoto = null;
            }
        }
    }
}
