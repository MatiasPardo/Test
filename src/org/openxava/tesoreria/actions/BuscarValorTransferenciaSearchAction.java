package org.openxava.tesoreria.actions;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.view.*;

public class BuscarValorTransferenciaSearchAction extends ReferenceSearchAction{
	
	@Override
	public void execute() throws Exception {
		super.execute();
		
		View viewParent = getViewInfo().getView().getParent();
		String tesoreria = viewParent.getValueString("origen.id");
		String tipoValor = viewParent.getValueString("tipoValor.id");
		if ((!Is.emptyString(tesoreria)) && (!Is.emptyString(tipoValor))){
			String condition = "${estado} = 0 AND ${tesoreria.id} = '" + tesoreria + "' AND ${tipoValor.id} = '" + tipoValor + "' " + 
					"AND ${tipoValor.consolidaAutomaticamente} = 'f'";
			this.getTab().setBaseCondition(condition);
		}
		else{
			// vacio
			this.getTab().setBaseCondition("${id} = null");
		}
	}
	
}
