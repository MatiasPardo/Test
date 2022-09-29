package org.openxava.planificacion.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;

@Entity

@View(members="nombre; desde, hasta; usuario, fechaCreacion;")

public class PeriodoPlanificacion extends ObjetoNegocio{
	
	@Column(length=25, unique=true)
	@Required
	@SearchKey
	private String nombre;
	
	@Required
	private Date desde;
	
	@Required
	private Date hasta;

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

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}
