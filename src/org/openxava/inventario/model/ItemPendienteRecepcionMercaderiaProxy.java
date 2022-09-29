package org.openxava.inventario.model;

import java.math.*;

import org.openxava.base.model.*;
import org.openxava.compras.model.*;
import org.openxava.negocio.model.*;

public class ItemPendienteRecepcionMercaderiaProxy extends ItemPendientePorCantidadProxy{
	
	private ItemOrdenCompra itemOrdenCompra;
	
	private ItemRecepcionMercaderia itemRecepcionMercaderia;
	
	public ItemOrdenCompra getItemOrdenCompra() {
		return itemOrdenCompra;
	}

	public void setItemOrdenCompra(ItemOrdenCompra itemOrdenCompra) {
		this.itemOrdenCompra = itemOrdenCompra;
	}

	@Override
	public UnidadMedida getUnidadMedida() {
		return this.getItemOrdenCompra().getUnidadMedida();
	}

	@Override
	public ObjetoNegocio getItem() {
		return this.getItemOrdenCompra();
	}

	@Override
	public BigDecimal getCantidadPendiente() {
		return this.getItemOrdenCompra().getPendienteRecepcion();
	}

	@Override
	public void setCantidadPendiente(BigDecimal cantidadPendiente) {
		BigDecimal pendienteAntesActualizar = this.getCantidadPendiente();
		this.getItemOrdenCompra().setPendienteRecepcion(cantidadPendiente);
				
		if (this.getItemRecepcionMercaderia() != null){
			if (cantidadPendiente.compareTo(pendienteAntesActualizar) > 0){
				// La cantidad pendiente nueva es mayor, se esta liberando el pendiente
				// se resta a las unidades no preparadas, cuando se libera el pendiente
				this.getItemOrdenCompra().setNoEntregado(this.getItemOrdenCompra().getNoEntregado().subtract(this.getItemRecepcionMercaderia().getNoEntregados()));
			}
			else{
				// se esta cancelando el pendiente
				// se acumulan las unidades no preparadas cuando se cancela el pendiente.
				this.getItemOrdenCompra().setNoEntregado(this.getItemOrdenCompra().getNoEntregado().add(this.getItemRecepcionMercaderia().getNoEntregados()));
			}
		}
		
	}

	@Override
	public BigDecimal getCantidadOriginal() {
		return this.getItemOrdenCompra().getCantidad();
	}

	private Pendiente pendiente = null;
	
	@Override
	public Pendiente getPendiente() {
		if (this.pendiente == null){			
			this.pendiente = getItemOrdenCompra().getOrdenCompra().buscarPendiente(RecepcionMercaderia.class);
		}
		return this.pendiente;
	}
	
	@Override
	public String toString(){
		return this.getItemOrdenCompra().toString(); 
	}

	public ItemRecepcionMercaderia getItemRecepcionMercaderia() {
		return itemRecepcionMercaderia;
	}

	public void setItemRecepcionMercaderia(ItemRecepcionMercaderia itemRecepcionMercaderia) {
		this.itemRecepcionMercaderia = itemRecepcionMercaderia;
	}
}
