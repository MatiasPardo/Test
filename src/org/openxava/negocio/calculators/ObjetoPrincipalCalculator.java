package org.openxava.negocio.calculators;

import javax.persistence.*;

import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.util.*;

@SuppressWarnings("serial")
public class ObjetoPrincipalCalculator implements ICalculator{

	private String entidad;
	
	public String getEntidad() {
		return entidad;
	}

	public void setEntidad(String entidad) {
		this.entidad = entidad;
	}


	@Override
	public Object calculate() throws Exception {
		if (!Is.emptyString(this.getEntidad())){
			Query query = XPersistence.getManager().createQuery(" from " + this.getEntidad() + " where principal = :principal");
			query.setParameter("principal", true);
			query.setMaxResults(1);
			try{
				return query.getSingleResult();
			}
			catch(Exception e){
				return null;
			}
		}
		else{
			return null;
		}
	}

}
