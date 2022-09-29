package org.openxava.contabilidad.calculators;

import org.openxava.calculators.ICalculator;
import org.openxava.contabilidad.model.TipoCuentaInflacion;

@SuppressWarnings("serial")
public class TipoCuentaInflacionCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		return TipoCuentaInflacion.noAjustable;
	}

}
