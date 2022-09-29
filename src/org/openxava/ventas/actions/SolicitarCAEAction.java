package org.openxava.ventas.actions;

import org.openxava.afip.model.*;
import org.openxava.base.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

public class SolicitarCAEAction extends GrabarTransaccionAction{

	@Override
	public void execute() throws Exception {
		Estado estado = (Estado)getView().getValue("estado");
		if ((estado == null) || (!estado.equals(Estado.Procesando))){
			super.execute();
			if (!this.getErrors().isEmpty()){
				return;
			}
			this.commit();
		}
				
		try{
			VentaElectronica venta = (VentaElectronica) MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
			FacturaElectronicaAfip facturadorAfip = new FacturaElectronicaAfip();
			facturadorAfip.autorizarComprobante(venta);
			if (facturadorAfip.erroresAutorizacion().isEmpty()){
				addMessage("Operación Autorizada");
				
				if (venta.tieneAccionesPosCommitAutorizaciones()){
					this.commit();
					venta = (VentaElectronica) MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
					venta.ejecutarAccionesPosCommitAutorizaciones();
				}
			}
			else{
				addError(facturadorAfip.erroresAutorizacion());
			}
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
	
	@Override
	public String getNextAction() throws Exception {
		return "Transaccion.editar";
	}
}
