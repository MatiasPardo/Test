package org.openxava.inventario.model;

import java.math.*;

import org.openxava.base.model.*;
import org.openxava.negocio.model.*;

public class ItemPendienteOrdenPreparacionProxy extends ItemPendientePorCantidadProxy{

	private IItemOriginaOrdenPreparacion itemOrigenOP;
	
	private ItemOrdenPreparacion itemOrdenPreparacion;
	
	private Pendiente pendiente = null;
	
	public IItemOriginaOrdenPreparacion getItemOrigenOP() {
		return itemOrigenOP;
	}

	public void setItemOrigenOP(IItemOriginaOrdenPreparacion itemOrigenOP) {
		this.itemOrigenOP = itemOrigenOP;
	}

	@Override
	public BigDecimal getCantidadPendiente() {
		return getItemOrigenOP().getPendientePreparacion();
	}
	
	@Override
	public void setCantidadPendiente(BigDecimal cantidadPendiente){
		BigDecimal pendienteAntesActualizar = this.getCantidadPendiente();
		this.getItemOrigenOP().setPendientePreparacion(cantidadPendiente);
		
		if (this.getItemOrdenPreparacion() != null){
			if (cantidadPendiente.compareTo(pendienteAntesActualizar) > 0){
				// La cantidad pendiente nueva es mayor, se esta liberando el pendiente
				// se resta a las unidades no preparadas, cuando se libera el pendiente
				this.getItemOrigenOP().setNoEntregado(this.getItemOrigenOP().getNoEntregado().subtract(this.getItemOrdenPreparacion().getNoPreparar()));
			}
			else{
				// se esta cancelando el pendiente
				// se acumulan las unidades no preparadas cuando se cancela el pendiente.
				this.getItemOrigenOP().setNoEntregado(this.getItemOrigenOP().getNoEntregado().add(this.getItemOrdenPreparacion().getNoPreparar()));
			}
		}		
	}
	
	@Override
	public BigDecimal getCantidadOriginal() {
		return getItemOrigenOP().getCantidad();
	}

	@Override
	public Pendiente getPendiente() {
		if (this.pendiente == null){
			this.pendiente = ((ItemTransaccion)this.getItemOrigenOP()).transaccion().buscarPendiente(OrdenPreparacion.class);			
		}
		return this.pendiente;
	}
	
	@Override
	public String toString(){
		return this.getItemOrigenOP().toString(); 
	}

	@Override
	public ObjetoNegocio getItem() {
		return (ObjetoNegocio)this.getItemOrigenOP();
	}

	@Override
	public UnidadMedida getUnidadMedida() {
		return this.getItemOrigenOP().getUnidadMedida();
	}
	
	public ItemOrdenPreparacion getItemOrdenPreparacion() {
		return itemOrdenPreparacion;
	}

	public void setItemOrdenPreparacion(ItemOrdenPreparacion itemOrdenPreparacion) {
		this.itemOrdenPreparacion = itemOrdenPreparacion;
	}
}
