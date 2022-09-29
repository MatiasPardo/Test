package org.openxava.negocio.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;

public class VerObjetoGeneradorAction extends ViewBaseAction{

	@Override
	public void execute() throws Exception {
		if (getView().isKeyEditable()){
			addError("primero_grabar");
		}
		else{
			IGeneradoPor entidad = (IGeneradoPor)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			String id = entidad.generadaPorId();
			if (!Is.emptyString(id)){
				this.showNewView();
				Map<String, Object> key = new HashMap<String, Object>();
				key.put("id", id);
				getView().setModelName(entidad.generadaPorTipoEntidad());
				getView().setValues(key);
				getView().findObject();                               
	            getView().setKeyEditable(false);
	            getView().setEditable(false);
	            
	            String[] controladores = new String[1];
	            controladores[0] = "Return";
	            this.setControllers(controladores);
			}
			else{
				addWarning("no_generado_por");
			}
		}
	}

}
