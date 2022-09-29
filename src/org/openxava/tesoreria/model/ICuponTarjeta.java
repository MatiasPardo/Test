package org.openxava.tesoreria.model;

import java.math.BigDecimal;

public interface ICuponTarjeta {
	
	public LiquidacionTarjetaCredito getLiquidacionTarjeta();

	public void setLiquidacionTarjeta(LiquidacionTarjetaCredito liquidacionTarjeta);
	
	public BigDecimal importeCupon();
}
