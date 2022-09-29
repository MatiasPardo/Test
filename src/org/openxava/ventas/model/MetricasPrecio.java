package org.openxava.ventas.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.View;

@Entity

@Table(name="VIEW_METRICASPRECIO")

@View(members="importeMasIva")

public class MetricasPrecio {
	
	@Hidden
	@Column(length=32)
	@Id
	private String id;
	
	@OneToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="precio_id", referencedColumnName="id")
	@ReadOnly
	private Precio precio;
	
	@ReadOnly
	private BigDecimal importeMasIva;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Precio getPrecio() {
		return precio;
	}

	public void setPrecio(Precio precio) {
		this.precio = precio;
	}

	public BigDecimal getImporteMasIva() {
		return importeMasIva;
	}

	public void setImporteMasIva(BigDecimal importeMasIva) {
		this.importeMasIva = importeMasIva;
	}
}
