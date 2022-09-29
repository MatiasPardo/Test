package org.openxava.contabilidad.calculators;

import java.math.*;

import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class ImporteDebeCalculator implements IOptionalCalculator{
	
	BigDecimal haber = BigDecimal.ZERO;
	
	public BigDecimal getHaber() {
		return haber;
	}

	public void setHaber(BigDecimal haber) {
		if (haber != null){
			this.haber = haber;
		}
	}

	@Override
	public Object calculate() throws Exception {
		return BigDecimal.ZERO;
	}

	@Override
	public boolean isCalculate() {
		if (this.getHaber().compareTo(BigDecimal.ZERO) == 0){
			return false;
		}
		else{
			return true;
		}
	}
}
