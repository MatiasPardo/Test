package org.openxava.inventario.actions;

import org.openxava.base.actions.ModificarItemsTransaccionAction;
import org.openxava.base.model.Transaccion;
import org.openxava.inventario.model.LiquidacionConsignacion;

public class DevolverCompletaLiquidacionAction extends ModificarItemsTransaccionAction{

	@Override
	protected boolean modificarItemsTransaccion(Transaccion transaccion) {
		((LiquidacionConsignacion)transaccion).devolverTodos();
		return true;
	}
}
