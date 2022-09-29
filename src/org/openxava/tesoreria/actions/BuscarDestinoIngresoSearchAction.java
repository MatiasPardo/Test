package org.openxava.tesoreria.actions;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.view.*;

public class BuscarDestinoIngresoSearchAction extends ReferenceSearchAction{
	@Override
	public void execute() throws Exception {
		super.execute();
		
		View viewParent = getViewInfo().getView().getParent();
		if (viewParent.getValue("empresa") != null){
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
		else if (viewParent.getParent() != null){
			if (viewParent.getParent().getValue("empresa") != null){
				String empresa = viewParent.getParent().getValueString("empresa.id");		
				if (!Is.emptyString(empresa)){
					String condition = "${empresa.id} = '" + empresa + "'"; 
					this.getTab().setBaseCondition(condition);
				}
			}
		}
	}
}
