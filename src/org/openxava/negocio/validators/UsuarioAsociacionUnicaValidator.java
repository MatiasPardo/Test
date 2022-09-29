package org.openxava.negocio.validators;

import javax.persistence.*;

import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import com.openxava.naviox.model.*;

@SuppressWarnings("serial")
public class UsuarioAsociacionUnicaValidator implements IValidator{
	
	private User usuario;

	private String modelo;
	
	private String atributoUsuario;
	
	private String id;
		
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public String getAtributoUsuario() {
		return atributoUsuario;
	}

	public void setAtributoUsuario(String atributoUsuario) {
		this.atributoUsuario = atributoUsuario;
	}

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}


	@Override
	public void validate(Messages errors) throws Exception {
		if (this.getUsuario() != null){
			String sql = "from " + this.getModelo() + " where " + this.getAtributoUsuario() + " = :usuario";
			if (!Is.emptyString(this.getId())){
				sql += " and id != :id";
			}
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("usuario", this.getUsuario());
			if (!Is.emptyString(this.getId())){
				query.setParameter("id", this.getId());
			}
			query.setMaxResults(1);
			query.setFlushMode(FlushModeType.COMMIT);
			if (!query.getResultList().isEmpty()){
				errors.add("usuario_repetido", this.getUsuario().getName());
			}
		}
	}
}
