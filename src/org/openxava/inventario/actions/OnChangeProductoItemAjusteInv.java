package org.openxava.inventario.actions;

import java.util.*;

import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.actions.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

public class OnChangeProductoItemAjusteInv extends OnChangeProducto{
	@Override
	public void execute() throws Exception {
		super.execute();
		if (getNewValue() != null){
			String idProducto = (String)getNewValue();
			if (!Is.emptyString(idProducto)){
				String idDeposito = this.getView().getParent().getValueString("depositoOrigen.id");
				if (!Is.emptyString(idDeposito)){
					Producto producto = (Producto)XPersistence.getManager().find(Producto.class, idProducto);
					DespachoImportacion despacho = producto.ultimoDespacho(idDeposito);
					if (despacho != null){
						Map<String, Object> values = new HashMap<String, Object>();
						values.put("id", despacho.getId());
						values.put("codigo", despacho.getCodigo());
						this.getView().setValue("despacho", values);
					}
					else{
						this.getView().getSubview("despacho").clear();
					}
					Lote lote = producto.loteMasViejo(idDeposito);
					if (lote != null){
						Map<String, Object> values = new HashMap<String, Object>();
						values.put("id", lote.getId());
						values.put("codigo", lote.getCodigo());
						this.getView().setValue("lote", values);
					}
					else{
						this.getView().getSubview("lote").clear();
					}
				}
			}
		}
	}
}
