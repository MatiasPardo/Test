package org.openxava.inventario.actions;

import java.util.*;

import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.actions.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

public class OnChangeProductoAsignaAtributosInventarioAction extends OnChangeProducto{
	
	@Override
	public void execute() throws Exception {
		super.execute();
		if (getNewValue() != null){
			Producto producto = this.getProducto();
			if (producto == null){
				String idProducto = (String)getNewValue();
				if (!Is.emptyString(idProducto)){
					producto = (Producto)XPersistence.getManager().find(Producto.class, idProducto);
				}
			}
			if (producto != null){
				DespachoImportacion despacho = producto.ultimoDespachoGeneral();
				if (despacho != null){
					Map<String, Object> values = new HashMap<String, Object>();
					values.put("id", despacho.getId());
					values.put("codigo", despacho.getCodigo());					
					this.getView().trySetValue("despacho", values);
				}
				else{
					try{
						this.getView().getSubview("despacho").clear();
					}
					catch(Exception e){						
					}
				}				
			}
		}
	}
}

