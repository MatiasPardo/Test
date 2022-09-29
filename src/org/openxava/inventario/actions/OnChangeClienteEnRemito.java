package org.openxava.inventario.actions;

import org.openxava.actions.*;
import org.openxava.view.*;

public class OnChangeClienteEnRemito extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		View viewDomicilio = this.getView().getSubview("domicilioEntrega");
		viewDomicilio.clear();
	}
}
