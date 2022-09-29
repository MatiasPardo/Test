package org.openxava.inventario.validators;

import org.openxava.util.Messages;
import org.openxava.validators.IValidator;
import org.openxava.ventas.model.Producto;

@SuppressWarnings("serial")
public class LoteValidator implements IValidator{
	
	private Producto producto;
		
	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	@Override
	public void validate(Messages errors) throws Exception {
		if (!producto.getLote()){
			errors.add("El producto no utiliza lote");
		}
	}
}
