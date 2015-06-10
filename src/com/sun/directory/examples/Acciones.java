package com.sun.directory.examples;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.businessLogic.validate.Emisor;
import com.sun.businessLogic.validate.LeerDocumentos;
import com.sun.DAO.Destinatarios;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import com.tradise.reportes.entidades.FacCabDocumento;
import com.tradise.reportes.servicios.ReporteSentencias;
import com.util.util.key.Environment;

public class Acciones
{
	/*************************************************************************/
	/* Validacion de Estado RECIBIDA */
	/*************************************************************************/
	/****************** Insert con la Base de Datos ***************************/
	public void documentoRecibido(Emisor emite, InfoEmpresa infoEmp)
	{
		System.out.println("-- INICIO Acciones.documentoRecibido --");
		
		SimpleDateFormat sm = new SimpleDateFormat("dd-MM-yyyy");
		String strDate = sm.format(new Date());
		int intentos = Environment.c.getInt("facElectronica.general.ws.consultaAutorizacion.intentos");
		int timeIntentos = Environment.c.getInt("facElectronica.general.ws.consultaAutorizacion.timeIntentos");
		
		FacCabDocumento CabDoc = null;
		ReporteSentencias rpSen = new ReporteSentencias();
		String nameFile = "";
		//SimpleDateFormat sm = new SimpleDateFormat("dd-MM-yyyy");
		//String strDate = sm.format(new Date());
		
		/*
		 * ======================================================== INI -
		 * Validaciones por cada tipo de documento
		 * ========================================================
		 */
		// Validacion para Facturas
		if (emite.getInfEmisor().getCodDocumento().equals("01"))
		{
			CabDoc = ServiceDataHilo.preparaCabDocumentoFac(emite,
															infoEmp.getRuc(),
															emite.getInfEmisor().getCodEstablecimiento(),
															emite.getInfEmisor().getCodPuntoEmision(),
															emite.getInfEmisor().getTipoComprobante(),
															emite.getInfEmisor().getSecuencial(),
															"Recibido por el SRI",
															"RS");
			
			if (!rpSen.existFacCabDocumentos(CabDoc))
			{
				rpSen.insertFacCabDocumentos(CabDoc);
				//rpSen.insertFacDetallesDocumento(CabDoc.getListDetalleDocumento());
				//rpSen.insertInfoAdicional(emite);
				//int existCli = rpSen.existeCliente(infoEmp.getRuc(), CabDoc.getCodCliente(), "C");
				int existCli = rpSen.existeCliente(infoEmp.getRuc(), CabDoc.getIdentificacionComprador(), "C");
				if (existCli == 0)
				{
					rpSen.insertaClientes(infoEmp.getRuc(),
										  CabDoc.getRazonSocialComprador(),
										  CabDoc.getDireccion(),
										  CabDoc.getEmailCliente(),
										  "C",
										  CabDoc.getTipoIdentificacion(),
										  CabDoc.getRise(), CabDoc.getTelefono(),
										  CabDoc.getIdentificacionComprador(),
										  String.valueOf(CabDoc.getCodCliente()));
				}
			}
			else
			{
				rpSen.updateFacCabDocumentosPostgreSQL(CabDoc);
			}
			rpSen.insertFacDetallesDocumento(CabDoc.getListDetalleDocumento());
			rpSen.insertInfoAdicional(emite);
			
		}
		// Validacion para retenciones
		if (emite.getInfEmisor().getCodDocumento().equals("07"))
		{
			CabDoc = ServiceDataHilo.preparaCabDocumentoRet(emite,
															infoEmp.getRuc(),
															emite.getInfEmisor().getCodEstablecimiento(),
															emite.getInfEmisor().getCodPuntoEmision(),
															emite.getInfEmisor().getTipoComprobante(),
															emite.getInfEmisor().getSecuencial(),
															"Recibido por el SRI",
															"RS");
			rpSen = new ReporteSentencias();
			if (!rpSen.existFacCabDocumentos(CabDoc))
			{
				rpSen.insertFacCabDocumentos(CabDoc);
				//rpSen.insertFacDetallesRetenciones(CabDoc);
				//rpSen.insertInfoAdicional(emite);
				//int existCli = rpSen.existeCliente(infoEmp.getRuc(), CabDoc.getCodCliente(), "P");
				int existCli = rpSen.existeCliente(infoEmp.getRuc(), CabDoc.getIdentificacionDestinatario(), "P");
				if (existCli == 0)
				{
					rpSen.insertaClientes(infoEmp.getRuc(),
										  CabDoc.getRazonSocialComprador(),
										  (CabDoc.getDireccion() == null ? "" : CabDoc.getDireccion()),
										  (CabDoc.getEmailCliente() == null ? "" : CabDoc.getEmailCliente()),
										  "P",
										  CabDoc.getTipoIdentificacion(),
										  CabDoc.getRise(),
										  (CabDoc.getTelefono() == null ? "" : CabDoc.getTelefono()),
										  CabDoc.getIdentificacionComprador(),
										  String.valueOf(CabDoc.getCodCliente()));
				}
			}
			
			else
			{
				rpSen.updateFacCabDocumentosPostgreSQL(CabDoc);
			}
			rpSen.insertFacDetallesRetenciones(CabDoc);
			rpSen.insertInfoAdicional(emite);
			
		}
		// Validacion para Notas credito
		if (emite.getInfEmisor().getCodDocumento().equals("04"))
		{
			CabDoc = ServiceDataHilo.preparaCabDocumentoCre(emite,
															infoEmp.getRuc(),
															emite.getInfEmisor().getCodEstablecimiento(),
															emite.getInfEmisor().getCodPuntoEmision(),
															emite.getInfEmisor().getTipoComprobante(),
															emite.getInfEmisor().getSecuencial(),
															"Recibido por el SRI",
															"RS");
			
			rpSen = new ReporteSentencias();
			if (!rpSen.existFacCabDocumentos(CabDoc))
			{
				rpSen.insertFacCabDocumentos(CabDoc);
				//rpSen.insertInfoAdicional(emite);
				//rpSen.insertFacDetallesDocumento(CabDoc.getListDetalleDocumento());
				//int existCli = rpSen.existeCliente(infoEmp.getRuc(), CabDoc.getCodCliente(), "C");
				int existCli = rpSen.existeCliente(infoEmp.getRuc(), CabDoc.getIdentificacionComprador(), "C");
				if (existCli == 0)
				{
					rpSen.insertaClientes(infoEmp.getRuc(),
										  CabDoc.getRazonSocialComprador(),
										  CabDoc.getDireccion(),
										  CabDoc.getEmailCliente(),
										  "C",
										  CabDoc.getTipoIdentificacion(),
										  CabDoc.getRise(),
										  CabDoc.getTelefono(),
										  CabDoc.getIdentificacionComprador(),
										  String.valueOf(CabDoc.getCodCliente()));
				}
			}
			
			else
			{
				rpSen.updateFacCabDocumentosPostgreSQL(CabDoc);
			}
			rpSen.insertInfoAdicional(emite);
			rpSen.insertFacDetallesDocumento(CabDoc.getListDetalleDocumento());
			
		}
		
		// Validacion para Notas debito
		if (emite.getInfEmisor().getCodDocumento().equals("05"))
		{
			CabDoc = ServiceDataHilo.preparaCabDocumentoNotaDeb(emite,
																infoEmp.getRuc(),
																emite.getInfEmisor().getCodEstablecimiento(),
																emite.getInfEmisor().getCodPuntoEmision(),
																emite.getInfEmisor().getTipoComprobante(),
																emite.getInfEmisor().getSecuencial(),
																"Recibido por el SRI",
																"RS");
			rpSen = new ReporteSentencias();
			if (!rpSen.existFacCabDocumentos(CabDoc))
			{
				rpSen.insertFacCabDocumentos(CabDoc);
				//rpSen.insertInfoAdicional(emite);
				//rpSen.insertFacDetallesDocumento(CabDoc.getListDetalleDocumento());
				//int existCli = rpSen.existeCliente(infoEmp.getRuc(), CabDoc.getCodCliente(), "C");
				int existCli = rpSen.existeCliente(infoEmp.getRuc(), CabDoc.getIdentificacionComprador(), "C");
				if (existCli == 0)
				{
					rpSen.insertaClientes(infoEmp.getRuc(),
										  CabDoc.getRazonSocialComprador(),
										  CabDoc.getDireccion(),
										  CabDoc.getEmailCliente(),
										  "C",
										  CabDoc.getTipoIdentificacion(),
										  CabDoc.getRise(),
										  CabDoc.getTelefono(),
										  CabDoc.getIdentificacionComprador(),
										  String.valueOf(CabDoc.getCodCliente()));
				}
			}
			
			else
			{
				rpSen.updateFacCabDocumentosPostgreSQL(CabDoc);
			}
			rpSen.insertInfoAdicional(emite);
			if(CabDoc.getListDetalleDocumento()!= null)
				rpSen.insertFacDetallesDocumento(CabDoc.getListDetalleDocumento());
			
		}
		
		
		
		if (emite.getInfEmisor().getCodDocumento().equals("06"))
		{
			CabDoc=ServiceDataHilo.preparaCabDocumentoGuiaRem(emite,
															  infoEmp.getRuc(),
															  emite.getInfEmisor().getCodEstablecimiento(),
															  emite.getInfEmisor().getCodPuntoEmision(),
															  emite.getInfEmisor().getTipoComprobante(),
															  emite.getInfEmisor().getSecuencial(),
															  "Recibido por el SRI",
															  "RS");
			rpSen = new ReporteSentencias();
			if (!rpSen.existFacCabDocumentos(CabDoc))
			{
				rpSen.insertFacCabDocumentos(CabDoc);
				//int resAdic=rpSen.insertInfoAdicional(emite);
				//rpSen.insertDestinatariosGuiaRemision(emite.getInfEmisor().getListDetDestinatarios());
				
				// Inserto Destinatarios como Clientes en fac_clientes
				for(Destinatarios destinatario:  emite.getInfEmisor().getListDetDestinatarios())
				{
					String ls_codCliente ="";
					if(destinatario.getIdentificacionDestinatario().length()>6)
						ls_codCliente = destinatario.getIdentificacionDestinatario().substring(destinatario.getIdentificacionDestinatario().length()-6);
					else
						ls_codCliente = destinatario.getIdentificacionDestinatario();
					
					rpSen.insertaClientes(infoEmp.getRuc(),
										  destinatario.getRazonSocialDestinatario(),
										  destinatario.getDireccionDestinatario(),
										  null, //CabDoc.getEmailCliente(),
										  "C",
										  null, // CabDoc.getTipoIdentificacion(),
										  null, // CabDoc.getRise(),
										  null, // CabDoc.getTelefono(),
										  destinatario.getIdentificacionDestinatario(),
										  ls_codCliente);
				}
			}
			else
			{
				rpSen.updateFacCabDocumentosPostgreSQL(CabDoc);
			}
			
			int resAdic=rpSen.insertInfoAdicional(emite);
			rpSen.insertDestinatariosGuiaRemision(emite.getInfEmisor().getListDetDestinatarios());
		}
		
		/*
		 * ======================================================== FIN -
		 * Validaciones por cada tipo de documento
		 * ========================================================
		 */
		
		String respAutorizacion = "";
		String[] infoAutorizacion = new String[10];
		/*
		 * ======================================================== INI -
		 * Obtengo estado del docuemnto - 2da consulta
		 * ========================================================
		 */
		try
		{
			System.out.println("  --------> CONSULTA DE DOCUMENTO A SRI ");
			System.out.println("  --------> fileXml "+emite.getFilexml());
			respAutorizacion = com.sun.directory.examples.AutorizacionComprobantesWs.autorizarComprobanteIndividual(emite.getInfEmisor().getClaveAcceso(),
																													emite.getFilexml(),
																													new Integer(emite.getInfEmisor().getAmbiente()).toString(),
																													infoEmp.getDirAutorizados(),
																													infoEmp.getDirNoAutorizados(),
																													infoEmp.getDirFirmados(),
																													// VPI - Time out parametrizar
																													intentos,
																													timeIntentos,
																													emite.getInfEmisor().getSecuencial());
			
			System.out.println("  Respuesta ---> " + respAutorizacion.replaceAll("\n", ""));
			
			if (respAutorizacion.equals(""))
			{
				infoAutorizacion[0] = "SIN-RESPUESTA";
			} else {
				infoAutorizacion = respAutorizacion.split("\\|");
			}
			
			
			for(int i=0;i<infoAutorizacion.length;i++)
				System.out.println("Posicion "+i+" :"+infoAutorizacion[i]);
			
		} catch (Exception excep) {
			infoAutorizacion[0] = "SIN-RESPUESTA";
		}
		
		System.out.println("  InfoAutorizacion[0] ---> " + infoAutorizacion[0]);
		
		/*
		 * ======================================================== INI -
		 * Obtengo estado del docuemnto - 2da consulta
		 * ========================================================
		 */
		
		
		
		String reportePdf = "";
		if (infoAutorizacion[0].trim().equals("AUTORIZADO"))
		{
			String xmlString = "";
			String numeroAutorizacion = infoAutorizacion[1];
			String fechaAutorizacion = infoAutorizacion[2];
			XMLGregorianCalendar xmlFechaAutorizacion = XMLGregorianCalendarImpl.parse(fechaAutorizacion);
			Date dateFechaAutorizacion = ServiceDataHilo.toDate(xmlFechaAutorizacion);
			CabDoc.setAutorizacion(numeroAutorizacion);
			
			try
			{
				File verificaXml = new File(infoEmp.getDirAutorizados() + emite.getFilexml());
				if (verificaXml.exists()) {
					xmlString = ArchivoUtils.archivoToString(infoEmp.getDirAutorizados() + emite.getFilexml());
				} else {
					verificaXml = new File(infoEmp.getDirFirmados() + emite.getFilexml());
					if (verificaXml.exists())
						xmlString = ArchivoUtils.archivoToString(infoEmp.getDirFirmados() + emite.getFilexml());
				}
			} catch (Exception e)
			{
				System.out.println("Error:: COPIANDO AUTORIZADO");
				System.out.println(e);
	            System.out.println(e.getMessage());
	            System.out.println(e.getCause());
	        	System.out.println(e.getLocalizedMessage());
	        	System.out.println(e.getStackTrace());
				e.printStackTrace();
			}
			
			rpSen.updateEstadoAutorizacionXmlDocumento(CabDoc, xmlString, fechaAutorizacion, dateFechaAutorizacion, xmlString, "AT", "Autorizado por SRI");

			emite.getInfEmisor().setNumeroAutorizacion(infoAutorizacion[1]);
			emite.getInfEmisor().setFechaAutorizacion(fechaAutorizacion);
			emite.getInfEmisor().setFechaAutorizado(dateFechaAutorizacion);
			nameFile = emite.getFilexml().replace("xml", "pdf");
			ServiceDataHilo.delFile(emite, "", infoEmp.getDirGenerado(), "");
			
			try {
				reportePdf = com.tradise.reportes.reportes.ReporteUtil.generaPdfDocumentos(emite,
																						   emite.getInfEmisor().getRuc(),
																						   emite.getInfEmisor().getCodEstablecimiento(),
																						   emite.getInfEmisor().getCodPuntoEmision(),
																						   emite.getInfEmisor().getCodDocumento(),
																						   emite.getInfEmisor().getSecuencial(),
																						   infoEmp.getPathReports(),
																						   infoEmp.getDirFirmados(),
																						   nameFile);
			} catch (Exception e) {
				System.out.println(e);
	            System.out.println(e.getMessage());
	            System.out.println(e.getCause());
	        	System.out.println(e.getLocalizedMessage());
	        	System.out.println(e.getStackTrace());
	        	e.printStackTrace();
			}
			
			if(CabDoc.getCodigoDocumento().equals("07") || CabDoc.getCodigoDocumento().equals("01") || CabDoc.getCodigoDocumento().equals("04"))
			{
				//System.out.println("  Email del Cliente "+CabDoc.getEmailCliente());
				//int li_envio = ServiceDataHilo.enviaEmailCliente("message_exito", emite, "", "", infoEmp.getDirAutorizados() + emite.getFilexml(), reportePdf, CabDoc.getEmailCliente());
				int li_envio = ServiceDataHilo.enviaEmailCliente("message_exito", emite, "", "", infoEmp.getDirAutorizados() + emite.getFilexml(), reportePdf, CabDoc);
				System.out.println((li_envio >= 0) ? "Mail enviado Correctamente" : "Error en envio de Mail");
			}
			
			
			
			
			// Y AQUI ELIMINO LOS ARCHIVOS HFU
		    System.out.println("  Eliminacion de archivos...");
		    ServiceDataHilo.delFile(infoEmp.getDirAutorizados()+emite.getFilexml());
		    ServiceDataHilo.delFile(infoEmp.getDirRecibidos()+emite.getFilexml());
		    ServiceDataHilo.delFile(infoEmp.getDirRecibidos()+emite.getFilexml().replaceAll(".xml", "_backup.xml"));
		    ServiceDataHilo.delFile(infoEmp.getDirRecibidos()+"reenviados/"+emite.getFilexml());
		    ServiceDataHilo.delFile(infoEmp.getDirRecibidos()+"reenviados/"+emite.getFilexml().replaceAll(".xml", "_backup.xml"));
		    ServiceDataHilo.delFile(infoEmp.getDirNoAutorizados()+emite.getFilexml());
		    ServiceDataHilo.delFile(infoEmp.getDirFirmados()+emite.getFilexml());
		    ServiceDataHilo.delFile(infoEmp.getDirFirmados()+emite.getFilexml().replaceAll(".xml", ".pdf"));
		    ServiceDataHilo.delFile(infoEmp.getDirContingencias()+emite.getFilexml());
		    System.out.println("  Archivos eliminados...");
			
			
			if (rpSen.existeUsuario(infoEmp.getRuc(), CabDoc.getCodCliente(),"C") <= 0)
			{
				String msg = rpSen.insertaUsuarioRol(infoEmp.getRuc(),
													 CabDoc.getRazonSocialComprador(),
													 CabDoc.getIdentificacionComprador(),
													 FacEncriptarcadenasControlador.encrypt(CabDoc.getIdentificacionComprador()),
													 "Clien",
													 "C");
				System.out.println(msg);
			}
			// VPI se modifica las condiciones
		} else if (infoAutorizacion[0].equals("NO AUTORIZADO"))
		{
			ServiceDataHilo.delFile(emite,
									infoEmp.getDirFirmados(),
									infoEmp.getDirRecibidos(),
									infoEmp.getDirNoAutorizados());
			
			
			// HFU
			String xmlStringNoAutorizado = ArchivoUtils.archivoToString(infoEmp.getDirNoAutorizados() + emite.getFilexml());
			rpSen.updateEstadoDocumento("NA", respAutorizacion.replace("|", " -"), emite.getInfEmisor().getTipoEmision(), CabDoc, xmlStringNoAutorizado);
			
			int li_envio = ServiceDataHilo.enviaEmail("message_error", emite, "", respAutorizacion, infoEmp.getDirRecibidos() + emite.getFilexml(), null);
			System.out.println((li_envio >= 0) ? "Mail enviado Correctamente" : "Error en envio de Mail");
		}
		else
		{
			try
			{
				
				//rpSen.updateFechaUltimaConsultaDocumento(CabDoc);
				
				emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
											   emite.getInfEmisor().getRuc(),
											   emite.getInfEmisor().getCodEstablecimiento(),
											   emite.getInfEmisor().getCodPuntoEmision(),
											   emite.getInfEmisor().getSecuencial(),
											   emite.getInfEmisor().getTipoComprobante(),
											   strDate,
											   "RC",
											   emite.toString() + "Reproceso Consulta consulta autorizacion sin Respuesta",
											   "", "", "", "", "",
											   emite.getInfEmisor().getTipoEmision());
			} catch (Exception e) {
				System.out.println(e);
	            System.out.println(e.getMessage());
	            System.out.println(e.getCause());
	        	System.out.println(e.getLocalizedMessage());
	        	System.out.println(e.getStackTrace());
	        	e.printStackTrace();
			}
		}
		
