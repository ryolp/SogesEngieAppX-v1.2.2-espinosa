package enruta.soges_engie.clases;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import enruta.soges_engie.R;

public final class Utils {
    public static Date getDateTime() {
        Calendar calendar = Calendar.getInstance();

        return calendar.getTime();
    }

    public static void showMessageLong(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void showMessageShort(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void logMessageLong(Context context, String msg, Throwable t) {
        if (t != null)
            msg = msg + " : " + t.getMessage();
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        Log.d("CPL", msg);
    }

    public static void logMessageShort(Context context, String msg, Throwable t) {
        if (t != null)
            msg = msg + " : " + t.getMessage();
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        Log.d("CPL", msg);
    }

    public static String convToDateTimeStr(Date d, String... format){
        String fmt;
        String result;

        if (d == null)
            return "";

        if (format.length == 0)
            fmt = "yyyy-MM-dd";
        else
            fmt = format[0];

        SimpleDateFormat dateFormat = new SimpleDateFormat(fmt);

        result = dateFormat.format(d);

        return result;
    }

    public static Date convToDate(String value, String... format) throws Exception{
        String fmt;
        Date result;

        if (format.length == 0)
            fmt = "yyyyMMdd hhmmss";
        else
            fmt = format[0];

        SimpleDateFormat simpledateformat = new SimpleDateFormat(fmt);
        result = simpledateformat.parse(value);

        return result;
    }

    public static String ifNullStr(String s) {
        if (s == null)
            return "";
        else
            return s;
    }

    public static Date getFechaAgregarSegundos(int segundos){
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, segundos);
        return calendar.getTime();
    }

    public static int convToInt(String value, Integer...defaultValue) {
        int defaultValueAux = 0;

        try {
            if (defaultValue.length>0)
                defaultValueAux = defaultValue[0];

            return Integer.parseInt(value.trim());
        } catch (Exception e)
        {
            return defaultValueAux;
        }
    }

    public static long convToLong(String value, long...defaultValue) {
        long defaultValueAux = 0;

        try {
            if (defaultValue.length>0)
                defaultValueAux = defaultValue[0];

            return Integer.parseInt(value.trim());
        } catch (Exception e)
        {
            return defaultValueAux;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        if(context == null)  return false;


        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {


            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                }
            }
            else {
                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_statut", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_statut", "" + e.getMessage());
                }
            }
        }
        Log.i("update_statut","Network is available : FALSE ");
        return false;
    }

    public static void mostrarAlerta(Context context, String titulo, String ls_mensaje){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(ls_mensaje).setTitle(titulo)
                .setCancelable(false)
                .setNegativeButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void mostrarAlerta(Context context, String titulo, String ls_mensaje, DialogInterface.OnClickListener onClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(ls_mensaje).setTitle(titulo)
                .setCancelable(false)
                .setNegativeButton(R.string.aceptar, onClickListener);

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static String concatenar(String separador, String ... valores) {
        String s = "";

        for(String valor : valores ){
            if (!s.equals("") && !valor.equals(""))
                s = s + separador + valor;
            else if(!valor.equals(""))
                s = valor;
        }

        return s;
    }

    public static String concatenarColumnas(String separador, String ... valores) {
        String s = "";
        int i = 0;

        for(String valor : valores ){
            if (valor == null)
                valor = "";

            if (i == 0)
                s = valor;
            else
                s = s + separador + valor;

            i++;
        }

        return s;
    }

    public static int getInt(Cursor c, String columnName, int defaultValue)  throws Exception{
        int idx;

        if (c == null)
            throw new Exception("No se proporcionó el cursor");

        idx = c.getColumnIndex(columnName);

        if (idx < 0)
            throw new Exception("No existe la columna '" + columnName + "'");

        return c.getInt(idx);
    }

    public static long getLong(Cursor c, String columnName, long defaultValue)  throws Exception{
        int idx;
        long value;

        if (c == null)
            throw new Exception("No se proporcionó el cursor");

        idx = c.getColumnIndex(columnName);

        if (idx < 0)
            throw new Exception("No existe la columna '" + columnName + "'");

        return c.getLong(idx);
    }

    public static double getDouble(Cursor c, String columnName, long defaultValue)  throws Exception{
        int idx;
        double value;

        if (c == null)
            throw new Exception("No se proporcionó el cursor");

        idx = c.getColumnIndex(columnName);

        if (idx < 0)
            throw new Exception("No existe la columna '" + columnName + "'");

        return c.getDouble(idx);
    }

    public static String getString(Cursor c, String columnName, String defaultValue)  throws Exception{
        int idx;
        String value;

        if (c == null)
            throw new Exception("No se proporcionó el cursor");

        idx = c.getColumnIndex(columnName);

        if (idx < 0)
            throw new Exception("No existe la columna '" + columnName + "'");

        value = c.getString(idx);

        if (value == null)
            value = defaultValue;

        return value;
    }

    public static byte[] getBlob(Cursor c, String columnName) throws Exception {
        int idx;

        if (c == null)
            throw new Exception("No se proporcionó el cursor");

        idx = c.getColumnIndex(columnName);

        if (idx < 0)
            throw new Exception("No existe la columna '" + columnName + "'");

        return c.getBlob(idx);
    }

    public static String convToStr(long n)
    {
        return String.valueOf(n);
    }

    public static String convToStr(int n) {
        return String.valueOf(n);
    }
}
