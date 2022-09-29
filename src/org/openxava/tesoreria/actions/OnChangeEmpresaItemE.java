package org.openxava.tesoreria.actions;

import org.openxava.actions.*;

public class OnChangeEmpresaItemE extends OnChangePropertyBaseAction{
	
	@Override
	public void execute() throws Exception {
		if (this.getNewValue() != null){
			if (getView().getParent() != null){
				int editingRow = getView().getCollectionEditingRow();		
				getView().getParent().setFocus("items." + editingRow + "." + "empresa.id");
			}
		}
		
	}
}
