package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;

public class CancelarTransaccionAction extends ViewBaseAction implements IChainAction{
	
	@Override
	public void execute() throws Exception {
		
		try{
			Transaccion transaccion = (Transaccion) MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
			transaccion.cancelarTransaccion();
			this.commit();
			addMessage("Operación cancelada");
		}
		catch(Exception ex){
			addError(ex.getMessage());
			this.rollback();
		}
	}
	
	@Override
	public String getNextAction() throws Exception {
		return "Transaccion.editar";
	}
}
