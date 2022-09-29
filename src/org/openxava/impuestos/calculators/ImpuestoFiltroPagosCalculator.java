package org.openxava.impuestos.calculators;

import org.openxava.calculators.*;
import org.openxava.impuestos.model.*;

@SuppressWarnings("serial")
public class ImpuestoFiltroPagosCalculator implements ICalculator{
	
	private DefinicionImpuesto definicionImpuesto;
		
	public DefinicionImpuesto getDefinicionImpuesto() {
		return definicionImpuesto;
	}

	public void setDefinicionImpuesto(DefinicionImpuesto definicionImpuesto) {
		this.definicionImpuesto = definicionImpuesto;
	}



	@Override
	public Object calculate() throws Exception {
		if (this.getDefinicionImpuesto() != null){
			return Boolean.valueOf(this.getDefinicionImpuesto().isPagos());
		}
		else{
			return Boolean.FALSE;
		}		
	}

}
