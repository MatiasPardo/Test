package org.openxava.inventario.calculators;

import java.math.*;

import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class CantidadExcedePedidoCalculator implements ICalculator{

	private BigDecimal cantidadPreparar;
	
	private BigDecimal cantidadPendientePreparar;
		
	public BigDecimal getCantidadPreparar() {
		return cantidadPreparar == null ? BigDecimal.ZERO : this.cantidadPreparar;
	}

	public void setCantidadPreparar(BigDecimal cantidadPreparar) {
		this.cantidadPreparar = cantidadPreparar;
	}

	public BigDecimal getCantidadPendientePreparar() {
		return cantidadPendientePreparar == null ? BigDecimal.ZERO : this.cantidadPendientePreparar;
	}

	public void setCantidadPendientePreparar(BigDecimal cantidadPendientePreparar) {
		this.cantidadPendientePreparar = cantidadPendientePreparar;
	}

	@Override
	public Object calculate() throws Exception {
		if (this.getCantidadPreparar().compareTo(this.getCantidadPendientePreparar()) > 0){
			return this.getCantidadPreparar().subtract(this.getCantidadPendientePreparar());
		}
		else{
			return BigDecimal.ZERO;
		}
	}

}
