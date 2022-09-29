package org.openxava.tesoreria.actions;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.view.*;

public class BuscarOrigenEgresoSearchAction extends ReferenceSearchAction{
	@Override
	public void execute() throws Exception {
		super.execute();
		
		View viewParent = getViewInfo().getView().getParent();
		String empresa = viewParent.getValueString("empresa.id");
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
