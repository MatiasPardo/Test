package org.openxava.reclamos.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;

import com.openxava.naviox.model.*;

@Entity

public class UsuarioObjetoReclamo extends ObjetoNegocio{
	
	@OneToOne(fetch=FetchType.LAZY, optional=false, orphanRemoval=false)
	@DescriptionsList(descriptionProperties="name")
	@NoCreate @NoModify	
	private User usuarioSistema;

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@NoCreate @NoModify @ReadOnly
	@ReferenceView("Simple")
	private GrupoUsuarioObjetoReclamo grupo;
	
	public User getUsuarioSistema() {
		return usuarioSistema;
	}

	public void setUsuarioSistema(User usuarioSistema) {
		this.usuarioSistema = usuarioSistema;
	}

	public GrupoUsuarioObjetoReclamo getGrupo() {
		return grupo;
	}

	public void setGrupo(GrupoUsuarioObjetoReclamo grupo) {
		this.grupo = grupo;
	}
}
