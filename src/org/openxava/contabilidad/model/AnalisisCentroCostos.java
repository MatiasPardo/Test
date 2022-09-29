package org.openxava.contabilidad.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.base.model.Empresa;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.negocio.model.Sucursal;

@Entity

@Table(name="VIEW_ANALISISCENTROCOSTOS")

@View(members="ejercicio, periodo, empresa, sucursal;" + 
		"cuenta;" + 
		"centroCostos, unidadNegocio;" + 
		"debe, haber, saldo;")

@Tab(properties="empresa.nombre, ejercicio.nombre, periodo.nombre, centroCostos.codigo, unidadNegocio.codigo, cuenta.codigo, debe, haber, saldo, sucursal.nombre", 
		filter=SucursalEmpresaFilter.class, 
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL)

public class AnalisisCentroCostos {
	
	@ReadOnly
	@Hidden
	@Id
	// Empresa: 1
	// Sucursal: 1
	// Cta cble: 8
	// periodo: 6 (yyyymm)
	// Centro costos: 10
	// Unidad negocio: 10
	@Column(length=36)
	private String id;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private Empresa empresa;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private Sucursal sucursal;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly 
	@DescriptionsList(descriptionProperties="nombre", forTabs="Combo")
	private EjercicioContable ejercicio;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly 
	@DescriptionsList(descriptionProperties="nombre", forTabs="Combo")
	private PeriodoContable periodo;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly 
	@ReferenceView("Simple")
	private CuentaContable cuenta;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly 
	@DescriptionsList(descriptionProperties="codigo, nombre", forTabs="Combo")
	private CentroCostos centroCostos;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly 
	@DescriptionsList(descriptionProperties="codigo, nombre", forTabs="Combo")
	private UnidadNegocio unidadNegocio;
	
	@ReadOnly
	private BigDecimal debe;
	
	@ReadOnly
	private BigDecimal haber;
	
	@ReadOnly
	private BigDecimal saldo;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public EjercicioContable getEjercicio() {
		return ejercicio;
	}

	public void setEjercicio(EjercicioContable ejercicio) {
		this.ejercicio = ejercicio;
	}

	public PeriodoContable getPeriodo() {
		return periodo;
	}

	public void setPeriodo(PeriodoContable periodo) {
		this.periodo = periodo;
	}

	public CuentaContable getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaContable cuenta) {
		this.cuenta = cuenta;
	}

	public CentroCostos getCentroCostos() {
		return centroCostos;
	}

	public void setCentroCostos(CentroCostos centroCostos) {
		this.centroCostos = centroCostos;
	}

	public UnidadNegocio getUnidadNegocio() {
		return unidadNegocio;
	}

	public void setUnidadNegocio(UnidadNegocio unidadNegocio) {
		this.unidadNegocio = unidadNegocio;
	}

	public BigDecimal getDebe() {
		return debe;
	}

	public void setDebe(BigDecimal debe) {
		this.debe = debe;
	}

	public BigDecimal getHaber() {
		return haber;
	}

	public void setHaber(BigDecimal haber) {
		this.haber = haber;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}
}
