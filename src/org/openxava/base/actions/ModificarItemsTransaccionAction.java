package org.openxava.base.actions;

import org.openxava.actions.CollectionElementViewBaseAction;
import org.openxava.base.model.Transaccion;
import org.openxava.model.MapFacade;

public abstract class ModificarItemsTransaccionAction extends CollectionElementViewBaseAction{

	@Override
	public void execute() throws Exception {		
		if (this.getParentView().isKeyEditable()){
			this.addError("primero_grabar");
		}
		else if (!this.getParentView().isEditable()){
			this.addError("no_modificar");				
		}
		else{
			Transaccion transaccion = (Transaccion)MapFacade.findEntity(this.getParentView().getModelName(), this.getParentView().getKeyValues());
			if (!transaccion.soloLectura()){
				if (this.modificarItemsTransaccion(transaccion)){
					this.getParentView().refreshCollections();
					this.addMessage("items_modificados");
				}
			}
			else{
				this.addError("no_modificar");
			}
		}
	}
	
	protected abstract boolean modificarItemsTransaccion(Transaccion transaccion);
}

