package org.openxava.tesoreria.actions;

import org.openxava.actions.ReferenceSearchAction;
import org.openxava.util.Is;
import org.openxava.view.View;

public class BuscarChequeCambioSearchAction extends ReferenceSearchAction{

	@Override
	public void execute() throws Exception {
		super.execute();
		
		this.getTab().setTabName("ValoresPropios");
		
		View view = getViewInfo().getView().getParent();
		String empresa = view.getValueString("empresa.id");
		if (!Is.emptyString(empresa)){
			String condition = "${empresa.id} = '" + empresa + "'"; 
			this.getTab().setBaseCondition(condition);
		}
		else{
			// vacio
			this.getTab().setBaseCondition("${id} = null");
		}
	}
}

