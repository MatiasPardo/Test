package org.openxava.ventas.actions;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.ventas.model.*;

public class ReGenerarReciboCobranzaFacturaContadoAction extends ViewBaseAction implements IChainAction{

	@Override
	public void execute() throws Exception {
		if (!getView().isKeyEditable() && (!getView().isEditable())){
			FacturaVentaContado factura = (FacturaVentaContado)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
			if (factura.tieneOperacionQueRevierte()){
				this.addError("El comprobante tiene un crédito asociado, no se puede registrar la cobranza");
			}			
		}		
	}

	@Override
	public String getNextAction() throws Exception {
		if (this.getErrors().isEmpty()){
			return "FacturaVentaContado.GenerarReciboCobranza";
		}
		else{
			return null;
		}
	}	
}

