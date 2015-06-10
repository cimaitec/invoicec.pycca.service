package com.sun.directory.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.DAO.InformacionTributaria;
import com.sun.businessLogic.validate.Emisor;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import com.tradise.reportes.util.key.GenericTransaction;
import com.util.util.key.Environment;
import com.util.util.key.Util;
import com.tradise.reportes.entidades.FacCabDocumento;
import com.tradise.reportes.servicios.ReporteSentencias;

public class ServiceDataAutorizacion extends GenericTransaction
{
	ArrayList<Emisor> Listcontenido;
	Emisor emite = null;
	public static String databaseType=null;
	InfoEmpresa infEmp = new InfoEmpresa();
	String ruc ="";
	
	Date dateFechaAutorizacion = null;
	String respAutorizacion = "";
	int contadorAut = 0;
	int contadorNoAut = 0;
	String nameFile = "", file_adjunto = "";
	FacCabDocumento CabDoc=new FacCabDocumento();
	ReporteSentencias rpSen= new ReporteSentencias();
	
	String[] infoAutorizacion = new String[10];
	String nameXml ="";
	
	public ServiceDataAutorizacion(int hilo, 
						   		   InfoEmpresa infoEmpresa, 
								   Emisor emitir, 
								   String nivelLog,
								   int intentos,
								   int timeIntentos)
	{
		
		
	}
	
	
	public ServiceDataAutorizacion(String ruc)
	{
		this.ruc = ruc;
	}
	
	public void mostrar()
	{
		
	}
	
