package org.openxava.base.actions;

import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.validators.*;

public abstract class ModificarYGrabarTransaccionAction extends GrabarTransaccionAction{
	
	protected abstract void modificarTransaccion(Transaccion transaccion);
		
	public void execute() throws Exception {
		if (!this.getView().isEditable()){
			addMessage("No se puede modificar");
		}
		else{
			super.execute();
			
			if (this.getErrors().isEmpty()){
				try{
					Transaccion transaccion = (Transaccion)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
					if (!transaccion.soloLectura()){
						this.modificarTransaccion(transaccion);
						this.commit();
					}
					else{
						addMessage("No se puede modificar");
					}
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
		}
	}

}
