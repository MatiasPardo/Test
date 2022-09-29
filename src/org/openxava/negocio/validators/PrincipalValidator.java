package org.openxava.negocio.validators;

import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class PrincipalValidator  implements IValidator{

	private String modelo = "";
	private String idEntidad = "";
	private Boolean principal = Boolean.FALSE;
	
	@Override
	public void validate(Messages errors) throws Exception {
		if (!Is.emptyString(this.getModelo())){
			if (this.getPrincipal()){
				String sql = " from " + this.getModelo() + " where principal = :principal";
				if (!Is.emptyString(this.getIdEntidad())){
					sql += " and id <> :id";
				}
				Query query = XPersistence.getManager().createQuery(sql);
				query.setParameter("principal", Boolean.TRUE);
				if (!Is.emptyString(this.getIdEntidad())){
					query.setParameter("id", this.getIdEntidad());
				}		
				query.setMaxResults(1);
				query.setFlushMode(FlushModeType.COMMIT);
				List<?> results = query.getResultList();
				if (!results.isEmpty()){
					errors.add("Solo puede existir un principal"); 
				}
			}
		}
		else{
			throw new ValidationException("Falta asignar el modelo al validador Principal");
		}
		
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public String getIdEntidad() {
		return idEntidad;
	}

	public void setIdEntidad(String idEntidad) {
		this.idEntidad = idEntidad;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}
}
