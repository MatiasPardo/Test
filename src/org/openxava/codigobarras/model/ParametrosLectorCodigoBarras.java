package org.openxava.codigobarras.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReadOnly;

public class ParametrosLectorCodigoBarras {

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private CodigoBarrasProducto tipoCodigoBarras;
	
	private BigDecimal cantidad;
	
	@Column(length=100)
	private String codigoBarras;

	@ReadOnly
	private BigDecimal total;
	
	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public String getCodigoBarras() {
		return codigoBarras;
	}

	public void setCodigoBarras(String codigoBarras) {
		this.codigoBarras = codigoBarras;
	}

	public CodigoBarrasProducto getTipoCodigoBarras() {
		return tipoCodigoBarras;
	}

	public void setTipoCodigoBarras(CodigoBarrasProducto tipoCodigoBarras) {
		this.tipoCodigoBarras = tipoCodigoBarras;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}
}
