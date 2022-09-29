package org.openxava.contabilidad.model;

import java.math.BigDecimal;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.Digits;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.actions.*;
import org.openxava.contabilidad.validators.*;
import org.openxava.validators.ValidationException;

@Entity

@Views({
	@View(members="Principal[codigo, activo;" + 
			"nombre;" + 
			"desde, hasta, estado;" + 
			"indiceInflacion;];" + 
			"Reportes[fechaDesde, fechaHasta];"),
	@View(name="Simple",
		members="codigo, nombre")	
})

@Tab(properties="ejercicio.nombre, codigo, nombre, estado, desde, hasta",
	defaultOrder="${desde} desc")

@EntityValidator(
	value=PeriodoContableValidator.class, 
	properties= {
		@PropertyValue(name="estado"),
		@PropertyValue(name="desde"),
		@PropertyValue(name="hasta")
	}
)

public class PeriodoContable extends ObjetoEstatico implements IParametrosReporteContable{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private EjercicioContable ejercicio;
	
	@Required
	@ReadOnly
	private Date desde;
	
	@Required
	@ReadOnly
	private Date hasta;
	
	@OnChange(OnChangeEstadoPeriodoContableAction.class)
	private EstadoPeriodoContable estado = EstadoPeriodoContable.Borrador;

	@Hidden
	@Digits(integer=19, fraction=4)
	private BigDecimal indiceInflacion = null;
	
	public EjercicioContable getEjercicio() {
		return ejercicio;
	}

	public void setEjercicio(EjercicioContable ejercicio) {
		this.ejercicio = ejercicio;
	}

	public Date getDesde() {
		return desde;
	}

	public void setDesde(Date desde) {
		this.desde = desde;
	}

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}

	public EstadoPeriodoContable getEstado() {
		if (estado == null){
			return EstadoPeriodoContable.Borrador;
		}
		else{
			return estado;
		}
	}

	public void setEstado(EstadoPeriodoContable estado) {
		this.estado = estado;
	}
	
	public boolean valido(Date fecha){
		
		if ((fecha.equals(this.getDesde()) || fecha.after(this.getDesde())) &&
		   (fecha.equals(this.getHasta()) || fecha.before(this.getHasta()))){
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean permiteAsientos(){
		if (this.getEstado().equals(EstadoPeriodoContable.Abierto)){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public EjercicioContable ejercicio() {
		return this.getEjercicio();
	}

	public BigDecimal getIndiceInflacion() {
		return indiceInflacion;
	}

	public void setIndiceInflacion(BigDecimal indiceInflacion) {
		this.indiceInflacion = indiceInflacion;
	}
	
	// Fechas para que el usuario cambie para los reportes
	@Transient
	@Hidden
	private Date fechaDesde;

	@Transient
	@Hidden
	private Date fechaHasta;
	
	public Date getFechaDesde() {
		return this.getDesde();
	}

	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}

	public Date getFechaHasta() {
		return this.getHasta();
	}

	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}
	
	public void validarRangoFechas(Date fechaDesde, Date fechaHasta){
		if (fechaDesde.compareTo(fechaHasta) > 0){
			throw new ValidationException(UtilERP.convertirString(fechaDesde) + " debe ser anterior a " + UtilERP.convertirString(fechaHasta));
		}
		if (fechaDesde.compareTo(this.getDesde()) < 0){
			throw new ValidationException(UtilERP.convertirString(fechaDesde) + " no puede ser anterior a " + UtilERP.convertirString(this.getDesde()));			
		}
		if (fechaDesde.compareTo(this.getHasta()) > 0){
			throw new ValidationException(UtilERP.convertirString(fechaDesde) + " no puede ser posterior a " + UtilERP.convertirString(this.getHasta()));			
		}
		if (fechaHasta.compareTo(this.getDesde()) < 0){
			throw new ValidationException(UtilERP.convertirString(fechaHasta) + " no puede ser anterior a " + UtilERP.convertirString(this.getDesde()));			
		}
		if (fechaHasta.compareTo(this.getHasta()) > 0){
			throw new ValidationException(UtilERP.convertirString(fechaHasta) + " no puede ser posterior a " + UtilERP.convertirString(this.getHasta()));			
		}
	}
}
