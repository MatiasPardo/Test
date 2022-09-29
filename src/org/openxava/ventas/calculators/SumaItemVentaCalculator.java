package org.openxava.ventas.calculators;

import java.math.*;


@SuppressWarnings("serial")
public class SumaItemVentaCalculator extends ImporteVentaElectronicaCalculator{
	
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
		BigDecimal porcentaje = (new BigDecimal(100)).subtract(this.getPorcentajeDescuento());
		return this.getCantidad().multiply(this.getPrecioUnitario()).multiply(porcentaje).divide(new BigDecimal(100));
	}
}
