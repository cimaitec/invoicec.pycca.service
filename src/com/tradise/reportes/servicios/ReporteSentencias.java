package com.tradise.reportes.servicios;
 
import java.io.IOException;
import java.sql.Connection;
//import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.NamingException;
import javax.swing.JOptionPane;

import com.sun.DAO.Destinatarios;
import com.sun.DAO.InformacionAdicional;
import com.sun.directory.examples.ServiceData;
import com.tradise.reportes.entidades.DetalleGuiaRemision;
import com.tradise.reportes.entidades.FacCabDocumento;
import com.tradise.reportes.entidades.FacDetAdicional;
import com.tradise.reportes.entidades.FacDetDocumento;
import com.tradise.reportes.entidades.FacDetMotivosdebito;
import com.tradise.reportes.entidades.FacDetRetencione;
 
public class ReporteSentencias
{
	
	public String insertFacCabDocumentos(FacCabDocumento cabDoc)
	{
		String resultado = "";
		if (ServiceData.databaseMotor.equals("PostgreSQL")){
			resultado = insertFacCabDocumentosPostgreSQL(cabDoc);
		}
		if (ServiceData.databaseMotor.equals("SQLServer")){
			resultado = insertFacCabDocumentosSQLServer(cabDoc);
		}
		 //resultado = insertFacCabDetAdicional(cabDoc);
			return resultado;
	 }
	 
	public String insertFacCabDetAdicional(FacCabDocumento cabDoc)
	{
		String resultado = "";
		if (ServiceData.databaseMotor.equals("PostgreSQL")){
			resultado = insertFacCabDetAdicionalSQLServer(cabDoc);
		}
		if (ServiceData.databaseMotor.equals("SQLServer")){
			resultado = insertFacCabDetAdicionalSQLServer(cabDoc);
		}
		return resultado;
	}
	 
	public String insertFacCabDetAdicionalSQLServer(FacCabDocumento cabDoc)
	{
		ArrayList<InformacionAdicional> listInfAdicional = cabDoc.getListInfAdicional();
		PreparedStatement ps = null;
	    Connection con = null;
	    String msg = "";     
	    String documento = "::Ruc::"+cabDoc.getRuc()+"::CodEstablecimiento::"+ cabDoc.getCodEstablecimiento()+"::PuntoEmision::"+ 
	    cabDoc.getCodPuntEmision()+"::Secuencial::"+cabDoc.getSecuencial()+"::TipoDocumento::"+ cabDoc.getCodigoDocumento();
	    String sql = "delete from fac_cab_documentos_adicional where Ruc=?  and CodEstablecimiento=? and CodPuntoEmision=? and secuencial=? and CodigoDocumento=? ";
	    try {
	    	con = Conexion.conectar();
	    	ps = con.prepareStatement(sql);
	    	ps.setString(1, cabDoc.getRuc());
	    	ps.setString(2, cabDoc.getCodEstablecimiento());
	    	ps.setString(3, cabDoc.getCodPuntEmision());
	    	ps.setString(4, cabDoc.getSecuencial());
	    	ps.setString(5, cabDoc.getCodigoDocumento());
	    	int r = ps.executeUpdate();
	     
	    	if (r > 0) 
	    		msg = "Se Elimino correctamente los detalles del documento->"+documento;
	    	else
	    		msg = "Ocurrio un error al eliminar los detalles del documento->"+documento;

	     	} catch (Exception e) {
	     		System.out.println(e);
		        System.out.println(e.getMessage());
		        System.out.println(e.getCause());
		        System.out.println(e.getLocalizedMessage());
		        System.out.println(e.getStackTrace());
		        e.printStackTrace();       
	     	}
	    finally {
	    	try {
	    		if (ps != null)
	    			ps.close();
	            if (con != null)
	            	con.close();
	    	} catch (Exception exc) {
	    		throw new RuntimeException(exc);
	    	}
	    }
	    
	    msg = "";
	    sql = "INSERT INTO fac_det_documentos (Ruc, CodEstablecimiento, CodPuntoEmision, secuencial, CodigoDocumento," +
	     		" nombre, descripcion)  " +
	     		"VALUES (?, ?, ?, ?, ?, ?, ?)";
	    try {
	    	con = Conexion.conectar();
	    	for (int i = 0; i < cabDoc.getListInfAdicional().size(); i++) {
	    		ps = con.prepareStatement(sql);
		        ps.setString(1, cabDoc.getRuc());
		        ps.setString(2, cabDoc.getCodEstablecimiento());
		        ps.setString(3, cabDoc.getCodPuntEmision());
		        ps.setString(4, cabDoc.getSecuencial());
		        ps.setString(5, cabDoc.getCodigoDocumento());
		        ps.setString(6, cabDoc.getListInfAdicional().get(i).getName());
		        ps.setString(7, cabDoc.getListInfAdicional().get(i).getValue());
		        
		        int r = ps.executeUpdate();
		        
		        if (r > 0)
		        	msg = "Se Guardo correctamente los detalles del documento->"+documento;
		        else
		        	msg = "Ocurrio un error al Guardar los detalles del documento->"+documento;
	    	}
	    } catch (Exception e) {
	    	System.out.println(e);
	        System.out.println(e.getMessage());
	        System.out.println(e.getCause());
	        System.out.println(e.getLocalizedMessage());
	        System.out.println(e.getStackTrace());
	        e.printStackTrace();
	    } finally {
	    	try {
	    		if (ps != null)
	    			ps.close();
	    		if (con != null)
	    			con.close();
	    	} catch (Exception exc) {
	    		throw new RuntimeException(exc);
	    	}
	    }
	    return msg;
	}
	 
