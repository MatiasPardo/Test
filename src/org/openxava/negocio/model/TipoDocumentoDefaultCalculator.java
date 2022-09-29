package org.openxava.negocio.model;

import org.openxava.base.model.*;
import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class TipoDocumentoDefaultCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		return Esquema.getEsquemaApp().getTipoDocumento();
	}	
}
