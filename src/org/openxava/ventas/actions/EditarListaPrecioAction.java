package org.openxava.ventas.actions;

import org.openxava.base.actions.EditarObjetoNegocioAction;

public class EditarListaPrecioAction extends EditarObjetoNegocioAction{

	public void execute() throws Exception {
		super.execute();
		
		this.getView().getSubview("formatoImportacionCSV").getCollectionTab().setCustomizeAllowed(false);
		this.getView().getSubview("formatoImportacionCSV").getCollectionTab().restoreDefaultProperties();
	}
}
