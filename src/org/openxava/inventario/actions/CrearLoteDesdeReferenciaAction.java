package org.openxava.inventario.actions;

import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.NewAction;
import org.openxava.view.View;

public class CrearLoteDesdeReferenciaAction extends NewAction{
	
	public void execute() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		try{
			View viewProducto = this.getPreviousView().getSubview("producto");
			values.put("id", viewProducto.getValue("id"));
			values.put("codigo", viewProducto.getValue("codigo"));
			values.put("nombre", viewProducto.getValue("nombre"));
		}
		catch(Exception e){
		}
		
		super.execute();
		
		if (!values.isEmpty()){
			this.getView().trySetValue("producto", values);
		}
	}
}
