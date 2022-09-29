package org.openxava.inventario.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.OnChange;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Required;
import org.openxava.annotations.SearchListCondition;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.ItemTransaccion;
import org.openxava.base.model.Transaccion;
import org.openxava.negocio.actions.OnChangeProducto;
import org.openxava.negocio.actions.OnChangeUnidadMedida;
import org.openxava.negocio.model.UnidadMedida;
import org.openxava.ventas.model.Producto;

@Entity

@Views({
	@View(members=
			"producto;"+
			"unidadMedida;" + 	
			"cantidad;" + 
			"detalle;" + 
			"pendientePreparacion"
		),
	@View(name="ItemPedidoML", 
		members="solicitud")	
})



@Tab(properties="solicitud.fecha, solicitud.numero, solicitud.estado, origen.nombre, destino.nombre, producto.codigo, producto.nombre, cantidad")

public class ItemSolicitudMercaderia extends ItemTransaccion implements IItemOriginaOrdenPreparacion{
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private SolicitudMercaderia solicitud;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY) 
	@ReferenceView("Simple")
	@OnChange(OnChangeProducto.class)
	@NoCreate @NoModify
	@SearchListCondition(value="${tipo} = 0")
	private Producto producto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre",
					depends=UnidadMedida.DEPENDSDESCRIPTIONLIST,
					condition=UnidadMedida.CONDITIONDESCRIPTIONLIST)
	@NoCreate @NoModify
	@OnChange(OnChangeUnidadMedida.class)
	private UnidadMedida unidadMedida;
	
	@Required
	private BigDecimal cantidad;

	@Column(length=50)
	private String detalle;

	@ReadOnly
	private BigDecimal pendientePreparacion = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal noEntregado = BigDecimal.ZERO;
		
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

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public SolicitudMercaderia getSolicitud() {
		return solicitud;
	}

	public void setSolicitud(SolicitudMercaderia solicitud) {
		this.solicitud = solicitud;
	}

	@Override
	public Transaccion transaccion() {
		return this.getSolicitud();
	}

	@Override
	public void recalcular() {
		if (this.getProducto() != null){
			if (this.getUnidadMedida() == null){
				this.setUnidadMedida(this.getProducto().getUnidadMedida());
			}
		}
	}
	
	public BigDecimal getPendientePreparacion() {
		return pendientePreparacion == null ? BigDecimal.ZERO : this.pendientePreparacion;
	}

	public void setPendientePreparacion(BigDecimal pendientePreparacion) {
		this.pendientePreparacion = pendientePreparacion;
	}
	
	public BigDecimal getNoEntregado() {
		return noEntregado == null ? BigDecimal.ZERO : this.noEntregado;
	}

	public void setNoEntregado(BigDecimal noEntregado) {
		this.noEntregado = noEntregado;
	}
	
	public ItemPendienteOrdenPreparacionProxy itemPendienteOrdenPreparacionProxy() {
		ItemPendienteOrdenPreparacionProxy item = new ItemPendienteOrdenPreparacionProxy();
		item.setItemOrigenOP(this);
		return item;
	}	
}

