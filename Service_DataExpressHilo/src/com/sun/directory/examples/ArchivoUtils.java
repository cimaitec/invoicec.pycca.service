package com.sun.directory.examples;

import com.sun.DAO.ControlErrores;
import com.sun.businessLogic.validate.LeerDocumentos;
import com.sun.comprobantes.util.EmailSender;
import com.thoughtworks.xstream.XStream;

import ec.gob.sri.comprobantes.administracion.modelo.ClaveContingencia;
import ec.gob.sri.comprobantes.administracion.modelo.ConfiguracionDirectorio;
import ec.gob.sri.comprobantes.administracion.modelo.Emisor;
import ec.gob.sri.comprobantes.modelo.factura.Factura;
import ec.gob.sri.comprobantes.modelo.guia.GuiaRemision;
import ec.gob.sri.comprobantes.modelo.notacredito.NotaCredito;
import ec.gob.sri.comprobantes.modelo.notadebito.NotaDebito;
import ec.gob.sri.comprobantes.modelo.rentencion.ComprobanteRetencion;
import ec.gob.sri.comprobantes.sql.ClavesSQL;
import ec.gob.sri.comprobantes.sql.ComprobantesSQL;
import ec.gob.sri.comprobantes.sql.ConfiguracionDirectorioSQL;
import ec.gob.sri.comprobantes.util.AutorizacionComprobantesWs;
import ec.gob.sri.comprobantes.util.DirectorioEnum;
import ec.gob.sri.comprobantes.util.FormGenerales;
import ec.gob.sri.comprobantes.util.TipoComprobanteEnum;

import com.sun.comprobantes.util.X509Utils;

import ec.gob.sri.comprobantes.util.xml.Java2XML;
import ec.gob.sri.comprobantes.util.xml.LectorXPath;
import ec.gob.sri.comprobantes.util.xml.ValidadorEstructuraDocumento;
import ec.gob.sri.comprobantes.util.xml.XStreamUtil;
import ec.gob.sri.comprobantes.ws.RespuestaSolicitud;
import ec.gob.sri.comprobantes.ws.aut.Autorizacion;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import xadesbes.ServicioFirmaXades;

import com.util.webServices.EnvioComprobantesWs;

