package org.openxava.compras.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.impuestos.model.*;
import org.openxava.negocio.model.*;

@Entity

@Table(name="VIEW_IMPUESTOCOMPRAELECTRONICA")

@Tab(
	filter=EmpresaFilter.class,
	properties="compra.fecha, compra.tipoOperacion, compra.numero, compra.proveedor.nombre, impuesto.codigo, impuesto.nombre, importeImpuesto, alicuota, total, subtotal, compra.moneda, provincia.provincia",
	baseCondition=EmpresaFilter.BASECONDITION,
	defaultOrder="${compra.fechaCreacion} desc")

public class ImpuestoCompraElectronica implements IGeneradoPor{
	
	@Id
	@Hidden
	@Column(length=64)
	private String id;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@ReadOnly
	private CompraElectronica compra;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@ReadOnly
	private Impuesto impuesto;
	
	@ReadOnly
	private BigDecimal importeImpuesto = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal alicuota = BigDecimal.ZERO;

	@ReadOnly
	private BigDecimal total;
	
	@ReadOnly
	private BigDecimal subtotal;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Empresa empresa;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReadOnly
	@ReferenceView("Simple")
	private Provincia provincia;
	
	@Hidden
	@ReadOnly
	private String tipoEntidad;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CompraElectronica getCompra() {
		return compra;
	}

	public void setCompra(CompraElectronica compra) {
		this.compra = compra;
	}

	public Impuesto getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(Impuesto impuesto) {
		this.impuesto = impuesto;
	}

	public BigDecimal getImporteImpuesto() {
		return importeImpuesto;
	}

	public void setImporteImpuesto(BigDecimal importeImpuesto) {
		this.importeImpuesto = importeImpuesto;
	}

	public BigDecimal getAlicuota() {
		return alicuota;
	}

	public void setAlicuota(BigDecimal alicuota) {
		this.alicuota = alicuota;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public String getTipoEntidad() {
		return tipoEntidad;
	}

	public void setTipoEntidad(String tipoEntidad) {
		this.tipoEntidad = tipoEntidad;
	}

	@Override
	public String generadaPorId() {
		return this.getCompra().getId();
	}

	@Override
	public String generadaPorTipoEntidad() {
		return this.getTipoEntidad();
	}

	public Provincia getProvincia() {
		return provincia;
	}

	public void setProvincia(Provincia provincia) {
		this.provincia = provincia;
	}
}
