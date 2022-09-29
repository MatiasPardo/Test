package org.openxava.negocio.actions;

import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class ParametrosReporteAction extends TabBaseAction implements IChainAction{
	
	private String accionReporte;
	
	private String modeloParametros;
	
	private String vistaParametros;
	
	private boolean dialogo = true;
	
	private boolean objetosSeleccionadosObligatorios = false;
	
	private Integer cantidadObjetosDebeSeleccionar = null;
	
	public String getAccionReporte() {
		return accionReporte;
	}

	public void setAccionReporte(String accionReporte) {
		this.accionReporte = accionReporte;
	}

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
	
	public Integer getCantidadObjetosDebeSeleccionar() {
		return cantidadObjetosDebeSeleccionar;
	}

	public void setCantidadObjetosDebeSeleccionar(Integer cantidadObjetosDebeSeleccionar) {
		this.cantidadObjetosDebeSeleccionar = cantidadObjetosDebeSeleccionar;
	}

	@Override
	public void execute() throws Exception {
		if (Is.emptyString(this.getAccionReporte())){
			throw new ValidationException("No esta asignado el nombre de la acción del reporte");
		}
		
		if (this.getObjetosSeleccionadosObligatorios()){
			if (this.getSelectedKeys() == null){
				throw new ValidationException("Debe seleccionar al menos un elemento de la lista");
			}
			else if (this.getSelectedKeys().length == 0){
				throw new ValidationException("Debe seleccionar al menos un elemento de la lista");
			}
		}
		
		if (this.getCantidadObjetosDebeSeleccionar() != null){
			if (this.getSelectedKeys() != null && this.getSelectedKeys().length != this.getCantidadObjetosDebeSeleccionar()){
				throw new ValidationException("La cantidad de comprobantes que puede seleccionar: " + this.getCantidadObjetosDebeSeleccionar().toString());
			}
		}
		
		if(this.isDialogo()){
			this.showDialog();
			getView().setTitle("Reporte");
			Class<?> classParametros = ParametrosRangoFecha.class;
			if (!Is.emptyString(this.getModeloParametros())){
				classParametros = Class.forName(this.getModeloParametros());
			}
		
			getView().setModelName(classParametros.getSimpleName());
			if (!Is.emptyString(this.getVistaParametros())){
				getView().setViewName(this.getVistaParametros());
			}
			IParametrosReporte parametros = (IParametrosReporte)classParametros.newInstance();
			parametros.asignarValoresIniciales(this.getView(), this.getPreviousView(), this.getSelectedKeys());
			
			if (!this.getObjetosSeleccionadosObligatorios()){
				this.addActions(this.getAccionReporte(), "Dialog.cancel");
			}
			else{
				this.addActions(this.getAccionReporte(), "CancelarSeleccionDesdeLista.cancel");
			}
			
		}
		
				
	}

	public boolean isDialogo() {
		return dialogo;
	}

	public void setDialogo(boolean dialogo) {
		this.dialogo = dialogo;
	}

	@Override
	public String getNextAction() throws Exception {
		if(!this.isDialogo()){
			if(getTab().hasSelected()){
				this.getRequest().getSession().setAttribute("objetosSeleccionados", getTab().getSelectedKeys());
			}else{
				if (this.getView().getKeyValues().get("id") != null){
					Map<?, ?>[] keys = new HashMap<?, ?>[1];
					keys[0] = getView().getKeyValues();				
					this.getRequest().getSession().setAttribute("objetosSeleccionados", keys);
				}
			}
			return this.getAccionReporte();
		}else return null;
	}
}
