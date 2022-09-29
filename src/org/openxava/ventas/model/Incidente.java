package org.openxava.ventas.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.EmpresaFilter;
import org.openxava.base.model.*;
import org.openxava.proyectos.model.Proyecto;
import org.openxava.rrhh.calculators.EmpleadoSistemaCalculator;
import org.openxava.rrhh.model.Empleado;

@View(members="empresa, fecha, numero;"
		+ "estado, subestado, usuario;"
		+ "proyecto, prioridad, responsable, asignado;"
		+ "tema, link; detalle; observaciones;"  
		+ "historicoEstados;")

@Tab(
	filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION,
	properties="fecha, numero, subestado.nombre, tema, proyecto.nombre, responsable.nombre, asignado.nombre, observaciones, fechaCreacion, usuario",
	defaultOrder="${fechaCreacion} desc")

@Entity
public class Incidente extends Transaccion{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="numero, nombre", forTabs="Combo", 
				condition="${estado} in (1, 2)")
	@NoCreate @NoModify
	private Proyecto proyecto;
	
	@Column(length = 100)
	@Required
	private String tema;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private Prioridad prioridad;
	
	@Column(length=511)
	@Stereotype("MEMO")
	private String detalle;

	@Column(length=150)
	private String link;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	@DefaultValueCalculator(value=EmpleadoSistemaCalculator.class)
	private Empleado responsable;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	@DefaultValueCalculator(value=EmpleadoSistemaCalculator.class)
	private Empleado asignado;
	
	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Incidente";
	}

	public Proyecto getProyecto() {
		return proyecto;
	}

	public void setProyecto(Proyecto proyecto) {
		this.proyecto = proyecto;
	}

	public String getTema() {
		return tema;
	}

	public void setTema(String tema) {
		this.tema = tema;
	}

	public Prioridad getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(Prioridad prioridad) {
		this.prioridad = prioridad;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Empleado getResponsable() {
		return responsable;
	}

	public void setResponsable(Empleado responsable) {
		this.responsable = responsable;
	}

	public Empleado getAsignado() {
		return asignado;
	}

	public void setAsignado(Empleado asignado) {
		this.asignado = asignado;
	}
}