	public String insertFacCabDocumentosSQLServer(FacCabDocumento cabDoc)
	{
		Connection con = null;
		PreparedStatement ps = null;
		int r;
		int i = 0;    
		String documento = "::Ruc::"+cabDoc.getRuc()+"::CodEstablecimiento::"+ cabDoc.getCodEstablecimiento()+"::PuntoEmision::"+ cabDoc.getCodPuntEmision()+"::Secuencial::"+ cabDoc.getSecuencial()+"::TipoDocumento::"+ cabDoc.getCodigoDocumento();
		String msg = "";
		String sql = "delete from fac_cab_documentos " +
     		      " where ambiente=? and \"Ruc\"=? " +
     		      " and \"CodEstablecimiento\"=? " +
     		      " and \"CodPuntEmision\"=? " +
     		      " and secuencial=? " +
     		      " and \"CodigoDocumento\"=? ";
     
		try
		{
			con = Conexion.conectar();
			ps = con.prepareStatement(sql);	     
			i++;
		    ps.setInt(i, cabDoc.getAmbiente().intValue());
		    i++;
		    ps.setString(i, cabDoc.getRuc());	     
		    i++;
		    ps.setString(i, cabDoc.getCodEstablecimiento());
		    i++;
		    ps.setString(i, cabDoc.getCodPuntEmision());
		    i++;
		    ps.setString(i, cabDoc.getSecuencial());
		    i++;
		    ps.setString(i, cabDoc.getCodigoDocumento());
		    r = ps.executeUpdate();
		} catch (Exception e1) {
			e1.printStackTrace();
			r = 0;
		}
		if (r > 0)
			msg = "Se Guardo Correctamente el documento->"+documento ;
	    else
	    	msg = "Ocurrio un error al ingresar el documento->"+documento;
	     
	    msg = "";
	     
	    sql = "INSERT INTO fac_cab_documentos(ambiente,Ruc,CodEstablecimiento,CodPuntEmision,secuencial,CodigoDocumento,tipIdentificacionComprador,guiaRemision,razonSocialComprador,identificacionComprador," +
     									   "totalSinImpuesto,totalDescuento,email,propina,moneda,infoAdicional,periodoFiscal,rise,placa,motivoRazon," +
     									   "identificacionDestinatario,razonSocialDestinatario,direccionDestinatario,docAduaneroUnico,codEstablecimientoDest,ruta,codDocSustento,numDocSustento,numAutDocSustento,motivoTraslado," +
     									   "autorizacion,claveAcceso,importeTotal,codDocModificado,numDocModificado,motivoValor,tipoEmision,partida,subtotal12,subtotal0," +
     									   "subtotalNoIva,totalvalorICE,iva12,isActive,ESTADO_TRANSACCION,MSJ_ERROR,fechaEmision,fechaInicioTransporte,fechaFinTransporte,fechaEmisionDocSustento, fecEmisionDocSustento" + 
     									   "" +
		      ")VALUES(?,?,?,?,?,?,?,?,?,?" +
		      "       ,?,?,?,?,?,?,?,?,?,?" +
		      "       ,?,?,?,?,?,?,?,?,?,?" +
		      "       ,?,?,?,?,?,?,?,?,?,?" +
		      "       ,?,?,?,?,?,?";
     
         String ls_sql_adicional="";
         System.out.println("cabDoc.getFechaEmision::"+cabDoc.getFechaEmision());
         if (cabDoc.getFechaEmision()!= null){
        	 ls_sql_adicional = ls_sql_adicional + ",cast(? as date)";
         }else{
        	 ls_sql_adicional = ls_sql_adicional + ",NULL";
         }
     
         //ps.setString(1,sqlToday);
     
         System.out.println("cabDoc.getFechaInicioTransporte::"+cabDoc.getFechaInicioTransporte());
         if (cabDoc.getFechaInicioTransporte()!= null){    	 
        	 ls_sql_adicional = ls_sql_adicional + ",cast(? as date) ";
         }else{
        	 ls_sql_adicional = ls_sql_adicional + ",NULL";	   
         }
         System.out.println("cabDoc.getFechaFinTransporte::"+cabDoc.getFechaFinTransporte());
         if (cabDoc.getFechaFinTransporte()!= null){
        	 ls_sql_adicional = ls_sql_adicional + ",cast(? as date)";
         }else{
        	 ls_sql_adicional = ls_sql_adicional + ",NULL";
     }
     System.out.println("	cabDoc.getFechaEmisionDocSustento::"+cabDoc.getFechaEmisionDocSustento());
     if (cabDoc.getFechaEmisionDocSustento()!= null){
    	 ls_sql_adicional = ls_sql_adicional + ",? ";
     }else{
    	 ls_sql_adicional = ls_sql_adicional + ",NULL";
     }   
     
     System.out.println("   cabDoc.getFechaEmisionDocSustento::"+cabDoc.getFechaEmisionDocSustento());
     if (cabDoc.getFechaEmision()!= null){    	 
    	 ls_sql_adicional = ls_sql_adicional + ",?";
     }else{
    	 ls_sql_adicional = ls_sql_adicional + ",NULL";	   
     }
     
     sql = sql + ls_sql_adicional +")";
     
     int year=0, month=0, idx=0; 
     try {
       con = Conexion.conectar();
       ps = con.prepareStatement(sql);
       i = 0;
       //ambiente,Ruc,CodEstablecimiento,CodPuntEmision,secuencial,CodigoDocumento
       i++;
       System.out.println("cabDoc.getAmbiente::"+cabDoc.getAmbiente());
       ps.setInt(i, cabDoc.getAmbiente().intValue());
       i++;
       System.out.println("cabDoc.getRuc::"+cabDoc.getRuc());
       ps.setString(i, cabDoc.getRuc());
       i++;
       System.out.println("cabDoc.getCodEstablecimiento::"+cabDoc.getCodEstablecimiento());
       ps.setString(i, cabDoc.getCodEstablecimiento());
       i++;
       System.out.println("cabDoc.getCodPuntEmision::"+cabDoc.getCodPuntEmision());
       ps.setString(i, cabDoc.getCodPuntEmision());
       i++;
       System.out.println("cabDoc.getSecuencial::"+cabDoc.getSecuencial());
       ps.setString(i, cabDoc.getSecuencial());
       i++;
       System.out.println("cabDoc.getCodigoDocumento::"+cabDoc.getCodigoDocumento());
       ps.setString(i, cabDoc.getCodigoDocumento());
       //identificacionComprador,guiaRemision,razonSocialComprador
       i++;
       System.out.println("cabDoc.getTipIdentificacionComprador::"+cabDoc.getTipoIdentificacion());
       ps.setString(i, String.valueOf(cabDoc.getTipoIdentificacion()));
       i++;
       System.out.println("cabDoc.getGuiaRemision::"+cabDoc.getGuiaRemision());
       ps.setString(i, cabDoc.getGuiaRemision());
       i++;
       System.out.println("cabDoc.getRazonSocialComprador::"+cabDoc.getRazonSocialComprador());
       ps.setString(i, cabDoc.getRazonSocialComprador().trim());
       i++;
       System.out.println("cabDoc.getIdentificacionComprador::"+cabDoc.getIdentificacionComprador());
       ps.setString(i, cabDoc.getIdentificacionComprador());
       //totalSinImpuesto,totalDescuento,email,propina
       i++;
       System.out.println("cabDoc.getTotalSinImpuesto::"+cabDoc.getTotalSinImpuesto());
       ps.setDouble(i, cabDoc.getTotalSinImpuesto());
       i++;
       System.out.println("cabDoc.getTotalDescuento::"+cabDoc.getTotalDescuento());
       ps.setDouble(i, cabDoc.getTotalDescuento());
       i++;
       System.out.println("cabDoc.getEmail::"+cabDoc.getEmail());
       ps.setString(i, cabDoc.getEmail());
       i++;
       System.out.println("cabDoc.getPropina::"+cabDoc.getPropina());
       ps.setDouble(i, cabDoc.getPropina());
       //moneda,infoAdicional,periodoFiscal,rise
       i++;
       System.out.println("cabDoc.getMoneda::"+cabDoc.getMoneda());
       if (cabDoc.getMoneda()!= null){ 
    	   ps.setString(i, cabDoc.getMoneda().toString());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
       }
       i++;
       System.out.println("cabDoc.getInfoAdicional::"+(cabDoc.getInfoAdicional().length()>300?cabDoc.getInfoAdicional().substring(1,300):cabDoc.getInfoAdicional()));
       ps.setString(i, (cabDoc.getInfoAdicional().length()>300?cabDoc.getInfoAdicional().substring(1,300):cabDoc.getInfoAdicional()));
       i++;
       System.out.println("cabDoc.getPeriodoFiscal::"+cabDoc.getPeriodoFiscal());
       ps.setString(i, cabDoc.getPeriodoFiscal());
       i++;
       System.out.println("cabDoc.getRise::"+cabDoc.getRise());
       if (cabDoc.getRise()!= null){    	   
    	   ps.setString(i, cabDoc.getRise().toString());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR); 
       }
       //placa,motivoRazon,identificacionDestinatario,razonSocialDestinatario
       i++;
       System.out.println("cabDoc.getPlaca::"+cabDoc.getPlaca());
       if (cabDoc.getPlaca()!= null){
    	   ps.setString(i, cabDoc.getPlaca());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
       }
       i++;
       System.out.println("cabDoc.getMotivoRazon::"+cabDoc.getMotivoRazon());
       if (cabDoc.getMotivoRazon()!= null){
    	   ps.setString(i, cabDoc.getMotivoRazon());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
       }
       i++;
       System.out.println("cabDoc.getIdentificacionDestinatario::"+cabDoc.getIdentificacionDestinatario());
       if (cabDoc.getIdentificacionDestinatario()!= null){
    	   ps.setString(i, cabDoc.getIdentificacionDestinatario());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
       }
       i++;
       System.out.println("cabDoc.getRazonSocialDestinatario::"+cabDoc.getRazonSocialDestinatario());
       if (cabDoc.getRazonSocialDestinatario()!= null){
    	   ps.setString(i, cabDoc.getRazonSocialDestinatario());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
       }
       //direccionDestinatario,docAduaneroUnico,codEstablecimientoDest,ruta,codDocSustento,numDocSustento
       i++;
       System.out.println("cabDoc.getDireccionDestinatario::"+cabDoc.getDireccionDestinatario());
       if (cabDoc.getDireccionDestinatario()!= null){
    	   ps.setString(i, cabDoc.getDireccionDestinatario());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
       }
       i++;
       System.out.println("cabDoc.getDocAduaneroUnico::"+cabDoc.getDocAduaneroUnico());
       if (cabDoc.getDocAduaneroUnico()!= null){
    	   ps.setString(i, cabDoc.getDocAduaneroUnico());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
       }
       i++;
       System.out.println("cabDoc.getCodEstablecimientoDest::"+cabDoc.getCodEstablecimientoDest());
       if (cabDoc.getCodEstablecimientoDest()!= null){
    	   ps.setString(i, cabDoc.getCodEstablecimientoDest());
	   }else{
		   ps.setNull(i, java.sql.Types.VARCHAR);
	   }
       i++;
       System.out.println("cabDoc.getRuta::"+cabDoc.getRuta());
       if (cabDoc.getRuta()!= null){
    	   ps.setString(i, cabDoc.getRuta());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
	   }
       i++;
       System.out.println("cabDoc.getCodDocSustento::"+cabDoc.getCodDocSustento());
       if (cabDoc.getCodDocSustento()!= null){
    	   ps.setString(i, cabDoc.getCodDocSustento());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
	   }
       i++;
       System.out.println("cabDoc.getNumDocSustento::"+cabDoc.getNumDocSustento());
       if (cabDoc.getNumDocSustento()!= null){
    	   ps.setString(i, cabDoc.getNumDocSustento());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
	   }
       //numAutDocSustento,motivoTraslado,autorizacion,claveAcceso,importeTotal,codDocModificado
       i++;
       System.out.println("cabDoc.getNumAutDocSustento::"+cabDoc.getNumAutDocSustento());
       if (cabDoc.getNumAutDocSustento()!= null){
    	   ps.setString(i, cabDoc.getNumAutDocSustento());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
	   }
       i++;
       System.out.println("cabDoc.getMotivoTraslado::"+cabDoc.getMotivoTraslado());
       if (cabDoc.getMotivoTraslado()!= null){
    	   ps.setString(i, cabDoc.getMotivoTraslado());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
	   }
       i++;
       System.out.println("cabDoc.getAutorizacion::"+cabDoc.getAutorizacion());
       if (cabDoc.getAutorizacion()!= null){
    	   ps.setString(i, cabDoc.getAutorizacion());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
       }
       i++;
       System.out.println("cabDoc.getClaveAcceso::"+cabDoc.getClaveAcceso());
       if (cabDoc.getClaveAcceso()!= null){
    	   ps.setString(i, cabDoc.getClaveAcceso());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
       }
       i++;
       System.out.println("cabDoc.getImporteTotal::"+cabDoc.getImporteTotal());
       if (cabDoc.getImporteTotal()>=0){
    	   ps.setDouble(i, cabDoc.getImporteTotal());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
       }
       i++;
       System.out.println("cabDoc.getCodDocModificado::"+cabDoc.getCodDocModificado());
       if (cabDoc.getCodDocModificado()!= null){
    	   ps.setString(i, cabDoc.getCodDocModificado());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
       }
       //numDocModificado,motivoValor,tipoEmision,partida,subtotal12,subtotal0,subtotalNoIva
       i++;
       System.out.println("cabDoc.getNumDocModificado::"+cabDoc.getNumDocModificado());
       if (cabDoc.getNumDocModificado()!= null){
    	   ps.setString(i, cabDoc.getNumDocModificado());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
       }
       i++;
       System.out.println("cabDoc.getMotivoValor::"+cabDoc.getMotivoValor());
       if (cabDoc.getMotivoValor()>=0){
    	   ps.setDouble(i, cabDoc.getMotivoValor());
       }else{
    	   ps.setNull(i, java.sql.Types.DOUBLE);
       }
       i++;
       System.out.println("cabDoc.getTipoEmision::"+cabDoc.getTipoEmision());
       if (cabDoc.getTipoEmision()!= null){
    	   ps.setString(i, cabDoc.getTipoEmision());
	   }else{
		   ps.setNull(i, java.sql.Types.VARCHAR);
	   }
       i++;
       System.out.println("cabDoc.getPartida::"+cabDoc.getPartida());
       if (cabDoc.getPartida()!= null){
    	   ps.setString(i, cabDoc.getPartida());
       }else{
    	   ps.setNull(i, java.sql.Types.VARCHAR);
	   }
       i++;
       System.out.println("cabDoc.getImporteTotal::"+cabDoc.getImporteTotal());
       if (cabDoc.getImporteTotal()>=0){
    	   ps.setDouble(i, cabDoc.getSubtotal12());
       }else{
    	   ps.setNull(i, java.sql.Types.DOUBLE);
       }
       i++;
       System.out.println("cabDoc.getSubtotal0::"+cabDoc.getSubtotal0());
       if (cabDoc.getSubtotal0()>=0){
    	   ps.setDouble(i, cabDoc.getSubtotal0());
       }else{
    	   ps.setNull(i, java.sql.Types.DOUBLE);
       }
       i++;
       System.out.println("cabDoc.getSubtotalNoIva::"+cabDoc.getSubtotalNoIva()); 
       if (cabDoc.getSubtotalNoIva()>=0){
    	   ps.setDouble(i, cabDoc.getSubtotalNoIva());
	   }else{
		   ps.setNull(i, java.sql.Types.DOUBLE);
	   }
       
       //totalvalorICE,iva12,isActive,ESTADO_TRANSACCION,MSJ_ERROR
       
       i++;
       System.out.println("cabDoc.getTotalvalorICE::"+cabDoc.getTotalvalorICE());
       if (cabDoc.getTotalvalorICE()>=0){
    	   ps.setDouble(i, cabDoc.getTotalvalorICE());
       }else{
    	   ps.setNull(i, java.sql.Types.DOUBLE);
	   }
       i++;
       System.out.println("cabDoc.getIva12::"+cabDoc.getIva12());
       if (cabDoc.getIva12()>=0){
    	   ps.setDouble(i, cabDoc.getIva12());
       }else{
    	   ps.setNull(i, java.sql.Types.DOUBLE);
	   }
       i++;
       System.out.println("i::Y");
       ps.setString(i, "Y");
       i++;
       System.out.println("cabDoc.getESTADO_TRANSACCION::"+cabDoc.getESTADO_TRANSACCION());
       ps.setString(i, cabDoc.getESTADO_TRANSACCION());
       i++;
       System.out.println("cabDoc.getMSJ_ERROR::"+cabDoc.getMSJ_ERROR());
       ps.setString(i, cabDoc.getMSJ_ERROR());
       
       
       String fecha = cabDoc.getFechaEmision().substring(6, 10) + "-" + cabDoc.getFechaEmision().substring(3, 5)+"-"+cabDoc.getFechaEmision().substring(0, 2);
       //cal.set(arg0, arg1, arg2);
       System.out.println("cabDoc.getFechaEmision::"+cabDoc.getFechaEmision());
       if (cabDoc.getFechaEmision()!= null){
    	   i++;
           ps.setString(i, fecha);
       }else{
    	   ;;
    	   //ps.setNull(i, java.sql.Types.DATE);    	   
       }
       i++;
       System.out.println("cabDoc.getFechaInicioTransporte::"+cabDoc.getFechaInicioTransporte());
       if (cabDoc.getFechaInicioTransporte()!= null){
    	  
    	   ps.setString(i, cabDoc.getFechaInicioTransporte());
       }else{
    	   ;;
    	   //ps.setNull(i, java.sql.Types.DATE);    	   
       }
       
       System.out.println("cabDoc.getFechaFinTransporte::"+cabDoc.getFechaFinTransporte());
       if (cabDoc.getFechaFinTransporte()!= null){
    	   i++;
           ps.setString(i, cabDoc.getFechaFinTransporte());
       }else{
    	  ;; // ps.setNull(i, java.sql.Types.DATE);
       }
      
       System.out.println("cabDoc.getFechaEmisionDocSustento::"+cabDoc.getFecEmisionDocSustento());
       if (cabDoc.getFecEmisionDocSustento()!= null){
    	   i++;
           ps.setString(i, cabDoc.getFecEmisionDocSustento());
       }else{
    	  ;;// ps.setNull(i, java.sql.Types.VARCHAR);    	   
       }
       i++;
       System.out.println("cabDoc.getFechaEmisionDocSustento::"+cabDoc.getFechaEmisionDocSustento());
       if (cabDoc.getFechaEmisionDocSustento()!= null){    	   
           ps.setDate(i, new java.sql.Date(cabDoc.getFechaEmisionDocSustento().getTime()));
       }else{    	   
    	   ps.setNull(i, java.sql.Types.VARCHAR);    	   
       }
       r = ps.executeUpdate();
       
       if (r > 0)
         msg = "Se Guardo Correctamente " ;
       else
         msg = "Ocurrio un error al ingresar el documento ";
     } catch (Exception e) {
    	 e.printStackTrace();
       String str1 = msg = null; return str1;
     } finally {
       try {
         if (ps != null)
           ps.close();
         if (con != null)
           con.close();
       } catch (Exception exc) {
         throw new RuntimeException(exc);
       }
     }
     return msg;
   }

   public int insertInfoAdicional(com.sun.businessLogic.validate.Emisor emite)
   {
	   System.out.println("-- INICIO ReporteSentencias.insertInfoAdicional --");
	   Connection con = null;
	   PreparedStatement ps = null;
	   int r = 0;
	   int i = 0;   
	   String documento = "::Ruc::"+emite.getInfEmisor().getRuc()+
	    		 			"::CodEstablecimiento::"+ emite.getInfEmisor().getCodEstablecimiento()+
	    		 			"::PuntoEmision::"+ emite.getInfEmisor().getCodPuntoEmision()+
	    		 			"::Secuencial::"+ emite.getInfEmisor().getSecuencial()+
	    		 			"::TipoDocumento::"+ emite.getInfEmisor().getCodDocumento()+
	    		 			"::Ambiente::"+ emite.getInfEmisor().getAmbiente();
	   String msg = "";
	   String sql = "delete from fac_det_adicional " +
	     		      " where ambiente=? and \"Ruc\"=? " +
	     		      " and \"CodEstablecimiento\"=? " +
	     		      " and \"CodPuntEmision\"=? " +
	     		      " and secuencial=? " +
	     		      " and \"CodigoDocumento\"=? ";
	   
	   if (emite.getInfEmisor().getListInfAdicional().size()>0)
	   {
		   try
		   {
			   con = Conexion.conectar();
			   ps = con.prepareStatement(sql);
			   i++;
			   ps.setInt(i, emite.getInfEmisor().getAmbiente());
			   i++;
			   ps.setString(i, emite.getInfEmisor().getRuc());
			   i++;
			   ps.setString(i, emite.getInfEmisor().getCodEstablecimiento());
			   i++;
			   ps.setString(i, emite.getInfEmisor().getCodPuntoEmision());
			   i++;
			   ps.setString(i, emite.getInfEmisor().getSecuencial());
			   i++;
			   ps.setString(i, emite.getInfEmisor().getCodDocumento());
			   r = ps.executeUpdate();
		   } catch (Exception e) {
			   System.out.println(e);
		       System.out.println(e.getMessage());
		       System.out.println(e.getCause());
		       System.out.println(e.getLocalizedMessage());
		       System.out.println(e.getStackTrace());
		       e.printStackTrace();
			   r = 0;
		   } finally {
		       try {
		           if (ps != null)
		             ps.close();
		           if (con != null)
		             con.close();
		         } catch (Exception exc) {
		           throw new RuntimeException(exc);
		         }
		   }
		   if(r > 0)
			   msg = "Se Guardo Correctamente el documento->"+documento ;
		   else
			   msg = "Ocurrio un error al ingresar el documento->"+documento;
    
	    
		   sql = " insert into fac_det_adicional  (ambiente," +
		    											 "\"Ruc\"," +
		    											 "\"CodEstablecimiento\"," +
		    											 "\"CodPuntEmision\"," +
		    											 "secuencial," +
		    											 "\"CodigoDocumento\"," +
		    											 "\"secuencialDetalle\"," +
		    											 " \"secuencialDetAdicional\", nombre, valor)" +
		     		      " values(?,?,?,?,?,?,?,?,?,?) ";
		 
		    
		   try
		   {
			   for (int j=0; j<emite.getInfEmisor().getListInfAdicional().size();j++)
		 	    {
		    	 con = Conexion.conectar();
					ps = con.prepareStatement(sql);	    
					i = 0;
				     i++;
				     ps.setInt(i, emite.getInfEmisor().getAmbiente());
				     i++;
				     ps.setString(i, emite.getInfEmisor().getRuc());	     
				     i++;
				     ps.setString(i, emite.getInfEmisor().getCodEstablecimiento());
				     i++;
				     ps.setString(i, emite.getInfEmisor().getCodPuntoEmision());
				     i++;
				     ps.setString(i, emite.getInfEmisor().getSecuencial());
				     i++;
				     ps.setString(i, emite.getInfEmisor().getCodDocumento());			     
				     i++;
				     ps.setInt(i, j+1);
				     i++;
				     ps.setInt(i, j+1);
				     i++;
				     ps.setString(i, emite.getInfEmisor().getListInfAdicional().get(j).getName());
				     i++;
				     ps.setString(i, capitalizeString(emite.getInfEmisor().getListInfAdicional().get(j).getValue()));
				     r = ps.executeUpdate();
		 	    }
				} catch (Exception e) {
					System.out.println(e);
			        System.out.println(e.getMessage());
			        System.out.println(e.getCause());
			        System.out.println(e.getLocalizedMessage());
			        System.out.println(e.getStackTrace());
			        e.printStackTrace();
					r = 0;
				} finally {
				       try {
				           if (ps != null)
				             ps.close();
				           if (con != null)
				             con.close();
				         } catch (Exception exc) {
				           throw new RuntimeException(exc);
				         }
				       }
			     if (r > 0)
			       msg = "Se Guardo Correctamente el documento->"+documento ;
			     else
			       msg = "Ocurrio un error al ingresar el documento->"+documento;
	     }	     
	     System.out.println("-- FIN ReporteSentencias.insertInfoAdicional --");
	     return r;
   }
   
   public String insertFacCabDocumentosPostgreSQL(FacCabDocumento cabDoc)
   {
     Connection con = null;
     PreparedStatement ps = null;
     int r;
     int i = 0;    
     String documento = "::Ruc::"+cabDoc.getRuc()+"::CodEstablecimiento::"+ cabDoc.getCodEstablecimiento()+"::PuntoEmision::"+ cabDoc.getCodPuntEmision()+"::Secuencial::"+ cabDoc.getSecuencial()+"::TipoDocumento::"+ cabDoc.getCodigoDocumento();
     String msg = "";
     String sql = "delete from fac_cab_documentos " +
     		      " where ambiente=? and \"Ruc\"=? " +
     		      " and \"CodEstablecimiento\"=? " +
     		      " and \"CodPuntEmision\"=? " +
     		      " and secuencial=? " +
     		      " and \"CodigoDocumento\"=? ";
     
		 try
		 {
			 System.out.println("-- INICIO ReporteSentencias.insertFacCabDocumentosPostgreSQL --");
			 con = Conexion.conectar();
			 ps = con.prepareStatement(sql);
			 i++;
			 ps.setInt(i, cabDoc.getAmbiente().intValue());
			 i++;
			 ps.setString(i, cabDoc.getRuc());
			 i++;
			 ps.setString(i, cabDoc.getCodEstablecimiento());
			 i++;
			 ps.setString(i, cabDoc.getCodPuntEmision());
			 i++;
			 ps.setString(i, cabDoc.getSecuencial());
			 i++;
			 ps.setString(i, cabDoc.getCodigoDocumento());
			 r = ps.executeUpdate();
		 }
		 catch (Exception e)
		 {
			 System.out.println(e);
	         System.out.println(e.getMessage());
	         System.out.println(e.getCause());
	         System.out.println(e.getLocalizedMessage());
	         System.out.println(e.getStackTrace());
	         e.printStackTrace();
			 r = 0;
		 }     
		 if (r > 0)
			 msg = "Se Guardo Correctamente el documento->"+documento ;
		 else
			 msg = "Ocurrio un error al ingresar el documento->"+documento;
		 
		 msg = "";
		 sql = "INSERT INTO fac_cab_documentos" +
     			"(ambiente, \"Ruc\", \"identificacionComprador\", \"CodEstablecimiento\", \"CodPuntEmision\", secuencial, \"CodigoDocumento\"," +
     			  "\"guiaRemision\", \"razonSocialComprador\"," +
     		      "\"tipIdentificacionComprador\",\"totalSinImpuesto\", \"totalDescuento\", email, propina, moneda, \"infoAdicional\"," +
     		      "\"periodoFiscal\",rise,placa,\"motivoRazon\",\"identificacionDestinatario\",\"razonSocialDestinatario\"," +
     		      "\"direccionDestinatario\",\"docAduaneroUnico\",\"codEstablecimientoDest\",ruta,\"codDocSustento\",\"numDocSustento\",\"numAutDocSustento\",\"motivoTraslado\"," +
     		      "autorizacion,\"claveAcceso\" ,\"importeTotal\",\"codDocModificado\",\"numDocModificado\",\"motivoValor\",\"tipoEmision\",partida,subtotal12,subtotal0,\"subtotalNoIva\"," +
     		      "\"totalvalorICE\",iva12,\"isActive\",\"ESTADO_TRANSACCION\",\"MSJ_ERROR\"," +
     		      "\"fechaEmision\",\"fechaInicioTransporte\",\"fechaFinTransporte\",\"fechaEmisionDocSustento\" " +
     		      //",\"fechaautorizacion\"" +
     		      ")" +
     		      "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?,?,?,?,?,?";
		 String ls_sql_adicional="";
		 //Ruc", "CodEstablecimiento", "CodPuntEmision", secuencial, "CodigoDocumento"
		 if (cabDoc.getFechaEmision()!= null){
			 ls_sql_adicional = ls_sql_adicional + ",to_date(?,'DD/MM/YYYY')";
		 }else{
			 ls_sql_adicional = ls_sql_adicional + ",?";
		 }
		 if (cabDoc.getFechaInicioTransporte()!= null){
			 ls_sql_adicional = ls_sql_adicional + ",to_date(?,'DD/MM/YYYY')";
		 }else{
			 ls_sql_adicional = ls_sql_adicional + ",?";
		 }
		 if (cabDoc.getFechaFinTransporte()!= null){
			 ls_sql_adicional = ls_sql_adicional + ",to_date(?,'DD/MM/YYYY')";
		 }else{
			 ls_sql_adicional = ls_sql_adicional + ",?";
		 }
		 if (cabDoc.getFechaEmisionDocSustento()!= null){
			 ls_sql_adicional = ls_sql_adicional + ",to_date(?,'DD/MM/YYYY')";
		 }else{
			 ls_sql_adicional = ls_sql_adicional + ",?";
		 }
		 sql = sql + ls_sql_adicional +")";
		 int year=0, month=0, idx=0;
		 try
		 {
			 con = Conexion.conectar();
			 ps = con.prepareStatement(sql);
			 i = 0;
			 i++;
			 ps.setInt(i, cabDoc.getAmbiente().intValue());
			 i++;
			 ps.setString(i, cabDoc.getRuc());
			 i++;
			 //ps.setString(i, String.valueOf(cabDoc.getTipIdentificacionComprador())); // HFU
			 ps.setString(i, String.valueOf(cabDoc.getIdentificacionComprador()));	  // HFU
			 i++;
			 ps.setString(i, cabDoc.getCodEstablecimiento());
			 i++;
			 ps.setString(i, cabDoc.getCodPuntEmision());
	       i++;
	       ps.setString(i, cabDoc.getSecuencial());
	       i++;
	       ps.setString(i, cabDoc.getCodigoDocumento());
	       i++;
	       ps.setString(i, cabDoc.getGuiaRemision());
	       i++;
	       ps.setString(i, (cabDoc.getRazonSocialComprador()==null)?null:cabDoc.getRazonSocialComprador().trim());
	       i++;
	       ps.setString(i, cabDoc.getTipIdentificacionComprador());
	       i++;
	       ps.setDouble(i, cabDoc.getTotalSinImpuesto());
	       i++;
	       ps.setDouble(i, cabDoc.getTotalDescuento());
	       i++;
	       ps.setString(i, cabDoc.getEmail());
	       i++;
	       ps.setDouble(i, cabDoc.getPropina());
	       i++;
	       if (cabDoc.getMoneda()!= null){ 
	    	   ps.setString(i, cabDoc.getMoneda().toString());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (((cabDoc.getInfoAdicional()==null)?"":cabDoc.getInfoAdicional()).length()>300)
	    	   ps.setString(i, cabDoc.getInfoAdicional().substring(0, 300));
	       else
	    	   ps.setString(i, cabDoc.getInfoAdicional());
	       i++;
	       ps.setString(i, cabDoc.getPeriodoFiscal());
	       i++;
	       if (cabDoc.getRise()!= null){ 
	    	   ps.setString(i, cabDoc.getRise().toString());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR); 
	       }
	       i++;
	       if (cabDoc.getPlaca()!= null){
	    	   ps.setString(i, cabDoc.getPlaca());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getMotivoRazon()!= null){
	    	   ps.setString(i, cabDoc.getMotivoRazon());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getIdentificacionDestinatario()!= null){
	    	   ps.setString(i, cabDoc.getIdentificacionDestinatario());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getRazonSocialDestinatario()!= null){
	    	   ps.setString(i, cabDoc.getRazonSocialDestinatario());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getDireccionDestinatario()!= null){
	    	   ps.setString(i, cabDoc.getDireccionDestinatario());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getDocAduaneroUnico()!= null){
	    	   ps.setString(i, cabDoc.getDocAduaneroUnico());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getCodEstablecimientoDest()!= null){
	    	   ps.setString(i, cabDoc.getCodEstablecimientoDest());
		   }else{
			   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getRuta()!= null){
	    	   ps.setString(i, cabDoc.getRuta());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getCodDocSustento()!= null){
	    	   ps.setString(i, cabDoc.getCodDocSustento());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getNumDocSustento()!= null){
	    	   ps.setString(i, cabDoc.getNumDocSustento());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getNumAutDocSustento()!= null){
	    	   ps.setString(i, cabDoc.getNumAutDocSustento());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getMotivoTraslado()!= null){
	    	   ps.setString(i, cabDoc.getMotivoTraslado());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getAutorizacion()!= null){
	    	   ps.setString(i, cabDoc.getAutorizacion());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getClaveAcceso()!= null){
	    	   ps.setString(i, cabDoc.getClaveAcceso());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getImporteTotal()>=0){
	    	   ps.setDouble(i, cabDoc.getImporteTotal());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getCodDocModificado()!= null){
	    	   ps.setString(i, cabDoc.getCodDocModificado());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getNumDocModificado()!= null){
	    	   ps.setString(i, cabDoc.getNumDocModificado());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getMotivoValor()>=0){
	    	   ps.setDouble(i, cabDoc.getMotivoValor());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DOUBLE);
	       }
	       i++;
	       if (cabDoc.getTipoEmision()!= null){
	    	   ps.setString(i, cabDoc.getTipoEmision());
		   }else{
			   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getPartida()!= null){
	    	   ps.setString(i, cabDoc.getPartida());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getImporteTotal()>=0){
	    	   ps.setDouble(i, cabDoc.getSubtotal12());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DOUBLE);
	       }
	       i++;
	       if (cabDoc.getSubtotal0()>=0){
	    	   ps.setDouble(i, cabDoc.getSubtotal0());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DOUBLE);
	       }
	       i++;
	       if (cabDoc.getSubtotalNoIva()>=0){
	    	   ps.setDouble(i, cabDoc.getSubtotalNoIva());
		   }else{
			   ps.setNull(i, java.sql.Types.DOUBLE);
		   }
	       i++;
	       if (cabDoc.getTotalvalorICE()>=0){
	    	   ps.setDouble(i, cabDoc.getTotalvalorICE());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DOUBLE);
		   }
	       i++;
	       if (cabDoc.getIva12()>=0){
	    	   ps.setDouble(i, cabDoc.getIva12());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DOUBLE);
		   }
	       i++;
	       ps.setString(i, "Y");
	       i++;
	       ps.setString(i, cabDoc.getESTADO_TRANSACCION());
	       i++;
	       ps.setString(i, cabDoc.getMSJ_ERROR());
	       i++;
	       if (cabDoc.getFechaEmision()!= null){
	           ps.setString(i, cabDoc.getFechaEmision());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DATE);    	   
	       }
	       i++;
	       if (cabDoc.getFechaInicioTransporte()!= null){
	    	   ps.setString(i, cabDoc.getFechaInicioTransporte());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DATE);    	   
	       }
	       i++;
	       if (cabDoc.getFechaFinTransporte()!= null){
	           ps.setString(i, cabDoc.getFechaFinTransporte());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DATE);
	       }
	       i++;
	       if (cabDoc.getFechaEmisionDocSustento()!= null){
	           ps.setString(i, cabDoc.getFecEmisionDocSustento());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DATE);    	   
	       }
	       //i++;
	       //ps.setDate(i,  Date.valueOf(cabDoc.getFechaautorizacion()));
	       //ps.setString(i,  null);
       
	       r = ps.executeUpdate();
	       if (r > 0)
	    	   msg = "Se Guardo Correctamente" ;
	       else
	    	   msg = "Ocurrio un error al ingresar el documento ";
     }
     catch (Exception e)
     {
    	 System.out.println(e);
         System.out.println(e.getMessage());
         System.out.println(e.getCause());
         System.out.println(e.getLocalizedMessage());
         System.out.println(e.getStackTrace());
         e.printStackTrace();
    	 String str1 = msg = null; return str1;
     }
     finally
     {
    	 try {
    		 if (ps != null)
    			 ps.close();
    		 if (con != null)
    			 con.close();
    	 } catch (Exception exc) {
    		 System.out.println("	Error en insertFacCabDocumentosPostgreSQL...");
    		 System.out.println(exc);
	         System.out.println(exc.getMessage());
	         System.out.println(exc.getCause());
	         System.out.println(exc.getLocalizedMessage());
	         System.out.println(exc.getStackTrace());
	         exc.printStackTrace();
    		 throw new RuntimeException(exc);
    	 }
     }
     System.out.println("-- FIN ReporteSentencias.insertFacCabDocumentosPostgreSQL --");
     return msg;
   }
   
   
   
   
   
   
   
   public String updateFacCabDocumentosPostgreSQL(FacCabDocumento cabDoc)
   {
	   Connection con = null;
	   PreparedStatement ps = null;
	   int r;
	   int i = 0;    
	   String documento = "::Ruc::"+cabDoc.getRuc()+"::CodEstablecimiento::"+ cabDoc.getCodEstablecimiento()+"::PuntoEmision::"+ cabDoc.getCodPuntEmision()+"::Secuencial::"+ cabDoc.getSecuencial()+"::TipoDocumento::"+ cabDoc.getCodigoDocumento();
	   String msg = "";
	   String sql = "";
     
	   System.out.println("-- INICIO ReporteSentencias.updateFacCabDocumentosPostgreSQL --");
	   
	   msg = "";
	   sql = "update fac_cab_documentos " +
     		 " set \"identificacionComprador\" = ?, 	\"guiaRemision\" = ?, 	 "
     		   + " \"razonSocialComprador\" = ?,  		\"tipIdentificacionComprador\" = ?, "
     		   + " \"totalSinImpuesto\" = ?,  			\"totalDescuento\" = ?, "
     		   + "   email = ?, 						  propina = ?, "
     		   + "   moneda = ?,  						\"infoAdicional\" = ?, "
     		   + " \"periodoFiscal\" = ?,	  		  	  rise = ?, "
     		   + "   placa = ?,							\"motivoRazon\" = ?,	"
     		   + " \"identificacionDestinatario\" = ?,  \"razonSocialDestinatario\" = ?, "
     		   + " \"direccionDestinatario\" = ?,		\"docAduaneroUnico\" = ?, "
     		   + "   ruta = ?,							\"codDocSustento\" = ?,	"
     		   + " \"numDocSustento\" = ?,	 			\"numAutDocSustento\" = ?,	"
     		   + " \"motivoTraslado\" = ?,			  	  autorizacion = ?, "
     		   + " \"claveAcceso\" = ?,					\"importeTotal\" = ?,	"
     		   + " \"codDocModificado\" = ?,  			\"numDocModificado\" = ?,	"
     		   + " \"motivoValor\" = ?,					\"tipoEmision\" = ?, "
     		   + " 	 partida = ?,				  	  	  subtotal12 = ?,	"
     		   + "   subtotal0 = ?,  					\"subtotalNoIva\" = ?,	"
     		   + " \"totalvalorICE\" = ?, 			  	  iva12 = ?, "
     		   + " \"isActive\" = ?,					\"ESTADO_TRANSACCION\" = ?, "
     		   + " \"MSJ_ERROR\" = ?,  					\"fechaEmision\" = to_date(?,'dd/mm/yyyy'), ";
	   
	   if(cabDoc.getFechaInicioTransporte() == null)
		   sql= sql + " \"fechaInicioTransporte\" = ?, ";
	   else
		   sql= sql + " \"fechaInicioTransporte\" = to_date(?,'dd/mm/yyyy'), ";
	   if(cabDoc.getFechaFinTransporte() == null)
		   sql= sql + " \"fechaFinTransporte\" = ?, ";
	   else
		   sql= sql + " \"fechaFinTransporte\" = to_date(?,'dd/mm/yyyy'), ";
     		  // + " \"fechaInicioTransporte\" = to_date(?,'dd/mm/yyyy'), 	\"fechaFinTransporte\" = to_date(?,'dd/mm/yyyy'), "
     	
     	if(cabDoc.getFechaEmisionDocSustento() == null)
     		sql= sql + " \"fechaEmisionDocSustento\" = ? ";
     	else
     		sql= sql + " \"fechaEmisionDocSustento\" = to_date(?,'dd/mm/yyyy') ";
     			
	   
	   sql = sql	
			   //+    " \"fechaEmisionDocSustento\" = to_date(?,'dd/mm/yyyy') "
     		   + " where   ambiente = ? "
     		   + "	 and \"Ruc\" = ? "
     		   + "   and \"CodEstablecimiento\" = ? "
     		   + "   and \"CodPuntEmision\" = ?  "
     		   + "   and   secuencial = ? "
     		   + "   and \"CodigoDocumento\" = ? ";
	   
		int year=0, month=0, idx=0;
		try
		{
			con = Conexion.conectar();
			ps = con.prepareStatement(sql);
			i = 0;
			i++;
			ps.setString(i, String.valueOf(cabDoc.getIdentificacionComprador()));	  // HFU
			i++;
			ps.setString(i, cabDoc.getGuiaRemision());
			i++;
			ps.setString(i, (cabDoc.getRazonSocialComprador()==null)?null:cabDoc.getRazonSocialComprador().trim());
			i++;
			ps.setString(i, cabDoc.getTipIdentificacionComprador());
			i++;
			ps.setDouble(i, cabDoc.getTotalSinImpuesto());
			i++;
			ps.setDouble(i, cabDoc.getTotalDescuento());
			i++;
			ps.setString(i, cabDoc.getEmail());
			i++;
			ps.setDouble(i, cabDoc.getPropina());
			i++;
			if (cabDoc.getMoneda()!= null){
				 ps.setString(i, cabDoc.getMoneda().toString());
			}else{
				 ps.setNull(i, java.sql.Types.VARCHAR);
			}
			i++;
			if (((cabDoc.getInfoAdicional()==null)?"":cabDoc.getInfoAdicional()).length()>300)
				 ps.setString(i, cabDoc.getInfoAdicional().substring(0, 300));
			else
				 ps.setString(i, cabDoc.getInfoAdicional());
			i++;
			ps.setString(i, cabDoc.getPeriodoFiscal());
			i++;
			if (cabDoc.getRise()!= null){
	    	  ps.setString(i, cabDoc.getRise().toString());
			}else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR); 
			}
			i++;
			if (cabDoc.getPlaca()!= null){
	    	   ps.setString(i, cabDoc.getPlaca());
			}else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
			}
			i++;
			if (cabDoc.getMotivoRazon()!= null){
	    	  ps.setString(i, cabDoc.getMotivoRazon());
			}else{
	    	  ps.setNull(i, java.sql.Types.VARCHAR);
			}
			i++;
			if (cabDoc.getIdentificacionDestinatario()!= null){
	    	  ps.setString(i, cabDoc.getIdentificacionDestinatario());
			}else{
	    	  ps.setNull(i, java.sql.Types.VARCHAR);
			}
	       i++;
	       if (cabDoc.getRazonSocialDestinatario()!= null){
	    	   ps.setString(i, cabDoc.getRazonSocialDestinatario());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getDireccionDestinatario()!= null){
	    	   ps.setString(i, cabDoc.getDireccionDestinatario());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getDocAduaneroUnico()!= null){
	    	   ps.setString(i, cabDoc.getDocAduaneroUnico());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       /*i++;
	       if (cabDoc.getCodEstablecimientoDest()!= null){
	    	   ps.setString(i, cabDoc.getCodEstablecimientoDest());
		   }else{
			   ps.setNull(i, java.sql.Types.VARCHAR);
		   }*/
	       i++;
	       if (cabDoc.getRuta()!= null){
	    	   ps.setString(i, cabDoc.getRuta());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getCodDocSustento()!= null){
	    	   ps.setString(i, cabDoc.getCodDocSustento());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getNumDocSustento()!= null){
	    	   ps.setString(i, cabDoc.getNumDocSustento());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getNumAutDocSustento()!= null){
	    	   ps.setString(i, cabDoc.getNumAutDocSustento());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getMotivoTraslado()!= null){
	    	   ps.setString(i, cabDoc.getMotivoTraslado());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getAutorizacion()!= null){
	    	   ps.setString(i, cabDoc.getAutorizacion());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getClaveAcceso()!= null){
	    	   ps.setString(i, cabDoc.getClaveAcceso());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getImporteTotal()>=0){
	    	   ps.setDouble(i, cabDoc.getImporteTotal());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getCodDocModificado()!= null){
	    	   ps.setString(i, cabDoc.getCodDocModificado());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getNumDocModificado()!= null){
	    	   ps.setString(i, cabDoc.getNumDocModificado());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
	       }
	       i++;
	       if (cabDoc.getMotivoValor()>=0){
	    	   ps.setDouble(i, cabDoc.getMotivoValor());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DOUBLE);
	       }
	       i++;
	       if (cabDoc.getTipoEmision()!= null){
	    	   ps.setString(i, cabDoc.getTipoEmision());
		   }else{
			   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getPartida()!= null){
	    	   ps.setString(i, cabDoc.getPartida());
	       }else{
	    	   ps.setNull(i, java.sql.Types.VARCHAR);
		   }
	       i++;
	       if (cabDoc.getImporteTotal()>=0){
	    	   ps.setDouble(i, cabDoc.getSubtotal12());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DOUBLE);
	       }
	       i++;
	       if (cabDoc.getSubtotal0()>=0){
	    	   ps.setDouble(i, cabDoc.getSubtotal0());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DOUBLE);
	       }
	       i++;
	       if (cabDoc.getSubtotalNoIva()>=0){
	    	   ps.setDouble(i, cabDoc.getSubtotalNoIva());
		   }else{
			   ps.setNull(i, java.sql.Types.DOUBLE);
		   }
	       i++;
	       if (cabDoc.getTotalvalorICE()>=0){
	    	   ps.setDouble(i, cabDoc.getTotalvalorICE());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DOUBLE);
		   }
	       i++;
	       if (cabDoc.getIva12()>=0){
	    	   ps.setDouble(i, cabDoc.getIva12());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DOUBLE);
		   }
	       i++;
	       ps.setString(i, "Y");
	       i++;
	       ps.setString(i, cabDoc.getESTADO_TRANSACCION());
	       i++;
	       ps.setString(i, cabDoc.getMSJ_ERROR());
	       i++;
	       if (cabDoc.getFechaEmision()!= null){
	           ps.setString(i, cabDoc.getFechaEmision());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DATE);    	   
	       }
	       i++;
	       if (cabDoc.getFechaInicioTransporte()!= null){
	    	   ps.setString(i, cabDoc.getFechaInicioTransporte());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DATE);    	   
	       }
	       i++;
	       if (cabDoc.getFechaFinTransporte()!= null){
	           ps.setString(i, cabDoc.getFechaFinTransporte());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DATE);
	       }
	       i++;
	       if (cabDoc.getFechaEmisionDocSustento()!= null){
	           ps.setString(i, cabDoc.getFecEmisionDocSustento());
	       }else{
	    	   ps.setNull(i, java.sql.Types.DATE);    	   
	       } 
	       
	       i++;
	       ps.setInt(i, cabDoc.getAmbiente().intValue());
	       i++;
	       ps.setString(i, cabDoc.getRuc());
	       i++;
	       ps.setString(i, cabDoc.getCodEstablecimiento());
	       i++;
	       ps.setString(i, cabDoc.getCodPuntEmision());
	       i++;
	       ps.setString(i, cabDoc.getSecuencial());
	       i++;
	       ps.setString(i, cabDoc.getCodigoDocumento());
       
	       r = ps.executeUpdate();
	       if (r > 0)
	    	   msg = "Se actualizo Correctamente" ;
	       else
	    	   msg = "Ocurrio un error al actualizar el documento ";
     }
     catch (Exception e)
     {
    	 System.out.println(e);
         System.out.println(e.getMessage());
         System.out.println(e.getCause());
         System.out.println(e.getLocalizedMessage());
         System.out.println(e.getStackTrace());
         e.printStackTrace();
    	 String str1 = msg = null; return str1;
     }
     finally
     {
    	 try {
    		 if (ps != null)
    			 ps.close();
    		 if (con != null)
    			 con.close();
    	 } catch (Exception exc) {
    		 System.out.println("	Error en updateCabDocumentosPostgreSQL...");
    		 System.out.println(exc);
	         System.out.println(exc.getMessage());
	         System.out.println(exc.getCause());
	         System.out.println(exc.getLocalizedMessage());
	         System.out.println(exc.getStackTrace());
	         exc.printStackTrace();
    		 throw new RuntimeException(exc);
    	 }
     }
     System.out.println("-- FIN ReporteSentencias.updateCabDocumentosPostgreSQL --");
     return msg;
   }
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   

	public boolean existFacCabDocumentos(FacCabDocumento cabDoc)
	{
		 Connection con = null;
		 PreparedStatement ps = null;
		 ResultSet rs = null;
		 int cantidad = 0;
		 String sql = "Select count(1) from fac_cab_documentos "
     		+ " where ambiente = ? and  "
     		+ " \"Ruc\" = ? and "
     		+ " \"CodEstablecimiento\" = ? and "
     		+ " \"CodPuntEmision\" = ? "
     		+ " and secuencial = ? and "
     		+ " \"CodigoDocumento\" = ? ";    
		 try
		 {
			 con = Conexion.conectar();
			 ps = con.prepareStatement(sql);       
			 int i = 0;
			 i++;
			 ps.setInt(i, cabDoc.getAmbiente().intValue());
			 i++;
			 ps.setString(i, cabDoc.getRuc());       
			 i++;
			 ps.setString(i, cabDoc.getCodEstablecimiento());
			 i++;
			 ps.setString(i, cabDoc.getCodPuntEmision());
			 i++;
			 ps.setString(i, cabDoc.getSecuencial());
			 i++;
			 ps.setString(i, cabDoc.getCodigoDocumento());
			 rs = ps.executeQuery();
			 while (rs.next()){
				 cantidad = rs.getInt(1);  
			 }
       
			 if (cantidad>0) return true; else return false;
		 } catch (Exception e) {
			 System.out.println(e);
	         System.out.println(e.getMessage());
	         System.out.println(e.getCause());
	         System.out.println(e.getLocalizedMessage());
	         System.out.println(e.getStackTrace());
	         e.printStackTrace();
			 return false;
		 } finally {
	       try {
	         if (ps != null)
	           ps.close();
	         if (con != null)
	           con.close();
	       } catch (Exception exc) {
	         throw new RuntimeException(exc);
	       }
		 }     
   }
    
	public boolean updateFacCabDocumentos(FacCabDocumento cabDoc)
	{
		 Connection con = null;
		 PreparedStatement ps = null;
		 ResultSet rs = null;
		 int cantidad = 0;
		 String documento = "::Ruc::"+cabDoc.getRuc()+"::CodEstablecimiento::"+ cabDoc.getCodEstablecimiento()+"::PuntoEmision::"+ cabDoc.getCodPuntEmision()+"::Secuencial::"+ cabDoc.getSecuencial()+"::TipoDocumento::"+ cabDoc.getCodigoDocumento();
		 String sql = " update fac_cab_documentos set  " +
    		 " \"guiaRemision\" = ?, \"razonSocialComprador\"=?, \"identificacionComprador\"=?,\"totalSinImpuesto\"=?,\"totalDescuento\"=?,email=?,propina=?,moneda=?,\"infoAdicional\"=?,\"periodoFiscal\"=?,rise=?,placa=?, " +
    		 " \"motivoRazon\"=?,\"identificacionDestinatario\"=?,\"razonSocialDestinatario\"=?,\"direccionDestinatario\"=?,\"docAduaneroUnico\"=?,\"codEstablecimientoDest\"=?,ruta,\"codDocSustento\"=?,\"numDocSustento\"=?," +
    		 " \"numAutDocSustento\"=?,\"motivoTraslado\"=?,autorizacion=?,\"claveAcceso\"=?,\"importeTotal\"=?,\"codDocModificado\"=?,\"numDocModificado\"=?,\"motivoValor\"=?,\"tipoEmision\"=?,partida=?,subtotal12=?," +
    		 " subtotal0=?,\"subtotalNoIva\"=?,\"totalvalorICE\"=?,iva12=?,\"isActive\"=?,\"ESTADO_TRANSACCION\"=?,\"MSJ_ERROR\"=?,\"fechaEmision\"=?,\"fechaInicioTransporte\"=?,\"fechaFinTransporte\"=?,\"fechaEmisionDocSustento\"=? "+ 
    		 " where ambiente = ? and  \"Ruc\" = ? and \"CodEstablecimiento\" = ? and \"CodPuntEmision\" = ? and secuencial = ? and \"CodigoDocumento\" = ? ";    
		 try
		 {
			 con = Conexion.conectar();
			 ps = con.prepareStatement(sql);
			 int i = 0;
			 i++;
			 ps.setInt(i, cabDoc.getAmbiente().intValue());
			 i++;
			 ps.setString(i, cabDoc.getRuc());
			 i++;
			 ps.setString(i, String.valueOf(cabDoc.getIdentificacionComprador()));
			 i++;
			 ps.setString(i, cabDoc.getCodEstablecimiento());
			 i++;
			 ps.setString(i, cabDoc.getCodPuntEmision());
			 i++;
			 ps.setString(i, cabDoc.getSecuencial());
			 i++;
			 ps.setString(i, cabDoc.getCodigoDocumento());
			 
			 rs = ps.executeQuery();
			 while (rs.next()){
				 cantidad = rs.getInt(1);  
			 }
       
			 if (cantidad>0) 
				 return true; 
			 else 
				 return false;
		 } catch (Exception e) {
			 System.out.println(e);
	         System.out.println(e.getMessage());
	         System.out.println(e.getCause());
	         System.out.println(e.getLocalizedMessage());
	         System.out.println(e.getStackTrace());
	         e.printStackTrace();
			 return false;
		 } finally {
	       try {
	         if (ps != null)
	           ps.close();
	         if (con != null)
	           con.close();
	       } catch (Exception exc) {
	         throw new RuntimeException(exc);
	       }
		 } 
    }
	
	public String updateEstadoAutorizacionXmlDocumento(FacCabDocumento cabDoc,
													   String xmlAutorizacion,
													   String fechaAutorizado,
													   Date fechaAutorizacion,
													   String docuAutorizacion,
													   
													   String estado,
													   String mensaje) // dos parametros nuevos
	{
		System.out.println("-- INICIO ReporteSentencias.updateEstadoAutorizacionXmlDocumento --");
		Connection con = null;
		PreparedStatement ps = null;
		String documento = "::Ruc::"+cabDoc.getRuc()+"::CodEstablecimiento::"+ cabDoc.getCodEstablecimiento()+"::PuntoEmision::"+ cabDoc.getCodPuntEmision()+"::Secuencial::"+ cabDoc.getSecuencial()+"::TipoDocumento::"+ cabDoc.getCodigoDocumento();
		String msg = "";
		
		String sql = "update fac_cab_documentos set  \"docuAutorizacion\" = ?,  autorizacion = ?, \"fechaautorizacion\" = ?,  " +
     		"	\"fechaAutorizado\" = ?, \"ESTADO_TRANSACCION\" = ?,  \"MSJ_ERROR\" = ? "
     		+ " where ambiente = ? and \"Ruc\" = ? " + 
     		      " and \"CodEstablecimiento\"= ? and \"CodPuntEmision\"= ? and secuencial = ? and \"CodigoDocumento\"= ? ";
		
		try
		{
			con = Conexion.conectar();
			ps = con.prepareStatement(sql);
			int i = 0;
			i++;
			ps.setString(i, docuAutorizacion);
		    i++;
		    ps.setString(i, cabDoc.getAutorizacion());
		    i++;
		    ps.setDate(i, new java.sql.Date(fechaAutorizacion.getTime()));
			/*i++;
       		ps.setString(i, cabDoc.getClaveAcceso());*/
       		i++;
       		ps.setString(i, fechaAutorizado);
			
			i++;
		    ps.setString(i, estado);
		    i++;
		    ps.setString(i, mensaje);
			
		    i++;
       		ps.setInt(i, cabDoc.getAmbiente().intValue());
		    i++;
		    ps.setString(i, cabDoc.getRuc());
		    i++;
		    ps.setString(i, cabDoc.getCodEstablecimiento());
		    i++;
	        ps.setString(i, cabDoc.getCodPuntEmision());
    	    i++;
	        ps.setString(i, cabDoc.getSecuencial());
    	    i++;
       	    ps.setString(i, cabDoc.getCodigoDocumento());
       	    
       	    int r = ps.executeUpdate();
       	    if (r > 0)
       	    	msg = "Se Guardo Correctamente el documento->"+documento ;
       	    else
       	    	msg = "Ocurrio un error al ingresar el documento->"+documento;
		} catch (Exception e) {
			System.out.println(e);
			System.out.println(e.getMessage());
			System.out.println(e.getCause());
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.getStackTrace());
			e.printStackTrace();
			String str1 = msg = null; return str1;
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();
			} catch (Exception exc) {
				throw new RuntimeException(exc);
			}
		}
		System.out.println("-- FIN ReporteSentencias.updateEstadoAutorizacionXmlDocumento --");
		return msg;
	}
	
	
	public String updateEstadoContingencia(FacCabDocumento cabDoc,
		   								  String estadoTransaccion,
		   								  String msjError,
		   								  String claveAccesoContingente,
		   								  String claveContingencia)
    {
		System.out.println("-- INCICIO ReporteSentencias.updateEstadoContingencia --");
		Connection con = null;
		PreparedStatement ps = null;
		String documento = "::Ruc::"+cabDoc.getRuc()+"::CodEstablecimiento::"+ cabDoc.getCodEstablecimiento()+
    		 			"::PuntoEmision::"+ cabDoc.getCodPuntEmision()+"::Secuencial::"+ cabDoc.getSecuencial()+
    		 			"::TipoDocumento::"+ cabDoc.getCodigoDocumento();
	   
		String msg = "";
		String sql = "update fac_cab_documentos "+
	   		 		  "set  \"ESTADO_TRANSACCION\" = ?,  \"MSJ_ERROR\" = ?, "+
	   		 		  	  " \"claveAccesoContigente\" = ?,  " +
	   		 		  	  "	\"claveContingencia\" = ? " +
	   		 		"  where ambiente = ? and \"Ruc\" = ? " +
	   		 		  " and \"CodEstablecimiento\"= ? and \"CodPuntEmision\"= ? and secuencial = ? and \"CodigoDocumento\"= ? ";
	   
		/*String sql = "update fac_cab_documentos "+
		 		  "set  \"ESTADO_TRANSACCION\" = ?,  \"MSJ_ERROR\" = ?, "+
		 		  	  " \"claveAccesoContigente\" = COALESCE(?,\"claveAccesoContigente\"),  " +
		 		  	  "	\"claveContingencia\" = COALESCE(?,\"claveAccesoContigente\")" +
		 		"  where ambiente = ? and \"Ruc\" = ? " +
		 		  " and \"CodEstablecimiento\"= ? and \"CodPuntEmision\"= ? and secuencial = ? and \"CodigoDocumento\"= ? ";*/
	    
		try
		{
			con = Conexion.conectar();
			ps = con.prepareStatement(sql);
			int i = 0;
		    
			i++;
			ps.setString(i, estadoTransaccion);
			i++;
			ps.setString(i, msjError);
			i++;
			ps.setString(i, claveAccesoContingente);
			i++;
			ps.setString(i, claveContingencia);
			i++;
			ps.setInt(i, cabDoc.getAmbiente().intValue());
			i++;
			ps.setString(i, cabDoc.getRuc());
			i++;
			ps.setString(i, cabDoc.getCodEstablecimiento());
			i++;
			ps.setString(i, cabDoc.getCodPuntEmision());
			i++;
			ps.setString(i, cabDoc.getSecuencial());
			i++;
			ps.setString(i, cabDoc.getCodigoDocumento());
       
		   int r = ps.executeUpdate();
		   if (r > 0)
		   	 msg = "Se Guardo Correctamente el documento->"+documento ;
		   else
		     msg = "Ocurrio un error al ingresar el documento->"+documento;
		} catch (Exception e) {
			System.out.println(e);
	        System.out.println(e.getMessage());
	        System.out.println(e.getCause());
	        System.out.println(e.getLocalizedMessage());
	        System.out.println(e.getStackTrace());
	        e.printStackTrace();
		    String str1 = msg = null; return str1;
		} finally {
	       try {
	         if (ps != null)
	           ps.close();
	         if (con != null)
	           con.close();
	       } catch (Exception exc) {
	         throw new RuntimeException(exc);
	       }
		}
	   System.out.println("-- FIN ReporteSentencias.updateEstadoContingencia --");
     return msg;
   }
   
	public String updateEstadoDocumento(String estado, String msg_error, String tipo_emision,FacCabDocumento cabDoc, String xmlAutorizacion)
	{
		System.out.println("-- INICIO ReporteSentencias.updateEstadoDocumento --");
		Connection con = null;
	    PreparedStatement ps = null;
	    String documento = "::Ruc::"+cabDoc.getRuc()+"::CodEstablecimiento::"+ cabDoc.getCodEstablecimiento()+"::PuntoEmision::"+ cabDoc.getCodPuntEmision()+"::Secuencial::"+ cabDoc.getSecuencial()+"::TipoDocumento::"+ cabDoc.getCodigoDocumento();
	    String msg = "";
	    String sql = "update fac_cab_documentos set \"ESTADO_TRANSACCION\" = ?, \"MSJ_ERROR\" = ?, \"tipoEmision\" = ?, \"docuAutorizacion\" = COALESCE(?,\"docuAutorizacion\")  " +
     		"  where ambiente = ? and \"Ruc\" = ? " + //and \"tipIdentificacionComprador\" = ? " +
     		      " and \"CodEstablecimiento\"= ? and \"CodPuntEmision\"= ? and secuencial = ? and \"CodigoDocumento\"= ? ";
	    
	    try
	    {
	    	con = Conexion.conectar();
	    	ps = con.prepareStatement(sql);
	    	int i = 0;
	    	i++;
	    	ps.setString(i, estado);
	        i++;
	        ps.setString(i, msg_error);
	        i++;
	        ps.setString(i, tipo_emision);
	        
	        i++;
	        ps.setString(i, xmlAutorizacion);
	        
	        i++;
	        ps.setInt(i, cabDoc.getAmbiente().intValue());
	        i++;
	        ps.setString(i, cabDoc.getRuc());
	        i++;
	        /*
	        ps.setString(i, String.valueOf(cabDoc.getTipoIdentificacion()));
	        i++;*/
	        ps.setString(i, cabDoc.getCodEstablecimiento());
	        i++;
	        ps.setString(i, cabDoc.getCodPuntEmision());
	        i++;
	        ps.setString(i, cabDoc.getSecuencial());
	        i++;
	        ps.setString(i, cabDoc.getCodigoDocumento());
       
	        int r = ps.executeUpdate();
	        if (r > 0)
	    	   msg = "Se Guardo Correctamente el documento->"+documento ;
	        else
	    	   msg = "Ocurrio un error al ingresar el documento->"+documento;
	    } catch (Exception e) {
	    	System.out.println(e);
	        System.out.println(e.getMessage());
	        System.out.println(e.getCause());
	        System.out.println(e.getLocalizedMessage());
	        System.out.println(e.getStackTrace());
	        e.printStackTrace();
	    	String str1 = msg = null;
			return str1;
	    } finally {
	    	try {
	    		if (ps != null)
	    			ps.close();
	    		if (con != null)
	    			con.close();
	    	} catch (Exception exc) {
	    		throw new RuntimeException(exc);
	    	}
	    }
	    System.out.println("-- FIN ReporteSentencias.updateEstadoDocumento --");
	    return msg;
	}
	
	   public String updateColaDocumentos(FacCabDocumento cabDoc, 
			  String estadoTransaccion)
	{
	Connection con = null;
	PreparedStatement ps = null;
	String documento = "::Ruc::"+cabDoc.getRuc()+"::CodEstablecimiento::"+ cabDoc.getCodEstablecimiento()+
	"::PuntoEmision::"+ cabDoc.getCodPuntEmision()+"::Secuencial::"+ cabDoc.getSecuencial()+
	"::TipoDocumento::"+ cabDoc.getCodigoDocumento();
	String msg = "";
	
	String sql = "update fac_cola_documentos set  \"EstadoDocumento\" = ?  where ambiente = ? and \"Ruc\" = ? " +
	" and \"CodEstablecimiento\"= ? and \"CodPuntoEmision\"= ? and secuencial = ? and \"CodigoDocumento\"= ? ";
	try {
	con = Conexion.conectar();
	ps = con.prepareStatement(sql);
	int i = 0;
	
	i++;
	ps.setString(i, estadoTransaccion);       
	i++;
	ps.setInt(i, cabDoc.getAmbiente().intValue());
	i++;
	ps.setString(i, cabDoc.getRuc());
	i++;
	ps.setString(i, cabDoc.getCodEstablecimiento());
	i++;
	ps.setString(i, cabDoc.getCodPuntEmision());
	i++;
	ps.setString(i, cabDoc.getSecuencial());
	i++;
	ps.setString(i, cabDoc.getCodigoDocumento());
	
	int r = ps.executeUpdate();
	if (r > 0)
	msg = "Se Guardo Correctamente el documento->"+documento ;
	else
	msg = "Ocurrio un error al ingresar el documento->"+documento;
	} catch (Exception e) {
		System.out.println(e);
        System.out.println(e.getMessage());
        System.out.println(e.getCause());
        System.out.println(e.getLocalizedMessage());
        System.out.println(e.getStackTrace());
        e.printStackTrace();
	String str1 = msg = null; return str1;
	} finally {
	try {
	if (ps != null)
	ps.close();
	if (con != null)
	con.close();
	} catch (Exception exc) {
	throw new RuntimeException(exc);
	}
	}
	return msg;
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


	public String insertFacDetallesDocumento(List<FacDetDocumento> detalles)
	{
		 System.out.println("-- INCICIO ReporteSentencias.insertFacDetallesDocumento --");	// HFU
     
		 PreparedStatement ps = null;
		 Connection con = null;
		 String msg = "";
		 String documento = "::Ruc::"+detalles.get(0).getRuc()+
    		 			"::CodEstablecimiento::"+ detalles.get(0).getCodEstablecimiento()+
    		 			"::PuntoEmision::"+ detalles.get(0).getCodPuntEmision()+
    		 			"::Secuencial::"+ detalles.get(0).getSecuencial()+
    		 			"::TipoDocumento::"+ detalles.get(0).getCodigoDocumento();
		 
		 String sql="delete from fac_det_documentos  where \"Ruc\"=?  and \"CodEstablecimiento\"=? and \"CodPuntEmision\"=? and secuencial=? and \"CodigoDocumento\"=? ";
		 
		 try
		 {
			 con = Conexion.conectar();
			 ps = con.prepareStatement(sql);
			 ps.setString(1, ((FacDetDocumento)detalles.get(0)).getRuc());
			 ps.setString(2, ((FacDetDocumento)detalles.get(0)).getCodEstablecimiento());
			 ps.setString(3, ((FacDetDocumento)detalles.get(0)).getCodPuntEmision());
			 ps.setString(4, ((FacDetDocumento)detalles.get(0)).getSecuencial());
			 ps.setString(5, ((FacDetDocumento)detalles.get(0)).getCodigoDocumento());
			 //if(detalles.get(0).getCodigoDocumento().equals("06"))
			 //	 ps.setString(6, ((FacDetDocumento)detalles.get(0)).getIdentificacion());
			 int r = ps.executeUpdate();
			 
			 if (r > 0)
				 msg = "Se Elimino correctamente los detalles del documento->"+documento;
			 else
				 msg = "Ocurrio un error al eliminar los detalles del documento->"+documento;
		 } catch (Exception e) {
			 System.out.println(e);
	         System.out.println(e.getMessage());
	         System.out.println(e.getCause());
	         System.out.println(e.getLocalizedMessage());
	         System.out.println(e.getStackTrace());
	         e.printStackTrace();
		 }
		 finally{
			 try {
				 if (ps != null)
					 ps.close();
				 if (con != null)
					 con.close();
			 } catch (Exception e) {
				 System.out.println(e);
		         System.out.println(e.getMessage());
		         System.out.println(e.getCause());
		         System.out.println(e.getLocalizedMessage());
		         System.out.println(e.getStackTrace());
		         e.printStackTrace();
				 throw new RuntimeException(e);
			 }
		 }
		 msg = "";
		 sql = "INSERT INTO fac_det_documentos  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		 try
		 {
			 con = Conexion.conectar();
			 for (int i = 0; i < detalles.size(); i++)
			 {
				 ps = con.prepareStatement(sql);
				 ps.setString(1, ((FacDetDocumento)detalles.get(i)).getRuc());
				 ps.setString(2, ((FacDetDocumento)detalles.get(i)).getCodEstablecimiento());
				 ps.setString(3, ((FacDetDocumento)detalles.get(i)).getCodPuntEmision());
				 ps.setString(4, ((FacDetDocumento)detalles.get(i)).getSecuencial());
				 ps.setString(5, ((FacDetDocumento)detalles.get(i)).getCodPrincipal());
				 ps.setString(6, ((FacDetDocumento)detalles.get(i)).getCodAuxiliar());
				 ps.setString(7, ((FacDetDocumento)detalles.get(i)).getDescripcion());
				 ps.setInt(8, 	 ((FacDetDocumento)detalles.get(i)).getCantidad().intValue());
				 ps.setDouble(9, ((FacDetDocumento)detalles.get(i)).getPrecioUnitario());
				 ps.setDouble(10, ((FacDetDocumento)detalles.get(i)).getDescuento());
				 ps.setDouble(11, ((FacDetDocumento)detalles.get(i)).getPrecioTotalSinImpuesto());
				 ps.setDouble(12, ((FacDetDocumento)detalles.get(i)).getValorIce());
				 ps.setInt(13, i + 1);
				 ps.setString(14, ((FacDetDocumento)detalles.get(i)).getCodigoDocumento());
         		 ps.setInt(15, ((FacDetDocumento)detalles.get(i)).getAmbiente());
			   
				 int r = ps.executeUpdate();
				 if (r > 0)
					 msg = "Se Guardo correctamente los detalles del documento->"+documento;
				 else
					 msg = "Ocurrio un error al Guardar los detalles del documento->"+documento;
			 }
		 } catch (Exception e) {
			 System.out.println(e);
	         System.out.println(e.getMessage());
	         System.out.println(e.getCause());
	         System.out.println(e.getLocalizedMessage());
	         System.out.println(e.getStackTrace());
	         e.printStackTrace();
			 e.printStackTrace();
		 } finally {
			 try {
				 if (ps != null)
					 ps.close();
				  if (con != null)
					 con.close();
			 } catch (Exception e) {
				 System.out.println(e);
		         System.out.println(e.getMessage());
		         System.out.println(e.getCause());
		         System.out.println(e.getLocalizedMessage());
		         System.out.println(e.getStackTrace());
		         e.printStackTrace();
				 throw new RuntimeException(e);
			 }
		 }
		 System.out.println("-- FIN ReporteSentencias.insertFacDetallesDocumento --");
		 return msg;
	 }
      
	// INI HFU
	public String insertGuiaDetallesDocumento(List<DetalleGuiaRemision> detalles)
	{
		System.out.println("-- INCICIO ReporteSentencias.insertGuiaDetallesDocumento --");	// HFU
     
		PreparedStatement ps = null;
		Connection con = null;
		String msg = "";
		 
		String documento = "::Ruc::"+detalles.get(0).getRuc()+"::CodEstablecimiento::"+ detalles.get(0).getCodEstablecimiento()+"::PuntoEmision::"+ detalles.get(0).getCodPuntEmision()+"::Secuencial::"+ detalles.get(0).getSecuencial()+"::TipoDocumento::"+ detalles.get(0).getIdentificacionDestinatario();
		String sql="";
		
		//sql = "delete from fac_det_guia_remision  where \"Ruc\"=?  and \"CodEstablecimiento\"=? and \"CodPuntEmision\"=? and secuencial=? and \"CodigoDocumento\"=? ";
		sql = "delete from fac_det_guia_remision  where ambiente=? and \"Ruc\"=?  and \"codEstablecimiento\"=? and \"codPuntEmision\"=? and secuencial=? and \"identificacionDestinatario\"=? ";
		
		for (int i = 0; i < detalles.size(); i++)
		{
			try
			{
				con = Conexion.conectar();
				ps = con.prepareStatement(sql);
				ps.setInt(1, ((DetalleGuiaRemision)detalles.get(i)).getAmbiente());
				ps.setString(2, ((DetalleGuiaRemision)detalles.get(i)).getRuc());
				ps.setString(3, ((DetalleGuiaRemision)detalles.get(i)).getCodEstablecimiento());
				ps.setString(4, ((DetalleGuiaRemision)detalles.get(i)).getCodPuntEmision());
				ps.setString(5, ((DetalleGuiaRemision)detalles.get(i)).getSecuencial());
				ps.setString(6, ((DetalleGuiaRemision)detalles.get(i)).getIdentificacionDestinatario());
				int r = ps.executeUpdate();
			   
				if (r > 0)
					 msg = "Se Elimino correctamente los detalles del documento->"+documento;
				else
					 msg = "Ocurrio un error al eliminar los detalles del documento->"+documento;
			 } catch (Exception e) {
				 System.out.println(e);
		         System.out.println(e.getMessage());
		         System.out.println(e.getCause());
		         System.out.println(e.getLocalizedMessage());
		         System.out.println(e.getStackTrace());
		         e.printStackTrace();
				 e.printStackTrace();         
			 }
			 finally{
				 try {
					 if (ps != null)
						 ps.close();
					 if (con != null)
						 con.close();
				 } catch (Exception exc) {
					 System.out.println(exc);
			         System.out.println(exc.getMessage());
			         System.out.println(exc.getCause());
			         System.out.println(exc.getLocalizedMessage());
			         System.out.println(exc.getStackTrace());
			         exc.printStackTrace();
					 throw new RuntimeException(exc);
				 }
			 }
		}
		
		
		 
		 
		msg = "";
		//sql = "INSERT INTO fac_det_guia_remision  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		sql = "INSERT INTO fac_det_guia_remision (ambiente, \"Ruc\", \"codEstablecimiento\", \"codPuntEmision\", \"secuencial\", \"identificacionDestinatario\", "
				+ "\"secuencialDetalle\", \"codigoInterno\", \"codigoAdicional\", \"descripcion\", \"cantidad\", \"detallesAdicionales\") "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try
		{
			con = Conexion.conectar();
			for (int i = 0; i < detalles.size(); i++)
			{
				ps = con.prepareStatement(sql);
				ps.setInt(1, ((DetalleGuiaRemision)detalles.get(i)).getAmbiente());
				ps.setString(2, ((DetalleGuiaRemision)detalles.get(i)).getRuc());
				ps.setString(3, ((DetalleGuiaRemision)detalles.get(i)).getCodEstablecimiento());
				ps.setString(4, ((DetalleGuiaRemision)detalles.get(i)).getCodPuntEmision());
				ps.setString(5, ((DetalleGuiaRemision)detalles.get(i)).getSecuencial());
				ps.setString(6, ((DetalleGuiaRemision)detalles.get(i)).getIdentificacionDestinatario());
				ps.setInt(7, i + 1);
				 
				ps.setString(8, ((DetalleGuiaRemision)detalles.get(i)).getCodigoInterno());
				ps.setString(9, ((DetalleGuiaRemision)detalles.get(i)).getCodigoAdicional());
				ps.setString(10, ((DetalleGuiaRemision)detalles.get(i)).getDescripcion());
				ps.setInt(11, 	 ((DetalleGuiaRemision)detalles.get(i)).getCantidad());
				ps.setString(12, ((DetalleGuiaRemision)detalles.get(i)).getDetallesAdicionales());
			   
				int r = ps.executeUpdate();
				if (r > 0) 
					 msg = "Se Guardo correctamente los detalles del documento->"+documento;
				else
					 msg = "Ocurrio un error al Guardar los detalles del documento->"+documento;
			} 
		} catch (Exception e) {
			System.out.println(e);
	        System.out.println(e.getMessage());
	        System.out.println(e.getCause());
	        System.out.println(e.getLocalizedMessage());
	        System.out.println(e.getStackTrace());
	        e.printStackTrace();
		} finally {
			 try {
				 if (ps != null)
					 ps.close();
				  if (con != null)
					 con.close();
			 } catch (Exception exc) {
				 System.out.println(exc);
		         System.out.println(exc.getMessage());
		         System.out.println(exc.getCause());
		         System.out.println(exc.getLocalizedMessage());
		         System.out.println(exc.getStackTrace());
		         exc.printStackTrace();
				 throw new RuntimeException(exc);
			 }
		}
		 System.out.println("-- FIN ReporteSentencias.insertGuiaDetallesDocumento --");
		 return msg;
	}
	
	public String insertDestinatariosGuiaRemision(List<Destinatarios> destinatarios)
	{
		System.out.println("-- INCICIO ReporteSentencias.insertDestinatariosGuiaRemision --");
		 
		PreparedStatement ps = null;
		Connection con = null;
		String documento = "::Ruc::"+destinatarios.get(0).getRuc()+"::CodEstablecimiento::"+ destinatarios.get(0).getCodEstablecimiento()+"::PuntoEmision::"+ destinatarios.get(0).getCodPuntEmision()+"::Secuencial::"+ destinatarios.get(0).getSecuencial()+"::TipoDocumento::"+ destinatarios.get(0).getCodigoDocumento();
		String msg = "";
		String sql ="";
		
		sql = "delete from fac_destinatario  where ambiente = ? and \"Ruc\"=?  and \"codEstablecimiento\"=? and \"CodPuntEmision\"=? and secuencial=? and \"identificacion\"=? ";
		for (int i = 0; i < destinatarios.size(); i++)
		{
			try
			{
				con = Conexion.conectar();
				ps = con.prepareStatement(sql);
				ps.setInt(1, ((Destinatarios)destinatarios.get(i)).getAmbiente());
				ps.setString(2, ((Destinatarios)destinatarios.get(i)).getRuc());
				ps.setString(3, ((Destinatarios)destinatarios.get(i)).getCodEstablecimiento());
				ps.setString(4, ((Destinatarios)destinatarios.get(i)).getCodPuntEmision());
				ps.setString(5, ((Destinatarios)destinatarios.get(i)).getSecuencial());
				ps.setString(6, ((Destinatarios)destinatarios.get(i)).getIdentificacionDestinatario());
				int r = ps.executeUpdate();
			   
				if (r > 0)
					msg = "Se Elimino correctamente el destinatario detalles del documento->"+documento;
				else
					msg = "Ocurrio un error al eliminar los detalles del documento->"+documento;
			} catch (Exception e) {
				System.out.println(e);
		        System.out.println(e.getMessage());
		        System.out.println(e.getCause());
		        System.out.println(e.getLocalizedMessage());
		        System.out.println(e.getStackTrace());
		        e.printStackTrace();
			}
			finally{
				try {
					if (ps != null)
						ps.close();
					if (con != null)
						con.close();
				} catch (Exception exc) {
					System.out.println(exc);
					throw new RuntimeException(exc);
				}
			}
		}
		
		
		msg = "";
		//sql = "INSERT INTO fac_destinatario VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, to_date(?,'DD/MM/YYYY'))";
		
		sql = "INSERT INTO fac_destinatario (ambiente, \"Ruc\",  \"codEstablecimiento\", \"CodPuntEmision\", \"secuencial\", "
				+ "\"secuencialDetalle\", \"identificacion\",  \"razonSocial\", direccion, \"motivoTraslado\", \"documentoAduanero\", "
				+ " \"codEstabDestino\", \"ruta\", \"codDocSustento\", \"numDocSustento\", \"numAutorizacionDocSustento\", \"fechaEmisionDocSustento\" )"
				
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, to_date(?,'DD/MM/YYYY'))";
		
		try
		{
			con = Conexion.conectar();
			for (int i = 0; i < destinatarios.size(); i++)
			{
				ps = con.prepareStatement(sql);
				 
				ps.setInt(1, ((Destinatarios)destinatarios.get(i)).getAmbiente());
				ps.setString(2, ((Destinatarios)destinatarios.get(i)).getRuc());
				ps.setString(3, ((Destinatarios)destinatarios.get(i)).getCodEstablecimiento());
				ps.setString(4, ((Destinatarios)destinatarios.get(i)).getCodPuntEmision());
				ps.setString(5, ((Destinatarios)destinatarios.get(i)).getSecuencial());
				ps.setInt(6, i + 1);
				ps.setString(7, ((Destinatarios)destinatarios.get(i)).getIdentificacionDestinatario());
				ps.setString(8, ((Destinatarios)destinatarios.get(i)).getRazonSocialDestinatario());
				ps.setString(9, ((Destinatarios)destinatarios.get(i)).getDireccionDestinatario());
				ps.setString(10, ((Destinatarios)destinatarios.get(i)).getMotivoTraslado());
				ps.setString(11, ((Destinatarios)destinatarios.get(i)).getDocAduanero());//
				ps.setString(12, ((Destinatarios)destinatarios.get(i)).getCodEstabDestino());
				ps.setString(13, ((Destinatarios)destinatarios.get(i)).getRutaDest());//
				ps.setString(14, ((Destinatarios)destinatarios.get(i)).getCodDocSustentoDest());//
				ps.setString(15, ((Destinatarios)destinatarios.get(i)).getNumDocSustentoDest());
				ps.setString(16, ((Destinatarios)destinatarios.get(i)).getNumAutDocSustDest());
				ps.setString(17, ((Destinatarios)destinatarios.get(i)).getFechEmisionDocSustDest());
				
				int r = ps.executeUpdate();
				if (r > 0)
				{
					insertGuiaDetallesDocumento(destinatarios.get(i).getListDetallesGuiaRemision());
					msg = "Se Guardo correctamente los detalles del documento->"+documento;
				}
				else
					msg = "Ocurrio un error al Guardar los detalles del documento->"+documento;
			}
		} catch (Exception e) {
			System.out.println(e);
	        System.out.println(e.getMessage());
	        System.out.println(e.getCause());
	        System.out.println(e.getLocalizedMessage());
	        System.out.println(e.getStackTrace());
	        e.printStackTrace();
		} finally {
			 try {
				 if (ps != null)
					 ps.close();
				  if (con != null)
					 con.close();
			 } catch (Exception exc) {
				 System.out.println(exc);
				 throw new RuntimeException(exc);
			 }
		 }
		
		 System.out.println("-- FIN ReporteSentencias.insertDestinatariosGuiaRemision --");
		 return msg;
	}
	// FIN HFU

	 
	public String insertFacDetallesRetenciones(FacCabDocumento facCab)
	{
	   String resultado = "";
	   if (ServiceData.databaseMotor.equals("PostgreSQL")){
		   resultado = insertFacDetallesRetencionesPostgreSQL(facCab);
	   }
	   if (ServiceData.databaseMotor.equals("SQLServer")){
		   resultado = insertFacDetallesRetencionesSQLServer(facCab);
	   }
	   return resultado;
	}
   
	 // Inserta detalle de retenciones (Impuestos)
	 public String insertFacDetallesRetencionesPostgreSQL(FacCabDocumento facCab)
	 {
		 System.out.println("-- INCICIO ReporteSentencias.insertFacDetallesRetencionesPostgreSQL --");
	     PreparedStatement ps = null;
	     Connection con = null;
	     String msg = "";
	     String sql = "delete from fac_det_retenciones  where ambiente = ? and \"Ruc\"=?  "
	     		+ "and \"CodEstablecimiento\"=? and \"CodPuntEmision\"=? "
	     		+ "and secuencial=? and \"CodigoDocumento\"=? and \"ambiente\" = ? ";
	     try {
	     con = Conexion.conectar();
	     ps = con.prepareStatement(sql);
	     ps.setInt(1, facCab.getAmbiente());
	     ps.setString(2, facCab.getRuc());
	     ps.setString(3, facCab.getCodEstablecimiento());
	     ps.setString(4, facCab.getCodPuntEmision());
	     ps.setString(5, facCab.getSecuencial());
	     ps.setString(6, facCab.getCodigoDocumento());
	     ps.setInt(7, facCab.getAmbiente());
	     int r = ps.executeUpdate();

	     if (r > 0) 
	       msg = "Se Guardo Correctamente Detalles de la "; 
	     else
	       msg = "Ocurrio un error al guardar los detalles de la ";

	     } catch (Exception e) {
	    	 System.out.println(e);
	         System.out.println(e.getMessage());
	         System.out.println(e.getCause());
	         System.out.println(e.getLocalizedMessage());
	         System.out.println(e.getStackTrace());
	         e.printStackTrace();
	     }
	     finally {
	         try {
	           if (ps != null)
	             ps.close();
	           if (con != null)
	             con.close();
	         } catch (Exception exc) {
	           throw new RuntimeException(exc);
	         }
	     }
	     
	     msg = "";
	     sql = "INSERT INTO fac_det_retenciones (\"ambiente\"," +
	     										"\"Ruc\"," +
	     										"\"CodEstablecimiento\"," +
	     										"\"CodPuntEmision\"," +
	     										"secuencial," +
	     										"\"CodigoDocumento\"," +
	     										"\"codImpuesto\"," +
	     										"\"secuencialRetencion\","+
	     										"\"codPorcentaje\"," +
	     										"\"baseImponible\"," +
	     										"tarifa," +
	     										"valor," +
	     										"\"porcentajeRetencion\"," +
	     										"\"numDocSustento\"," +
	     										"\"codDocSustento\"," +
	     										"\"fechaEmisionDocSustento\"" +
	     										" ) " +
	     								"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, to_date(?, 'dd/mm/yyyy'))";
	     try
	     {
	    	 con = Conexion.conectar();
	    	 for (int i = 0; i < facCab.getListImpuestosRetencion().size(); i++)
	    	 {
	    		 ps = con.prepareStatement(sql);
	    		 ps.setInt(1, facCab.getAmbiente());
	    		 ps.setString(2, facCab.getRuc());
	    		 ps.setString(3, facCab.getCodEstablecimiento());
	    		 ps.setString(4, facCab.getCodPuntEmision());
		         ps.setString(5, facCab.getSecuencial());
		         ps.setString(6, facCab.getCodigoDocumento());
		         ps.setInt(7, Integer.parseInt(facCab.getListImpuestosRetencion().get(i).getCodigo()));
		         ps.setInt(8, i+1);
		         System.out.println();
//		         ps.setInt(9, Integer.parseInt(facCab.getListImpuestosRetencion().get(i).getCodigoRetencion()));
		         ps.setString(9, facCab.getListImpuestosRetencion().get(i).getCodigoRetencion());
		         ps.setDouble(10, facCab.getListImpuestosRetencion().get(i).getBaseImponible());
		         ps.setDouble(11, facCab.getListImpuestosRetencion().get(i).getPorcentajeRetener()); //Tarifa ?
		         ps.setDouble(12, facCab.getListImpuestosRetencion().get(i).getValorRetenido());
		         ps.setDouble(13, facCab.getListImpuestosRetencion().get(i).getPorcentajeRetener());
		         ps.setString(14, facCab.getListImpuestosRetencion().get(i).getNumDocSustento());
		         if (facCab.getListImpuestosRetencion().get(i).getCodDocSustento()!=null)
		        	 ps.setString(15, facCab.getListImpuestosRetencion().get(i).getCodDocSustento());
		         else
		        	 ps.setString(15, null);
		         if (facCab.getListImpuestosRetencion().get(i).getFechaEmisionDocSustento()!=null)
		        	 ps.setString(16, facCab.getListImpuestosRetencion().get(i).getFechaEmisionDocSustento());
		         else
		        	 ps.setString(16, null);
	         
		         int r = ps.executeUpdate();
	 
		         if (r > 0) 
		        	 msg = "Se Guardo Correctamente Detalles de la "; 
		         else
		        	 msg = "Ocurrio un error al guardar los detalles de la ";
	    	 }
	     } catch (Exception e) {
	    	 System.out.println(e);
	         System.out.println(e.getMessage());
	         System.out.println(e.getCause());
	         System.out.println(e.getLocalizedMessage());
	         System.out.println(e.getStackTrace());
	         e.printStackTrace();
	     } finally {
	       try {
	         if (ps != null)
	           ps.close();
	         if (con != null)
	           con.close();
	       } catch (Exception exc) {
	         throw new RuntimeException(exc);
	       }
	     }
	     System.out.println("-- FIN ReporteSentencias.insertFacDetallesRetencionesPostgreSQL --");
	     return msg;
	   }
   
