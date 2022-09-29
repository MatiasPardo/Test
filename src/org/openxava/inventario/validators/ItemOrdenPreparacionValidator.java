package org.openxava.inventario.validators;

import java.math.*;

import org.openxava.base.validators.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class ItemOrdenPreparacionValidator extends ItemTransaccionValidator{

	private BigDecimal cantidadSolicitada;
	
	private BigDecimal cantidadNoPreparar;
	
	private BigDecimal cantidadPreparar;
	
	private Producto producto;
	
	private Boolean aceptaCantidadExcede;
	
	public BigDecimal getCantidadSolicitada() {
		return cantidadSolicitada == null ? BigDecimal.ZERO : cantidadSolicitada;
	}

	public void setCantidadSolicitada(BigDecimal cantidadSolicitada) {
		this.cantidadSolicitada = cantidadSolicitada;
	}

	public BigDecimal getCantidadNoPreparar() {
		return cantidadNoPreparar == null ? BigDecimal.ZERO : cantidadNoPreparar;
	}

	public void setCantidadNoPreparar(BigDecimal cantidadNoPreparar) {
		this.cantidadNoPreparar = cantidadNoPreparar;
	}

	public BigDecimal getCantidadPreparar() {
		return cantidadPreparar == null ? BigDecimal.ZERO : cantidadPreparar;
	}

	public void setCantidadPreparar(BigDecimal cantidadPreparar) {
		this.cantidadPreparar = cantidadPreparar;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public Boolean getAceptaCantidadExcede() {
		return aceptaCantidadExcede == null ? Boolean.FALSE : this.aceptaCantidadExcede;
	}

	public void setAceptaCantidadExcede(Boolean aceptaCantidadExcede) {
		this.aceptaCantidadExcede = aceptaCantidadExcede;
	}

	protected void validarItemTransaccion(Messages errores){
		if ((this.getCantidadNoPreparar().compareTo(BigDecimal.ZERO) == 0) &&
			(this.getCantidadPreparar().compareTo(BigDecimal.ZERO) == 0)){
			errores.add(getProducto().getCodigo() +": la cantidad y lo que no se prepara no pueden ser ambas 0" );
		}	
		else{
			if (this.getCantidadSolicitada().compareTo(BigDecimal.ZERO) > 0){
				if (!this.getAceptaCantidadExcede()){
					if (this.getCantidadPreparar().compareTo(this.getCantidadSolicitada()) > 0){						
						errores.add(getProducto().toString() + ": La cantidad " + this.getCantidadPreparar().toString() + " supera la cantidad pendiente del pedido " + this.getCantidadSolicitada().toString());
					}
					
					if (this.getCantidadNoPreparar().compareTo(this.getCantidadSolicitada()) > 0){
						errores.add(getProducto().toString() + ": Lo no preparado " + this.getCantidadNoPreparar().toString() + " supera la cantidad pendiente del pedido " + this.getCantidadSolicitada().toString());
					}
					
					if (errores.isEmpty()){
						if (this.getCantidadNoPreparar().add(this.getCantidadPreparar()).compareTo(this.getCantidadSolicitada()) > 0){
							errores.add(getProducto().toString() + ": Las cantidades superan lo pendiente del pedido " + this.getCantidadSolicitada().toString());
						}
					}
				}
				else{
					if (this.getCantidadNoPreparar().compareTo(BigDecimal.ZERO) > 0){
						errores.add(getProducto().toString() + ": si acepta una cantidad superior a lo pedido lo no preparado debe ser 0");
					}
				}
			}
		}
	}
}

