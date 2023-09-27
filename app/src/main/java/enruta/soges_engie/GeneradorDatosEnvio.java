package enruta.soges_engie;

import android.database.Cursor;

import enruta.soges_engie.clases.Utils;

public class GeneradorDatosEnvio {
    private Cursor mCursor;

    public String generarInfoOrdenes(Cursor c, long idEmpleado) throws Exception  {
        String dato;
        String tipoRegistro = "ORD";

        if (c == null)
            return "";

        mCursor = c;

        dato = Utils.concatenarColumnas("|",
                tipoRegistro,
                getString("numOrden"),
                getString("estadoDeLaOrden"),
                getString("anomalia"),
                getString("serieMedidor"),
                getString("lectura"),
                getString("fecha") + getString("hora"),
                getString("sospechosa"),
                String.valueOf(idEmpleado),
                getString("fechaEnvio"),
                getString("fechaDeInicio"),
                getString("fecha") + getString("hora"),
                getString("comentarios"),
                getString("latitud"),
                getString("longitud"),
                getString("poliza"),
                getString("habitado"),
                getString("registro"),

                getString("idArchivo"),
                getString("idTarea"),
                getString("ciclo"),
                getString("numGrupo"),
                getString("idOrden")
        );

        return dato;
    }

    public String generarNoregistrado(Cursor c) throws Exception {
        String dato;

        if (c == null)
            return "";

        mCursor = c;

        dato = Utils.concatenarColumnas("|",
                getString("TipoRegistro"),
                getString("idLectura"),
                getString("idUnidadLect"),
                getString("idArchivo"),
                getString("idEmpleado"),
                getString("Calle"),
                getString("Colonia"),
                getString("NumMedidor"),
                getString("Lectura"),
                getString("Observaciones"),
                getString("rowid")
        );

        return dato;
    }

    private String getString(String columna) throws Exception {
        String dato;

        dato = Utils.getString(mCursor, columna, "");   // Obtener el dato de la base de datos SQL Lite
        dato = dato.replaceAll("\\|", " "); // Quitar el caracter pipe (|) por si se capturó

        return dato;
    }
}
