package org.openxava.ventas.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.ventas.model.*;

public class GenerarCreditoVentaAction extends ViewBaseAction implements IChainAction{

	private boolean showCredito = false;
	
	@Override
	public void execute() throws Exception {
		if (!this.getView().isKeyEditable()){
			VentaElectronica factura = (VentaElectronica)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			VentaElectronica credito = factura.generarComprobanteReversion(null);
			if (credito != null){
				this.commit();
				
				this.showNewView();				
				Map<String, Object> key = new HashMap<String, Object>();
				key.put("id", credito.getId());
				getView().setModelName(credito.getClass().getSimpleName());
				getView().setValues(key);
				getView().findObject();                               
	            getView().setKeyEditable(false);
	                 
	            String[] controladores = new String[1];
	            controladores[0] = "VentaCAEGenerado";			            
	            this.setControllers(controladores);
	            
	            this.showCredito = true;
			}
			else{
				addWarning("No se pudo generar el crédito");
			}
		}
		else{
			addError("primero_grabar");
		}
	}

	@Override
	public String getNextAction() throws Exception {
		if (this.showCredito){
			return "Transaccion.editar";
		}
		else{
			return null;
		}
	}
}
