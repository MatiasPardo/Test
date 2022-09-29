package org.openxava.base.actions;

import java.util.*;

import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class CambiarEstadoAction extends GrabarTransaccionAction{

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute() throws Exception {
		if (this.getView().isEditable()){
			super.execute();
			this.commit();
		}
		
		if (getView().getErrors().isEmpty()){	
			String tipoEntidad = getView().getModelName();
			ObjetoNegocio objetoNegocio = (ObjetoNegocio)MapFacade.findEntity(tipoEntidad, getView().getKeyValues());
			
			String idEntidad = objetoNegocio.getId();
			EstadoEntidad estado = ((ITransicionable)objetoNegocio).getSubestado();
			
			String subEstadoID = "";
			if (estado != null) subEstadoID = estado.getId();
			TransicionEstado transicionDefault = TransicionEstado.buscarTransicionDefault(tipoEntidad, subEstadoID, Users.getCurrent());
			
			if (transicionDefault == null){
				throw new ValidationException("Usuario no autorizado o no hay transiciones definidas");
			}
			
			showNewView();
			getView().setModelName("EjecucionCambioEstado");
			getView().setViewName("transicion");
			getView().setValue("tipoEntidad", tipoEntidad);
			getView().setValue("idEntidad", idEntidad);
			
			if (estado != null){
				Map values = new HashMap();
			    values.put("__MODEL_NAME__", "EstadoEntidad");
			    values.put("id", estado.getId());
			    values.put("nombre", estado.getNombre());
			    getView().getSubview("estadoOriginal").setValues(values);
			}
			
		    if (transicionDefault != null){
		    	Map values = new HashMap();
		    	values.put("__MODEL_NAME__", "TransicionEstado");
			    values.put("id", transicionDefault.getId());
			    values.put("nombre", transicionDefault.getNombre());
			    getView().getSubview("transicion").setValues(values);
		    }
		    
			setControllers("EjecucionCambioEstado");
		}
	}
	
	@Override
	public String getNextAction() throws Exception {
		return null;
	}

}
