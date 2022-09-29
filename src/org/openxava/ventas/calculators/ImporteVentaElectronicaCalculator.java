package org.openxava.ventas.calculators;

import java.math.*;

import org.openxava.calculators.*;

@SuppressWarnings("serial")
public abstract class ImporteVentaElectronicaCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		return this.calcularImporte().setScale(4, RoundingMode.HALF_UP);
	}

	protected abstract BigDecimal calcularImporte();
}
