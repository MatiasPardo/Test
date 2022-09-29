package org.openxava.negocio.calculators;

import org.openxava.afip.model.*;
import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class TipoPersonaAfipCalculators implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		return TipoPersonaAfip.Empresa;		
	}

}