public class ArchivoUtils
{
	public static String archivoToString(String rutaArchivo)
	{
		System.out.println("-- INICIO ArchivoUtils.archivoToString --");
		StringBuffer buffer = new StringBuffer();
		try
		{
			FileInputStream fis = new FileInputStream(rutaArchivo);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			Reader in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) > -1) {
				buffer.append((char)ch);
			}
			in.close();
			return buffer.toString();
		} catch (IOException e) {
			System.out.println(e);
			Logger.getLogger(ArchivoUtils.class.getName()).log(Level.SEVERE, null, e);
		}
		System.out.println("-- FIN ArchivoUtils.archivoToString --");
		return null;
	}
	
	public static File stringToArchivo(String rutaArchivo, String contenidoArchivo)
	{
		System.out.println("-- INICIO ArchivoUtils.stringToArchivo --");
		FileOutputStream fos = null;
		File archivoCreado = null;
		try
		{
			fos = new FileOutputStream(rutaArchivo);
			OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
			for (int i = 0; i < contenidoArchivo.length(); i++) {
				out.write(contenidoArchivo.charAt(i));
			}
			out.close();
			archivoCreado = new File(rutaArchivo);
		}
		catch (Exception ex)
		{
			System.out.println(ex);
            System.out.println(ex.getMessage());
            System.out.println(ex.getCause());
        	System.out.println(ex.getLocalizedMessage());
        	System.out.println(ex.getStackTrace());
			int i;
			Logger.getLogger(ArchivoUtils.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		} finally {
			try {
				if (fos != null)
					fos.close();
				}
			catch (Exception ex) {
				System.out.println(ex);
				Logger.getLogger(ArchivoUtils.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println("-- FIN ArchivoUtils.stringToArchivo --");
		return archivoCreado;
	}

  public static byte[] archivoToByte(File file)
    throws IOException
  {
    byte[] buffer = new byte[(int)file.length()];
    InputStream ios = null;
    try {
      ios = new FileInputStream(file);
      if (ios.read(buffer) == -1)
        throw new IOException("EOF reached while trying to read the whole file");
    }
    finally {
      try {
        if (ios != null)
          ios.close();
      }
      catch (IOException e) {
        Logger.getLogger(ArchivoUtils.class.getName()).log(Level.SEVERE, null, e);
      }
    }

    return buffer;
  }

  public static boolean byteToFile(byte[] arrayBytes, String rutaArchivo)
  {
    boolean respuesta = false;
    try {
      File file = new File(rutaArchivo);
      file.createNewFile();
      FileInputStream fileInputStream = new FileInputStream(rutaArchivo);
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(arrayBytes);
      OutputStream outputStream = new FileOutputStream(rutaArchivo);
      int data;
      while ((data = byteArrayInputStream.read()) != -1) {
        outputStream.write(data);
      }

      fileInputStream.close();
      outputStream.close();
      respuesta = true;
    } catch (IOException ex) {
      Logger.getLogger(ArchivoUtils.class.getName()).log(Level.SEVERE, null, ex);
    }
    return respuesta;
  }

  public static String obtenerValorXML(File xmlDocument, String expression)
  {
    String valor = null;
    try
    {
      LectorXPath reader = new LectorXPath(xmlDocument.getPath());
      valor = (String)reader.leerArchivo(expression, XPathConstants.STRING);
    }
    catch (Exception e) {
      Logger.getLogger(ArchivoUtils.class.getName()).log(Level.SEVERE, null, e);
    }

    return valor;
  }

  public static String seleccionaXsd(String tipo)
  {
    String nombreXsd = null;

    if (tipo.equals(TipoComprobanteEnum.FACTURA.getCode()))
      nombreXsd = TipoComprobanteEnum.FACTURA.getXsd();
    else if (tipo.equals(TipoComprobanteEnum.COMPROBANTE_DE_RETENCION.getCode()))
      nombreXsd = TipoComprobanteEnum.COMPROBANTE_DE_RETENCION.getXsd();
    else if (tipo.equals(TipoComprobanteEnum.GUIA_DE_REMISION.getCode()))
      nombreXsd = TipoComprobanteEnum.GUIA_DE_REMISION.getXsd();
    else if (tipo.equals(TipoComprobanteEnum.NOTA_DE_CREDITO.getCode()))
      nombreXsd = "notaCredito.xsd";
    else if (tipo.equals(TipoComprobanteEnum.NOTA_DE_DEBITO.getCode()))
      nombreXsd = TipoComprobanteEnum.NOTA_DE_DEBITO.getXsd();
    else if (tipo.equals(TipoComprobanteEnum.LOTE.getCode())) {
      nombreXsd = TipoComprobanteEnum.LOTE.getXsd();
    }
    return nombreXsd;
  }
  
  public static String firmarArchivo(com.sun.businessLogic.validate.Emisor emisor, String archivoACrear, String dirFirmados, String tokenId, String password, String rutaCertificado)
  {
	  String respuestaFirma = null;
	  try
	  {
		  System.out.println("-- INCICIO ArchivoUtils.firmarArchivo --");

		  if ((password==null)||(System.getProperty("os.name").toUpperCase().indexOf("LINUX") == 0) || (System.getProperty("os.name").toUpperCase().indexOf("MAC") == 0))
		  {
	    	  respuestaFirma = X509Utils.firmaValidaArchivo(new File(archivoACrear), dirFirmados, emisor.getInfEmisor().getRuc(), tokenId, password, rutaCertificado);
		  }
		  else
		  {
	    	  respuestaFirma = ServicioFirmaXades.firmaValidaArchivo(new File(archivoACrear), dirFirmados, emisor.getInfEmisor().getRucFirmante(), password.toCharArray());
		  }
	  }
	  catch (Exception ex)
	  {
		  System.out.println("	Error Firmado");
		  System.out.println("	 Respuesta firma "+respuestaFirma);
		  System.out.println(ex);
		  ex.printStackTrace();
		  Logger.getLogger(ArchivoUtils.class.getName()).log(Level.SEVERE, null, ex);
		  return ex.getMessage();
    }
	  System.out.println("  Respuesta firma  "+respuestaFirma);
	  System.out.println("-- FIN ArchivoUtils.firmarArchivo --");
    return respuestaFirma;
  }  
  

  public static String validaArchivoXSD(String tipoComprobante, String pathArchivoXML, String pathXSD)
  {
    String respuestaValidacion = null;
    try
    {
      ValidadorEstructuraDocumento validador = new ValidadorEstructuraDocumento();
      String nombreXsd = seleccionaXsd(tipoComprobante);

      String pathArchivoXSD = pathXSD + nombreXsd;

      if (pathArchivoXML != null) {
        validador.setArchivoXML(new File(pathArchivoXML));
        validador.setArchivoXSD(new File(pathArchivoXSD));

        respuestaValidacion = validador.validacion();
      }
    } catch (Exception ex) {
    	ex.printStackTrace();
      Logger.getLogger(ArchivoUtils.class.getName()).log(Level.SEVERE, null, ex);
    }
    return respuestaValidacion;
  }

  private static String realizaMarshal(Object comprobante, String pathArchivo)
  {
    String respuesta = null;

    if ((comprobante instanceof Factura))
      respuesta = Java2XML.marshalFactura((Factura)comprobante, pathArchivo);
    else if ((comprobante instanceof NotaDebito))
      respuesta = Java2XML.marshalNotaDeDebito((NotaDebito)comprobante, pathArchivo);
    else if ((comprobante instanceof NotaCredito))
      respuesta = Java2XML.marshalNotaDeCredito((NotaCredito)comprobante, pathArchivo);
    else if ((comprobante instanceof ComprobanteRetencion))
      respuesta = Java2XML.marshalComprobanteRetencion((ComprobanteRetencion)comprobante, pathArchivo);
    else if ((comprobante instanceof GuiaRemision)) {
      respuesta = Java2XML.marshalGuiaRemision((GuiaRemision)comprobante, pathArchivo);
    }
    return respuesta;
  }

  public static String crearArchivoXml2(String pathArchivo, Object objetoModelo, ClaveContingencia claveContingencia, Long secuencial, String tipoComprobante)
  {
    String respuestaCreacion = null;
    if (objetoModelo != null)
      try {
        respuestaCreacion = realizaMarshal(objetoModelo, pathArchivo);

        if (respuestaCreacion == null)
        {
          if ((claveContingencia != null) && (claveContingencia.getCodigoComprobante() != null)) {
            claveContingencia.setUsada("S");
            new ClavesSQL().actualizaClave(claveContingencia);
          }

          new ComprobantesSQL().actualizaSecuencial(tipoComprobante, Long.valueOf(secuencial.longValue() + 1L));
        }
      } catch (Exception ex) {
        Logger.getLogger(ArchivoUtils.class.getName()).log(Level.SEVERE, null, ex);
      }
    else {
      respuestaCreacion = "Ingrese los campos obligatorios del comprobante";
    }
    return respuestaCreacion;
  }

  public static String obtieneClaveAccesoAutorizacion(Autorizacion item)
  {
    String claveAcceso = null;

    String xmlAutorizacion = XStreamUtil.getRespuestaLoteXStream().toXML(item);
    File archivoTemporal = new File("temp.xml");

    stringToArchivo(archivoTemporal.getPath(), xmlAutorizacion);
    String contenidoXML = decodeArchivoBase64(archivoTemporal.getPath());

    if (contenidoXML != null) {
      stringToArchivo(archivoTemporal.getPath(), contenidoXML);
      claveAcceso = obtenerValorXML(archivoTemporal, "/*/infoTributaria/claveAcceso");
    }

    return claveAcceso;
  }

  public static String decodeArchivoBase64(String pathArchivo)
  {
    String xmlDecodificado = null;
    try {
      File file = new File(pathArchivo);
      if (file.exists())
      {
        String encd = obtenerValorXML(file, "/*/comprobante");

        xmlDecodificado = encd;
      }
      else {
        System.out.print("File not found!");
      }
    } catch (Exception e) {
      Logger.getLogger(AutorizacionComprobantesWs.class.getName()).log(Level.SEVERE, null, e);
    }
    return xmlDecodificado;
  }

  public static boolean anadirMotivosRechazo(File archivo, RespuestaSolicitud respuestaRecepcion,String nombreArchivoRespuesta, String pathRespuesta)
  {
    boolean exito = false;
    File respuesta = new File(pathRespuesta+nombreArchivoRespuesta);
    Java2XML.marshalRespuestaSolicitud(respuestaRecepcion, respuesta.getPath());
    if (adjuntarArchivo(respuesta, archivo) == true) {
      exito = true;
      respuesta.delete();
    }
    return exito;
  }

  public static boolean adjuntarArchivo(File respuesta, File comprobante)
  {
    boolean exito = false;
    try
    {
      Document document = merge("*", new File[] { comprobante, respuesta });

      DOMSource source = new DOMSource(document);

      StreamResult result = new StreamResult(new OutputStreamWriter(new FileOutputStream(comprobante), "UTF-8"));

      TransformerFactory transFactory = TransformerFactory.newInstance();
      Transformer transformer = transFactory.newTransformer();

      transformer.transform(source, result);
    }
    catch (Exception ex) {
      Logger.getLogger(ArchivoUtils.class.getName()).log(Level.SEVERE, null, ex);
    }
    return exito;
  }

  private static Document merge(String exp, File[] files)
    throws Exception
  {
    XPathFactory xPathFactory = XPathFactory.newInstance();
    XPath xpath = xPathFactory.newXPath();
    XPathExpression expression = xpath.compile(exp);

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setIgnoringElementContentWhitespace(true);
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document base = docBuilder.parse(files[0]);

    Node results = (Node)expression.evaluate(base, XPathConstants.NODE);
    if (results == null) {
      throw new IOException(files[0] + ": expression does not evaluate to node");
    }

    for (int i = 1; i < files.length; i++) {
      Document merge = docBuilder.parse(files[i]);
      Node nextResults = (Node)expression.evaluate(merge, XPathConstants.NODE);
      results.appendChild(base.importNode(nextResults, true));
    }

    return base;
  }

  public static boolean copiarArchivo(File archivoOrigen, String pathDestino)
  {
	  FileReader in = null;
	  boolean resultado = false;
	  try
	  {
		  File outputFile = new File(pathDestino);
		  in = new FileReader(archivoOrigen);
		  FileWriter out = new FileWriter(outputFile);
		  int c;
		  while ((c = in.read()) != -1) {
			  out.write(c);
		  }
		  in.close();
		  out.close();
		  resultado = true;
	  }
	  catch (Exception ex) {
		  ex.printStackTrace();
	  } finally {
		  try {
			  in.close();
		  } catch (IOException ex) {
			  ex.printStackTrace();
		  }
	  }
	  return resultado;
  }

}