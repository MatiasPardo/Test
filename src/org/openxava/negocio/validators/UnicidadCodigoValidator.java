package org.openxava.negocio.validators;

import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class UnicidadCodigoValidator implements IValidator{
	private String modelo = "";
	private String codigo = "";
	private String idEntidad = "";
	
	@Override
	public void validate(Messages errors) throws Exception {
		if (!Is.emptyString(this.getModelo()) && (!Is.emptyString(this.getCodigo()))){
			String sql = " from " + this.getModelo() + " where codigo = :codigo";
			if (!Is.emptyString(this.getIdEntidad())){
				sql += " and id <> :id";
			}
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("codigo", this.getCodigo());
			if (!Is.emptyString(this.getIdEntidad())){
				query.setParameter("id", this.getIdEntidad());
			}		
			query.setMaxResults(1);
			List<?> results = query.getResultList();
			if (!results.isEmpty()){
				errors.add("Código repetido"); 
			}

		}				
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getIdEntidad() {
		return idEntidad;
	}

	public void setIdEntidad(String idEntidad) {
		this.idEntidad = idEntidad;
	}
}

