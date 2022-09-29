package org.openxava.ventas.calculators;

import org.openxava.base.model.*;
import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class LimiteCreditoDefaultCalcultor implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		return Esquema.getEsquemaApp().getLimiteCredito();
	}

}
