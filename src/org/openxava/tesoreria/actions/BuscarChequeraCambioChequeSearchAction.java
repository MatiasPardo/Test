package org.openxava.tesoreria.actions;

import org.openxava.actions.ReferenceSearchAction;
import org.openxava.util.Is;
import org.openxava.view.View;

public class BuscarChequeraCambioChequeSearchAction extends ReferenceSearchAction{

	@Override
	public void execute() throws Exception {
		super.execute();
		
		View view = getViewInfo().getView().getParent();
		String origen = view.getValueString("cuenta.id");
		if (!Is.emptyString(origen)){
			String condition = "${cuenta.id} = '" + origen + "'"; 
			this.getTab().setBaseCondition(condition);
		}
		else{
			// vacio
			this.getTab().setBaseCondition("${id} = null");
		}
	}
}

