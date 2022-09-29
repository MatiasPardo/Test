package org.openxava.inventario.model;

import java.math.*;

import org.openxava.negocio.model.*;
import org.openxava.validators.*;

public class TipoMovInvEgreso implements ITipoMovimientoInventario{
	
	// por defecto, no hace falta especificar despacho, serie, lote, etc para egresar
	private boolean atributosInventarioRequerido = false;
	
	public TipoMovInvEgreso(){
		
	}
	
	public TipoMovInvEgreso(boolean atributosInvObligatorios){
		this.setAtributosInventarioRequerido(atributosInvObligatorios);
	}
	
	public boolean isAtributosInventarioRequerido() {
		return atributosInventarioRequerido;
	}

	public void setAtributosInventarioRequerido(boolean atributosInventarioRequerido) {
		this.atributosInventarioRequerido = atributosInventarioRequerido;
	}

	@Override
	public boolean requiereAtributosInventario() {
		return this.isAtributosInventarioRequerido();
	}

	@Override
	public void actualizarStockSinInventario(IItemMovimientoInventario movimiento, Kardex kardex, boolean stockObligatorio) {
		if (!stockObligatorio){
			if (movimiento.getProducto().usaAtributoInventario()){
				// los productos con atributos de fusión: como despacho, siempre tiene existencia obligatoria
				throw new ValidationException("No hay stock para " + movimiento.getProducto().getCodigo());
			}
			else{
				Inventario inv = Inventario.crearInventario(movimiento);
				Cantidad cantidad = movimiento.cantidadStock();
				cantidad.setCantidad(cantidad.getCantidad().negate());			
				inv.actualizarStock(cantidad);
				kardex.actualizarCantidad(cantidad);
			}
		}
		else{
			throw new ValidationException("No hay stock para " + movimiento.getProducto().getCodigo());
		}
	}

	@Override
	public boolean actualizarStock(Inventario inv, IItemMovimientoInventario movimiento, Kardex kardex, boolean stockObligatorio) {
		boolean actualizado = true;
		if (movimiento.getProducto().usaAtributoInventario()){
			if (Inventario.atributosInventarioCompleto(movimiento, null)){
				Cantidad cantidad = movimiento.cantidadStock();
				cantidad.setCantidad(cantidad.getCantidad().negate());
				BigDecimal disponible = inv.getDisponible();
				inv.actualizarStock(cantidad);
				if (inv.getDisponible().compareTo(BigDecimal.ZERO) < 0){
					throw new ValidationException("No alcanza el stock disponible para " + inv.toString() + ": Disponible " + disponible.toString() + inv.getProducto().getUnidadMedida().toString() + " solicitado " + movimiento.cantidadStock().toString());
				}
				kardex.actualizarCantidad(cantidad);
			}
			else{
				Cantidad cantidad = movimiento.cantidadStock();
				BigDecimal cantidadConvertida = cantidad.convertir(inv.getProducto().getUnidadMedida());
				int compare = inv.getDisponible().compareTo(cantidadConvertida);
				if (compare >= 0){
					inv.setStock(inv.getStock().subtract(cantidadConvertida));
					inv.completarAtributosInventario(movimiento);
					inv.completarAtributosInventario(kardex);
					
					// la cantidad en el kardex es en negativo
					cantidad.setCantidad(cantidad.getCantidad().negate());
					kardex.actualizarCantidad(cantidad);					
				}
				else if (inv.getDisponible().compareTo(BigDecimal.ZERO) > 0){
					IItemMovimientoInventario nuevoItem = inv.explotarItem(movimiento);
					
					Cantidad cantidadDisponible = new Cantidad();
					cantidadDisponible.setUnidadMedida(inv.getProducto().getUnidadMedida());
					cantidadDisponible.setCantidad(inv.getDisponible());
					Cantidad cantidadNuevoItem = new Cantidad();
					cantidadNuevoItem.setUnidadMedida(nuevoItem.getUnidadMedida());
					cantidadNuevoItem.setCantidad(cantidadDisponible.convertir(nuevoItem.getUnidadMedida()));
					nuevoItem.actualizarCantidadItem(cantidadNuevoItem);
					inv.completarAtributosInventario(nuevoItem);					
					inv.completarAtributosInventario(kardex);
					
					cantidad.setCantidad(cantidad.getCantidad().subtract(cantidadNuevoItem.getCantidad()));
					movimiento.actualizarCantidadItem(cantidad);
					inv.setStock(inv.getStock().subtract(inv.getDisponible()));
									
					// la cantidad en el kardex es en negativo
					cantidadNuevoItem.setCantidad(cantidadNuevoItem.getCantidad().negate());
					kardex.actualizarCantidad(cantidadNuevoItem);
					
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
			Cantidad cantidad = movimiento.cantidadStock();
			cantidad.setCantidad(cantidad.getCantidad().negate());
			BigDecimal disponible = inv.getDisponible();
			inv.actualizarStock(cantidad);	
			
			kardex.actualizarCantidad(cantidad);
			if (stockObligatorio){
				if (inv.getDisponible().compareTo(BigDecimal.ZERO) < 0){
					throw new ValidationException("No alcanza el stock disponible para " + inv.toString() + ": Disponible " + disponible.toString() + inv.getProducto().getUnidadMedida().toString() + " solicitado " + movimiento.cantidadStock().toString());
				}
			}
		}
		return actualizado;
	}	
}
