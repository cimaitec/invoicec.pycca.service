package com.tradise.reportes.reportes;
 
import com.sun.DAO.Destinatarios;
import com.sun.DAO.DetalleTotalImpuestos;
import com.sun.reportes.detalles.DetallesAdicionales;
import com.sun.reportes.detalles.InfoAdicional;
import com.tradise.reportes.entidades.DetalleGuiaRemision;
import com.tradise.reportes.entidades.FacCabDocumento;
import com.tradise.reportes.entidades.FacDetAdicional;
import com.tradise.reportes.entidades.FacDetDocumento;
import com.tradise.reportes.entidades.FacDetMotivosdebito;
import com.tradise.reportes.entidades.FacDetRetencione;
import com.tradise.reportes.entidades.FacEmpresa;
import com.tradise.reportes.entidades.FacGeneral;
import com.tradise.reportes.entidades.FacProducto;
import com.tradise.reportes.servicios.ReporteServicio;
import com.util.util.key.Environment;
import com.util.util.key.GenericTransaction;

import ec.gob.sri.comprobantes.administracion.modelo.Emisor;
import ec.gob.sri.comprobantes.modelo.reportes.DetalleGuiaReporte;
import ec.gob.sri.comprobantes.modelo.reportes.DetallesAdicionalesReporte;
import ec.gob.sri.comprobantes.modelo.reportes.GuiaRemisionReporte;
import ec.gob.sri.comprobantes.modelo.reportes.DetalleGuiaReporte;
import ec.gob.sri.comprobantes.modelo.reportes.InformacionAdicional;
import ec.gob.sri.comprobantes.sql.EmisorSQL;
import ec.gob.sri.comprobantes.util.reportes.JasperViwerSRI;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JRSaveContributor;
import net.sf.jasperreports.view.save.JRPdfSaveContributor;

public class ReporteUtil extends GenericTransaction
{
	private String Ruc;
	private String codEst;
	private String codPuntEm;
	private String codDoc;
	private String secuencial;
	private ReporteServicio servicio = new ReporteServicio();
	private static String classReference;
 
    private static Emisor obtenerEmisor()
     throws SQLException, ClassNotFoundException
    {
    	EmisorSQL emisSQL = new EmisorSQL();
    	return emisSQL.obtenerDatosEmisor();
    }
   
