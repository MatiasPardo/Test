package org.openxava.compras.actions;

import org.openxava.afip.model.*;
import org.openxava.calculators.*;
import org.openxava.fisco.model.TipoComprobante;

@SuppressWarnings("serial")
public class TipoComprobanteComprasCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		// FALTA REGIONALIZAR
		return TipoComprobante.buscarPorId(TipoComprobanteArg.A.ordinal());
	}

}
