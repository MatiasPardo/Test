package org.openxava.compras.validators;

import org.openxava.negocio.validators.ActivoObjetoEstaticoEnItemTransaccionValidator;
import org.openxava.util.Messages;
import org.openxava.ventas.model.Producto;

@SuppressWarnings("serial")
public class ProductoItemCompraValidator extends ActivoObjetoEstaticoEnItemTransaccionValidator{

	@Override
	protected void validarItemTransaccion(Messages errores) {
		super.validarItemTransaccion(errores);
		
		if (this.getObjetoEstatico() != null){
			Producto producto = (Producto)this.getObjetoEstatico();
			if (!producto.getCompras()){
				errores.add(producto.getCodigo() + " no esta habilitado para la compra");
			}
		}		
	}
	
}
