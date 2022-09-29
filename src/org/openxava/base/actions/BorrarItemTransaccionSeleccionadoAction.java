package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;

public class BorrarItemTransaccionSeleccionadoAction extends RemoveSelectedInCollectionAction implements IChainAction{
	
	@Override
	public void execute() throws Exception{
		super.execute();
		Transaccion tr = (Transaccion)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
		tr.grabarTransaccion();
		this.commit();
	}
	
	@Override
	public String getNextAction() throws Exception {
		return "Transaccion.editar";
	}
}