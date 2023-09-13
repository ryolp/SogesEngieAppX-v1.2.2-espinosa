package enruta.soges_engie.clases;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import enruta.soges_engie.DBHelper;
import enruta.soges_engie.Globales;

public class BaseMgr {
    protected Context mContext = null;
    protected Globales mGlobales = null;
    protected DBHelper mDbHelper = null;
    protected SQLiteDatabase mDb = null;

    protected void openDatabase() {
        if (mDbHelper == null)
            mDbHelper = new DBHelper(mContext);

        if (mDb == null)
            mDb = mDbHelper.getReadableDatabase();
    }

    public void closeDatabase() {
        if (mDb != null)
            if (mDb.isOpen()) {
                mDb.close();
                mDbHelper.close();
            }

        if (mDbHelper != null)
            mDbHelper.close();
    }

    protected int getInt(Cursor c, String columnName, int defaultValue) {
        int idx;

        if (c == null)
            return defaultValue;

        idx = c.getColumnIndex(columnName);

        if (idx < 0)
            return defaultValue;

        return c.getInt(idx);
    }

    protected long getLong(Cursor c, String columnName, long defaultValue) {
        int idx;

        if (c == null)
            return defaultValue;

        idx = c.getColumnIndex(columnName);

        if (idx < 0)
            return defaultValue;

        return c.getLong(idx);
    }

    protected String getString(Cursor c, String columnName, String defaultValue) {
        int idx;
        String value;

        if (c == null)
            return defaultValue;

        idx = c.getColumnIndex(columnName);

        if (idx < 0)
            return defaultValue;

        value = c.getString(idx);

        if (value == null)
            value = defaultValue;

        return value;
    }

    protected byte[] getBlob(Cursor c, String columnName) {
        int idx;

        if (c == null)
            return null;

        idx = c.getColumnIndex(columnName);

        if (idx < 0)
            return null;

        return c.getBlob(idx);
    }
}
