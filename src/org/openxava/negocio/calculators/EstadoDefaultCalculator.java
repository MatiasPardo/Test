package org.openxava.negocio.calculators;

import org.openxava.base.model.*;
import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class EstadoDefaultCalculator implements ICalculator {
	public Object calculate() throws Exception {
	 	return Estado.Borrador;
	}
}
