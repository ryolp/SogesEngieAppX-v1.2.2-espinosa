package enruta.soges_engie.services;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import enruta.soges_engie.DBHelper;
import enruta.soges_engie.BuildConfig;
import enruta.soges_engie.DBHelper;
import enruta.soges_engie.clases.AppException;
import enruta.soges_engie.clases.Utils;

public class DbConfigMgr extends  DbBaseMgr {
    private static DbConfigMgr config;
    private DBHelper dbHelper;
    private SQLiteDatabase mDb;

    private DbConfigMgr() {

    }

    public static DbConfigMgr getInstance(){
        config = new DbConfigMgr();
        return config;
    }

    private void openDatabase(Context context) {
        dbHelper = new DBHelper(context);

        mDb = dbHelper.getReadableDatabase();
    }

    private void closeDatabase() {
        mDb.close();
        dbHelper.close();
    }

    public String getServidor(Context context){
        String servidor = BuildConfig.BASE_URL;

        try {
            openDatabase(context);

            Cursor c = mDb.rawQuery("Select value from config where key='server_gprs'", null);
            c.moveToFirst();

            if (c.getCount() == 0)
                return BuildConfig.BASE_URL;

            servidor = getString(c,"value", "");

        } catch (Exception e) {
            String error;
            servidor = BuildConfig.BASE_URL;
            error = e.getMessage();
        }
        finally {
            closeDatabase();
            return servidor;
        }
    }

    public String getArchivo(Context context){
        String archivo="";

        try {
            openDatabase(context);

            Cursor c = mDb.rawQuery("Select value from config where key='cpl'", null);
            c.moveToFirst();

            if (c.getCount() == 0)
                return "";

            archivo = getString(c,"value", "");

        } catch (Exception e) {
            String error;
            archivo = "";
            error = e.getMessage();
        }
        finally {
            closeDatabase();
            return archivo;
        }
    }

//    public void actualizarParametros(SQLiteDatabase db, String linea) {
//        String[] camposStr = linea.split("\\|", -1);
//        int numCampos;
//        ParametrosTPL param = new ParametrosTPL();
//
//        if (camposStr == null) // Si al separar los campos es un valor nulo, la estructura del dato no es correcta
//            throw new AppException("Formato de parámetros incorrectos");
//
//        if (camposStr.length < 2)  // Si al separar los campos no hay al menos 2 columnas, la estructura del dato no es correcta
//            throw new AppException("Formato de parámetros incorrectos");
//
//        if (!camposStr[0].equals("P"))  // Si la 1er columna no empieza con P, la estructura del dato no es correcta.
//            throw new AppException("Formato de parámetros incorrectos");
//
//        numCampos = Utils.convToInt(camposStr[1]);
//
//        if (numCampos < 5)  // Si la 2a columna viene una cantidad de campos menor a la esperada, entonces la estructura del dato no es correcta.
//            throw new AppException("Cantidad de parámetros incorrecto");
//
//        param.intercambiarSerieMedidor = Utils.convToInt(camposStr[2]);
//        param.alinearDerechaNumMedidor = Utils.convToInt(camposStr[3]);
//        param.ModoCapturaClave = Utils.convToInt(camposStr[4]);
//
//        actualizarParametros(db, param);
//    }
//
//    public void actualizarParametros(SQLiteDatabase db, ParametrosTPL param) {
//        mDb = db;
//
//        setIntercambiarSerieMedidor(param.intercambiarSerieMedidor);
//        setAlinearDerechaNumMedidor(param.alinearDerechaNumMedidor);
//    }

    public int getIntercambiarSerieMedidor(Context context) throws Exception {
        int valor;

        try {
            openDatabase(context);

            Cursor c = mDb.rawQuery("SELECT value FROM config WHERE key='IntercambiarSerieMedidor'", null);
            c.moveToFirst();

            if (c.getCount() == 0) {
                closeDatabase();
                return 0;
            }
            else {
                valor = Utils.getInt(c, "value", 0);
                closeDatabase();
                return valor;
            }
        } catch (Throwable e) {
            closeDatabase();
            throw new Exception("Error en getIntercambiarSerieMedidor");
        }
    }

    public int getAlinearDerechaNumMedidor(Context context) throws Exception {
        int valor;

        try {
            openDatabase(context);
            Cursor c = mDb.rawQuery("SELECT value FROM config WHERE key='AlinearDerechaNumMedidor'", null);
            c.moveToFirst();

            if (c.getCount() == 0) {
                closeDatabase();
                return 0;
            } else {
                valor =  Utils.getInt(c, "value", 0);
                closeDatabase();
                return valor;
            }
        } catch (Throwable e) {
            closeDatabase();
            throw new Exception("Error en getAlinearDerechaNumMedidor");
        }
    }

    public int getIdModoCaptura(Context context) throws Exception {
        int valor;

        try {
            openDatabase(context);
            Cursor c = mDb.rawQuery("SELECT value FROM config WHERE key='ModoCapturaClave'", null);
            c.moveToFirst();

            if (c.getCount() == 0) {
                closeDatabase();
                return 0;
            } else {
                valor =  Utils.getInt(c, "value", 0);
                closeDatabase();
                return valor;
            }
        } catch (Throwable e) {
            closeDatabase();
            throw new Exception("Error en getIdModoCaptura");
        }
    }

    public void setIntercambiarSerieMedidor(int value) {
        Cursor c;
        ContentValues cv_params = new ContentValues(2);

        c = mDb.rawQuery("Select value from config where key='IntercambiarSerieMedidor'", null);
        c.moveToFirst();

        if (c.getCount() == 0) {

            cv_params.put("key", "IntercambiarSerieMedidor");
            cv_params.put("value", value);

            mDb.insert("config", null, cv_params);
        }
        else
        {
            String whereClause="key=?";
            String[] whereArgs={"IntercambiarSerieMedidor"};

            cv_params.put("value", value);

            mDb.update("config", cv_params, whereClause, whereArgs);
        }
    }

    public void setAlinearDerechaNumMedidor(int value) {
        Cursor c;
        ContentValues cv_params = new ContentValues(2);

        c = mDb.rawQuery("Select value from config where key='AlinearDerechaNumMedidor'", null);
        c.moveToFirst();

        if (c.getCount() == 0) {

            cv_params.put("key", "AlinearDerechaNumMedidor");
            cv_params.put("value", value);

            mDb.insert("config", null, cv_params);
        }
        else
        {
            String whereClause="key=?";
            String[] whereArgs={"AlinearDerechaNumMedidor"};

            cv_params.put("value", value);

            mDb.update("config", cv_params, whereClause, whereArgs);
        }
    }

    public void setIdModoCaptura(int value) {
        Cursor c;
        ContentValues cv_params = new ContentValues(2);

        c = mDb.rawQuery("Select value from config where key='ModoCapturaClave'", null);
        c.moveToFirst();

        if (c.getCount() == 0) {

            cv_params.put("key", "ModoCapturaClave");
            cv_params.put("value", value);

            mDb.insert("config", null, cv_params);
        }
        else
        {
            String whereClause="key=?";
            String[] whereArgs={"ModoCapturaClave"};

            cv_params.put("value", value);

            mDb.update("config", cv_params, whereClause, whereArgs);
        }
    }
}
