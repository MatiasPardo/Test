package org.openxava.negocio.calculators;

import org.openxava.calculators.*;
import org.openxava.negocio.model.*;

@SuppressWarnings("serial")
public class SucursalPrincipalCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		return Sucursal.sucursalDefault();
	}

}
