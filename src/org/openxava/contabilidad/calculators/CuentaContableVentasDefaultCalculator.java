package org.openxava.contabilidad.calculators;

import org.openxava.base.model.*;
import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class CuentaContableVentasDefaultCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		return Esquema.getEsquemaApp().getCuentaContableVentas();
	}

}
