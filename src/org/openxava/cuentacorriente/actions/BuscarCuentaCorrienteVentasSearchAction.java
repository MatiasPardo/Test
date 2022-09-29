package org.openxava.cuentacorriente.actions;

import org.openxava.actions.*;
import org.openxava.view.*;


public class BuscarCuentaCorrienteVentasSearchAction extends ReferenceSearchAction{
	
	@Override
	public void execute() throws Exception {
		super.execute();
		
		View viewParent = getViewInfo().getView().getParent();
		String empresaId = (String)viewParent.getValue("empresa.id");
		if (empresaId == null) empresaId = "";
		
		String condition = "${empresa.id} = '" + empresaId + "' and ";
		
		String clienteId = (String)viewParent.getValue("cliente.id");
		if (clienteId == null) clienteId = "";
		
		condition += "${cliente.id} = '" + clienteId + "'";
		
		condition += "and ${pendiente} = 't' and "; 
		
		if (getViewInfo().getMemberName().equalsIgnoreCase("origen")){
			condition += "${ingreso} > 0";
		}
		else{
			condition += "${egreso} > 0";
		}		
		this.getTab().setBaseCondition(condition);
	}
}