// Inserta detalle de retenciones (Impuestos)
   public String insertFacDetallesRetencionesSQLServer(FacCabDocumento facCab)
   {
	     PreparedStatement ps = null;
	     Connection con = null;
	     String msg = "";
	     String sql = "delete from fac_det_retenciones  where \"Ruc\"=?  "
	     		+ "and \"CodEstablecimiento\"=? and \"CodPuntEmision\"=? "
	     		+ "and secuencial=? and \"CodigoDocumento\"=? and \"ambiente\" = ? ";
	     try {
	    	 con = Conexion.conectar();
		     ps = con.prepareStatement(sql);
		     ps.setString(1, facCab.getRuc());
		     ps.setString(2, facCab.getCodEstablecimiento());
		     ps.setString(3, facCab.getCodPuntEmision());
		     ps.setString(4, facCab.getSecuencial());
		     ps.setString(5, facCab.getCodigoDocumento());
		     ps.setInt(6, facCab.getAmbiente());
		     int r = ps.executeUpdate();

		     if (r > 0) 
		       msg = "Se Guardo Correctamente Detalles de la "; 
		     else
		       msg = "Ocurrio un error al guardar los detalles de la ";

	     } catch (Exception e) {
	    	 System.out.println(e);
	         System.out.println(e.getMessage());
	         System.out.println(e.getCause());
	         System.out.println(e.getLocalizedMessage());
	         System.out.println(e.getStackTrace());
	         e.printStackTrace();
	       }
	     finally {
	         try {
	           if (ps != null)
	             ps.close();
	           if (con != null)
	             con.close();
	         } catch (Exception exc) {
	           throw new RuntimeException(exc);
	         }
	     }
	     msg = "";
	     
	     sql = "INSERT INTO fac_det_retenciones (\"Ruc\"," +//
	     										"\"CodEstablecimiento\"," + //
	     										"\"CodPuntEmision\"," +//
	     										"secuencial," +//
	     										"\"CodigoDocumento\"," +//
	     										"\"ambiente\", " +//
	     										"\"codImpuesto\"," +//
	     										"\"secuencialRetencion\"," +//
	     										"\"codPorcentaje\"," +
	     										"\"baseImponible\"," +
	     										"tarifa," +
	     										"valor," +
	     										"\"porcentajeRetencion\"," +	     										
	     										"\"numDocSustento\"," +
	     										"\"codDocSustento\"," +
	     										"\"fechaEmisionDocSustento\")values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; try {
	       con = Conexion.conectar(); for (int i = 0; i < facCab.getListImpuestosRetencion().size(); i++){
	         ps = con.prepareStatement(sql);
	         ps.setString(1, facCab.getRuc());
	         ps.setString(2, facCab.getCodEstablecimiento());
	         ps.setString(3, facCab.getCodPuntEmision());
	         ps.setString(4, facCab.getSecuencial());	
	         ps.setString(5, facCab.getCodigoDocumento());
	         ps.setInt(6, facCab.getAmbiente());
	         ps.setString(7, facCab.getListImpuestosRetencion().get(i).getCodigo());
	         ps.setInt(8, i+1);
	         ps.setString(9, facCab.getListImpuestosRetencion().get(i).getCodigoRetencion());
	         ps.setDouble(10, facCab.getListImpuestosRetencion().get(i).getBaseImponible());
	         ps.setDouble(11, facCab.getListImpuestosRetencion().get(i).getPorcentajeRetener()); //Tarifa ?
	         ps.setDouble(12, facCab.getListImpuestosRetencion().get(i).getValorRetenido());
	         ps.setDouble(13, facCab.getListImpuestosRetencion().get(i).getPorcentajeRetener());
	         ps.setString(14, facCab.getListImpuestosRetencion().get(i).getNumDocSustento());
	         if (facCab.getListImpuestosRetencion().get(i).getCodDocSustento()!=null) ps.setString(15, facCab.getListImpuestosRetencion().get(i).getCodDocSustento());
	         if (facCab.getListImpuestosRetencion().get(i).getFechaEmisionDocSustento()!=null) ps.setString(16, facCab.getListImpuestosRetencion().get(i).getFechaEmisionDocSustento());
	         
	         int r = ps.executeUpdate();
	         if (r > 0) 
	           msg = "Se Guardo Correctamente Detalles de la "; 
	         else
	           msg = "Ocurrio un error al guardar los detalles de la ";
	       }
	     } catch (Exception e) {
	    	 System.out.println(e);
	         System.out.println(e.getMessage());
	         System.out.println(e.getCause());
	         System.out.println(e.getLocalizedMessage());
	         System.out.println(e.getStackTrace());
	         e.printStackTrace();
	       //JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
	 
	       //String str1 = msg = null; return str1;
	     } finally {
	       try {
	         if (ps != null)
	           ps.close();
	         if (con != null)
	           con.close();
	       } catch (Exception exc) {
	         throw new RuntimeException(exc);
	       }
	     }
	     return msg;
	   }
  
   
   public String insertFacDetAdicional(List<FacDetAdicional> detallesAdicionales) {
     PreparedStatement ps = null;
     Connection con = null;
     String msg = "";
     String sql = "INSERT INTO fac_det_adicional VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
     try {
       con = Conexion.conectar();
       for (int i = 0; i < detallesAdicionales.size(); i++) {
         ps = con.prepareStatement(sql);
         ps.setString(1, ((FacDetAdicional)detallesAdicionales.get(i)).getRuc());
         ps.setString(2, ((FacDetAdicional)detallesAdicionales.get(i)).getCodEstablecimiento());
         ps.setString(3, ((FacDetAdicional)detallesAdicionales.get(i)).getCodPuntEmision());
         ps.setString(4, ((FacDetAdicional)detallesAdicionales.get(i)).getSecuencial());
         ps.setString(5, ((FacDetAdicional)detallesAdicionales.get(i)).getNombre());
         ps.setString(6, ((FacDetAdicional)detallesAdicionales.get(i)).getValor());
         ps.setInt(7, i + 1);
         ps.setString(8, ((FacDetAdicional)detallesAdicionales.get(i)).getCodigoDocumento());
         String documento = "";
         if (((FacDetAdicional)detallesAdicionales.get(0)).getCodigoDocumento().trim().equals("01")) documento = "FACTURA";
         if (((FacDetAdicional)detallesAdicionales.get(0)).getCodigoDocumento().trim().equals("04")) documento = "NOTA DE CREDITO";
         if (((FacDetAdicional)detallesAdicionales.get(0)).getCodigoDocumento().trim().equals("05")) documento = "NOTA DE DEBITO";
         if (((FacDetAdicional)detallesAdicionales.get(0)).getCodigoDocumento().trim().equals("06")) documento = "GUIA DE REMISION";
         if (((FacDetAdicional)detallesAdicionales.get(0)).getCodigoDocumento().trim().equals("07")) documento = "COMPORBANTE DE RETENECION";
         int r = ps.executeUpdate();
         if (r > 0) msg = "Se Guardo correctamente detalles adicionales de la " + documento; else
           msg = "Ocurrio un error al guardar \n detalles adiciaonales de la " + documento;
       }
     } catch (Exception e) {
       JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
 
       String str1 = msg = null; return str1;
     } finally {
       try {
         if (ps != null)
           ps.close();
         if (con != null)
           con.close();
       } catch (Exception exc) {
         throw new RuntimeException(exc);
       }
     }
     return msg;
   }
 
	 public String insertFacDetRetencion(List<FacDetRetencione> retenciones) {
     Connection con = null;
     PreparedStatement ps = null;
     String msg = "";
     String sql = "INSERT INTO fac_det_retenciones VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
     try {
       con = Conexion.conectar();
       for (int i = 0; i < retenciones.size(); i++) {
         ps = con.prepareStatement(sql);
         ps.setString(1, ((FacDetRetencione)retenciones.get(i)).getRuc());
         ps.setString(2, ((FacDetRetencione)retenciones.get(i)).getCodEstablecimiento());
         ps.setString(3, ((FacDetRetencione)retenciones.get(i)).getCodPuntEmision());
         ps.setString(4, ((FacDetRetencione)retenciones.get(i)).getSecuencial());
         ps.setInt(5, ((FacDetRetencione)retenciones.get(i)).getCodImpuesto().intValue());
         ps.setInt(6, ((FacDetRetencione)retenciones.get(i)).getCodPorcentaje().intValue());
         ps.setDouble(7, ((FacDetRetencione)retenciones.get(i)).getBaseImponible());
         ps.setDouble(8, ((FacDetRetencione)retenciones.get(i)).getTarifa());
         ps.setDouble(9, ((FacDetRetencione)retenciones.get(i)).getValor());
         ps.setDouble(10, ((FacDetRetencione)retenciones.get(i)).getPorcentajeRetencion());
         ps.setInt(11, i + 1);
         ps.setString(12, ((FacDetRetencione)retenciones.get(i)).getCodigoDocumento());
         int r = ps.executeUpdate();
         if (r > 0) msg = "Se Guardo correctamente detalles de retenciones"; else
           msg = "Ocurrio un error al guardar";
       }
     } catch (Exception e) {
       JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
 
       String str1 = msg = null; return str1;
     } finally {
       try {
         if (ps != null)
           ps.close();
         if (con != null)
           con.close();
       } catch (Exception exc) {
         throw new RuntimeException(exc);
       }
     }
     return msg;
   }
 
	 public String insertFacDetMotivodebito(List<FacDetMotivosdebito> debito) {
     Connection con = null;
     PreparedStatement ps = null;
     String msg = "";
     String sql = "INSERT INTO fac_det_motivosdebito VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
     try {
       con = Conexion.conectar();
       int r = 0;
       for (int i = 0; i < debito.size(); i++) {
         ps = con.prepareStatement(sql);
         ps.setString(1, ((FacDetMotivosdebito)debito.get(i)).getRuc());
         ps.setString(2, ((FacDetMotivosdebito)debito.get(i)).getCodEstablecimiento());
         ps.setString(3, ((FacDetMotivosdebito)debito.get(i)).getCodPuntEmision());
         ps.setString(4, ((FacDetMotivosdebito)debito.get(i)).getSecuencial());
         ps.setString(5, ((FacDetMotivosdebito)debito.get(i)).getCodigoDocumento());
         ps.setInt(6, i + 1);
         ps.setString(7, ((FacDetMotivosdebito)debito.get(i)).getRazon());
         ps.setInt(8, ((FacDetMotivosdebito)debito.get(i)).getCodImpuesto().intValue());
         ps.setInt(9, ((FacDetMotivosdebito)debito.get(i)).getCodPorcentaje().intValue());
         ps.setDouble(10, ((FacDetMotivosdebito)debito.get(i)).getBaseImponible());
         ps.setInt(11, ((FacDetMotivosdebito)debito.get(i)).getTarifa().intValue());
         ps.setDouble(12, ((FacDetMotivosdebito)debito.get(i)).getValor());
         ps.setString(13, ((FacDetMotivosdebito)debito.get(i)).getTipoImpuestos());
         r = ps.executeUpdate();
       }
       if (r > 0)
         msg = "Se Guardo correctamente detalles debitos";
       else
         msg = "Ocurrio un error al guardar";
     } catch (Exception e) {
       JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
 
       String str1 = msg = null; return str1;
     } finally {
       try {
         if (ps != null)
           ps.close();
         if (con != null)
           con.close();
       } catch (Exception exc) {
         throw new RuntimeException(exc);
       }
     }
     return msg;
   }
 
	 public String modificarFacCabDocumentos(FacCabDocumento cabDoc) {
     PreparedStatement ps = null;
     Connection con = null;
     String msg = "";
     ReporteServicio servicio = new ReporteServicio();
     FacCabDocumento documentos = new FacCabDocumento();     
     String sql = "UPDATE fac_cab_documentos SET ambiente=NULLIF(?,ambiente), \"TipoIdentificacion\"=NULLIF(?,\"TipoIdentificacion\"), \"fechaEmision\"=NULLIF(?,\"fechaEmision\"), " +
     			  " \"guiaRemision\"=NULLIF(?,\"guiaRemision\"),\"razonSocialComprador\"=NULLIF(?,\"razonSocialComprador\"),\"identificacionComprador\"=NULLIF(?,\"identificacionComprador\"), " +
     			  " \"totalSinImpuesto\"=NULLIF(?,\"totalSinImpuesto\"),\"totalDescuento\"=NULLIF(?,\"totalDescuento\"), email=NULLIF(?,email), propina=NULLIF(?,propina), moneda=NULLIF(?,moneda), " +
     			  " \"infoAdicional\"=NULLIF(?,\"infoAdicional\"),\"periodoFiscal\"=NULLIF(?,\"periodoFiscal\"), rise=NULLIF(?,rise), \"fechaInicioTransporte\"=NULLIF(?,\"fechaInicioTransporte\"), '" +
     			  " \"fechaFinTransporte\"=IFNULL(?,\"fechaFinTransporte\") , placa=IFNULL(?,placa), \"fechaEmisionDocSustento\"=IFNULL(?,\"fechaEmisionDocSustento\"), \"motivoRazon\"=IFNULL(?,\"motivoRazon\"), " +
     			  " \"identificacionDestinatario\"=IFNULL(?,\"identificacionDestinatario\"),\"razonSocialDestinatario\"=IFNULL(?,\"razonSocialDestinatario\"), \"direccionDestinatario\"=IFNULL(?,\"direccionDestinatario\"), " +
     			  " \"motivoTraslado\"=IFNULL(?,\"motivoTraslado\"),\"docAduaneroUnico\"=IFNULL(?,\"docAduaneroUnico\"), \"codEstablecimientoDest\"=IFNULL(?,\"codEstablecimientoDest\"), ruta=IFNULL(?,ruta), " +
     			  " \"codDocSustento\"=IFNULL(?,\"codDocSustento\"), \"numDocSustento\"=IFNULL(?,\"numDocSustento\"), \"numAutDocSustento\"=IFNULL(?,\"numAutDocSustento\"), \"fecEmisionDocSustento\"=IFNULL(?,\"fecEmisionDocSustento\"), " +
     			  " autorizacion=IFNULL(?,autorizacion), fechaautorizacion=IFNULL(?,fechaautorizacion), \"claveAcceso\"=IFNULL(?,\"claveAcceso\"), \"importeTotal\"=IFNULL(?,\"importeTotal\"),\"codDocModificado\"=IFNULL(?,\"codDocModificado\"), " +
     			  " \"numDocModificado\"=IFNULL(?,\"numDocModificado\"), \"motivoValor\"=IFNULL(?,\"motivoValor\"), \"tipIdentificacionComprador\"=IFNULL(?,\"tipIdentificacionComprador\"),\"tipoEmision\"=IFNULL(?,\"tipoEmision\") , partida=IFNULL(?, partida), " +
     			  " subtotal12=IFNULL(?,subtotal12), subtotal0=IFNULL(?,subtotal0), \"subtotalNoIva\"=IFNULL(?,\"subtotalNoIva\"), \"totalvalorICE\"=IFNULL(?,\"totalvalorICE\"), iva12=IFNULL(?,iva12), \"isActive\"=IFNULL(?,\"isActive\"), \"ESTADO_TRANSACCION\"=IFNULL(?,\"ESTADO_TRANSACCION\"), " +
     			  " \"MSJ_ERROR\"=IFNULL(?,\"MSJ_ERROR\") WHERE \"Ruc\"=? And \"CodEstablecimiento\"=? And \"CodPuntEmision\"=? And secuencial=? And \"CodigoDocumento\"=? ";
     try
     {
       documentos = servicio.buscarDatosCabDocumentos(cabDoc.getRuc(), cabDoc.getCodEstablecimiento(), cabDoc.getCodPuntEmision(), cabDoc.getCodigoDocumento(), cabDoc.getSecuencial());
       con = Conexion.conectar();
       ps = con.prepareStatement(sql);
       if ((cabDoc.getAmbiente()==null)||(cabDoc.getAmbiente().equals(""))){
    	   ps.setNull(1, java.sql.Types.NUMERIC);
       }else{
    	   ps.setInt(1, (cabDoc.getAmbiente().intValue() == 0 ? documentos.getAmbiente() : cabDoc.getAmbiente()).intValue());
       }
       
       if ((cabDoc.getTipoIdentificacion()==null)||(cabDoc.getTipoIdentificacion().equals(""))){
    	   ps.setNull(2, java.sql.Types.VARCHAR);
       }else{
    	   ps.setString(2, cabDoc.getTipoIdentificacion() == null ? documentos.getTipoIdentificacion() : documentos.getTipoIdentificacion().length()<=0 ? "" : cabDoc.getTipoIdentificacion());
       }
       
       if ((cabDoc.getTipoIdentificacion()==null)||(cabDoc.getTipoIdentificacion().equals(""))){
    	   ps.setNull(3, java.sql.Types.VARCHAR);
       }else{
    	   ps.setString(3, cabDoc.getFechaEmision());
       }
       
       if ((cabDoc.getTipoIdentificacion()==null)||(cabDoc.getTipoIdentificacion().equals(""))){
    	   ps.setNull(4, java.sql.Types.VARCHAR);
       }else{
    	   ps.setString(4, cabDoc.getGuiaRemision() != null ? cabDoc.getGuiaRemision() : documentos.getGuiaRemision());
       }
       ps.setString(5, cabDoc.getRazonSocialComprador() != null ? cabDoc.getRazonSocialComprador() : documentos.getRazonSocialComprador());
       ps.setString(6, cabDoc.getIdentificacionComprador() != null ? cabDoc.getIdentificacionComprador() : documentos.getIdentificacionComprador());
       ps.setDouble(7, cabDoc.getTotalSinImpuesto() != 0.0D ? cabDoc.getTotalSinImpuesto() : documentos.getTotalSinImpuesto());
       ps.setDouble(8, cabDoc.getTotalDescuento() != 0.0D ? cabDoc.getTotalDescuento() : documentos.getTotalDescuento());
       ps.setString(9, cabDoc.getEmail() != null ? cabDoc.getEmail() : documentos.getEmail());
       ps.setDouble(10, cabDoc.getPropina() != 0.0D ? cabDoc.getPropina() : documentos.getPropina());
       ps.setString(11, (cabDoc.getMoneda() != null ? cabDoc.getMoneda() : documentos.getMoneda()));
       ps.setString(12, cabDoc.getInfoAdicional() != null ? cabDoc.getInfoAdicional() : documentos.getInfoAdicional());
       ps.setString(13, cabDoc.getPeriodoFiscal() != null ? cabDoc.getPeriodoFiscal() : documentos.getPeriodoFiscal());
       ps.setString(14, cabDoc.getRise() != null ? cabDoc.getRise() : documentos.getRise());
       ps.setString(15, cabDoc.getFechaInicioTransporte());
       ps.setString(16, cabDoc.getFechaFinTransporte());
       ps.setString(17, cabDoc.getPlaca() != null ? cabDoc.getPlaca() : documentos.getPlaca());
       ps.setString(18, cabDoc.getFecEmisionDocSustento());
       ps.setString(19, cabDoc.getMotivoRazon() != null ? cabDoc.getMotivoRazon() : documentos.getMotivoRazon());
       ps.setString(20, cabDoc.getIdentificacionDestinatario() != null ? cabDoc.getIdentificacionDestinatario() : documentos.getIdentificacionDestinatario());
       ps.setString(21, cabDoc.getRazonSocialDestinatario() != null ? cabDoc.getRazonSocialDestinatario() : documentos.getRazonSocialDestinatario());
       ps.setString(22, cabDoc.getDireccionDestinatario() != null ? cabDoc.getDireccionDestinatario() : documentos.getDireccionDestinatario());
       ps.setString(23, cabDoc.getMotivoTraslado() != null ? cabDoc.getMotivoTraslado() : documentos.getMotivoTraslado());
       ps.setString(24, cabDoc.getDocAduaneroUnico() != null ? cabDoc.getDocAduaneroUnico() : documentos.getDocAduaneroUnico());
       ps.setString(25, cabDoc.getCodEstablecimientoDest() != null ? cabDoc.getCodEstablecimientoDest() : documentos.getCodEstablecimientoDest());
       ps.setString(26, cabDoc.getRuta() != null ? cabDoc.getRuta() : documentos.getRuta());
       ps.setString(27, cabDoc.getCodDocSustento() != null ? cabDoc.getCodDocSustento() : documentos.getCodDocSustento());
       ps.setString(28, cabDoc.getNumDocSustento() != null ? cabDoc.getNumDocSustento() : documentos.getNumDocSustento());
       ps.setString(29, cabDoc.getNumAutDocSustento() != null ? cabDoc.getNumAutDocSustento() : documentos.getNumAutDocSustento());
       ps.setString(30, (cabDoc.getFecEmisionDocSustento() != null ? cabDoc.getFecEmisionDocSustento() : documentos.getFecEmisionDocSustento()));
       ps.setString(31, cabDoc.getAutorizacion() != null ? cabDoc.getAutorizacion() : documentos.getAutorizacion());
       ps.setString(32, (cabDoc.getFechaautorizacion() != null ? cabDoc.getFechaautorizacion() : documentos.getFechaautorizacion()));
       ps.setString(33, cabDoc.getClaveAcceso() != null ? cabDoc.getClaveAcceso() : documentos.getClaveAcceso());
       ps.setDouble(34, cabDoc.getImporteTotal() != 0.0D ? cabDoc.getImporteTotal() : documentos.getImporteTotal());
       ps.setString(35, cabDoc.getCodDocModificado() != null ? cabDoc.getCodDocModificado() : documentos.getCodDocModificado());
       ps.setString(36, cabDoc.getNumDocModificado() != null ? cabDoc.getNumDocModificado() : documentos.getNumDocModificado());
       ps.setDouble(37, cabDoc.getMotivoValor() != 0.0D ? cabDoc.getMotivoValor() : documentos.getMotivoValor());
       ps.setString(38, cabDoc.getTipIdentificacionComprador() != null ? cabDoc.getTipIdentificacionComprador() : documentos.getTipIdentificacionComprador());
       ps.setString(39, cabDoc.getTipoEmision() != null ? cabDoc.getTipoEmision() : documentos.getTipoEmision());
       ps.setString(40, cabDoc.getPartida() != null ? cabDoc.getPartida() : documentos.getPartida());
       ps.setDouble(41, cabDoc.getSubtotal12() != 0.0D ? cabDoc.getSubtotal12() : documentos.getSubtotal12());
       ps.setDouble(42, cabDoc.getSubtotal0() != 0.0D ? cabDoc.getSubtotal0() : documentos.getSubtotal0());
       ps.setDouble(43, cabDoc.getSubtotalNoIva() != 0.0D ? cabDoc.getSubtotalNoIva() : documentos.getSubtotalNoIva());
       ps.setDouble(44, cabDoc.getTotalvalorICE() != 0.0D ? cabDoc.getTotalvalorICE() : documentos.getTotalvalorICE());
       ps.setDouble(45, cabDoc.getIva12() != 0.0D ? cabDoc.getIva12() : documentos.getIva12());
       ps.setString(46, cabDoc.getIsActive() != null ? cabDoc.getIsActive() : documentos.getIsActive());
       ps.setString(47, cabDoc.getESTADO_TRANSACCION() != null ? cabDoc.getESTADO_TRANSACCION() : documentos.getESTADO_TRANSACCION());
       ps.setString(48, cabDoc.getMSJ_ERROR() != null ? cabDoc.getMSJ_ERROR() : documentos.getMSJ_ERROR());
 
       ps.setString(49, cabDoc.getRuc());
       ps.setString(50, cabDoc.getCodEstablecimiento());
       ps.setString(51, cabDoc.getCodPuntEmision());
       ps.setString(52, cabDoc.getSecuencial());
       ps.setString(53, cabDoc.getCodigoDocumento());
 
       String documento = "";
       if (cabDoc.getCodigoDocumento().trim().equals("01")) documento = "FACTURA";
       if (cabDoc.getCodigoDocumento().trim().equals("04")) documento = "NOTA DE CREDITO";
       if (cabDoc.getCodigoDocumento().trim().equals("05")) documento = "NOTA DE DEBITO";
       if (cabDoc.getCodigoDocumento().trim().equals("06")) documento = "GUIA DE REMISION";
       if (cabDoc.getCodigoDocumento().trim().equals("07")) documento = "COMPORBANTE DE RETENECION";
 
       int r = ps.executeUpdate();
       if (r > 0) msg = "Se modifico Correctamente la " + documento; else
         msg = "Ocurrio un error al modificar la " + documento;
     }
     catch (Exception e) {
       JOptionPane.showMessageDialog(null, "Error de modificar el documentos \n" + e.getLocalizedMessage());
       e.printStackTrace();
       String str1 = msg = null; return str1;
     } finally {
       try {
         if (ps != null) ps.close();
         if (con != null) con.close(); 
       }
       catch (Exception exc) { throw new RuntimeException(exc); }
 
     }
     return msg;
   }
 
	 public String modificarDetallesDocumentos(List<FacDetDocumento> detalles) {
     PreparedStatement ps = null;
     Connection con = null;
     String msg = "";
     String sql = "UPDATE fac_det_documentos SET  \"CodPrincipal\"=?, \"CodAuxiliar\"=?, descripcion=?, cantidad=?,\"precioUnitario\"=?, descuento=?, \"precioTotalSinImpuesto\"=?, \"valorIce\"=? WHERE\"Ruc\"=? AND \"CodEstablecimiento\"=? AND \"CodPuntEmision\"=? AND secuencial=? AND \"CodigoDocumento\"=? AND \"secuencialDetalle\"=?";
     try
     {
       con = Conexion.conectar();
       for (int i = 0; i < detalles.size(); i++) {
         ps = con.prepareStatement(sql);
 
         ps.setString(1, ((FacDetDocumento)detalles.get(i)).getCodPrincipal());
         ps.setString(2, ((FacDetDocumento)detalles.get(i)).getCodAuxiliar());
         ps.setString(3, ((FacDetDocumento)detalles.get(i)).getDescripcion());
         ps.setInt(4, ((FacDetDocumento)detalles.get(i)).getCantidad().intValue());
         ps.setDouble(5, ((FacDetDocumento)detalles.get(i)).getPrecioUnitario());
         ps.setDouble(6, ((FacDetDocumento)detalles.get(i)).getDescuento());
         ps.setDouble(7, ((FacDetDocumento)detalles.get(i)).getPrecioTotalSinImpuesto());
         ps.setDouble(8, ((FacDetDocumento)detalles.get(i)).getValorIce());
 
         ps.setString(9, ((FacDetDocumento)detalles.get(i)).getRuc());
         ps.setString(10, ((FacDetDocumento)detalles.get(i)).getCodEstablecimiento());
         ps.setString(11, ((FacDetDocumento)detalles.get(i)).getCodPuntEmision());
         ps.setString(12, ((FacDetDocumento)detalles.get(i)).getSecuencial());
         ps.setString(13, ((FacDetDocumento)detalles.get(i)).getCodigoDocumento());
         ps.setInt(14, i + 1);
 
         String documento = "";
         if (((FacDetDocumento)detalles.get(0)).getCodigoDocumento().trim().equals("01")) documento = "FACTURA";
         if (((FacDetDocumento)detalles.get(0)).getCodigoDocumento().trim().equals("04")) documento = "NOTA DE CREDITO";
         if (((FacDetDocumento)detalles.get(0)).getCodigoDocumento().trim().equals("05")) documento = "NOTA DE DEBITO";
         if (((FacDetDocumento)detalles.get(0)).getCodigoDocumento().trim().equals("06")) documento = "GUIA DE REMISION";
         if (((FacDetDocumento)detalles.get(0)).getCodigoDocumento().trim().equals("07")) documento = "COMPORBANTE DE RETENECION";
 
         int r = ps.executeUpdate();

         if (r > 0) msg = "Se modifico Correctamente Detalles de la " + documento; else
           msg = "Ocurrio un error al guardar los detalles de la " + documento;
       }
     } catch (Exception e) {
       JOptionPane.showMessageDialog(null, "Error de Detalles Documentos \n" + e.getLocalizedMessage());
       e.printStackTrace();
       String str1 = msg = null; return str1;
     } finally {
       try {
         if (ps != null) ps.close();
         if (con != null) con.close(); 
       }
       catch (Exception exc) { throw new RuntimeException(exc); }
 
     }
     return msg;
   }
 
	 public String modificarDetallesAdicionales(List<FacDetAdicional> detallesAdicionales) {
     PreparedStatement ps = null;
     Connection con = null;
     String msg = "";
     String sql = "UPDATE fac_det_adicional SET nombre=?, valor=? WHERE \"Ruc\"=? AND \"CodEstablecimiento\"=? AND \"CodPuntEmision\"=?  AND secuencial=? AND \"secuencialDetAdicional\"=? AND \"CodigoDocumento\"=?";
     try
     {
       con = Conexion.conectar();
       for (int i = 0; i < detallesAdicionales.size(); i++) {
         ps = con.prepareStatement(sql);
         ps.setString(1, ((FacDetAdicional)detallesAdicionales.get(i)).getNombre());
         ps.setString(2, ((FacDetAdicional)detallesAdicionales.get(i)).getValor());
         ps.setString(3, ((FacDetAdicional)detallesAdicionales.get(i)).getRuc());
         ps.setString(4, ((FacDetAdicional)detallesAdicionales.get(i)).getCodEstablecimiento());
         ps.setString(5, ((FacDetAdicional)detallesAdicionales.get(i)).getCodPuntEmision());
         ps.setString(6, ((FacDetAdicional)detallesAdicionales.get(i)).getSecuencial());
         ps.setInt(7, i + 1);
         ps.setString(8, ((FacDetAdicional)detallesAdicionales.get(i)).getCodigoDocumento());
 
         String documento = "";

         if (((FacDetAdicional)detallesAdicionales.get(0)).getCodigoDocumento().trim().equals("01")) documento = "FACTURA";
         if (((FacDetAdicional)detallesAdicionales.get(0)).getCodigoDocumento().trim().equals("04")) documento = "NOTA DE CREDITO";
         if (((FacDetAdicional)detallesAdicionales.get(0)).getCodigoDocumento().trim().equals("05")) documento = "NOTA DE DEBITO";
         if (((FacDetAdicional)detallesAdicionales.get(0)).getCodigoDocumento().trim().equals("06")) documento = "GUIA DE REMISION";
         if (((FacDetAdicional)detallesAdicionales.get(0)).getCodigoDocumento().trim().equals("07")) documento = "COMPORBANTE DE RETENECION";

         int r = ps.executeUpdate();
         if (r > 0) msg = "Se modifico correctamente detalles adicionales de la " + documento;
         else
         {
           msg = "Ocurrio un error al modificar detalles adiciaonales de la " + documento;
         }
       }
     } catch (Exception e) {
       JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
       e.printStackTrace();
       String str1 = msg = null; return str1;
     } finally {
       try {
         if (ps != null)
           ps.close();
         if (con != null)
           con.close();
       } catch (Exception exc) {
         throw new RuntimeException(exc);
       }
     }
     return msg;
   }
 
	 public String modificarDetallesNotaDebitos(List<FacDetMotivosdebito> debito) {
     PreparedStatement ps = null;
     Connection con = null;
     String msg = "";
     String sql = "UPDATE fac_det_motivosdebito SET razon=?, \"codImpuesto\"=?, \"codPorcentaje\"=?, \"baseImponible\"=?, tarifa=?, valor=?, \"tipoImpuestos\"=? WHERE \"Ruc\"=? AND  \"CodEstablecimiento\"=? AND \"CodPuntEmision\"=? AND secuencial=? AND \"CodigoDocumento\"=? AND \"secuencialDetalle\"=? ";
     try
     {
       con = Conexion.conectar();
       int r = 0;
       for (int i = 0; i < debito.size(); i++) {
         ps = con.prepareStatement(sql);
         ps.setString(1, ((FacDetMotivosdebito)debito.get(i)).getRazon());
         ps.setInt(2, ((FacDetMotivosdebito)debito.get(i)).getCodImpuesto().intValue());
         ps.setInt(3, ((FacDetMotivosdebito)debito.get(i)).getCodPorcentaje().intValue());
         ps.setDouble(4, ((FacDetMotivosdebito)debito.get(i)).getBaseImponible());
         ps.setInt(5, ((FacDetMotivosdebito)debito.get(i)).getTarifa().intValue());
         ps.setDouble(6, ((FacDetMotivosdebito)debito.get(i)).getValor());
         ps.setString(7, ((FacDetMotivosdebito)debito.get(i)).getTipoImpuestos());

         ps.setString(8, ((FacDetMotivosdebito)debito.get(i)).getRuc());
         ps.setString(9, ((FacDetMotivosdebito)debito.get(i)).getCodEstablecimiento());
         ps.setString(10, ((FacDetMotivosdebito)debito.get(i)).getCodPuntEmision());
         ps.setString(11, ((FacDetMotivosdebito)debito.get(i)).getSecuencial());
         ps.setString(12, ((FacDetMotivosdebito)debito.get(i)).getCodigoDocumento());
         ps.setInt(13, i + 1);

         r = ps.executeUpdate();
       }
       if (r > 0) msg = "Se modifico correctamente detalles NOTA DEBITO"; else
         msg = "Ocurrio un error al guardar";
     } catch (Exception e) {
       JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
       e.printStackTrace();
       String str1 = msg = null; return str1;
     } finally {
       try {
         if (ps != null) ps.close();
         if (con != null) con.close(); 
       }
       catch (Exception exc) { throw new RuntimeException(exc); }
 
     }
     return msg;
   }
 
	 public String modificarDetallesReteciones(List<FacDetRetencione> retenciones) {
     PreparedStatement ps = null;
     Connection con = null;
     String msg = "";
     String sql = "UPDATE fac_det_retenciones SET \"codImpuesto\"=?, \"codPorcentaje\"=?, \"baseImponible\"=?, tarifa=?, valor=?, \"porcentajeRetencion\"=?  WHERE \"Ruc\"=? AND  \"CodEstablecimiento\"=? AND  \"CodPuntEmision\"=? AND  secuencial=? AND \"secuencialRetencion\"=? AND  \"CodigoDocumento\"=?";
     try
     {
       con = Conexion.conectar();
       for (int i = 0; i < retenciones.size(); i++) {
         ps = con.prepareStatement(sql);
 
        ps.setInt(1, ((FacDetRetencione)retenciones.get(i)).getCodImpuesto().intValue());
        ps.setInt(2, ((FacDetRetencione)retenciones.get(i)).getCodPorcentaje().intValue());
        ps.setDouble(3, ((FacDetRetencione)retenciones.get(i)).getBaseImponible());
        ps.setDouble(4, ((FacDetRetencione)retenciones.get(i)).getTarifa());
        ps.setDouble(5, ((FacDetRetencione)retenciones.get(i)).getValor());
        ps.setDouble(6, ((FacDetRetencione)retenciones.get(i)).getPorcentajeRetencion());
        ps.setString(7, ((FacDetRetencione)retenciones.get(i)).getRuc());
        ps.setString(8, ((FacDetRetencione)retenciones.get(i)).getCodEstablecimiento());
        ps.setString(9, ((FacDetRetencione)retenciones.get(i)).getCodPuntEmision());
        ps.setString(10, ((FacDetRetencione)retenciones.get(i)).getSecuencial());
        ps.setInt(11, i + 1);
        ps.setString(12, ((FacDetRetencione)retenciones.get(i)).getCodigoDocumento());

        int r = ps.executeUpdate();

        if (r > 0)
          msg = "Se modifico correctamente detalles de RETENCIONES";
         else
           msg = "Ocurrio un error al guardar";
       }
     } catch (Exception e) {
       JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
       e.printStackTrace();
       String str1 = msg = null; return str1;
     } finally {
       try {
         if (ps != null)
           ps.close();
         if (con != null)
           con.close();
       } catch (Exception exc) {
         throw new RuntimeException(exc);
       }
     }
     return msg;
   }
   
   
	 public int existeCliente(String ps_ruc,
			 				  String ps_rucCliente,
			 				  String ps_tipoCliente) //throws SQLException, IOException, NamingException, ClassNotFoundException
	 {
		 System.out.println("-- INICIO ReporteSentencias.existeCliente --");
		
		 int cantidad = -1;
		 ResultSet Rs= null;
		 PreparedStatement pst = null;
		 Connection Con = null;
		 try
		 {
		 	Con = Conexion.conectar();  	
		 	String sql = "Select count(1) as cantidad from fac_clientes where \"Ruc\"=? and \"RucCliente\"=? and \"TipoCliente\" = ? ";
	    	pst = Con.prepareStatement(sql);
	    	pst.setString(1, ps_ruc);
	    	pst.setString(2, ps_rucCliente);
	    	pst.setString(3, ps_tipoCliente);
	    	
	    	Rs= pst.executeQuery();
	    	while (Rs.next()){
	    		cantidad = Rs.getInt("cantidad");
	    	}
	    	
		 }catch(Exception e)
		 {
			 System.out.println("  cantidad: "+cantidad);
	    	 System.out.println(e);
	         System.out.println(e.getMessage());
	         System.out.println(e.getCause());
	         System.out.println(e.getLocalizedMessage());
	         System.out.println(e.getStackTrace());
	         e.printStackTrace();
		 } finally {
			 try {
				if (pst != null)
					pst.close();
				if (Con != null)
					Con.close();
			} catch (Exception ex) {
				System.out.println("ReporteSentencias.existeCliente() >> Error al cerrar conexiones");
			}
		}  	   
		 System.out.println("-- FIN ReporteSentencias.existeCliente --");
		return cantidad;
	}
	 
	 
	 public int existeUsuario(String ps_ruc, String ps_codCliente,
			   String ps_tipoCliente)
	 {
		 //VPI - Se modifica para controlar excepcion en el bloque
		 int cantidad = -1;
		 ResultSet Rs = null;
		 PreparedStatement pst = null;
		 //Connection Con = ConexionBase.DBManager.get();
		 Connection Con = null; 
		 
		 try {
			 Con = Conexion.conectar();
			 
			 String sql = "Select count(1) as cantidad from fac_usuarios where \"Ruc\"=? and \"CodUsuario\"=? and \"TipoUsuario\" = ? ";
			 pst = Con.prepareStatement(sql);
			 pst.setString(1, ps_ruc);
			 pst.setString(2, ps_codCliente);
			 pst.setString(3, ps_tipoCliente);

			 Rs = pst.executeQuery();
			 while (Rs.next()) {
			    cantidad = Rs.getInt("cantidad");
			 }
		} catch (Exception e) {
			System.out.println("ReporteSentencias.existeUsuario() >> Error al consultar Usuario  - \n Ruc: "+ps_ruc+" \n Cod Cliente: "+ps_codCliente+" \n Tipo cliente"+ps_tipoCliente);
			e.printStackTrace();
		} finally {
			try {
				if (pst != null)
					pst.close();
			} catch (Exception ex) {
			    System.out.println("ReporteSentencias.existeUsuario() >> Error al cerrar conexiones");
			}
		}
		 return cantidad;
	 }
	 
   /*
    "Ruc" character varying(20) NOT NULL, -- Ruc del Cliente
  "RazonSocial" character varying(300), -- Razon Social del Cliente
  "Direccion" character varying(300), -- Direccion del Cliente
  "Email" character varying(100), -- Email del Cliente
  "TipoCliente" character(1), -- Tipo de Cliente Cliente (C)/ Transportista (T) / Proveedor (P)
  "TipoIdentificacion" character(2), -- Tipo de Identificacion del Cliente
  "Rise" character varying(40), -- Codigo del Rise del Cliente
  "Telefono" character varying(10), -- Telefono del Cliente
  "RucCliente" character varying(13) NOT NULL,
  "isActive" character(1),
    * */
   public String insertaUsuarioRol(String ruc, 
		   						   String nameCliente,  
		   						   String rucCliente, 
		   						   String clave,
		   						   String rol,
		   						   String tipoCliente)
   {
	   System.out.println("-- INICIO ReporteSentencias.insertaUsuarioRol --");
	   PreparedStatement ps = null;
	   Connection con = null;
	   String msg = "";	     		
	   String sql = " insert into fac_usuarios(\"Ruc\",\"CodUsuario\",\"Nombre\",\"TipoUsuario\",\"isActive\",\"Password\"," +
				 " \"RucEmpresa\") values (?,?,?,?,?,?,?) " ;
	   
	   try
	   {
		   con = Conexion.conectar();
		   ps = con.prepareStatement(sql);	 
		   ps.setString(1, ruc);
		   ps.setString(2, rucCliente);
		   ps.setString(3, nameCliente);
		   ps.setString(4, tipoCliente);
		   ps.setString(5, "Y");
		   ps.setString(6, clave);
		   ps.setString(7, rucCliente);
			
		   int r = ps.executeUpdate();
			
		   if (r > 0)
			   msg = "  Se inserto correctamente al fac_usuarios ";
		   else
			   msg = "  Ocurrio un error al guardar en fac_usuarios ";
			
	   } catch (Exception e) {
			msg = "  Ocurrio una Exception al guardar en fac_usuarios ";
			e.printStackTrace();
	   } finally {
		   try {
			   if (ps != null)
				   ps.close();
			   if (con != null)
				   con.close();
		   } catch (Exception exc) {
				System.out.println("  ReporteSentencias.insertaUsuarioRol() >> Error al cerrar conexiones");
		   }
	   }
		String msg2 = "";
	   sql = " insert into fac_usuarios_roles(\"Ruc\",\"CodUsuario\",\"CodRol\",\"isActive\") values (?,?,?,?) " ;
	   try
	   {
		   con = Conexion.conectar();
		   ps = con.prepareStatement(sql);	 
		   ps.setString(1, ruc);
		   ps.setString(2, rucCliente);
		   ps.setString(3, rol);
		   ps.setString(4, "Y");
			
		   int r = ps.executeUpdate();
			
		   if (r > 0)
			  msg2 = "Inserto correctamente al fac_usuarios_roles ";
			else
				msg2 = "Ocurrio un error al guardar en fac_usuarios_roles ";
			
	   } catch (Exception e) {
			msg2 = "Ocurrio una Exception al guardar en fac_usuarios_roles ";
		   e.printStackTrace();
	   } finally {
		   try {
			   if (ps != null)
				   ps.close();
			   if (con != null)
				   con.close();
		   } catch (Exception exc) {
				System.out.println("ReporteSentencias.insertaUsuarioRol() >> Error al cerrar conexiones");
		   }
	   }
	   System.out.println("-- FIN ReporteSentencias.insertaUsuarioRol --");
		return msg + " <<>> " + msg2;
   }
   
   public String insertaClientes(String ruc,
		   						 String razonSocial,
		   						 String direccion,
		   						 String email,
		   						 String tipoCliente,
		   						 String tipoIdentificacion,
		   						 String rise,
		   						 String telefono,
		   						 String rucCliente,
		   						 String codCliente)
   {
	   System.out.println("-- INICIO ReporteSentencias.insertaClientes --");
	   PreparedStatement ps = null;
	     Connection con = null;
	     String msg = "";
	     String sql = " insert into fac_clientes(\"Ruc\",\"RazonSocial\",\"Direccion\",\"Email\",\"TipoCliente\",\"TipoIdentificacion\"," +
	     				" \"Rise\",\"Telefono\",\"RucCliente\",\"codCliente\",\"isActive\") values (?,?,?,?,?,?,?,?,?,?,?) " ;
	     try
	     {
	    	con = Conexion.conectar();
	        ps = con.prepareStatement(sql);	 
	        ps.setString(1, ruc);
	        ps.setString(2, razonSocial);
	        ps.setString(3, direccion);
	        if ((email == null)||(email.equals("notiene")))
	        	ps.setNull(4, java.sql.Types.VARCHAR);
	        else
	        	ps.setString(4, email);
	        ps.setString(5, tipoCliente);
	        ps.setString(6, tipoIdentificacion);
	        if (rise == null)
	        	ps.setNull(7, java.sql.Types.VARCHAR);
	        else
	        	ps.setString(7, rise);
	        if(telefono != null)
	        	ps.setString(8, telefono.replace("||", "-"));
	        else
	        	ps.setNull(8, java.sql.Types.VARCHAR);
	        ps.setString(9, rucCliente);
	        ps.setString(10, codCliente);
	        ps.setString(11, "Y");
	        
	        int r = ps.executeUpdate();
	        
	        if (r > 0)
		          msg = "Se inserto correctamente al Cliente";
		         else
		           msg = "Ocurrio un error al guardar";
	       
	     } catch (Exception e) {
	    	 //JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
	    	 System.out.println("  "+rucCliente);
	    	 System.out.println(e);
	         System.out.println(e.getMessage());
	         System.out.println(e.getCause());
	         System.out.println(e.getLocalizedMessage());
	         System.out.println(e.getStackTrace());
	         e.printStackTrace();
	         
	         String str1 = msg = null;
	         System.out.println("-- FIN ReporteSentencias.insertaClientes --");
	         return str1;
	     } finally {
	       try {
	         if (ps != null)
	           ps.close();
	         if (con != null)
	           con.close();
	       } catch (Exception exc) {
	         throw new RuntimeException(exc);
	       }
	     }
	     System.out.println("-- FIN ReporteSentencias.insertaClientes --");
	     return msg;
	   }
 }