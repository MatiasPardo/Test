package org.openxava.base.model;

import java.math.*;

import org.openxava.negocio.model.*;
import org.openxava.validators.*;

public abstract class ItemPendientePorCantidadProxy implements IItemPendientePorCantidad{

	private Cantidad cantidadCancelar = null;
	
	public abstract BigDecimal getCantidadPendiente();

	public abstract void setCantidadPendiente(BigDecimal cantidadPendiente);
	
	public abstract BigDecimal getCantidadOriginal();
	
	public abstract Pendiente getPendiente();

	@Override
	public Boolean cumplido() {
		if (this.getCantidadPendiente().compareTo(BigDecimal.ZERO) == 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public void cancelarPendiente() throws ValidationException{
		BigDecimal cantidadPendiente = this.getCantidadPendiente();
		Cantidad cantidad = this.getCantidadACancelar();
		BigDecimal cantidadCancelar = cantidad.convertir(this.getUnidadMedida()); 
		if (cantidadCancelar.compareTo(cantidadPendiente) > 0){
			throw new ValidationException(this.toString() + " tiene pendiente " + cantidadPendiente.toString() + " pero se esta intentando cancelar " + cantidadCancelar.toString());
		}
		else{
			this.setCantidadPendiente(cantidadPendiente.subtract(cantidadCancelar));
		}
	}
	
	@Override
	public Cantidad getCantidadACancelar() {
		if (this.cantidadCancelar == null){
			this.cantidadCancelar = new Cantidad();
			this.cantidadCancelar.setUnidadMedida(this.getUnidadMedida());
		}
		return this.cantidadCancelar;
	}
	
	@Override
	public void liberar(){
		Cantidad cantidad = this.getCantidadACancelar();
		BigDecimal cantidadCancelo = cantidad.convertir(this.getUnidadMedida());
		BigDecimal cantidadPendiente = this.getCantidadPendiente();
		this.setCantidadPendiente(cantidadPendiente.add(cantidadCancelo));
		
		if (this.getCantidadOriginal().compareTo(this.getCantidadPendiente()) < 0){
			throw new ValidationException("El item " + this.getItem().toString() + " ya ha sido liberado. Cantidad Pendiente " + cantidadPendiente.toString());
		}
	}
}
