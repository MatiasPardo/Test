package org.openxava.ventas.actions;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.ventas.model.*;

public class FacturacionVentaIntercompanyAction  extends ViewBaseAction{

	@Override
	public void execute() throws Exception {
		if (!this.getView().isKeyEditable()){
			if (!this.getView().isEditable()){
				VentaElectronica venta = (VentaElectronica)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
				if (venta.puedeGenerarTransaccionIntercompany()){		
					this.showDialog();
					getView().setTitle("Intercompany");
					getView().setModelName("ParametrosFacturacionVentaIntercompany");
		            String[] controladores = new String[1];
		            controladores[0] = "FacturacionIntercompany";
		            this.setControllers(controladores);
				}
				
			}
			else{
				addError("Estado inválido");
			}
		}
		else{
			addError("primero_grabar");
		}	
	}
}
