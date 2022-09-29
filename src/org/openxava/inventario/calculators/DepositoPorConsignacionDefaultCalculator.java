package org.openxava.inventario.calculators;

import javax.persistence.*;

import org.openxava.calculators.*;
import org.openxava.jpa.*;

@SuppressWarnings("serial")
public class DepositoPorConsignacionDefaultCalculator implements ICalculator{

	private Boolean consignacion;
	
	public Boolean getConsignacion() {
		return consignacion == null ? Boolean.FALSE : consignacion;
	}
	
	public void setConsignacion(Boolean consignacion) {
		this.consignacion = consignacion;
	}


	@Override
	public Object calculate() throws Exception {
		if (this.getConsignacion()){
			Query query = XPersistence.getManager().createQuery("from Deposito d where d.consignacion = :consignacion");
			query.setParameter("consignacion", true);
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

