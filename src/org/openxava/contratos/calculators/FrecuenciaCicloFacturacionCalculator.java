package org.openxava.contratos.calculators;

import org.openxava.calculators.ICalculator;
import org.openxava.contratos.model.FrecuenciaCicloFacturacion;

@SuppressWarnings("serial")
public class FrecuenciaCicloFacturacionCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		return FrecuenciaCicloFacturacion.Mensual;
	}

}
