package org.openxava.ventas.calculators;

import java.util.*;

import javax.persistence.*;

import org.openxava.calculators.*;
import org.openxava.jpa.*;


@SuppressWarnings("serial")
public class ListaPrecioDefaultCalculator implements ICalculator{
	
	private Boolean costos = Boolean.FALSE;
		
	public Boolean getCostos() {
		return costos;
	}

	public void setCostos(Boolean costos) {
		this.costos = costos;
	}

	@Override
	public Object calculate() throws Exception {
		String sql = "from ListaPrecio where costo = :costos";		
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("costos", this.getCostos());
		query.setMaxResults(1);
		
		List<?> result = query.getResultList();		
		if (!result.isEmpty()){
			return result.get(0);
		}
		else{
			return null;
		}		
	}

}
