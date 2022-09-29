package org.openxava.ventas.model;

import java.math.*;

import org.openxava.base.model.*;
import org.openxava.negocio.model.*;

public class ItemPendienteFacturaVentaPorCantidadProxy extends ItemPendientePorCantidadProxy{

	private EstadisticaPedidoVenta itemPedidoVenta;
	
	private Pendiente pendiente = null;
	
	public EstadisticaPedidoVenta getItemPedidoVenta() {
		return itemPedidoVenta;
	}
	
	public void setItemPedidoVenta(EstadisticaPedidoVenta itemPedidoVenta) {
		this.itemPedidoVenta = itemPedidoVenta;
	}
	
	@Override
	public BigDecimal getCantidadPendiente() {
		return getItemPedidoVenta().getPendientePreparacion();
	}
	
	@Override
	public void setCantidadPendiente(BigDecimal cantidadPendiente){		
		this.getItemPedidoVenta().setPendientePreparacion(cantidadPendiente);
	}
	
	@Override
	public BigDecimal getCantidadOriginal() {
		return getItemPedidoVenta().getCantidad();
	}

	@Override
	public Pendiente getPendiente() {
		if (this.pendiente == null){			
			this.pendiente = getItemPedidoVenta().getVenta().buscarPendienteGeneradoPorTr(PendienteFacturaVenta.class.getSimpleName());
		}
		return this.pendiente;
	}
	
	@Override
	public String toString(){
		return this.getItemPedidoVenta().getProducto().getCodigo(); 
	}

	@Override
	public ObjetoNegocio getItem() {
		return this.getItemPedidoVenta();
	}

	@Override
	public UnidadMedida getUnidadMedida() {
		return this.getItemPedidoVenta().getUnidadMedida();
	}

}
