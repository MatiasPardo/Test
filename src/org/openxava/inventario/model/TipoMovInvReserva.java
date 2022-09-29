package org.openxava.inventario.model;

import java.math.*;

import org.openxava.negocio.model.*;
import org.openxava.validators.*;

public class TipoMovInvReserva implements ITipoMovimientoInventario{

	@Override
	public boolean requiereAtributosInventario() {
		return false;
	}

	@Override
	public void actualizarStockSinInventario(IItemMovimientoInventario movimiento, Kardex kardex, boolean stockObligatorio) {
		if (stockObligatorio){
			throw new ValidationException("No hay stock para " + movimiento.getProducto().getCodigo());
		}
		else{
			if (movimiento.getProducto().usaAtributoInventario()){
				throw new ValidationException("No hay stock para " + movimiento.getProducto().getCodigo());
			}
			else{
				Inventario inv = Inventario.crearInventario(movimiento);
				inv.actualizarReservado(movimiento.cantidadStock());
			}
		}
	}

	@Override
	public boolean actualizarStock(Inventario inv, IItemMovimientoInventario movimiento, Kardex kardex, boolean stockObligatorio) {
		boolean actualizado = true;
		if (movimiento.getProducto().usaAtributoInventario()){
			if (Inventario.atributosInventarioCompleto(movimiento, null)){
				Cantidad cantidad = movimiento.cantidadStock();
				inv.actualizarReservado(cantidad);
				if (inv.getDisponible().compareTo(BigDecimal.ZERO) < 0){
					throw new ValidationException("No alcanza el stock disponible para reservar " + inv.toString() + ": Disponible " + inv.getDisponible().toString() + " solicitado " + movimiento.cantidadStock().toString());
				}
			}
			else{
				Cantidad cantidad = movimiento.cantidadStock();
				BigDecimal cantidadSolicitada = cantidad.convertir(inv.getProducto().getUnidadMedida());
				int compare = inv.getDisponible().compareTo(cantidadSolicitada);
				if (compare >= 0){
					inv.setReservado(inv.getReservado().add(cantidadSolicitada));
					inv.completarAtributosInventario(movimiento);
				}
				else if (inv.getDisponible().compareTo(BigDecimal.ZERO) > 0){
					IItemMovimientoInventario nuevoItem = inv.explotarItem(movimiento);
					Cantidad cantidadDisponible = new Cantidad();
					cantidadDisponible.setUnidadMedida(inv.getProducto().getUnidadMedida());
					cantidadDisponible.setCantidad(inv.getDisponible());
					Cantidad cantidadNuevoItem = new Cantidad();
					cantidadNuevoItem.setUnidadMedida(movimiento.getUnidadMedida());
					cantidadNuevoItem.setCantidad(cantidadDisponible.convertir(movimiento.getUnidadMedida()));
					
					nuevoItem.actualizarCantidadItem(cantidadNuevoItem);
					inv.completarAtributosInventario(nuevoItem);
					cantidad.setCantidad(cantidad.getCantidad().subtract(cantidadNuevoItem.getCantidad()));
					movimiento.actualizarCantidadItem(cantidad);
					
					inv.setReservado(inv.getReservado().add(inv.getDisponible()));
					actualizado = false;
					
					// Después de explotar un item y actualizar ambos (original y copia) se activa el evento de posactualizacion
					movimiento.posActualizarItemGeneradoPorInventario(nuevoItem);
				}
				else{
					actualizado = false;
				}
			}
		}
		else{
			BigDecimal disponible = inv.getDisponible();
			inv.actualizarReservado(movimiento.cantidadStock());
			if (stockObligatorio){
				if (inv.getDisponible().compareTo(BigDecimal.ZERO) < 0){
					throw new ValidationException("No alcanza el stock disponible para reservar " + inv.toString() + ": Disponible " + disponible.toString() + " solicitado " + movimiento.cantidadStock().toString());
				}
			}		
		}
		return actualizado;
	}



}
