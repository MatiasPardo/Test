package org.openxava.inventario.model;

import java.math.*;

import org.openxava.negocio.model.*;
import org.openxava.validators.*;

public class TipoMovInvDesreserva implements ITipoMovimientoInventario{

	@Override
	public boolean requiereAtributosInventario() {
		return true;
	}

	@Override
	public void actualizarStockSinInventario(IItemMovimientoInventario movimiento, Kardex kardex, boolean stockObligatorio) {
		if (stockObligatorio){
			throw new ValidationException("No hay stock reservado para " + movimiento.getProducto().getCodigo());
		}
		else{
			if (movimiento.getProducto().usaAtributoInventario()){
				throw new ValidationException("No hay stock reservado para " + movimiento.getProducto().getCodigo());
			}
			else{
				Inventario inv = Inventario.crearInventario(movimiento);
				Cantidad cantidad = movimiento.cantidadStock();
				cantidad.setCantidad(cantidad.getCantidad().negate());
				inv.actualizarReservado(cantidad);
			}
		}
	}

	@Override
	public boolean actualizarStock(Inventario inv, IItemMovimientoInventario movimiento, Kardex kardex, boolean stockObligatorio) {
		BigDecimal cantidadConvertida = movimiento.cantidadStock().convertir(inv.getProducto().getUnidadMedida());
		if (stockObligatorio || movimiento.getProducto().usaAtributoInventario()){
			if (inv.getReservado().compareTo(cantidadConvertida) < 0){
				throw new ValidationException("No alcanza el stock reservado para " + inv.toString() + ": Reservado " + inv.getReservado().toString() + " solicitado " + movimiento.cantidadStock().toString());
			}
		}
		inv.setReservado(inv.getReservado().subtract(cantidadConvertida));
		return true;
	}	
}
