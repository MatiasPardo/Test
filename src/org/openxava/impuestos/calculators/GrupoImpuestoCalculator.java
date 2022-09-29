package org.openxava.impuestos.calculators;

import org.openxava.calculators.*;
import org.openxava.impuestos.model.*;

@SuppressWarnings("serial")
public class GrupoImpuestoCalculator implements ICalculator{
	
	private DefinicionImpuesto definicionImpuesto;
	
	public DefinicionImpuesto getDefinicionImpuesto() {
		return definicionImpuesto;
	}

	public void setDefinicionImpuesto(DefinicionImpuesto definicionImpuesto) {
		this.definicionImpuesto = definicionImpuesto;
	}

	@Override
	public Object calculate() throws Exception {
		GrupoImpuesto grupo = null;
		if (definicionImpuesto != null){
			grupo = definicionImpuesto.getGrupo();
		}
		if (grupo == null){
			grupo = GrupoImpuesto.SinGrupo;
		}
		return grupo;
	}

}
