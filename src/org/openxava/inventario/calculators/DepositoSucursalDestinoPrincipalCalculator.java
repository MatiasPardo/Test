package org.openxava.inventario.calculators;

import javax.persistence.Query;

import org.openxava.calculators.ICalculator;
import org.openxava.inventario.model.Deposito;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.calculators.ObjetoPrincipalCalculator;
import org.openxava.negocio.model.Sucursal;

@SuppressWarnings("serial")
public class DepositoSucursalDestinoPrincipalCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		// El depósito de las sucursal principal (es la central) solo se devuelve si el usuario no pertenece a la sucursal principal
		// Si el usuario es de la central, tiene que devolver vacío
		Sucursal sucursal = Sucursal.sucursalDefault();
		Deposito deposito = null;
		if (!sucursal.getPrincipal()){
			ObjetoPrincipalCalculator calculator = new ObjetoPrincipalCalculator();
			calculator.setEntidad(Sucursal.class.getSimpleName());			
			sucursal = (Sucursal)calculator.calculate();
			if (sucursal != null){
				Query query = XPersistence.getManager().createQuery("from Deposito where principal = :principal and sucursal.id = :sucursal");
				query.setParameter("principal", Boolean.TRUE);
				query.setParameter("sucursal", sucursal.getId());
				query.setMaxResults(1);
				try{
					deposito = (Deposito)query.getSingleResult();
				}
				catch(Exception e){					
				}
			}			
		}	
		return deposito;
	}
	
}

