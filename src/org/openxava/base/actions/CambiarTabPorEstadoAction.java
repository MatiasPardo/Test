package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.util.*;

public class CambiarTabPorEstadoAction extends TabBaseAction{

	@Override
	public void execute() throws Exception {
		String tabName = this.getTab().getTabName();
		if (Is.emptyString(tabName)){
			String tabInactivo = ObjetoEstatico.TABNAME_INACTIVOS;
			try{
				this.getTab().getMetaTab().getMetaComponent().getMetaTab(tabInactivo);
				this.getTab().setTabName(tabInactivo);
			}
			catch(Exception e){
				addError("No esta definida la vista de inactivos");
			}
			addMessage("INACTIVOS (con la misma acción se vuelte a la vista de activos)");
		}
		else{
			this.getTab().setTabName(null);
			addMessage("Activos");
		}		
	}
}
