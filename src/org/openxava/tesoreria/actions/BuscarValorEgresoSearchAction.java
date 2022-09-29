package org.openxava.tesoreria.actions;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.view.*;

public class BuscarValorEgresoSearchAction extends ReferenceSearchAction{

	@Override
	public void execute() throws Exception {
		super.execute();
		
		View viewParent = getViewInfo().getView().getParent();
		String tesoreria = viewParent.getValueString("origen.id");
		String tipoValor = viewParent.getValueString("tipoValor.id");
		if ((!Is.emptyString(tesoreria)) && (!Is.emptyString(tipoValor))){
			String condition = "${historico} = 'f' AND ${anulado} = 'f' AND ${tipoValor.consolidaAutomaticamente} = 'f' " +
					"AND ${tesoreria.id} = '" + tesoreria + "' AND ${tipoValor.id} = '" + tipoValor + "'";
			this.getTab().setBaseCondition(condition);
		}
		else{
			// vacio
			this.getTab().setBaseCondition("${id} = null");
		}
	}
}
