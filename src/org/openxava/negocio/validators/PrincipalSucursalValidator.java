package org.openxava.negocio.validators;

import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class PrincipalSucursalValidator implements IValidator{

	private String modelo = "";
	private String idEntidad = "";
	private Boolean principal = Boolean.FALSE;
	private Sucursal sucursal = null;
	
	@Override
	public void validate(Messages errors) throws Exception {
		if (!Is.emptyString(this.getModelo())){
			if (this.getSucursal() == null){
				errors.add("Falta asignar la sucursal");
			}
			else if (this.getPrincipal()){
				String sql = " from " + this.getModelo() + " where principal = :principal and sucursal.id = :sucursal";
				if (!Is.emptyString(this.getIdEntidad())){
					sql += " and id <> :id";
				}
				Query query = XPersistence.getManager().createQuery(sql);
				query.setParameter("principal", Boolean.TRUE);
				query.setParameter("sucursal", this.getSucursal().getId());
				if (!Is.emptyString(this.getIdEntidad())){
					query.setParameter("id", this.getIdEntidad());
				}		
				query.setMaxResults(1);
				List<?> results = query.getResultList();
				if (!results.isEmpty()){
					errors.add("Solo puede existir un principal por cada sucursal"); 
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

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
}
