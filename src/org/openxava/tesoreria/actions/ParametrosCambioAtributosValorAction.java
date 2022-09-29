package org.openxava.tesoreria.actions;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class ParametrosCambioAtributosValorAction extends ViewBaseAction{
	
	private String accion;
	
	private String nombreVistaValor;

	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}

	public String getNombreVistaValor() {
		return nombreVistaValor;
	}

	public void setNombreVistaValor(String nombreVistaValor) {
		this.nombreVistaValor = nombreVistaValor;
	}
	
	@Override
	public void execute() throws Exception {
		if (Is.emptyString(this.getAccion())){
			throw new ValidationException("No esta asignado el nombre de la acción");
		}
		if (Is.emptyString(this.getNombreVistaValor())){
			throw new ValidationException("No esta asignado el nombre de la vista");
		}
		
		this.showDialog();
		getView().setTitle("Cambiar");
		getView().setModelName("Valor");
		getView().setViewName(this.getNombreVistaValor());
				
		this.addActions(this.getAccion(), "Dialog.cancel");		
	}
}
