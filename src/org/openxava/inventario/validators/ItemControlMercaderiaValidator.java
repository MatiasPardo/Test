package org.openxava.inventario.validators;

import java.math.BigDecimal;

import org.openxava.base.validators.ItemTransaccionValidator;
import org.openxava.inventario.model.ItemRemito;
import org.openxava.util.Messages;

@SuppressWarnings("serial")
public class ItemControlMercaderiaValidator extends ItemTransaccionValidator{

	private BigDecimal cantidad;
	
	private ItemRemito itemRemito;
	
	@Override
	protected void validarItemTransaccion(Messages errores) {
		if (this.getItemRemito() != null){
			if (this.getCantidad().compareTo(this.getItemRemito().getPendienteLiquidacion()) > 0){
				errores.add("No puede superar la cantidad remitida pendiente de controlar: " + this.getItemRemito().getPendienteLiquidacion().toString());
			}
		}
		else{
			errores.add("No tiene asociado un remito");
		}
		
	}

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}



	public ItemRemito getItemRemito() {
		return itemRemito;
	}

	public void setItemRemito(ItemRemito itemRemito) {
		this.itemRemito = itemRemito;
	}
}
