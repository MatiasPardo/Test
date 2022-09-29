package org.openxava.negocio.calculators;

import org.openxava.calculators.*;
import org.openxava.negocio.model.*;


@SuppressWarnings("serial")
public class TipoPorcentajeDescuentoDefaultCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		return TipoPorcentaje.Descuento;
	}

}
