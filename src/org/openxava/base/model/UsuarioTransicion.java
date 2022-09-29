package org.openxava.base.model;

import javax.persistence.*;

import org.openxava.annotations.*;

import com.openxava.naviox.model.*;

@Entity

@Tab(properties="transicion.nombre, transicion.entidad.entidad, usuarioHabilitado.name, transicion.origen.nombre, transicion.destino1.nombre, transicion.condicion1, transicion.destino2.nombre")

public class UsuarioTransicion extends ObjetoNegocio{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList
	@NoCreate @NoModify
	@SearchKey
	private User usuarioHabilitado;

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private TransicionEstado transicion;
	
	
	public TransicionEstado getTransicion() {
		return transicion;
	}

	public void setTransicion(TransicionEstado transicion) {
		this.transicion = transicion;
	}

	public User getUsuarioHabilitado() {
		return usuarioHabilitado;
	}

	public void setUsuarioHabilitado(User usuarioHabilitado) {
		this.usuarioHabilitado = usuarioHabilitado;
	}
}
