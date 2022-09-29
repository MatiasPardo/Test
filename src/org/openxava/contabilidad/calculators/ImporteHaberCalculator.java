package org.openxava.contabilidad.calculators;

import java.math.*;

import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class ImporteHaberCalculator implements IOptionalCalculator{

	BigDecimal debe = BigDecimal.ZERO;
	
	public BigDecimal getDebe() {
		return debe;
	}

	public void setDebe(BigDecimal debe) {
		if (debe != null){
			this.debe = debe;
		}
	}

	@Override
	public Object calculate() throws Exception {		
		return BigDecimal.ZERO;
	}

	@Override
	public boolean isCalculate() {
		if (this.getDebe().compareTo(BigDecimal.ZERO) == 0){
			return false;
		}
		else{
			return true;
		}
		
	}

}
