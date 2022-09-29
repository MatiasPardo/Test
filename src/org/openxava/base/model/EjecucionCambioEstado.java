package org.openxava.base.model;

import javax.persistence.*;

import org.openxava.annotations.*;

@Entity

@Views({
	@View(name="transicion", 
		members="Principal{Transicion[transicion]}" +
				"Auditoria{tipoEntidad; estadoOriginal; idEntidad};"
	) 
})

public class EjecucionCambioEstado extends ObjetoNegocio{
		
	@Hidden
	@ReadOnly
	@Column(length=32)
	private String idEntidad;
	
	@ReadOnly
	@Column(length=30)
	private String tipoEntidad;

	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList
	private EstadoEntidad estadoOriginal;
	
	@SearchKey
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@SearchAction(value="ReferenciaTransicion.buscar")
	@NoCreate @NoModify
	@NoFrame
	private TransicionEstado transicion;
	
	public TransicionEstado getTransicion() {
		return transicion;
	}

	public void setTransicion(TransicionEstado transicion) {
		this.transicion = transicion;
	}

	public String getIdEntidad() {
		return idEntidad;
	}

	public void setIdEntidad(String idEntidad) {
		this.idEntidad = idEntidad;
	}

	public String getTipoEntidad() {
		return tipoEntidad;
	}

	public void setTipoEntidad(String tipoEntidad) {
		this.tipoEntidad = tipoEntidad;
	}

	public EstadoEntidad getEstadoOriginal() {
		return estadoOriginal;
	}

	public void setEstadoOriginal(EstadoEntidad estadoOriginal) {
		this.estadoOriginal = estadoOriginal;
	}
}