    public static String generaPdfDocumentos(com.sun.businessLogic.validate.Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String rutaJasper, String rutaReporte, String nameReporte)
     throws Exception
    {
	   System.out.println("-- INCICIO ReporteUtil.generaPdfDocumentos --");	// HFU
	   ReporteUtil rep = new ReporteUtil();
	   
	   rep.setRuc(ruc);
	   rep.setCodEst(codEst);
	   rep.setCodPuntEm(codPtoEmi);
	   rep.setCodDoc(tipoDocumento);
	   rep.setSecuencial(secuencial);
	   classReference = "ReporteUtil";
	   String name_xml = "facturacion.xml";
	   String reportePdf="";
	   String jasperFile = "";
	   
	   try {
		   jasperFile = Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc()) == null ? "" : Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc());
	   }
	   catch (Exception e)
	   {
		   System.out.println(e);
		   jasperFile = Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc()) == null ? "" : Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc());
	   }
	   if ((jasperFile.equals("")) || (jasperFile == null)) {
		   jasperFile = Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc()) == null ? "" : Environment.c.getString("facElectronica.pdf.jasper.doc" + ruc + "_" + rep.getCodDoc());
	   }
	   if ((jasperFile.equals("")) || (jasperFile == null)) {
		   if (rep.getCodDoc().equals("01"))
			   jasperFile = "factura.jasper";
		   if (rep.getCodDoc().equals("04"))
			   jasperFile = "notaCreditoFinal.jasper";
		   if (rep.getCodDoc().equals("05"))
			   jasperFile = "notaDebitoFinal.jasper";
		   if (rep.getCodDoc().equals("06"))
			   jasperFile = "guiaRemisionFinal.jasper";
		   if (rep.getCodDoc().equals("07"))
			   jasperFile = "comprobanteRetencion.jasper";
	   }
	   try
	   {
		   if (rep.getCodDoc().equals("01"))
			   reportePdf=rep.generarReporteFac(emite,rutaJasper + jasperFile, rutaReporte + nameReporte);
		   if (rep.getCodDoc().equals("04"))
			   reportePdf= rep.generarReporteCred(emite,rutaJasper + jasperFile, rutaReporte + nameReporte);
		   if (rep.getCodDoc().equals("05"))
			   rep.generarReporteNotaDebito(rutaJasper + jasperFile, rutaReporte + nameReporte);
		   if (rep.getCodDoc().equals("06"))
			   reportePdf=rep.generarReporteGuia(emite, rutaJasper + jasperFile, rutaReporte + nameReporte);
		   if (rep.getCodDoc().equals("07"))
			   reportePdf=rep.generarReporteRetencion(emite,rutaJasper + jasperFile, rutaReporte + nameReporte);
	   }
	   catch (SQLException e)
	   {
		   System.out.println(e);
		   e.printStackTrace();
		   return "";
	   }
	   catch (ClassNotFoundException e) {
		   System.out.println(e);
		   e.printStackTrace();
		   return "";
	   }
	   
	   System.out.println("-- FIN ReporteUtil.generaPdfDocumentos --");	// HFU
	   return reportePdf;
   }
    
    public String generarReporteFac(com.sun.businessLogic.validate.Emisor emite,
								    String urlReporte, 
								    String numfact) throws SQLException, ClassNotFoundException
    {
	   FileInputStream is = null;
	   JRDataSource dataSource = null;
	   List detallesAdiciones = new ArrayList();
	   List infoAdicional = new ArrayList();
	   List detDocumento = new ArrayList();
	   List detAdicional = new ArrayList();
	   try
	   {
		   detDocumento = emite.getInfEmisor().getListDetDocumentos();
		   
		   if (detDocumento!= null){
			   if (!detDocumento.isEmpty()){
				   for (int i = 0; i < emite.getInfEmisor().getListDetDocumentos().size(); i++) {
					   DetallesAdicionalesReporte detAd = new DetallesAdicionalesReporte();
					   detAd.setCodigoPrincipal(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoPrincipal());
					   //detAd = emite.getInfEmisor().getListDetDocumentos().get(i).getListDetAdicionalesDocumentos();
					   detAd.setDescuento(String.valueOf((emite.getInfEmisor().getListDetDocumentos().get(i).getDescuento())));
					   detAd.setCodigoAuxiliar(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoAuxiliar());
					   detAd.setDescripcion(emite.getInfEmisor().getListDetDocumentos().get(i).getDescripcion());
					   detAd.setCantidad(String.valueOf(emite.getInfEmisor().getListDetDocumentos().get(i).getCantidad()));
					   detAd.setPrecioTotalSinImpuesto(String.valueOf(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioTotalSinImpuesto()));
					   detAd.setPrecioUnitario(new Double(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioUnitario()).toString());
					   detAd.setInfoAdicional(infoAdicional.isEmpty() ? null : infoAdicional);
					   detallesAdiciones.add(i, detAd);
				   }
			   }
		   }
	   }
	   catch (Exception e) {
		   e.printStackTrace();
	   }
	   try {
		   dataSource = new JRBeanCollectionDataSource(detallesAdiciones);
		   is = new FileInputStream(urlReporte);
		   JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTributaria(emite), obtenerInfoFactura(emite)), dataSource);
		   JasperExportManager.exportReportToPdfFile(reporte_view, numfact);
	   }
	   catch (FileNotFoundException ex) {
		   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
		   try
		   {
			   if (is != null)
				   is.close();
		   }
		   catch (IOException ex1) {
			   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
			   }
	   }
	   catch (JRException e)
	   {
		   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
		   try
		   {
			   if (is != null)
				   is.close();
		   }
		   catch (IOException ex) {
			   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
		   }
	   }
	   finally
	   {
		   try
		   {
			   if (is != null)
				   is.close();
			   }
		   catch (IOException ex) {
			   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
		   }
	   }
	   return numfact;
   }

    public String generarReporteCred(com.sun.businessLogic.validate.Emisor emite,
    								 String urlReporte, 
    								 String numcred) throws SQLException, ClassNotFoundException
    {
	   FileInputStream is = null;
	   JRDataSource dataSource = null;
	   List detallesAdiciones = new ArrayList();
	   List infoAdicional = new ArrayList();
	   List detDocumento = new ArrayList();
	   List detAdicional = new ArrayList();
	   try
	   {
		   detDocumento = emite.getInfEmisor().getListDetDocumentos();
		   DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		   otherSymbols.setDecimalSeparator('.');
		   otherSymbols.setGroupingSeparator(','); 
		   DecimalFormat df = new DecimalFormat("###,##0.00",otherSymbols);
		   
		   if (detDocumento!= null){
			   if (!detDocumento.isEmpty())
			   {
				   for (int i = 0; i < emite.getInfEmisor().getListDetDocumentos().size(); i++) {
					   DetallesAdicionalesReporte detAd = new DetallesAdicionalesReporte();
					   detAd.setCodigoPrincipal(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoPrincipal());
					   detAd.setCodigoAuxiliar(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoAuxiliar());
					   detAd.setDescripcion(emite.getInfEmisor().getListDetDocumentos().get(i).getDescripcion());
					   detAd.setDescuento(df.format((emite.getInfEmisor().getListDetDocumentos().get(i).getDescuento())));
					   detAd.setCantidad(String.valueOf(emite.getInfEmisor().getListDetDocumentos().get(i).getCantidad()));
					   detAd.setPrecioTotalSinImpuesto(df.format(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioTotalSinImpuesto()));
					   detAd.setPrecioUnitario(df.format(new Double(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioUnitario())));
					   detAd.setInfoAdicional(infoAdicional.isEmpty() ? null : infoAdicional);
					   detallesAdiciones.add(i, detAd);
				   }
			   }
		   }
	   }
	   catch (Exception e) {
		   e.printStackTrace();
	   }
	   try {
		   dataSource = new JRBeanCollectionDataSource(detallesAdiciones);
		   is = new FileInputStream(urlReporte);
		   JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTributaria(emite), obtenerInfoCredito(emite)), dataSource);
		   JasperExportManager.exportReportToPdfFile(reporte_view, numcred);
	   }
	   catch (FileNotFoundException ex) {
		   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
		   try
		   {
			   if (is != null)
				   is.close();
		   }
		   catch (IOException ex1) {
			   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
		   }
	   }
	   catch (JRException e)
	   {
		   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
		   try
		   {
			   if (is != null)
				   is.close();
		   }
		   catch (IOException ex) {
			   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
		   }
	   }
	   finally
	   {
		   try
		   {
			   if (is != null)
				   is.close();
		   }
		   catch (IOException ex) {
			   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
		   }
	   }
	   return numcred;
   }
 
    public void generarReporteNotaDebito(String urlReporte, String numrep)
     throws SQLException, ClassNotFoundException
    {
    	FileInputStream is = null;
    	try
    	{
    		List debito = new ArrayList();
    		List adicional = new ArrayList();
    		List detAdicional = new ArrayList();
    		List infoAdicional = new ArrayList();
    		try
    		{
    			adicional = this.servicio.buscarDetAdicional(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
    			debito = this.servicio.buscarMotivosDebito(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
    			if (!adicional.isEmpty()) {
    				for (int i = 0; i < adicional.size(); i++) {
    					InformacionAdicional info = new InformacionAdicional();
    					info.setNombre(((FacDetAdicional)adicional.get(i)).getNombre());
    					info.setValor(((FacDetAdicional)adicional.get(i)).getValor());
    					infoAdicional.add(i, info);
    				}
    			}
    			if (!debito.isEmpty())
    				for (int i = 0; i < debito.size(); i++) {
    					DetallesAdicionales detAdi = new DetallesAdicionales();
    					detAdi.setRazonModificacion(((FacDetMotivosdebito)debito.get(i)).getRazon());
    					detAdi.setValorModificacion(String.valueOf(((FacDetMotivosdebito)debito.get(i)).getBaseImponible()));
    					detAdi.setInfoAdicional(infoAdicional.isEmpty() ? null : infoAdicional);
    					detAdicional.add(i, detAdi);
    				}
    		}
    		catch (Exception e)
    		{
    			e.printStackTrace();
    		}
    		JRDataSource dataSource = new JRBeanCollectionDataSource(detAdicional);
    		is = new FileInputStream(urlReporte);
    		JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(), obtenerInfoND()), dataSource);
    		JasperExportManager.exportReportToPdfFile(reporte_view, numrep);
    	}
    	catch (FileNotFoundException ex) {
    		Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
    		ex.printStackTrace();
    		try
    		{
    			if (is != null)
    				is.close();
    		}
    		catch (IOException ex1) {
/* 289 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
       }
     }
     catch (JRException e)
     {
/* 281 */       Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
/* 282 */       e.printStackTrace();
       try
       {
/* 285 */         if (is != null)
/* 286 */           is.close();
       }
       catch (IOException ex) {
/* 289 */         Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
     finally
     {
       try
       {
/* 285 */         if (is != null)
/* 286 */           is.close();
       }
       catch (IOException ex) {
    	   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
   }
   
   
	public String generarReporteGuia(com.sun.businessLogic.validate.Emisor emite, String urlReporte, String numrep)
     throws SQLException, ClassNotFoundException
    {
		System.out.println("-- INCICIO ReporteUtil.generarReporteGuia --");	// HFU
		FileInputStream is = null;
		try
		{
			List<Destinatarios> listaDestinatarios = new ArrayList<Destinatarios>();
			List guiaLista = new ArrayList();
			
			try
			{
				FacCabDocumento cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
				//detDocumento = this.servicio.buscarDatosDetallesDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
				listaDestinatarios = this.servicio.buscarDatosDestinatarios(emite.getInfEmisor().getAmbiente(), this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
				if (!listaDestinatarios.isEmpty())
				{
					for (int i = 0; i < listaDestinatarios.size(); i++)
					{
						for(int j=0; j<listaDestinatarios.get(i).getListDetallesGuiaRemision().size(); j++)
					    {
							DetalleGuiaReporte detalleGuia = new DetalleGuiaReporte();
					    	detalleGuia.setCodigoPrincipal(listaDestinatarios.get(i).getListDetallesGuiaRemision().get(j).getCodigoInterno());
					    	detalleGuia.setCodigoAuxiliar(listaDestinatarios.get(i).getListDetallesGuiaRemision().get(j).getCodigoAdicional());
					    	detalleGuia.setDescripcion(listaDestinatarios.get(i).getListDetallesGuiaRemision().get(j).getDescripcion());
					    	detalleGuia.setCantidad(String.valueOf(listaDestinatarios.get(i).getListDetallesGuiaRemision().get(j).getCantidad()));
					    	guiaLista.add(detalleGuia);
					    }
					}
			   }
		   }
		   catch (Exception e)
		   {
			   System.out.println(e);
			   e.printStackTrace();
		   }
		   
		   JRDataSource dataSource = new JRBeanCollectionDataSource(guiaLista);
		   //JRDataSource dataSource = new JRBeanCollectionDataSource(detGuia);
		   is = new FileInputStream(urlReporte);
		   JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTributaria(emite), obtenerInfoGR(emite)), dataSource);
		   JasperExportManager.exportReportToPdfFile(reporte_view, numrep);
	   }
	   catch (FileNotFoundException ex) {
		   System.out.println(ex);
		   ex.printStackTrace();
		   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
		   try
		   {
			   if (is != null)
				   is.close();
		   }
		   catch (IOException ex1) {
			   System.out.println(ex1);
			   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
		   }
	   }
	   catch (JRException e)
	   {
		   System.out.println(e);
		   e.printStackTrace();
		   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
		   try
		   {
			   if (is != null)
				   is.close();
		   }
		   catch (IOException ex) {
			   System.out.println(ex);
			   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
		   }
	   }
	   finally
	   {
		   try {
			   if (is != null)
				   is.close();
		   }
		   catch (IOException ex) {
			   System.out.println(ex);
			   Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
		   }
	   }
	   System.out.println("-- FIN ReporteUtil.generarReporteGuia --");	// HFU
	   return numrep;
   }
	
	private Map<String, Object> obtenerInfoGR(com.sun.businessLogic.validate.Emisor emite)
    {
	   Map param = new HashMap();
	   FacCabDocumento cabDoc = new FacCabDocumento();
	   List infoAdicional = new ArrayList();
	   List detAdicional = new ArrayList();
	   List detalles = new ArrayList();
	   try {
		   System.out.println("-- INICIO ReporteUtil.obtenerInfoGR --");
		   cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
		   detAdicional = this.servicio.buscarDetAdicional(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
		   
		   if (cabDoc != null)
		   {
			   param.put("RS_TRANSPORTISTA", cabDoc.getRazonSocialComprador());
			   param.put("RUC_TRANSPORTISTA", cabDoc.getIdentificacionComprador());
			   param.put("FECHA_EMISION", cabDoc.getFechaEmision());
			   param.put("GUIA", cabDoc.getGuiaRemision());
			   param.put("VALOR_TOTAL", Double.valueOf(cabDoc.getImporteTotal()));
			   param.put("IVA", Double.valueOf(cabDoc.getIva12()));
			   param.put("IVA_0", Double.valueOf(cabDoc.getSubtotal0()));
			   param.put("IVA_12", Double.valueOf(cabDoc.getSubtotal12()));
			   param.put("ICE", Double.valueOf(cabDoc.getTotalvalorICE()));
			   param.put("NO_OBJETO_IVA", Double.valueOf(cabDoc.getSubtotalNoIva()));
			   param.put("SUBTOTAL", Double.valueOf(cabDoc.getTotalSinImpuesto()));
			   param.put("PROPINA", Double.valueOf(cabDoc.getPropina()));
			   param.put("TOTAL_DESCUENTO", Double.valueOf(cabDoc.getTotalDescuento()));
			   param.put("PLACA", cabDoc.getPlaca());
			   param.put("PUNTO_PARTIDA", cabDoc.getPartida());
			   param.put("FECHA_INI_TRANSPORTE", cabDoc.getFechaInicioTransporte());
			   param.put("FECHA_FIN_TRANSPORTE", cabDoc.getFechaFinTransporte());
			   param.put("FECHA_AUTORIZACION", cabDoc.getFechaautorizacion()==null?emite.getInfEmisor().getFechaAutorizacion():cabDoc.getFechaautorizacion());
			   param.put("GUIA", cabDoc.getCodEstablecimiento()+"-"+cabDoc.getCodPuntEmision()+"-"+cabDoc.getSecuencial());
			   
			   
			   param.put("PUNTO_PARTIDA", emite.getInfEmisor().getDirPartida());
			   
			   ////////////////////////
			   List<Destinatarios> listaDestinatarios = new ArrayList();
			   listaDestinatarios = this.servicio.buscarDatosDestinatarios(emite.getInfEmisor().getAmbiente(), this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
			   if (!listaDestinatarios.isEmpty())
			   {
				   for (int i = 0; i < listaDestinatarios.size(); i++)
				   {
					   param.put("RUC_DESTINATARIO", listaDestinatarios.get(i).getIdentificacionDestinatario());
					   param.put("RS_DESTINATARIO", listaDestinatarios.get(i).getRazonSocialDestinatario());
					   param.put("DIRECCION_DESTINATARIO", listaDestinatarios.get(i).getDireccionDestinatario());
					   param.put("MOTIVO_TRASLADO", listaDestinatarios.get(i).getMotivoTraslado());
					   param.put("NUM_DOC_ADUANERO", listaDestinatarios.get(i).getDocAduanero()==null?"":listaDestinatarios.get(i).getDocAduanero());
					   param.put("NUM_DOC_SUSTENTO", listaDestinatarios.get(i).getNumDocSustentoDest()==null?"":listaDestinatarios.get(i).getNumDocSustentoDest());
					   param.put("NUM_AUT_DOC_SUSTENTO", listaDestinatarios.get(i).getNumAutDocSustDest()==null?"":listaDestinatarios.get(i).getNumAutDocSustDest());
					   param.put("FECHA_EMISION_DOC_SUTENTO", listaDestinatarios.get(i).getFechEmisionDocSustDest()==null?"":listaDestinatarios.get(i).getFechEmisionDocSustDest());
					   
					   String lsTipoDoc ="";
					   if(listaDestinatarios.get(i).getCodDocSustentoDest()!=null)
					   {
						   if(listaDestinatarios.get(i).getCodDocSustentoDest().equals("01"))
							   lsTipoDoc = "Factura";
						   else if(listaDestinatarios.get(i).getCodDocSustentoDest().equals("04"))
							   lsTipoDoc = "Nota de Crédito";
						   else if(listaDestinatarios.get(i).getCodDocSustentoDest().equals("07"))
							   lsTipoDoc = "Comprobante Retención";
						   else if(listaDestinatarios.get(i).getCodDocSustentoDest().equals("06"))
							   lsTipoDoc = "Guía Remisión";
					   }
					   param.put("TIP_DOC_SUSTENTO", lsTipoDoc);
				   }
				}
			   ////////////////////////		   
			   
			   if (!detAdicional.isEmpty()) {
				   for (int i = 0; i < detAdicional.size(); i++) {
					   InformacionAdicional infoAdic = new InformacionAdicional();
					   infoAdic.setNombre(((FacDetAdicional)detAdicional.get(i)).getNombre());
					   infoAdic.setValor(((FacDetAdicional)detAdicional.get(i)).getValor());
					   infoAdicional.add(i, infoAdic);
				   }
				   param.put("INFO_ADICIONAL", infoAdicional);
			   }
		   }
	   }
	   catch (Exception e)
	   {
		   System.out.println(e);
		   e.printStackTrace();
	   }
	   System.out.println("-- FIN ReporteUtil.obtenerInfoGR --");
	   return param;
   }
   
	public String generarReporteRetencion(com.sun.businessLogic.validate.Emisor emite,
										  String urlReporte, 
										  String numret) throws SQLException, ClassNotFoundException
    {
    	System.out.println("-- INICIO ReporteUtil.generarReporteRetencion --");
    	FileInputStream is = null;
    	try
    	{
    		List detRetencion = new ArrayList();
    		FacCabDocumento cabDoc = new FacCabDocumento();
    		InformacionAdicional info = null;
    		List infoAdicional = new ArrayList();
    		DetallesAdicionales detalles = null;
    		List detallesAdicional = new ArrayList();
    		List detAdicionals = new ArrayList();
    		try
    		{
    			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
    			otherSymbols.setDecimalSeparator('.');
    			otherSymbols.setGroupingSeparator(',');
    			DecimalFormat df = new DecimalFormat("###,##0.00",otherSymbols);
    			if (emite.getInfEmisor().getListDetImpuestosRetenciones().size()>0)
    			{
    				for (int i = 0; i < emite.getInfEmisor().getListDetImpuestosRetenciones().size(); i++)
    				{
    					String comprobante = "";
						if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodDocSustento().trim().equals("01")) comprobante = "FACTURA";
						if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodDocSustento().trim().equals("04")) comprobante = "NOTA DE CREDITO";
						if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodDocSustento().trim().equals("05")) comprobante = "NOTA DE DEBITO";
						if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodDocSustento().trim().equals("06")) comprobante = "GUIA DE REMISION";
						detalles = new DetallesAdicionales();
						detalles.setBaseImponible(df.format(emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getBaseImponible()));
						detalles.setComprobante(comprobante);
						detalles.setNombreComprobante(comprobante);
						detalles.setFechaEmisionCcompModificado(emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getFechaEmisionDocSustento() == null ? null : (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getFechaEmisionDocSustento()));
						detalles.setNumeroComprobante(String.valueOf(emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getNumDocSustento()));
					 
						detalles.setPorcentajeRetencion(String.valueOf(emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodigoRetencion()));
						detalles.setPorcentajeRetener(String.valueOf((emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getPorcentajeRetener())));
						detalles.setValorRetenido(String.valueOf((emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getValorRetenido())));
						//detalles.setInfoAdicional(infoAdicional);
						//FacGeneral general = new FacGeneral();
							 
						boolean flagIva = false;
						String descripcion = "";
						if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodigo().equals("1")){
							 descripcion = "RET. FTE";
						}
						if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodigo().equals("2")){
							 flagIva = true;
							 descripcion = "IVA";
						}
						if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodigo().equals("6")){
							 descripcion = "ISD";
						}
						if (flagIva)
						{
							if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodigoRetencion().equals("1")){
								detalles.setPorcentajeRetencion(String.valueOf("721"));
							}
							if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodigoRetencion().equals("2")){
								detalles.setPorcentajeRetencion(String.valueOf("723"));
							}
							if (emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodigoRetencion().equals("3")){
								detalles.setPorcentajeRetencion(String.valueOf("725"));
							}
						}else{
							detalles.setPorcentajeRetencion(emite.getInfEmisor().getListDetImpuestosRetenciones().get(i).getCodigoRetencion());
						}
						//general = this.servicio.buscarNombreCodigo(String.valueOf(((FacDetRetencione)detRetencion.get(i)).getCodImpuesto()), "29");							 
						detalles.setNombreImpuesto(descripcion);							 
						detallesAdicional.add(i, detalles);														
    				}
    			}/*
						detAdicionals = emite.getInfEmisor().getListInfAdicional();
						if (!detAdicionals.isEmpty()) {
						  for (int i = 0; i < detAdicionals.size(); i++) {
						    InformacionAdicional infoAd = new InformacionAdicional();
						    infoAd.setNombre(((FacDetAdicional)detAdicionals.get(i)).getNombre());
						    infoAd.setValor(((FacDetAdicional)detAdicionals.get(i)).getValor());
						    infoAdicional.add(i, infoAd);
						  }
						}*/
         		//JZU RETENCION
			    JRDataSource dataSource = new JRBeanCollectionDataSource(detallesAdicional);
				is = new FileInputStream(urlReporte);
				//JasperPrint reporte_view=JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTriobutaria(), obtenerInfoCompRetencion()), dataSource);
				JasperPrint reporte_view = JasperFillManager.fillReport(is, obtenerMapaParametrosReportes(obtenerParametrosInfoTributaria(emite), obtenerInfoCompRetencion(emite)), dataSource);
				JasperExportManager.exportReportToPdfFile(reporte_view, numret);
    		}
    		catch (FileNotFoundException ex) {
    			ex.printStackTrace();
    			Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
    			try
    			{
    				if (is != null)
    					is.close();
    			}
    			catch (IOException ex1) {
    				Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex1);
    			}
    		}
    		catch (JRException e)
    		{
    			Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, e);
    			try
    			{
    				if (is != null)
    					is.close();
    			}
    			catch (IOException ex) {
    				Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
    			}
    		}
    		finally
    		{
    			try
    			{
    				if (is != null)
    					is.close();
    			}
    			catch (IOException ex) {
    				Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
    			}
    		}
    	}catch (Exception ex) {
    		Logger.getLogger(ReporteUtil.class.getName()).log(Level.SEVERE, null, ex);
    	}
    	System.out.println("-- FIN ReporteUtil.generarReporteRetencion --");
    	return numret;
   }
 
    
    private Map<String, Object> obtenerMapaParametrosReportes(Map<String, Object> mapa1, Map<String, Object> mapa2)
    {
    	System.out.println("-- INICIO ReporteUtil.obtenerMapaParametrosReportes --");
    	mapa1.putAll(mapa2);
    	System.out.println("-- FIN ReporteUtil.obtenerMapaParametrosReportes --");
    	return mapa1;
    }

	
    private Map<String, Object> obtenerParametrosInfoTributaria(com.sun.businessLogic.validate.Emisor emite) throws SQLException, ClassNotFoundException, FileNotFoundException
    {
		Map param = new HashMap(); 	  
	    FacEmpresa empresa = new FacEmpresa();
	    try
	    {
	    	System.out.println("-- INCICIO ReporteUtil.obtenerParametrosInfoTributaria --");
		   
		   empresa = this.servicio.buscarEmpresa(emite.getInfEmisor().getRuc());
		   param.put("RUC", emite.getInfEmisor().getRuc());
		   param.put("CLAVE_ACC", (emite.getInfEmisor().getClaveAcceso().trim().equals("")) || (emite.getInfEmisor().getClaveAcceso() == null) ? "" : emite.getInfEmisor().getClaveAcceso());
		   param.put("RAZON_SOCIAL", emite.getInfEmisor().getRazonSocial());
		   param.put("NOM_COMERCIAL", emite.getInfEmisor().getRazonSocial());
		   param.put("DIR_MATRIZ", emite.getInfEmisor().getDireccionMatriz());
		   param.put("SUBREPORT_DIR", "C://resources//reportes//");
		   //param.put("SUBREPORT_DIR", "C://DataExpress//Pycca//generales//Jasper//");
		   param.put("TIPO_EMISION", emite.getInfEmisor().getTipoEmision().equals("1") ? "NORMAL" : "CONTINGENCIA");
		   param.put("NUM_AUT", (emite.getInfEmisor().getNumeroAutorizacion() == null) || (emite.getInfEmisor().getNumeroAutorizacion().equals("")) ? null : emite.getInfEmisor().getNumeroAutorizacion());
		   param.put("FECHA_AUT", emite.getInfEmisor().getFechaAutorizacion() == null ? null : emite.getInfEmisor().getFechaAutorizacion());
		   param.put("NUM_FACT", emite.getInfEmisor().getCodEstablecimiento() + "-" + emite.getInfEmisor().getCodPuntoEmision() + "-" + emite.getInfEmisor().getSecuencial());
		   param.put("AMBIENTE", emite.getInfEmisor().getAmbiente() == 1 ? "PRUEBA" : "PRODUCCION");
		   param.put("DIR_SUCURSAL", emite.getInfEmisor().getDireccionMatriz());
		   param.put("CONT_ESPECIAL", emite.getInfEmisor().getContribEspecial());
		   param.put("LLEVA_CONTABILIDAD", (emite.getInfEmisor().getObligContabilidad().trim().equals("S")||emite.getInfEmisor().getObligContabilidad().trim().equals("SI")) ? "SI" : "NO");
		   
		   if (emite.getInfEmisor().getListInfAdicional()!= null)
		   {
			   if (emite.getInfEmisor().getListInfAdicional().size()>0) {
				   for (int i = 0; i < emite.getInfEmisor().getListInfAdicional().size(); i++) {
					   InformacionAdicional infoAd = new InformacionAdicional();
					   param.put(emite.getInfEmisor().getListInfAdicional().get(i).getName().toUpperCase(), capitalizeString(emite.getInfEmisor().getListInfAdicional().get(i).getValue()));				   		   
				   	}
			   }
		   }
		   
		   File f = new File(empresa.getPathLogoEmpresa());
		   //if (((this.codDoc.equals("04")) || (this.codDoc.equals("05")) || (this.codDoc.equals("06")) || (this.codDoc.equals("07"))) && 
		   // (f.exists())) param.put("LOGO", new FileInputStream(empresa.getPathLogoEmpresa()));
		   //if ((this.codDoc.equals("01")) && 
		   //(f.exists())) 
		   param.put("LOGO", empresa.getPathLogoEmpresa());
		   
		   //String file = (emite.getInfEmisor().getAmbientePuntoEmision().equals("1") ? "produccion.jpeg" : "pruebas.jpeg");
		   //String ruta = (empresa.getPathMarcaAgua() == null) || (empresa.getPathMarcaAgua().trim().equals("")) ? "C://resources//images//" : empresa.getPathMarcaAgua();
		   /*System.out.println("MARCA_AGUA::" + ruta + file);
			String marca = empresa.getMarcaAgua().trim().equals("S") ? ruta + file : "C://resources//images//produccion.jpeg";
			f = new File(marca);
			         if (f.exists()) param.put("MARCA_AGUA", marca);
				  */
       //}
	   }
	   catch (Exception e) {
		   System.out.println(e);
		   e.printStackTrace();
	   }
	   System.out.println("-- FIN ReporteUtil.obtenerParametrosInfoTributaria --");
	   return param;
    }
    
	private Map<String, Object> obtenerParametrosInfoTriobutaria() throws SQLException, ClassNotFoundException, FileNotFoundException
    {
		Map param = new HashMap();
 
		FacCabDocumento cabDoc = new FacCabDocumento();
		FacEmpresa empresa = new FacEmpresa();
		try
		{
			System.out.println("-- INICIO ReporteUtil.obtenerParametrosInfoTriobutaria --");
			cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
			if (cabDoc != null)
			{
				empresa = this.servicio.buscarEmpresa(cabDoc.getRuc());
				param.put("RUC", cabDoc.getRuc());
				param.put("CLAVE_ACC", (cabDoc.getClaveAcceso().trim().equals("")) || (cabDoc.getClaveAcceso() == null) ? "1111111" : cabDoc.getClaveAcceso());
				param.put("RAZON_SOCIAL", empresa.getRazonSocial());
				param.put("NOM_COMERCIAL", empresa.getRazonComercial());
				param.put("DIR_MATRIZ", empresa.getDireccionMatriz());
				//param.put("SUBREPORT_DIR", "C://resources//reportes//");
				param.put("SUBREPORT_DIR", "C://DataExpress//CimaIT//generales//Jasper//");
				param.put("TIPO_EMISION", cabDoc.getTipoEmision().trim().equals("1") ? "NORMAL" : "CONTINGENCIA");
				param.put("NUM_AUT", (cabDoc.getNumAutDocSustento() == null) || (cabDoc.getNumAutDocSustento().equals("")) ? null : cabDoc.getNumAutDocSustento());
				param.put("FECHA_AUT", cabDoc.getFechaEmisionDocSustento() == null ? null : new SimpleDateFormat("dd/MM/yyyy").format(cabDoc.getFechaEmisionDocSustento()));
				param.put("NUM_FACT", cabDoc.getCodEstablecimiento() + "-" + cabDoc.getCodPuntEmision() + "-" + cabDoc.getSecuencial());
				param.put("AMBIENTE", cabDoc.getAmbiente().intValue() == 1 ? "PRUEBA" : "PRODUCCION");
				param.put("DIR_SUCURSAL", cabDoc.getDirEstablecimiento());
				param.put("CONT_ESPECIAL", empresa.getContribEspecial());
				param.put("LLEVA_CONTABILIDAD", cabDoc.getObligadoContabilidad());
				param.put("FECHA_AUTORIZACION", cabDoc.getFechaautorizacion());
				File f = new File(empresa.getPathLogoEmpresa());
				if (((this.codDoc.equals("04")) || (this.codDoc.equals("05")) || (this.codDoc.equals("06")) || (this.codDoc.equals("07"))) && 
						(f.exists()))
					param.put("LOGO", new FileInputStream(empresa.getPathLogoEmpresa()));
				if ((this.codDoc.equals("01")) && 
						(f.exists()))
					param.put("LOGO", empresa.getPathLogoEmpresa());
 
				String file = cabDoc.getAmbiente().intValue() == 1 ? "produccion.jpeg" : "pruebas.jpeg";
				String ruta = (empresa.getPathMarcaAgua() == null) || (empresa.getPathMarcaAgua().trim().equals("")) ? "C://resources//images//" : empresa.getPathMarcaAgua();
				
				/*System.out.println("MARCA_AGUA::" + ruta + file);
			         String marca = empresa.getMarcaAgua().trim().equals("S") ? ruta + file : "C://resources//images//produccion.jpeg";
			         f = new File(marca);
			         if (f.exists()) param.put("MARCA_AGUA", marca);
				  */
			}
		}
		catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		System.out.println("-- FIN ReporteUtil.obtenerParametrosInfoTriobutaria --");
		return param;
   }
	
	
	private Map<String, Object> obtenerInfoFactura(com.sun.businessLogic.validate.Emisor emite)
	{
		Map param = new HashMap();
		FacCabDocumento cabDoc = new FacCabDocumento();
	    try
	    {
	    	System.out.println("-- INICIO ReporteUtil.obtenerInfoFactura --");
	    	
	    	param.put("RS_COMPRADOR", capitalizeString(emite.getInfEmisor().getRazonSocialComp()));
	    	param.put("RUC_COMPRADOR", emite.getInfEmisor().getIdentificacionComp());
	    	param.put("FECHA_EMISION", emite.getInfEmisor().getFecEmision());
	    	param.put("GUIA", emite.getInfEmisor().getGuiaRemision());
	    	param.put("VALOR_TOTAL", Double.valueOf(emite.getInfEmisor().getImporteTotal()));
	    	//param.put("IVA", Double.valueOf(emite.getInfEmisor().getTotalIva12()));
			boolean total12 = false, total0= false, totalice=false, totalNoObjeto=false;
			ArrayList<DetalleTotalImpuestos> lisDetImp = emite.getInfEmisor().getListDetDetImpuestos();
			for ( DetalleTotalImpuestos det : lisDetImp){
				if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 2)){
					total12 = true;							
					cabDoc.setSubtotal12(det.getBaseImponibleImp());
					cabDoc.setIva12(det.getValorImp());
				}
				if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 6)){
					totalNoObjeto = true;
					cabDoc.setSubtotalNoIva(det.getBaseImponibleImp());
				}
				if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 0)){
					total0 = true;
					cabDoc.setSubtotal0(det.getBaseImponibleImp());							
				}
			}
			if (total12){
				if (cabDoc.getSubtotal12() >= 0)
					//--param.put("IVA_12", (cabDoc.getSubtotal12()==0?"0.00":cabDoc.getSubtotal12()));
					//--System.out.println("IVA_12::"+(cabDoc.getSubtotal12()==0?"0.00":cabDoc.getSubtotal12()));
					param.put("SUBTOTAL_12", (cabDoc.getSubtotal12()==0?"0.00":cabDoc.getSubtotal12()));
				if (cabDoc.getIva12() >= 0)
					//--param.put("IVA", (cabDoc.getIva12()==0?"0.00":cabDoc.getIva12()));
					//--System.out.println("IVA::"+(cabDoc.getIva12()==0?"0.00":cabDoc.getIva12()));
					param.put("IVA_12", (cabDoc.getIva12()==0?"0.00":cabDoc.getIva12()));
			}else{
				//--param.put("IVA_12", "0.00");
				//--param.put("IVA", "0.00");
				//--System.out.println("IVA_12::0.00");
				//--System.out.println("IVA::0.00");
				param.put("SUBTOTAL_12", "0.00");
				param.put("IVA_12", "0.00");
			}
			if (totalNoObjeto){
				param.put("NO_OBJETO_IVA", ((Double.valueOf(cabDoc.getSubtotalNoIva())==0)?"0.00":Double.valueOf(cabDoc.getSubtotalNoIva())));
			}else{
				param.put("NO_OBJETO_IVA", "0.00");
			}
			if (total0){
				if (cabDoc.getSubtotal0() >= 0)
					//--param.put("IVA_0", (cabDoc.getSubtotal0()==0?"0.00":cabDoc.getSubtotal0()));
					//--System.out.println("IVA_0::"+(cabDoc.getSubtotal0()==0?"0.00":cabDoc.getSubtotal0()));
					param.put("SUBTOTAL_0", (cabDoc.getSubtotal0()==0?"0.00":cabDoc.getSubtotal0()));					
			}else{
				//--param.put("IVA_0", "0.00");
				//--System.out.println("IVA_0::0.00");
				param.put("SUBTOTAL_0", "0.00");
			}
			if (totalice){
				param.put("ICE", (Double.valueOf(emite.getInfEmisor().getTotalICE())==0?"0.00":Double.valueOf(emite.getInfEmisor().getTotalICE())));
			}else{
				param.put("ICE", "0.00");
			}
			param.put("SUBTOTAL", (Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos())==0?"0.00":Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos())));
			param.put("PROPINA", (Double.valueOf(emite.getInfEmisor().getPropina())==0?"0.00":Double.valueOf(emite.getInfEmisor().getPropina())));
			param.put("TOTAL_DESCUENTO", (Double.valueOf(emite.getInfEmisor().getTotalDescuento())==0?"0.00":Double.valueOf(emite.getInfEmisor().getTotalDescuento())));
			
			param.put("FECHA_AUTORIZACION", cabDoc.getFechaautorizacion()==null?emite.getInfEmisor().getFechaAutorizacion():cabDoc.getFechaautorizacion());
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    System.out.println("-- FIN ReporteUtil.obtenerInfoFactura --");
	    return param;
	}

	
	private Map<String, Object> obtenerInfoCredito(com.sun.businessLogic.validate.Emisor emite)
    {
		System.out.println("-- INICIO ReporteUtil.obtenerInfoCredito --");
		Map param = new HashMap();
	    FacCabDocumento cabDoc = new FacCabDocumento();
	    try {
	    	DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
			otherSymbols.setDecimalSeparator('.');
			otherSymbols.setGroupingSeparator(','); 
			DecimalFormat df = new DecimalFormat("###,##0.00",otherSymbols);
			param.put("RS_COMPRADOR", capitalizeString(emite.getInfEmisor().getRazonSocialComp()));
			param.put("RUC_COMPRADOR", emite.getInfEmisor().getIdentificacionComp());
			param.put("FECHA_EMISION", emite.getInfEmisor().getFecEmision());
			param.put("GUIA", emite.getInfEmisor().getGuiaRemision());
			
			//param.put("DOC_MODIFICADO", Double.valueOf(emite.getInfEmisor().getCodDocModificado()));
			String lsTipoDocModificado ="";
			System.out.println("  TIPO DOC MODIFICADO... " +emite.getInfEmisor().getCodDocModificado());
			if(emite.getInfEmisor().getCodDocModificado()!=null)
			{
			   if(emite.getInfEmisor().getCodDocModificado().equals("01"))
				   lsTipoDocModificado = "Factura";
			   else if(emite.getInfEmisor().getCodDocModificado().equals("04"))
				   lsTipoDocModificado = "Nota de Crédito";
			   else if(emite.getInfEmisor().getCodDocModificado().equals("07"))
				   lsTipoDocModificado = "Comprobante Retención";
			   else if(emite.getInfEmisor().getCodDocModificado().equals("06"))
				   lsTipoDocModificado = "Guía Remisión";
		    }
			param.put("TIP_DOC_MODIFICADO", lsTipoDocModificado);
			
			
			param.put("FECHA_EMISION_DOC_SUSTENTO", emite.getInfEmisor().getFecEmisionDoc());
			param.put("NUM_DOC_MODIFICADO", emite.getInfEmisor().getNumDocModificado());
			boolean total12 = false, total0= false, totalice=false, totalNoObjeto=false;
			ArrayList<DetalleTotalImpuestos> lisDetImp = emite.getInfEmisor().getListDetDetImpuestos();
			for ( DetalleTotalImpuestos det : lisDetImp)
			{
				if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 2)){
					total12 = true;							
					cabDoc.setSubtotal12(det.getBaseImponibleImp());
					cabDoc.setIva12(det.getValorImp());
				}
				if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 6)){
					totalNoObjeto = true;
					cabDoc.setSubtotalNoIva(det.getBaseImponibleImp());
				}
				if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 0)){
					total0 = true;
					cabDoc.setSubtotal0(det.getBaseImponibleImp());							
				}
			}
			if (total12){
				if (cabDoc.getSubtotal12() >= 0)
					//param.put("IVA_12", df.format(cabDoc.getSubtotal12()));
					param.put("SUBTOTAL_12", df.format(cabDoc.getSubtotal12()));
					//System.out.println("IVA_12::"+df.format(cabDoc.getSubtotal12()));
				if (cabDoc.getIva12() >= 0)
					//param.put("IVA", df.format(cabDoc.getIva12()));
					param.put("IVA_12", df.format(cabDoc.getIva12()));
					//System.out.println("IVA::"+df.format(cabDoc.getIva12()));
			}else{
				//param.put("IVA_12", "0.00");
				param.put("SUBTOTAL_12", "0.00");
				//param.put("IVA", "0.00");
				param.put("IVA_12", "0.00");
				//System.out.println("IVA_12::0.00");
				//System.out.println("IVA::0.00");
			}
			
			if (totalNoObjeto){
				param.put("NO_OBJETO_IVA", df.format(Double.valueOf(cabDoc.getSubtotalNoIva())));
			}else{
				param.put("NO_OBJETO_IVA", "0.00");
			}
			if (total0){
				if (cabDoc.getSubtotal0() >= 0)
					param.put("IVA_0", df.format(cabDoc.getSubtotal0()));
			}else{
				param.put("IVA_0", "0.00");
			}
			if (totalice){
				param.put("ICE",df.format(Double.valueOf(emite.getInfEmisor().getTotalICE())));
			}else{
				param.put("ICE", "0.00");
			}
			double total = cabDoc.getSubtotal12()+cabDoc.getSubtotalNoIva()+cabDoc.getSubtotal0()+emite.getInfEmisor().getTotalICE()+cabDoc.getIva12();
			param.put("VALOR_TOTAL", df.format(total));
					
			param.put("SUBTOTAL", df.format(Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos())));
			param.put("PROPINA", df.format(Double.valueOf(emite.getInfEmisor().getPropina())));
			param.put("TOTAL_DESCUENTO", df.format(Double.valueOf(emite.getInfEmisor().getTotalDescuento())));
			param.put("FECHA_AUTORIZACION", cabDoc.getFechaautorizacion()==null?emite.getInfEmisor().getFechaAutorizacion():cabDoc.getFechaautorizacion());
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    System.out.println("-- FIN ReporteUtil.obtenerInfoCredito --");
	    return param;
    }
   
    private Map<String, Object> obtenerInfoFactura()
    {
    	Map param = new HashMap();
    	FacCabDocumento cabDoc = new FacCabDocumento();
    	try {
    		cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
    		if (cabDoc != null) {
    			param.put("RS_COMPRADOR", cabDoc.getRazonSocialComprador());
    			param.put("RUC_COMPRADOR", cabDoc.getIdentificacionComprador());
    			param.put("FECHA_EMISION", cabDoc.getFechaEmision());
    			param.put("GUIA", cabDoc.getGuiaRemision());
    			param.put("VALOR_TOTAL", Double.valueOf(cabDoc.getImporteTotal()));
    			param.put("IVA", Double.valueOf(cabDoc.getIva12()));
    			//emite.getInfEmisor().getTotalSinImpuestos()
				param.put("IVA_0", Double.valueOf(cabDoc.getSubtotal0()));
				param.put("IVA_12", Double.valueOf(cabDoc.getSubtotal12()));
				param.put("ICE", Double.valueOf(cabDoc.getTotalvalorICE()));
				param.put("NO_OBJETO_IVA", Double.valueOf(cabDoc.getSubtotalNoIva()));
				param.put("SUBTOTAL", Double.valueOf(cabDoc.getTotalSinImpuesto()));
				param.put("PROPINA", Double.valueOf(cabDoc.getPropina()));
				param.put("TOTAL_DESCUENTO", Double.valueOf(cabDoc.getTotalDescuento()));
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return param;
    }
   
   private Map<String, Object> obtenerInfoNC(com.sun.businessLogic.validate.Emisor emite)
   {
	   Map param = new HashMap();
	   FacCabDocumento cabDoc = new FacCabDocumento();
	   try {
		    	 	DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
					otherSymbols.setDecimalSeparator('.');
					otherSymbols.setGroupingSeparator(','); 
					DecimalFormat df = new DecimalFormat("###,##0.00",otherSymbols);
		   String comprobante = "";
		   if (cabDoc.getCodDocModificado().trim().equals("01")) comprobante = "FACTURA";
		   if (cabDoc.getCodDocModificado().trim().equals("04")) comprobante = "NOTA DE CREDITO";
		   if (cabDoc.getCodDocModificado().trim().equals("05")) comprobante = "NOTA DE DEBITO";
		   if (cabDoc.getCodDocModificado().trim().equals("06")) comprobante = "GUIA DE REMISION";

		   param.put("RS_COMPRADOR", capitalizeString(emite.getInfEmisor().getRazonSocialComp()));
		   param.put("RUC_COMPRADOR", emite.getInfEmisor().getIdentificacionComp());
		   param.put("FECHA_EMISION", emite.getInfEmisor().getFecEmision());
		   param.put("VALOR_TOTAL", df.format(Double.valueOf(emite.getInfEmisor().getImporteTotal())));
		   param.put("IVA", df.format(Double.valueOf(emite.getInfEmisor().getTotalIva12())));

		   param.put("IVA_0", df.format(Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos())));
		   param.put("IVA_12", df.format(Double.valueOf(emite.getInfEmisor().getTotalIva12())));
		   param.put("ICE", df.format(Double.valueOf(emite.getInfEmisor().getTotalICE())));
		   param.put("NO_OBJETO_IVA", df.format(Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos())));
		   param.put("SUBTOTAL", df.format(Double.valueOf(emite.getInfEmisor().getTotalSinImpuestos())));
		   param.put("PROPINA", df.format(Double.valueOf(emite.getInfEmisor().getPropina())));
		   param.put("TOTAL_DESCUENTO", df.format(Double.valueOf(emite.getInfEmisor().getTotalDescuento())));

		   param.put("NUM_DOC_MODIFICADO", emite.getInfEmisor().getNumDocModificado());
		   param.put("FECHA_EMISION_DOC_SUSTENTO", emite.getInfEmisor().getFecEmisionDoc());
		   param.put("DOC_MODIFICADO", emite.getInfEmisor().getCodDocModificado());
		   param.put("RAZON_MODIF", emite.getInfEmisor().getMotivo());
     } catch (Exception e) {
    	 e.printStackTrace();
     }
	   return param;
   }
  
   private Map<String, Object> obtenerInfoND()
   {
/* 672 */     Map param = new HashMap();
 
/* 674 */     FacCabDocumento cabDoc = new FacCabDocumento();
     try {
/* 676 */       cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
/* 677 */       if (cabDoc != null) {
/* 678 */         String comprobante = "";
/* 679 */         if (cabDoc.getCodDocModificado().trim().equals("01")) comprobante = "FACTURA";
/* 680 */         if (cabDoc.getCodDocModificado().trim().equals("04")) comprobante = "NOTA DE CREDITO";
/* 681 */         if (cabDoc.getCodDocModificado().trim().equals("05")) comprobante = "NOTA DE DEBITO";
/* 682 */         if (cabDoc.getCodDocModificado().trim().equals("06")) comprobante = "GUIA DE REMISION";
/* 683 */         param.put("RS_COMPRADOR", cabDoc.getRazonSocialComprador());
/* 684 */         param.put("RUC_COMPRADOR", cabDoc.getIdentificacionComprador());
/* 685 */         param.put("FECHA_EMISION", cabDoc.getFechaEmision());
/* 686 */         param.put("GUIA", cabDoc.getGuiaRemision());
/* 687 */         param.put("TOTAL", Double.valueOf(cabDoc.getImporteTotal()));
/* 688 */         param.put("IVA", Double.valueOf(cabDoc.getIva12()));
/* 689 */         param.put("IVA_0", Double.valueOf(cabDoc.getSubtotal0()));
/* 690 */         param.put("IVA_12", Double.valueOf(cabDoc.getSubtotal12()));
/* 691 */         param.put("ICE", Double.valueOf(cabDoc.getTotalvalorICE()));
/* 692 */         param.put("NO_OBJETO_IVA", Double.valueOf(cabDoc.getSubtotalNoIva()));
/* 693 */         param.put("SUBTOTAL", Double.valueOf(cabDoc.getTotalSinImpuesto()));
/* 694 */         param.put("PROPINA", Double.valueOf(cabDoc.getPropina()));
/* 695 */         param.put("TOTAL_SIN_IMP", Double.valueOf(cabDoc.getTotalSinImpuesto()));
/* 696 */         param.put("NUM_DOC_MODIFICADO", cabDoc.getNumDocModificado());
/* 697 */         param.put("DOC_MODIFICADO", comprobante);
/* 698 */         param.put("FECHA_EMISION_DOC_SUSTENTO", cabDoc.getFecEmisionDocSustento() == null ? "NO ENVIADO" : new SimpleDateFormat("dd/MM/yyyy").format(cabDoc.getFecEmisionDocSustento()));
       }
     } catch (Exception e) {
/* 701 */       e.printStackTrace();
     }
/* 703 */     return param;
   }
 
   private Map<String, Object> obtenerInfoCompRetencion(com.sun.businessLogic.validate.Emisor emite)
   {
	   Map param = new HashMap(); 
	   FacCabDocumento cabDoc = new FacCabDocumento();
	   try
	   {
		   cabDoc = this.servicio.buscarDatosCabDocumentos(this.Ruc, this.codEst, this.codPuntEm, this.codDoc, this.secuencial);
		   if (cabDoc != null) {
			   param.put("FECHA_AUTORIZACION", emite.getInfEmisor().getFechaAutorizacion());
			   param.put("RS_COMPRADOR", cabDoc.getRazonSocialComprador());
			   param.put("RUC_COMPRADOR", cabDoc.getIdentificacionComprador());
			   param.put("FECHA_EMISION", cabDoc.getFechaEmision());
			   param.put("EJERCICIO_FISCAL", cabDoc.getPeriodoFiscal());
		   }
	   } catch (Exception e) {
		   e.printStackTrace();
	   }
	   return param;
   }
     
   
   public String getRuc(){
	   return this.Ruc;
   }   
   public void setRuc(String ruc) {
	   this.Ruc = ruc;
   }
   
   public String getCodEst() {
	   return this.codEst;
   }   
   public void setCodEst(String codEst) {
	   this.codEst = codEst;
   }
   
   public String getCodPuntEm() {
	   return this.codPuntEm;
   }
   public void setCodPuntEm(String codPuntEm) {
	   this.codPuntEm = codPuntEm;
   }
   
   public String getCodDoc() {
	   return this.codDoc;
   }
   public void setCodDoc(String codDoc) {
	   this.codDoc = codDoc;
   }

   public String getSecuencial() {
	   return this.secuencial;
   }
   public void setSecuencial(String secuencial) {
	   this.secuencial = secuencial;
   }

   public static String capitalizeString(String string) {
	   char[] chars = string.toLowerCase().toCharArray();
	   boolean found = false;
	   for (int i = 0; i < chars.length; i++) {
		   if (!found && Character.isLetter(chars[i])) {
			   chars[i] = Character.toUpperCase(chars[i]);
			   found = true;
		   } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
			   found = false;
		   }
	   }
	   return String.valueOf(chars);
   }


}