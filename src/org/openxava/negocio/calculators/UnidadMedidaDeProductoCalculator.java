package org.openxava.negocio.calculators;

import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class UnidadMedidaDeProductoCalculator implements ICalculator{

	private String idProducto;
		
	public String getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(String idProducto) {
		this.idProducto = idProducto;
	}

	private Producto buscarProducto(){
		if (!Is.empty(this.idProducto)){
			return (Producto)XPersistence.getManager().find(Producto.class, idProducto);
		}
		else{
			return null;
		}
	}
	
	@Override
	public Object calculate() throws Exception {
		Producto producto = this.buscarProducto();
		if (producto != null){
			return producto.getUnidadesMedida();
		}
		else{
			return null;
		}
	}
}
