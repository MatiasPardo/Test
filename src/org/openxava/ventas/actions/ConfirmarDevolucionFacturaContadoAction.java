package org.openxava.ventas.actions;

import org.openxava.actions.IChainAction;
import org.openxava.base.actions.GrabarTransaccionAction;
import org.openxava.base.model.Estado;
import org.openxava.model.MapFacade;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.DevolucionFacturaContado;

public class ConfirmarDevolucionFacturaContadoAction extends GrabarTransaccionAction implements IChainAction{
	boolean ejecutarAccionesPosConfirmacion = false;
	
	public void execute() throws Exception {
		this.ejecutarAccionesPosConfirmacion = false;
		
		if (!Is.equal(this.getView().getValue("estado"), Estado.Procesando)){		
			super.execute();
		}
		
		if (this.getErrors().isEmpty()){
			try{
				// Primero se pasa a procesando para que luego se confirme la operación cuando este el crédito autorizado 
				DevolucionFacturaContado transaccion = (DevolucionFacturaContado) MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
				if (!Is.equal(transaccion.getEstado(), Estado.Procesando)){
					transaccion.cambiarEstadoAProcesando();				
					this.commit();
					transaccion = (DevolucionFacturaContado) MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
				}
				boolean autorizarCreditoPorDevolucion = true;
				if (transaccion.getCredito() != null){
					autorizarCreditoPorDevolucion = !transaccion.getCredito().cerrado();
				}
				if (autorizarCreditoPorDevolucion){
					transaccion.autorizarAfipCreditoPorDevolucion();
					this.commit();
					this.addMessage("Crédito por devolución confirmado");
				}								
				this.ejecutarAccionesPosConfirmacion = true;								
			}
			catch(Exception ex){
				this.rollback();
				if (ex instanceof ValidationException){
					addErrors(((ValidationException)ex).getErrors());
				}
				else{
					if (ex.getMessage() != null){
						addError(ex.getMessage());
					}
					else{
						addError(ex.toString());
					}
				}
				this.getMessages().removeAll();
			}
		}		
	}
	
	@Override
	public String getNextAction() throws Exception {
		if (this.ejecutarAccionesPosConfirmacion){
			return "DevolucionFacturaContado.FacturarDevolucion";
		}
		else{
			return "Transaccion.editar";
		}
	}
}