		System.out.println("-- FIN Acciones.documentoRecibido --");
	}
	
	// =======================================================================
	// Funcion para estados de contingencia
	// =======================================================================
	public void documentoContingencia(Emisor emite, String respAutorizacion,
			InfoEmpresa infoEmp, FacCabDocumento CabDoc,
			String mensajeContingencia)
	{
		ReporteSentencias rpSen = new ReporteSentencias();
		SimpleDateFormat sm = new SimpleDateFormat("dd-MM-yyyy");
		String strDate = sm.format(new Date());
		int clavesDisponibles = 0;
		
		List umbralClavesContingencia = Environment.c.getList("facElectronica.database.facturacion.sql.umbralClavesContingencias");
		
		if (emite.getInfEmisor().getTipoEmision().equals("1"))
		{
			clavesDisponibles = emite.verificaClavesContingencia(String.valueOf(emite.getInfEmisor().getAmbiente()), emite.getInfEmisor().getRuc());

			// VPI - se agrega validacion de claves de contingencias disponibles
			if (clavesDisponibles > 0)
			{
				for (int i = 0; i < umbralClavesContingencia.size(); i++)
				{
					if (Integer.valueOf(umbralClavesContingencia.get(i).toString()) == clavesDisponibles)
					{
						// Se debe enviar notificacion por mail de que ya quedan
						// pocas
						// claves de contingencia
						System.out.println(" Envio Notificacion :" + umbralClavesContingencia.get(i).toString());
						break;
					}
				}

				// continua flujo normal
				// VPI - se quita validacion por tipos de documentos
				/*
				 * if ((emite.getInfEmisor().getCodDocumento().equals("01")) ||
				 * (emite.getInfEmisor().getCodDocumento().equals("07"))) {
				 */

				respAutorizacion = respAutorizacion + " >>" + mensajeContingencia + ", Verificar que la transaccion se va por el Esquema de Contingencia ";
				// VPI - Se comenta hasta saber con fin se los elimina
				/*
				 * ServiceDataHilo.delFile(emite, infoEmp.getDirFirmados(),
				 * infoEmp.getDirRecibidos(), infoEmp.getDirNoAutorizados());
				 */
				
				rpSen.updateEstadoDocumento("CT", "Contingencia por SRI", "2", CabDoc, null);
				
				// VPI - Falta un metodo que avise si se acabaron las claves
				// de contingencias y que las transacciones las deje en
				// algun
				// estado para que vuelvan a ser tomadas una vez que se
				// reestablezca el servicio o se reestablezca el servicio
				// SRI.
				
				String ls_clave_accesoCont = "";
				String ls_clave_contingencia = "";
				try {
					ls_clave_contingencia = emite.obtieneClaveContingencia(emite.getInfEmisor().getRuc(),
																		   emite.getInfEmisor().getAmbiente(),
																		   "0");
					
					ls_clave_accesoCont = LeerDocumentos.generarClaveAccesoContingencia(emite, ls_clave_contingencia);
					// VPI - xq le pone que es diferente de 49
					if (ls_clave_accesoCont.length() != 49) {

						ls_clave_accesoCont = LeerDocumentos.generarClaveAccesoContingencia(emite, ls_clave_contingencia);
					}
				} catch (Exception e) {
					System.out.println(e);
		            System.out.println(e.getMessage());
		            System.out.println(e.getCause());
		        	System.out.println(e.getLocalizedMessage());
		        	System.out.println(e.getStackTrace());
		        	e.printStackTrace();
				}

				emite.getInfEmisor().setTipoEmision("2");
				emite.getInfEmisor().setClaveAcceso(ls_clave_accesoCont);

				String ls_xml_inicial = ArchivoUtils.archivoToString(infoEmp.getDirRecibidos() + emite.getFilexml().replace(".xml", "_backup.xml"));

				ServiceDataHilo.copiarXmlDir(infoEmp.getDirRecibidos() + emite.getFilexml(),
											 infoEmp.getDirContingencias());

				String ls_xml = ArchivoUtils.archivoToString(infoEmp.getDirContingencias() + emite.getFilexml());
				// Verifico el tipo de emision para saber si es la primera vez
				// que
				// cae
				// en contingencia inserto caso contrario actualizo.
				emite.insertaColaDocumentos(String.valueOf(emite.getInfEmisor().getAmbiente()),
											emite.getInfEmisor().getRuc(),
											emite.getInfEmisor().getCodEstablecimiento(),
											emite.getInfEmisor().getCodPuntoEmision(),
											emite.getInfEmisor().getSecuencial(),
											emite.getInfEmisor().getTipoComprobante(),
											strDate,
											"CT", // VPI se cambia estado TD por "CT"
											infoEmp.getDirContingencias(),
											emite.getFilexml(),
											ls_clave_contingencia,
											emite.getInfEmisor().getClaveAcceso(),
											ls_clave_accesoCont,
											ls_xml,
											ls_xml_inicial);
				// Genero y envio el pdf segun configuracion
				generaEnviaPdf(emite, infoEmp, respAutorizacion);
			} else {
				// VPI - si no existen claves de contingencias se retorna el
				// archivo
				// a generados
				// para que vuelva al flujo normal hasta que se restablezca el
				// servicio o en su defecto
				// hayan claves de contingencias disponibles
				ArchivoUtils.stringToArchivo(infoEmp.getDirGenerado() + emite.getFilexml(),
											 ArchivoUtils.archivoToString(emite.getFileXmlBackup()));
			}

		} else {
			try {
				// VPI - si la emision es dierente de "1" osea que vuelva
				// caer en contingencia
				emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
											   emite.getInfEmisor().getRuc(),
											   emite.getInfEmisor().getCodEstablecimiento(),
											   emite.getInfEmisor().getCodPuntoEmision(),
											   emite.getInfEmisor().getSecuencial(),
											   emite.getInfEmisor().getTipoComprobante(),
											   strDate,
											   "CT",
											   emite.toString() + "Reproceso Consulta consulta autorizacion sin Respuesta",
											   "", "", "", "", "",
											   emite.getInfEmisor().getTipoEmision());

				// Actualizar estado del documento en la cola
				rpSen.updateColaDocumentos(CabDoc, "CT");

			} catch (Exception e) {
				System.out.println(e);
	            System.out.println(e.getMessage());
	            System.out.println(e.getCause());
	        	System.out.println(e.getLocalizedMessage());
	        	System.out.println(e.getStackTrace());
	        	e.printStackTrace();
			}
		}
	}

	// =======================================================================
	// Funcion para generar y enviar PDF
	// =======================================================================
	public void generaEnviaPdf(Emisor emite, InfoEmpresa infoEmp, String mensajeAutorizacion)
	{
		String reportePdfContingencia = null;
		String enviaPdf = Environment.c.getString("facElectronica.general.contingencia.enviaPdf");
		String envioCliente = Environment.c.getString("facElectronica.general.contingencia.envioCliente");
		
		if (enviaPdf.equals("S"))
		{
			try
			{
				String nameFile = emite.getFilexml().replace("xml", "pdf");
				reportePdfContingencia = com.tradise.reportes.reportes.ReporteUtil.generaPdfDocumentos(emite,
																									   emite.getInfEmisor().getRuc(),
																									   emite.getInfEmisor().getCodEstablecimiento(),
																									   emite.getInfEmisor().getCodPuntoEmision(),
																									   emite.getInfEmisor().getCodDocumento(),
																									   emite.getInfEmisor().getSecuencial(),
																									   infoEmp.getPathReports(),
																									   infoEmp.getDirFirmados(),
																									   nameFile);				
				int li_envio = -1;
				
				if (envioCliente.equals("S")) {
					li_envio = ServiceDataHilo.enviaEmailCliente("message_exito", emite, "", "", null, null, reportePdfContingencia);
				} else {
					//li_envio = ServiceDataHilo.enviaEmail("message_error", emite, "", mensajeAutorizacion, null, reportePdfContingencia);
				}
				
				if (li_envio >= 0)
					System.out.println("Mail contingencia enviado Correctamente");
				else
					System.out.println("Error en envio de Mail contingencia");
			} catch (Exception e) {
				System.out.println(e);
	            System.out.println(e.getMessage());
	            System.out.println(e.getCause());
	        	System.out.println(e.getLocalizedMessage());
	        	System.out.println(e.getStackTrace());
	        	e.printStackTrace();
			}
		}

	}	
	
}
