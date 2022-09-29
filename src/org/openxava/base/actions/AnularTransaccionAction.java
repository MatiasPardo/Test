package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.validators.*;

public class AnularTransaccionAction extends ViewBaseAction implements IChainAction{
	
	@Override
	public void execute() throws Exception {
		
		try{
			Transaccion transaccion = (Transaccion) MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
			transaccion.anularTransaccion();
			this.commit();
			addMessage("Operación anulada");
		}
		catch(Exception ex){
			this.rollback();
			if (ex instanceof ValidationException){
				addErrors(((ValidationException)ex).getErrors());
			}
			else{
				if (ex.getMessage() != null){
					addError(ex.getMessage());
				}
				else{
					addError(ex.toString());
				}
			}
		}
	}
	
	@Override
	public String getNextAction() throws Exception {
		return "Transaccion.editar";
	}
}
