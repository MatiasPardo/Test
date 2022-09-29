package org.openxava.reclamos.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.reclamos.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class GenerarAsignacionReclamoAction extends TabBaseAction{
	
	private UsuarioReclamo asignarA;
	
	private Date fecha;
	
	public void execute() throws Exception {
		this.fecha = (Date)this.getView().getValue("fechaDeAtencion");
		String asignarA = this.getView().getValueString("asignarA.id");
		
		if (Is.emptyString(asignarA)){
			addError("Usuario no asignado");
		}
		if (fecha == null){
			addError("Fecha no asignada");
		}	
		if (this.getErrors().isEmpty()){
			this.asignarA = XPersistence.getManager().find(UsuarioReclamo.class, asignarA);
			
			AsignacionReclamo();
			this.closeDialog();
			getTab().deselectAll();
			if (this.getMessages().isEmpty() && this.getErrors().isEmpty()){
				addError("No a seleccionado ningun reclamo");
			}
		}
	}

	private void AsignacionReclamo() throws Exception{
		
		for(Map<?, ?> key: getTab().getSelectedKeys()){
			Reclamo reclamo = null;
			try{
				reclamo = (Reclamo)MapFacade.findEntity("Reclamo", key); 
				reclamo.setAsignadoA(this.asignarA);
				reclamo.setFechaServicio(this.fecha);
				if(!reclamo.confirmada()){
					this.confirmarReclamo(reclamo);
				}
				this.commit();
				addMessage("el reclamo numero: "+ reclamo.getNumero() + " se asgino correctamente a "+ reclamo.getAsignadoA().toString());
			}
			catch(Exception e){
				this.rollback();
				String error = e.getMessage();
				if (Is.emptyString(error)){
					error = e.toString();
				}
				addError("Error en " + reclamo.getNumero() + "; " + error); 
			}
		}
	}
	
	private void confirmarReclamo(Reclamo reclamo) {
		if(reclamo.getEstado() == null){
			reclamo.confirmarTransaccion();
		}else if(reclamo.getEstado().equals(Estado.Abierta) || reclamo.getEstado().equals(Estado.Borrador) ){
			reclamo.confirmarTransaccion();
		}else addError("El estado " + reclamo.getEstado() + " no es un es un estado valido");
	}

}
