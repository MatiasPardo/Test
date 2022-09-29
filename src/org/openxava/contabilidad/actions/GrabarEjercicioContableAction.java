package org.openxava.contabilidad.actions;

import org.openxava.base.actions.*;
import org.openxava.contabilidad.model.*;
import org.openxava.model.*;

public class GrabarEjercicioContableAction extends GrabarObjetoNegocioAction{
	
	public void execute() throws Exception {
		super.execute();
		
		EjercicioContable ejercicio = (EjercicioContable)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
		ejercicio.generarPeriodos();
	}
}
