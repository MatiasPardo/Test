package org.openxava.contabilidad.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.contabilidad.model.*;
import org.openxava.model.*;
import org.openxava.util.*;

public class GenerarAsientoDesdePlantillaAction extends TabBaseAction implements IChainAction{
	
	boolean editarAsientoGenerado = false;
	
	@Override
	public void execute() throws Exception {
		if (getView().isKeyEditable()){
			addError("primero_grabar");
		}
		else{
			Asiento nuevoAsiento = null;
			
			@SuppressWarnings("rawtypes")
			Map [] keys = this.getSelectedKeys();
			if (keys.length > 0){				
				for(int i = 0; i < keys.length; i++){
					AsientoPlantilla plantilla = (AsientoPlantilla)MapFacade.findEntity(getView().getModelName(), keys[i]);
					nuevoAsiento = plantilla.crearAsiento();					
				}
				this.commit();
				
				if (keys.length == 1){
					this.editarAsientoGenerado = true;					
				}
				else{
					addMessage("Se generaron " + Integer.toString(keys.length) + " asientos.");
				}
			}
			else{
				@SuppressWarnings("rawtypes")
				Map key = getView().getKeyValues();
				
				if (key.containsKey("id")){
					if (!Is.emptyString((String)key.get("id"))){
						AsientoPlantilla plantilla = (AsientoPlantilla)MapFacade.findEntity(getView().getModelName(), key);
						nuevoAsiento = plantilla.crearAsiento();
						this.commit();
						this.editarAsientoGenerado = true;	
					}
				}
			}
			
			if (this.editarAsientoGenerado){
				// se muestra el asiento creado
				this.showNewView();
				Map<String, Object> key = new HashMap<String, Object>();
				key.put("id", nuevoAsiento.getId());
				getView().setModelName(nuevoAsiento.getClass().getSimpleName());
				getView().setValues(key);
				getView().findObject();                               
	            getView().setKeyEditable(false);
	            
	            String[] controladores = new String[2];
	            controladores[0] = "Transaccion";
	            controladores[1] = "Return";
	            this.setControllers(controladores);
			}
		}
	}

	@Override
	public String getNextAction() throws Exception {
		if (editarAsientoGenerado){
			return "Transaccion.editar";
		}
		else{
			return null;
		}
	}

}
