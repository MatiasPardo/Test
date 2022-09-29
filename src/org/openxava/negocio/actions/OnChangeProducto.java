package org.openxava.negocio.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

public class OnChangeProducto extends OnChangePropertyBaseAction{

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
				String idUnidadMedida = (String)this.getView().getValue("unidadMedida.id");
				if (Is.emptyString(idUnidadMedida)){	
					this.producto = (Producto)XPersistence.getManager().find(Producto.class, idProducto);
					Map<String, Object> values = new HashMap<String, Object>();
					values.put("id", this.producto.getUnidadMedida().getId());
					this.getView().setValue("unidadMedida", values);
				}
				
			}
		}		
		
	}
}
