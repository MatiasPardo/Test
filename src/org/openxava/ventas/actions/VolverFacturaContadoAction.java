package org.openxava.ventas.actions;

import org.openxava.actions.*;

public class VolverFacturaContadoAction extends ReturnAction implements IChainAction{

	@Override
	public String getNextAction() throws Exception {
		return "Transaccion.editar";
	}

}

