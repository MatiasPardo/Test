package org.openxava.inventario.actions;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.view.*;

public class BuscarDespachoPorProductoSearchAction extends ReferenceSearchAction{
	
	@Override
	public void execute() throws Exception {
		super.execute();
		
		View viewParent = getViewInfo().getView().getParent();
				
		String depositoId = (String)viewParent.getValue("deposito.id");
		String productoId = (String)viewParent.getValue("producto.id");
		String condition = "";
		if (!Is.emptyString(depositoId) && !Is.emptyString(productoId)){
			condition = "${id} in (select i.despacho.id from Inventario i where i.deposito.id = '" + depositoId + "' and i.producto.id = '" + productoId + "')";
		}
		else{
			condition = "${id} = 'null'";
		}		
		this.getTab().setBaseCondition(condition);
	}
}
