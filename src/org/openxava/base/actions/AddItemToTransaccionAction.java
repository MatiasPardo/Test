package org.openxava.base.actions;

import org.openxava.actions.*;

public class AddItemToTransaccionAction extends AddElementsToCollectionAction implements IChainAction{
	
	@Override
	public String getNextAction() throws Exception {
		if (this.getErrors().isEmpty()){
			return "Transaccion.editar";
		}
		else{
			return null;
		}
	}
}
