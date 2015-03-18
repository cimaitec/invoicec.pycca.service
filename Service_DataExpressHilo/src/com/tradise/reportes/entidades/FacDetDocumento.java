package com.tradise.reportes.entidades;

public class FacDetDocumento
{
	private Integer cantidad;
	private String codAuxiliar;
	private String codPrincipal;
	private String descripcion;
	private double descuento;
	private double precioTotalSinImpuesto;
	private double precioUnitario;
	private double valorIce;
	private String ruc;
	private String codEstablecimiento;
	private String codPuntEmision;
	private String secuencial;
	private String codigoDocumento;
	private Integer secuencialDetalle;
	private String identificacion;	// HFU
	private int ambiente;

	public String getIdentificacion() {
		return identificacion;
	}
	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}
	
	public String getRuc()
	{
		return this.ruc;
	}
	public void setRuc(String ruc) {
		  this.ruc = ruc;
	}
	public String getCodEstablecimiento() {
	  return this.codEstablecimiento;
	}
	public void setCodEstablecimiento(String codEstablecimiento) {
	  this.codEstablecimiento = codEstablecimiento;
	}
	public String getCodPuntEmision() {
	  return this.codPuntEmision;
	}
	public void setCodPuntEmision(String codPuntEmision) {
	  this.codPuntEmision = codPuntEmision;
	}
	public String getSecuencial() {
	  return this.secuencial;
	}
	public void setSecuencial(String secuencial) {
	  this.secuencial = secuencial;
	}
	public String getCodigoDocumento() {
	  return this.codigoDocumento;
	}
	public void setCodigoDocumento(String codigoDocumento) {
	  this.codigoDocumento = codigoDocumento;
	}
	public Integer getSecuencialDetalle() {
	  return this.secuencialDetalle;
	}
	public void setSecuencialDetalle(Integer secuencialDetalle) {
	  this.secuencialDetalle = secuencialDetalle;
	}
	public Integer getCantidad() {
	  return this.cantidad;
	}
	public void setCantidad(Integer cantidad) {
	  this.cantidad = cantidad;
	}
	public String getCodAuxiliar() {
	  return this.codAuxiliar;
	}
	public void setCodAuxiliar(String codAuxiliar) {
	  this.codAuxiliar = codAuxiliar;
	}
	public String getCodPrincipal() {
	  return this.codPrincipal;
	}
	public void setCodPrincipal(String codPrincipal) {
	  this.codPrincipal = codPrincipal;
	}
	public String getDescripcion() {
	  return this.descripcion;
	}
	public void setDescripcion(String descripcion) {
	  this.descripcion = descripcion;
	}
	public double getDescuento() {
	  return this.descuento;
	}

	public void setDescuento(double descuento) {
	  this.descuento = descuento;
	}

	public double getPrecioTotalSinImpuesto() {
	  return this.precioTotalSinImpuesto;
	}

	public void setPrecioTotalSinImpuesto(double precioTotalSinImpuesto) {
	  this.precioTotalSinImpuesto = precioTotalSinImpuesto;
	}

	public double getPrecioUnitario() {
	   return this.precioUnitario;
	}

	public void setPrecioUnitario(double precioUnitario) {
	  this.precioUnitario = precioUnitario;
	}

	public double getValorIce() {
	  return this.valorIce;
	}

	public void setValorIce(double valorIce) {
	  this.valorIce = valorIce;
	}
public int getAmbiente() {
	return ambiente;
}
public void setAmbiente(int ambiente) {
	this.ambiente = ambiente;
}
}

/* Location:           C:\resources\reportes\printReportFacturacion.jar
 * Qualified Name:     cimait.entidades.FacDetDocumento
 * JD-Core Version:    0.6.2
 */