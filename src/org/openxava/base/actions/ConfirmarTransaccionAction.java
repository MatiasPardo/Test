package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.validators.*;

public class ConfirmarTransaccionAction extends GrabarTransaccionAction implements IChainAction{
	boolean ejecutarAccionesPosConfirmacion = false;
	
	public void execute() throws Exception {
		this.ejecutarAccionesPosConfirmacion = false;
		
		super.execute();		
		
		if (this.getErrors().isEmpty()){
			try{
				Transaccion transaccion = (Transaccion) MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
				transaccion.confirmarTransaccion();
				this.commit();
				
				this.ejecutarAccionesPosConfirmacion = true;				
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
				this.getMessages().removeAll();
			}
		}
	}
	
	@Override
	public String getNextAction() throws Exception {
		if (this.ejecutarAccionesPosConfirmacion){
			return "Transaccion.posConfirmacion";
		}
		else{
			return "Transaccion.editar";
		}
	}		
}

