package org.openxava.inventario.model;

import java.math.BigDecimal;

import org.openxava.base.model.ItemPendientePorCantidadProxy;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.base.model.Pendiente;
import org.openxava.negocio.model.UnidadMedida;

public class ItemPendienteControlMercaderiaProxy extends ItemPendientePorCantidadProxy{
	
	private ItemRemito itemRemito;
	
	public ItemRemito getItemRemito() {
		return itemRemito;
	}

	public void setItemRemito(ItemRemito itemRemito) {
		this.itemRemito = itemRemito;
	}

	@Override
	public UnidadMedida getUnidadMedida() {
		return this.getItemRemito().getUnidadMedida();
	}

	@Override
	public ObjetoNegocio getItem() {
		return this.getItemRemito();
	}

	@Override
	public BigDecimal getCantidadPendiente() {
		return this.getItemRemito().getPendienteLiquidacion();
	}

	@Override
	public void setCantidadPendiente(BigDecimal cantidadPendiente) {
		this.getItemRemito().setPendienteLiquidacion(cantidadPendiente);		
	}

	@Override
	public BigDecimal getCantidadOriginal() {
		return this.getItemRemito().getCantidad();
	}

	private Pendiente pendiente = null;
	
	@Override
	public Pendiente getPendiente() {
		if (pendiente == null){
			this.pendiente = getItemRemito().getRemito().buscarPendiente(ControlMercaderia.class);
		}
		return pendiente;
	}
}

