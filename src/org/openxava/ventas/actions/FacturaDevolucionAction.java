package org.openxava.ventas.actions;

import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.IChainAction;
import org.openxava.actions.ViewBaseAction;
import org.openxava.model.MapFacade;
import org.openxava.ventas.model.DevolucionFacturaContado;
import org.openxava.ventas.model.FacturaVentaContado;

public class FacturaDevolucionAction extends ViewBaseAction implements IChainAction{
	@Override
	public void execute() throws Exception {
		DevolucionFacturaContado devolucion = (DevolucionFacturaContado)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
		FacturaVentaContado factura = devolucion.generarFacturaPorDevolucion();
		this.commit();
						
		this.showNewView();
		Map<String, Object> key = new HashMap<String, Object>();
		key.put("id", factura.getId());
		getView().setModelName(factura.getClass().getSimpleName());
		getView().setValues(key);
		getView().findObject();                               
	    getView().setKeyEditable(false);
	        
	    String[] controladores = new String[1];
	    controladores[0] = "FacturaVentaContadoGeneradaPorDevolucion";			            
	    this.setControllers(controladores);
	    
	    this.addInfo("Registrar Factura");		
	}
	
	@Override
	public String getNextAction() throws Exception {
		return "Transaccion.editar";		
	}
}
