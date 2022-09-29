package org.openxava.afip.model;

import java.math.*;

public class ImpuestoAfip {
	
	private BigDecimal importe = BigDecimal.ZERO;
	
	private BigDecimal baseImponible = BigDecimal.ZERO;
	
	private BigDecimal alicuota = BigDecimal.ZERO;

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public BigDecimal getBaseImponible() {
		return baseImponible;
	}

	public void setBaseImponible(BigDecimal baseImponible) {
		this.baseImponible = baseImponible;
	}

	public BigDecimal getAlicuota() {
		return alicuota;
	}

	public void setAlicuota(BigDecimal alicuota) {
		this.alicuota = alicuota;
	}
}
