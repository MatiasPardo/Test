package org.openxava.reclamos.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;

public class ParametrosAsignacionReclamo {

	@ManyToOne(fetch=FetchType.LAZY, optional=false) 
	@NoCreate @NoModify 
	@DescriptionsList(descriptionProperties="codigo, nombre")
	private UsuarioReclamo asignarA;
	
	private Date fechaDeAtencion;

	public UsuarioReclamo getAsignarA() {
		return asignarA;
	}

	public void setAsignarA(UsuarioReclamo asignarA) {
		this.asignarA = asignarA;
	}

	public Date getFechaDeAtencion() {
		return fechaDeAtencion;
	}

	public void setFechaDeAtencion(Date fechaDeAtencion) {
		this.fechaDeAtencion = fechaDeAtencion;
	}
}