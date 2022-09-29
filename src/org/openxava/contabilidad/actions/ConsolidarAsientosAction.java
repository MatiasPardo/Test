package org.openxava.contabilidad.actions;

import org.openxava.actions.*;
import org.openxava.contabilidad.model.*;
import org.openxava.model.*;

public class ConsolidarAsientosAction extends ViewBaseAction{

	@Override
	public void execute() throws Exception {
		if (!this.getView().isKeyEditable()){
			EjercicioContable ejercicio = (EjercicioContable)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			ejercicio.consolidarAsientos();
			this.commit();
			
			ejercicio = (EjercicioContable)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			ejercicio.renumerarAsientosConsolidados();
			this.commit();
			addMessage("ejecucion_OK");
		}
		else{
			this.addError("primero_grabar");
		}
		
	}

}
