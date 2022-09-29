package org.openxava.ventas.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;


@Entity

@Table(name="VIEW_METRICASPRODUCTO")

@Tab(properties="producto.codigo, producto.nombre, stock, reservado")

@View(name="Producto", 
	members="stock, reservado, pedidos, comprados;" + 
			"disponible")

public class MetricasProducto {

	@Id @Hidden 
	@Column(length=32)
	private String id;
	
	@ReferenceView("Simple")
	@OneToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="producto_id", referencedColumnName="id")
	@ReadOnly
	private Producto producto;
	
	@ReadOnly
	private BigDecimal stock;

	@ReadOnly
	private BigDecimal reservado;
	
	@ReadOnly
	private BigDecimal pedidos;
	
	@ReadOnly
	private BigDecimal comprados;

	@ReadOnly
	private BigDecimal disponible;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public BigDecimal getStock() {
		return stock;
	}

	public void setStock(BigDecimal stock) {
		this.stock = stock;
	}

	public BigDecimal getReservado() {
		return reservado;
	}

	public void setReservado(BigDecimal reservado) {
		this.reservado = reservado;
	}

	public BigDecimal getComprados() {
		return comprados;
	}

	public void setComprados(BigDecimal comprados) {
		this.comprados = comprados;
	}

	public BigDecimal getPedidos() {
		return pedidos;
	}

	public void setPedidos(BigDecimal pedidos) {
		this.pedidos = pedidos;
	}

	public BigDecimal getDisponible() {
		return disponible;
	}

	public void setDisponible(BigDecimal disponible) {
		this.disponible = disponible;
	}
}
