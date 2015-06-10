package com.sun.directory.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.sun.DAO.ControlErrores;
import com.sun.DAO.Destinatarios;
import com.sun.DAO.DetalleDocumento;
import com.sun.DAO.DetalleImpuestosRetenciones;
import com.sun.DAO.DetalleTotalImpuestos;
import com.sun.DAO.DocumentoImpuestos;
import com.sun.DAO.InformacionAdicional;
import com.sun.businessLogic.validate.Emisor;
import com.sun.businessLogic.validate.LeerDocumentos;
import com.sun.comprobantes.util.EmailSender;
import com.sun.comprobantes.util.FormGenerales;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import com.tradise.reportes.entidades.DetalleGuiaRemision;
import com.tradise.reportes.entidades.FacCabDocumento;
import com.tradise.reportes.entidades.FacDetDocumento;
import com.tradise.reportes.servicios.ReporteSentencias;
import com.tradise.reportes.util.key.GenericTransaction;
import com.util.util.key.Environment;
import com.util.webServices.EnvioComprobantesWs;

public class ServiceDataHilo extends GenericTransaction
{
	private int idHilo;
	private InfoEmpresa infoEmp;
	private Emisor emite;
	
	public static String classReference = "ServiceData";
	public static StringBuilder SBmsj = null;
	public File fxml = null;
	public File[] contenido;
	public static org.apache.log4j.Logger log = null;
	private static String nivelLog= null;
	private int intentos = 5;
	private int timeIntentos = 3000;
	private static String nombreEmpresa = Environment.c.getString("facElectronica.alarm.email.nombreEmpresa");
	
	public ServiceDataHilo(int hilo,
						   InfoEmpresa infoEmpresa,
						   Emisor emitir,
						   String nivelLog,
						   int intentos,
						   int timeIntentos)
	{
		this.nivelLog = nivelLog;
		if (log == null)
		{
			Properties props=new Properties();
			props.setProperty("log4j.appender.file","org.apache.log4j.DailyRollingFileAppender");
			props.setProperty("log4j.appender.file.DatePattern","'.'yyyy-MM-dd");
			props.setProperty("log4j.appender.file.layout.ConversionPattern","%d %X{thread-id} [%-5p] %m%n");		
			props.setProperty("log4j.appender.file.maxFileSize","100MB");
			props.setProperty("log4j.appender.file.maxBackupIndex","100");
			props.setProperty("log4j.appender.file.File","./logs/Invoice_"+hilo+".log");
			props.setProperty("log4j.appender.file.threshold","debug");
			props.setProperty("log4j.appender.file.layout","org.apache.log4j.PatternLayout");
			props.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
			props.setProperty("log4j.logger."+"Thread" + hilo,nivelLog+", file");
			PropertyConfigurator.configure(props);
			log = Logger.getLogger("Thread" + hilo);
		}
		ServiceData.listAtencion.add("S");
		this.idHilo = hilo;
		this.infoEmp = infoEmpresa;
		this.emite = emitir;
		this.intentos = intentos;
		this.timeIntentos = timeIntentos;
	}
	
