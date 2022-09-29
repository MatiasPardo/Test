package org.openxava.negocio.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;

public class VerObjetoGeneradorItemColeccionAction extends ViewElementInCollectionAction{
	
	@SuppressWarnings("rawtypes")
	public void execute() throws Exception {	
		// se redefine todo el comportamiento, no se llama a la superclase
		Map keys = (Map) getCollectionElementView().getCollectionTab().getTableModel().getObjectAt(this.getRow());
		if (keys != null){
			IGeneradoPor entidad = (IGeneradoPor)MapFacade.findEntity(getCollectionElementView().getCollectionTab().getModelName(), keys);
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
		else{
			throw new XavaException("only_list_collection_for_aggregates");
		}
	}
}
