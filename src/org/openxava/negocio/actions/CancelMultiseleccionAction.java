package org.openxava.negocio.actions;

import org.openxava.actions.*;

public class CancelMultiseleccionAction extends CancelFromCustomListAction implements IChainAction{

	public void execute() throws Exception {
		super.execute();
	}	
	
	@Override
	public String getNextAction() throws Exception {
		return "Transaccion.editar";
	}
}

