package org.openxava.reclamos.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.util.*;

@Entity

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})


@View(name="Simple", members="codigo, nombre")

public class GrupoUsuarioObjetoReclamo extends ObjetoEstatico{
	
	public static GrupoUsuarioObjetoReclamo buscarGrupo(String usuario){
		String sql = "select grupo_id from {h-schema}UsuarioObjetoReclamo where usuariosistema_name = :usuario";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("usuario", Users.getCurrent());
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			return XPersistence.getManager().find(GrupoUsuarioObjetoReclamo.class, result.get(0));
		}
		else{
			return null;
		}
	}
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean administrador = Boolean.FALSE;
	
	@OneToMany(mappedBy="grupo", cascade=CascadeType.ALL)
	@ListProperties(value="usuarioSistema.name")
	private Collection<UsuarioObjetoReclamo> usuarios;

	public Collection<UsuarioObjetoReclamo> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(Collection<UsuarioObjetoReclamo> usuarios) {
		this.usuarios = usuarios;
	}

	public Boolean getAdministrador() {
		return administrador == null ? Boolean.FALSE : this.administrador;
	}

	public void setAdministrador(Boolean administrador) {
		this.administrador = administrador;
	}
}
