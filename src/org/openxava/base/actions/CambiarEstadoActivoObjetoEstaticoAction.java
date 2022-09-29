package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;

public class CambiarEstadoActivoObjetoEstaticoAction extends ViewBaseAction implements IChainAction{

	@Override
	public void execute() throws Exception {
		if (!getView().isKeyEditable()){
			ObjetoEstatico objeto = (ObjetoEstatico)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
			objeto.cambiarEstado();
			this.commit();
			if (objeto.getActivo()){
				addMessage("Activo");
			}
			else{
				addWarning("Inactivo");
			}
		}
		else{
			addError("No se puede ejecutar en una entidad nueva");
		}
		
	}

	@Override
	public String getNextAction() throws Exception {
		return "ObjetoNegocio.editar";
	}

}
