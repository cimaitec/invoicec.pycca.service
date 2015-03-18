package com.sun.DAO;

import java.util.Date;
import java.util.List;
import com.tradise.reportes.entidades.DetalleGuiaRemision;
import com.tradise.reportes.entidades.FacDetDocumento;

public class Destinatarios
{

	//DEST //Cabecera
	private int	ambiente;
	private String ruc;
	private String codEstablecimiento;
	private String codPuntEmision;
	private String secuencial;
	private String codigoDocumento;
	
	private int codigo;
	private String identificacionDestinatario;
	private String razonSocialDestinatario;
	private String direccionDestinatario;
	private String motivoTraslado;
	private String docAduanero;
	private String codEstabDestino;
	private String rutaDest;
	private String codDocSustentoDest;
	private String numDocSustentoDest;
	private String numAutDocSustDest;
	private String fechEmisionDocSustDest;
	private int sizeDEST = 11;
	//private List<DetalleDocumento> listaDetalles;
	private List<FacDetDocumento> listaDetallesDocumentos;
	private List<DetalleGuiaRemision> listDetallesGuiaRemision;

	// INI HFU
	public int getAmbiente() {
		return ambiente;
	}
	public void setAmbiente(int ambiente) {
		this.ambiente = ambiente;
	}
	
	public List<DetalleGuiaRemision> getListDetallesGuiaRemision() {
		return listDetallesGuiaRemision;
	}
	public void setListDetallesGuiaRemision(
			List<DetalleGuiaRemision> listDetallesGuiaRemision) {
		this.listDetallesGuiaRemision = listDetallesGuiaRemision;
	}
	
	public String getRuc() {
		return ruc;
	}
	public void setRuc(String ruc) {
		this.ruc = ruc;
	}
	public String getCodEstablecimiento() {
		return codEstablecimiento;
	}
	public void setCodEstablecimiento(String codEstablecimiento) {
		this.codEstablecimiento = codEstablecimiento;
	}
	
	public String getCodPuntEmision() {
		return codPuntEmision;
	}
	public void setCodPuntEmision(String codPuntEmision) {
		this.codPuntEmision = codPuntEmision;
	}
	public String getSecuencial() {
		return secuencial;
	}
	public void setSecuencial(String secuencial) {
		this.secuencial = secuencial;
	}
	public String getCodigoDocumento() {
		return codigoDocumento;
	}
	public void setCodigoDocumento(String codigoDocumento) {
		this.codigoDocumento = codigoDocumento;
	}
	
	/*public List<FacDetDocumento> getListaDetallesDocumentos() {
		return listaDetallesDocumentos;
	}
	public void setListaDetallesDocumentos(
			List<FacDetDocumento> listaDetallesDocumentos) {
		this.listaDetallesDocumentos = listaDetallesDocumentos;
	}*/
	// FIN HFU
	
	
	/*public List<DetalleDocumento> getListaDetalles() {
		return listaDetalles;
	}
	public void setListaDetalles(List<DetalleDocumento> listaDetalles) {
		this.listaDetalles = listaDetalles;
	}*/
	
	
	public String getIdentificacionDestinatario() {
		return identificacionDestinatario;
	}
	public void setIdentificacionDestinatario(String identDestinatario) {
		this.identificacionDestinatario = identDestinatario;
	}
	public String getRazonSocialDestinatario() {
		return razonSocialDestinatario;
	}
	public void setRazonSocialDestinatario(String razonSocialDestinatario) {
		this.razonSocialDestinatario = razonSocialDestinatario;
	}
	public String getDireccionDestinatario() {
		return direccionDestinatario;
	}
	public void setDireccionDestinatario(String dirDestinatario) {
		this.direccionDestinatario = dirDestinatario;
	}
	public String getMotivoTraslado() {
		return motivoTraslado;
	}
	public void setMotivoTraslado(String motTraslDestinatario) {
		this.motivoTraslado = motTraslDestinatario;
	}
	public String getDocAduanero() {
		return docAduanero;
	}
	public void setDocAduanero(String docAduanero) {
		this.docAduanero = docAduanero;
	}
	public String getCodEstabDestino() {
		return codEstabDestino;
	}
	public void setCodEstabDestino(String codEstabDestino) {
		this.codEstabDestino = codEstabDestino;
	}
	public String getRutaDest() {
		return rutaDest;
	}
	public void setRutaDest(String rutaDest) {
		this.rutaDest = rutaDest;
	}
	public String getCodDocSustentoDest() {
		return codDocSustentoDest;
	}
	public void setCodDocSustentoDest(String codDocSustentoDest) {
		this.codDocSustentoDest = codDocSustentoDest;
	}
	public String getNumDocSustentoDest() {
		return numDocSustentoDest;
	}
	public void setNumDocSustentoDest(String numDocSustentoDest) {
		this.numDocSustentoDest = numDocSustentoDest;
	}
	public String getNumAutDocSustDest() {
		return numAutDocSustDest;
	}
	public void setNumAutDocSustDest(String numAutDocSustDest) {
		this.numAutDocSustDest = numAutDocSustDest;
	}
	public String getFechEmisionDocSustDest() {
		return fechEmisionDocSustDest;
	}
	public void setFechEmisionDocSustDest(String fechEmisionDocSustDest) {
		this.fechEmisionDocSustDest = fechEmisionDocSustDest;
	}
	public int getSizeDEST() {
		return sizeDEST;
	}
	public void setSizeDEST(int sizeDEST) {
		this.sizeDEST = sizeDEST;
	}
}
