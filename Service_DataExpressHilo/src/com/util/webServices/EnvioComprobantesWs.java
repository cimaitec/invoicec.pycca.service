package com.util.webServices;

 
 import ec.gob.sri.comprobantes.modelo.Respuesta;
import ec.gob.sri.comprobantes.sql.RespuestaSQL;
import ec.gob.sri.comprobantes.ws.Comprobante;
import ec.gob.sri.comprobantes.ws.Comprobante.Mensajes;
import ec.gob.sri.comprobantes.ws.Mensaje;
import ec.gob.sri.comprobantes.ws.RecepcionComprobantes;
import ec.gob.sri.comprobantes.ws.RecepcionComprobantesService;
import ec.gob.sri.comprobantes.ws.RespuestaSolicitud;
import ec.gob.sri.comprobantes.ws.RespuestaSolicitud.Comprobantes;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;






import com.sun.DAO.ControlErrores;
import com.util.util.key.ArchivoUtils;
 
 public class EnvioComprobantesWs
 {
   private static RecepcionComprobantesService service;
   private static final String VERSION = "1.0.0";
   public static final String ESTADO_RECIBIDA = "RECIBIDA";
   public static final String ESTADO_DEVUELTA = "DEVUELTA";
 
   public EnvioComprobantesWs(String wsdlLocation, int timeout)
     throws Exception
   {
	     try{
		     URL url = new URL(wsdlLocation);
		     URLConnection con = (URLConnection) url.openConnection();
		     con.setConnectTimeout(timeout);
		     con.setReadTimeout(timeout);
		     new Thread(new InterruptThread(Thread.currentThread(), con,timeout)).start();
		     QName qname = new QName("http://ec.gob.sri.ws.recepcion", "RecepcionComprobantesService");
		     service = new RecepcionComprobantesService(con.getURL(), qname);
	     }catch (java.net.SocketTimeoutException e) {	    	 
	    	 throw new Exception("TIMEOUT,"+e.getMessage());
	     }catch(IOException ex){
	     	 throw new Exception("SIN-SERVICIO,"+ex.getMessage());
	     }catch(Exception exc){
	    	 throw new Exception("ERROR-GENERAL,"+exc.getMessage());
	     }
   }
  
   public RespuestaSolicitud enviarComprobante(String ruc, File xmlFile, String tipoComprobante, String versionXsd, int timeout) throws Exception
   {
	   RespuestaSolicitud response = null;
	   try {
		   service.getWSDLDocumentLocation().openConnection().setReadTimeout(timeout);
		   service.getWSDLDocumentLocation().openConnection().setConnectTimeout(timeout);
		   RecepcionComprobantes port = service.getRecepcionComprobantesPort();
		   service.getWSDLDocumentLocation().openConnection().setReadTimeout(timeout);
		   service.getWSDLDocumentLocation().openConnection().setConnectTimeout(timeout);
		   response = port.validarComprobante(ArchivoUtils.archivoToByte(xmlFile));
	   }
	   catch (Exception e) {
		   //Logger.getLogger(EnvioComprobantesWs.class.getName()).log(Level.SEVERE, null, e);
		   response = new RespuestaSolicitud();
		   response.setEstado(e.getMessage());
		   throw new Exception(e.getMessage());
		   //return response;
     }
	 return response;
   }
 
   public RespuestaSolicitud enviarComprobanteLotes(String ruc,
   													byte[] xml,
													String tipoComprobante,
													String versionXsd)
   {
	   RespuestaSolicitud response = null;
	   try {
		   RecepcionComprobantes port = service.getRecepcionComprobantesPort();
		   response = port.validarComprobante(xml);
	   }
	   catch (Exception e) {
		   Logger.getLogger(EnvioComprobantesWs.class.getName()).log(Level.SEVERE, null, e);
		   response = new RespuestaSolicitud();
		   response.setEstado(e.getMessage());
		   return response;
	   }
	   return response;
   }
 
   public RespuestaSolicitud enviarComprobanteLotes(String ruc, File xml, String tipoComprobante, String versionXsd)
   {
	   RespuestaSolicitud response = null;
	   try {
		   RecepcionComprobantes port = service.getRecepcionComprobantesPort();
		   response = port.validarComprobante(ArchivoUtils.archivoToByte(xml));
	   } catch (Exception e) {
		   Logger.getLogger(EnvioComprobantesWs.class.getName()).log(Level.SEVERE, null, e);
		   response = new RespuestaSolicitud();
		   response.setEstado(e.getMessage());
		   return response;
	   }
	   return response;
   }
   
   public static RespuestaSolicitud obtenerRespuestaEnvio(File archivo,
		   												  String ruc,
												   		  String tipoComprobante,
														  String claveDeAcceso,
														  String urlWsdl,
														  int timeout) throws Exception
   {
	   System.out.println("-- INICIO EnvioComprobantesWS.obtenerRespuestaEnvio --");
	   RespuestaSolicitud respuesta = new RespuestaSolicitud();
	   EnvioComprobantesWs cliente = null;
	   try {       
		   cliente = new EnvioComprobantesWs(urlWsdl,timeout);
		   respuesta = cliente.enviarComprobante(ruc, archivo, tipoComprobante, "1.0.0",timeout);
     }catch(Exception exc){    	 
	   }
	   System.out.println("-- FIN EnvioComprobantesWS.obtenerRespuestaEnvio --");
	   return respuesta;
   }
   
 
   public class InterruptThread implements Runnable
   {
	    Thread parent;
	    URLConnection con;
	    int timeout;
	    public InterruptThread(Thread parent, URLConnection con, int timeout)
	    {
	        this.parent = parent;
	        this.con = con;
	        this.timeout = timeout;
	    }

	    public void run()
	    {
	        try {
	            Thread.sleep(timeout);
	        } catch (InterruptedException e) {

	        }
	        //System.out.println("Timer thread forcing parent to quit connection");
	        HttpURLConnection hCon = ((HttpURLConnection)con);
	        hCon.disconnect();
	        //System.out.println("Timer thread closed connection held by parent, exiting");
	    }
	}
 
 }
 
