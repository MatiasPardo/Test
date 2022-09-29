package org.openxava.ventas.validators;

import javax.persistence.*;

import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class ListaPrecioValidator implements IValidator{

	private String id;
	
	private Boolean costo;
	
	private Boolean principal;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getCosto() {
		return costo == null ? Boolean.FALSE : this.costo;
	}

	public void setCosto(Boolean costo) {
		this.costo = costo;
	}

	public Boolean getPrincipal() {
		return principal == null ? Boolean.FALSE : this.principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	@Override
	public void validate(Messages errors) throws Exception {
		if (Esquemas.getEsquemaApp().getListaPrecioUnica()){
			if (this.getCosto()){
				errors.add("No se puede activar una lista como costo. El costo es el precio base de la lista");				
			}
		}
		else{
			if (this.getPrincipal()){
				String sql = "from ListaPrecio where costo = :costo and principal = :principal";
				if (!Is.emptyString(this.getId())){
					sql += " and id != :id";
				}
				
				Query query = XPersistence.getManager().createQuery(sql);
				query.setParameter("costo", this.getCosto());
				query.setParameter("principal", this.getPrincipal());
				if (!Is.emptyString(this.getId())){
					query.setParameter("id", this.getId());
				}
				query.setMaxResults(1);
				query.setFlushMode(FlushModeType.COMMIT);
				if (!query.getResultList().isEmpty()){
					errors.add("Solo puede tener una lista principal");
				}
			}
		}
	}

}
