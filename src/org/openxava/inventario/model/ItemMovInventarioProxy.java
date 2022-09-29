package org.openxava.inventario.model;

import org.openxava.negocio.model.*;
import org.openxava.ventas.model.*;

public abstract class ItemMovInventarioProxy implements IItemMovimientoInventario{
	
	private Deposito deposito;
	
	private Cantidad cantidad;
	
	IItemMovimientoInventario item;
	
	public ItemMovInventarioProxy(IItemMovimientoInventario item){
		this.item = item;
		this.setDeposito(item.getDeposito());
		this.setCantidad(item.cantidadStock());
	}
	
	public Cantidad getCantidad() {
		return cantidad;
	}
	
	public void setCantidad(Cantidad cantidad) {
		this.cantidad = cantidad;
	}

	@Override
	public Producto getProducto() {
		return this.item.getProducto();
	}

	@Override
	public DespachoImportacion getDespacho() {
		return this.item.getDespacho();
	}

	@Override
	public void setDespacho(DespachoImportacion despacho) {
		this.item.setDespacho(despacho);		
	}

	@Override
	public Lote getLote() {
		return this.item.getLote();
	}

	@Override
	public void setLote(Lote lote) {
		this.item.setLote(lote);		
	}
	
	@Override
	public abstract ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion); 
		
	@Override
	public Deposito getDeposito() {
		return this.deposito;
	}

	public void setDeposito(Deposito deposito){
		this.deposito = deposito;
	}
	
	@Override
	public Cantidad cantidadStock() {
		return this.getCantidad();
	}

	@Override
	public void actualizarCantidadItem(Cantidad cantidad) {
		this.item.actualizarCantidadItem(cantidad);
	}

	@Override
	public UnidadMedida getUnidadMedida() {
		return this.item.getUnidadMedida();
	}
	
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem){
		this.item.crearItemGeneradoPorInventario(nuevoItem);
	}
	
	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {
		this.item.posActualizarItemGeneradoPorInventario(nuevoItem);
	}
}
