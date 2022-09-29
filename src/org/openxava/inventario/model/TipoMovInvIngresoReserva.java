package org.openxava.inventario.model;

import java.math.*;

import org.openxava.negocio.model.*;
import org.openxava.validators.ValidationException;

public class TipoMovInvIngresoReserva implements ITipoMovimientoInventario{

	@Override
	public boolean requiereAtributosInventario() {
		return true;
	}

	@Override
	public void actualizarStockSinInventario(IItemMovimientoInventario movimiento, Kardex kardex, boolean stockObligatorio) {
		Inventario inv = Inventario.crearInventario(movimiento);
		
		Cantidad cantidad = movimiento.cantidadStock();
		BigDecimal cantidadConvertida = cantidad.convertir(inv.getProducto().getUnidadMedida());
		
		inv.setStock(cantidadConvertida);
		inv.setReservado(cantidadConvertida);
		kardex.setCantidad(cantidadConvertida);
		kardex.setCantidadOperacion(cantidad.getCantidad());
		kardex.setUnidadMedidaOperacion(movimiento.getUnidadMedida());
		
		this.verificarLote(inv);
	}

	@Override
	public boolean actualizarStock(Inventario inv, IItemMovimientoInventario movimiento, Kardex kardex, boolean stockObligatorio) {
		Cantidad cantidad = movimiento.cantidadStock();
		BigDecimal cantidadConvertida = cantidad.convertir(inv.getProducto().getUnidadMedida());
		
		inv.setStock(inv.getStock().add(cantidadConvertida));
		inv.setReservado(inv.getReservado().add(cantidadConvertida));
		kardex.setCantidad(cantidadConvertida);
		kardex.setCantidadOperacion(cantidad.getCantidad());
		kardex.setUnidadMedidaOperacion(movimiento.getUnidadMedida());
		
		this.verificarLote(inv);
		
		return true;
	}	
	
	private void verificarLote(Inventario stock){
		if (stock.getProducto().getLote()){
			Lote lote = stock.getLote();
			if (lote != null){
				if (!lote.getProducto().equals(stock.getProducto())){
					throw new ValidationException("El lote " + lote.getCodigo() + " esta asociada al producto " + lote.getProducto().toString() + ". No puede ser ingresada con el producto " + stock.getProducto().toString());
				}				
			}
			else{
				throw new ValidationException(stock.getProducto().toString() + ": lote no asignado");
			}
		}
		else if (stock.getLote() != null){
			throw new ValidationException(stock.getProducto().toString() + " no utiliza lote");
		}
	}
}

