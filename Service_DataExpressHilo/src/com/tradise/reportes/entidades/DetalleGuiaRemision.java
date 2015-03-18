package com.tradise.reportes.entidades;

public class DetalleGuiaRemision
{
	private int ambiente;
	private String ruc;
	private String codEstablecimiento;
	private String codPuntEmision;
	private String secuencial;
	private String identificacionDestinatario;
	private String codigoInterno;
	private String codigoAdicional;
	private String descripcion;
	private int cantidad;
	private String detallesAdicionales;
	
	public int getAmbiente() {
		return ambiente;
	}
	public void setAmbiente(int ambiente) {
		this.ambiente = ambiente;
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
	public String getIdentificacionDestinatario() {
		return identificacionDestinatario;
	}
	public void setIdentificacionDestinatario(String identificacionDestinatario) {
		this.identificacionDestinatario = identificacionDestinatario;
	}
	public String getCodigoInterno() {
		return codigoInterno;
	}
	public void setCodigoInterno(String codigoInterno) {
		this.codigoInterno = codigoInterno;
	}
	public String getCodigoAdicional() {
		return codigoAdicional;
	}
	public void setCodigoAdicional(String codigoAdicional) {
		this.codigoAdicional = codigoAdicional;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public int getCantidad() {
		return cantidad;
	}
	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
	public String getDetallesAdicionales() {
		return detallesAdicionales;
	}
	public void setDetallesAdicionales(String detallesAdicionales) {
		this.detallesAdicionales = detallesAdicionales;
	}

}
