package org.openxava.reclamos.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.negocio.model.*;
import org.openxava.view.View;

public class ParametrosReporteReclamo implements IParametrosReporte{
	
	private Date fechaServicioReclamo;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@DescriptionsList(descriptionProperties="codigo")
	@NoCreate @NoModify
	private UsuarioReclamo asignadoA;

	public Date getFechaServicioReclamo() {
		return fechaServicioReclamo;
	}

	public void setFechaServicioReclamo(Date _fechaServicioReclamo) {
		fechaServicioReclamo = _fechaServicioReclamo;
	}

	public UsuarioReclamo getAsignadoA() {
		return asignadoA;
	}

	public void setAsignadoA(UsuarioReclamo asignadoA) {
		this.asignadoA = asignadoA;
	}

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {
		// TODO Auto-generated method stub
		
	}
	
	
}
