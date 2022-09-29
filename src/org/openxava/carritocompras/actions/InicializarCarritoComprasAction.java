package org.openxava.carritocompras.actions;

import org.openxava.actions.*;

public class InicializarCarritoComprasAction extends TabBaseAction{

	@Override
	public void execute() throws Exception {
		this.getTab().setCustomizeAllowed(false);		
	}

}
