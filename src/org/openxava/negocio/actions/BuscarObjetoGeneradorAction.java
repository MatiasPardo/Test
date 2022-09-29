package org.openxava.negocio.actions;

import java.util.*;

import javax.ejb.*;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.negocio.model.*;

public class BuscarObjetoGeneradorAction  extends ViewBaseAction{
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws Exception {
		try{
			Object entidad = MapFacade.findEntity(getView().getModelName(), this.getView().getKeyValues());
		
			Map<String, Object> clave = null;
			String modelo = null;
			if (entidad instanceof IGeneradoPor){
				clave = new HashMap<String, Object>();
				clave.put("id", ((IGeneradoPor)entidad).generadaPorId());
				modelo = ((IGeneradoPor)entidad).generadaPorTipoEntidad();
			}
			else{
				clave = getView().getKeyValues();
				modelo = getView().getModelName();
			}
			getView().setModelName(modelo);
			getView().setValues(clave);
			getView().findObject();
			getView().setKeyEditable(false);
			getView().setEditable(false);
		}
		catch(ObjectNotFoundException e){
			addError("No se pudo encontrar el objeto");
		}
	}
}
