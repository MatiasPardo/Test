package org.openxava.contabilidad.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class EnumerarLibroDiarioAction extends ViewBaseAction{

	@Override
	public void execute() throws Exception {
		Integer numero = (Integer)this.getView().getValue("primerNumero");
		if (numero == null){
			throw new ValidationException("Número no asignado");
		}
		if (Is.emptyString(this.getView().getValueString("empresa.id"))){
			throw new ValidationException("Empresa no asignada");
		}
		
		Empresa empresa = XPersistence.getManager().find(Empresa.class, this.getView().getValue("empresa.id"));
		IParametrosReporteContable objeto = (IParametrosReporteContable)MapFacade.findEntity(getPreviousView().getModelName(), getPreviousView().getKeyValues());
		objeto.ejercicio().enumerarLibroDiario(numero, empresa);
		
		this.addMessage("ejecucion_OK");
		this.closeDialog();
	}

}
