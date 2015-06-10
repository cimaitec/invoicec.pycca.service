 package com.tradise.reportes.servicios;
 
 import com.sun.DAO.Destinatarios;
import com.tradise.reportes.entidades.DetalleGuiaRemision;
import com.tradise.reportes.entidades.FacCabDocumento;
import com.tradise.reportes.entidades.FacDetAdicional;
import com.tradise.reportes.entidades.FacDetDocumento;
import com.tradise.reportes.entidades.FacDetMotivosdebito;
import com.tradise.reportes.entidades.FacDetRetencione;
import com.tradise.reportes.entidades.FacEmpresa;
import com.tradise.reportes.entidades.FacGeneral;
import com.tradise.reportes.entidades.FacProducto;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
 

public class ReporteServicio
{
	public FacCabDocumento buscarDatosCabDocumentos(String Ruc, String codEst, String codPuntEm, String codDoc, String secuencial)
	{
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		FacCabDocumento cabDocumento = null;
		String sql = "SELECT * FROM fac_cab_documentos WHERE \"Ruc\" = ? AND \"CodigoDocumento\" = ? AND \"CodEstablecimiento\" = ?  AND \"CodPuntEmision\" = ? AND secuencial = ? ";
		try {
			con = Conexion.conectar();
			ps = con.prepareStatement(sql);
			ps.setString(1, Ruc);
			ps.setString(2, codDoc);
			ps.setString(3, codEst);
			ps.setString(4, codPuntEm);
			ps.setString(5, secuencial);
			rs = ps.executeQuery();
			while (rs.next())
			{
				cabDocumento = new FacCabDocumento();
				cabDocumento.setCodEstablecimiento(rs.getString("codEstablecimiento"));
				cabDocumento.setCodigoDocumento(rs.getString("codigoDocumento"));
				cabDocumento.setCodPuntEmision(rs.getString("codPuntEmision"));
		        cabDocumento.setRuc(rs.getString("ruc"));
		        cabDocumento.setSecuencial(rs.getString("secuencial"));
		        cabDocumento.setAmbiente(Integer.valueOf(rs.getInt("ambiente")));
		        cabDocumento.setAutorizacion(rs.getString("autorizacion"));
		        cabDocumento.setClaveAcceso(rs.getString("claveAcceso"));
		        cabDocumento.setCodDocModificado(rs.getString("codDocModificado"));
		        cabDocumento.setCodDocSustento(rs.getString("codDocSustento"));
		        cabDocumento.setCodEstablecimientoDest(rs.getString("codEstablecimientoDest"));
		        cabDocumento.setDocAduaneroUnico(rs.getString("docAduaneroUnico"));
		        cabDocumento.setEmail(rs.getString("email"));
		        cabDocumento.setFecEmisionDocSustento(rs.getString("fecEmisionDocSustento"));
		        cabDocumento.setFechaautorizacion(rs.getString("fechaautorizacion"));
		        cabDocumento.setFechaEmision(rs.getString("fechaEmision"));
		        cabDocumento.setFecEmisionDocSustento(rs.getString("fechaEmisionDocSustento"));
		        cabDocumento.setFechaFinTransporte(rs.getString("fechaFinTransporte"));
		        cabDocumento.setFechaInicioTransporte(rs.getString("fechaInicioTransporte"));
		        cabDocumento.setGuiaRemision(rs.getString("guiaRemision"));
		        cabDocumento.setIdentificacionComprador(rs.getString("identificacionComprador"));
		        cabDocumento.setIdentificacionDestinatario(rs.getString("identificacionDestinatario"));
		        cabDocumento.setImporteTotal(rs.getDouble("importeTotal"));
		        cabDocumento.setInfoAdicional(rs.getString("infoAdicional"));
		        cabDocumento.setIva12(rs.getDouble("iva12"));
		        cabDocumento.setMoneda(rs.getString("moneda"));
		        cabDocumento.setMotivoRazon(rs.getString("motivoRazon"));
		        cabDocumento.setMotivoTraslado(rs.getString("motivoTraslado"));
		        cabDocumento.setMotivoValor(rs.getDouble("motivoValor"));
		        cabDocumento.setNumAutDocSustento(rs.getString("numAutDocSustento"));
		        cabDocumento.setNumDocModificado(rs.getString("numDocModificado"));
		        cabDocumento.setNumDocSustento(rs.getString("numDocSustento"));
		        cabDocumento.setPartida(rs.getString("partida"));
		        cabDocumento.setPropina(rs.getDouble("propina"));
		        cabDocumento.setPeriodoFiscal(rs.getString("periodoFiscal"));
		        cabDocumento.setPlaca(rs.getString("placa"));
		        cabDocumento.setRazonSocialComprador(rs.getString("razonSocialComprador"));
		        cabDocumento.setRazonSocialDestinatario(rs.getString("razonSocialDestinatario"));
		        cabDocumento.setRise(rs.getString("rise"));
		        cabDocumento.setRuta(rs.getString("ruta"));
		        cabDocumento.setSubtotal0(rs.getDouble("subtotal0"));
		        cabDocumento.setSubtotal12(rs.getDouble("subtotal12"));
		        cabDocumento.setSubtotalNoIva(rs.getDouble("subtotalNoIva"));
		        cabDocumento.setTipIdentificacionComprador(rs.getString("tipIdentificacionComprador"));
		        cabDocumento.setTipoEmision(rs.getString("tipoEmision"));
		        cabDocumento.setTipoIdentificacion((rs.getString("tipoIdentificacion")));
		        cabDocumento.setTotalDescuento(rs.getDouble("totalDescuento"));
		        cabDocumento.setTotalSinImpuesto(rs.getDouble("totalSinImpuesto"));
		        cabDocumento.setTotalvalorICE(rs.getDouble("totalvalorICE"));
		        cabDocumento.setESTADO_TRANSACCION(rs.getString("ESTADO_TRANSACCION"));
		        cabDocumento.setMSJ_ERROR(rs.getString("mSJ_ERROR"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}
				if (rs != null)
					rs.close();
			}
			catch (Exception exc) {
				throw new RuntimeException(exc);
			}
		}
		return cabDocumento;
	}
	
	public List<FacDetDocumento> buscarDatosDetallesDocumentos(com.sun.businessLogic.validate.Emisor emite)
	{
		List detDocumentos = null;
		FacDetDocumento detDoc = null;
     	ArrayList ListDetalleDocumento = new ArrayList();
     	ListDetalleDocumento =  emite.getInfEmisor().getListDetDocumentos();
       /*try
           {
           
            for (int i = 0; i < ListDetalleDocumento.size(); i++) {          
           detDoc = new FacDetDocumento();
           detDoc = (FacDetDocumento)ListDetalleDocumento.get(i);          
            detDoc.setCodEstablecimiento(detDoc.getCodEstablecimiento());          
            detDoc.setCodigoDocumento(detDoc.getCodigoDocumento());
            detDoc.setCodPuntEmision(detDoc.getCodPuntEmision());
            detDoc.setRuc(detDoc.getRuc());
            detDoc.setSecuencial(detDoc.getSecuencial());
            detDoc.setSecuencialDetalle(detDoc.getSecuencialDetalle());
            detDoc.setCantidad(Integer.valueOf(detDoc.getCantidad()));
            detDoc.setCodAuxiliar(detDoc.getCodAuxiliar());
            detDoc.setCodPrincipal(detDoc.getCodPrincipal());
            detDoc.setDescripcion(detDoc.getDescripcion());
            detDoc.setDescuento(detDoc.getDescuento());
            detDoc.setPrecioTotalSinImpuesto(detDoc.getPrecioTotalSinImpuesto());
            detDoc.setPrecioUnitario(detDoc.getPrecioUnitario());
            detDoc.setValorIce(detDoc.getValorIce());
           } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        */
     	return ListDetalleDocumento;
	}

	public List<FacDetDocumento> buscarDatosDetallesDocumentos(String Ruc, String codEst, String codPuntEm, String codDoc, String secuencial)
    {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List detDocumentos = null;
		FacDetDocumento detDoc = null;
		
		String sql = "SELECT * FROM fac_det_documentos WHERE \"Ruc\" = ? AND \"CodEstablecimiento\" = ? AND \"CodPuntEmision\" = ? AND secuencial = ? AND \"CodigoDocumento\" = ?";
		try
		{
			con = Conexion.conectar();
			ps = con.prepareStatement(sql);
			ps.setString(1, Ruc);
			ps.setString(2, codEst);
			ps.setString(3, codPuntEm);
			ps.setString(4, secuencial);
			ps.setString(5, codDoc);
			rs = ps.executeQuery();
			detDocumentos = new ArrayList();
			int i = detDocumentos.size();
			while (rs.next()) {
				detDoc = new FacDetDocumento();
				detDoc.setCodEstablecimiento(rs.getString("codEstablecimiento"));
		        detDoc.setCodigoDocumento(rs.getString("codigoDocumento"));
		        detDoc.setCodPuntEmision(rs.getString("codPuntEmision"));
		        detDoc.setRuc(rs.getString("codPuntEmision"));
		        detDoc.setSecuencial(rs.getString("secuencial"));
		        detDoc.setSecuencialDetalle(Integer.valueOf(rs.getInt("secuencialDetalle")));
		        detDoc.setCantidad(Integer.valueOf(rs.getInt("cantidad")));
		        detDoc.setCodAuxiliar(rs.getString("codAuxiliar").trim());
		        detDoc.setCodPrincipal(rs.getString("codPrincipal").trim());
		        detDoc.setDescripcion(rs.getString("descripcion"));
		        detDoc.setDescuento(rs.getDouble("descuento"));
		        detDoc.setPrecioTotalSinImpuesto(rs.getDouble("precioTotalSinImpuesto"));
		        detDoc.setPrecioUnitario(rs.getDouble("precioUnitario"));
		        detDoc.setValorIce(rs.getDouble("valorIce"));
		        detDocumentos.add(i, detDoc);
		        i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}
				if (rs != null)
					rs.close();
			}
			catch (Exception exc) {
				throw new RuntimeException(exc);
			}
		}
		return detDocumentos;
    }
	
	
	public List<Destinatarios> buscarDatosDestinatarios(int ambiente, String Ruc, String codEst, String codPuntEm, String codDoc, String secuencial)
    {
		System.out.println("-- INCICIO ReporteServicio.buscarDatosDestinatarios --");
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Destinatarios> destinatarios = null;
		Destinatarios destinatario = null;
		
		String sql = "SELECT * FROM fac_destinatario where ambiente=? and \"Ruc\" = ? AND \"codEstablecimiento\" = ? AND \"CodPuntEmision\" = ? AND secuencial = ? ";
		try
		{
			con = Conexion.conectar();
			ps = con.prepareStatement(sql);
			ps.setInt(1, ambiente);
			ps.setString(2, Ruc);
			ps.setString(3, codEst);
			ps.setString(4, codPuntEm);
			ps.setString(5, secuencial);
			//ps.setString(5, codDoc);
			rs = ps.executeQuery();
			destinatarios = new ArrayList();
			int i = 0;//destinatarios.size();
			while (rs.next()) {
				destinatario = new Destinatarios();
				destinatario.setRuc(rs.getString("Ruc"));
				destinatario.setCodEstablecimiento(rs.getString("codEstablecimiento"));
				destinatario.setCodPuntEmision(rs.getString("CodPuntEmision"));
				//destinatario.setCodigoDocumento(rs.getString("codigoDocumento"));
				
				destinatario.setSecuencial(rs.getString("secuencial"));
				
				destinatario.setIdentificacionDestinatario(rs.getString("identificacion"));
				destinatario.setRazonSocialDestinatario(rs.getString("razonSocial"));
				destinatario.setDireccionDestinatario(rs.getString("direccion"));
				destinatario.setMotivoTraslado(rs.getString("motivoTraslado"));
				destinatario.setDocAduanero(rs.getString("documentoAduanero"));
				//destinatario.setCodEstabDestino(rs.getString("codEstabDestino"));
				destinatario.setRutaDest(rs.getString("ruta"));
				destinatario.setCodDocSustentoDest(rs.getString("codDocSustento"));
				destinatario.setNumDocSustentoDest(rs.getString("numDocSustento"));
				destinatario.setNumAutDocSustDest(rs.getString("numAutorizacionDocSustento"));
				destinatario.setFechEmisionDocSustDest(rs.getDate("fechaEmisionDocSustento")==null?null:rs.getDate("fechaEmisionDocSustento").toString());
				
				System.out.println("i: "+i);
				destinatario.setListDetallesGuiaRemision(buscarDetallesDestinatarios(destinatario));
				
		        
		        /*detDoc.setSecuencialDetalle(Integer.valueOf(rs.getInt("secuencialDetalle")));
		        detDoc.setCantidad(Integer.valueOf(rs.getInt("cantidad")));
		        detDoc.setCodAuxiliar(rs.getString("codAuxiliar").trim());
		        detDoc.setCodPrincipal(rs.getString("codPrincipal").trim());
		        detDoc.setDescripcion(rs.getString("descripcion"));
		        detDoc.setDescuento(rs.getDouble("descuento"));
		        detDoc.setPrecioTotalSinImpuesto(rs.getDouble("precioTotalSinImpuesto"));
		        detDoc.setPrecioUnitario(rs.getDouble("precioUnitario"));
		        detDoc.setValorIce(rs.getDouble("valorIce"));*/
		        destinatarios.add(i, destinatario);
		        i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}
				if (rs != null)
					rs.close();
			}
			catch (Exception exc) {
				throw new RuntimeException(exc);
			}
		}
		System.out.println("-- FIN ReporteServicio.buscarDatosDestinatarios --");
		return destinatarios;
    }
	
	
	public List<DetalleGuiaRemision> buscarDetallesDestinatarios(Destinatarios destinatario)
    {
		System.out.println("-- INCICIO ReporteServicio.buscarDetallesDestinatarios --");
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<DetalleGuiaRemision> detalles = null;
		DetalleGuiaRemision detalle = null;
		
		String sql = "SELECT * FROM fac_det_guia_remision "
					 + "where \"Ruc\" = ? AND \"codEstablecimiento\" = ? "
					   + "AND \"codPuntEmision\" = ? AND secuencial = ?  and \"identificacionDestinatario\" = ? ";
		try
		{
			con = Conexion.conectar();
			ps = con.prepareStatement(sql);
			ps.setString(1, destinatario.getRuc());
			ps.setString(2, destinatario.getCodEstablecimiento());
			ps.setString(3, destinatario.getCodPuntEmision());
			ps.setString(4, destinatario.getSecuencial());
			ps.setString(5, destinatario.getIdentificacionDestinatario());
			rs = ps.executeQuery();
			detalles = new ArrayList();
			int i = detalles.size();
			while (rs.next()) {
				detalle = new DetalleGuiaRemision();
				detalle.setRuc(rs.getString("Ruc"));
				detalle.setCodEstablecimiento(rs.getString("codEstablecimiento"));
				detalle.setCodPuntEmision(rs.getString("codPuntEmision"));
				detalle.setSecuencial(rs.getString("secuencial"));
				detalle.setIdentificacionDestinatario(rs.getString("identificacionDestinatario"));
				detalle.setCodigoInterno(rs.getString("codigoInterno"));
				detalle.setCodigoAdicional(rs.getString("codigoAdicional"));
				detalle.setDescripcion(rs.getString("descripcion"));
				detalle.setCantidad(Integer.parseInt(rs.getString("cantidad")));
				detalle.setDetallesAdicionales(rs.getString("detallesAdicionales"));
		       
				detalles.add(i, detalle);
		        i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}
				if (rs != null)
					rs.close();
			}
			catch (Exception exc) {
				throw new RuntimeException(exc);
			}
		}
		System.out.println("-- FIN ReporteServicio.buscarDetallesDestinatarios --");
		return detalles;
    }
 
	public FacEmpresa buscarEmpresa(String Ruc) {
     Connection con = null;
     PreparedStatement ps = null;
     ResultSet rs = null;
     FacEmpresa empresa = null;
     String sql = "SELECT * FROM fac_empresa WHERE \"Ruc\" = ? ";
     try {
       con = Conexion.conectar();
       ps = con.prepareStatement(sql);
       ps.setString(1, Ruc);
       rs = ps.executeQuery();
       while (rs.next()) {
         empresa = new FacEmpresa();
         empresa.setColorEmpresa(rs.getString("colorEmpresa"));
         empresa.setContribEspecial(Integer.valueOf(rs.getInt("contribEspecial")));
         empresa.setDireccionMatriz(rs.getString("direccionMatriz"));
         empresa.setEmailEnvio(rs.getString("emailEnvio"));
         empresa.setIsActive(rs.getString("isActive"));
         empresa.setObligContabilidad(rs.getString("obligContabilidad"));
         empresa.setPassSMTP(rs.getString("passSMTP"));
         empresa.setPathCompAutorizados(rs.getString("pathCompAutorizados"));
         empresa.setPathCompFirmados(rs.getString("pathCompFirmados"));
         empresa.setPathCompGenerados(rs.getString("pathCompGenerados"));
         empresa.setPathCompNoAutorizados(rs.getString("pathCompNoAutorizados"));
         empresa.setPathFirma(rs.getString("pathFirma"));
         empresa.setPathInfoRecibida(rs.getString("pathInfoRecibida"));
         empresa.setPathLogoEmpresa(rs.getString("pathLogoEmpresa"));
         empresa.setPuertoSMTP(Integer.valueOf(rs.getInt("puertoSMTP")));
         empresa.setRazonComercial(rs.getString("razonComercial"));
         empresa.setRazonSocial(rs.getString("razonSocial"));
         empresa.setRuc(rs.getString("ruc"));
         empresa.setServidorSMTP(rs.getString("servidorSMTP"));
         empresa.setSslSMTP(Boolean.valueOf(rs.getBoolean("sslSMTP")));
         empresa.setUrlWebServices(rs.getString("urlWebServices"));
         empresa.setUserSMTP(rs.getString("userSMTP"));
         empresa.setMarcaAgua(rs.getString("marcaAgua"));
         empresa.setPathMarcaAgua(rs.getString("pathMarcaAgua"));
       }
     } catch (Exception e) {
       e.printStackTrace();
       try
       {
         if (ps != null) {
           ps.close();
         }
         if (con != null) {
           con.close();
         }
         if (rs != null)
           rs.close();
       }
       catch (Exception exc) {
         throw new RuntimeException(exc);
       }
     }
     finally
     {
       try
       {
         if (ps != null) {
           ps.close();
         }
         if (con != null) {
           con.close();
         }
         if (rs != null)
           rs.close();
       }
       catch (Exception exc) {
         throw new RuntimeException(exc);
       }
     }
     return empresa;
   }
 
	public List<FacDetAdicional> buscarDetAdicional(String Ruc, String codEst, String codPuntEm, String codDoc, String secuencial) {
     List detAdicional = null;
     try {
       Connection con = null;
       PreparedStatement ps = null;
       ResultSet rs = null;
       FacDetAdicional detAdic = null;
       String sql = "SELECT * FROM fac_det_adicional WHERE \"Ruc\" = ? AND \"CodEstablecimiento\" = ? AND \"CodPuntEmision\" = ? AND \"CodigoDocumento\" = ? AND secuencial = ?";
       con = Conexion.conectar();
       ps = con.prepareStatement(sql);
       ps.setString(1, Ruc);
       ps.setString(2, codEst);
       ps.setString(3, codPuntEm);
       ps.setString(4, codDoc);
       ps.setString(5, secuencial);
       rs = ps.executeQuery();
       detAdicional = new ArrayList();
       int i = detAdicional.size();
       while (rs.next()) {
         detAdic = new FacDetAdicional();
         detAdic.setCodEstablecimiento(rs.getString("codEstablecimiento"));
         detAdic.setCodigoDocumento(rs.getString("codigoDocumento"));
         detAdic.setCodPuntEmision(rs.getString("codPuntEmision"));
         detAdic.setRuc(rs.getString("codPuntEmision"));
         detAdic.setSecuencial(rs.getString("secuencial"));
         detAdic.setSecuencialDetAdicional(Integer.valueOf(rs.getInt("secuencialDetAdicional")));
         detAdic.setNombre(rs.getString("nombre").trim());
         detAdic.setValor(rs.getString("valor").trim());
         detAdicional.add(i, detAdic);
         i++;
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return detAdicional;
   }
 
     /*
     public List<FacDetAdicional> buscarDetAdicional(com.cimait.businessLogic.validate.Emisor emite) {
               ArrayList detAdicional = new ArrayList();
            try {
              detAdicional = new ArrayList();
              int i = detAdicional.size();
       
                FacDetAdicional detAdic = new FacDetAdicional();
                 detAdic = emite.getInfEmisor().getListInfAdicional();
                detAdic.setCodEstablecimiento(rs.getString("codEstablecimiento"));
                detAdic.setCodigoDocumento(rs.getString("codigoDocumento"));
                detAdic.setCodPuntEmision(rs.getString("codPuntEmision"));
                detAdic.setRuc(rs.getString("codPuntEmision"));
                detAdic.setSecuencial(rs.getString("secuencial"));
                detAdic.setSecuencialDetAdicional(Integer.valueOf(rs.getInt("secuencialDetAdicional")));
                detAdic.setNombre(rs.getString("nombre").trim());
                detAdic.setValor(rs.getString("valor").trim());
                detAdicional.add(i, detAdic);
                i++;
            } catch (Exception e) {
              e.printStackTrace();
            }
            return detAdicional;
          }*/
	public FacProducto buscarProductos(int codProducto) {
     FacProducto producto = null;
     try {
       Connection con = null;
       PreparedStatement ps = null;
       ResultSet rs = null;
       String sql = "SELECT * FROM fac_productos WHERE \"codPrincipal\" = ?";
       con = Conexion.conectar();
       ps = con.prepareStatement(sql);
       ps.setInt(1, codProducto);
       rs = ps.executeQuery();
       while (rs.next()) {
         producto = new FacProducto();
         producto.setAtributo1(rs.getString("atributo1"));
         producto.setAtributo2(rs.getString("atributo2"));
         producto.setAtributo3(rs.getString("atributo3"));
         producto.setCodAuxiliar(Integer.valueOf(rs.getInt("codAuxiliar")));
         producto.setCodIce(Integer.valueOf(rs.getInt("codIce")));
         producto.setCodPrincipal(Integer.valueOf(rs.getInt("codPrincipal")));
         producto.setDescripcion(rs.getString("descripcion"));
         producto.setTipoIva(Integer.valueOf(rs.getInt("tipoIva")));
         producto.setTipoProducto(rs.getString("tipoProducto"));
         producto.setValor1(rs.getString("valor1"));
         producto.setValor2(rs.getString("valor2"));
         producto.setValor3(rs.getString("valor3"));
         producto.setValorUnitario(rs.getDouble("valorUnitario"));
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return producto;
   }
 
   public List<FacDetRetencione> buscarDatosCompRetencion(String Ruc, String codEst, String codPuntEm, String codDoc, String secuencial) {
     List retencion = null;
     try {
       Connection con = null;
       PreparedStatement ps = null;
       ResultSet rs = null;
 
       String sql = "select * from fac_det_retenciones  where \"Ruc\" = ? AND \"CodEstablecimiento\" = ? AND \"CodPuntEmision\" = ? AND \"CodigoDocumento\" = ? AND secuencial = ?";
       con = Conexion.conectar();
       ps = con.prepareStatement(sql);
       ps.setString(1, Ruc);
       ps.setString(2, codEst);
       ps.setString(3, codPuntEm);
       ps.setString(4, codDoc);
       ps.setString(5, secuencial);
       rs = ps.executeQuery();
       retencion = new ArrayList();
       int i = retencion.size();
       while (rs.next()) {
         FacDetRetencione ret = new FacDetRetencione();
         ret.setCodEstablecimiento(rs.getString("codEstablecimiento"));
         ret.setCodigoDocumento("codigoDocumento");
         ret.setCodImpuesto(Integer.valueOf(rs.getInt("codImpuesto")));
         ret.setCodPuntEmision(rs.getString("codPuntEmision"));
         ret.setRuc(rs.getString("ruc"));
         ret.setSecuencial(rs.getString("secuencial"));
         ret.setSecuencialRetencion(Integer.valueOf(rs.getInt("secuencialRetencion")));
         ret.setBaseImponible(rs.getDouble("baseImponible"));
         ret.setCodPorcentaje(Integer.valueOf(rs.getInt("codPorcentaje")));
         ret.setPorcentajeRetencion(rs.getDouble("porcentajeRetencion"));
         ret.setTarifa(rs.getDouble("tarifa"));
         ret.setValor(rs.getDouble("valor"));
         retencion.add(i, ret);
         i++;
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return retencion;
   }
 
   public FacGeneral buscarNombreCodigo(String codUnico, String codTabla) {
     FacGeneral general = null;
     try {
       Connection con = null;
       PreparedStatement ps = null;
       ResultSet rs = null;
       String sql = "select * from fac_general where \"codTabla\" = ? And \"codUnico\" = ? ";
       con = Conexion.conectar();
       ps = con.prepareStatement(sql);
       ps.setString(1, codTabla);
       ps.setString(2, codUnico);
       rs = ps.executeQuery();
       while (rs.next()) {
         general = new FacGeneral();
         general.setCodTabla(rs.getString("codTabla"));
         general.setCodUnico(rs.getString("codUnico"));
         general.setDescripcion(rs.getString("descripcion"));
         general.setIdGeneral(Integer.valueOf(rs.getInt("idGeneral")));
         general.setPorcentaje(Integer.valueOf(rs.getInt("porcentaje")));
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return general;
   }
 
   public List<FacDetMotivosdebito> buscarMotivosDebito(String Ruc, String codEst, String codPuntEm, String codDoc, String secuencial) {
     List debito = new ArrayList();
     try {
       Connection con = null;
       PreparedStatement ps = null;
       ResultSet rs = null;
       String sql = "select * from fac_det_motivosdebito where \"Ruc\" = ? AND \"CodEstablecimiento\" = ? AND \"CodPuntEmision\" = ? AND \"CodigoDocumento\" = ? AND secuencial = ?";
       con = Conexion.conectar();
       ps = con.prepareStatement(sql);
       ps.setString(1, Ruc);
       ps.setString(2, codEst);
       ps.setString(3, codPuntEm);
       ps.setString(4, codDoc);
       ps.setString(5, secuencial);
       rs = ps.executeQuery();
       int i = debito.size();
       while (rs.next()) {
         FacDetMotivosdebito motDet = new FacDetMotivosdebito();
         motDet.setCodEstablecimiento(rs.getString("codEstablecimiento"));
         motDet.setCodigoDocumento(rs.getString("codigoDocumento"));
         motDet.setCodPuntEmision(rs.getString("codPuntEmision"));
         motDet.setRuc(rs.getString("ruc"));
         motDet.setSecuencial(rs.getString("secuencial"));
         motDet.setSecuencialDetalle(Integer.valueOf(rs.getInt("secuencialDetalle")));
         motDet.setBaseImponible(rs.getDouble("baseImponible"));
         motDet.setCodImpuesto(Integer.valueOf(rs.getInt("codImpuesto")));
         motDet.setCodPorcentaje(Integer.valueOf(rs.getInt("codPorcentaje")));
 
         motDet.setRazon(rs.getString("razon"));
         motDet.setTarifa(Integer.valueOf(rs.getInt("tarifa")));
         motDet.setTipoImpuestos(rs.getString("tipoImpuestos"));
         motDet.setValor(rs.getDouble("valor"));
         debito.add(i, motDet);
         i++;
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return debito;
   }
   
   

 }

