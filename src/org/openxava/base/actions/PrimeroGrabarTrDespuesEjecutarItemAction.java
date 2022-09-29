package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.validators.*;

public abstract class PrimeroGrabarTrDespuesEjecutarItemAction extends CollectionElementViewBaseAction{

	@Override
	public void execute() throws Exception {
		// Primero se graba la transacción
		this.grabarTransaccion();
		this.ejecutarAccionItem();		
	}
	
	private void grabarTransaccion() throws Exception{
		// Lo que se hace es grabar las modificaciones de la view de la transacción.
		// Por un tema de performance, no se utiliza el método grabarTransaccion, 
		// ya que después al ejecutar una acción desde el item, la transacción se recalculará
		// Si la acción de item no hace nada, podría quedar sin recalcular. 
		// Igualmente, al confirmar, siempre graba y recalcula la transacción
		if (!this.getView().isEditable()){
			throw new ValidationException("no_modificar");
		}
		
		@SuppressWarnings("rawtypes")
		Map values = null;
		if (!getView().isKeyEditable()){			
			@SuppressWarnings("rawtypes")
			Map keyValues = getView().getKeyValues();				
			MapFacade.setValues(getModelName(), keyValues, getView().getValues());
			getView().clear(); 
			values = MapFacade.getValues(getModelName(), keyValues, getView().getMembersNamesWithHidden());
		}
		else{
			@SuppressWarnings("rawtypes")
			Map keyValues = MapFacade.createReturningKey(getModelName(), getView().getValues());					
			addMessage("entity_created", getModelName());
			getView().clear(); 
			values = MapFacade.getValues(getModelName(), keyValues, getView().getMembersNamesWithHidden());
		}
		getView().setKeyEditable(false);				
		getView().setValues(values); 
		resetDescriptionsCache();
		this.commit();
	}
	
	protected abstract void ejecutarAccionItem() throws Exception;		

}

