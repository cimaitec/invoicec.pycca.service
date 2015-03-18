package com.sun.directory.examples;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.sun.businessLogic.validate.Emisor;

public class ModifyDocumentAcceso
{
	public static Emisor addPutClaveAcceso(String fileName, Emisor emite) throws Exception
	{
		try
		{
			System.out.println("-- INCICIO ModifyDocumentAcceso.addPutClaveAcceso --");
			String ls_claveAcceso = "";
			String lsNodoRaiz = "infoTributaria";
			boolean lbExistClaveAcceso = false;
		    try {
				ls_claveAcceso = generarClaveAcceso(emite);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
			emite.getInfEmisor().setClaveAcceso(ls_claveAcceso);
			if(emite.getInfEmisor().getClaveAcceso().length()!=49){
				String mensaje = " Error al generar la clave de acceso. Ruc->" +emite.getInfEmisor().getRuc()+ " Establecimient"+emite.getInfEmisor().getCodEstablecimiento();
	    	    throw new Exception(mensaje);
			}
			String filepath = fileName;
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(filepath);
	 
			// Get the root element
			Node tipodocumento = doc.getFirstChild();
			
			Node documento = doc.getElementsByTagName(lsNodoRaiz).item(0);
			Node NclaveAcceso= getNode(doc, "claveAcceso", lsNodoRaiz);
			if (NclaveAcceso == null){
				lbExistClaveAcceso = false;
			}else{
				lbExistClaveAcceso = true;
			}
			if (lbExistClaveAcceso){
				//Tag claveAcceso existe proceder a modificar el valor 
				NodeList list = documento.getChildNodes();				 
				for (int i = 0; i < list.getLength(); i++) {
		           Node node = list.item(i);
				   // get the salary element, and update the value
				   if ("claveAcceso".equals(node.getNodeName())) {
					   node.setTextContent(ls_claveAcceso);
				   }
				}
			}else{
				//Tag claveAcceso no existe proceder a armar el xml con el nuevo tag
				NodeList nodesCodDoc = doc.getElementsByTagName("codDoc");
				Text a = doc.createTextNode(ls_claveAcceso); 
				Element p = doc.createElement("claveAcceso"); 
				p.appendChild(a);
				nodesCodDoc.item(0).getParentNode().insertBefore(p, nodesCodDoc.item(0));
/*
				NodeList list = documento.getChildNodes();					
				for (int i = 0; i < list.getLength(); i++) {
			           Node node = list.item(i);			   
			           //remove campos
					   if ("codDoc".equals(node.getNodeName())) {
						   documento.removeChild(node);
					   }			   
				}
				list = documento.getChildNodes();
				for (int i = 0; i < list.getLength(); i++) {
		           Node node = list.item(i);			   
		           //remove campos
				   if ("estab".equals(node.getNodeName())) {
					   documento.removeChild(node);
				   }			   
				}
				list = documento.getChildNodes();
				for (int i = 0; i < list.getLength(); i++) {
			           Node node = list.item(i);			   
			           //remove campos
				   if ("ptoEmi".equals(node.getNodeName())) {
					   documento.removeChild(node);
				   }
				}
				list = documento.getChildNodes();
				for (int i = 0; i < list.getLength(); i++) {
			           Node node = list.item(i);			   
			           //remove campos
				   if ("secuencial".equals(node.getNodeName())) {
					   documento.removeChild(node);
				   }
				}
				list = documento.getChildNodes();
				for (int i = 0; i < list.getLength(); i++) {
			           Node node = list.item(i);			   
			           //remove campos
				   if ("dirMatriz".equals(node.getNodeName())) {
					   documento.removeChild(node);
				   }
				}*/
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			StreamResult result = new StreamResult(new File(filepath));
			transformer.transform(source, result);
			/*
			if (!lbExistClaveAcceso){
				filepath = fileName;
				docFactory = DocumentBuilderFactory.newInstance();
				docBuilder = docFactory.newDocumentBuilder();
				doc = docBuilder.parse(filepath);
		 
				// Get the root element
				tipodocumento = doc.getFirstChild();
	
				documento = doc.getElementsByTagName(lsNodoRaiz).item(0);
				// append a new node to documento
				Element claveAcceso = doc.createElement("claveAcceso");
				claveAcceso.appendChild(doc.createTextNode(ls_claveAcceso));
				documento.appendChild(claveAcceso);
				
				Element codDoc = doc.createElement("codDoc");
				codDoc.appendChild(doc.createTextNode(emite.getInfEmisor().getTipoComprobante()));
				documento.appendChild(codDoc);
				
				Element estab = doc.createElement("estab");
				estab.appendChild(doc.createTextNode(emite.getInfEmisor().getCodEstablecimiento()));
				documento.appendChild(estab);
				
				Element ptoEmi = doc.createElement("ptoEmi");
				ptoEmi.appendChild(doc.createTextNode(emite.getInfEmisor().getCodPuntoEmision()));
				documento.appendChild(ptoEmi);
				
				Element secuencial = doc.createElement("secuencial");
				secuencial.appendChild(doc.createTextNode(emite.getInfEmisor().getSecuencial()));
				documento.appendChild(secuencial);
				
				Element dirMatriz = doc.createElement("dirMatriz");
				dirMatriz.appendChild(doc.createTextNode(emite.getInfEmisor().getDireccionMatriz()));
				documento.appendChild(dirMatriz);
	
				// write the content into xml file
				transformerFactory = TransformerFactory.newInstance();
				transformer = transformerFactory.newTransformer();
				source = new DOMSource(doc);
							
				result = new StreamResult(new File(filepath));
				transformer.transform(source, result);
		   }*/
							
	 
		   } catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		   } catch (TransformerException tfe) {
			tfe.printStackTrace();
		   } catch (IOException ioe) {
			ioe.printStackTrace();
		   } catch (SAXException sae) {
			sae.printStackTrace();
		   }
			System.out.println("-- FIN ModifyDocumentAcceso.addPutClaveAcceso --");
		   return emite;
		}


	public static Emisor addPutClaveAccesoContingencia(String fileName, Emisor emite) throws Exception {
		try {
			System.out.println("-- INICIO ModifyDocumentAcceso.addPutClaveAccesoContingencia --");
			   
			String ls_claveAcceso = "";
			String lsNodoRaiz = "infoTributaria";
			boolean lbExistClaveAcceso = false;
			ls_claveAcceso = emite.getInfEmisor().getClaveAcceso();
			String filepath = fileName;
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(filepath);
	 
			// Get the root element
			Node tipodocumento = doc.getFirstChild();
			Node documento = doc.getElementsByTagName(lsNodoRaiz).item(0);
			Node NclaveAcceso= getNode(doc, "claveAcceso", lsNodoRaiz);
			if (NclaveAcceso == null){
				lbExistClaveAcceso = false;
			}else{
				lbExistClaveAcceso = true;
			}
			if (lbExistClaveAcceso){
				//Tag claveAcceso existe proceder a modificar el valor 
				NodeList list = documento.getChildNodes();				 
				for (int i = 0; i < list.getLength(); i++) {
		           Node node = list.item(i);
				   // get the salary element, and update the value
				   if ("claveAcceso".equals(node.getNodeName())) {
					   node.setTextContent(ls_claveAcceso);
				   }
				   if ("tipoEmision".equals(node.getNodeName())) {
					   node.setTextContent("2");
				   }
				}
			}else{
				//Tag claveAcceso no existe proceder a armar el xml con el nuevo tag
				NodeList nodesCodDoc = doc.getElementsByTagName("codDoc");
				Text a = doc.createTextNode(ls_claveAcceso); 
				Element p = doc.createElement("claveAcceso"); 
				p.appendChild(a);
				nodesCodDoc.item(0).getParentNode().insertBefore(p, nodesCodDoc.item(0));		
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			StreamResult result = new StreamResult(new File(filepath));
			transformer.transform(source, result);
} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		   } catch (TransformerException tfe) {
			tfe.printStackTrace();
		   } catch (IOException ioe) {
			ioe.printStackTrace();
		   } catch (SAXException sae) {
			sae.printStackTrace();
		   }
		System.out.println("-- FIN ModifyDocumentAcceso.addPutClaveAccesoContingencia --");
		   return emite;
		}
	
	public static Node getNode(Document doc, String nameNodo, String nameRaiz)
	{
		Node nodeFound = null;
		Node nodeFind = doc.getElementsByTagName(nameRaiz).item(0);
		NodeList list = nodeFind.getChildNodes();					
		for (int i = 0; i < list.getLength(); i++) {
	           Node node = list.item(i);			   
			   if (nameNodo.equals(node.getNodeName())) {
				   nodeFound = node;
			   }			   
		}
		return nodeFound;
	}
	
	public static Emisor addClaveAcceso(String fileName, Emisor emite) throws Exception
	{
		try
		{
			String filepath = fileName;
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(filepath);
	 
			// Get the root element
			Node tipodocumento = doc.getFirstChild();
			String lsNodoRaiz = "";
			if (emite.getInfEmisor().getTipoComprobante().equals("01")){
				lsNodoRaiz = "infoTributaria";
			}
			if (emite.getInfEmisor().getTipoComprobante().equals("07")){
				lsNodoRaiz = "infoTributaria";
			}
			if (emite.getInfEmisor().getTipoComprobante().equals("04")){
				lsNodoRaiz = "infoTributaria";
			}
			if (emite.getInfEmisor().getTipoComprobante().equals("05")){
				lsNodoRaiz = "infoTributaria";
			}
			if (emite.getInfEmisor().getTipoComprobante().equals("06")){
				lsNodoRaiz = "infoTributaria";
			}
			
			Node documento = doc.getElementsByTagName(lsNodoRaiz).item(0);	 
			String ls_claveAcceso = "";
			try {
				ls_claveAcceso = generarClaveAcceso(emite);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			emite.getInfEmisor().setClaveAcceso(ls_claveAcceso);
			if(emite.getInfEmisor().getClaveAcceso().length()!=49){
				String mensaje = " Error al generar la clave de acceso. Ruc->" +emite.getInfEmisor().getRuc()+ " Establecimient"+emite.getInfEmisor().getCodEstablecimiento();
	    	    throw new Exception(mensaje);
			}
			NodeList list = documento.getChildNodes();					
			for (int i = 0; i < list.getLength(); i++) {
		           Node node = list.item(i);			   
		           //remove campos
				   if ("codDoc".equals(node.getNodeName())) {
					   documento.removeChild(node);
				   }			   
			}
			list = documento.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
	           Node node = list.item(i);			   
	           //remove campos
			   if ("estab".equals(node.getNodeName())) {
				   documento.removeChild(node);
			   }			   
			}
			list = documento.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
		           Node node = list.item(i);			   
		           //remove campos
			   if ("ptoEmi".equals(node.getNodeName())) {
				   documento.removeChild(node);
			   }
			}
			list = documento.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
		           Node node = list.item(i);			   
		           //remove campos
			   if ("secuencial".equals(node.getNodeName())) {
				   documento.removeChild(node);
			   }
			}
			list = documento.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
		           Node node = list.item(i);			   
		           //remove campos
			   if ("dirMatriz".equals(node.getNodeName())) {
				   documento.removeChild(node);
			   }
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			
			StreamResult result = new StreamResult(new File(filepath));
			transformer.transform(source, result);
			
		    filepath = fileName;
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(filepath);
	 
			// Get the root element
			tipodocumento = doc.getFirstChild();
			lsNodoRaiz = "";
			if (emite.getInfEmisor().getTipoComprobante().equals("01")){
				lsNodoRaiz = "infoTributaria";
			}
			if (emite.getInfEmisor().getTipoComprobante().equals("07")){
				lsNodoRaiz = "infoTributaria";
			}
			if (emite.getInfEmisor().getTipoComprobante().equals("04")){
				lsNodoRaiz = "infoTributaria";
			}
			if (emite.getInfEmisor().getTipoComprobante().equals("05")){
				lsNodoRaiz = "infoTributaria";
			}
			if (emite.getInfEmisor().getTipoComprobante().equals("06")){
				lsNodoRaiz = "infoTributaria";
			}

			documento = doc.getElementsByTagName(lsNodoRaiz).item(0);
			// append a new node to documento
			Element claveAcceso = doc.createElement("claveAcceso");
			claveAcceso.appendChild(doc.createTextNode(ls_claveAcceso));
			documento.appendChild(claveAcceso);
			
			Element codDoc = doc.createElement("codDoc");
			codDoc.appendChild(doc.createTextNode(emite.getInfEmisor().getTipoComprobante()));
			documento.appendChild(codDoc);
			
			Element estab = doc.createElement("estab");
			estab.appendChild(doc.createTextNode(emite.getInfEmisor().getCodEstablecimiento()));
			documento.appendChild(estab);
			
			Element ptoEmi = doc.createElement("ptoEmi");
			ptoEmi.appendChild(doc.createTextNode(emite.getInfEmisor().getCodPuntoEmision()));
			documento.appendChild(ptoEmi);
			
			Element secuencial = doc.createElement("secuencial");
			secuencial.appendChild(doc.createTextNode(emite.getInfEmisor().getSecuencial()));
			documento.appendChild(secuencial);
			
			Element dirMatriz = doc.createElement("dirMatriz");
			dirMatriz.appendChild(doc.createTextNode(emite.getInfEmisor().getDireccionMatriz()));
			documento.appendChild(dirMatriz);

			// write the content into xml file
			transformerFactory = TransformerFactory.newInstance();
			transformer = transformerFactory.newTransformer();
			source = new DOMSource(doc);
						
			result = new StreamResult(new File(filepath));
			transformer.transform(source, result);
		} 
		catch (ParserConfigurationException pce){
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}
		return emite;
	}
	
	public static String generarClaveAcceso(Emisor emite) throws Exception
	{
		System.out.println("-- INICIO ModifyDocumentAcceso.generarClaveAcceso --");
	    //int li_random = (int)Math.round(Math.random() * 10000000);
	    String lsSecuencialRamdom = emite.getInfEmisor().getSecuencial().substring(1, 9);
	    int li_random = Integer.parseInt(lsSecuencialRamdom.substring(1,8));
	    
        String ls_random = String.valueOf(li_random);
        int tam = ls_random.length();
        
        for (int i=tam+1;i<8;i++){
        	ls_random = ls_random + "0";
        }
        
        li_random = Integer.parseInt(ls_random);
	    String fecha_clave = "";
	    String codigoNumerico = digitoVerificador(Integer.toString(li_random));
	    SimpleDateFormat formatoFecha = new SimpleDateFormat();
	    formatoFecha.applyPattern("ddMMyyyy");
	    
	    if(emite.getInfEmisor().getTipoComprobante().equals("06"))
	    {
	    	/*Calendar calendario = GregorianCalendar.getInstance();
	    	Date fecha = calendario.getTime();
	    	SimpleDateFormat formatoDeFecha = new SimpleDateFormat("ddMMyyyy");
	    	fecha_clave = formatoDeFecha.format(fecha).toString();*/
	    	fecha_clave = emite.getInfEmisor().getFechaIniTransp().replace("/", "");
	    }
	    else
	    	fecha_clave = emite.getInfEmisor().getFecEmision().replace("/", "");
	    
	    if (fecha_clave.length()!=8){
	    	throw new Exception("generarClaveAcceso::FechaEmision Formato Invalido:"+fecha_clave);
	    }
	    
	    String clave = fecha_clave + emite.getInfEmisor().getTipoComprobante() + emite.getInfEmisor().getRuc() + 
	    emite.getInfEmisor().getAmbiente() + emite.getInfEmisor().getCodEstablecimiento() + 
	    emite.getInfEmisor().getCodPuntoEmision() + emite.getInfEmisor().getSecuencial() + codigoNumerico + emite.getInfEmisor().getTipoEmision();
	    clave = digitoVerificador(clave);    
	    
	    
	    System.out.println("-- FIN ModifyDocumentAcceso.generarClaveAcceso --");
	    
	    return clave;
	  }
	
	public static String digitoVerificador(String number)
	{
		    int Sum = 0;
		    int i = number.length() - 1; for (int Multiplier = 2; i >= 0; i--)
		    {
		      Sum += Integer.parseInt(number.substring(i, i + 1)) * Multiplier;
		      Multiplier++; if (Multiplier == 8) Multiplier = 2;
		    }
		    int Validator = 11 - Sum % 11;

		    if (Validator == 11) Validator = 0;
		    else if (Validator == 10) Validator = 1;

		    return number + Validator;
	}
}
