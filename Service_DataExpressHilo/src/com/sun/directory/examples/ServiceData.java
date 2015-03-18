package com.sun.directory.examples;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.sun.DAO.InformacionTributaria;
import com.sun.businessLogic.validate.Emisor;
import com.sun.comprobantes.util.EmailSender;
import com.tradise.reportes.entidades.FacCabDocumento;
import com.tradise.reportes.servicios.ReporteSentencias;
import com.util.util.key.Environment;
import com.util.util.key.Util;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class ServiceData extends com.util.util.key.GenericTransaction
{
	public static String classReference = "ServiceData";
	public static StringBuilder SBmsj = null;
	public File fxml = null;	
	public static int contador;
	public static int numHilo;
	public static int sleepHilo;
	public static int sleepBloqueHilo;
	
	public static File[] contenido;
	public static List listErrores = null;
	public static List listWarning = null;
	public static List listErroresEstados = null;
	public static List listWarningEstados = null;
	
	public InfoEmpresa InforEmpresa = null;
	public static List listAtencion = null;
	public static String databaseMotor=null;
	public static String databaseType=null;
	private static org.apache.log4j.Logger logPrin = null;
	private static String nivelLog = "";
	
	public static void iniServiceData()
	{
        listErrores = Environment.c.getList("facElectronica.general.EMISION.error-wsdls.error-wsdl");		
		listWarning = Environment.c.getList("facElectronica.general.EMISION.warning-wsdls.warning-wsdl");
		listErroresEstados = Environment.c.getList("facElectronica.general.EMISION.error-wsdls.ESTADO");
		listWarningEstados = Environment.c.getList("facElectronica.general.EMISION.warning-wsdls.ESTADO");
		nivelLog = Environment.c.getString("facElectronica.general.EMISION.nivelLog");
	}
		
	public static void main( String args[] ) throws Exception
	{
		int intentos = 0;
		int timeIntentos = 0;
		Emisor emite = null;
		SBmsj = new StringBuilder();
	
		System.out.println("==============================================================================");
		System.out.println("Empieza mi programa...");
	
		Properties props=new Properties();
		props.setProperty("log4j.appender.file","org.apache.log4j.RollingFileAppender");
		props.setProperty("log4j.appender.file.maxFileSize","200MB");
		props.setProperty("log4j.appender.file.maxBackupIndex","200");
		props.setProperty("log4j.appender.file.File","Invioce.log");
		props.setProperty("log4j.appender.file.threshold","debug");
		props.setProperty("log4j.appender.file.layout","org.apache.log4j.PatternLayout");
		props.setProperty("log4j.appender.file.layout.ConversionPattern","%d -%m[%t]%-5p%-C[%-4L]%n");
		props.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
		//props.setProperty("log4j.appender.stdout.Target"|,"System.out");
		props.setProperty("log4j.logger."+"Thread" + Thread.currentThread().getName(),nivelLog+", file");
		// props.setProperty("log4j.logger.LoadHandler","DEBUG, file");
		PropertyConfigurator.configure(props);
		logPrin = Logger.getLogger("Thread" + Thread.currentThread().getName());
	
		//Archivo de Configuracion
		String name_xml="facturacion.xml";
		try{
			Environment.setConfiguration(name_xml);
			Environment.setCtrlFile();
			Environment.setLogger(Util.log_control);
			ServiceData.iniServiceData();
		}catch(Exception ex){
			SBmsj.append(classReference+"::main>>FacturacionElectronica.Service::main::Proceso de Carga de Archivo Xml Configuraciones::::"+". Proceso de Emision de Documentos no se levanto.");
			int li_envio = enviaEmail("message_error", emite, SBmsj.toString(),"",null, null);
			throw new Exception(SBmsj.toString());
	    }
		
		String ruc = args[0];
		if ((ruc == null)||ruc.equals("")||(ruc.length()<13))
		{
			SBmsj.append("Error::"+classReference+":: Debe enviar el parametro de Ruc Correcto. Ruc->"+ruc+". Proceso de Emision de Documentos no se levanto.");
			int li_envio = enviaEmail("message_error", emite, SBmsj.toString(),"","", null);		
			throw new Exception(SBmsj.toString());		
		}
		
		emite = new Emisor();
		ServiceData.databaseType = Util.driverConection;
		InformacionTributaria infTribAdic = new InformacionTributaria();
		InfoEmpresa infEmp = new InfoEmpresa();
		if (!emite.existeEmpresa(ruc)){
			String mensaje = " Empresa no existe o no se encuentra Activa. Ruc->" +ruc+". Proceso de Emision de Documentos no se levanto.";
			int li_envio = enviaEmail("message_error", emite,  mensaje,mensaje,null, null);
			throw new Exception(mensaje);
		}
		if (ServiceData.databaseType.indexOf("postgresql")>0)
			ServiceData.databaseType = "PostgreSQL";
		if (ServiceData.databaseType.indexOf("sqlserver")>0)
			ServiceData.databaseType = "SQLServer";
	
		infEmp = emite.obtieneInfoEmpresa(ruc);
		String estado = "", mensaje = "";
		//"NgRm2014"
		System.out.println("Directorio::"+infEmp.getDirectorio());
		boolean flagFile = false;
		FacCabDocumento CabDoc=null;
		ReporteSentencias rpSen=null;
		
		numHilo = Integer.parseInt(Environment.c.getString("facElectronica.general.EMISION.numHilos"));
		sleepHilo = Integer.parseInt(Environment.c.getString("facElectronica.general.EMISION.sleepHilos"));
		sleepBloqueHilo = Integer.parseInt(Environment.c.getString("facElectronica.general.EMISION.sleepBloqueHilo"));
		intentos = Integer.parseInt(Environment.c.getString("facElectronica.general.EMISION.AutorizacionIntentos"));
		timeIntentos = Integer.parseInt(Environment.c.getString("facElectronica.general.EMISION.AutorizacionTimeIntentos"));
		
		Thread hilo = null;
		contador = 0;
		
		/*
		try{
			ServiceDataAutorizacion hiloAutorizacion = new ServiceDataAutorizacion(ruc);
			Thread threadAutorizacion = new Thread(hiloAutorizacion);
			threadAutorizacion.start();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			ServiceDataCT hiloCT = new ServiceDataCT(ruc);
			Thread threadCT = new Thread(hiloCT);
			threadCT.start();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			ServiceDataNoEnviados hiloNoEnviados = new ServiceDataNoEnviados(ruc);
			Thread threadNoEnviados = new Thread(hiloNoEnviados);
			threadNoEnviados.start();
		}catch(Exception e){
			e.printStackTrace();
		}
		*/
		
		while ((Environment.cf.readCtrl().equals("S")))
		{
			try
			{
				contenido = FileDemo.busqueda(infEmp.getDirectorio(),".xml");
				if (contenido !=null)
				{
					if(contenido.length>0)
					{
						listAtencion = new ArrayList();
						for (int i=0; i < contenido.length; i++)
						{
					    	if (Environment.cf.readCtrl().equals("N")){
					    		break;
					    	}
					    	
					    	if (Environment.cf.readCtrl().equals("X")){
					    		numHilo = Integer.parseInt(Environment.c.getString("facElectronica.general.EMISION.numHilos"));
								sleepHilo = Integer.parseInt(Environment.c.getString("facElectronica.general.EMISION.sleepHilos"));
								sleepBloqueHilo = Integer.parseInt(Environment.c.getString("facElectronica.general.EMISION.sleepBloqueHilo"));
								intentos = Integer.parseInt(Environment.c.getString("facElectronica.general.EMISION.AutorizacionIntentos"));
								timeIntentos = Integer.parseInt(Environment.c.getString("facElectronica.general.EMISION.AutorizacionTimeIntentos"));
					    	}
					    	
							System.out.println("  Archivo a Procesar::"+contenido[i].getName());
							
							if (contador == numHilo)
							{
								hilo.join();
								new Thread().sleep(sleepBloqueHilo);
								System.gc();
								int contEjecutandose = 0;
								boolean flagCtrl = true;
								contador=0;
							}
					    	++contador ;
					    	System.out.println("Name File::"+contenido[i].getName());
		    	
					    	//Preparacion de Xml para procesar
					    	File fileProcesar =new File(contenido[i].getAbsolutePath());
					    	//Verificacion de archivos existentes. Si existen se mueven a reenviadosç
					    	File fileEliminar=new File(infEmp.getDirRecibidos()+"reenviados/"+contenido[i].getName());
				    		if(fileEliminar.exists()){
				    			fileEliminar.delete();
				    		}
				    		fileEliminar=new File(infEmp.getDirRecibidos()+"reenviados/"+contenido[i].getName().replace(".xml", "_backup.xml"));
				    		if(fileEliminar.exists()){
				    			fileEliminar.delete();
				    		}
				    		
				    		if ((System.getProperty("os.name").toUpperCase().indexOf("LINUX") == 0) || (System.getProperty("os.name").toUpperCase().indexOf("MAC") == 0))
					        {
				    			moveExist(infEmp.getDirRecibidos(), contenido[i].getName(), infEmp.getDirRecibidos()+"reenviados//");
					    		moveExist(infEmp.getDirNoAutorizados(), contenido[i].getName(), infEmp.getDirNoAutorizados()+"reenviados//");
					    		moveExist(infEmp.getDirContingencias(), contenido[i].getName(), infEmp.getDirContingencias()+"reenviados//");
					        }else{
					        	moveExist(infEmp.getDirRecibidos(), contenido[i].getName(), infEmp.getDirRecibidos()+"reenviados\\");
					    		moveExist(infEmp.getDirNoAutorizados(), contenido[i].getName(), infEmp.getDirNoAutorizados()+"reenviados\\");
					    		moveExist(infEmp.getDirContingencias(), contenido[i].getName(), infEmp.getDirContingencias()+"reenviados\\");
					      	}
				    		// FIN HFU
				    		File fremove = new File(infEmp.getDirRecibidos() + fileProcesar.getName());
					        if (fremove.exists()){
					        	fremove.delete();
					        }				        
					        fremove = new File(infEmp.getDirRecibidos() + fileProcesar.getName().replace(".xml", "_backup.xml"));
					        if (fremove.exists()){
					        	fremove.delete();
					        }
		    		
					        if(fileProcesar.renameTo(new File(infEmp.getDirRecibidos() + fileProcesar.getName())))
					        {
					        	fileProcesar =new File(infEmp.getDirRecibidos() + fileProcesar.getName());
					        	if (fileProcesar.exists())
					        	{
					        		emite.setFilexml(fileProcesar.getName());
					        		ServiceDataHilo threadAtiende = new ServiceDataHilo(contador,
					        															infEmp,
					        															new Emisor(emite.getFilexml(),Integer.parseInt(infEmp.getContribEspecial()),infEmp.getObligContabilidad()),
					        															nivelLog,
					        															intentos,
					        															timeIntentos);
					        		hilo = new Thread(threadAtiende);
					        		hilo.start();
					        		new Thread().sleep(sleepHilo);
					        	}else{
					        		logPrin.debug("Error en Procesar::Archivo No Existe::"+contenido+" Asignado Hilo "+contador);
					        	}
					        }else{
					        	logPrin.debug("Error al Mover::Archivo No Existe::"+infEmp.getDirRecibidos()+fileProcesar.getName()+" Asignado Hilo "+contador);
					        }
						}
						new Thread().sleep(sleepBloqueHilo);
					}else{
						//System.out.println("No Hay archivos que procesar...");
						new Thread().sleep(sleepBloqueHilo);
					}
				}
			}catch(Exception excep){
				excep.printStackTrace();
				int li_envio = enviaEmail("message_error", emite, "", "Proceso Invoice Error de Excepcion::"+excep.toString() ,"","");
			}		
    	}
	}
	
	
	
	public static void moveExist(String path, String filename, String pathMove)
	{
		File f = new File(path+filename);				      
		if(f.exists()){
			f.renameTo(new File(pathMove + filename));
		}
		f = new File(path+filename.replace(".xml", "_backup.xml"));
		if(f.exists()){
			f.renameTo(new File(pathMove + filename.replace(".xml", "_backup.xml")));
		}
	}
	
	
	public static int moveFile(String absolutePathOrigen, String pathDestino)
	{
		try{
			File dataInputFile = new File(absolutePathOrigen); 
			File fileSendPath = new File(pathDestino, dataInputFile.getName());  
			dataInputFile.renameTo(fileSendPath);
   	 	}catch(Exception e){
   	 		return 0;
   	 	}
		return 1;
	}
	
	public static int enviaEmail(String ls_id_mensaje, Emisor emi, String mensaje_mail, String mensaje_error, String fileAttachXml, String fileAttachPdf)
	{
		String emailHost = null;
		String emailFrom = null;
		String emailTo = null;
		String emailSubject = null;
		String emailMensaje = null;	
		String emailHelpDesk = null;
		//Host Mail Server
		emailHost = Environment.c.getString("facElectronica.alarm.email.host");
		//Enviado desde
		emailFrom = Environment.c.getString("facElectronica.alarm.email.sender");
		//Enviado para
		emailTo = Environment.c.getString("facElectronica.alarm.email.receivers-list");
		//Asunto
		emailSubject = Environment.c.getString("facElectronica.alarm.email.subject");
		//Email HelpDesk
		emailHelpDesk = Environment.c.getString("facElectronica.alarm.email.helpdesk");
		EmailSender emSend = new EmailSender(emailHost,emailFrom);
		emailMensaje = Environment.c.getString("facElectronica.alarm.email."+ls_id_mensaje);
		
		String clave = Environment.c.getString("facElectronica.alarm.email.password");
		String subject = Environment.c.getString("facElectronica.alarm.email.subject");
		String receivers = "";
		String user = Environment.c.getString("facElectronica.alarm.email.user");
		String tipo_autentificacion = Environment.c.getString("facElectronica.alarm.email.tipo_autentificacion");
		
		
		String tipoMail = Environment.c.getString("facElectronica.alarm.email.tipoMail");
		receivers = Environment.c.getString("facElectronica.alarm.email.receivers-list");				
		emSend.setPassword(clave);
		emSend.setSubject(subject);
		emSend.setUser(user);
		emSend.setAutentificacion(tipo_autentificacion);
		emSend.setTipoMail(tipoMail);
		emailTo = receivers;
		
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
			emailMensaje = emailMensaje.replace("|CabError|", "Hubo inconvenientes con");
			emailMensaje = emailMensaje.replace("|Mensaje|", mensaje_error);
		}
		if (ls_id_mensaje.equals("message_exito"))
		{
			emailMensaje = emailMensaje.replace("|CabMensaje|", " ");
		}		
		if ((emailTo!=null) && (emailTo.length()>0)){
					emSend.send(emailTo, 
								subject,
								emailMensaje,
			  		  	        fileAttachXml,
			  		  	        fileAttachPdf);
		}
		return 0;
	}
	
	

	public String getNivelLog() {
		return nivelLog;
	}
	
	public void setNivelLog(String nivelLog) {
		this.nivelLog = nivelLog;
	}
	
	
}