	public synchronized void atiendeHilo() throws Exception
	{
		emite = new Emisor();
		ServiceDataAutorizacion.databaseType = Util.driverConection;
		InformacionTributaria infTribAdic = new InformacionTributaria();
		
		if (!emite.existeEmpresa(ruc)){
	    	String mensaje = " Empresa no existe o no se encuentra Activa. Ruc->" +ruc+". Proceso de Emision de Documentos no se levanto.";
	    	int li_envio = ServiceDataHilo.enviaEmail("message_error", emite,  mensaje, mensaje,null, null);
	    	throw new Exception(mensaje);
	    }
		if (ServiceDataAutorizacion.databaseType.indexOf("postgresql")>0)
			ServiceDataAutorizacion.databaseType = "PostgreSQL";
		if (ServiceDataAutorizacion.databaseType.indexOf("sqlserver")>0)
			ServiceDataAutorizacion.databaseType = "SQLServer";
		infEmp = emite.obtieneInfoEmpresa(ruc);
		
		Thread.currentThread().sleep(60000);
		
		while ((Environment.cf.readCtrl().equals("S")))
		{
			if(Environment.c.getString("facElectronica.general.AUTORIZACION.activadoConsulta").equals("S"))
			{
				int limite = Integer.parseInt(Environment.c.getString("facElectronica.general.AUTORIZACION.limiteConsultaRecibidos"));
				int minutos = Integer.parseInt(Environment.c.getString("facElectronica.general.AUTORIZACION.minutosEntreConsultas"));
				
				Listcontenido = emite.getTrxRecibidos(ruc, "RS", minutos, limite);
				
				if (Listcontenido !=null)
				{
					if(Listcontenido.size()>0)
					{
						for (int i=0; i < Listcontenido.size(); i++)
						{
					    	//String respAutorizacion = "";
							respAutorizacion = "";
							//String[] infoAutorizacion = new String[10];
					    	infoAutorizacion = new String[10];
							try
							{
								nameXml = Listcontenido.get(i).getAmbiente() +
							  		      ruc +
							  		      Listcontenido.get(i).getCodigoDocumento()+
							  		      Listcontenido.get(i).getCodEstablecimiento()+
							  		      Listcontenido.get(i).getCodPuntoEmision()+
							  		      Listcontenido.get(i).getSecuencial()+".xml";
								
								Listcontenido.get(i).setFilexml(nameXml);
								
								CabDoc.setAmbiente(Listcontenido.get(i).getAmbiente());
								CabDoc.setRuc(ruc);
								CabDoc.setCodEstablecimiento(Listcontenido.get(i).getCodEstablecimiento());
								CabDoc.setCodPuntEmision(Listcontenido.get(i).getCodPuntoEmision());
								CabDoc.setSecuencial(Listcontenido.get(i).getSecuencial());
								CabDoc.setCodigoDocumento(Listcontenido.get(i).getCodigoDocumento());
								
								
								String claveAcceso ="";
								
								if(Listcontenido.get(i).getClaveAcceso() != null)
								{
									if(Listcontenido.get(i).getClaveAcceso().length() <= 0)
										claveAcceso = Listcontenido.get(i).getClaveContingencia();
									else
										claveAcceso = Listcontenido.get(i).getClaveAcceso();
								}
								
								respAutorizacion = com.sun.directory.examples.AutorizacionComprobantesWs.autorizarComprobanteIndividual(claveAcceso, //Listcontenido.get(i).getClaveAcceso(),
																			  														nameXml, 
																			  														String.valueOf(Listcontenido.get(i).getAmbiente()),
																			  														infEmp.getDirAutorizados(),
																			  														infEmp.getDirNoAutorizados(),
																			  														infEmp.getDirFirmados(),
																			  														3,
																			  														3000,
																			  														Listcontenido.get(i).getSecuencial());
								
								if (respAutorizacion.equals(""))
								{
									infoAutorizacion[0] = "SIN-RESPUESTA";
								} else {
									infoAutorizacion = respAutorizacion.split("\\|");
								}
								
								
								
								System.out.println(" - DataAutorizacion - ");
								System.out.println("  infoAutorizacion[0]-->"+infoAutorizacion[0]);
								if (infoAutorizacion[0].trim().equals("AUTORIZADO"))
								{
									documentoAutorizado(Listcontenido.get(i));
									
									
								}else if (infoAutorizacion[0].equals("NO AUTORIZADO"))
								{
									documentoNoAutorizado(Listcontenido.get(i));
									
									
								}
								else if(infoAutorizacion[0].equals("NO-EXISTE-DOCUMENTO"))
								{
									documentoNoExiste(Listcontenido.get(i));
									
								}
								else
								{
									rpSen.updateFechaUltimaConsultaDocumento(CabDoc);
								}
							
							}catch(Exception excep)
							{
								//infoAutorizacion = new String[1];
								infoAutorizacion[0] = new String("SIN-RESPUESTA");
								System.out.println("  DataAutorizacion --> Sin respuesta");
								
								System.out.println(excep);
					            System.out.println(excep.getMessage());
					            System.out.println(excep.getCause());
					        	System.out.println(excep.getLocalizedMessage());
					        	System.out.println(excep.getStackTrace());
								excep.printStackTrace();
							}
						
							if(infoAutorizacion.length > 2)
							{
								String numeroAutorizacion = infoAutorizacion[1];
								String fechaAutorizacion = infoAutorizacion[2];
								Listcontenido.get(i).setNumeroAutorizacion(numeroAutorizacion);
								Listcontenido.get(i).setFechaAutorizacion(fechaAutorizacion);
								
								try{
									XMLGregorianCalendar xmlFechaAutorizacion = XMLGregorianCalendarImpl.parse(fechaAutorizacion);
									dateFechaAutorizacion = ServiceDataHilo.toDate(xmlFechaAutorizacion);
									Listcontenido.get(i).setDateFechaAutorizacion(dateFechaAutorizacion);
								}catch(Exception e){
									Listcontenido.get(i).setDateFechaAutorizacion(null);
									System.out.println("Error en convertir a Date la fecha de Autorizacion::"+e.getMessage());
								}
							
								Listcontenido.get(i).setEstadoAutorizacion(infoAutorizacion[0]);
								if (infoAutorizacion[0].equals("AUTORIZADO")){
									contadorAut ++;
								}else{
									contadorNoAut ++;
								}
							}
													
							Thread.sleep(500);
						}
					}
				}
			}
			
			
			Thread.sleep(60000);
		}
    	
	}
	
