package org.openxava.mercadolibre.actions;

import org.openxava.calculators.ICalculator;
import org.openxava.mercadolibre.model.Ecommerce;

@SuppressWarnings("serial")
public class EcommerceDefaulCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		return Ecommerce.MercadoLibre;
	}  
}
