package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;
import org.openxava.view.*;

public class OnChangeProductoItemTransaccion extends OnChangePropertyBaseAction{

	private Producto producto = null;
	
	protected Producto getProducto(){
		return this.producto;
	}
	
	@Override
	public void execute() throws Exception {
		this.producto = null;
		if (getNewValue() != null){
			String idProducto = (String)getNewValue();
			if (!Is.emptyString(idProducto)){
					
				this.producto = (Producto)XPersistence.getManager().find(Producto.class, idProducto);
				View viewUnidadMedida = this.getView().getSubview("unidadMedida");
				viewUnidadMedida.clear();
				Map<String, Object> values = new HashMap<String, Object>();
				values.put("__MODEL_NAME__", viewUnidadMedida.getModelName());
				values.put("id", this.producto.getUnidadMedida().getId());
				values.put("codigo", this.producto.getUnidadMedida().getCodigo());
				viewUnidadMedida.setValues(values);
			}
		}		
		
	}
}