package org.openxava.inventario.model;

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
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Tab;
import org.openxava.base.filter.EmpresaFilter;
import org.openxava.base.model.Empresa;
import org.openxava.ventas.model.Cliente;

@Entity

@Table(name="VIEW_PENDIENTECONSIGNACIONPORCLIENTE")

@Tab(
	filter=EmpresaFilter.class,
	properties="cliente.codigo, cliente.nombre, empresa.codigo, cantidadPendiente, importePendiente1, importePendiente2",
	baseCondition=EmpresaFilter.BASECONDITION,
	defaultOrder="${cliente.codigo} asc")

public class PendienteConsignacionPorCliente {
	
	@Id
	@Hidden
	@Column(length=34)
	private String id;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Cliente cliente;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Empresa empresa;
	
	@ReadOnly
	private BigDecimal cantidadPendiente;
	
	@ReadOnly
	private BigDecimal importePendiente1;
	
	@ReadOnly
	private BigDecimal importePendiente2;
	
	@ReadOnly
	private Date fechaPrimerRemito;
	
	@ReadOnly
	private Date fechaUltimoRemito;

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

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public BigDecimal getCantidadPendiente() {
		return cantidadPendiente;
	}

	public void setCantidadPendiente(BigDecimal cantidadPendiente) {
		this.cantidadPendiente = cantidadPendiente;
	}

	public BigDecimal getImportePendiente1() {
		return importePendiente1;
	}

	public void setImportePendiente1(BigDecimal importePendiente1) {
		this.importePendiente1 = importePendiente1;
	}

	public BigDecimal getImportePendiente2() {
		return importePendiente2;
	}

	public void setImportePendiente2(BigDecimal importePendiente2) {
		this.importePendiente2 = importePendiente2;
	}

	public Date getFechaPrimerRemito() {
		return fechaPrimerRemito;
	}

	public void setFechaPrimerRemito(Date fechaPrimerRemito) {
		this.fechaPrimerRemito = fechaPrimerRemito;
	}

	public Date getFechaUltimoRemito() {
		return fechaUltimoRemito;
	}

	public void setFechaUltimoRemito(Date fechaUltimoRemito) {
		this.fechaUltimoRemito = fechaUltimoRemito;
	}
}
