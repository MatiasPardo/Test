package org.openxava.negocio.actions;

import org.openxava.actions.*;

public class OnChangeUnidadMedida extends OnChangePropertyBaseAction{
	@Override
	public void execute() throws Exception {
		if (getNewValue() != null){
			this.getView().setValueNotifying("cantidad", null);
		}		
	}
}
