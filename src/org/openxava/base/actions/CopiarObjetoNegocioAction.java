package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.validators.*;

public class CopiarObjetoNegocioAction extends ViewBaseAction implements IChainAction{

	@Override
	public void execute() throws Exception {
		if (!getView().isKeyEditable()){
			ObjetoNegocio objetoNegocio = (ObjetoNegocio)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			ObjetoNegocio copia = objetoNegocio.generarCopia();
			this.commit();
			
			getView().reset();
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("id", copia.getId());
			getView().setValues(values);
		}
		else{
			throw new ValidationException("Primero debe grabar");
		}
		
	}

	@Override
	public String getNextAction() throws Exception {
		return this.getEnvironment().getValue("XAVA_SEARCH_ACTION");
	}

}
