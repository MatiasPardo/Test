package org.openxava.reclamos.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.negocio.calculators.*;
import org.openxava.reclamos.actions.*;
import org.openxava.ventas.model.*;

@Entity

@View(members="numero, estado;" + 
		"fechaCreacion, usuario;" + 
		"vehiculo;" + 
		"tipo, fecha, kilometraje;" +
		"Insumo[producto, cantidad];" + 
		"observaciones")

@Tab(
		filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION,
		properties="numero, fecha, estado, vehiculo.codigo, vehiculo.nombre, tipo.nombre, kilometraje, producto.nombre, fechaCreacion, usuario",
		rowStyles={
			@RowStyle(style="pendiente-ejecutado", property="estado", value="Anulada"),	
			@RowStyle(style="pendiente-ejecutado", property="estado", value="Cancelada"),
		},
		defaultOrder="${fechaCreacion} desc")


public class MantenimientoVehiculo extends Transaccion{
	
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@OnChange(OnChangeVehiculoAction.class)
	private Vehiculo vehiculo;
	
	@Required
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=ObjetoPrincipalCalculator.class, 
			properties= {@PropertyValue(name="entidad",value="TipoMantenimiento")})
	private TipoMantenimiento tipo;
	
	@Required
	private Integer kilometraje;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@NoFrame
	private Producto producto;
	
	private BigDecimal cantidad;
	
	public Vehiculo getVehiculo() {
		return vehiculo;
	}

	public void setVehiculo(Vehiculo vehiculo) {
		this.vehiculo = vehiculo;
	}

	public TipoMantenimiento getTipo() {
		return tipo;
	}

	public void setTipo(TipoMantenimiento tipo) {
		this.tipo = tipo;
	}

	public Integer getKilometraje() {
		return kilometraje;
	}

	public void setKilometraje(Integer kilometraje) {
		this.kilometraje = kilometraje;
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Mantenimiento";
	}

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
}
