package org.openxava.mercadolibre.actions;

import org.openxava.actions.*;
import org.openxava.mercadolibre.model.*;
import org.openxava.util.*;
import org.openxava.validators.ValidationException;

public class ParametrosActualizacionEcommerceAction extends TabBaseAction{
	
	private String accionActualizar;
	
	private String modeloParametros;
	
	private String vistaParametros;
	
	private String aModificar;
	
	private boolean objetosSeleccionadosObligatorios = false;


	public String getModeloParametros() {
		return modeloParametros;
	}

	public void setModeloParametros(String modeloParametros) {
		this.modeloParametros = modeloParametros;
	}

	public String getVistaParametros() {
		return vistaParametros;
	}

	public void setVistaParametros(String vistaParametros) {
		this.vistaParametros = vistaParametros;
	}

	public boolean getObjetosSeleccionadosObligatorios() {
		return objetosSeleccionadosObligatorios;
	}

	public void setObjetosSeleccionadosObligatorios(boolean objetosSeleccionadosObligatorios) {
		this.objetosSeleccionadosObligatorios = objetosSeleccionadosObligatorios;
	}
	
	@Override
	public void execute() throws Exception {
		if (Is.emptyString(this.getAccionActualizar())){
			throw new ValidationException("No esta asignado el nombre de la acción");
		}
		
		if (this.getObjetosSeleccionadosObligatorios()){
			if (this.getSelectedKeys() == null){
				throw new ValidationException("Debe seleccionar al menos un elemento de la lista");
			}
			else if (this.getSelectedKeys().length == 0){
				throw new ValidationException("Debe seleccionar al menos un elemento de la lista");
			}
			
		}
		
		
		//this.getRequest().setAttribute("aModificar", this.getaModificar());
		this.getRequest().getSession().setAttribute("aModificar", this.getaModificar());
		this.getRequest().getSession().setAttribute("seleccionados", this.getObjetosSeleccionadosObligatorios());
		this.showDialog();
		getView().setTitle("Actualizacion");
		Class<?> classParametros = ParametrosActualizacionEcommerce.class;
		if (!Is.emptyString(this.getModeloParametros())){
			classParametros = Class.forName(this.getModeloParametros());
		}
		getView().setModelName(classParametros.getSimpleName());
		if (!Is.emptyString(this.getVistaParametros())){
			getView().setViewName(this.getVistaParametros());
		}
	
		if (!this.getObjetosSeleccionadosObligatorios()){
			this.addActions(this.getAccionActualizar(), "Dialog.cancel");
		}
		else{
			this.addActions(this.getAccionActualizar(), "CancelarSeleccionDesdeLista.cancel");
		}
	}

	public String getaModificar() {
		return aModificar;
	}

	public void setaModificar(String aModificar) {
		this.aModificar = aModificar;
	}

	public String getAccionActualizar() {
		return accionActualizar;
	}

	public void setAccionActualizar(String accionActualizar) {
		this.accionActualizar = accionActualizar;
	}

}
