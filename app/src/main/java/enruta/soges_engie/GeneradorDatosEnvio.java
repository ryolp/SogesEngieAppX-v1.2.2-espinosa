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

        try {
            String strAnomalia = getString("anomalia");
            String strLectura = getString("lectura");
            String strLecturaReal = getString("LecturaReal");
            String strComentarios = getString("comentarios");
            if (strAnomalia.equals("E")) {
                strLectura = strComentarios;
                strLecturaReal = strComentarios;
                strComentarios = "";
            }
            dato = Utils.concatenarColumnas("|",
                    tipoRegistro,
                    getString("numOrden"),
                    getString("estadoDeLaOrden"),
                    strAnomalia,
                    getString("serieMedidor"),
                    strLectura,
                    getString("fecha") + getString("hora"),     // Fecha de Ejecución
                    getString("sospechosa"),
                    String.valueOf(idEmpleado),
                    getString("fechaDeRecepcion"),                      // Momento en que el celular recibe la orden
                    //getString("fechaEnvio"),                            // En Soges Web, se llama FechaRecepcion
                    getString("fechaDeInicio"),
                    getString("fecha") + getString("hora"),     // Fecha de Ejecución
                    strComentarios,
                    getString("latitud"),
                    getString("longitud"),
                    getString("poliza"),
                    getString("habitado"),
                    getString("registro"),

                    getString("idArchivo"),
                    getString("idTarea"),
                    getString("ciclo"),
                    getString("numGrupo"),
                    getString("idOrden"),
//************************************************************************************************************************************
// CE, 06/10/23, Aqui vamos a poner todos los CamposEngie que vamos a enviar de regreso al servidor
                getString("EncuestaDeSatisfaccion"),
                getString("MedidorInstalado"),
                getString("idMarcaInstalada"),
                strLecturaReal,
                getString("Repercusion"),
                getString("idMaterialUtilizado"),
                getString("idTipoDeReconexion"),
                getString("idTipoDeRemocion"),
                getString("ClienteYaPagoMonto"),
                getString("ClienteYaPagoFecha"),
                getString("ClienteYaPagoAgente"),
                getString("QuienAtendio"),
                getString("MarcaInstalada"),
                getString("SeQuitoTuberia"),
                getString("TuberiaRetirada"),
                getString("MarcaRetirada"),
                getString("MedidorRetirado")
//************************************************************************************************************************************
            );
            return dato;
        } catch (Throwable t) {
            throw new Exception("Error al leer información para ser enviada");
        }
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
