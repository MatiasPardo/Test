package org.openxava.ventas.actions;

import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.IChainAction;
import org.openxava.actions.ViewBaseAction;
import org.openxava.model.MapFacade;
import org.openxava.ventas.model.DevolucionFacturaContado;
import org.openxava.ventas.model.FacturaVentaContado;

public class DevolucionFacturaContadoAction extends ViewBaseAction implements IChainAction{
	
	private boolean showTransaccion = false;
	
	@Override
	public void execute() throws Exception {
		if (!this.getView().isKeyEditable()){
			FacturaVentaContado factura = (FacturaVentaContado)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			DevolucionFacturaContado devolucion = factura.generarDevolucion();
			if (devolucion != null){
				this.commit();
							
				this.showNewView();				
				Map<String, Object> key = new HashMap<String, Object>();
				key.put("id", devolucion.getId());
				getView().setModelName(devolucion.getClass().getSimpleName());
				getView().setValues(key);
				getView().findObject();                               
	            getView().setKeyEditable(false);
	                 
	            String[] controladores = new String[1];
	            controladores[0] = "DevolucionFacturaContado";
	            this.setControllers(controladores);
	            
	            this.showTransaccion = true;
			}
			else{
				addWarning("No se pudo generar la devolución");
			}
		}
		else{
			addError("primero_grabar");
		}
		
	}

	@Override
	public String getNextAction() throws Exception {
		if (this.showTransaccion){
			return "Transaccion.editar";
		}
		else{
			return null;
		}
	}
}

