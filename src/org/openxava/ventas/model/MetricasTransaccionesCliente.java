package org.openxava.ventas.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.View;

@Entity

@Table(name="VIEW_METRICASTRANSACCIONESCLIENTE")

@View(name="Cliente", 
		members="fechaUltimaVenta")

public class MetricasTransaccionesCliente {
	
	@Id @Hidden 
	@Column(name="id", length=32)
	private String id;
	
	@MapsId
	@OneToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="id")
	@ReadOnly
	@ReferenceView("Simple")
	private Cliente cliente;
	
	@ReadOnly
	private Date fechaUltimaVenta;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Date getFechaUltimaVenta() {
		return fechaUltimaVenta;
	}

	public void setFechaUltimaVenta(Date fechaUltimaVenta) {
		this.fechaUltimaVenta = fechaUltimaVenta;
	}
}
