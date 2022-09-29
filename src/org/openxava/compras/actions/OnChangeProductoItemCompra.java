package org.openxava.compras.actions;

import java.util.HashMap;
import java.util.Map;

import org.openxava.base.actions.*;
import org.openxava.impuestos.model.TasaImpuesto;
import org.openxava.view.View;

public class OnChangeProductoItemCompra extends OnChangeProductoItemTransaccion{
	
	@Override
	public void execute() throws Exception {
		super.execute();
		
		if (this.getProducto() != null){
			TasaImpuesto tasa = this.getProducto().getTasaIva();
			View viewTasa = this.getView().getSubview("alicuotaIva");
			viewTasa.clear();
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("codigo", tasa.getCodigo());
			viewTasa.setValues(values);
		}
	}
}
