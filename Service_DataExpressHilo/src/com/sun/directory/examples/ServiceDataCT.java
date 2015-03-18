package com.sun.directory.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import com.sun.DAO.ControlErrores;
import com.sun.DAO.InformacionTributaria;
import com.sun.businessLogic.validate.Emisor;
import com.sun.comprobantes.util.FormGenerales;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import com.tradise.reportes.util.key.GenericTransaction;
import com.util.util.key.Environment;
import com.util.util.key.Util;
import com.util.webServices.EnvioComprobantesWs;
import com.tradise.reportes.entidades.FacCabDocumento;
import com.tradise.reportes.servicios.ReporteSentencias;

public class ServiceDataCT extends GenericTransaction
{
	ArrayList<Emisor> Listcontenido;
	Emisor emite = null;
	public static String databaseType=null;
	InfoEmpresa infEmp = new InfoEmpresa();
	String ruc ="";
	
	Date dateFechaAutorizacion = null;
	int contadorAut = 0;
	int contadorNoAut = 0;
	String nameFile = "", file_adjunto = "";
	FacCabDocumento CabDoc=new FacCabDocumento();
	ReporteSentencias rpSen= new ReporteSentencias();
	
	private static org.apache.log4j.Logger logContingencia = null;
	
	public ServiceDataCT(int hilo, 
			   		   	 InfoEmpresa infoEmpresa, 
			   		   	 Emisor emitir, 
			   		   	 String nivelLog,
			   		   	 int intentos,
			   		   	 int timeIntentos)
	{
		
	}
	
	
	public ServiceDataCT(String ruc)
	{
		this.ruc = ruc;
	}
	
	
	public synchronized void atiendeHilo() throws Exception
	{
		emite = new Emisor();
		ServiceDataAutorizacion.databaseType = Util.driverConection;
		
		if (!emite.existeEmpresa(ruc)){
	    	String mensaje = " Empresa no existe o no se encuentra Activa. Ruc->" +ruc+". Proceso de Emision de Documentos no se levanto.";
	    	int li_envio = ServiceDataHilo.enviaEmail("message_error", emite,  mensaje, mensaje,null, null);
	    	throw new Exception(mensaje);
	    }
		if (ServiceDataAutorizacion.databaseType.indexOf("postgresql")>0)
			ServiceDataAutorizacion.databaseType = "PostgreSQL";
		if (ServiceDataAutorizacion.databaseType.indexOf("sqlserver")>0)
			ServiceDataAutorizacion.databaseType = "SQLServer";
		
		emite.setAmbiente(Environment.c.getString("facElectronica.alarm.email.ambiente").equals("PRUEBAS")?1:2);
		emite.getInfEmisor().setAmbiente(Environment.c.getString("facElectronica.alarm.email.ambiente").equals("PRUEBAS")?1:2);
		infEmp = emite.obtieneInfoEmpresa(ruc);
		
		Thread.currentThread().sleep(60000);
		
		logContingencia = Logger.getLogger("Thread" + Thread.currentThread().getName());
		logContingencia.debug("---- CONTINGENCIA ----");
		
		boolean flagServiceDisponible=false;
		
		while ((Environment.cf.readCtrl().equals("S")))
		{
			int limite = Integer.parseInt(Environment.c.getString("facElectronica.general.AUTORIZACION.limiteConsultaRecibidos"));
			int minutos = Integer.parseInt(Environment.c.getString("facElectronica.general.AUTORIZACION.minutosEntreConsultas"));

			Listcontenido = emite.getTrxRecibidos(ruc, "CT", minutos, limite);
			
			if (Listcontenido !=null)
			{
				if(Listcontenido.size()>0)
				{
					for (int i=0; i < Listcontenido.size(); i++)
					{
						try
						{
							String nameXml = Listcontenido.get(i).getAmbiente() +
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
							
							File fileFirmado = ArchivoUtils.stringToArchivo(infEmp.getDirContingencias()+"Testing.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ec=\"http://ec.gob.sri.ws.recepcion\">   <soapenv:Header/>   <soapenv:Body>      <ec:validarComprobante>         <xml>cid:22869388362</xml>      </ec:validarComprobante>   </soapenv:Body></soapenv:Envelope>");
							ec.gob.sri.comprobantes.ws.RespuestaSolicitud respuestaRecepcion = new ec.gob.sri.comprobantes.ws.RespuestaSolicitud();
							
							try
							{
								respuestaRecepcion = solicitudRecepcion(fileFirmado, emite, 2000);
			      	  			System.out.println("  Contingencia Respuesta recepcion estado-->"+respuestaRecepcion.getEstado());
			      	  			logContingencia.debug(">>PruebaEnvioSolicitudRecepcion-->"+respuestaRecepcion);
			      	  			if (respuestaRecepcion.getEstado().equals("DEVUELTA"))
			      	  				flagServiceDisponible = true;
			      	  		}catch(Exception e){
			      	  			flagServiceDisponible = false;
			      	  		}
							
							if (flagServiceDisponible)
			      	  		{
								System.out.println("  Contingencia --> Paso a generados");
								File docContingencia = new File(infEmp.getDirContingencias() + Listcontenido.get(i).getFilexml());
								if (docContingencia.exists())
								{
									ArchivoUtils.stringToArchivo(infEmp.getDirGenerado() + Listcontenido.get(i).getFilexml(),
					   						ArchivoUtils.archivoToString(infEmp.getDirContingencias() + Listcontenido.get(i).getFilexml()));
									System.out.println("  Contingencia --> Pasado a generados");
								}
			      	  		}
							else
								Thread.currentThread().sleep(30000);
						
						}catch(Exception excep)
						{
							System.out.println(" ");
							System.out.println("  DataCT --> Sin respuesta");
							excep.printStackTrace();  
						}
					
					}
				}
			}
    	
			Thread.sleep(60000);
		}
	}
	
	
	public static ec.gob.sri.comprobantes.ws.RespuestaSolicitud solicitudRecepcion(File archivoFirmado, 
																				   Emisor emi,
																				   int timeout)
	{
		ec.gob.sri.comprobantes.ws.RespuestaSolicitud respuestaRecepcion = null;
		try{
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
		return respuestaRecepcion;		
	}

	
	public void run()
	{
		try{
			atiendeHilo();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		}
	}


}
