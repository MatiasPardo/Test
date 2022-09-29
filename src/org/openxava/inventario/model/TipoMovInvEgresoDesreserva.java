package org.openxava.inventario.model;

import java.math.*;

import org.openxava.negocio.model.*;
import org.openxava.validators.*;

public class TipoMovInvEgresoDesreserva implements ITipoMovimientoInventario{

	@Override
	public boolean requiereAtributosInventario() {
		return true;
	}

	@Override
	public void actualizarStockSinInventario(IItemMovimientoInventario movimiento, Kardex kardex, boolean stockObligatorio) {
		if (stockObligatorio || movimiento.getProducto().usaAtributoInventario()){
			throw new ValidationException("No hay stock reservado para " + movimiento.getProducto().getCodigo());
		}
		else{
			Inventario inv = Inventario.crearInventario(movimiento);
			Cantidad cantidad = movimiento.cantidadStock();
			cantidad.setCantidad(cantidad.getCantidad().negate());
			BigDecimal cantidadConvertida = cantidad.convertir(inv.getProducto().getUnidadMedida());
			
			inv.setStock(cantidadConvertida);
			inv.setReservado(cantidadConvertida);
			
			kardex.setCantidad(cantidadConvertida);
			kardex.setUnidadMedidaOperacion(movimiento.getUnidadMedida());
			kardex.setCantidadOperacion(cantidad.getCantidad());
		}		
	}

	@Override
	public boolean actualizarStock(Inventario inv, IItemMovimientoInventario movimiento, Kardex kardex, boolean stockObligatorio) {
		Cantidad cantidad = movimiento.cantidadStock();
		BigDecimal cantidadConvertida = cantidad.convertir(inv.getProducto().getUnidadMedida());
		if (stockObligatorio || movimiento.getProducto().usaAtributoInventario()){
			if (inv.getReservado().compareTo(cantidadConvertida) < 0){
				throw new ValidationException("No hay stock reservado suficiente: Disponible " + inv.toString() + " solicitado " + movimiento.cantidadStock().toString());
			}
		}
		
		inv.setReservado(inv.getReservado().subtract(cantidadConvertida));
		inv.setStock(inv.getStock().subtract(cantidadConvertida));
		kardex.setCantidad(cantidadConvertida.negate());
		kardex.setUnidadMedidaOperacion(movimiento.getUnidadMedida());
		kardex.setCantidadOperacion(cantidad.getCantidad().negate());
		return true;
	}

	
}
