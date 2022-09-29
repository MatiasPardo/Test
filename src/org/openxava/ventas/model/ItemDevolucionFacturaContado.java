package org.openxava.ventas.model;

import java.math.BigDecimal;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.ReadOnly;
import org.openxava.negocio.model.UnidadMedida;

@Embeddable

public class ItemDevolucionFacturaContado {

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	private Producto producto;
	
	@ReadOnly
	private BigDecimal cantidad;
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private UnidadMedida unidadMedida;
		
	private BigDecimal devolucion;
			
	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getDevolucion() {
		return devolucion == null ? BigDecimal.ZERO : devolucion;
	}

	public void setDevolucion(BigDecimal devolucion) {
		this.devolucion = devolucion;
	}
	
	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}	
}
