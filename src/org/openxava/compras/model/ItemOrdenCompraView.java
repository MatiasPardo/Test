package org.openxava.compras.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.view.*;

public class ItemOrdenCompraView extends ItemTransaccionView{

	@Override
	public void copiarValoresCabecera(View cabecera, View items, ObjetoNegocio cabeceraPosCommit) {
		super.copiarValoresCabecera(cabecera, items, cabeceraPosCommit);
		
		try{
			BigDecimal porcentajeDescuento = ultimoDescuentoRegistrado(cabecera.getValueString("id"));
			items.setValue("porcentajeDescuento", porcentajeDescuento);
		}
		catch(Exception e){}
		
		try{
			items.setEditable("fechaRecepcion", (Boolean)cabecera.getValue("fechaRecepcionPorItem")); 
		}
		catch(Exception e){}
	}
	
	private BigDecimal ultimoDescuentoRegistrado(String id) {
		String sql = "select porcentajeDescuento from " + Esquema.concatenarEsquema("ItemOrdenCompra") + 
					" where ordenCompra_id = :id order by fechaCreacion desc limit 1";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("id", id);
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			return (BigDecimal)result.get(0);
		}
		else{
			return BigDecimal.ZERO;
		}
	}
}
