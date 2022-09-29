package org.openxava.inventario.validators;

import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class DepositoConsignacionValidator implements IValidator{
	private String idEntidad;
	private Boolean consignacion = Boolean.FALSE;
	
	@Override
	public void validate(Messages errors) throws Exception {
	
		if (this.getConsignacion()){
			String sql = " from Deposito where consignacion = :consignacion";
			if (!Is.emptyString(this.getIdEntidad())){
				sql += " and id <> :id";
			}
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("consignacion", Boolean.TRUE);
			if (!Is.emptyString(this.getIdEntidad())){
				query.setParameter("id", this.getIdEntidad());
			}		
			query.setMaxResults(1);
			List<?> results = query.getResultList();
			if (!results.isEmpty()){
				errors.add("Solo puede existir un depósito para consignación"); 
			}
		}		
	}

	public String getIdEntidad() {
		return idEntidad;
	}

	public void setIdEntidad(String idEntidad) {
		this.idEntidad = idEntidad;
	}

	public Boolean getConsignacion() {
		return consignacion;
	}

	public void setConsignacion(Boolean consignacion) {
		this.consignacion = consignacion;
	}
}
