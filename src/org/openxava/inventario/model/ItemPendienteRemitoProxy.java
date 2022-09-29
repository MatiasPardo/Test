package org.openxava.inventario.model;

import org.openxava.base.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

public class ItemPendienteRemitoProxy implements IItemPendiente{
	
	private ItemOrdenPreparacion itemOrdenPreparacion;
	
	private ItemVentaElectronica itemVentaElectronica;
	
	private Transaccion reciboContado;
	
	public ItemOrdenPreparacion getItemOrdenPreparacion() {
		return itemOrdenPreparacion;
	}

	public void setItemOrdenPreparacion(ItemOrdenPreparacion itemOrdenPreparacion) {
		this.itemOrdenPreparacion = itemOrdenPreparacion;
	}

	public ItemVentaElectronica getItemVentaElectronica() {
		return itemVentaElectronica;
	}

	public void setItemVentaElectronica(ItemVentaElectronica itemVentaElectronica) {
		this.itemVentaElectronica = itemVentaElectronica;
		if (itemVentaElectronica != null){
			this.reciboContado = itemVentaElectronica.getVenta().buscarReciboCobranzaContado();			
		}
		else{
			this.reciboContado = null;
		}
	}	
	
	private Pendiente pendiente = null;
	
	@Override
	public Pendiente getPendiente() {
		if (pendiente == null){
			if (this.getItemOrdenPreparacion() != null){
				pendiente = this.getItemOrdenPreparacion().getOrdenPreparacion().buscarPendiente(Remito.class);
			}
			else if (this.getItemVentaElectronica() != null){
				if (reciboContado == null){
					throw new ValidationException("Recibo de cobranza vacio: no se puede encontrar el pendiente de remito");
				}
				this.pendiente = reciboContado.buscarPendiente(Remito.class);
			}
			else{
				throw new ValidationException("Error ItemPendienteProxy");
			}
		}
		return pendiente;
	}

	@Override
	public Boolean cumplido() {
		if (this.getItemOrdenPreparacion() != null){
			return this.getItemOrdenPreparacion().getRemitido();
		}
		else if (this.getItemVentaElectronica() != null){
			return this.getItemVentaElectronica().getCumplido();
		}
		else{
			throw new ValidationException("Error ItemPendienteProxy");
		}
	}

	@Override
	public void cancelarPendiente() throws ValidationException {
		if (this.getItemOrdenPreparacion() != null){
			this.getItemOrdenPreparacion().setRemitido(Boolean.TRUE);
		}
		else if (this.getItemVentaElectronica() != null){
			this.getItemVentaElectronica().setCumplido(Boolean.TRUE);
		}
		else{
			throw new ValidationException("Error ItemPendienteProxy");
		}
	}

	@Override
	public void liberar(){
		if (this.getItemOrdenPreparacion() != null){
			this.getItemOrdenPreparacion().setRemitido(Boolean.FALSE);
		}
		else if (this.getItemVentaElectronica() != null){
			this.getItemVentaElectronica().setCumplido(Boolean.FALSE);
		}
		else{
			throw new ValidationException("Error ItemPendienteProxy");
		}		
	}
	
	@Override
	public ObjetoNegocio getItem() {
		if (this.getItemOrdenPreparacion() != null){
			return this.getItemOrdenPreparacion();
		}
		else if (this.getItemVentaElectronica() != null){
			return this.getItemVentaElectronica();
		}
		else{
			throw new ValidationException("Error ItemPendienteProxy");
		}
	}
}
