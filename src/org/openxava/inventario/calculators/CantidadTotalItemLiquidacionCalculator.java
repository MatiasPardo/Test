package org.openxava.inventario.calculators;

import java.math.*;

import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class CantidadTotalItemLiquidacionCalculator implements ICalculator{
	
	private BigDecimal cantidadFacturar;
	
	private BigDecimal cantidadDevolucion;
	
	public BigDecimal getCantidadFacturar() {
		return cantidadFacturar == null ? BigDecimal.ZERO : cantidadFacturar;
	}

	public void setCantidadFacturar(BigDecimal cantidadFacturar) {
		this.cantidadFacturar = cantidadFacturar;
	}

	public BigDecimal getCantidadDevolucion() {
		return cantidadDevolucion == null ? BigDecimal.ZERO : cantidadDevolucion;
	}

	public void setCantidadDevolucion(BigDecimal cantidadDevolucion) {
		this.cantidadDevolucion = cantidadDevolucion;
	}

	@Override
	public Object calculate() throws Exception {
		return this.getCantidadDevolucion().add(this.getCantidadFacturar());
	}

}
