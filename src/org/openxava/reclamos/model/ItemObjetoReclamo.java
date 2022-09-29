package org.openxava.reclamos.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.actions.*;
import org.openxava.negocio.model.*;
import org.openxava.ventas.model.*;

@Views({
	@View(name="Simple", members=" objetoReclamo; producto; cantidad, unidadMedida"),
	@View(members=" objetoReclamo; producto; cantidad, unidadMedida ")
})

@Entity
public class ItemObjetoReclamo extends ObjetoNegocio{
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@NoCreate @NoModify
	private ObjetoReclamo objetoReclamo;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@OnChange(OnChangeProducto.class)
	private Producto producto;

	@Required
	private BigDecimal cantidad;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@DescriptionsList(descriptionProperties="codigo, nombre",
		depends = UnidadMedida.DEPENDSDESCRIPTIONLIST,
		condition=UnidadMedida.CONDITIONDESCRIPTIONLIST)
	@NoCreate @NoModify
	private UnidadMedida unidadMedida;
	
	public ObjetoReclamo getObjetoReclamo() {
		return objetoReclamo;
	}

	public void setObjetoReclamo(ObjetoReclamo objetoReclamo) {
		this.objetoReclamo = objetoReclamo;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}
}
