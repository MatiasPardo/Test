package org.openxava.contabilidad.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;

@Entity

@Views({
	@View(members=
		"Principal{Principal[" + 
			"fechaCreacion, usuario;" +
			"fecha, numero;" +
			"empresa, sucursal;" + 
			"detalle;" +
			"];" +
			"items}"  		
		),
	@View(name="Simple",
		members="numero")
})

@Tab(properties="numero, fecha, empresa.nombre, periodo.ejercicio.nombre, periodo.nombre, detalle, fechaCreacion, usuario",
	filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION,	
	defaultOrder="${numero} desc")

public class AsientoConsolidado extends ObjetoNegocio{

	@Required 
	private Date fecha;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify	
	@ReadOnly	
	private Empresa empresa;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	@ReadOnly	
	private Sucursal sucursal;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	@ReadOnly	
	private Moneda moneda;
		
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private PeriodoContable periodo;
	
	@Column(length=100)
	@ReadOnly	
	private String detalle;
	
	@Column(length=20)  
	@SearchKey
	@ReadOnly	
	//@Action(value="Transaccion.cambiarNumero", alwaysEnabled=true)
	private String numero = new String("");
	
	@Hidden
	@ReadOnly
	private Long numeroInterno;
	
	@Hidden
	@ReadOnly
	@Column(length=100)
	private String tipoTransaccion;
	
	@OneToMany(mappedBy="asiento", cascade=CascadeType.ALL) 
	@ListProperties("cuenta.codigo, cuenta.nombre, debe, haber")
	@ReadOnly
	private Collection<ItemAsientoConsolidado> items;

	public Collection<ItemAsientoConsolidado> getItems() {
		return items;
	}
		
	public void setItems(Collection<ItemAsientoConsolidado> items) {
		this.items = items;
	}

	public PeriodoContable getPeriodo() {
		return periodo;
	}

	public void setPeriodo(PeriodoContable periodo) {
		this.periodo = periodo;
	}

	public String getTipoTransaccion() {
		return tipoTransaccion;
	}

	public void setTipoTransaccion(String tipoTransaccion) {
		this.tipoTransaccion = tipoTransaccion;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
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

	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Long getNumeroInterno() {
		return numeroInterno;
	}

	public void setNumeroInterno(Long numeroInterno) {
		this.numeroInterno = numeroInterno;
	}
	
	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
		
		this.setNumero(null);
		this.setNumeroInterno(null);
		this.setItems(null);		
	}
	
	@Override
	public void asignarNumeracion(String numeracion, Long numero){
		this.setNumero(numeracion);
		this.setNumeroInterno(numero);
	}

	public void actualizarDetalle() {
		if (!Is.emptyString(this.getTipoTransaccion())){
			this.setDetalle(Labels.get(this.getTipoTransaccion()));
		}
	}
}
