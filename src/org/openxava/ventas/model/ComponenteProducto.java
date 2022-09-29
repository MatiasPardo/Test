package org.openxava.ventas.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.actions.*;
import org.openxava.negocio.model.*;
import org.openxava.ventas.validators.*;

@Entity

@Tabs({
	@Tab(properties="producto.codigo, producto.activo, producto.nombre, componente.codigo, componente.activo, componente.nombre, cantidad, unidadMedida.codigo"
		)	
})

@EntityValidators({
	@EntityValidator(
			value=ReferenciaCircularComposicionValidator.class, 
			properties= {
				@PropertyValue(name="producto", from="producto"),
				@PropertyValue(name="componente", from="componente"),
				@PropertyValue(name="id", from="id"),
			}
	)
})

public class ComponenteProducto extends ObjetoNegocio{
	
	@ReadOnly
	@ReferenceView("Simple")
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	private Producto producto;
	
	@ReferenceView("Simple")
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@OnChange(OnChangeProducto.class)
	private Producto componente;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			depends="this.componente.id, this.componente.id",
			condition=UnidadMedida.CONDITIONDESCRIPTIONLIST)
	private UnidadMedida unidadMedida;
	
	@Min(value=0, message="No puede ser negativo")
	@Required
	private BigDecimal cantidad;
	
	@SuppressWarnings("unchecked")
	@ListProperties(value="componente.codigo, componente.nombre, cantidad, unidadMedida.codigo")
	public Collection<ComponenteProducto> getComposicion(){
		if (this.getComponente() != null){
			return this.getComponente().getComposicion();
		}
		else {
			return Collections.EMPTY_LIST;
		}
	}
	
	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public Producto getComponente() {
		return componente;
	}

	public void setComponente(Producto componente) {
		this.componente = componente;
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : this.cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}	
}
