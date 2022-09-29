package org.openxava.model.importaciones;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Formula;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.inventario.model.DespachoImportacion;

@Entity

@Table(name="VIEW_ANALISISDESPACHOIMPORTACION")

@Tab(properties="despacho.codigo, recepciones, ventas, ventasMoneda2, consignaciones, pendientesFacturacion, otrosIngresos, otrosEgresos, stock, control, diferencia")

@View(members="despacho;" +
		"recepciones;" + 
		"ventas, ventasMoneda2, consignaciones;" + 
		"pendientesFacturacion;" + 
		"otrosIngresos, otrosEgresos;" + 
		"stock, control, diferencia")

public class AnalisisDespachoImportacion {
	
	@Hidden
	@Column(length=32)
	@Id
	private String id;
	
	@OneToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="id")
	@ReadOnly @NoCreate @NoModify
	@MapsId
	@ReferenceView("Simple")
	private DespachoImportacion despacho;
	
	@ReadOnly
	private BigDecimal recepciones;
	
	@ReadOnly
	private BigDecimal ventas;
	
	@ReadOnly
	private BigDecimal ventasMoneda2;
	
	@ReadOnly
	private BigDecimal consignaciones;
	
	@ReadOnly
	private BigDecimal pendientesFacturacion;
	
	@ReadOnly
	private BigDecimal otrosIngresos;
	
	@ReadOnly
	private BigDecimal otrosEgresos;
	
	@ReadOnly
	private BigDecimal stock;
	
	@ReadOnly
	private BigDecimal control;

	@ReadOnly
	@Formula("(stock - control)")
	private BigDecimal diferencia; 
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DespachoImportacion getDespacho() {
		return despacho;
	}

	public void setDespacho(DespachoImportacion despacho) {
		this.despacho = despacho;
	}

	public BigDecimal getRecepciones() {
		return recepciones;
	}

	public void setRecepciones(BigDecimal recepciones) {
		this.recepciones = recepciones;
	}

	public BigDecimal getVentas() {
		return ventas;
	}

	public void setVentas(BigDecimal ventas) {
		this.ventas = ventas;
	}

	public BigDecimal getVentasMoneda2() {
		return ventasMoneda2;
	}

	public void setVentasMoneda2(BigDecimal ventasMoneda2) {
		this.ventasMoneda2 = ventasMoneda2;
	}

	public BigDecimal getConsignaciones() {
		return consignaciones;
	}

	public void setConsignaciones(BigDecimal consignaciones) {
		this.consignaciones = consignaciones;
	}

	public BigDecimal getPendientesFacturacion() {
		return pendientesFacturacion;
	}

	public void setPendientesFacturacion(BigDecimal pendientesFacturacion) {
		this.pendientesFacturacion = pendientesFacturacion;
	}

	public BigDecimal getOtrosIngresos() {
		return otrosIngresos;
	}

	public void setOtrosIngresos(BigDecimal otrosIngresos) {
		this.otrosIngresos = otrosIngresos;
	}

	public BigDecimal getOtrosEgresos() {
		return otrosEgresos;
	}

	public void setOtrosEgresos(BigDecimal otrosEgresos) {
		this.otrosEgresos = otrosEgresos;
	}

	public BigDecimal getStock() {
		return stock;
	}

	public void setStock(BigDecimal stock) {
		this.stock = stock;
	}

	public BigDecimal getControl() {
		return control;
	}

	public void setControl(BigDecimal control) {
		this.control = control;
	}

	public BigDecimal getDiferencia() {
		return diferencia;
	}
}
