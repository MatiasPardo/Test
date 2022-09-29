package org.openxava.compras.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;


@Entity

@Table(name="VIEW_COMPRASPORDIA")

@Tab(properties="empresa.codigo, fecha, importeNeto1, importeNeto2, iva1, iva2", 
	defaultOrder="${fecha} desc", 
	filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION)

@View(members="empresa, fecha; importeNeto1, iva1, importeNeto2, iva2;" + 
		"compras;")

public class ComprasDiaria {
	@Id
	@Hidden
	@Column(length=40)
	private String id;
	
	@ManyToOne(optional = true, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="codigo")
	@NoCreate @NoModify
	private Empresa empresa;
	
	@ReadOnly
	private Date fecha;
	
	@ReadOnly
	private BigDecimal importeNeto1;
	
	@ReadOnly
	private BigDecimal importeNeto2;

	@ReadOnly
	private BigDecimal iva1;
	
	@ReadOnly
	private BigDecimal iva2;
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public BigDecimal getImporteNeto1() {
		return importeNeto1;
	}

	public void setImporteNeto1(BigDecimal importeNeto1) {
		this.importeNeto1 = importeNeto1;
	}

	public BigDecimal getImporteNeto2() {
		return importeNeto2;
	}

	public void setImporteNeto2(BigDecimal importeNeto2) {
		this.importeNeto2 = importeNeto2;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BigDecimal getIva1() {
		return iva1;
	}

	public void setIva1(BigDecimal iva1) {
		this.iva1 = iva1;
	}

	public BigDecimal getIva2() {
		return iva2;
	}

	public void setIva2(BigDecimal iva2) {
		this.iva2 = iva2;
	}

	@Condition("${empresa.id} = ${this.empresa.id} and ${fecha} = ${this.fecha} and ${estado} = 1")
	@ListProperties("numero, tipo, tipoOperacion, proveedor.codigo, proveedor.nombre, subtotalCtaCte1, subtotalCtaCte2, totalCtaCte1, totalCtaCte2")
	@OrderBy("fechaCreacion desc")
	public Collection<CompraElectronica> getCompras() {
		return null;
	}	

}
