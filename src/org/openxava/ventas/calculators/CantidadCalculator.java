package org.openxava.ventas.calculators;

import java.math.*;

import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class CantidadCalculator implements ICalculator{
	private String productoID;
	
	public String getProductoID() {
		return productoID;
	}

	public void setProductoID(String productoID) {
		this.productoID = productoID;
	}

	public Object calculate() throws Exception {
	 	return BigDecimal.ZERO;
	}
}
