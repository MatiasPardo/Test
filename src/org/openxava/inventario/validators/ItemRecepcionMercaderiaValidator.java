package org.openxava.inventario.validators;

import java.math.*;

import org.openxava.base.validators.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class ItemRecepcionMercaderiaValidator extends ItemTransaccionValidator{

	private BigDecimal cantidadPendiente;
	
	private BigDecimal cantidadNoEntregada;
	
	private BigDecimal cantidadRecepcionada;
	
	private Producto producto;
	
	public BigDecimal getCantidadPendiente() {
		return cantidadPendiente == null ? BigDecimal.ZERO : cantidadPendiente;
	}

	public void setCantidadPendiente(BigDecimal cantidadPendiente) {
		this.cantidadPendiente = cantidadPendiente;
	}

	public BigDecimal getCantidadNoEntregada() {
		return cantidadNoEntregada == null ? BigDecimal.ZERO : cantidadNoEntregada;
	}

	public void setCantidadNoEntregada(BigDecimal cantidadNoEntregada) {
		this.cantidadNoEntregada = cantidadNoEntregada;
	}

	public BigDecimal getCantidadRecepcionada() {
		return cantidadRecepcionada == null ? BigDecimal.ZERO : cantidadRecepcionada;
	}

	public void setCantidadRecepcionada(BigDecimal cantidadRecepcionada) {
		this.cantidadRecepcionada = cantidadRecepcionada;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	protected void validarItemTransaccion(Messages errores){
		if ((this.getCantidadNoEntregada().compareTo(BigDecimal.ZERO) == 0) &&
			(this.getCantidadRecepcionada().compareTo(BigDecimal.ZERO) == 0)){
			errores.add(getProducto().getCodigo() +": ambas cantidades no pueden ser 0" );
		}	
		else{
			if (this.getCantidadPendiente().compareTo(BigDecimal.ZERO) > 0){
				if (this.getCantidadNoEntregada().compareTo(this.getCantidadPendiente()) > 0){
					errores.add(getProducto().toString() + ": Lo no entregado " + this.getCantidadNoEntregada().toString() + " supera la cantidad pendiente " + this.getCantidadPendiente().toString());
				}
				else if (this.getCantidadRecepcionada().compareTo(this.getCantidadPendiente()) > 0){
					if (this.getCantidadNoEntregada().compareTo(BigDecimal.ZERO) != 0){
						errores.add(getProducto().toString() + ": La cantidad no entregada debe ser 0 porque la cantidad recepcionada es mayor a la cantidad pendiente");
					}
				}
				else if (this.getCantidadNoEntregada().add(this.getCantidadRecepcionada()).compareTo(this.getCantidadPendiente()) > 0){
						errores.add(getProducto().toString() + ": ambas cantidades " + this.getCantidadNoEntregada().add(this.getCantidadRecepcionada()).toString() + " superan lo pendiente " + this.getCantidadPendiente().toString());
				}				
			}
		}
	}
}