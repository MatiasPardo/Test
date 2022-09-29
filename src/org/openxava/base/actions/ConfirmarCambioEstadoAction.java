package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;


public class ConfirmarCambioEstadoAction extends SaveAction implements IChainAction, INavigationAction{

	boolean ejecutarAccionesPosConfirmacion = false;
	
	@Override
	public void execute() throws Exception {
		this.ejecutarAccionesPosConfirmacion = false;
		
		// Se verifica que la entidad EjecucionCambioEstado sea válida y se grabe
		Messages errors = MapFacade.validate(getView().getModelName(), getView().getValues());
		if (errors.contains()) throw new ValidationException(errors);
		
		this.setRefreshAfter(false);
		this.setResetAfter(false);	
		super.execute();
		
		//getView().getMessages().removeAll();
		try{
			TransicionEstado transicion = (TransicionEstado) MapFacade.findEntity(getView().getSubview("transicion").getModelName(), getView().getSubview("transicion").getKeyValues());
			
			this.returnToPreviousView();
			
			try{
				ITransicionable objeto = (ITransicionable)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
				
				boolean estabaConfirmado = false;
				if ((objeto.getSubestado() != null) && (objeto.getSubestado().getEstadoTransaccional().equals(Estado.Confirmada))){
					estabaConfirmado = true;
				}
				
				transicion.transicionar(objeto);
				
				boolean confirmo = false;
				if (!estabaConfirmado){
					if ((objeto.getSubestado() != null) && (objeto.getSubestado().getEstadoTransaccional().equals(Estado.Confirmada))){
						confirmo = true;
					}
				}
				this.commit();
				
				if (confirmo){
					this.ejecutarAccionesPosConfirmacion = true;
				}
			}
			catch(Exception ex){
				addError(ex.getMessage());
				this.rollback();
			}	
		}
		catch(Exception ex){
			addError(ex.getMessage());
			this.rollback();
			this.returnToPreviousView();
		}	
	}

	@Override
	public String getNextAction() throws Exception {
		if (this.ejecutarAccionesPosConfirmacion){
			return "Transaccion.posConfirmacion";
		}
		else{
			return "Transaccion.editar";
		}
	}
	
	public String [] getNextControllers() {
		return PREVIOUS_CONTROLLERS;		
	}
	
	public String getCustomView() {
		return PREVIOUS_VIEW; 
	}
}
