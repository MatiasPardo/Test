package org.openxava.tesoreria.actions;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.view.View;

public class BuscarChequeTerceroRechazoValoresSearchAction  extends ReferenceSearchAction{
	@Override
	public void execute() throws Exception {
		super.execute();
		
		View view = getViewInfo().getView().getParent();
		String empresa = view.getValueString("empresa.id");		
		if (!Is.emptyString(empresa)){
			String condition = "${tipoValor.comportamiento} = 1 and ${estado} = 1 and ${empresa.id} = '" + empresa + "'"; 
			this.getTab().setBaseCondition(condition);
		}
		else{
			// vacio				
			this.getTab().setBaseCondition("${id} = null");
		}		
	}
}