	public void documentoAutorizado(Emisor emite)
	{

		String xmlString = "";
		String numeroAutorizacion = infoAutorizacion[1];
		String fechaAutorizacion = infoAutorizacion[2];
		XMLGregorianCalendar xmlFechaAutorizacion = XMLGregorianCalendarImpl.parse(fechaAutorizacion);
		Date dateFechaAutorizacion = ServiceDataHilo.toDate(xmlFechaAutorizacion);
		CabDoc.setAutorizacion(numeroAutorizacion);
		
		try
		{
			System.out.println(infEmp.getDirAutorizados() + emite.getFilexml());
			File verificaXml = new File(infEmp.getDirAutorizados() + emite.getFilexml());
			if (verificaXml.exists())
			{
				xmlString = ArchivoUtils.archivoToString(infEmp.getDirAutorizados() + emite.getFilexml());
			} else {
				System.out.println(infEmp.getDirFirmados() + emite.getFilexml());
				
				verificaXml = new File(infEmp.getDirFirmados() + emite.getFilexml());
				if (verificaXml.exists())
					xmlString = ArchivoUtils.archivoToString(infEmp.getDirFirmados() + emite.getFilexml());
			}
			
			System.out.println("  infoAutorizacion: "+infoAutorizacion);

		}
		catch (Exception e)
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
	
		String reportePdf="";
		try {
			reportePdf = com.tradise.reportes.reportes.ReporteUtil.generaPdfDocumentos(emite,
																					   emite.getInfEmisor().getRuc(),
																					   emite.getInfEmisor().getCodEstablecimiento(),
																					   emite.getInfEmisor().getCodPuntoEmision(),
																					   emite.getInfEmisor().getCodDocumento(),
																					   emite.getInfEmisor().getSecuencial(),
																					   infEmp.getPathReports(),
																					   infEmp.getDirFirmados(),
																					   nameXml.replace("xml", "pdf"));
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
			int li_envio = ServiceDataHilo.enviaEmailCliente("message_exito", emite, "", "", infEmp.getDirAutorizados() + emite.getFilexml(), reportePdf, CabDoc.getEmailCliente());
			System.out.println((li_envio >= 0) ? "Mail enviado Correctamente" : "Error en envio de Mail");
		}
		/*
		int li_envio = ServiceDataHilo.enviaEmailCliente("message_exito", emite, "", "", infoEmp.getDirAutorizados() + emite.getFilexml(), reportePdf, "");
		System.out.println((li_envio >= 0) ? "Mail enviado Correctamente" : "Error en envio de Mail");
		*/
		
		// Y AQUI ELIMINO LOS ARCHIVOS HFU
	    System.out.println("  Eliminacion de archivos Aut...");
	    ServiceDataHilo.delFile(infEmp.getDirAutorizados()+nameXml);
	    ServiceDataHilo.delFile(infEmp.getDirRecibidos()+nameXml);
	    ServiceDataHilo.delFile(infEmp.getDirRecibidos()+nameXml.replaceAll(".xml", "_backup.xml"));
	    ServiceDataHilo.delFile(infEmp.getDirRecibidos()+"reenviados/"+nameXml);
	    ServiceDataHilo.delFile(infEmp.getDirRecibidos()+"reenviados/"+nameXml.replaceAll(".xml", "_backup.xml"));
	    ServiceDataHilo.delFile(infEmp.getDirNoAutorizados()+nameXml);
	    ServiceDataHilo.delFile(infEmp.getDirFirmados()+nameXml);
	    ServiceDataHilo.delFile(infEmp.getDirFirmados()+nameXml.replaceAll(".xml", ".pdf"));
	    ServiceDataHilo.delFile(infEmp.getDirContingencias()+nameXml);
	    System.out.println("  Archivos eliminados Aut...");
	    
	
	}

	
	public void documentoNoAutorizado(Emisor emite)
	{

		ServiceDataHilo.delFile(emite,
								infEmp.getDirFirmados(),
								infEmp.getDirRecibidos(),
								infEmp.getDirNoAutorizados());
		
		String xmlStringNoAutorizado = ArchivoUtils.archivoToString(infEmp.getDirNoAutorizados() + nameXml);
		rpSen.updateEstadoDocumento("NA", respAutorizacion.replace("|", " -"), emite.getInfEmisor().getTipoEmision(), CabDoc, xmlStringNoAutorizado);
	
	}
	
	
	public void documentoNoExiste(Emisor emite)
	{

		System.out.println("  DataAutorizacion --> Paso a generados");
		File docRecibidoSinRespuesta = new File(infEmp.getDirRecibidos() + emite.getFilexml());
		if (docRecibidoSinRespuesta.exists())
		{
			ArchivoUtils.stringToArchivo(infEmp.getDirGenerado() + emite.getFilexml(),
						ArchivoUtils.archivoToString(infEmp.getDirRecibidos() + emite.getFilexml()));
			System.out.println("  DataAutorizacion --> Pasado a generados");
		}
		else
		{
			docRecibidoSinRespuesta = new File(infEmp.getDirRecibidos() + "reenviados/" + emite.getFilexml());
			if(docRecibidoSinRespuesta.exists())
			{
				ArchivoUtils.stringToArchivo(infEmp.getDirGenerado() + emite.getFilexml(),
   						ArchivoUtils.archivoToString(infEmp.getDirRecibidos() + emite.getFilexml()));
				System.out.println("  DataAutorizacion --> Pasado a generados");
			}
		}
	
	}
	
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


}
