package org.openxava.distribucion.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.EmpresaFilter;
import org.openxava.base.model.Empresa;
import org.openxava.ventas.model.Producto;

@Entity

@Table(name="VIEW_FACTURACIONZONAREPARTO")

@Tab(properties="fecha, zona.nombre, empresa.codigo, producto.codigo, producto.nombre, cantidad, subtotal1", 
	filter=EmpresaFilter.class, 
	baseCondition=EmpresaFilter.BASECONDITION,
	defaultOrder="${fecha} desc")

public class FacturacionZonaReparto {
	
	// el id cliente + id producto + periodo (yyyymmdd) + nro empresa (dos digitos)
	@Id
	@Hidden @ReadOnly
	@Column(length=74)
	private String id;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly @NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private Empresa empresa;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly @NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private ZonaReparto zona;
	
	@ReadOnly 
	private Date fecha;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly @NoCreate @NoModify
	@ReferenceView("Simple")
	private Producto producto;
	
	@ReadOnly 
	private BigDecimal cantidad;
	
	@ReadOnly 
	private BigDecimal subtotal1;

	@ReadOnly 
	private BigDecimal subtotal2;
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public ZonaReparto getZona() {
		return zona;
	}

	public void setZona(ZonaReparto zona) {
		this.zona = zona;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getSubtotal1() {
		return subtotal1;
	}

	public void setSubtotal1(BigDecimal subtotal1) {
		this.subtotal1 = subtotal1;
	}

	public BigDecimal getSubtotal2() {
		return subtotal2;
	}

	public void setSubtotal2(BigDecimal subtotal2) {
		this.subtotal2 = subtotal2;
	}
}
