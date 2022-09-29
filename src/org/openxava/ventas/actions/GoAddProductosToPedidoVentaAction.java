package org.openxava.ventas.actions;

import org.openxava.actions.*;

public class GoAddProductosToPedidoVentaAction extends GoAddElementsToCollectionAction{
	@Override
	public void execute() throws Exception {
		super.execute();
	}
	
	@Override
	public String getNextController() { 
		return "AgregarProductosPedidoVenta"; 
	} 
}
