package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;


public class CrearTransaccionAction extends CrearObjetoNegocioAction implements IHideActionsAction, IShowActionsAction{
	
	private Boolean numeroEditable = Boolean.FALSE; 
	
	public Boolean getNumeroEditable() {
		return numeroEditable;
	}

	public void setNumeroEditable(Boolean numeroEditable) {
		this.numeroEditable = numeroEditable;
	}

	private List<String> showActions = new LinkedList<String>();
	private List<String> hideActions = new LinkedList<String>();
	
	protected List<String> getAccionesVisibles(){
		return this.showActions;
	}
	
	protected List<String> getAccionesOcultas(){
		return this.hideActions;
	}
	
	@Override
	public void execute() throws Exception {
		getView().setViewName(this.getNombreVistaAlCrear());
		super.execute();
						
		if (this.getConfiguracionEntidad() != null){
			Empresa empresaDefault = this.getConfiguracionEntidad().empresaDefault();
			if (getView().getValue("empresa") != null){
								
				if (empresaDefault != null){
					Map<String, Object> values = new HashMap<String, Object>();
					values.put("id", empresaDefault.getId());
					getView().setValue("empresa", values);
				}
			}
			if (getView().getValue("moneda") != null){
				Moneda monedaDefault = this.getConfiguracionEntidad().getMoneda();
				if (monedaDefault != null){
					Map<String, Object> values = new HashMap<String, Object>();
				    values.put("id", monedaDefault.getId());
				    getView().setValue("moneda", values);
				}
			    
			}
			try{
				if (getView().getValue("subestado") != null){
					EstadoEntidad estadoInicial = this.getConfiguracionEntidad().getEstadoInicial();
					if (estadoInicial != null){
						Map<String, Object> values = new HashMap<String, Object>();
					    values.put("id", estadoInicial.getId());
					    getView().setValue("subestado", values);
					}
				}
			}
			catch(Exception e){				
			}
			
			if (empresaDefault != null){
				this.setNumeroEditable(!this.getConfiguracionEntidad().tieneNumerador(empresaDefault)); 
			}
		}
		
		getView().setEditable("numero", this.getNumeroEditable());
		
		showActions.clear();
		hideActions.clear();		
		Transaccion.accionesValidasAlCrear(getView().getModelName(), this.getConfiguracionEntidad(), showActions, hideActions);
	}
	
	@Override
	public String[] getActionsToShow() {
		String[] actions = new String[this.showActions.size()];
		int i = 0;
		for(String action: this.showActions){
			actions[i] = action;
			i++;
		}
		return actions;
	}

	@Override
	public String[] getActionsToHide() {
		String[] actions = new String[this.hideActions.size()];
		int i = 0;
		for(String action: this.hideActions){
			actions[i] = action;
			i++;
		}
		return actions;
	}
			
	@Override
	protected void propiedadesSoloLectura(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLectura(propiedadesSoloLectura, propiedadesEditables, configuracion);
		if (configuracion != null){
			if (configuracion.getCotizacionSoloLectura()){
				propiedadesSoloLectura.add("cotizacion");
			}
			else{
				propiedadesEditables.add("cotizacion");
			}
			
			if (configuracion.getEmpresaSoloLectura()){
				propiedadesSoloLectura.add("empresa");
			}
			else{
				propiedadesEditables.add("empresa");
			}
			
			if (configuracion.getMonedaSoloLectura()){
				propiedadesSoloLectura.add("moneda");
			}
			else{
				propiedadesEditables.add("moneda");
			}
		}
	}
	
	protected String getNombreVistaAlCrear(){
		return null;
	}
}