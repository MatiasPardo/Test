package org.openxava.base.actions;

import org.openxava.base.model.IAccionCancelacionPendientes;
import org.openxava.base.model.Transaccion;
import org.openxava.validators.ValidationException;

public class CancelarItemsPendientesSeleccionadosAction extends ProcesarItemPendienteSeleccionadosAction{
	
	@Override
	protected void posGenerarTransacciones() {
		super.posGenerarTransacciones();
		
		for(Transaccion tr: this.getTransacciones()){
			if (tr instanceof IAccionCancelacionPendientes){
				((IAccionCancelacionPendientes)tr).prepararParaCancelarPendiente();
			}
			else{
				throw new ValidationException(tr.getDescripcion() + " no implementa interfaz para cancelar pendientes" );
			}
		}
	}
	
}
