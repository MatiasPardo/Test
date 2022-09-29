package org.openxava.impuestos.calculators;

import static org.openxava.jpa.XPersistence.getManager;

import javax.persistence.*;

import org.openxava.calculators.*;
import org.openxava.impuestos.model.*;
import org.openxava.validators.ValidationException;

@SuppressWarnings("serial")
public class TasaImpuestoIvaDefaultCalculator implements ICalculator {
	
	public Object calculate() throws Exception {
		Query query = (Query)getManager().createQuery("from TasaImpuesto t where " +  		
				"t.principal = :principal");
				query.setParameter("principal", Boolean.TRUE);
		try{		
			TasaImpuesto tasa = (TasaImpuesto) query.getSingleResult();
			return tasa;
		}
		catch(Exception e){
			throw new ValidationException("Error al buscar tasa impuesto por defecto. Debe existir una única TasaImpuesto principal " + e.toString());
		}
	}
}
