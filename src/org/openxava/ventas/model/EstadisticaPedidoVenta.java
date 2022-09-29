package org.openxava.ventas.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.inventario.model.*;
import org.openxava.ventas.validators.ProductoItemVentaValidator;

@Entity

@Views({
	@View(members=
		"producto;"+
		"unidadMedida;" + 
		"cantidad, precioUnitario, porcentajeDescuento;" +
		"detalle;" + 
		"descuento, suma;" +
		"descuentoGlobal, subtotal;" +
		"tasaiva;" + 
		"centroCostos;" +
		"pendientePreparacion;"
	),
	@View(name="PendienteOrdenPreparacion", members=
		"producto;"+
		"unidadMedida;" + 
		"pendientePreparacion;" +
		"cantidad;" + 
		"centroCostos;" 
	)
})
	
@Tab(
		properties="venta.fecha, venta.numero, venta.estado, producto.codigo, producto.nombre, cantidad, precioUnitario, descuento, suma, venta.cliente.nombre, venta.vendedor.nombre, venta.total",
		defaultOrder="${venta.fecha} desc")

@EntityValidators({
	@EntityValidator(value=ProductoItemVentaValidator.class, 
			properties= {
					@PropertyValue(name="transaccion", from="venta"), 
					@PropertyValue(name="objetoEstatico", from="producto")					
				}
	)
})

public class EstadisticaPedidoVenta extends EstadisticaItemVenta implements IItemOriginaOrdenPreparacion{
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private PedidoVenta venta;

	@ReadOnly
	private BigDecimal pendientePreparacion = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal noEntregado = BigDecimal.ZERO;
	
	@Column(length=50)
	private String detalle;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private CentroCostos centroCostos;
	
	public CentroCostos getCentroCostos() {
		return centroCostos;
	}

	
	public void setCentroCostos(CentroCostos centroCostos) {
		this.centroCostos = centroCostos;
	}
	
	
	public PedidoVenta getVenta() {
		return venta;
	}

	public void setVenta(PedidoVenta venta) {
		this.venta = venta;
	}

	public BigDecimal getPendientePreparacion() {
		return pendientePreparacion == null ? BigDecimal.ZERO : this.pendientePreparacion;
	}

	public void setPendientePreparacion(BigDecimal pendientePreparacion) {
		this.pendientePreparacion = pendientePreparacion;
	}
	
	public ItemPendienteOrdenPreparacionProxy itemPendienteOrdenPreparacionProxy() {
		ItemPendienteOrdenPreparacionProxy item = new ItemPendienteOrdenPreparacionProxy();
		item.setItemOrigenOP(this);
		return item;
	}
	
	public ItemPendienteFacturaVentaPorCantidadProxy itemPendienteFacturaVentaProxy(){
		if (!this.getProducto().getTipo().equals(TipoProducto.Producto)){
			ItemPendienteFacturaVentaPorCantidadProxy item = new ItemPendienteFacturaVentaPorCantidadProxy();
			item.setItemPedidoVenta(this);
			return item;
		}
		else{
			return null;
		}
	}
	
	@Override
	public Transaccion transaccion() {
		return this.getVenta();
	}
	
	public BigDecimal getNoEntregado() {
		return noEntregado == null ? BigDecimal.ZERO : this.noEntregado;
	}

	public void setNoEntregado(BigDecimal noEntregado) {
		if (noEntregado == null){
			this.noEntregado = BigDecimal.ZERO;
		}
		else{
			this.noEntregado = noEntregado;
		}
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}
}
