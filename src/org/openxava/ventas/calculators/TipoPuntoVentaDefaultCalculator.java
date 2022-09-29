package org.openxava.ventas.calculators;

import org.openxava.calculators.*;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class TipoPuntoVentaDefaultCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		return TipoPuntoVenta.Electronico;
	}

}
