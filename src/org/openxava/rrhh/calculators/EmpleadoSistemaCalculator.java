package org.openxava.rrhh.calculators;

import javax.persistence.Query;

import org.openxava.calculators.ICalculator;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Users;

@SuppressWarnings("serial")
public class EmpleadoSistemaCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		Query query = XPersistence.getManager().createQuery("from Empleado where usuarioSistema.name = :usuario");
		query.setParameter("usuario", Users.getCurrent());
		query.setMaxResults(1);
		try{
			return query.getSingleResult();
		}
		catch(Exception e){
			return null;
		}		
	}
}