	public void atiendeHilo() throws Exception
	{
		System.out.println(" =============================== INICIO HILO =============================== ");
		boolean flagFile = false,
		li_result = false;
		String mensaje = "",
		 respuestaFirma = "", nameFile = "";
		mostrar();
		obtieneInfoXml(emite.getFilexml());
		System.out.println("Atiende Hilo::" + idHilo);
		flagFile = false;
		FacCabDocumento CabDoc = null;
		ReporteSentencias rpSen = null;
		try {
			flagFile = validaEstadoDocumento(emite);
		} catch (Exception e) {
			mensaje = e.getMessage();
			flagFile = false;
		}
		if (!flagFile)
		{
			File f = new File(infoEmp.getDirRecibidos() + emite.getFilexml());
			f = new File(infoEmp.getDirRecibidos() + emite.getFilexml().replace(".xml", "_backup.xml"));
			if (f.exists()) {
				f.delete();
			}
			f = new File(infoEmp.getDirectorio() + emite.getFilexml());
			if (f.exists())
			{
				f.renameTo(new File(infoEmp.getDirRecibidos() + emite.getFilexml()));
			}
		}
		
		if (flagFile)
		{
			System.out.println("Empieza todo...");
			
			// Ingreso de Bitacora
			SimpleDateFormat sm = new SimpleDateFormat("dd-MM-yyyy");
			String strDate = sm.format(new Date());
			
			/*emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
											emite.getInfEmisor().getRuc(), 
											emite.getInfEmisor().getCodEstablecimiento(),
											emite.getInfEmisor().getCodPuntoEmision(),
											emite.getInfEmisor().getSecuencial(),
											emite.getInfEmisor().getTipoComprobante(),
											strDate, 
											"IN",
											emite.toString() + "Carga Inicial del Proceso de Despacho",
											"",
											"",
											"", 
											"", 
											"", 
											"1");*/
			
			emite.insertaBitacoraDocumento(strDate, "IN", emite.toString() + "Carga Inicial del Proceso de Despacho",
										   "", "", "",  "",  "");
			
			
			try
			{
				leerXml(infoEmp.getDirRecibidos() + emite.getFilexml(), emite);
				/*emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
												emite.getInfEmisor().getRuc(),
												emite.getInfEmisor().getCodEstablecimiento(),
												emite.getInfEmisor().getCodPuntoEmision(),
												emite.getInfEmisor().getSecuencial(),
												emite.getInfEmisor().getTipoComprobante(),
												strDate,
												"LX",
												emite.toString() + "Lectura del XML Terminada Proceso de Despacho",
												"", "", "", "", "",
												emite.getInfEmisor().getTipoEmision());*/
				
				emite.insertaBitacoraDocumento(strDate, 
											   "LX",
											   emite.toString() + "Lectura del XML Terminada Proceso de Despacho",
											   "", "", "", "", "");
				
				
				
				// Generacion de Xml de Backup
				li_result = copiarXml(infoEmp.getDirRecibidos() + emite.getFilexml());
				
				if (li_result)
				{
					if (emite.getInfEmisor().getTipoEmision().equals("1"))
					{
						emite.setFileXmlBackup(infoEmp.getDirRecibidos() + emite.getFilexml().replace(".xml", "_backup.xml"));
						emite = ModifyDocumentAcceso.addPutClaveAcceso(infoEmp.getDirRecibidos() + emite.getFilexml(), emite);
						
						/*emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
														emite.getInfEmisor().getRuc(),
														emite.getInfEmisor().getCodEstablecimiento(),
														emite.getInfEmisor().getCodPuntoEmision(),
														emite.getInfEmisor().getSecuencial(),
														emite.getInfEmisor().getTipoComprobante(),
														strDate,
														"MA",
														emite.toString() + "Modificacion de Clave de Acceso Terminada Proceso de Despacho",
														"", "", "", "", "",
														emite.getInfEmisor().getTipoEmision());*/
						
						emite.insertaBitacoraDocumento(strDate,
													   "MA",
													   emite.toString() + "Modificacion de Clave de Acceso Terminada Proceso de Despacho",
													   "", "", "", "", "");
						
						
						
					} else {
					}
				}

			} catch (Exception ex)
			{
				mensaje = " Error en Leer Xml. Mensaje->" + ex.getMessage()
						+ "::Ruc::" + emite.getInfEmisor().getRuc()
						+ " ::Establecimiento::"
						+ emite.getInfEmisor().getCodEstablecimiento()
						+ " ::Punto Emision::"
						+ emite.getInfEmisor().getCodPuntoEmision()
						+ " ::CodDocumento::"
						+ emite.getInfEmisor().getCodDocumento()
						+ " ::Secuencial::"
						+ emite.getInfEmisor().getSecuencial()
						+ "::ClaveAcceso::" + emite.toString();
				ex.printStackTrace();
				throw new Exception(mensaje);
			}
			try {
				validacionAdicional(emite);
			} catch (Exception e) {
				mensaje = e.getMessage();
				emite.setResultado(-1);
				System.out.println(e.getMessage());
				System.out.println(e.getStackTrace());
			}
			if (emite.getResultado() == 0)
			{
				// Validacion con el archivo XSD
				String ls_validaXSD = "";
				ls_validaXSD = com.sun.directory.examples.ArchivoUtils.validaArchivoXSD(emite.getInfEmisor().getTipoComprobante(),
																						infoEmp.getDirRecibidos() + emite.getFilexml(),
																						infoEmp.getPathXsd());
				
				/*emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
															  emite.getInfEmisor().getRuc(),
															  emite.getInfEmisor().getCodEstablecimiento(),
															  emite.getInfEmisor().getCodPuntoEmision(),
															  emite.getInfEmisor().getSecuencial(),
															  emite.getInfEmisor().getTipoComprobante(),
															  strDate,
															  "VX",
															  emite.toString() + "Validacion XSD Terminada " + ls_validaXSD,
															  "", "", "", "", "",
															  emite.getInfEmisor().getTipoEmision());*/
				
				emite.insertaBitacoraDocumento(strDate,
						  					   "VX",
						  					   emite.toString() + "Validacion XSD Terminada " + ls_validaXSD,
						  					   "", "", "", "", "");
				
				
				
				
				
				// VPI - Validacion XSD que estaba al ultimo -- Logica inversa
				if (!(ls_validaXSD == null)/* || !(ls_validaXSD.equals("")) */)
				{
					// VPI - Validacion XSD que estaba al ultimo - se por
					// excepcion si es diferente de null
					// Error en validacion del XSD
					sm = new SimpleDateFormat("dd-MM-yyyy");
					strDate = sm.format(new Date());
					/*emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
																	emite.getInfEmisor().getRuc(),
																	emite.getInfEmisor().getCodEstablecimiento(),
																	emite.getInfEmisor().getCodPuntoEmision(),
																	emite.getInfEmisor().getSecuencial(),
																	emite.getInfEmisor().getTipoComprobante(),
																	strDate,
																	"EX",
																	"Validacion en XSD::" + ls_validaXSD, 
																	"", "", "",	"", "",
																	emite.getInfEmisor().getTipoEmision());*/
					
					emite.insertaBitacoraDocumento(strDate,
												   "EX",
												   "Validacion en XSD::" + ls_validaXSD, 
												   "", "", "",	"", "");
					
					
					
					int li_envio = enviaEmail("message_error", emite, "", "Validacion en XSD::" + ls_validaXSD, null, null);
				} else
				{
					// VPI - se comenta codigo quemado
					/*
					 * li_result = true; if (li_result){
					 */
					try {
						// Preparacion del xml en byte para firmarlo
						fxml = new File(infoEmp.getDirRecibidos() + emite.getFilexml());
						byte xmlbyte[] = com.sun.directory.examples.ArchivoUtils.archivoToByte(fxml);
					} catch (IOException excIo)
					{
						SBmsj.append(classReference	+ "::main>>FacturacionElectronica.Service::main::" + excIo.toString() + "::::");
						fxml = new File(infoEmp.getDirRecibidos() + emite.getFilexml());
						throw new Exception(SBmsj.toString());
					}
					// Preparacion del Ruc para la firmar el documento
					emite.getInfEmisor().setRucFirmante(infoEmp.getRucFirmante());
					// Firmado del Documento
					if ((System.getProperty("os.name").toUpperCase().indexOf("LINUX") == 0)	|| (System.getProperty("os.name").toUpperCase().indexOf("MAC") == 0))
					{
						respuestaFirma = com.sun.directory.examples.ArchivoUtils.firmarArchivo(emite,
																								infoEmp.getDirRecibidos() + emite.getFilexml(),
																								infoEmp.getDirFirmados(),
																								infoEmp.getTipoFirma()/* "BCE_IKEY2032" */,
																								infoEmp.getClaveFirma(),
																								infoEmp.getRutaFirma());
					} else {
						respuestaFirma = com.sun.directory.examples.ArchivoUtils.firmarArchivo(emite,
																								infoEmp.getDirRecibidos() + emite.getFilexml(),
																								infoEmp.getDirFirmados(),
																								infoEmp.getTipoFirma()/* "BCE_IKEY2032" */,
																								null,//infoEmp.getClaveFirma(),//NULL
																								infoEmp.getRutaFirma());
					}
					/*emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
													emite.getInfEmisor().getRuc(),
													emite.getInfEmisor().getCodEstablecimiento(),
													emite.getInfEmisor().getCodPuntoEmision(),
													emite.getInfEmisor().getSecuencial(),
													emite.getInfEmisor().getTipoComprobante(),
													strDate,
													"FX",
													emite.toString() + "::" + emite.toStringInfo() + "::Validacion Firmado Terminada " + respuestaFirma,
													"", "", "", "", "",
													emite.getInfEmisor().getTipoEmision());*/
					
					
					emite.insertaBitacoraDocumento(strDate,
												   "FX",
												   emite.toString() + "::" + emite.toStringInfo() + "::Validacion Firmado Terminada " + respuestaFirma,
												   "", "", "", "", "");
					
					
					
					// Respuesta del firmado del Documento
					// VPI - se invierte logica
					// if (respuestaFirma == null)
					if (respuestaFirma != null)
					{
						// VPI - Si hay algun fallo en el firmado != null
						// Error en Firmado del Documento
						int li_envio = enviaEmail("message_error", emite, "", " Error al firmar documento", null, null);

						if (li_envio < 0)
							System.out.println("Error en envio de Mail");
						
						// Se regitra en bitacora error en firmado
						/*emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
														emite.getInfEmisor().getRuc(),
														emite.getInfEmisor().getCodEstablecimiento(),
														emite.getInfEmisor().getCodPuntoEmision(),
														emite.getInfEmisor().getSecuencial(),
														emite.getInfEmisor().getTipoComprobante(),
														strDate,
														"EF",// Error Firma
														emite.toString() + "::" + emite.toStringInfo() + "::Error al firmar documento " + respuestaFirma,
														"", "", "", "", "",
														emite.getInfEmisor().getTipoEmision());*/
						
						
						emite.insertaBitacoraDocumento(strDate,
													   "EF",// Error Firma
													   emite.toString() + "::" + emite.toStringInfo() + "::Error al firmar documento " + respuestaFirma,
													   "", "", "", "", "");
						// VPI - Fin error en firmado
					} else {
						
						
						
						
						// VALIDO SI EL DOCUMENTO EXITE EN RS
						
						/*if(emite.existeDocumentoEnEstado(emite, "RS"))
						{
							System.out.println(" Existe documento en RS...");
							return;
						}*/
						
						///
						
						
						/*emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
								emite.getInfEmisor().getRuc(),
								emite.getInfEmisor().getCodEstablecimiento(),
								emite.getInfEmisor().getCodPuntoEmision(),
								emite.getInfEmisor().getSecuencial(),
								emite.getInfEmisor().getTipoComprobante(),
								strDate,
								"CA",
								emite.toString() + "::" + emite.toStringInfo() + "::Antes de envio a SRI, Clave de Aceso "+emite.getInfEmisor().getClaveAcceso(),
								"ClaveAcceso"+emite.getInfEmisor().getClaveAcceso(), "", "", "", "",
								emite.getInfEmisor().getTipoEmision());*/
						
						emite.insertaBitacoraDocumento(strDate,
													   "CA",
													   emite.toString() + "::" + emite.toStringInfo() + "::Antes de envio a SRI, Clave de Aceso "+emite.getInfEmisor().getClaveAcceso(),
													   "ClaveAcceso"+emite.getInfEmisor().getClaveAcceso(), "", "", "", "");
						
						
						
						
						
						
						File fileFirmado = new File(infoEmp.getDirFirmados() + emite.getFilexml());
						ec.gob.sri.comprobantes.ws.RespuestaSolicitud respuestaRecepcion = new ec.gob.sri.comprobantes.ws.RespuestaSolicitud();
						try {
							respuestaRecepcion = solicitudRecepcion(fileFirmado, emite, 100);
							if (respuestaRecepcion == null || respuestaRecepcion.getEstado() ==null)
							{
								respuestaRecepcion = new ec.gob.sri.comprobantes.ws.RespuestaSolicitud();
								respuestaRecepcion.setEstado("ERROR-GENERAL");
							}
						} catch (Exception e)
						{
							respuestaRecepcion = new ec.gob.sri.comprobantes.ws.RespuestaSolicitud();
							respuestaRecepcion.setEstado("ERROR-GENERAL");
							StackTraceElement[] ste = e.getStackTrace();
							for (StackTraceElement i : ste)
							e.printStackTrace();
						}

						System.out.println("   "+ emite.toString() + "EstadoAutorizacion::" + respuestaRecepcion.getEstado());

						/*************************************************************************/
						/* Validacion de Estado RECIBIDA */
						/*************************************************************************/
						Acciones ac = new Acciones();// Clase para encapsular las acciones a realizar
						
						
						if (respuestaRecepcion.getEstado().equals("RECIBIDA"))
						{
							/*emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
									emite.getInfEmisor().getRuc(),
									emite.getInfEmisor().getCodEstablecimiento(),
									emite.getInfEmisor().getCodPuntoEmision(),
									emite.getInfEmisor().getSecuencial(),
									emite.getInfEmisor().getTipoComprobante(),
									strDate,
									"TR",
									emite.toString() + "::" + emite.toStringInfo() + "::Transaccion Recibida, Clave de Aceso "+emite.getInfEmisor().getClaveAcceso(),
									"", "", "", "", "",
									emite.getInfEmisor().getTipoEmision());*/
							
							emite.insertaBitacoraDocumento(strDate,
														   "TR",
														   emite.toString() + "::" + emite.toStringInfo() + "::Transaccion Recibida, Clave de Aceso "+emite.getInfEmisor().getClaveAcceso(),
														   "", "", "", "", "");
							
							ac.documentoRecibido(emite, infoEmp);
							/*************************************************************************/
							/* Validacion de Estado DEVUELTA */
							/*************************************************************************/
						} else if (respuestaRecepcion.getEstado().equals("DEVUELTA"))
						{
							int respSize = respuestaRecepcion.getComprobantes().getComprobante().size();
							String ls_mensaje_respuesta = "";
							String ls_tipo = "";
							String ls_mensaje = "";
							String ls_infoAdicional = "";
							
							if (respSize > 0)
							{
								for (int r = 0; r < respSize; r++) {
									ec.gob.sri.comprobantes.ws.Comprobante respuesta = respuestaRecepcion.getComprobantes().getComprobante().get(r);
									int respMsjSize = respuesta.getMensajes().getMensaje().size();
									for (int m = 0; m < respMsjSize; m++) 
									{
										ls_tipo = respuesta.getMensajes().getMensaje().get(m).getTipo();
										if (ls_tipo.equals("ERROR")) {
											ls_mensaje = respuesta.getMensajes().getMensaje().get(m).getMensaje();
											ls_infoAdicional = respuesta.getMensajes().getMensaje().get(m).getInformacionAdicional();
											ls_mensaje_respuesta = ls_mensaje_respuesta + ls_infoAdicional + " (" + ls_mensaje + ") " + "\n";
											System.out.println("mensaje respuesta... "+ls_mensaje_respuesta);
										}
									}
								}
							}
							
							delFile(emite, infoEmp.getDirFirmados(), infoEmp.getDirGenerado(), infoEmp.getDirNoAutorizados());
							sm = new SimpleDateFormat("dd-MM-yyyy");
							strDate = sm.format(new Date());

							/*emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
																			emite.getInfEmisor().getRuc(),
																			emite.getInfEmisor().getCodEstablecimiento(),
																			emite.getInfEmisor().getCodPuntoEmision(),
																			emite.getInfEmisor().getSecuencial(),
																			emite.getInfEmisor().getTipoComprobante(),
																			strDate,
																			"TD",
																			ls_mensaje_respuesta,
																			emite.getInfEmisor().getClaveAcceso(), "", "", "",	"",
																			emite.getInfEmisor().getTipoEmision());*/
							
							emite.insertaBitacoraDocumento(strDate,
														   "TD",
														   ls_mensaje_respuesta,
														   emite.getInfEmisor().getClaveAcceso(), "", "", "",	"");
							
							// VPI - se actualiza informacion en
							// fac_cab_documento
							int li_envio = enviaEmail("message_error", emite, "", "Transaccion devuelta : "+ls_mensaje_respuesta, null, null);
							System.out.println((li_envio < 0) ?"Mail enviado Correctamente": "Error en envio de Mail");
						} else
						{
							int clavesDisponibles = 0;
							List umbralClavesContingencia = Environment.c.getList("facElectronica.database.facturacion.sql.umbralClavesContingencias");
							
							clavesDisponibles = emite.verificaClavesContingencia(String.valueOf(emite.getInfEmisor().getAmbiente()),
																				 emite.getInfEmisor().getRuc());
							// Verifico si hay claves disponibles para proceder
							// por contingencia
							if (clavesDisponibles > 0)
							{
								for (int i = 0; i < umbralClavesContingencia.size(); i++)
								{
									if (umbralClavesContingencia.get(i).toString().equals(clavesDisponibles))
									{
										// Se debe enviar notificacion por mail
										// de que ya quedan
										// pocas
										// claves de contingencia
										//
										//
										//
										//
										//
										//
									}
								}

								if (emite.getInfEmisor().getTipoEmision().equals("1"))
								{
									/*emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
																	emite.getInfEmisor().getRuc(),
																	emite.getInfEmisor().getCodEstablecimiento(),
																	emite.getInfEmisor().getCodPuntoEmision(),
																	emite.getInfEmisor().getSecuencial(),
																	emite.getInfEmisor().getTipoComprobante(),
																	strDate,
																	"IN",
																	emite.toString() + "Proceso Consulta RecepcionComprobantes Sin Respuesta",
																	emite.getInfEmisor().getClaveAcceso(), "", "", "", "",
																	emite.getInfEmisor().getTipoEmision());*/
									
									emite.insertaBitacoraDocumento(strDate,
																   "IN",
																   emite.toString() + "Proceso Consulta RecepcionComprobantes Sin Respuesta",
																   emite.getInfEmisor().getClaveAcceso(), "", "", "", "");
									

									String ls_clave_contingencia = emite.obtieneClaveContingencia(emite.getInfEmisor().getRuc(),
																								  emite.getInfEmisor().getAmbiente(),
																								  "0");
									
									String ls_clave_accesoCont = "";
									try {
										synchronized(this)
										{
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

									
									if (emite.getInfEmisor().getCodDocumento().equals("01"))
									{
										CabDoc = preparaCabDocumentoFac(emite,
																		infoEmp.getRuc(),
																		emite.getInfEmisor().getCodEstablecimiento(),
																		emite.getInfEmisor().getCodPuntoEmision(),
																		emite.getInfEmisor().getTipoComprobante(),
																		emite.getInfEmisor().getSecuencial(),
																		"No Receptado SRI Contingencia",
																		"CT");
																		rpSen = new ReporteSentencias();

										if (!rpSen.existFacCabDocumentos(CabDoc))
										{
											rpSen.insertFacCabDocumentos(CabDoc);
											rpSen.insertFacDetallesDocumento(CabDoc.getListDetalleDocumento());
											int resAdic = rpSen.insertInfoAdicional(emite);
										}
									}
									
									if (emite.getInfEmisor().getCodDocumento().equals("07"))
									{
										CabDoc = preparaCabDocumentoRet(emite,
																		infoEmp.getRuc(),
																		emite.getInfEmisor().getCodEstablecimiento(),
																		emite.getInfEmisor().getCodPuntoEmision(),
																		emite.getInfEmisor().getTipoComprobante(),
																		emite.getInfEmisor().getSecuencial(),
																		"No Receptado SRI Contingencia",
																		"CT");
										rpSen = new ReporteSentencias();
										if (!rpSen.existFacCabDocumentos(CabDoc))
										{
											rpSen.insertFacCabDocumentos(CabDoc);
											rpSen.insertFacDetallesRetenciones(CabDoc);
											int resAdic = rpSen.insertInfoAdicional(emite);
										}
									}
									
									if (emite.getInfEmisor().getCodDocumento().equals("04"))
									{
										CabDoc = preparaCabDocumentoCre(emite,
																		infoEmp.getRuc(),
																		emite.getInfEmisor().getCodEstablecimiento(),
																		emite.getInfEmisor().getCodPuntoEmision(),
																		emite.getInfEmisor().getTipoComprobante(),
																		emite.getInfEmisor().getSecuencial(),
																		"No Receptado SRI Contingencia",
																		"CT");
										rpSen = new ReporteSentencias();
										if (!rpSen.existFacCabDocumentos(CabDoc))
										{
											rpSen.insertFacCabDocumentos(CabDoc);
											int resAdic = rpSen.insertInfoAdicional(emite);
											rpSen.insertFacDetallesDocumento(CabDoc.getListDetalleDocumento());
										}
									}
									
									if (emite.getInfEmisor().getCodDocumento().equals("06"))
									{
										CabDoc=preparaCabDocumentoGuiaRem(emite,
																		  infoEmp.getRuc(),
																		  emite.getInfEmisor().getCodEstablecimiento(),
																		  emite.getInfEmisor().getCodPuntoEmision(),
																		  emite.getInfEmisor().getTipoComprobante(),
																		  emite.getInfEmisor().getSecuencial(),
																		  "No Receptado SRI Contingencia",
																		  "CT");
										rpSen = new ReporteSentencias();
										if (!rpSen.existFacCabDocumentos(CabDoc))
										{
											rpSen.insertFacCabDocumentos(CabDoc);
											int resAdic=rpSen.insertInfoAdicional(emite);
											rpSen.insertDestinatariosGuiaRemision(emite.getInfEmisor().getListDetDestinatarios());
										}
									}
									
									String ls_xml_inicial = ArchivoUtils.archivoToString(infoEmp.getDirRecibidos() + emite.getFilexml().replace(".xml", "_backup.xml"));
									copiarXmlDir(infoEmp.getDirRecibidos() + emite.getFilexml(), infoEmp.getDirContingencias());
									ModifyDocumentAcceso.addPutClaveAccesoContingencia(infoEmp.getDirContingencias() + emite.getFilexml(), emite);
									
									String ls_xml = ArchivoUtils.archivoToString(infoEmp.getDirContingencias() + emite.getFilexml());
									
									/*Thread.currentThread().sleep(1000);
									 * Aquí se ponia en generados */
									
									
									
									emite.insertaColaDocumentos(String.valueOf(emite.getInfEmisor().getAmbiente()),
																emite.getInfEmisor().getRuc(),
																emite.getInfEmisor().getCodEstablecimiento(),
																emite.getInfEmisor().getCodPuntoEmision(),
																emite.getInfEmisor().getSecuencial(),
																emite.getInfEmisor().getTipoComprobante(),
																strDate,
																"CT",
																infoEmp.getDirContingencias(),
																emite.getFilexml(),
																ls_clave_contingencia,
																emite.getInfEmisor().getClaveAcceso(),
																ls_clave_accesoCont,
																ls_xml,
																ls_xml_inicial);
									
									rpSen.updateEstadoContingencia(CabDoc,
																	"CT",
																	"Transaccion en Contingencia de Recepcion",
																	ls_clave_contingencia,
																	ls_clave_accesoCont);
									
									//ac.generaEnviaPdf(emite, infoEmp, "Transaccion en Contingencia de Recepcion");	
									/////////////
									
								} else
								{
									// VPI - si la emision es dierente de "1"
									// osea que vuelva caer en contingencia
									System.out.println("...Se va a contignecia otra vez...");
									/*emite.insertaBitacoraDocumento(String.valueOf(emite.getInfEmisor().getAmbiente()),
																	emite.getInfEmisor().getRuc(),
																	emite.getInfEmisor().getCodEstablecimiento(),
																	emite.getInfEmisor().getCodPuntoEmision(),
																	emite.getInfEmisor().getSecuencial(),
																	emite.getInfEmisor().getTipoComprobante(),
																	strDate,
																	"IN",
																	emite.toString() + "ReProceso Consulta RecepcionComprobantes Sin Respuesta",
																	//"", "", "", "", "",
																	emite.getInfEmisor().getClaveAcceso(), "", "", "", "",
																	emite.getInfEmisor().getTipoEmision());*/
									
									emite.insertaBitacoraDocumento(strDate,
																   "IN",
																   emite.toString() + "ReProceso Consulta RecepcionComprobantes Sin Respuesta",
																   emite.getInfEmisor().getClaveAcceso(), "", "", "", "");
									
									
									
									
									// Actualizar estado del documento.
									try {
										
										
										rpSen.updateColaDocumentos(CabDoc, "CT");
										
										
									} catch (Exception e) {
										System.out.println(e);
							            System.out.println(e.getMessage());
							            System.out.println(e.getCause());
							        	System.out.println(e.getLocalizedMessage());
							        	System.out.println(e.getStackTrace());
							        	e.printStackTrace();
									}
									// JZU Contingencia.
								}
							} else {
								// VPI - si no existen claves de contingencias
								// se retorna el archivo a generados
								// para que vuelva al flujo normal hasta que se
								// restablezca el servicio o en su defecto
								// hayan claves de contingencias disponibles
								
								
								Thread.currentThread().sleep(1000);
								File archivo = new File(infoEmp.getDirGenerado() + emite.getFilexml());
								
								if(!archivo.exists()){
									Thread.currentThread().sleep(1000);
									ArchivoUtils.stringToArchivo(infoEmp.getDirGenerado() + emite.getFilexml(),
											ArchivoUtils.archivoToString(emite.getFileXmlBackup()));
								}
								
							}
						}
					}
				}
			}
		}
	}

	
	public void mostrar()
	{
		
	}
	
	
	public boolean validaEstadoDocumento(Emisor emite)throws Exception
	{
		boolean flagFile=false;
		String estado = "", mensaje = "";
        String ls_statusDocumento = emite.statusDocumento(emite.getInfEmisor().getAmbiente(),
    		  										  	  emite.getInfEmisor().getRuc(),
    		  											  emite.getInfEmisor().getTipoComprobante(),
    		  											  emite.getInfEmisor().getCodEstablecimiento(),
    		  											  emite.getInfEmisor().getCodPuntoEmision(),
    		  											  emite.getInfEmisor().getSecuencial()).trim();
        
        //Estado "RS" para el proceso lo vuelva a tomar por generados
        /*if (ls_statusDocumento.equals("RS"))
        {
        	Acciones ac = new Acciones();
        	leerXml(infoEmp.getDirRecibidos() + emite.getFilexml(), emite);
        	ac.documentoRecibido(emite, infoEmp);
        	
        	mensaje = " El Documento ya se encuentra en estado RECIBIDO por lo cual "
        			 +" solo se procede a consultar su estado de autorizacion "
  	    		  +"Ruc::" + emite.getInfEmisor().getRuc()+ " Establecimiento::"+emite.getInfEmisor().getCodEstablecimiento()
  	    		  +" Punto de Emision::"+emite.getInfEmisor().getCodPuntoEmision()+" Secuncial::"+emite.getInfEmisor().getSecuencial()
  	    		  +" Tipo de Documento::"+emite.getInfEmisor().getTipoComprobante()+" Ambiente::"+emite.getInfEmisor().getAmbiente();
        	throw new Exception(emite.toString()+mensaje);
        }*/

        if (ls_statusDocumento.equals("AT")||ls_statusDocumento.equals("SR"))
        {
	    	if (ls_statusDocumento.equals("AT")){
	    		estado = "AUTORIZADO";
	    	}
	    	
	    	if (ls_statusDocumento.equals("RS")){
	    		estado = "Recibido por el SRI";
	    	}
	    	if (ls_statusDocumento.equals("CT")){
	    		estado = "Contingencia en Recepcion del SRI";
	    	}
    	    flagFile = true;
    	    mensaje = estado+",El Documento ya se encuentra en estado "+ estado +" por lo cual no se procede a reprocesar. " +
    	    		  "Ruc::" + emite.getInfEmisor().getRuc()+ " Establecimiento::"+emite.getInfEmisor().getCodEstablecimiento()+
    	    		  " Punto de Emision::"+emite.getInfEmisor().getCodPuntoEmision()+" Secuncial::"+emite.getInfEmisor().getSecuencial()+
    	    		  " Tipo de Documento::"+emite.getInfEmisor().getTipoComprobante()+" Ambiente::"+emite.getInfEmisor().getAmbiente();
    	    throw new Exception(emite.toString()+mensaje);
    	    
        }
        
        int li_existsContingencia = emite.statusDocumentoContingencia(emite.getInfEmisor().getAmbiente(), 
																  	  emite.getInfEmisor().getRuc(), 
																	  emite.getInfEmisor().getTipoComprobante(), 
																	  emite.getInfEmisor().getCodEstablecimiento(), 
																	  emite.getInfEmisor().getCodPuntoEmision(), 
																	  emite.getInfEmisor().getSecuencial());
        
        String ls_estadoContingencia = emite.getStatusDocumentoContingencia(emite.getInfEmisor().getAmbiente(), 
																	  	    emite.getInfEmisor().getRuc(), 
																		    emite.getInfEmisor().getTipoComprobante(), 
																		    emite.getInfEmisor().getCodEstablecimiento(), 
																		    emite.getInfEmisor().getCodPuntoEmision(), 
																		    emite.getInfEmisor().getSecuencial());
        //if (li_existsContingencia >= 1){
        if (ls_estadoContingencia==null)
        	ls_estadoContingencia = "";
        if (ls_estadoContingencia.equals("PC"))
        {
        	 mensaje = estado+",El Documento ya se encuentra Procesando en estado estado de Contigencia por lo cual no se procede a reprocesar. Ruc::" +
   	    		  emite.getInfEmisor().getRuc()+ " Establecimiento::"+emite.getInfEmisor().getCodEstablecimiento()+" Punto de Emision::"+
   	    		  emite.getInfEmisor().getCodPuntoEmision()+" Secuncial::"+emite.getInfEmisor().getSecuencial()+" Tipo de Documento::"+
   	    		  emite.getInfEmisor().getTipoComprobante()+" Ambiente::"+emite.getInfEmisor().getAmbiente();
        	 throw new Exception(emite.toString()+mensaje);
        }else if(ls_estadoContingencia.equals("CT")){
        		emite.getInfEmisor().setTipoEmision("2");
        		flagFile= true; //Puede Procesar por Contingencia
        	 
        }else{
        	flagFile= true; //Puede Procesar Normalmente
        }
        
		return flagFile;
	}

	
	public void validacionAdicional(Emisor emite)throws Exception
	{
		System.out.println("-- INICIO ServiceDataHilo.validacionAdicional --");
		String mensaje = "";
	    /*Validaciones Adicionales*/
	  	//Verificacion de si existe Establecimiento Configurado en la tabla fac_establecimiento
		if (!emite.existeEstablecimiento(emite.getInfEmisor().getRuc(),emite.getInfEmisor().getCodEstablecimiento())){
   		 	mensaje = " Establecimiento no existe o no se encuentra Activa. Ruc->" +emite.getInfEmisor().getRuc()+ 
   		 			  " Establecimient"+emite.getInfEmisor().getCodEstablecimiento();          
   		 	int li_envio = enviaEmail("message_error", emite,  mensaje,mensaje,null, null);
   	        throw new Exception(mensaje);
   	 	}
	   	//Verificacion de si existe Punto de Emision Configurado en la tabla fac_punto_emision
	   	if (!emite.existePuntoEmision(emite.getInfEmisor().getRuc(),emite.getInfEmisor().getCodEstablecimiento(), emite.getInfEmisor().getCodPuntoEmision())){
	   	       mensaje = " Punto de Emision no existe o no se encuentra Activo. Ruc->" 
	   	    		   	 +emite.getInfEmisor().getRuc()+ " Establecimiento"
	   	    		   	 +emite.getInfEmisor().getCodEstablecimiento()
	   	    		   	 + " Punto Emision"
	   	    		   	 +emite.getInfEmisor().getCodPuntoEmision();
	   	       
	   	       int li_envio = enviaEmail("message_error", emite,  mensaje,mensaje,null, null);
	   	       throw new Exception(mensaje);
	   	}
	   	//Verificacion de si existe documento en Configurado en Punto de Emision
	   	if (!emite.existeDocumentoPuntoEmision(emite.getInfEmisor().getRuc(),emite.getInfEmisor().getCodEstablecimiento(), emite.getInfEmisor().getCodPuntoEmision(), emite.getInfEmisor().getCodDocumento())){
	   	       mensaje = " En el Establecimiento el tipo de Documento no existe o no se encuentra Activo. Ruc->" 
	   	    		   	 +emite.getInfEmisor().getRuc()+ " Establecimient"
	   	    		   	 +emite.getInfEmisor().getCodEstablecimiento()
	   	    		   	 +" Tipo de Document"+emite.getInfEmisor().getCodDocumento();
	   	       
	   	       int li_envio = enviaEmail("message_error", emite,  mensaje,mensaje,null, null);
	   	       throw new Exception(mensaje);
	   	}
	     if (emite.getInfEmisor().getAmbiente()==-1){
   	       mensaje = " Revise el valor del Ambiente-> "
   	    		   	 +emite.getInfEmisor().getAmbiente()+". Ruc->" 
   	    		   	 +emite.getInfEmisor().getRuc()
   	    		   	 + " Establecimient"
   	    		   	 +emite.getInfEmisor().getCodEstablecimiento()
   	    		   	 +" Tipo de Document"
   	    		   	 +emite.getInfEmisor().getCodDocumento();
   	       
   	       int li_envio = enviaEmail("message_error", emite,  mensaje,mensaje,null, null);
   	       throw new Exception(mensaje);
	     }    	        
	    //Obtencion del mail del establecimiento
	     emite.obtieneMailEstablecimiento(emite.getInfEmisor());		        
	     emite.setResultado(0);
		System.out.println("-- FIN ServiceDataHilo.validacionAdicional --");
	}
	
	public static String getXML(String path) throws IOException{
		FileInputStream input = new FileInputStream( new File(path));

		 byte[] fileData = new byte[input.available()];

		 input.read(fileData);
		 input.close();
		 String resultadoXml = new String(fileData, "UTF-8");
		 return resultadoXml;
	}
	
	//***********************//////////////////////////////////////////////////////************************************//
	/*									   	Envios al Sri por WebServices											  */
	//***********************//////////////////////////////////////////////////////************************************//	
	public static ec.gob.sri.comprobantes.ws.RespuestaSolicitud solicitudRecepcion(File archivoFirmado, 
																				   Emisor emi,
																				   int timeout)
	{
		System.out.println("-- INICIO ServiceDataHilo.solicitudRecepcion --");
		ec.gob.sri.comprobantes.ws.RespuestaSolicitud respuestaRecepcion = null;
		try{
		String flagErrores = "";	
			respuestaRecepcion = new ec.gob.sri.comprobantes.ws.RespuestaSolicitud();
	    	respuestaRecepcion = EnvioComprobantesWs.obtenerRespuestaEnvio(archivoFirmado, 
	    																   emi.getInfEmisor().getRuc(), 
	    																   emi.getInfEmisor().getCodDocumento(), 
	    																   emi.getInfEmisor().getClaveAcceso(), 
	    																   FormGenerales.devuelveUrlWs(new Integer(emi.getInfEmisor().getAmbiente()).toString() ,"RecepcionComprobantes"),
	    																   timeout);
		}catch(Exception exc){
			respuestaRecepcion.setEstado(exc.getMessage());
	    }
		System.out.println("-- FIN ServiceDataHilo.solicitudRecepcion --");
		return respuestaRecepcion;		
	}
	//***********************//////////////////////////////////////////////////////************************************//
	/*									   	Envios de Mail															  */
	//***********************//////////////////////////////////////////////////////************************************//		
	public static int enviaEmail(String ls_id_mensaje, Emisor emi, String mensaje_mail, String mensaje_error, String fileAttachXml, String fileAttachPdf){
		System.out.println("-- INICIO ServiceDataHilo.enviaEmail --");
		String resultEnvioMail = "";
		String emailMensaje = "";
		String emailHost = "";
		String emailFrom = "";
		String emailTo = "";
		String emailSubject = "";		
		String emailHelpDesk = "";
		emailMensaje = Environment.c.getString("facElectronica.alarm.email."+ls_id_mensaje);
		String host = Environment.c.getString("facElectronica.alarm.email.host");
		String helpdesk = Environment.c.getString("facElectronica.alarm.email.helpdesk");
		emailHost = host;
		emailFrom = Environment.c.getString("facElectronica.alarm.email.sender");
		emailHelpDesk= helpdesk;
		EmailSender emSend = new EmailSender(emailHost,emailFrom);
		String ambiente = Environment.c.getString("facElectronica.alarm.email.ambiente");
		String clave = Environment.c.getString("facElectronica.alarm.email.password");
		String subject = Environment.c.getString("facElectronica.alarm.email.subject");
		String receivers = "";
		String user = Environment.c.getString("facElectronica.alarm.email.user");
		String tipo_autentificacion = Environment.c.getString("facElectronica.alarm.email.tipo_autentificacion");
		
		String tipoMail = Environment.c.getString("facElectronica.alarm.email.tipoMail");
		receivers = Environment.c.getString("facElectronica.alarm.email.receivers-list");
		
		String noDocumento = "";
		String tipoDoc = "";
		if ((emi.getInfEmisor().getCodEstablecimiento()!=null)&&(emi.getInfEmisor().getCodPuntoEmision()!=null)&&(emi.getInfEmisor().getSecuencial()!=null)){
			noDocumento = emi.getInfEmisor().getCodEstablecimiento()+emi.getInfEmisor().getCodPuntoEmision()+emi.getInfEmisor().getSecuencial();
			tipoDoc = emi.getInfEmisor().getCodDocumento();
		}
		emailMensaje = emailMensaje.replace("|FECHA|", (emi.getInfEmisor().getFecEmision()==null?"":emi.getInfEmisor().getFecEmision().toString()));
		emailMensaje = emailMensaje.replace("|NODOCUMENTO|", (noDocumento==null?"":noDocumento));
		emailMensaje = emailMensaje.replace("|TIPODOC|", (tipoDoc==null?"":tipoDoc));
		
		emailMensaje = emailMensaje.replace("|HELPDESK|", emailHelpDesk);
		emailMensaje = StringEscapeUtils.unescapeHtml(emailMensaje);
		if (ls_id_mensaje.equals("message_error"))
		{
		receivers = Environment.c.getString("facElectronica.alarm.email.receivers-list-error");
			subject = "Error en documento electronico "+nombreEmpresa;
			emailMensaje = emailMensaje.replace("|CabError|", "Hubo inconvenientes con");
			emailMensaje = emailMensaje.replace("|Mensaje|", mensaje_error);
		}
		if (ls_id_mensaje.equals("message_exito"))
		{
			emailMensaje = emailMensaje.replace("|CabMensaje|", " ");
			String subjectMensaje = subject;
			subjectMensaje = subjectMensaje.replace("|NOMEMAIL|", nombreEmpresa).toString();
			subjectMensaje = subjectMensaje.replace("|NUMDOC|", (noDocumento==null?"":noDocumento)).toString();
			String ls_tipoDoc = "";
			if (emi.getInfEmisor().getCodDocumento().equals("01")){
				ls_tipoDoc = "Ha recibido una FACTURA";
			}
			if (emi.getInfEmisor().getCodDocumento().equals("04")){
				ls_tipoDoc = "Ha recibido una NOTA DE CREDITO";
			}
			if (emi.getInfEmisor().getCodDocumento().equals("07")){
				ls_tipoDoc = "Ha recibido un COMPROBANTE DE RETENCION";
			}
			if (emi.getInfEmisor().getCodDocumento().equals("06")){
				ls_tipoDoc = "Ha recibido una Guia de Remision";
			}
			if (emi.getInfEmisor().getCodDocumento().equals("05")){
				ls_tipoDoc = "Ha recibido una NOTA DE DEBITO";
			}
			subjectMensaje = subjectMensaje.replace("|TIPODOC|", ls_tipoDoc).toString();			
			subject = subjectMensaje;
		}
		emSend.setSubject(subject);
		emSend.setPassword(clave);
		emSend.setUser(user);
		emSend.setAutentificacion(tipo_autentificacion);
		emSend.setTipoMail(tipoMail);	
		emailTo = receivers;
		if ((emailTo!=null) && (emailTo.length()>0))
		{
			String[] partsMail = emailTo.split(";");
			resultEnvioMail = emSend.send(emailTo, subject, emailMensaje, fileAttachXml, fileAttachPdf);
		}
		if (resultEnvioMail.equals("Enviado"))
		{
			System.out.println("-- FIN ServiceDataHilo.enviaEmail --");
			return 0;
		}
		else
		{
			System.out.println("  Error de Envio de Mail::"+resultEnvioMail);
			System.out.println("-- FIN ServiceDataHilo.enviaEmail --");
			return -1;
		}
	}
	
	public static int enviaEmailCliente(String ls_id_mensaje, Emisor emi, String mensaje_mail, String mensaje_error, String fileAttachXml, String fileAttachPdf, String emailCliente)
	{
		System.out.println("-- INICIO ServiceDataHilo.enviaEmailCliente --");
		
		String resultEnvioMail = "";
		String host = Environment.c.getString("facElectronica.alarm.email.host");
		String helpdesk = Environment.c.getString("facElectronica.alarm.email.helpdesk");
		String portal = Environment.c.getString("facElectronica.alarm.email.portal");
		String emailMensaje = "";
		String emailHost = "";
		String emailFrom = "";
		String emailHelpDesk = "";
		emailHost = host;
		emailFrom = Environment.c.getString("facElectronica.alarm.email.sender");
		EmailSender emSend = new EmailSender(emailHost,emailFrom);	
		emailHelpDesk =helpdesk;
		String nombreEmpresa= Environment.c.getString("facElectronica.alarm.email.nombreEmpresa");
		emailMensaje = Environment.c.getString("facElectronica.alarm.email."+ls_id_mensaje);		
		String ambiente = Environment.c.getString("facElectronica.alarm.email.ambiente");
		String clave = Environment.c.getString("facElectronica.alarm.email.password");
		
		String user = Environment.c.getString("facElectronica.alarm.email.user");
		String subject = Environment.c.getString("facElectronica.alarm.email.subject");
		String tipo_autentificacion = Environment.c.getString("facElectronica.alarm.email.tipo_autentificacion");
		String tipoMail = Environment.c.getString("facElectronica.alarm.email.tipoMail");
		String receivers = "";
		
		
		if (ambiente.equals("PRUEBAS")){
			receivers = Environment.c.getString("facElectronica.alarm.email.receivers-list");
		}else{
			
			if(emailCliente!=null)
				if(emailCliente.equals("email@email.com")||(emailCliente.equals("notiene")))
					receivers=null;
				else
					receivers=emailCliente;
			
			/*String emailCli = null;
			if (emi.getInfEmisor().getMailCliente()!=null)
				emailCli  ="email@email.com";
			else
				emailCli = "";
			if ((emailCli.equals("email@email.com"))||(emailCli.equals("notiene"))){
				receivers =null;  
			}else{
				receivers = emailCliente;
			}*/
		}
		if (receivers!=null)
		{
			String noDocumento = "";
			String tipoDoc = "";
			if ((emi.getInfEmisor().getCodEstablecimiento()!=null)&&(emi.getInfEmisor().getCodPuntoEmision()!=null)&&(emi.getInfEmisor().getSecuencial()!=null)){
				noDocumento = emi.getInfEmisor().getCodEstablecimiento()+emi.getInfEmisor().getCodPuntoEmision()+emi.getInfEmisor().getSecuencial();
				tipoDoc = emi.getInfEmisor().getCodDocumento();
			}
			emailMensaje = emailMensaje.replace("|FECHA|", (emi.getInfEmisor().getFecEmision()==null?"":emi.getInfEmisor().getFecEmision().toString()));
			emailMensaje = emailMensaje.replace("|NODOCUMENTO|", (noDocumento==null?"":noDocumento));
			emailMensaje = emailMensaje.replace("|TIPODOC|", (tipoDoc==null?"":tipoDoc));
			emailMensaje = emailMensaje.replace("|HELPDESK|", emailHelpDesk);
			emailMensaje = emailMensaje.replace("|CLIENTE|", (emi.getInfEmisor().getRazonSocialComp()==null?"":emi.getInfEmisor().getRazonSocialComp()));
			emailMensaje = emailMensaje.replace("|CODCLIENTE|", (emi.getInfEmisor().getCodCliente()==null?"":emi.getInfEmisor().getCodCliente()));
			emailMensaje = emailMensaje.replace("|PORTAL|", (portal==null?"":portal));
		
			String ls_tipoDocumento ="";
			if (emi.getInfEmisor().getCodDocumento().equals("01"))
				ls_tipoDocumento ="Factura";
			if (emi.getInfEmisor().getCodDocumento().equals("04"))
				ls_tipoDocumento ="Nota de Credito";
			if (emi.getInfEmisor().getCodDocumento().equals("07"))
				ls_tipoDocumento ="Comprobante de Retencion";
			if (emi.getInfEmisor().getCodDocumento().equals("06"))
				ls_tipoDocumento ="Guia de Remision";
		
			emailMensaje = emailMensaje.replace("|TIPODOCUMENTO|", ls_tipoDocumento);
			
			String docSustento = "";
			if(emi.getInfEmisor().getListDetDetImpuestosRetenciones()!=null)
				for(int i=0; i<emi.getInfEmisor().getListDetDetImpuestosRetenciones().size(); i++)
					if(emi.getInfEmisor().getListDetDetImpuestosRetenciones().get(i).getNumDocSustento() != 0)
						docSustento = docSustento + String.valueOf(emi.getInfEmisor().getListDetDetImpuestosRetenciones().get(i).getNumDocSustento())+",";
			
			emailMensaje = emailMensaje.replace("|NUMDOCSUSTENTO|", docSustento);			
			emailMensaje = emailMensaje.replace("|RAZONSOCIALCOMP|", emi.getInfEmisor().getRazonSocialComp());
			emailMensaje = StringEscapeUtils.unescapeHtml(emailMensaje);
			
			
			if (ls_id_mensaje.equals("message_error"))
			{
				receivers = Environment.c.getString("facElectronica.alarm.email.receivers-list-error");
				subject = "Error en documento electronico "+nombreEmpresa;
				emailMensaje = emailMensaje.replace("|CabError|", "Hubo inconvenientes con");
				emailMensaje = emailMensaje.replace("|Mensaje|", mensaje_error);
			}
			if (ls_id_mensaje.equals("message_exito"))
			{
				String subjectMensaje = subject;
				subjectMensaje = subjectMensaje.replace("|NOMEMAIL|", nombreEmpresa).toString();
				subjectMensaje = subjectMensaje.replace("|NUMDOC|", (noDocumento==null?"":noDocumento)).toString();
				String ls_tipoDoc = "";
				if (emi.getInfEmisor().getCodDocumento().equals("01")){
					ls_tipoDoc = "Ha recibido una FACTURA";
				}
				if (emi.getInfEmisor().getCodDocumento().equals("04")){
					ls_tipoDoc = "Ha recibido una NOTA DE CREDITO";
				}
				if (emi.getInfEmisor().getCodDocumento().equals("07")){
					ls_tipoDoc = "Ha recibido un COMPROBANTE DE RETENCION";
				}
				if (emi.getInfEmisor().getCodDocumento().equals("06")){
					ls_tipoDoc = "Ha recibido una Guia de Remision";
				}
				if (emi.getInfEmisor().getCodDocumento().equals("05")){
					ls_tipoDoc = "Ha recibido una NOTA DE DEBITO";
				}
				subjectMensaje = subjectMensaje.replace("|TIPODOC|", ls_tipoDoc).toString();
				emailMensaje = emailMensaje.replace("|CabMensaje|", " ");
				subject = subjectMensaje;
			}
			emSend.setPassword(clave);
			emSend.setSubject(subject);
			emSend.setUser(user);
			emSend.setAutentificacion(tipo_autentificacion);
			emSend.setTipoMail(tipoMail);
			if (receivers!=null)
			{
				if (receivers!=null){
					emailCliente = receivers;
				}
				if ((emailCliente!=null) && (emailCliente.length()>0))
				{
					String[] partsMail = emailCliente.split(";");
					//for(int i=0;i<partsMail.length;i++)
					//if (partsMail[i].length()>0){
					resultEnvioMail = emSend.send(emailCliente, 
												  subject, 
												  emailMensaje,
												  fileAttachXml,
												  fileAttachPdf);
				}
			}
		}else{
			resultEnvioMail = "Sin Email";
		}
		if (resultEnvioMail.equals("Enviado"))
		{
			System.out.println("-- FIN ServiceDataHilo.enviaEmailCliente --");
			return 0;
		}
		else if(resultEnvioMail.equals("Sin Email"))
		{
			System.out.println("-- FIN ServiceDataHilo.enviaEmailCliente --");
			return 0;
		}
		else
		{
			System.out.println("Error de Envio de Mail::"+resultEnvioMail);
			System.out.println("-- FIN ServiceDataHilo.enviaEmailCliente --");
			return -1;
		}
	}
	
	public static int enviaEmailCliente(String ls_id_mensaje, Emisor emi, String mensaje_mail, String mensaje_error, String fileAttachXml, String fileAttachPdf, FacCabDocumento cabDoc)
	{
		System.out.println("-- INICIO ServiceDataHilo.enviaEmailCliente --");
		
		String resultEnvioMail = "";
		String host = Environment.c.getString("facElectronica.alarm.email.host");
		String helpdesk = Environment.c.getString("facElectronica.alarm.email.helpdesk");
		String portal = Environment.c.getString("facElectronica.alarm.email.portal");
		String emailMensaje = "";
		String emailHost = "";
		String emailFrom = "";
		String emailHelpDesk = "";
		emailHost = host;
		emailFrom = Environment.c.getString("facElectronica.alarm.email.sender");
		EmailSender emSend = new EmailSender(emailHost,emailFrom);	
		emailHelpDesk =helpdesk;
		String nombreEmpresa= Environment.c.getString("facElectronica.alarm.email.nombreEmpresa");
		emailMensaje = Environment.c.getString("facElectronica.alarm.email."+ls_id_mensaje);
		
		
		
		
		String logoFirma = Environment.c.getString("facElectronica.alarm.email.logo_firma");
		if(ls_id_mensaje.equals("message_exito") && emi.getInfEmisor().getCodDocumento().equals("07"))
		{
			System.out.println("logoFirma antes..."+logoFirma);
			emailMensaje ="<htm>Estimado(a) Senor(a):<br>"
					+ "|RAZONSOCIALCOMP|<br>"
					+ "Presente.<br><br>"
					+ "Le informamos que hemos emitido, con la autorizacion del Servicio de Rentas Internas, el siguiente |TIPODOCUMENTO|:<br>"
					+ "No. Documento:                |NODOCUMENTO|<br>"
					+ "Factura a la que aplica: |NUMDOCSUSTENTO|<br>"
					+ "Tambien puede consultar sus comprobantes ingresando a http://facturacion.pycca.com, con su usuario y clave<br><br>"
					+ "Atentamente,<br>"
					+ "<img src=\"cid:logo_firma\" width=\"20%\" height=\"10%\" /></html>";
		}
		if(ls_id_mensaje.equals("message_exito") && (emi.getInfEmisor().getCodDocumento().equals("01") || emi.getInfEmisor().getCodDocumento().equals("04")))
		{
			System.out.println("logoFirma antes..."+logoFirma);
			emailMensaje ="<htm>Estimado(a) cliente:<br>"
					+ "Acaba de recibir su Comprobante Electronico generado el |FECHA| con numero |NODOCUMENTO| <br>"
					+ "Este documento ya esta disponible para su visualizacion y descarga visitando facturacion.pycca.com Saludos Cordiales<br><br>"
					+ "Atentamente,<br>"
					+ "<img src=\"cid:logo_firma\" width=\"20%\" height=\"10%\" /></html>";
		}
	

		
		
		String ambiente = Environment.c.getString("facElectronica.alarm.email.ambiente");
		String clave = Environment.c.getString("facElectronica.alarm.email.password");
		
		String user = Environment.c.getString("facElectronica.alarm.email.user");
		String subject = Environment.c.getString("facElectronica.alarm.email.subject");
		String tipo_autentificacion = Environment.c.getString("facElectronica.alarm.email.tipo_autentificacion");
		//String tipoMail = Environment.c.getString("facElectronica.alarm.email.tipoMail");
		String tipoMail = "HTML";
		String receivers = "";
		
		
		if (ambiente.equals("PRUEBAS")){
			receivers = Environment.c.getString("facElectronica.alarm.email.receivers-list");
		}else
		{
			if(cabDoc.getEmailCliente()!=null)
				if(cabDoc.getEmailCliente().equals("email@email.com")||(cabDoc.getEmailCliente().equals("notiene")))
					receivers=null;
				else
					receivers=cabDoc.getEmailCliente();
		}
		if (receivers!=null)
		{
			String noDocumento = "";
			String tipoDoc = "";
			if ((emi.getInfEmisor().getCodEstablecimiento()!=null)&&(emi.getInfEmisor().getCodPuntoEmision()!=null)&&(emi.getInfEmisor().getSecuencial()!=null)){
				noDocumento = emi.getInfEmisor().getCodEstablecimiento()+emi.getInfEmisor().getCodPuntoEmision()+emi.getInfEmisor().getSecuencial();
				tipoDoc = emi.getInfEmisor().getCodDocumento();
			}
			emailMensaje = emailMensaje.replace("|FECHA|", (emi.getInfEmisor().getFecEmision()==null?"":emi.getInfEmisor().getFecEmision().toString()));
			emailMensaje = emailMensaje.replace("|NODOCUMENTO|", (noDocumento==null?"":noDocumento));
			emailMensaje = emailMensaje.replace("|TIPODOC|", (tipoDoc==null?"":tipoDoc));
			emailMensaje = emailMensaje.replace("|HELPDESK|", emailHelpDesk);
			emailMensaje = emailMensaje.replace("|CLIENTE|", (emi.getInfEmisor().getRazonSocialComp()==null?"":emi.getInfEmisor().getRazonSocialComp()));
			emailMensaje = emailMensaje.replace("|CODCLIENTE|", (emi.getInfEmisor().getCodCliente()==null?"":emi.getInfEmisor().getCodCliente()));
			emailMensaje = emailMensaje.replace("|PORTAL|", (portal==null?"":portal));
		
			String ls_tipoDocumento ="";
			if (emi.getInfEmisor().getCodDocumento().equals("01"))
				ls_tipoDocumento ="Factura";
			if (emi.getInfEmisor().getCodDocumento().equals("04"))
				ls_tipoDocumento ="Nota de Credito";
			if (emi.getInfEmisor().getCodDocumento().equals("07"))
				ls_tipoDocumento ="Comprobante de Retencion";
			if (emi.getInfEmisor().getCodDocumento().equals("06"))
				ls_tipoDocumento ="Guia de Remision";
			
			emailMensaje = emailMensaje.replace("|TIPODOCUMENTO|", ls_tipoDocumento);
			
			String docSust="";
			if(emi.getInfEmisor().getCodDocumento().equals("07"))
			{
				if(cabDoc.getListImpuestosRetencion().size()>0)
					//for(int i=0; i<cabDoc.getListImpuestosRetencion().size(); i++)
						docSust = docSust + cabDoc.getListImpuestosRetencion().get(0).getNumDocSustento();// + ",";
			}
			
			
			emailMensaje = emailMensaje.replace("|NUMDOCSUSTENTO|", docSust);	
			emailMensaje = emailMensaje.replace("|RAZONSOCIALCOMP|", emi.getInfEmisor().getRazonSocialComp());
			emailMensaje = StringEscapeUtils.unescapeHtml(emailMensaje);
			
			
			if (ls_id_mensaje.equals("message_error"))
			{
				receivers = Environment.c.getString("facElectronica.alarm.email.receivers-list-error");
				subject = "Error en documento electronico "+nombreEmpresa;
				emailMensaje = emailMensaje.replace("|CabError|", "Hubo inconvenientes con");
				emailMensaje = emailMensaje.replace("|Mensaje|", mensaje_error);
			}
			if (ls_id_mensaje.equals("message_exito"))
			{
				String subjectMensaje = subject;
				subjectMensaje = subjectMensaje.replace("|NOMEMAIL|", nombreEmpresa).toString();
				subjectMensaje = subjectMensaje.replace("|NUMDOC|", (noDocumento==null?"":noDocumento)).toString();
				String ls_tipoDoc = "";
				if (emi.getInfEmisor().getCodDocumento().equals("01")){
					ls_tipoDoc = "Ha recibido una FACTURA";
				}
				if (emi.getInfEmisor().getCodDocumento().equals("04")){
					ls_tipoDoc = "Ha recibido una NOTA DE CREDITO";
				}
				if (emi.getInfEmisor().getCodDocumento().equals("07")){
					ls_tipoDoc = "Ha recibido un COMPROBANTE DE RETENCION";
				}
				if (emi.getInfEmisor().getCodDocumento().equals("06")){
					ls_tipoDoc = "Ha recibido una Guia de Remision";
				}
				if (emi.getInfEmisor().getCodDocumento().equals("05")){
					ls_tipoDoc = "Ha recibido una NOTA DE DEBITO";
				}
				subjectMensaje = subjectMensaje.replace("|TIPODOC|", ls_tipoDoc).toString();
				emailMensaje = emailMensaje.replace("|CabMensaje|", " ");
				subject = subjectMensaje;
			}
			emSend.setPassword(clave);
			emSend.setSubject(subject);
			emSend.setUser(user);
			emSend.setAutentificacion(tipo_autentificacion);
			emSend.setTipoMail(tipoMail);
			emSend.setLogoFirma(logoFirma);
			
			if (receivers!=null)
			{
				String emC="";
				if (receivers!=null){
					emC = receivers;
				}
				if ((emC!=null) && (emC.length()>0))
				{
					String[] partsMail = emC.split(";");
					//for(int i=0;i<partsMail.length;i++)
					//if (partsMail[i].length()>0){
					resultEnvioMail = emSend.send(emC, 
												  subject, 
												  emailMensaje,
												  fileAttachXml,
												  fileAttachPdf);
				}
			}
		}else{
			resultEnvioMail = "Sin Email";
		}
		if (resultEnvioMail.equals("Enviado"))
		{
			System.out.println("-- FIN ServiceDataHilo.enviaEmailCliente --");
			return 0;
		}
		else if(resultEnvioMail.equals("Sin Email"))
		{
			System.out.println("-- FIN ServiceDataHilo.enviaEmailCliente --");
			return 0;
		}
		else
		{
			System.out.println("Error de Envio de Mail::"+resultEnvioMail);
			System.out.println("-- FIN ServiceDataHilo.enviaEmailCliente --");
			return -1;
		}
	}
	
	
	//***********************//////////////////////////////////////////////////////************************************//
	/*							   	Manejo de Archivos del XML														  */
	//***********************//////////////////////////////////////////////////////************************************//	
	//Copiar Xml
	public static boolean copiarXml(String fileName)
	{
		String fileBackup = "";
		try{
			System.out.println("-- INICIO ServiceDataHilo.copiarXml --");
			File fileOrigen = new File(fileName);
			File fileDestino = new File(fileName.replace(".xml", "_backup.xml"));	      
			if (fileOrigen.exists()) {	    	  
	    	  InputStream in = new FileInputStream(fileOrigen);
	    	  OutputStream out = new FileOutputStream(fileDestino);
	    	  byte[] buf = new byte[1024];int len; while ((len = in.read(buf)) > 0) {  out.write(buf, 0, len);}
	    	  in.close();
	    	  out.close();
	    	  fileBackup = fileName.replace(".xml", "_backup.xml");
	    	  System.out.println("-- FIN ServiceDataHilo.copiarXml --");
	    	  return true;	    	  
	      }
	      else{
	    	  System.out.println("-- FIN ServiceDataHilo.copiarXml --");
	    	  return false;
	      }
		}catch(IOException e){
			System.out.println(e);
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        	System.out.println(e.getLocalizedMessage());
        	System.out.println(e.getStackTrace());
        	e.printStackTrace();
			System.out.println("-- FIN ServiceDataHilo.copiarXml --");
			return false;
		}
	}
	
	public static boolean copiarXmlDir(String fileName, String dirDestino){
		String fileBackup = "";
		try{
		  File fileOrigen = new File(fileName);
	      File fileDestino = new File(dirDestino+""+fileOrigen.getName());	      
	      if (fileOrigen.exists()) {	    	  
	    	  InputStream in = new FileInputStream(fileOrigen);
	    	  OutputStream out = new FileOutputStream(fileDestino);
	    	  byte[] buf = new byte[1024];int len; while ((len = in.read(buf)) > 0) {  out.write(buf, 0, len);}
	    	  in.close();
	    	  out.close();
	    	  fileBackup = fileName.replace(".xml", "_backup.xml");
	    	  return true;	    	  
	      }
	      else{
	    	  return false;
	      }
		}catch(IOException e){
			return false;
		}
	}
	
	public boolean copiarXmlDir2(String fileName, String dirDestino){
		try{
		  File fileOrigen = new File(fileName);
	      File fileDestino = new File(dirDestino+""+fileOrigen.getName());	      
	      if (fileOrigen.exists()) {	    	  
	    	  InputStream in = new FileInputStream(fileOrigen);
	    	  OutputStream out = new FileOutputStream(fileDestino);
	    	  byte[] buf = new byte[1024];int len; while ((len = in.read(buf)) > 0) {  out.write(buf, 0, len);}
	    	  in.close();
	    	  out.close();
	    	  return true;	    	  
	      }
	      else{
	    	  return false;
	      }
		}catch(Exception e){
			System.out.println(e);
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        	System.out.println(e.getLocalizedMessage());
        	System.out.println(e.getStackTrace());
        	e.printStackTrace();
			return false;
		}
	}
	
	//Eliminacion de Archivos Firmados, Generados y No Autorizados.
	public static void delFile(Emisor emite, String rutaFirmado, String generado, String dirNoAutorizados)
	{
		//Eliminacion de Archivos				        		  				        		  
		File eliminar = new File(rutaFirmado+emite.getFilexml());
  	  	if (eliminar.exists()) {
  	  		eliminar.delete();
  	  	}
  	  	File fileDel = new File(generado+emite.getFileTxt());
  	  	copiarXml2(fileDel.getAbsolutePath(),dirNoAutorizados+fileDel.getName());
  	  	//Eliminacion de Archivos				        		  				        		  
		eliminar = new File(generado+emite.getFilexml());
  	  	if (eliminar.exists()) {
  	  		eliminar.delete();
  	  	}
  	  
  	  	eliminar = new File(generado+emite.getFileXmlBackup());
  	  	if (eliminar.exists()) {
  	  		eliminar.delete();
  	  	}
  	  	if (fileDel.exists()) {
  	  		fileDel.delete();
  	  	}
  	  	System.out.println("	Delete File");
	}
	
	public static void delFile(String rutaArchivo)
	{
		//Eliminacion de Archivos		        		  
		try{
			File eliminar = new File(rutaArchivo);
	  	  	if (eliminar.exists())
	  	  		eliminar.delete();
		}catch(Exception e){
			System.out.println(e);
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        	System.out.println(e.getLocalizedMessage());
        	System.out.println(e.getStackTrace());
        	e.printStackTrace();
		}
	}
	
	public static int copiarXml2(String fileNameOrigen, String fileNameDestino)
	{
		try
		{
		  File fileOrigen = new File(fileNameOrigen);
	      File fileDestino = new File(fileNameDestino);
	      if (fileOrigen.exists())
	      {
	    	  InputStream in = new FileInputStream(fileOrigen);
	    	  OutputStream out = new FileOutputStream(fileDestino);
	    	  byte[] buf = new byte[1024];int len; while ((len = in.read(buf)) > 0) {  out.write(buf, 0, len);}
	    	  in.close();
	    	  out.close();
	    	  return 1;
	      }
	      else{
	    	  return 0;
	      }
		}catch(IOException e){
			return -1;
		}
	}
	
	//***********************//////////////////////////////////////////////////////************************************//
	/*								Lectura del Nombre del XML														  */
	//***********************//////////////////////////////////////////////////////************************************//	
	public void obtieneInfoXml(String FileName) throws Exception
	{
		//Ambiente
		String ambiente = null;
		try{
			ambiente = FileName.substring(0, 1).trim();
		}catch (Exception e){
			throw new Exception("Name File incorrecto->Ambiente::"+e.toString());
		}		
		if (ambiente== null){
			throw new Exception("Name File incorrecto->Ambiente es null");
		}			
		if (ambiente.length()!= 1){
			throw new Exception("Name File incorrecto->Ambiente tamaño incorrecto::"+ambiente.length());
		}
		emite.getInfEmisor().setAmbiente(Integer.parseInt(ambiente));
		
		//Ruc
		String rucFile = null;
		try{
			rucFile = FileName.substring(1, 14).trim();
		}catch (Exception e){
			throw new Exception("Name File incorrecto->Ruc::"+e.toString());
		}
		if (rucFile== null){
			throw new Exception("Name File incorrecto->Ruc es null");
		}
		if (rucFile.length()!= 13){
			throw new Exception("Name File incorrecto->Ruc tamaño incorrecto::"+rucFile.length());
		}
		emite.getInfEmisor().setRuc(rucFile);
		
		
		//TipoDocumento
		String tipoDocumento= null;
		try{
			tipoDocumento = FileName.substring(14, 16).trim();
		}catch (Exception e){
			throw new Exception("Name File incorrecto->TipoDocumento::"+e.toString());
		}
		if (tipoDocumento== null){
			throw new Exception("Name File incorrecto->TipoDocumento es null");
		}
		if (tipoDocumento.length()!= 2){
			throw new Exception("Name File incorrecto->TipoDocumento tamaño incorrecto::"+tipoDocumento.length());
		}
		emite.getInfEmisor().setTipoComprobante(tipoDocumento);	     
	    emite.getInfEmisor().setCodDocumento(tipoDocumento);
	    
	    //CodEstablecimiento
	    String CodEstablecimiento = null;
	    try{
	    	CodEstablecimiento = FileName.substring(16, 19).trim();
	    }catch (Exception e){
			throw new Exception("Name File incorrecto->CodEstablecimiento::"+e.toString());
		}
	    if (CodEstablecimiento== null){
			throw new Exception("Name File incorrecto->CodEstablecimiento es null");
		}
	    if (CodEstablecimiento.length()!= 3){
			throw new Exception("Name File incorrecto->CodEstablecimiento tamaño incorrecto::"+CodEstablecimiento.length());
		}
	    emite.getInfEmisor().setCodEstablecimiento(CodEstablecimiento);
	    
	    //CodPuntEmision
	    String CodPuntEmision = null;
	    try{
	    	CodPuntEmision = FileName.substring(19, 22);
	    }catch (Exception e){
			throw new Exception("Name File incorrecto->CodPuntEmision::"+e.toString());
		}
	    if (CodPuntEmision== null){
			throw new Exception("Name File incorrecto->CodPuntEmision es null");
		}
	    if (CodPuntEmision.length()!= 3){
			throw new Exception("Name File incorrecto->CodPuntEmision tamaño incorrecto::"+CodPuntEmision.length());
		}
	    emite.getInfEmisor().setCodPuntoEmision(CodPuntEmision);
	    
	    //secuencial
	    String secuencial = null;
	    try{
	    secuencial =  FileName.substring(22, (FileName.length()<=9?FileName.length():31));
	    }catch (Exception e){
			throw new Exception("Name File incorrecto->secuencial::"+e.toString());
		}
	    if (secuencial.length()!= 9){
			throw new Exception("Name File incorrecto->CodPuntEmision tamaño incorrecto::"+secuencial.length());
		}
	    emite.getInfEmisor().setSecuencial(secuencial);
	}


	//***********************//////////////////////////////////////////////////////************************************//
	/*								Lectura del XML																	  */
	//***********************//////////////////////////////////////////////////////************************************//
	
	public static void leerXml(String nameXml, Emisor emite)
	{
		if (emite.getInfEmisor().getTipoComprobante().equals("01"))
			leerFacturaXml(nameXml, emite);
		if (emite.getInfEmisor().getCodDocumento().equals("04"))
			leerNotaCreditoXml(nameXml, emite);
		if (emite.getInfEmisor().getCodDocumento().equals("05"))
			leerNotaDebitoXml(nameXml, emite);
		
		// INI HFU - SE DESCOMENTÓ
		if (emite.getInfEmisor().getCodDocumento().equals("07"))
			leerComprobanteRetXml(nameXml, emite);
		
		if (emite.getInfEmisor().getCodDocumento().equals("06"))
			leerGuiaRemisionXml(nameXml, emite);
		// FIN HFU
	}
	
	//Lectura de Factura
	public static void leerFacturaXml(String nameXml, Emisor emite)
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        try
        {
        	System.out.println("-- INICIO ServiceDataHilo.leerFacturaXml --");
        	
            builder = factory.newDocumentBuilder();
            doc = builder.parse(nameXml);
            System.out.println("	File>>"+nameXml);
 
            // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();
            
            // Create XPath object
            XPath xpath = xpathFactory.newXPath();
            
            //infoTributaria
            XPathExpression expr = xpath.compile("/factura/infoTributaria/tipoEmision/text()");
            emite.getInfEmisor().setTipoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            //if (emite.getInfEmisor().getTipoEmision().equals("2")){
            	expr = xpath.compile("/factura/infoTributaria/claveAcceso/text()");
                emite.getInfEmisor().setClaveAcceso((String) expr.evaluate(doc, XPathConstants.STRING));
            //}
            expr = xpath.compile("/factura/infoTributaria/ambiente/text()");
            emite.getInfEmisor().setAmbiente(Integer.parseInt((String)expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/factura/infoTributaria/ruc/text()");
            emite.getInfEmisor().setRuc((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoTributaria/razonSocial/text()");
            emite.getInfEmisor().setRazonSocial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoTributaria/nombreComercial/text()");
            emite.getInfEmisor().setNombreComercial((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/factura/infoTributaria/codDoc/text()");
            emite.getInfEmisor().setCodDocumento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoTributaria/estab/text()");
            emite.getInfEmisor().setCodEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoTributaria/ptoEmi/text()");
            emite.getInfEmisor().setCodPuntoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoTributaria/secuencial/text()");
            emite.getInfEmisor().setSecuencial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoTributaria/dirMatriz/text()");
            emite.getInfEmisor().setDireccionMatriz((String) expr.evaluate(doc, XPathConstants.STRING));                        
            
            //infoFactura
            expr = xpath.compile("/factura/infoFactura/fechaEmision/text()");
            emite.getInfEmisor().setFecEmision((String)expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/dirEstablecimiento/text()");
            emite.getInfEmisor().setDireccionEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            //expr = xpath.compile("/factura/infoFactura/contribuyenteEspecial/text()");
            //emite.getInfEmisor().setContribEspecial(Integer.parseInt((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/factura/infoFactura/obligadoContabilidad/text()");
            emite.getInfEmisor().setObligContabilidad((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/tipoIdentificacionComprador/text()");
            emite.getInfEmisor().setTipoIdentificacion((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/guiaRemision/text()");
            emite.getInfEmisor().setGuiaRemision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/razonSocialComprador/text()");
            emite.getInfEmisor().setRazonSocialComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/identificacionComprador/text()");
            emite.getInfEmisor().setIdentificacionComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/totalSinImpuestos/text()");
            emite.getInfEmisor().setTotalSinImpuestos(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/factura/infoFactura/totalDescuento/text()");
            emite.getInfEmisor().setTotalDescuento(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
			
            expr = xpath.compile("/factura/infoFactura/propina/text()");
            emite.getInfEmisor().setPropina(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/factura/infoFactura/importeTotal/text()");
            emite.getInfEmisor().setImporteTotal(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/factura/infoFactura/moneda/text()");
            emite.getInfEmisor().setMoneda((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/factura/infoFactura/totalConImpuestos/totalImpuesto[*]/codigo/text()");
            List<String> listCodigo = new ArrayList();            
            NodeList nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigo.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/infoFactura/totalConImpuestos/totalImpuesto[*]/codigoPorcentaje/text()");
            List<String> listCodigoPorcentaje = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigoPorcentaje.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/infoFactura/totalConImpuestos/totalImpuesto[*]/baseImponible/text()");
            List<String> listBaseImponible = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listBaseImponible.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/infoFactura/totalConImpuestos/totalImpuesto[*]/valor/text()");
            List<String> listValor = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listValor.add(nodes.item(i).getNodeValue());
            }
            ArrayList<DetalleTotalImpuestos> listDetDetImpuestos = new ArrayList<DetalleTotalImpuestos>();             
            for (int i=0; i<listCodigo.size(); i++){
            	DetalleTotalImpuestos detImp = new DetalleTotalImpuestos();
            	detImp.setCodTotalImpuestos(Integer.parseInt(listCodigo.get(i).toString()));
            	detImp.setCodPorcentImp(Integer.parseInt(listCodigoPorcentaje.get(i).toString()));
            	detImp.setBaseImponibleImp(Double.parseDouble(listBaseImponible.get(i).toString()));
            	detImp.setValorImp(Double.parseDouble(listValor.get(i).toString()));
            	listDetDetImpuestos.add(detImp);
            }
            
            emite.getInfEmisor().setListDetDetImpuestos(listDetDetImpuestos);
            
            ArrayList<DetalleDocumento> listDetDocumentos = new ArrayList<DetalleDocumento>();
                                               
            expr = xpath.compile("/factura/detalles/detalle[*]/codigoPrincipal/text()");
            List<String> listCodPrin = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int l=0; l<nodes.getLength(); l++){
            	listCodPrin.add(nodes.item(l).getNodeValue());
            	//System.out.println("index::"+i);
            }
            
            expr = xpath.compile("/factura/detalles/detalle[*]/codigoAuxiliar/text()");
            List<String> listCodAux = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodAux.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/detalles/detalle[*]/descripcion/text()");
            List<String> listDescrip = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listDescrip.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/detalles/detalle[*]/cantidad/text()");
            List<String> listCantidad = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCantidad.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/detalles/detalle[*]/precioUnitario/text()");
            List<String> listPrecioUnitario = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listPrecioUnitario.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/detalles/detalle[*]/descuento/text()");
            List<String> listDescuento = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listDescuento.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/factura/detalles/detalle[*]/precioTotalSinImpuesto/text()");
            List<String> listPrecioTotalSinImpuesto = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listPrecioTotalSinImpuesto.add(nodes.item(i).getNodeValue());
            }
            
            for (int i=0; i<listCodPrin.size(); i++){
            	ArrayList<DocumentoImpuestos> listDetImpuestosDocumentos = new ArrayList<DocumentoImpuestos>();            	
            	DetalleDocumento detDoc = new DetalleDocumento();            	
            	detDoc.setCodigoPrincipal(listCodPrin.get(i).toString());
            	if (listCodAux.size()>0)
            	detDoc.setCodigoAuxiliar(listCodAux.get(i).toString());
            	if (listDescrip.size()>0)
            		detDoc.setDescripcion(listDescrip.get(i).toString());
            	if (listCantidad.size()>0)
            		detDoc.setCantidad(Double.parseDouble(listCantidad.get(i).toString()));
            	if (listPrecioUnitario.size()>0)
            		detDoc.setPrecioUnitario(Double.parseDouble(listPrecioUnitario.get(i).toString()));
            	if (listDescuento.size()>0)
            		detDoc.setDescuento(Double.parseDouble(listDescuento.get(i).toString()));
            	if (listPrecioTotalSinImpuesto.size()>0)
            		detDoc.setPrecioTotalSinImpuesto(Double.parseDouble(listPrecioTotalSinImpuesto.get(i).toString()));
            	//detDoc.setListDetImpuestosDocumentos();
            	
            	expr = xpath.compile("/factura/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/codigo/text()");
                List<String> listCodigoImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/factura/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/codigoPorcentaje/text()");
                List<String> listCodigoPorcentajeImpuesto = new ArrayList();
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoPorcentajeImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/factura/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/tarifa/text()");
                List<String> listTarifaImpuesto = new ArrayList();
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listTarifaImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/factura/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/baseImponible/text()");
                List<String> listBaseImponibleImpuesto = new ArrayList();
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listBaseImponibleImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/factura/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/valor/text()");
                List<String> listValorImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listValorImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                for (int j=0; j<listCodigoImpuesto.size(); j++){
                	DocumentoImpuestos DetDocImp = new DocumentoImpuestos();
                	DetDocImp.setImpuestoCodigo(Integer.parseInt(listCodigoImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoCodigoPorcentaje(Integer.parseInt(listCodigoPorcentajeImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoTarifa(Double.parseDouble(listTarifaImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoBaseImponible(Double.parseDouble(listBaseImponibleImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoValor(Double.parseDouble(listValorImpuesto.get(j).toString()));
                	listDetImpuestosDocumentos.add(DetDocImp);
                }
                detDoc.setListDetImpuestosDocumentos(listDetImpuestosDocumentos);
                listDetDocumentos.add(detDoc);
            }                       
            emite.getInfEmisor().setListDetDocumentos(listDetDocumentos);
            int indexCliente = 0;
            HashMap<String, String> infoAdicionalHash = new HashMap<String, String>();
            expr = xpath.compile("//campoAdicional/text()");
            List<String> listInfoAdicionalFacturaValue = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	if(nodes.item(i).getNodeValue() != null)
            		listInfoAdicionalFacturaValue.add(nodes.item(i).getNodeValue().trim());
            }
            
            expr = xpath.compile("//campoAdicional/@nombre");
            List<String> listInfoAdicionalFacturaName = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	if(nodes.item(i).getNodeValue() != null)
            		listInfoAdicionalFacturaName.add(nodes.item(i).getNodeValue().trim());
            }
            ArrayList<InformacionAdicional> ListInfAdicional = new ArrayList<InformacionAdicional>();
            ListInfAdicional.clear();
            //ArrayList<InfoAdicional> listInfoAdicional = new ArrayList<InfoAdicional>();
            for (int i=0; i<listInfoAdicionalFacturaValue.size(); i++){
            	//infoAdicionalHash.put(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("CLIENTE")){
            		emite.getInfEmisor().setCodCliente(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("DIRECCION")){
            		emite.getInfEmisor().setDireccion(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("EMAIL")){
            		emite.getInfEmisor().setEmailCliente(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("TELEFONO")){
            		emite.getInfEmisor().setTelefono(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("LOCAL")){
            		emite.getInfEmisor().setLocal(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("CAJA")){
            		emite.getInfEmisor().setCaja(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("MOVIMIENTO")){
            		emite.getInfEmisor().setIdMovimiento(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	InformacionAdicional info = new InformacionAdicional(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	ListInfAdicional.add(info);
            } 
            emite.getInfEmisor().setListInfAdicional(ListInfAdicional);
            System.out.println("-- FIN ServiceDataHilo.leerFacturaXml --");
        } catch (Exception e) {
        	System.out.println(e);
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        	System.out.println(e.getLocalizedMessage());
        	System.out.println(e.getStackTrace());
        	e.printStackTrace();
        }
    }
	
	//Lectura de Nota de Credito
	public static void leerNotaCreditoXml(String nameXml, Emisor emite)
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        try
        {
        	System.out.println("-- INCICIO ServiceDataHilo.leerNotaCreditoXml --");
            builder = factory.newDocumentBuilder();
            doc = builder.parse(nameXml);
            String ls_documento = "notaCredito";
            String ls_tipoDocumento = "infoNotaCredito";
            XPathFactory xpathFactory = XPathFactory.newInstance();

            // Create XPath object
            XPath xpath = xpathFactory.newXPath();            
            //infoTributaria            
            XPathExpression expr = xpath.compile("/"+ls_documento+"/infoTributaria/tipoEmision/text()");
            emite.getInfEmisor().setTipoEmision((String) expr.evaluate(doc, XPathConstants.STRING));           
            
            //if (emite.getInfEmisor().getTipoEmision().equals("2")){
            	expr = xpath.compile("/"+ls_documento+"/infoTributaria/claveAcceso/text()");
                emite.getInfEmisor().setClaveAcceso((String) expr.evaluate(doc, XPathConstants.STRING));
            //}
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ambiente/text()");
            emite.getInfEmisor().setAmbiente(Integer.parseInt((String)expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ruc/text()");
            emite.getInfEmisor().setRuc((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/razonSocial/text()");
            emite.getInfEmisor().setRazonSocial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/nombreComercial/text()");
            emite.getInfEmisor().setNombreComercial((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/codDoc/text()");
            emite.getInfEmisor().setCodDocumento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/estab/text()");
            emite.getInfEmisor().setCodEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ptoEmi/text()");
            emite.getInfEmisor().setCodPuntoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/secuencial/text()");
            emite.getInfEmisor().setSecuencial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/dirMatriz/text()");
            emite.getInfEmisor().setDireccionMatriz((String) expr.evaluate(doc, XPathConstants.STRING));                        
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/fechaEmision/text()");
            emite.getInfEmisor().setFecEmision((String)expr.evaluate(doc, XPathConstants.STRING));
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/dirEstablecimiento/text()");
            emite.getInfEmisor().setDireccionEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
            /*
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/contribuyenteEspecial/text()");
            System.out.println("Contrib Especial::"+(String) expr.evaluate(doc, XPathConstants.STRING));
            //emite.getInfEmisor().setContribEspecial();
            */
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/obligadoContabilidad/text()");
            emite.getInfEmisor().setObligContabilidad((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/tipoIdentificacionComprador/text()");
            emite.getInfEmisor().setTipoIdentificacion((String) expr.evaluate(doc, XPathConstants.STRING));
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/razonSocialComprador/text()");
            emite.getInfEmisor().setRazonSocialComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/identificacionComprador/text()");
            emite.getInfEmisor().setIdentificacionComp((String) expr.evaluate(doc, XPathConstants.STRING));
                        
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/rise/text()");
            emite.getInfEmisor().setRise((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/codDocModificado/text()");
            emite.getInfEmisor().setCodDocModificado((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/numDocModificado/text()");
            emite.getInfEmisor().setNumDocModificado((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/fechaEmisionDocSustento/text()");
            emite.getInfEmisor().setFecEmisionDoc((String) expr.evaluate(doc, XPathConstants.STRING));
            
			
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/motivo/text()");
            emite.getInfEmisor().setMotivo((String) expr.evaluate(doc, XPathConstants.STRING));            
			            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalSinImpuestos/text()");
            emite.getInfEmisor().setTotalSinImpuestos(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));           
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/valorModificacion/text()");
            emite.getInfEmisor().setValorModificado(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/moneda/text()");
            emite.getInfEmisor().setMoneda((String) expr.evaluate(doc, XPathConstants.STRING));
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/codigo/text()");
            List<String> listCodigo = new ArrayList();            
            NodeList nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigo.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/codigoPorcentaje/text()");
            List<String> listCodigoPorcentaje = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigoPorcentaje.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/baseImponible/text()");
            List<String> listBaseImponible = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listBaseImponible.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/valor/text()");
            List<String> listValor = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listValor.add(nodes.item(i).getNodeValue());
            }
            ArrayList<DetalleTotalImpuestos> listDetDetImpuestos = new ArrayList<DetalleTotalImpuestos>();             
            for (int i=0; i<listCodigo.size(); i++){
            	DetalleTotalImpuestos detImp = new DetalleTotalImpuestos();
            	detImp.setCodTotalImpuestos(Integer.parseInt(listCodigo.get(i).toString()));
            	detImp.setCodPorcentImp(Integer.parseInt(listCodigoPorcentaje.get(i).toString()));
            	detImp.setBaseImponibleImp(Double.parseDouble(listBaseImponible.get(i).toString()));
            	detImp.setValorImp(Double.parseDouble(listValor.get(i).toString()));
            	listDetDetImpuestos.add(detImp);
            }
            
            emite.getInfEmisor().setListDetDetImpuestos(listDetDetImpuestos);
            
            ArrayList<DetalleDocumento> listDetDocumentos = new ArrayList<DetalleDocumento>();
                                               
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/codigoInterno/text()");
            List<String> listCodPrin = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int l=0; l<nodes.getLength(); l++){
            	listCodPrin.add(nodes.item(l).getNodeValue());
            	//System.out.println("index::"+i);
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/codigoAdicional/text()");
            List<String> listCodAux = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodAux.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/descripcion/text()");
            List<String> listDescrip = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listDescrip.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/cantidad/text()");
            List<String> listCantidad = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCantidad.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/precioUnitario/text()");
            List<String> listPrecioUnitario = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listPrecioUnitario.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/descuento/text()");
            List<String> listDescuento = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listDescuento.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/precioTotalSinImpuesto/text()");
            List<String> listPrecioTotalSinImpuesto = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listPrecioTotalSinImpuesto.add(nodes.item(i).getNodeValue());
            }
            
            for (int i=0; i<listCodPrin.size(); i++){
            	ArrayList<DocumentoImpuestos> listDetImpuestosDocumentos = new ArrayList<DocumentoImpuestos>();            	
            	DetalleDocumento detDoc = new DetalleDocumento();            	
            	detDoc.setCodigoPrincipal(listCodPrin.get(i).toString());
            	if(listCodAux.size() > 0)
            		detDoc.setCodigoAuxiliar(listCodAux.get(i).toString());
            	detDoc.setDescripcion(listDescrip.get(i).toString());
            	detDoc.setCantidad(Double.parseDouble(listCantidad.get(i).toString()));
            	detDoc.setPrecioUnitario(Double.parseDouble(listPrecioUnitario.get(i).toString()));
            	if(listDescuento.size() > 0)
            		detDoc.setDescuento(Double.parseDouble(listDescuento.get(i).toString()));
            	detDoc.setPrecioTotalSinImpuesto(Double.parseDouble(listPrecioTotalSinImpuesto.get(i).toString()));
            	//detDoc.setListDetImpuestosDocumentos();
            	
            	expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/codigo/text()");
                List<String> listCodigoImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/codigoPorcentaje/text()");
                List<String> listCodigoPorcentajeImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoPorcentajeImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/tarifa/text()");
                List<String> listTarifaImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listTarifaImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/baseImponible/text()");
                List<String> listBaseImponibleImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listBaseImponibleImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/valor/text()");
                List<String> listValorImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listValorImpuesto.add(nodes.item(j).getNodeValue());
                }
                                
                for (int j=0; j<listCodigoImpuesto.size(); j++){
                	DocumentoImpuestos DetDocImp = new DocumentoImpuestos();
                	DetDocImp.setImpuestoCodigo(Integer.parseInt(listCodigoImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoCodigoPorcentaje(Integer.parseInt(listCodigoPorcentajeImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoTarifa(Double.parseDouble(listTarifaImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoBaseImponible(Double.parseDouble(listBaseImponibleImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoValor(Double.parseDouble(listValorImpuesto.get(j).toString()));
                	listDetImpuestosDocumentos.add(DetDocImp);
                }
                detDoc.setListDetImpuestosDocumentos(listDetImpuestosDocumentos);
                listDetDocumentos.add(detDoc);
            }                       
            emite.getInfEmisor().setListDetDocumentos(listDetDocumentos);
            
            HashMap<String, String> infoAdicionalHash = new HashMap<String, String>(); 
            expr = xpath.compile("//campoAdicional/text()");
            List<String> listInfoAdicionalFacturaValue = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){            	
            	if(nodes.item(i).getNodeValue() != null)
            		listInfoAdicionalFacturaValue.add(nodes.item(i).getNodeValue().trim());
            }
            
            expr = xpath.compile("//campoAdicional/@nombre");
            List<String> listInfoAdicionalFacturaName = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	if(nodes.item(i).getNodeValue() != null)
            		listInfoAdicionalFacturaName.add(nodes.item(i).getNodeValue().trim());
            }
            ArrayList<InformacionAdicional> ListInfAdicional = new ArrayList<InformacionAdicional>();
            ListInfAdicional.clear();
            //ArrayList<InfoAdicional> listInfoAdicional = new ArrayList<InfoAdicional>();
            for (int i=0; i<listInfoAdicionalFacturaValue.size(); i++){
            	//infoAdicionalHash.put(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("CLIENTE")){
            		emite.getInfEmisor().setCodCliente(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("DIRECCION")){
            		emite.getInfEmisor().setDireccion(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("EMAIL")){
            		emite.getInfEmisor().setEmailCliente(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("TELEFONO")){
            		emite.getInfEmisor().setTelefono(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("LOCAL")){
            		emite.getInfEmisor().setLocal(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("CAJA")){
            		emite.getInfEmisor().setCaja(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("MOVIMIENTO")){
            		emite.getInfEmisor().setIdMovimiento(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	InformacionAdicional info = new InformacionAdicional(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	ListInfAdicional.add(info);
            } 
            emite.getInfEmisor().setListInfAdicional(ListInfAdicional);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        	System.out.println(e.getLocalizedMessage());
        	System.out.println(e.getStackTrace());
        	e.printStackTrace();
        }
        System.out.println("-- FIN ServiceDataHilo.leerNotaCreditoXml --");
	}
	
	//Lectura de Nota de Debito
	public static void leerNotaDebitoXml(String nameXml, Emisor emite)
	{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        try
        {
        	System.out.println("-- INCICIO ServiceDataHilo.leerNotaDebitoXml --");
            builder = factory.newDocumentBuilder();
            doc = builder.parse(nameXml);
            String ls_documento = "notaDebito";
            String ls_tipoDocumento = "infoNotaDebito";
            // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();
            
            // Create XPath object
            XPath xpath = xpathFactory.newXPath();     
            //infoTributaria
            XPathExpression expr = xpath.compile("/"+ls_documento+"/infoTributaria/tipoEmision/text()");
            emite.getInfEmisor().setTipoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            //if (emite.getInfEmisor().getTipoEmision().equals("2")){
            	expr = xpath.compile("/"+ls_documento+"/infoTributaria/claveAcceso/text()");
                emite.getInfEmisor().setClaveAcceso((String) expr.evaluate(doc, XPathConstants.STRING));
            //}
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ambiente/text()");
            emite.getInfEmisor().setAmbiente(Integer.parseInt((String)expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ruc/text()");
            emite.getInfEmisor().setRuc((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/razonSocial/text()");
            emite.getInfEmisor().setRazonSocial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/nombreComercial/text()");
            emite.getInfEmisor().setNombreComercial((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/codDoc/text()");
            emite.getInfEmisor().setCodDocumento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/estab/text()");
            emite.getInfEmisor().setCodEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ptoEmi/text()");
            emite.getInfEmisor().setCodPuntoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/secuencial/text()");
            emite.getInfEmisor().setSecuencial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/dirMatriz/text()");
            emite.getInfEmisor().setDireccionMatriz((String) expr.evaluate(doc, XPathConstants.STRING));                        
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/fechaEmision/text()");
            emite.getInfEmisor().setFecEmision((String)expr.evaluate(doc, XPathConstants.STRING));
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/dirEstablecimiento/text()");
            emite.getInfEmisor().setDireccionEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
            /*
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/contribuyenteEspecial/text()");
            System.out.println("Contrib Especial::"+(String) expr.evaluate(doc, XPathConstants.STRING));
            //emite.getInfEmisor().setContribEspecial();
            */
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/obligadoContabilidad/text()");
            emite.getInfEmisor().setObligContabilidad((String) expr.evaluate(doc, XPathConstants.STRING));
            
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/tipoIdentificacionComprador/text()");
            emite.getInfEmisor().setIdentificacionComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/razonSocialComprador/text()");
            emite.getInfEmisor().setRazonSocialComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/identificacionComprador/text()");
            emite.getInfEmisor().setIdentificacionComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/rise/text()");
            emite.getInfEmisor().setRise((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/codDocModificado/text()");
            emite.getInfEmisor().setCodDocModificado((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/numDocModificado/text()");
            emite.getInfEmisor().setNumDocModificado((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/fechaEmisionDocSustento/text()");
            emite.getInfEmisor().setFecEmisionDoc((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalSinImpuestos/text()");
            emite.getInfEmisor().setTotalSinImpuestos(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));           
            
            //Ojo JZURITA
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/valorTotal/text()");
            emite.getInfEmisor().setValorModificado(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/"+ls_documento+"/infoNotaDebito/valorTotal/text()");
            emite.getInfEmisor().setImporteTotal(Double.parseDouble((String) expr.evaluate(doc, XPathConstants.STRING)));
            
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/codigo/text()");
            List<String> listCodigo = new ArrayList();
            NodeList nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigo.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/tarifa/text()");
            List<String> listTarifa = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigo.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/codigoPorcentaje/text()");
            List<String> listCodigoPorcentaje = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigoPorcentaje.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/baseImponible/text()");
            List<String> listBaseImponible = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listBaseImponible.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_tipoDocumento+"/totalConImpuestos/totalImpuesto[*]/valor/text()");
            List<String> listValor = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listValor.add(nodes.item(i).getNodeValue());
            }
            ArrayList<DetalleTotalImpuestos> listDetDetImpuestos = new ArrayList<DetalleTotalImpuestos>();
            for (int i=0; i<listCodigo.size(); i++){
            	DetalleTotalImpuestos detImp = new DetalleTotalImpuestos();
            	detImp.setCodTotalImpuestos(Integer.parseInt(listCodigo.get(i).toString()));
            	detImp.setCodPorcentImp(Integer.parseInt(listCodigoPorcentaje.get(i).toString()));
            	//listTarifa
            	detImp.setTarifaImp(Double.parseDouble(listTarifa.get(i).toString()));
            	detImp.setBaseImponibleImp(Double.parseDouble(listBaseImponible.get(i).toString()));
            	detImp.setValorImp(Double.parseDouble(listValor.get(i).toString()));
            	listDetDetImpuestos.add(detImp);
            }
            
            emite.getInfEmisor().setListDetDetImpuestos(listDetDetImpuestos);
            
            ArrayList<DetalleDocumento> listDetDocumentos = new ArrayList<DetalleDocumento>();
                                               
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/codigoInterno/text()");
            List<String> listCodPrin = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int l=0; l<nodes.getLength(); l++){
            	listCodPrin.add(nodes.item(l).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/codigoAdicional/text()");
            List<String> listCodAux = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listCodAux.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/descripcion/text()");
            List<String> listDescrip = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listDescrip.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/cantidad/text()");
            List<String> listCantidad = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCantidad.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/precioUnitario/text()");
            List<String> listPrecioUnitario = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listPrecioUnitario.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/descuento/text()");
            List<String> listDescuento = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listDescuento.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/detalles/detalle[*]/precioTotalSinImpuesto/text()");
            List<String> listPrecioTotalSinImpuesto = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listPrecioTotalSinImpuesto.add(nodes.item(i).getNodeValue());
            }
            
            for (int i=0; i<listCodPrin.size(); i++){
            	ArrayList<DocumentoImpuestos> listDetImpuestosDocumentos = new ArrayList<DocumentoImpuestos>();   	
            	DetalleDocumento detDoc = new DetalleDocumento();
            	detDoc.setCodigoPrincipal(listCodPrin.get(i).toString());
            	//detDoc.setCodigoAuxiliar(listCodAux.get(i).toString());
            	detDoc.setDescripcion(listDescrip.get(i).toString());
            	detDoc.setCantidad(Double.parseDouble(listCantidad.get(i).toString()));
            	detDoc.setPrecioUnitario(Double.parseDouble(listPrecioUnitario.get(i).toString()));
            	detDoc.setDescuento(Double.parseDouble(listDescuento.get(i).toString()));
            	detDoc.setPrecioTotalSinImpuesto(Double.parseDouble(listPrecioTotalSinImpuesto.get(i).toString()));
            	//detDoc.setListDetImpuestosDocumentos();
            	
            	expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/codigo/text()");
                List<String> listCodigoImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/codigoPorcentaje/text()");
                List<String> listCodigoPorcentajeImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoPorcentajeImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/tarifa/text()");
                List<String> listTarifaImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listTarifaImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/baseImponible/text()");
                List<String> listBaseImponibleImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listBaseImponibleImpuesto.add(nodes.item(j).getNodeValue());
                }
                
                expr = xpath.compile("/"+ls_documento+"/detalles/detalle[codigoPrincipal='"+listCodPrin.get(i).toString()+"']/impuestos/impuesto/valor/text()");
                List<String> listValorImpuesto = new ArrayList();            
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
                for (int j=0; j<nodes.getLength(); j++){
                	listValorImpuesto.add(nodes.item(j).getNodeValue());
                }
                                
                for (int j=0; j<listCodigoImpuesto.size(); j++){
                	DocumentoImpuestos DetDocImp = new DocumentoImpuestos();
                	DetDocImp.setImpuestoCodigo(Integer.parseInt(listCodigoImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoCodigoPorcentaje(Integer.parseInt(listCodigoPorcentajeImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoTarifa(Double.parseDouble(listTarifaImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoBaseImponible(Double.parseDouble(listBaseImponibleImpuesto.get(j).toString()));
                	DetDocImp.setImpuestoValor(Double.parseDouble(listValorImpuesto.get(j).toString()));
                	listDetImpuestosDocumentos.add(DetDocImp);
                }
                detDoc.setListDetImpuestosDocumentos(listDetImpuestosDocumentos);
                listDetDocumentos.add(detDoc);
            }                       
            emite.getInfEmisor().setListDetDocumentos(listDetDocumentos);
            
            HashMap<String, String> infoAdicionalHash = new HashMap<String, String>(); 
            expr = xpath.compile("//campoAdicional/text()");
            List<String> listInfoAdicionalFacturaValue = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){            	
            	if(nodes.item(i).getNodeValue() != null)
            		listInfoAdicionalFacturaValue.add(nodes.item(i).getNodeValue().trim());
            }
            
            expr = xpath.compile("//campoAdicional/@nombre");
            List<String> listInfoAdicionalFacturaName = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	if(nodes.item(i).getNodeValue() != null)
            		listInfoAdicionalFacturaName.add(nodes.item(i).getNodeValue().trim());
            }
            ArrayList<InformacionAdicional> ListInfAdicional = new ArrayList<InformacionAdicional>();
            ListInfAdicional.clear();
            //ArrayList<InfoAdicional> listInfoAdicional = new ArrayList<InfoAdicional>();
            for (int i=0; i<listInfoAdicionalFacturaValue.size(); i++){
            	//infoAdicionalHash.put(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("CLIENTE")){
            		emite.getInfEmisor().setCodCliente(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("DIRECCION")){
            		emite.getInfEmisor().setDireccion(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("EMAIL")){
            		emite.getInfEmisor().setEmailCliente(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("TELEFONO")){
            		emite.getInfEmisor().setTelefono(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("LOCAL")){
            		emite.getInfEmisor().setLocal(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("CAJA")){
            		emite.getInfEmisor().setCaja(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("MOVIMIENTO")){
            		emite.getInfEmisor().setIdMovimiento(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	InformacionAdicional info = new InformacionAdicional(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	ListInfAdicional.add(info);
            } 
            emite.getInfEmisor().setListInfAdicional(ListInfAdicional);
        } catch (Exception e) {
        	System.out.println(e);
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        	System.out.println(e.getLocalizedMessage());
        	System.out.println(e.getStackTrace());
        	e.printStackTrace();
        }
        System.out.println("-- FIN ServiceDataHilo.leerNotaDebitoXml --");
	}
	
	// INI HFU MOVIDO DESDE ServiceData
	public static void leerComprobanteRetXml(String nameXml, Emisor emite)
	{
		System.out.println("-- INICIO ServiceDataHilo.leerComprobanteRetXml --");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        String ls_documento = "comprobanteRetencion";
        String ls_infoDocumento = "infoCompRetencion";
        try
        {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(nameXml);
            // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();
 
            // Create XPath object
            XPath xpath = xpathFactory.newXPath();
            
            //infoTributaria            
            XPathExpression expr = xpath.compile("/"+ls_documento+"/infoTributaria/tipoEmision/text()");
            emite.getInfEmisor().setTipoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            //if (emite.getInfEmisor().getTipoEmision().equals("2")){
            	expr = xpath.compile("/"+ls_documento+"/infoTributaria/claveAcceso/text()");
                emite.getInfEmisor().setClaveAcceso((String) expr.evaluate(doc, XPathConstants.STRING));
            //}
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ambiente/text()");
            emite.getInfEmisor().setAmbiente(Integer.parseInt((String) expr.evaluate(doc, XPathConstants.STRING)));                      
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ruc/text()");
            emite.getInfEmisor().setRuc((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/razonSocial/text()");
            emite.getInfEmisor().setRazonSocial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/nombreComercial/text()");
            emite.getInfEmisor().setNombreComercial((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/codDoc/text()");
            emite.getInfEmisor().setCodDocumento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/estab/text()");
            emite.getInfEmisor().setCodEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ptoEmi/text()");
            emite.getInfEmisor().setCodPuntoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/secuencial/text()");
            emite.getInfEmisor().setSecuencial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/dirMatriz/text()");
            emite.getInfEmisor().setDireccionMatriz((String) expr.evaluate(doc, XPathConstants.STRING));                        
            
            //infoFactura
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/fechaEmision/text()");
            emite.getInfEmisor().setFecEmision((String)expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/dirEstablecimiento/text()");
            emite.getInfEmisor().setDireccionEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            /*
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/contribuyenteEspecial/text()");
            emite.getInfEmisor().setContribEspecial(Integer.parseInt((String) expr.evaluate(doc, XPathConstants.STRING)));
            */
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/obligadoContabilidad/text()");
            emite.getInfEmisor().setObligContabilidad((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/tipoIdentificacionSujetoRetenido/text()");
            emite.getInfEmisor().setTipoIdentificacion((String) expr.evaluate(doc, XPathConstants.STRING));
                       
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/razonSocialSujetoRetenido/text()");
            emite.getInfEmisor().setRazonSocialComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/identificacionSujetoRetenido/text()");
            emite.getInfEmisor().setIdentificacionComp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/periodoFiscal/text()");
            emite.getInfEmisor().setPeriodoFiscal((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/codigo/text()");
            List<String> listCodigo = new ArrayList();            
            NodeList nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigo.add(nodes.item(i).getNodeValue());            	
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/codigoRetencion/text()");
            List<String> listCodigoRetencion = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodigoRetencion.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/baseImponible/text()");
            List<String> listBaseImponible = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listBaseImponible.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/porcentajeRetener/text()");
            List<String> listPorcentajeRetener = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listPorcentajeRetener.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/valorRetenido/text()");
            List<String> listValorRetenido = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listValorRetenido.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/codDocSustento/text()");
            List<String> listCodDocSustento = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listCodDocSustento.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/numDocSustento/text()");
            List<String> listNumDocSustento = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listNumDocSustento.add(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/impuestos/impuesto[*]/fechaEmisionDocSustento/text()");
            List<String> listFechaEmisionDocSustento = new ArrayList();            
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listFechaEmisionDocSustento.add(nodes.item(i).getNodeValue());
            }
            double totalRetencion = 0;
            ArrayList<DetalleImpuestosRetenciones> listDetDetImpuestosRet = new ArrayList<DetalleImpuestosRetenciones>();             
            for (int i=0; i<listCodigo.size(); i++){
            	DetalleImpuestosRetenciones detImp = new DetalleImpuestosRetenciones();
            	detImp.setCodigo((listCodigo.get(i).toString()));
            	detImp.setCodigoRetencion((listCodigoRetencion.get(i).toString()));
            	detImp.setBaseImponible(Double.parseDouble(listBaseImponible.get(i).toString()));
            	detImp.setPorcentajeRetener(Integer.parseInt(listPorcentajeRetener.get(i).toString()));
            	totalRetencion = totalRetencion + Double.parseDouble(listValorRetenido.get(i).toString());
            	detImp.setValorRetenido(Double.parseDouble(listValorRetenido.get(i).toString()));
            	detImp.setCodDocSustento(listCodDocSustento.get(i).toString());
            	if (listNumDocSustento.size()>0){
            		detImp.setNumDocSustento(listNumDocSustento.get(i).toString());
            	}
            	if (listFechaEmisionDocSustento.size()>0){
            		detImp.setFechaEmisionDocSustento(listFechaEmisionDocSustento.get(i).toString());
            	}
            	listDetDetImpuestosRet.add(detImp);
            }
            
            emite.getInfEmisor().setImporteTotal(totalRetencion);
            
            emite.getInfEmisor().setListDetImpuestosRetenciones(listDetDetImpuestosRet);                        
            
            int indexCliente = 0;
            HashMap<String, String> infoAdicionalHash = new HashMap<String, String>(); 
            expr = xpath.compile("//campoAdicional/text()");
            List<String> listInfoAdicionalFacturaValue = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){            	
            	if(nodes.item(i).getNodeValue() != null)
            		listInfoAdicionalFacturaValue.add(nodes.item(i).getNodeValue().trim());
            }
            
            expr = xpath.compile("//campoAdicional/@nombre");
            List<String> listInfoAdicionalFacturaName = new ArrayList();                                    
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	if(nodes.item(i).getNodeValue() != null)
            		listInfoAdicionalFacturaName.add(nodes.item(i).getNodeValue().trim());
            }
            ArrayList<InformacionAdicional> ListInfAdicional = new ArrayList<InformacionAdicional>();
            ListInfAdicional.clear();
            //ArrayList<InfoAdicional> listInfoAdicional = new ArrayList<InfoAdicional>();
            for (int i=0; i<listInfoAdicionalFacturaValue.size(); i++){
            	//infoAdicionalHash.put(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("CLIENTE")){
            		emite.getInfEmisor().setCodCliente(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("DIRECCION")){
            		emite.getInfEmisor().setDireccion(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("EMAIL")){
            		emite.getInfEmisor().setEmailCliente(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("TELEFONO")){
            		emite.getInfEmisor().setTelefono(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("LOCAL")){
            		emite.getInfEmisor().setLocal(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("CAJA")){
            		emite.getInfEmisor().setCaja(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	if (listInfoAdicionalFacturaName.get(i).toString().toUpperCase().equals("MOVIMIENTO")){
            		emite.getInfEmisor().setIdMovimiento(listInfoAdicionalFacturaValue.get(i).toString());
            	}
            	InformacionAdicional info = new InformacionAdicional(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	ListInfAdicional.add(info);
            }
            emite.getInfEmisor().setListInfAdicional(ListInfAdicional);
        } catch (Exception e) {
        	System.out.println(e);
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        	System.out.println(e.getLocalizedMessage());
        	System.out.println(e.getStackTrace());
        	e.printStackTrace();
        }
        System.out.println("-- FIN ServiceDataHilo.leerComprobanteRetXml --");
     }
	
	//

	public static void leerGuiaRemisionXml(String nameXml, Emisor emite)
	{
		System.out.println("-- INICIO ServiceDataHilo.leerGuiaRemisionXml --");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        String ls_documento = "guiaRemision";
        String ls_infoDocumento = "infoGuiaRemision";
        try
        {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(nameXml);
            System.out.println("	File>>"+nameXml);
            // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();
            
            // Create XPath object
            XPath xpath = xpathFactory.newXPath();
            
            //infoTributaria            
            XPathExpression expr = xpath.compile("/"+ls_documento+"/infoTributaria/tipoEmision/text()");
            emite.getInfEmisor().setTipoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
			//if (emite.getInfEmisor().getTipoEmision().equals("2")){
            	expr = xpath.compile("/factura/infoTributaria/claveAcceso/text()");
                emite.getInfEmisor().setClaveAcceso((String) expr.evaluate(doc, XPathConstants.STRING));
            //}
			
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ambiente/text()");
            emite.getInfEmisor().setAmbiente(Integer.parseInt((String)expr.evaluate(doc, XPathConstants.STRING)));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ruc/text()");
            emite.getInfEmisor().setRuc((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/nombreComercial/text()");
            emite.getInfEmisor().setNombreComercial((String) expr.evaluate(doc, XPathConstants.STRING));
 
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/codDoc/text()");
            emite.getInfEmisor().setCodDocumento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/estab/text()");
            emite.getInfEmisor().setCodEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/ptoEmi/text()");
            emite.getInfEmisor().setCodPuntoEmision((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/secuencial/text()");
            emite.getInfEmisor().setSecuencial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/dirMatriz/text()");
            emite.getInfEmisor().setDireccionMatriz((String) expr.evaluate(doc, XPathConstants.STRING)); 
            
            expr = xpath.compile("/"+ls_documento+"/infoTributaria/razonSocial/text()");
            emite.getInfEmisor().setRazonSocial((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/dirEstablecimiento/text()");
            emite.getInfEmisor().setDireccionEstablecimiento((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/dirPartida/text()");
            emite.getInfEmisor().setDirPartida((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/razonSocialTransportista/text()");
            emite.getInfEmisor().setRazonSocTransp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/tipoIdentificacionTransportista/text()");
            emite.getInfEmisor().setTipoIdentTransp(Integer.parseInt(expr.evaluate(doc, XPathConstants.STRING).toString()));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/rucTransportista/text()");
            emite.getInfEmisor().setRucTransp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/obligadoContabilidad/text()");
            emite.getInfEmisor().setObligContabilidad((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/contribuyenteEspecial/text()");
            emite.getInfEmisor().setContribEspecial(Integer.parseInt(expr.evaluate(doc, XPathConstants.STRING).toString()));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/fechaIniTransporte/text()");
            emite.getInfEmisor().setFechaIniTransp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/fechaFinTransporte/text()");
            emite.getInfEmisor().setFechaFinTransp((String) expr.evaluate(doc, XPathConstants.STRING));
            
            expr = xpath.compile("/"+ls_documento+"/"+ls_infoDocumento+"/placa/text()");
            emite.getInfEmisor().setPlaca((String) expr.evaluate(doc, XPathConstants.STRING));
            
            ///////////////////////////////////////////////////////////////
            NodeList nodes;
            
            expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[*]/identificacionDestinatario/text()");
            List<String> listIdentificacionDestinatario = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listIdentificacionDestinatario.add(nodes.item(i).getNodeValue());
            	emite.getInfEmisor().setIdentificacionDestinatario(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[*]/razonSocialDestinatario/text()");
            List<String> listRazonSocialDestinatario = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));            
            for (int i=0; i<nodes.getLength(); i++){
            	listRazonSocialDestinatario.add(nodes.item(i).getNodeValue());
            	emite.getInfEmisor().setRazonSocialDestinatario(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[*]/dirDestinatario/text()");
            List<String> listDirDestinatario = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listDirDestinatario.add(nodes.item(i).getNodeValue());
            	emite.getInfEmisor().setDirDestinatario(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[*]/motivoTraslado/text()");
            List<String> listMotivoTraslado = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	listMotivoTraslado.add(nodes.item(i).getNodeValue());
            	emite.getInfEmisor().setMotTraslDestinatario(nodes.item(i).getNodeValue());
            }
            
            expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[*]/docAduaneroUnico/text()");
            List<String> listDocAduaneroUnico = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            if(nodes != null)
            	for (int i=0; i<nodes.getLength(); i++){
            		listDocAduaneroUnico.add(nodes.item(i).getNodeValue());
            		emite.getInfEmisor().setDocAduanero(nodes.item(i).getNodeValue());
            	}
            
            expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[*]/codEstabDestino/text()");
            List<String> listCodEstabDestino = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            if(nodes != null)
            	for (int i=0; i<nodes.getLength(); i++){
            		listCodEstabDestino.add(nodes.item(i).getNodeValue());
            		emite.getInfEmisor().setCodEstabDestino(nodes.item(i).getNodeValue());
            	}
            
            expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[*]/ruta/text()");
            List<String> listRuta = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            if(nodes != null)
            	for (int i=0; i<nodes.getLength(); i++){
            		listRuta.add(nodes.item(i).getNodeValue());
            		emite.getInfEmisor().setRutaDest(nodes.item(i).getNodeValue());
            	}
            
            expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[*]/codDocSustento/text()");
            List<String> listCodDocSustento = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            if(nodes != null)
            	for (int i=0; i<nodes.getLength(); i++){
            		listCodDocSustento.add(nodes.item(i).getNodeValue());
            		emite.getInfEmisor().setCodDocModificado(nodes.item(i).getNodeValue());
            	}     
            
            expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[*]/numDocSustento/text()");
            List<String> listNumDocSustento = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            if(nodes != null)
            	for (int i=0; i<nodes.getLength(); i++){
            		listNumDocSustento.add(nodes.item(i).getNodeValue());
            		emite.getInfEmisor().setNumDocSustentoDest(nodes.item(i).getNodeValue());
            	}
            
            expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[*]/numAutDocSustento/text()");
            List<String> listNumAutDocSustento = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            if(nodes != null)
            	for (int i=0; i<nodes.getLength(); i++){
            		listNumAutDocSustento.add(nodes.item(i).getNodeValue());
            		emite.getInfEmisor().setNumAutDocSustDest(nodes.item(i).getNodeValue());
            	}
            
            expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[*]/fechaEmisionDocSustento/text()");
            List<String> listFechaEmisionDocSustento = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            if(nodes != null)
            	for (int i=0; i<nodes.getLength(); i++){
            		listFechaEmisionDocSustento.add(nodes.item(i).getNodeValue());
            	}
            
            // DetalleDocumento
            ArrayList<Destinatarios> listDestinatarios = new ArrayList<Destinatarios>();
            for (int i=0; i<listIdentificacionDestinatario.size(); i++)
            {
            	// Detalles del Destinatario
            	expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[identificacionDestinatario='"+listIdentificacionDestinatario.get(i).toString()+"']/detalles/detalle/codigoInterno/text()");
                List<String> listCodigoInterno = new ArrayList();
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoInterno.add(nodes.item(j).getNodeValue());
                }
                expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[identificacionDestinatario='"+listIdentificacionDestinatario.get(i).toString()+"']/detalles/detalle/codigoAdicional/text()");
                List<String> listCodigoAdicional = new ArrayList();
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
                for (int j=0; j<nodes.getLength(); j++){
                	listCodigoAdicional.add(nodes.item(j).getNodeValue());
                }
                expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[identificacionDestinatario='"+listIdentificacionDestinatario.get(i).toString()+"']/detalles/detalle/descripcion/text()");
                List<String> listDescripcion = new ArrayList();
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
                for (int j=0; j<nodes.getLength(); j++){
                	listDescripcion.add(nodes.item(j).getNodeValue());
                }
                expr = xpath.compile("/"+ls_documento+"/destinatarios/destinatario[identificacionDestinatario='"+listIdentificacionDestinatario.get(i).toString()+"']/detalles/detalle/cantidad/text()");
                List<String> listCantidad = new ArrayList();
                nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
                for (int j=0; j<nodes.getLength(); j++){
                	listCantidad.add(nodes.item(j).getNodeValue());
                }
                // Lleno Destinatarios
                ArrayList<DetalleGuiaRemision> listDetallesDestinatario = new ArrayList();
                DetalleGuiaRemision detalleDestinatario;
                for(int j=0; j < listCodigoInterno.size(); j++)
                {
                	detalleDestinatario = new DetalleGuiaRemision();
                	detalleDestinatario.setAmbiente(emite.getInfEmisor().getAmbiente());
                	detalleDestinatario.setRuc(emite.getInfEmisor().getRuc());
                	detalleDestinatario.setCodEstablecimiento(emite.getInfEmisor().getCodEstablecimiento());
                	detalleDestinatario.setCodPuntEmision(emite.getInfEmisor().getCodPuntoEmision());
                	detalleDestinatario.setSecuencial(emite.getInfEmisor().getSecuencial());
                	detalleDestinatario.setCodigoInterno(listCodigoInterno.get(j).toString());
                	detalleDestinatario.setCodigoAdicional(listCodigoAdicional.get(j).toString());
                	detalleDestinatario.setDescripcion(listDescripcion.get(j).toString());
                	detalleDestinatario.setCantidad(Integer.parseInt(listCantidad.get(j).toString()));
                	detalleDestinatario.setIdentificacionDestinatario(listIdentificacionDestinatario.get(i).toString());
                	
                	listDetallesDestinatario.add(detalleDestinatario);
                }
                //
                Destinatarios destinatario = new Destinatarios();
                destinatario.setAmbiente(emite.getInfEmisor().getAmbiente());
                destinatario.setRuc(emite.getInfEmisor().getRuc());
                destinatario.setCodEstablecimiento(emite.getInfEmisor().getCodEstablecimiento());
                destinatario.setCodPuntEmision(emite.getInfEmisor().getCodPuntoEmision());
                destinatario.setSecuencial(emite.getInfEmisor().getSecuencial());
                destinatario.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
            	destinatario.setIdentificacionDestinatario(listIdentificacionDestinatario.get(i).toString());
            	destinatario.setRazonSocialDestinatario(listRazonSocialDestinatario.get(i).toString());
            	destinatario.setDireccionDestinatario(listDirDestinatario.get(i).toString());
            	destinatario.setMotivoTraslado(listMotivoTraslado.get(i).toString());
            	if(listDocAduaneroUnico.size()>0)
            		destinatario.setDocAduanero(listDocAduaneroUnico.get(i).toString());
            	if(listCodEstabDestino.size()>0)
            		destinatario.setCodEstabDestino(listCodEstabDestino.get(i));
            	if(listRuta.size()>0)
            		destinatario.setRutaDest(listRuta.get(i));
            	if(listCodDocSustento.size()>0)
            		destinatario.setCodDocSustentoDest(listCodDocSustento.get(i).toString());
            	if(listNumDocSustento.size()>0)
            		destinatario.setNumDocSustentoDest(listNumDocSustento.get(i).toString());
            	if(listNumAutDocSustento.size()>0)
            		destinatario.setNumAutDocSustDest(listNumAutDocSustento.get(i).toString());
            	if(listFechaEmisionDocSustento.size()>0)
            	{
            		java.util.Date temp = new SimpleDateFormat("dd/MM/yyyy").parse(listFechaEmisionDocSustento.get(i).toString());
            		destinatario.setFechEmisionDocSustDest(listFechaEmisionDocSustento.get(i).toString());
            	}
            	
            	destinatario.setListDetallesGuiaRemision(listDetallesDestinatario);
                listDestinatarios.add(destinatario);
            }
            emite.getInfEmisor().setListDetDestinatarios(listDestinatarios);
            ///////////////////////////////////////////////////////////////
            
            HashMap<String, String> infoAdicionalHash = new HashMap<String, String>(); 
            expr = xpath.compile("//campoAdicional/text()");
            List<String> listInfoAdicionalFacturaValue = new ArrayList();
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	if(nodes.item(i).getNodeValue() != null)
            		listInfoAdicionalFacturaValue.add(nodes.item(i).getNodeValue().trim());
            }
            
            expr = xpath.compile("//campoAdicional/@nombre");
            List<String> listInfoAdicionalFacturaName = new ArrayList();                   
            nodes =((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
            for (int i=0; i<nodes.getLength(); i++){
            	if(nodes.item(i).getNodeValue() != null)
            		listInfoAdicionalFacturaName.add(nodes.item(i).getNodeValue().trim());
            }
            
            ArrayList<InformacionAdicional> ListInfAdicional = new ArrayList<InformacionAdicional>();
            ListInfAdicional.clear();
            for (int i=0; i<listInfoAdicionalFacturaValue.size(); i++){
            	InformacionAdicional info = new InformacionAdicional(listInfoAdicionalFacturaName.get(i).toString(), listInfoAdicionalFacturaValue.get(i).toString());
            	ListInfAdicional.add(info);
            }
            emite.getInfEmisor().setListInfAdicional(ListInfAdicional);
            
        } catch (Exception e) {
        	System.out.println(e);
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        	System.out.println(e.getLocalizedMessage());
        	System.out.println(e.getStackTrace());
        	e.printStackTrace();
        }
        System.out.println("-- FIN ServiceDataHilo.leerGuiaRemisionXml --");
	}
	// FIN HFU
	
	 /*
     * Converts XMLGregorianCalendar to java.util.Date in Java
     */
    public static Date toDate(XMLGregorianCalendar calendar){
        if(calendar == null) {
            return null;
        }
        return calendar.toGregorianCalendar().getTime();
    }
	
	/*Preparacion de Documentos a PDF.*/	
	
	
	public static FacCabDocumento preparaCabDocumentoFac(com.sun.businessLogic.validate.Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String msg_error, String estado)
	{
		System.out.println("-- INICIO ServiceDataHilo.preparaCabDocumentoFac --");
		
		FacCabDocumento cabDoc = new FacCabDocumento();
//		emite.getInfEmisor().setMailEmpresa("e-pycca@pycca.com"); // HFU
		cabDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
		cabDoc.setRuc(ruc);
		cabDoc.setTipoIdentificacion(emite.getInfEmisor().getTipoIdentificacion());
		cabDoc.setIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		cabDoc.setCodEstablecimiento(codEst);
		cabDoc.setCodPuntEmision(codPtoEmi);
		cabDoc.setSecuencial(secuencial);
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());
		cabDoc.setGuiaRemision(emite.getInfEmisor().getGuiaRemision());		
		cabDoc.setRazonSocialComprador(emite.getInfEmisor().getRazonSocialComp());
		cabDoc.setDirEstablecimiento(emite.getInfEmisor().getDireccionEstablecimiento());
		//cabDoc.setIdentificacionComprador(emite.getInfEmisor().getTipoIdentificacion());
		cabDoc.setTotalSinImpuesto(emite.getInfEmisor().getTotalSinImpuestos());
		cabDoc.setTotalDescuento(emite.getInfEmisor().getTotalDescuento());
		cabDoc.setEmail(emite.getInfEmisor().getMailEmpresa());
		cabDoc.setPropina(emite.getInfEmisor().getPropina());
		cabDoc.setMoneda("0");
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		cabDoc.setObligadoContabilidad(emite.getInfEmisor().getObligContabilidad());
		
		//cabDoc.setCodCliente((emite.getInfEmisor().getCodCliente()));
		if(emite.getInfEmisor().getIdentificacionComp().length()>6)
			cabDoc.setCodCliente(emite.getInfEmisor().getIdentificacionComp().substring(emite.getInfEmisor().getIdentificacionComp().length()-6));
		else
			cabDoc.setCodCliente(emite.getInfEmisor().getIdentificacionComp());
		cabDoc.setDireccion(emite.getInfEmisor().getDireccion());
		cabDoc.setEmailCliente(emite.getInfEmisor().getEmailCliente());
		cabDoc.setTelefono(emite.getInfEmisor().getTelefono());
		
		String infoAdicional = "";
		if(emite.getInfEmisor().getListInfAdicional()!=null)
		{
			for (int i = 0; i<emite.getInfEmisor().getListInfAdicional().size(); i++)
			infoAdicional = infoAdicional + "/" + emite.getInfEmisor().getListInfAdicional().get(i).getName() + "-" +emite.getInfEmisor().getListInfAdicional().get(i).getValue(); 		
		}
		cabDoc.setInfoAdicional(infoAdicional);
		if (emite.getInfEmisor().getPeriodoFiscal()!=null)
		cabDoc.setPeriodoFiscal(emite.getInfEmisor().getPeriodoFiscal().toString());
		
		cabDoc.setRise(emite.getInfEmisor().getRise());
		cabDoc.setFechaInicioTransporte(emite.getInfEmisor().getFechaIniTransp());
		cabDoc.setFechaFinTransporte(emite.getInfEmisor().getFechaFinTransp());
		cabDoc.setPlaca(emite.getInfEmisor().getPlaca());
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());
		cabDoc.setMotivoRazon(emite.getInfEmisor().getMotivo());
		
		cabDoc.setClaveAcceso(emite.getInfEmisor().getClaveAcceso());
		cabDoc.setImporteTotal(emite.getInfEmisor().getImporteTotal());
		cabDoc.setTotalSinImpuesto(emite.getInfEmisor().getTotalSinImpuestos());
		cabDoc.setTotalDescuento(emite.getInfEmisor().getTotalDescuento());
		cabDoc.setEmail(emite.getInfEmisor().getMailEmpresa());
		cabDoc.setPropina(emite.getInfEmisor().getPropina());
		
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		cabDoc.setCodDocModificado(emite.getInfEmisor().getCodDocModificado());
		cabDoc.setNumDocModificado(emite.getInfEmisor().getNumDocModificado());
		cabDoc.setMotivoValor(emite.getInfEmisor().getMotivoValorND());
		
		cabDoc.setTipoEmision(emite.getInfEmisor().getTipoEmision());
		cabDoc.setListInfAdicional(emite.getInfEmisor().getListInfAdicional());
		
		cabDoc.setSubtotalNoIva(emite.getInfEmisor().getSubTotalNoSujeto());
		cabDoc.setTotalvalorICE(emite.getInfEmisor().getTotalICE());
		cabDoc.setIva12(emite.getInfEmisor().getTotalIva12());
		cabDoc.setIsActive("1");
		cabDoc.setESTADO_TRANSACCION(estado);
		cabDoc.setMSJ_ERROR(msg_error);
		ArrayList<DetalleTotalImpuestos> lisDetImp = emite.getInfEmisor().getListDetDetImpuestos();
		for ( DetalleTotalImpuestos det : lisDetImp){
			if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 2)){
				cabDoc.setSubtotal12(det.getBaseImponibleImp());
				cabDoc.setIva12(det.getValorImp());
			}
			if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 0)){
				//cabDoc.setSubtotal0(det.getValorImp());
				cabDoc.setSubtotal0(det.getBaseImponibleImp());
			}
			if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 6)){
				//cabDoc.setSubtotal0(det.getValorImp());
				cabDoc.setSubtotalNoIva(det.getBaseImponibleImp());
			}
		}
		
		
		if (emite.getInfEmisor().getListDetDocumentos().size()>0)
		{
			List<FacDetDocumento> detalles = new ArrayList<FacDetDocumento>();
			for (int i=0; i<emite.getInfEmisor().getListDetDocumentos().size();i++)
			{
				FacDetDocumento DetDoc = new FacDetDocumento();
				DetDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
				DetDoc.setRuc(ruc);
				DetDoc.setCodEstablecimiento(emite.getInfEmisor().getCodEstablecimiento());
				DetDoc.setCodPuntEmision(emite.getInfEmisor().getCodPuntoEmision());
				DetDoc.setSecuencial(emite.getInfEmisor().getSecuencial());
				DetDoc.setCodPrincipal(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoPrincipal());
				DetDoc.setCodAuxiliar(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoAuxiliar());
				DetDoc.setDescripcion(emite.getInfEmisor().getListDetDocumentos().get(i).getDescripcion());
				DetDoc.setCantidad(new Double(emite.getInfEmisor().getListDetDocumentos().get(i).getCantidad()).intValue());
				DetDoc.setPrecioUnitario(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioUnitario());
				DetDoc.setDescuento(emite.getInfEmisor().getListDetDocumentos().get(i).getDescuento());
				DetDoc.setPrecioTotalSinImpuesto(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioTotalSinImpuesto());
				int flagIce=0;
				if(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().size()>0){
					for (int j=0; j<emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().size();j++){
						if(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().get(j).getImpuestoCodigo()==3){
							DetDoc.setValorIce(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().get(j).getImpuestoValor());
							flagIce = 1;
						}
					}
				}
				
				if (flagIce==0)
					DetDoc.setValorIce(0);
				
				DetDoc.setSecuencialDetalle(emite.getInfEmisor().getListDetDocumentos().get(i).getLineaFactura());
				DetDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
				detalles.add(DetDoc);
			}
			cabDoc.setListDetalleDocumento(detalles);
		}
		cabDoc.setListInfAdicional(emite.getInfEmisor().getListInfAdicional());
		System.out.println("-- FIN ServiceDataHilo.preparaCabDocumentoFac --");
		return cabDoc;
	}

	//
	
	public static FacCabDocumento preparaCabDocumentoRet(com.sun.businessLogic.validate.Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String msg_error, String estado)
	{
		FacCabDocumento cabDoc = new FacCabDocumento();
		cabDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
		cabDoc.setRuc(ruc);
		cabDoc.setTipoIdentificacion((emite.getInfEmisor().getTipoIdentificacion()));
		
		cabDoc.setIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		cabDoc.setCodEstablecimiento(codEst);
		cabDoc.setCodPuntEmision(codPtoEmi);
		cabDoc.setSecuencial(secuencial);
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());
		cabDoc.setRazonSocialComprador(emite.getInfEmisor().getRazonSocialComp());
		cabDoc.setDirEstablecimiento(emite.getInfEmisor().getDireccionEstablecimiento());
		
		cabDoc.setEmail(emite.getInfEmisor().getMailEmpresa());
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		cabDoc.setObligadoContabilidad(emite.getInfEmisor().getObligContabilidad());
		//cabDoc.setCodCliente((emite.getInfEmisor().getCodCliente()));
		
		
		
		
		
		if(emite.getInfEmisor().getIdentificacionComp().length()>6)
			cabDoc.setCodCliente(emite.getInfEmisor().getIdentificacionComp().substring(emite.getInfEmisor().getIdentificacionComp().length()-6));
		else
			cabDoc.setCodCliente(emite.getInfEmisor().getIdentificacionComp());
		
		
		
		
		
		cabDoc.setDireccion(emite.getInfEmisor().getDireccion());
		cabDoc.setEmailCliente(emite.getInfEmisor().getEmailCliente());
		cabDoc.setTelefono(emite.getInfEmisor().getTelefono());
		String infoAdicional = "";
		if(emite.getInfEmisor().getListInfAdicional()!=null)
		{
			for (int i = 0; i<emite.getInfEmisor().getListInfAdicional().size(); i++)
			infoAdicional = infoAdicional + "/" + emite.getInfEmisor().getListInfAdicional().get(i).getName() + "-" +emite.getInfEmisor().getListInfAdicional().get(i).getValue(); 		
		}
		cabDoc.setInfoAdicional(infoAdicional);
		if (emite.getInfEmisor().getPeriodoFiscal()!=null)
		cabDoc.setPeriodoFiscal(emite.getInfEmisor().getPeriodoFiscal().toString());
		
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());
		cabDoc.setListImpuestosRetencion(emite.getInfEmisor().getListDetImpuestosRetenciones());
		double total = 0;
		for (DetalleImpuestosRetenciones impuestoRetencion : emite.getInfEmisor().getListDetImpuestosRetenciones()) {
			total = total + impuestoRetencion.getValorRetenido();
		}
		cabDoc.setImporteTotal(Double.valueOf(total));

		cabDoc.setClaveAcceso(emite.getInfEmisor().getClaveAcceso());
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		
		cabDoc.setCodDocModificado(emite.getInfEmisor().getCodDocModificado());
		cabDoc.setNumDocModificado(emite.getInfEmisor().getNumDocModificado());
		
		//cabDoc.setTipIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		cabDoc.setTipoEmision(emite.getInfEmisor().getTipoEmision());
		
		cabDoc.setIsActive("1");
		cabDoc.setESTADO_TRANSACCION(estado);
		cabDoc.setMSJ_ERROR(msg_error);
				
		return cabDoc;
	}
	
	
	public static FacCabDocumento preparaCabDocumentoCre(com.sun.businessLogic.validate.Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String msg_error, String estado)
	{
		System.out.println("-- INICIO ServiceDataHilo.preparaCabDocumentoCre --");
		FacCabDocumento cabDoc = new FacCabDocumento();
//		emite.getInfEmisor().setMailEmpresa("e-pycca@pycca.com"); // HFU
		cabDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
		cabDoc.setRuc(ruc);
		cabDoc.setTipoIdentificacion((emite.getInfEmisor().getTipoIdentificacion()));
		cabDoc.setCodEstablecimiento(codEst);
		cabDoc.setCodPuntEmision(codPtoEmi);
		cabDoc.setSecuencial(secuencial);
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());				
		cabDoc.setRazonSocialComprador(emite.getInfEmisor().getRazonSocialComp());
		cabDoc.setDirEstablecimiento(emite.getInfEmisor().getDireccionEstablecimiento());
		cabDoc.setCodDocSustento(emite.getInfEmisor().getCodDocModificado());
		cabDoc.setCodDocModificado(emite.getInfEmisor().getCodDocModificado());
		cabDoc.setNumDocSustento(emite.getInfEmisor().getNumDocModificado());
		cabDoc.setFecEmisionDocSustento(emite.getInfEmisor().getFecEmisionDoc());
		cabDoc.setMotivoRazon(emite.getInfEmisor().getMotivo());
		cabDoc.setNumDocModificado(emite.getInfEmisor().getNumDocModificado());
		
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		String strFecha = emite.getInfEmisor().getFecEmisionDoc();
		
		Date fechaDate = null;
		try {
			fechaDate = formato.parse(strFecha);
		} catch (Exception e) {
			System.out.println(e);
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        	System.out.println(e.getLocalizedMessage());
        	System.out.println(e.getStackTrace());
        	e.printStackTrace();
		}
		cabDoc.setFechaEmisionDocSustento(fechaDate);
		
		cabDoc.setIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		cabDoc.setEmail(emite.getInfEmisor().getMailEmpresa());
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		cabDoc.setObligadoContabilidad(emite.getInfEmisor().getObligContabilidad());
		
		if(emite.getInfEmisor().getIdentificacionComp().length()>6)
			cabDoc.setCodCliente(emite.getInfEmisor().getIdentificacionComp().substring(emite.getInfEmisor().getIdentificacionComp().length()-6));
		else
			cabDoc.setCodCliente(emite.getInfEmisor().getIdentificacionComp());
		//cabDoc.setCodCliente((emite.getInfEmisor().getCodCliente()));
		
		cabDoc.setDireccion(emite.getInfEmisor().getDireccion());
		cabDoc.setEmailCliente(emite.getInfEmisor().getEmailCliente());
		cabDoc.setTelefono(emite.getInfEmisor().getTelefono());
		String infoAdicional = "";
		
		if(emite.getInfEmisor().getListInfAdicional()!=null)
		{
			for (int i = 0; i<emite.getInfEmisor().getListInfAdicional().size(); i++)
			infoAdicional = infoAdicional + "/" + emite.getInfEmisor().getListInfAdicional().get(i).getName() + "-" +emite.getInfEmisor().getListInfAdicional().get(i).getValue(); 		
		}
		cabDoc.setInfoAdicional(infoAdicional);
		if (emite.getInfEmisor().getPeriodoFiscal()!=null)
		cabDoc.setPeriodoFiscal(emite.getInfEmisor().getPeriodoFiscal().toString());
		
		
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());
		cabDoc.setListImpuestosRetencion(emite.getInfEmisor().getListDetImpuestosRetenciones());
		
		cabDoc.setClaveAcceso(emite.getInfEmisor().getClaveAcceso());
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		
		//cabDoc.setTipIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		cabDoc.setTipoEmision(emite.getInfEmisor().getTipoEmision());
		
		cabDoc.setIsActive("1");
		cabDoc.setESTADO_TRANSACCION(estado);
		cabDoc.setMSJ_ERROR(msg_error);
		
		cabDoc.setTotalSinImpuesto(emite.getInfEmisor().getTotalSinImpuestos());
		cabDoc.setTotalDescuento(emite.getInfEmisor().getTotalDescuento());
		cabDoc.setPropina(emite.getInfEmisor().getPropina());

		ArrayList<DetalleTotalImpuestos> lisDetImp = emite.getInfEmisor().getListDetDetImpuestos();
		for ( DetalleTotalImpuestos det : lisDetImp){
			if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 2)){
				cabDoc.setSubtotal12(det.getBaseImponibleImp());
				cabDoc.setIva12(det.getValorImp());				
			}
			if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 0)){
				//cabDoc.setSubtotal0(det.getValorImp());
				cabDoc.setSubtotal0(det.getBaseImponibleImp());
			}
			if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 6)){
				//cabDoc.setSubtotal0(det.getValorImp());
				cabDoc.setSubtotalNoIva(det.getBaseImponibleImp());
			}
		}
		
		double total =  cabDoc.getSubtotal12()+
				   		cabDoc.getSubtotalNoIva()+
				   		cabDoc.getSubtotal0()+
				   		cabDoc.getIva12()+
				   		cabDoc.getTotalvalorICE();	
		cabDoc.setImporteTotal(total);
		/*
		cabDoc.setSubtotalNoIva(emite.geltInfEmisor().getSubTotalNoSujeto());
		cabDoc.setTotalvalorICE(emite.getInfEmisor().getTotalICE());
		cabDoc.setIva12(emite.getInfEmisor().getTotalIva12());
		*/
	
		if (emite.getInfEmisor().getListDetDocumentos().size()>0){
			List<FacDetDocumento> detalles = new ArrayList<FacDetDocumento>();
			for (int i=0; i<emite.getInfEmisor().getListDetDocumentos().size();i++){
				FacDetDocumento DetDoc = new FacDetDocumento();
				DetDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
				DetDoc.setRuc(ruc);
				DetDoc.setCodEstablecimiento(emite.getInfEmisor().getCodEstablecimiento());
				DetDoc.setCodPuntEmision(emite.getInfEmisor().getCodPuntoEmision());
				DetDoc.setSecuencial(emite.getInfEmisor().getSecuencial());
				DetDoc.setCodPrincipal(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoPrincipal());
				DetDoc.setCodAuxiliar(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoAuxiliar());
				DetDoc.setDescripcion(emite.getInfEmisor().getListDetDocumentos().get(i).getDescripcion());
				DetDoc.setCantidad(new Double(emite.getInfEmisor().getListDetDocumentos().get(i).getCantidad()).intValue());
				DetDoc.setPrecioUnitario(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioUnitario());
				DetDoc.setDescuento(emite.getInfEmisor().getListDetDocumentos().get(i).getDescuento());
				DetDoc.setPrecioTotalSinImpuesto(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioTotalSinImpuesto());
				int flagIce=0;
				if(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().size()>0){
					for (int j=0; j<emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().size();j++){
						if(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().get(j).getImpuestoCodigo()==3){
							DetDoc.setValorIce(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().get(j).getImpuestoValor());
							flagIce = 1;
						}
					}
				}
				if (flagIce==0)
					DetDoc.setValorIce(0);
				
				DetDoc.setSecuencialDetalle(emite.getInfEmisor().getListDetDocumentos().get(i).getLineaFactura());
				DetDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
				detalles.add(DetDoc);
			}
			cabDoc.setListDetalleDocumento(detalles);
		}
		System.out.println("-- FIN ServiceDataHilo.preparaCabDocumentoCre --");
		return cabDoc;
	}
	
	
	// INI HFU
	public static FacCabDocumento preparaCabDocumentoNotaDeb(com.sun.businessLogic.validate.Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String msg_error, String estado)
	{
		System.out.println("-- INICIO ServiceDataHilo.preparaCabDocumentoNotaDeb --");
		FacCabDocumento cabDoc = new FacCabDocumento();
//		emite.getInfEmisor().setMailEmpresa("e-pycca@pycca.com"); // HFU
		cabDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
		cabDoc.setRuc(ruc);
		cabDoc.setTipoIdentificacion((emite.getInfEmisor().getTipoIdentificacion()));
		cabDoc.setCodEstablecimiento(codEst);
		cabDoc.setCodPuntEmision(codPtoEmi);
		cabDoc.setSecuencial(secuencial);
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());				
		cabDoc.setRazonSocialComprador(emite.getInfEmisor().getRazonSocialComp());
		cabDoc.setDirEstablecimiento(emite.getInfEmisor().getDireccionEstablecimiento());
		cabDoc.setCodDocSustento(emite.getInfEmisor().getCodDocModificado());
		cabDoc.setCodDocModificado(emite.getInfEmisor().getCodDocModificado());
		cabDoc.setNumDocSustento(emite.getInfEmisor().getNumDocModificado());
		cabDoc.setFecEmisionDocSustento(emite.getInfEmisor().getFecEmisionDoc());
		cabDoc.setMotivoRazon(emite.getInfEmisor().getMotivo());
		cabDoc.setNumDocModificado(emite.getInfEmisor().getNumDocModificado());
		
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		String strFecha = emite.getInfEmisor().getFecEmisionDoc();
		
		Date fechaDate = null;
		try {
			fechaDate = formato.parse(strFecha);
		} catch (Exception e) {
			System.out.println(e);
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        	System.out.println(e.getLocalizedMessage());
        	System.out.println(e.getStackTrace());
        	e.printStackTrace();
		}
		cabDoc.setFechaEmisionDocSustento(fechaDate);
		
		cabDoc.setIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		cabDoc.setEmail(emite.getInfEmisor().getMailEmpresa());
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		cabDoc.setObligadoContabilidad(emite.getInfEmisor().getObligContabilidad());
		
		if(emite.getInfEmisor().getIdentificacionComp().length()>6)
			cabDoc.setCodCliente(emite.getInfEmisor().getIdentificacionComp().substring(emite.getInfEmisor().getIdentificacionComp().length()-6));
		else
			cabDoc.setCodCliente(emite.getInfEmisor().getIdentificacionComp());
		//cabDoc.setCodCliente((emite.getInfEmisor().getCodCliente()));
		
		cabDoc.setDireccion(emite.getInfEmisor().getDireccion());
		cabDoc.setEmailCliente(emite.getInfEmisor().getEmailCliente());
		cabDoc.setTelefono(emite.getInfEmisor().getTelefono());
		String infoAdicional = "";
		
		if(emite.getInfEmisor().getListInfAdicional()!=null)
		{
			for (int i = 0; i<emite.getInfEmisor().getListInfAdicional().size(); i++)
			infoAdicional = infoAdicional + "/" + emite.getInfEmisor().getListInfAdicional().get(i).getName() + "-" +emite.getInfEmisor().getListInfAdicional().get(i).getValue(); 		
		}
		cabDoc.setInfoAdicional(infoAdicional);
		if (emite.getInfEmisor().getPeriodoFiscal()!=null)
		cabDoc.setPeriodoFiscal(emite.getInfEmisor().getPeriodoFiscal().toString());
		
		
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());
		cabDoc.setListImpuestosRetencion(emite.getInfEmisor().getListDetImpuestosRetenciones());
		
		cabDoc.setClaveAcceso(emite.getInfEmisor().getClaveAcceso());
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		
		//cabDoc.setTipIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		cabDoc.setTipoEmision(emite.getInfEmisor().getTipoEmision());
		
		cabDoc.setIsActive("1");
		cabDoc.setESTADO_TRANSACCION(estado);
		cabDoc.setMSJ_ERROR(msg_error);
		
		cabDoc.setTotalSinImpuesto(emite.getInfEmisor().getTotalSinImpuestos());
		cabDoc.setImporteTotal(emite.getInfEmisor().getImporteTotal());
		
		cabDoc.setTotalDescuento(emite.getInfEmisor().getTotalDescuento());
		cabDoc.setPropina(emite.getInfEmisor().getPropina());
		
		ArrayList<DetalleTotalImpuestos> lisDetImp = emite.getInfEmisor().getListDetDetImpuestos();
		if(lisDetImp != null)
		{
			for ( DetalleTotalImpuestos det : lisDetImp){
				if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 2)){
					cabDoc.setSubtotal12(det.getBaseImponibleImp());
					cabDoc.setIva12(det.getValorImp());				
				}
				if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 0)){
					//cabDoc.setSubtotal0(det.getValorImp());
					cabDoc.setSubtotal0(det.getBaseImponibleImp());
				}
				if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 6)){
					//cabDoc.setSubtotal0(det.getValorImp());
					cabDoc.setSubtotalNoIva(det.getBaseImponibleImp());
				}
			}
		}
		
		
		double total =  cabDoc.getSubtotal12()+
				   		cabDoc.getSubtotalNoIva()+
				   		cabDoc.getSubtotal0()+
				   		cabDoc.getIva12()+
				   		cabDoc.getTotalvalorICE();	
		cabDoc.setImporteTotal(total);
		/*
		cabDoc.setSubtotalNoIva(emite.geltInfEmisor().getSubTotalNoSujeto());
		cabDoc.setTotalvalorICE(emite.getInfEmisor().getTotalICE());
		cabDoc.setIva12(emite.getInfEmisor().getTotalIva12());
		*/
	
		if (emite.getInfEmisor().getListDetDocumentos().size()>0){
			List<FacDetDocumento> detalles = new ArrayList<FacDetDocumento>();
			for (int i=0; i<emite.getInfEmisor().getListDetDocumentos().size();i++){
				FacDetDocumento DetDoc = new FacDetDocumento();
				DetDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
				DetDoc.setRuc(ruc);
				DetDoc.setCodEstablecimiento(emite.getInfEmisor().getCodEstablecimiento());
				DetDoc.setCodPuntEmision(emite.getInfEmisor().getCodPuntoEmision());
				DetDoc.setSecuencial(emite.getInfEmisor().getSecuencial());
				DetDoc.setCodPrincipal(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoPrincipal());
				DetDoc.setCodAuxiliar(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoAuxiliar());
				DetDoc.setDescripcion(emite.getInfEmisor().getListDetDocumentos().get(i).getDescripcion());
				DetDoc.setCantidad(new Double(emite.getInfEmisor().getListDetDocumentos().get(i).getCantidad()).intValue());
				DetDoc.setPrecioUnitario(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioUnitario());
				DetDoc.setDescuento(emite.getInfEmisor().getListDetDocumentos().get(i).getDescuento());
				DetDoc.setPrecioTotalSinImpuesto(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioTotalSinImpuesto());
				int flagIce=0;
				if(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().size()>0){
					for (int j=0; j<emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().size();j++){
						if(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().get(j).getImpuestoCodigo()==3){
							DetDoc.setValorIce(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().get(j).getImpuestoValor());
							flagIce = 1;
						}
					}
				}
				if (flagIce==0)
					DetDoc.setValorIce(0);
				
				DetDoc.setSecuencialDetalle(emite.getInfEmisor().getListDetDocumentos().get(i).getLineaFactura());
				DetDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
				detalles.add(DetDoc);
			}
			cabDoc.setListDetalleDocumento(detalles);
		}
		System.out.println("-- FIN ServiceDataHilo.preparaCabDocumentoNotaDeb --");
		return cabDoc;
	}
	
	
	
	public static FacCabDocumento preparaCabDocumentoGuiaRem(Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String msg_error, String estado)
	{
		System.out.println("-- INI ServiceDataHIlo.preparaCabDocumentoGuiaRem --");
		
		FacCabDocumento cabDoc = new FacCabDocumento();
		//emite.getInfEmisor().setMailEmpresa("e-pycca@pycca.com"); // HFU
		cabDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
		cabDoc.setRuc(ruc);
		cabDoc.setTipoIdentificacion((emite.getInfEmisor().getTipoIdentificacion()));
		cabDoc.setCodEstablecimiento(codEst);
		cabDoc.setCodPuntEmision(codPtoEmi);
		cabDoc.setSecuencial(secuencial);
		//cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());				
		
		//cabDoc.setIdentificacionComprador(emite.getInfEmisor().getTipoIdentificacion());		
		//cabDoc.setEmail(emite.getInfEmisor().getMailEmpresa());		
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		
		cabDoc.setClaveAcceso(emite.getInfEmisor().getClaveAcceso());
		cabDoc.setTipoEmision(emite.getInfEmisor().getTipoEmision());
		cabDoc.setDirEstablecimiento(emite.getInfEmisor().getDireccionEstablecimiento());
		cabDoc.setPartida(emite.getInfEmisor().getDirPartida());
		cabDoc.setRazonSocialComprador(emite.getInfEmisor().getRazonSocTransp());
		cabDoc.setTipIdentificacionComprador(String.valueOf(emite.getInfEmisor().getTipoIdentTransp()));
		cabDoc.setIdentificacionComprador(emite.getInfEmisor().getRucTransp());
		
		cabDoc.setIdentificacionDestinatario(emite.getInfEmisor().getIdentificacionDestinatario());
		cabDoc.setRazonSocialDestinatario(emite.getInfEmisor().getRazonSocialDestinatario());
		cabDoc.setDocAduaneroUnico(emite.getInfEmisor().getDocAduanero());
		cabDoc.setDireccionDestinatario(emite.getInfEmisor().getDirDestinatario());
		cabDoc.setMotivoTraslado(emite.getInfEmisor().getMotTraslDestinatario());
		cabDoc.setRuta(emite.getInfEmisor().getRutaDest());
		cabDoc.setCodEstablecimientoDest(emite.getInfEmisor().getCodEstabDestino());
		cabDoc.setCodDocSustento(emite.getInfEmisor().getCodDocSustentoDest());
		cabDoc.setNumAutDocSustento(emite.getInfEmisor().getNumAutDocSustDest());
		cabDoc.setNumDocSustento(emite.getInfEmisor().getNumDocSustentoDest());
		
		
		cabDoc.setObligadoContabilidad(emite.getInfEmisor().getObligContabilidad());
		cabDoc.setFechaInicioTransporte(emite.getInfEmisor().getFechaIniTransp());
		cabDoc.setFechaFinTransporte(emite.getInfEmisor().getFechaFinTransp());
		cabDoc.setPlaca(emite.getInfEmisor().getPlaca());
		
		cabDoc.setListInfAdicional(emite.getInfEmisor().getListInfAdicional());
		String infoAdicional = "";
		if(emite.getInfEmisor().getListInfAdicional()!=null)
		{
			for (int i = 0; i<emite.getInfEmisor().getListInfAdicional().size(); i++)
			infoAdicional = infoAdicional + "/" + emite.getInfEmisor().getListInfAdicional().get(i).getName() + "-" +emite.getInfEmisor().getListInfAdicional().get(i).getValue(); 		
		}
		cabDoc.setInfoAdicional(infoAdicional);
		
		cabDoc.setIsActive("1");
		cabDoc.setESTADO_TRANSACCION(estado);
		cabDoc.setMSJ_ERROR(msg_error);
		
		System.out.println("-- FIN ServiceDataHilo.preparaCabDocumentoGuiaRem --");
		return cabDoc;
	}
	// FIN HFU
	
	public static FacCabDocumento preparaCabDocumentoCreResp(com.sun.businessLogic.validate.Emisor emite, String ruc, String codEst, String codPtoEmi, String tipoDocumento, String secuencial, String msg_error, String estado)
	{
		System.out.println("-- INICIO ServiceDataHilo.preparaCabDocumentoCreResp --");
		FacCabDocumento cabDoc = new FacCabDocumento();
		//emite.getInfEmisor().setMailEmpresa("jzurita@cimait.com.ec");
		cabDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
		cabDoc.setRuc(ruc);
	
		cabDoc.setCodEstablecimiento(codEst);
		cabDoc.setCodPuntEmision(codPtoEmi);
		cabDoc.setSecuencial(secuencial);
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());				
		cabDoc.setRazonSocialComprador(emite.getInfEmisor().getRazonSocialComp());
		cabDoc.setDirEstablecimiento(emite.getInfEmisor().getDireccionEstablecimiento());
		
		cabDoc.setNumDocSustento(emite.getInfEmisor().getNumDocModificado());
		cabDoc.setFecEmisionDocSustento(emite.getInfEmisor().getFecEmisionDoc());
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		String strFecha = emite.getInfEmisor().getFecEmisionDoc();
		Date fechaDate = null;
		try {
			fechaDate = formato.parse(strFecha);
		} catch (Exception e) {
			System.out.println(e);
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        	System.out.println(e.getLocalizedMessage());
        	System.out.println(e.getStackTrace());
        	e.printStackTrace();			
		}
		cabDoc.setFechaEmisionDocSustento(fechaDate);
		
		cabDoc.setTipoIdentificacion(emite.getInfEmisor().getTipoIdentificacion());
		cabDoc.setIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		
		cabDoc.setEmail(emite.getInfEmisor().getMailEmpresa());		
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		cabDoc.setObligadoContabilidad(emite.getInfEmisor().getObligContabilidad());
		/*
		 "::Ruc::"+detalles.get(0).getRuc()+
    		 			"::CodEstablecimiento::"+ detalles.get(0).getCodEstablecimiento()+
    		 			"::PuntoEmision::"+ detalles.get(0).getCodPuntEmision()+
    		 			"::Secuencial::"+ detalles.get(0).getSecuencial()+
    		 			"::TipoDocumento::"+ detalles.get(0).getCodigoDocumento();
		 */
		
		if ((emite.getInfEmisor().getCodCliente()!=null)&&(emite.getInfEmisor().getCodCliente().length()>0))
			cabDoc.setCodCliente((emite.getInfEmisor().getCodCliente()));		
		
		cabDoc.setDireccion(emite.getInfEmisor().getDireccion());
		cabDoc.setEmailCliente(emite.getInfEmisor().getEmailCliente());
		cabDoc.setTelefono(emite.getInfEmisor().getTelefono());
		String infoAdicional = "";
		if(emite.getInfEmisor().getListInfAdicional()!=null)
		{
			for (int i = 0; i<emite.getInfEmisor().getListInfAdicional().size(); i++)
			infoAdicional = infoAdicional + "/" + emite.getInfEmisor().getListInfAdicional().get(i).getName() + "-" +emite.getInfEmisor().getListInfAdicional().get(i).getValue(); 		
		}
		cabDoc.setInfoAdicional(infoAdicional);
		if (emite.getInfEmisor().getPeriodoFiscal()!=null)
		cabDoc.setPeriodoFiscal(emite.getInfEmisor().getPeriodoFiscal().toString());
		
		
		cabDoc.setFechaEmision(emite.getInfEmisor().getFecEmision());
		cabDoc.setListImpuestosRetencion(emite.getInfEmisor().getListDetImpuestosRetenciones());
		
		cabDoc.setClaveAcceso(emite.getInfEmisor().getClaveAcceso());
		cabDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
		
		cabDoc.setCodDocModificado(emite.getInfEmisor().getCodDocModificado());
		cabDoc.setNumDocModificado(emite.getInfEmisor().getNumDocModificado());
		
		cabDoc.setFecEmisionDocSustento(emite.getInfEmisor().getFecEmisionDoc());
		
		//cabDoc.setTipIdentificacionComprador(emite.getInfEmisor().getIdentificacionComp());
		cabDoc.setTipoEmision(emite.getInfEmisor().getTipoEmision());
		
		cabDoc.setIsActive("1");
		cabDoc.setESTADO_TRANSACCION(estado);
		cabDoc.setMSJ_ERROR(msg_error);
		
		ArrayList<DetalleTotalImpuestos> lisDetImp = emite.getInfEmisor().getListDetDetImpuestos();
		for ( DetalleTotalImpuestos det : lisDetImp){
			if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 2)){
				cabDoc.setSubtotal12(det.getBaseImponibleImp());
				cabDoc.setIva12(det.getValorImp());				
			}
			if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 0)){
				//cabDoc.setSubtotal0(det.getValorImp());
				cabDoc.setSubtotal0(det.getBaseImponibleImp());
			}
			if ((det.getCodTotalImpuestos() == 2)&&(det.getCodPorcentImp() == 6)){
				//cabDoc.setSubtotal0(det.getValorImp());
				cabDoc.setSubtotalNoIva(det.getBaseImponibleImp());
			}
		}
		
		double total = cabDoc.getSubtotal12()+
				   	   cabDoc.getSubtotalNoIva()+
				   	   cabDoc.getSubtotal0()+
				   	   cabDoc.getIva12()+
				   	   cabDoc.getTotalvalorICE();	
	
		cabDoc.setSubtotalNoIva(emite.getInfEmisor().getSubTotalNoSujeto());
		cabDoc.setTotalvalorICE(emite.getInfEmisor().getTotalICE());
		cabDoc.setIva12(emite.getInfEmisor().getTotalIva12());
	
		if (emite.getInfEmisor().getListDetDocumentos().size()>0){
			List<FacDetDocumento> detalles = new ArrayList<FacDetDocumento>();
			for (int i=0; i<emite.getInfEmisor().getListDetDocumentos().size();i++){
				FacDetDocumento DetDoc = new FacDetDocumento();
				DetDoc.setAmbiente(emite.getInfEmisor().getAmbiente());
				DetDoc.setRuc(ruc);
				DetDoc.setCodEstablecimiento(emite.getInfEmisor().getCodEstablecimiento());
				DetDoc.setCodPuntEmision(emite.getInfEmisor().getCodPuntoEmision());
				DetDoc.setSecuencial(emite.getInfEmisor().getSecuencial());
				DetDoc.setCodPrincipal(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoPrincipal());
				DetDoc.setCodAuxiliar(emite.getInfEmisor().getListDetDocumentos().get(i).getCodigoAuxiliar());
				DetDoc.setDescripcion(emite.getInfEmisor().getListDetDocumentos().get(i).getDescripcion());
				DetDoc.setCantidad(new Double(emite.getInfEmisor().getListDetDocumentos().get(i).getCantidad()).intValue());
				DetDoc.setPrecioUnitario(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioUnitario());
				DetDoc.setDescuento(emite.getInfEmisor().getListDetDocumentos().get(i).getDescuento());
				DetDoc.setPrecioTotalSinImpuesto(emite.getInfEmisor().getListDetDocumentos().get(i).getPrecioTotalSinImpuesto());
				int flagIce=0;
				if(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().size()>0)
				{
					for (int j=0; j<emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().size();j++){
						if(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().get(j).getImpuestoCodigo()==3){
							DetDoc.setValorIce(emite.getInfEmisor().getListDetDocumentos().get(i).getListDetImpuestosDocumentos().get(j).getImpuestoValor());
							flagIce = 1;
						}
					}
				}	
				
				if (flagIce==0)
					DetDoc.setValorIce(0);
				
				DetDoc.setSecuencialDetalle(emite.getInfEmisor().getListDetDocumentos().get(i).getLineaFactura());
				DetDoc.setCodigoDocumento(emite.getInfEmisor().getCodDocumento());
				detalles.add(DetDoc);
			}
			cabDoc.setListDetalleDocumento(detalles);
		}
		System.out.println("-- FIN ServiceDataHilo.preparaCabDocumentoCreResp --");
		return cabDoc;
	}
	 
	
	@Override
	//Heredado de la Clase GenericTransaction
	public void run()
	{
		try{
			atiendeHilo();	
		}catch(Exception e){
			System.out.println(e);
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
        	System.out.println(e.getLocalizedMessage());
        	System.out.println(e.getStackTrace());
        	e.printStackTrace();
		}finally{
			//ServiceData.listAtencion.set(idHilo, "N");
		}
	}

	public int getIdHilo() {
		return idHilo;
	}

	public void setIdHilo(int idHilo) {
		this.idHilo = idHilo;
	}
	
	public InfoEmpresa getInfoEmp() {
		return infoEmp;
	}
	
	public void setInfoEmp(InfoEmpresa infoEmp) {
		this.infoEmp = infoEmp;
	}

	public Emisor getEmite() {
		return emite;
	}

	public void setEmite(Emisor emite) {
		this.emite = emite;
	}

	public File getFxml() {
		return fxml;
	}

	public void setFxml(File fxml) {
		this.fxml = fxml;
	}

	public File[] getContenido() {
		return contenido;
	}

	public void setContenido(File[] contenido) {
		this.contenido = contenido;
	}
	
}
