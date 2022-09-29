package org.openxava.compras.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.inventario.model.ItemRecepcionMercaderia;

@Entity

public class ItemRecepcionFacturaCompra extends ObjetoNegocio{
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("FacturaCompra")
	private ItemCompraElectronica itemFactura;
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("FacturaCompra")
	private ItemRecepcionMercaderia itemRecepcion;
	
	@ReadOnly
	private BigDecimal cancelar = BigDecimal.ZERO;

	public ItemCompraElectronica getItemFactura() {
		return itemFactura;
	}

	public void setItemFactura(ItemCompraElectronica itemFactura) {
		this.itemFactura = itemFactura;
	}

	public ItemRecepcionMercaderia getItemRecepcion() {
		return itemRecepcion;
	}

	public void setItemRecepcion(ItemRecepcionMercaderia itemRecepcion) {
		this.itemRecepcion = itemRecepcion;
	}

	public BigDecimal getCancelar() {
		return cancelar == null ? BigDecimal.ZERO : this.cancelar;
	}

	public void setCancelar(BigDecimal cancelar) {
		this.cancelar = cancelar;
	}	
}