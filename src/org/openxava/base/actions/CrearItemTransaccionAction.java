package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.validators.ValidationException;

public class CrearItemTransaccionAction extends CreateNewElementInCollectionAction{
	public void execute() throws Exception {

		// Primero se graba la transacción
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
		
		super.execute();
		
		try{
			ItemTransaccion.itemTransaccionView(this.getCollectionElementView()).copiarValoresCabecera(this.getView(), this.getCollectionElementView(), null);
		}
		catch(Exception e){
			throw new ValidationException("Error al copiar valores de la cabecera a los items: " + e.toString());
		}
		
		try{
			// evaluar atributos 
			ObjetoNegocio bo = (ObjetoNegocio)this.getCollectionElementView().getMetaModel().getPOJOClass().newInstance();
			UtilERP.copyValuesViewToObject(this.getCollectionElementView(), bo);
			List<String> propiedadesOcultar = new LinkedList<String>();
	    	List<String> propiedadesVisibles = new LinkedList<String>();
	    	bo.propiedadesOcultas(propiedadesOcultar, propiedadesVisibles);
	    	for(String propiedad: propiedadesOcultar){
	    		getCollectionElementView().setHidden(propiedad, true);
	    	}
	    	for(String propiedad: propiedadesVisibles){
	    		getCollectionElementView().setHidden(propiedad, false);
	    	}
		}
		catch(Exception e){
		}
	}
}
