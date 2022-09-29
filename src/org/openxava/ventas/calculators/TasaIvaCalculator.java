package org.openxava.ventas.calculators;

import java.math.*;

import static org.openxava.jpa.XPersistence.getManager;

import javax.persistence.*;

import org.openxava.calculators.*;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class TasaIvaCalculator implements ICalculator{
	
	private String productoID = "";
	
	public String getProductoID() {
		return productoID;
	}

	public void setProductoID(String productoID) {
		this.productoID = productoID;
	}

	public Object calculate() throws Exception {
		BigDecimal tasa = BigDecimal.ZERO;
		if (this.getProductoID() != null) { 
			Query query = (Query)getManager().createQuery("from Producto p where " +  		
			"p.id = :id");
			query.setParameter("id", this.productoID);
			Producto producto = (Producto) query.getSingleResult();	
			if (producto != null){
				tasa = producto.getTasaIva().getPorcentaje();
			}
		}	
		return tasa;
	}
}
