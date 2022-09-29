package org.openxava.ventas.calculators;

import javax.persistence.Query;

import org.openxava.calculators.ICalculator;
import org.openxava.jpa.XPersistence;
import org.openxava.validators.ValidationException;

@SuppressWarnings("serial")
public class CondicionVentaPrincipalCalculator implements ICalculator{

	private Boolean ventas = true;
	
	public Boolean getVentas() {
		return ventas;
	}

	public void setVentas(Boolean ventas) {
		this.ventas = ventas;
	}

	@Override
	public Object calculate() throws Exception {
		if (this.getVentas() == null){
			throw new ValidationException("Falta asigar atributos ventas");
		}
		
		String sql = " from CondicionVenta where principal = :principal";
		if (this.getVentas()){
			sql += " and ventas = :valor"; 
		}
		else{
			sql += " and compras = :valor";
		}
		
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("principal", true);
		query.setParameter("valor", true);
		query.setMaxResults(1);
		try{
			return query.getSingleResult();
		}
		catch(Exception e){
			return null;
		}
	}
}
