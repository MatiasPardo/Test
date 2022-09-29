package org.openxava.reportes.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;


public class EjecutarReportBaseMultipleTabAction extends TabBaseAction implements IChainAction{

	private Collection<Object> objetosSeleccionados = null;
	
	private String accionReporte = null;
	
	public String getAccionReporte() {
		return accionReporte;
	}

	public void setAccionReporte(String accionReporte) {
		this.accionReporte = accionReporte;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void execute() throws Exception {
		
		if (Is.emptyString(this.getAccionReporte())){
			throw new ValidationException("No esta asignado el parámetro accionReporte");
		}
		
		Map [] selectedOnes = getSelectedKeys();
		if (selectedOnes != null) {
			this.objetosSeleccionados = new LinkedList<Object>();
			for (int i = 0; i < selectedOnes.length; i++) {
				Map clave = selectedOnes[i];
				Object objeto = MapFacade.findEntity(this.getTab().getModelName(), clave);
				this.objetosSeleccionados.add(objeto);
			}
		}		
	}
	
	@Override
	public String getNextAction() throws Exception {
		if (this.getErrors().isEmpty()){
			this.getRequest().setAttribute("objetosSeleccionados", this.objetosSeleccionados);
			return this.getAccionReporte();
		}
		else{
			return null;
		}
	}
}


