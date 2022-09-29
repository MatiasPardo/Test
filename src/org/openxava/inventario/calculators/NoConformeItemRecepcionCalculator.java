package org.openxava.inventario.calculators;

import org.openxava.calculators.*;
import org.openxava.util.*;

@SuppressWarnings("serial")
public class NoConformeItemRecepcionCalculator implements ICalculator{

	private String motivo;
	
	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	@Override
	public Object calculate() throws Exception {
		if (Is.emptyString(this.getMotivo())){
			return Boolean.FALSE;
		}
		else{
			return Boolean.TRUE;
		}
	}
}
