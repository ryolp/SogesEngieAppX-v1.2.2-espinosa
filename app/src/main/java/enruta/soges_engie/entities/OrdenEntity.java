package enruta.soges_engie.entities;

public class OrdenEntity {
    public long idCiclo  = 0;
    public long idArchivo  = 0;
    public long idTarea  = 0;
    public long idOrden  = 0;
    public long idEmpleado  = 0;
    public String Ciclo  = "";
    public String NumOrden  = "";
    public int NumSecuencia  = 0;
    public String NumGrupo  = "";
    public String Indicador = "";
    public String Poliza  = "";
    public String Cliente  = "";
    public String Calle  = "";
    public String NumExterior  = "";
    public String NumInterior  = "";
    public String Colonia  = "";
    public String Municipio  = "";
    public String Estado  = "";
    public String EntreCalles  = "";
    public String ComoLlegar  = "";
    public String AvisoAlLector  = "";
    public String MarcaMedidor  = "";
    public String TipoMedidor  = "";
    public String EstadoDelServicio  = "";
    public String Tarifa  = "";
    public String TipoDeOrden  = "";
    public String TimeOfLife  = "";
    public String MedTimeOfLife  = "";
    public String FechaDeAsignacion  = "";
    public String Anio  = "";
    public String NumSello  = "";
    public String SerieMedidor  = "";
    public String Vencido  = "";
    public String Balance  = "";
    public String UltimoPago  = "";
    public String FechaUltimoPago  = "";
    public String Giro  = "";
    public String DiametroToma  = "";
    public String sectorCorto  = "";
    public String numEsferas  = "";
    public String lecturaAnterior  = "";
    public String promedio  = "";
    public String cvelectura  = "";
    public String economico  = "";
    public String contrato  = "";
    public String clave_usuario  = "";
    public String tu  = "";
    public String codigoBarras  = "";
    public String TipoAcuse  = "";
    public String UbicacionMedidor  = "";

    public String Estimaciones  = "";
    public String TipoCliente  = "";
    public String TextoLibre  = "";

//************************************************************************************************************************************
// CE, 06/10/23, Aqui debemos tener una variable de todos los CamposEngie que que vamos a recibir del servidor
    public String miLatitud = "";
    public String miLongitud = "";
    public String MensajeOut = "";
    public String NumAviso = "";
    public String CuentaContrato = "";
    public String idMaterialSolicitado = "";
    
    public int EncuestaDeSatisfaccion = 0;
    public String MedidorInstalado = "";
    public int idMarcaInstalada = 0;
    public String LecturaReal = "";
    public String Repercusion = "";
    public String idMaterialUtilizado = "";
    public int idTipoDeReconexion = 0;
    public int idTipoDeRemocion = 0;
	public String ClienteYaPagoMonto = "";    
	public String ClienteYaPagoFecha = "";    
	public String ClienteYaPagoAgente = "";    
//************************************************************************************************************************************

    public long id;
}
