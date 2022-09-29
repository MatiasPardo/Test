package org.openxava.tesoreria.calculators;

import java.math.BigDecimal;

import org.openxava.calculators.ICalculator;
import org.openxava.tesoreria.model.ReciboCobranza;

@SuppressWarnings("serial")
public class DiferenciaItemReciboCobranzaCalculator implements ICalculator{

	private BigDecimal importe;
	
	private BigDecimal pendiente;
	
	public BigDecimal getImporte() {
		return importe == null ? BigDecimal.ZERO : importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public BigDecimal getPendiente() {
		return pendiente == null ? BigDecimal.ZERO : pendiente;
	}

	public void setPendiente(BigDecimal pendiente) {
		this.pendiente = pendiente;
	}

	@Override
	public Object calculate() throws Exception {
		BigDecimal diferencia = this.getImporte().subtract(this.getPendiente());
		return ReciboCobranza.convertirStringPendienteCobrar(diferencia);
	}

}
