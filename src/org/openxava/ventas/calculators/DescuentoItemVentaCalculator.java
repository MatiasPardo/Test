package org.openxava.ventas.calculators;

import java.math.*;

@SuppressWarnings("serial")
public class DescuentoItemVentaCalculator extends ImporteVentaElectronicaCalculator{

	private BigDecimal porcentajeDescuento = BigDecimal.ZERO;
	
	private BigDecimal cantidad = BigDecimal.ZERO;
	
	private BigDecimal precioUnitario = BigDecimal.ZERO;
	
	public BigDecimal getPorcentajeDescuento() {
		return porcentajeDescuento;
	}
	
	public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) {
		if (porcentajeDescuento != null){
			this.porcentajeDescuento = porcentajeDescuento;
		}
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}
	public void setCantidad(BigDecimal cantidad) {
		if (cantidad != null){
			this.cantidad = cantidad;
		}
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}
	
	public void setPrecioUnitario(BigDecimal precioUnitario) {
		if (precioUnitario != null){
			this.precioUnitario = precioUnitario;
		}
	}

	@Override
	protected BigDecimal calcularImporte() {
		return this.getPrecioUnitario().multiply(this.getCantidad()).multiply(this.getPorcentajeDescuento()).divide(new BigDecimal(100)).negate();
	}
}
