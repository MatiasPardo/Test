package org.openxava.inventario.actions;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.view.*;

public class BuscarProductoConStockSearchAction extends ReferenceSearchAction{

	@Override
	public void execute() throws Exception {
		super.execute();
		
		View viewParent = getViewInfo().getView().getParent();
				
		String depositoId = (String)viewParent.getValue("deposito.id");
		String condition = "";
		if (!Is.emptyString(depositoId)){
			condition = "${id} in (select i.producto.id from Inventario i where i.deposito.id = '" + depositoId + "')";
		}
		else{
			condition = "${id} = 'null'";
		}		
		this.getTab().setBaseCondition(condition);
	}
	
}
