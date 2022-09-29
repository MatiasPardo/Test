package org.openxava.ventas.calculators;

import java.math.*;

import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class PrecioImporteFinalCalculator implements ICalculator{
	private BigDecimal porcentaje;
	
	public BigDecimal getPorcentaje() {
		return porcentaje;
	}
	public void setPorcentaje(BigDecimal porcentaje) {
		this.porcentaje = porcentaje;
	}

	private BigDecimal precioBase;

	public BigDecimal getPrecioBase() {
		return precioBase;
	}
	public void setPrecioBase(BigDecimal precioBase) {
		this.precioBase = precioBase;
	}

	public Object calculate() throws Exception {
		BigDecimal importe = new BigDecimal(0);
		if ((this.getPrecioBase() != null) && (this.getPorcentaje() != null)){
			importe = this.getPrecioBase().multiply(this.getPorcentaje().divide(new BigDecimal(100)));
			importe = importe.add(this.getPrecioBase());
		}
		return importe;
	}

}
