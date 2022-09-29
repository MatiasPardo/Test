package org.openxava.ventas.model;

import org.openxava.base.model.*;
import org.openxava.inventario.model.*;
import org.openxava.validators.*;

public class ItemPendienteFacturaVentaProxy implements IItemPendiente{
	
	private ItemRemito itemRemito;

	public ItemRemito getItemRemito() {
		return itemRemito;
	}

	public void setItemRemito(ItemRemito itemRemito) {
		this.itemRemito = itemRemito;
	}
	
	private ItemLiquidacionConsignacion itemLiquidacion;
	
	public ItemLiquidacionConsignacion getItemLiquidacion() {
		return itemLiquidacion;
	}

	public void setItemLiquidacion(ItemLiquidacionConsignacion itemLiquidacion) {
		this.itemLiquidacion = itemLiquidacion;
	}

	private Pendiente pendiente = null;
	
	@Override
	public Pendiente getPendiente() {
		if (pendiente == null){
			if (this.getItemLiquidacion() != null){
				pendiente = this.getItemLiquidacion().getLiquidacion().buscarPendienteFactura();
			}
			else{
				pendiente = this.getItemRemito().getRemito().buscarPendienteFactura();
			}
		}
		return pendiente;
	}
	
	@Override
	public Boolean cumplido() {
		if (this.getItemLiquidacion() != null){
			return this.getItemLiquidacion().getFacturado();
		}
		else{
			return this.getItemRemito().getFacturado();
		}
	}

	@Override
	public void cancelarPendiente() throws ValidationException {
		if (this.getItemLiquidacion() != null){
			this.getItemLiquidacion().setFacturado(Boolean.TRUE);
			if (this.getItemRemito().itemPendienteLiquidacionProxy().cumplido()){
				this.getItemRemito().setFacturado(Boolean.TRUE);
			}
		}
		else{
			this.getItemRemito().setFacturado(Boolean.TRUE);
		}
	}

	@Override
	public void liberar(){
		if (this.getItemLiquidacion() != null){
			this.getItemLiquidacion().setFacturado(Boolean.FALSE);
			this.getItemRemito().setFacturado(Boolean.FALSE);
		}
		else{
			this.getItemRemito().setFacturado(Boolean.FALSE);
		}
	}
	
	@Override
	public ObjetoNegocio getItem() {
		if (this.getItemLiquidacion() != null){
			return this.getItemLiquidacion();
		}
		else{
			return this.getItemRemito();
		}
	}

}
