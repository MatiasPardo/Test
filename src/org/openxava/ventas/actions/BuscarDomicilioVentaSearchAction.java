package org.openxava.ventas.actions;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;
import org.openxava.view.*;

public class BuscarDomicilioVentaSearchAction extends ReferenceSearchAction{

	@Override
	public void execute() throws Exception {
		super.execute();
		
		View viewParent = getViewInfo().getView().getParent();
				
		String clienteId = (String)viewParent.getValue("cliente.id");
		String condition = "";
		if (!Is.emptyString(clienteId)){
			Cliente cliente = (Cliente)XPersistence.getManager().find(Cliente.class, clienteId);
			condition = "${id} = '" + cliente.getDomicilio().getId() + "'";			
			condition += " or ${id} in (select l.domicilio.id from LugarEntregaMercaderia l where l.cliente.id = '" + clienteId + "')";
		}
		else{
			condition = "${id} = 'null'";
		}		
		this.getTab().setBaseCondition(condition);
	}
}
