package org.openxava.ventas.calculators;

import java.math.*;

@SuppressWarnings("serial")
public class IVACalculator extends ImporteVentaElectronicaCalculator{
	private BigDecimal tasa = BigDecimal.ZERO;

	private BigDecimal importe = BigDecimal.ZERO;
	
	public BigDecimal getTasa() {
		return tasa;
	}

	public void setTasa(BigDecimal tasa) {
		if (tasa != null){
			this.tasa = tasa;
		}
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		if (importe != null){
			this.importe = importe;
		}
	}

	protected BigDecimal calcularImporte(){
		return this.getImporte().multiply(this.getTasa()).divide(new BigDecimal(100));
	}
}
