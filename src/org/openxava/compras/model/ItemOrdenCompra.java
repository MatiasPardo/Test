package org.openxava.compras.model;

import java.math.*;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.CurrentDateCalculator;
import org.openxava.compras.validators.ProductoItemCompraValidator;
import org.openxava.contabilidad.model.CentroCostos;
import org.openxava.inventario.model.*;

@Entity

@Views({
	@View(
		members="producto;" + 
				"unidadMedida;" + 
				"cantidad;" + 
				"precioUnitario, porcentajeDescuento, descuento;" + 
				"detalle;" +
				"suma, alicuotaIva;" + 
				"pendienteRecepcion, noEntregado;" + 
				"fechaRecepcion;" + 
				"centroCostos;"),
	@View(name="Recepcion",
		members="producto;" +
				"unidadMedida, cantidad, precioUnitario;")
})

@Tabs({
	@Tab(
		properties="ordenCompra.fecha, ordenCompra.numero, ordenCompra.estado, producto.codigo, producto.nombre, cantidad, pendienteRecepcion, noEntregado, precioUnitario, suma",
		defaultOrder="${ordenCompra.fecha} desc"
	),
	@Tab(name="ItemPendienteRecepcionMercaderia",
		properties="ordenCompra.fecha, ordenCompra.numero, ordenCompra.estado, producto.codigo, producto.nombre, cantidad, pendienteRecepcion, precioUnitario",
		baseCondition="${pendienteRecepcion} > 0 and ${ordenCompra.estado} = 1",
		defaultOrder="${ordenCompra.fecha} desc")
})

@EntityValidators({
	@EntityValidator(value=ProductoItemCompraValidator.class, 
			properties= {
					@PropertyValue(name="transaccion", from="ordenCompra"), 
					@PropertyValue(name="objetoEstatico", from="producto")					
				}
	)
})

public class ItemOrdenCompra extends ItemCompra{


	@ReadOnly	
	private BigDecimal noEntregado = BigDecimal.ZERO;
	
	@Required
	@DefaultValueCalculator(CurrentDateCalculator.class)
	private Date fechaRecepcion;
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private OrdenCompra ordenCompra;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="codigo, nombre", forTabs="ninguno")
	private CentroCostos centroCostos;
	
	public OrdenCompra getOrdenCompra() {
		return ordenCompra;
	}

	public void setOrdenCompra(OrdenCompra ordenCompra) {
		this.ordenCompra = ordenCompra;
	}
	
	@ReadOnly
	private BigDecimal pendienteRecepcion;
	
	public BigDecimal getPendienteRecepcion() {
		return pendienteRecepcion == null ? BigDecimal.ZERO : pendienteRecepcion;
	}

	public void setPendienteRecepcion(BigDecimal pendienteRecepcion) {
		this.pendienteRecepcion = pendienteRecepcion;
	}

	@Override
	public Transaccion transaccion() {
		return this.getOrdenCompra();
	}
	
	public ItemPendienteRecepcionMercaderiaProxy itemPendienteRecepcionMercaderiaProxy(){
		ItemPendienteRecepcionMercaderiaProxy item = new ItemPendienteRecepcionMercaderiaProxy();
		item.setItemOrdenCompra(this);
		return item;
	}

	public String conformeOrdenCompra(ItemRecepcionMercaderia itemRecepcion) {
		String detalle = ""; 
		if (itemRecepcion.getProducto() != null){
			if (itemRecepcion.getProducto().equals(this.getProducto())){
				if (itemRecepcion.getPrecioUnitario().compareTo(this.getPrecioUnitario()) > 0){
					detalle += "Difiere el precio: " + this.getPrecioUnitario().toString();
				}
				if (itemRecepcion.getCantidad().compareTo(this.getPendienteRecepcion()) > 0){
					if (detalle != "") detalle += " / "; 
					detalle += "Mas unidades: " + this.getPendienteRecepcion().toString();
				}
			}
			else{
				detalle += "Distino producto: " + this.getProducto().toString();
			}
		}
		
		return detalle;
	}

	public BigDecimal getNoEntregado() {
		return noEntregado == null ? BigDecimal.ZERO : noEntregado;
	}

	public void setNoEntregado(BigDecimal noEntregado) {
		if (noEntregado != null){
			this.noEntregado = noEntregado;
		}
		else{
			this.noEntregado = BigDecimal.ZERO;
		}
	}
	
	public Date getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(Date fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

	@Override
	public void recalcular() {
		super.recalcular();
		
		if (this.getOrdenCompra() != null){
			if (!this.getOrdenCompra().getFechaRecepcionPorItem() || this.getFechaRecepcion() == null){
				this.setFechaRecepcion(this.getOrdenCompra().getFechaRecepcion());
			}
		}
		
		if (this.getCentroCostos() == null){
			if (this.getProducto() != null){
				this.setCentroCostos(this.getProducto().getCentroCostos());
			}
		}
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		
		if ((this.getOrdenCompra() != null) && (this.getOrdenCompra().getFechaRecepcionPorItem())){
			propiedadesEditables.add("fechaRecepcion");
		}
		else{
			propiedadesSoloLectura.add("fechaRecepcion");
		}
	}

	public CentroCostos getCentroCostos() {
		return centroCostos;
	}

	public void setCentroCostos(CentroCostos centroCostos) {
		this.centroCostos = centroCostos;
	}
}
