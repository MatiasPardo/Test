package org.openxava.compras.model;

import java.math.BigDecimal;

import org.openxava.base.model.ItemPendientePorCantidadProxy;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.base.model.Pendiente;
import org.openxava.inventario.model.ItemRecepcionMercaderia;
import org.openxava.negocio.model.UnidadMedida;

public class ItemPendienteFacturaCompraProxy extends ItemPendientePorCantidadProxy{

	private ItemRecepcionMercaderia itemRecepcion;
	
	public ItemRecepcionMercaderia getItemRecepcion() {
		return itemRecepcion;
	}

	public void setItemRecepcion(ItemRecepcionMercaderia itemRecepcion) {
		this.itemRecepcion = itemRecepcion;
	}

	@Override
	public UnidadMedida getUnidadMedida() {
		return this.getItemRecepcion().getUnidadMedida();
	}

	@Override
	public ObjetoNegocio getItem() {
		return this.getItemRecepcion();
	}

	@Override
	public BigDecimal getCantidadPendiente() {
		return this.getItemRecepcion().getPendienteFacturacion();
	}

	@Override
	public void setCantidadPendiente(BigDecimal cantidadPendiente) {
		this.getItemRecepcion().setPendienteFacturacion(cantidadPendiente);		
	}

	@Override
	public BigDecimal getCantidadOriginal() {
		return this.getItemRecepcion().getCantidad();
	}

	private Pendiente pendiente = null;
	
	@Override
	public Pendiente getPendiente() {
		if (this.pendiente == null){			
			this.pendiente = getItemRecepcion().getRecepcionMercaderia().buscarPendiente(FacturaCompra.class);
		}
		return this.pendiente;
	}
}

