package org.openxava.negocio.actions;

import org.openxava.actions.*;

public class TestEjecutarParametrosAction extends ViewBaseAction{

	@Override
	public void execute() throws Exception {
		addMessage("Ejecutado");
		this.closeDialog();		
	}

}
