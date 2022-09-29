package org.openxava.cuentacorriente.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.SearchKey;
import org.openxava.annotations.Stereotype;
import org.openxava.base.model.Empresa;
import org.openxava.negocio.model.IGeneradoPor;

@MappedSuperclass

public class CtaCteSaldoAcumulado implements IGeneradoPor{

	@Id 
	@ReadOnly
	@Hidden
	private String id;
	
	@ReadOnly
	private Date fecha;
	
	@ReadOnly
	@Stereotype("DATETIME")
	private Date fechaCreacion;
	
	@ReadOnly
	private Date fechaVencimiento;
	
	@Column(length=25) 
	@ReadOnly 
	private String tipo = "";
	
	@Column(length=20) 
	@ReadOnly
	@SearchKey
	private String numero = "";
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Empresa empresa;
	
	@ReadOnly
	private Boolean pendiente;
	
	@org.hibernate.annotations.Formula("(case when pendiente = 't' then cantidad_dias_hoy(fechaVencimiento) * -1 else 0 end)")
	private Integer dias;
	
	@ReadOnly
	private BigDecimal ingreso1;
	
	@ReadOnly
	private BigDecimal egreso1;
	
	@ReadOnly
	private BigDecimal saldoAcumulado1;
	
	@ReadOnly
	private BigDecimal cotizacion;
	
	@ReadOnly
	private BigDecimal ingreso2;
	
	@ReadOnly
	private BigDecimal egreso2;
	
	@ReadOnly
	private BigDecimal saldoAcumulado2;

	@ReadOnly
	@Hidden
	@Column(length=32)
	private String idTransaccion;
	
	@ReadOnly
	@Hidden
	@Column(length=100)	
	private String tipoEntidad;
	
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

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
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

	public Boolean getPendiente() {
		return pendiente;
	}

	public void setPendiente(Boolean pendiente) {
		this.pendiente = pendiente;
	}

	public Integer getDias() {
		return dias;
	}

	public void setDias(Integer dias) {
		this.dias = dias;
	}

	public BigDecimal getIngreso1() {
		return ingreso1;
	}

	public void setIngreso1(BigDecimal ingreso1) {
		this.ingreso1 = ingreso1;
	}

	public BigDecimal getEgreso1() {
		return egreso1;
	}

	public void setEgreso1(BigDecimal egreso1) {
		this.egreso1 = egreso1;
	}

	public BigDecimal getSaldoAcumulado1() {
		return saldoAcumulado1;
	}

	public void setSaldoAcumulado1(BigDecimal saldoAcumulado1) {
		this.saldoAcumulado1 = saldoAcumulado1;
	}

	public BigDecimal getCotizacion() {
		return cotizacion;
	}

	public void setCotizacion(BigDecimal cotizacion) {
		this.cotizacion = cotizacion;
	}

	public BigDecimal getIngreso2() {
		return ingreso2;
	}

	public void setIngreso2(BigDecimal ingreso2) {
		this.ingreso2 = ingreso2;
	}

	public BigDecimal getEgreso2() {
		return egreso2;
	}

	public void setEgreso2(BigDecimal egreso2) {
		this.egreso2 = egreso2;
	}

	public BigDecimal getSaldoAcumulado2() {
		return saldoAcumulado2;
	}

	public void setSaldoAcumulado2(BigDecimal saldoAcumulado2) {
		this.saldoAcumulado2 = saldoAcumulado2;
	}

	public String getIdTransaccion() {
		return idTransaccion;
	}

	public void setIdTransaccion(String idTransaccion) {
		this.idTransaccion = idTransaccion;
	}

	public String getTipoEntidad() {
		return tipoEntidad;
	}

	public void setTipoEntidad(String tipoEntidad) {
		this.tipoEntidad = tipoEntidad;
	}

	@Override
	public String generadaPorId() {
		return this.getIdTransaccion();
	}

	@Override
	public String generadaPorTipoEntidad() {
		return this.getTipoEntidad();
	}

}
