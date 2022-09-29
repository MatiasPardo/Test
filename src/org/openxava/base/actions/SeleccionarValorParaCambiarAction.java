package org.openxava.base.actions;

import java.util.Map;

import org.openxava.actions.ViewBaseAction;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.model.MapFacade;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.view.View;

public class SeleccionarValorParaCambiarAction extends ViewBaseAction{

	public final String NOMBREVISTACAMBIO = "Cambio";
	
	private String accion;
	
	private String nombreAtributo = null;
	
	private String nombreVista = null;
	
	private String nombreSubview = null;
	
	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}
	
	public String getNombreVista() {
		if (Is.emptyString(this.nombreVista)){
			if (!Is.emptyString(this.getNombreAtributo())){
				this.nombreVista = NOMBREVISTACAMBIO +  this.getNombreAtributo().substring(0, 1).toUpperCase() + this.getNombreAtributo().substring(1);
			}
			else{
				throw new ValidationException("No se pudo determinar el nombre de la vista");
			}
		}
		return nombreVista;		
	}

	public void setNombreVista(String nombreVista) {
		this.nombreVista = nombreVista;
	}
	
	@Override
	public void execute() throws Exception {
		if (!this.getView().isKeyEditable()){
			View vista = this.getView();
			if (!Is.emptyString(this.getNombreSubview())){
				vista = this.getView().getSubview(this.getNombreSubview());
			}
			ObjetoNegocio objetoNegocio = (ObjetoNegocio)MapFacade.findEntity(vista.getModelName(), vista.getKeyValues());
			
			objetoNegocio.permiteCambiarAtributo(this.getNombreVista());			
			if (Is.emptyString(this.getAccion())){
				throw new ValidationException("No esta asignado el nombre de la acción");
			}
			if (Is.emptyString(this.getNombreVista())){
				throw new ValidationException("No esta asignado el nombre de la vista");
			}
						
			String modelName = vista.getModelName();
			Map<?, ?> keyValue = vista.getKeyValues();
			
			this.showDialog();
			getView().setTitle("Cambiar");
			getView().setModelName(modelName);
			getView().setViewName(this.getNombreVista());
			
			// se copian los valores de la vista con el objeto actual de la base de datos, para que por defecto, tenga los mismos valores.			
			Map<?, ?> values = MapFacade.getValues(modelName, keyValue, vista.getMembersNamesWithHidden());
			getView().setValues(values);
			
			this.addActions(this.getAccion(), "Dialog.cancel");			
		}
		else{
			addError("primero_grabar");
		}
		
	}

	public String getNombreAtributo() {
		return nombreAtributo;
	}

	public void setNombreAtributo(String nombreAtributo) {
		this.nombreAtributo = nombreAtributo;
	}

	public String getNombreSubview() {
		return nombreSubview;
	}

	public void setNombreSubview(String nombreSubview) {
		this.nombreSubview = nombreSubview;
	}
}
