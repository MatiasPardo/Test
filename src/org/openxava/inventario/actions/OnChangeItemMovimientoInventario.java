package org.openxava.inventario.actions;

import java.math.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

public class OnChangeItemMovimientoInventario extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if(this.getNewValue() != null){
			if (this.getChangedProperty().equalsIgnoreCase("producto")){
				String idUnidadMedida = (String)this.getView().getValue("unidadMedida.id");
				if (Is.emptyString(idUnidadMedida)){	
					Producto producto = (Producto)XPersistence.getManager().find(Producto.class, this.getView().getValueString("producto.id"));
					Map<String, Object> values = new HashMap<String, Object>();
					values.put("id", producto.getUnidadMedida().getId());
					this.getView().setValue("unidadMedida", values);
				}
			}
			
			String idDeposito = (String)this.getView().getParent().getValue("deposito.id");
			String idProducto = (String)this.getView().getValue("producto.id");
			String idDespacho = (String)this.getView().getValue("despacho.id");
			String idLote = (String)this.getView().getValue("lote.id");
			HashMap<String, String> atributosInventario = new HashMap<String, String>();
			atributosInventario.put("despacho_id", idDespacho);
			atributosInventario.put("lote_id", idLote);
			BigDecimal stock = Inventario.buscarStockPorId(idDeposito, idProducto, atributosInventario);
			this.getView().setValue("stock", stock);
		}
	}
}
