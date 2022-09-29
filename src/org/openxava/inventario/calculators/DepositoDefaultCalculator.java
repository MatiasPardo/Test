package org.openxava.inventario.calculators;

import javax.persistence.*;

import org.openxava.calculators.*;
import org.openxava.jpa.*;

@SuppressWarnings("serial")
public class DepositoDefaultCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		Query query = XPersistence.getManager().createQuery("from Deposito d where d.principal = :principal");
		query.setParameter("principal", true);
		query.setMaxResults(1);
		try{
			return query.getSingleResult();
		}
		catch(Exception e){
			return null;
		}
	}

}
