package org.openxava.compras.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.Tab;
import org.openxava.base.model.Empresa;

@Entity

@Table(name="VIEW_IMPORTACIONFACTURACOMPRA")

@Tab(properties="fecha, fechaReal, numero, empresa.codigo, proveedor, fechaVencimiento, codigoImpuesto, " + 
		"importeImpuesto, alicuotaImpuesto, codigoProducto, cantidad, precioUnitario, tasaIva, centroCostos")

public class ImportacionFacturaCompra {
	
	@Id
	@Hidden
	@ReadOnly
	@Column(length=64)
	private String id;
	
	@ReadOnly
	private Date fecha;
	
	@ReadOnly
	private Date fechaReal;
	
	@ReadOnly
	@Column(length=30)
	private String numero;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo")
	private Empresa empresa;
	
	@ReadOnly
	@Column(length=25)
	private String proveedor;
	
	@ReadOnly
	private Date fechaVencimiento; 
	
	@ReadOnly
	@Column(length=25)
	private String codigoImpuesto;
	
	@ReadOnly
	private BigDecimal importeImpuesto;
	
	@ReadOnly
	private String alicuotaImpuesto;
	
	@ReadOnly
	@Column(length=25)
	private String codigoProducto;
	
	@ReadOnly
	private BigDecimal cantidad;
	
	@ReadOnly
	private BigDecimal precioUnitario;
	
	@ReadOnly
	private BigDecimal tasaIva;
	
	@ReadOnly
	private String centroCostos;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Date getFechaReal() {
		return fechaReal;
	}

	public void setFechaReal(Date fechaReal) {
		this.fechaReal = fechaReal;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public String getCodigoImpuesto() {
		return codigoImpuesto;
	}

	public void setCodigoImpuesto(String codigoImpuesto) {
		this.codigoImpuesto = codigoImpuesto;
	}

	public BigDecimal getImporteImpuesto() {
		return importeImpuesto;
	}

	public void setImporteImpuesto(BigDecimal importeImpuesto) {
		this.importeImpuesto = importeImpuesto;
	}

	public String getAlicuotaImpuesto() {
		return alicuotaImpuesto;
	}

	public void setAlicuotaImpuesto(String alicuotaImpuesto) {
		this.alicuotaImpuesto = alicuotaImpuesto;
	}

	public String getCodigoProducto() {
		return codigoProducto;
	}

	public void setCodigoProducto(String codigoProducto) {
		this.codigoProducto = codigoProducto;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(BigDecimal precioUnitario) {
		this.precioUnitario = precioUnitario;
	}

	public BigDecimal getTasaIva() {
		return tasaIva;
	}

	public void setTasaIva(BigDecimal tasaIva) {
		this.tasaIva = tasaIva;
	}

	public String getCentroCostos() {
		return centroCostos;
	}

	public void setCentroCostos(String centroCostos) {
		this.centroCostos = centroCostos;
	}
}
