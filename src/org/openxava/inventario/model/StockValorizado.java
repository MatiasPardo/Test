package org.openxava.inventario.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.negocio.filter.*;
import org.openxava.ventas.model.*;

@Entity

@Table(name="VIEW_STOCKVALORIZADO")

@Tab(properties="listaPrecio.nombre, deposito.nombre, producto.codigo, producto.nombre, stock, precio, importe", 
	filter=DepositoSucursalFilter.class, 
	baseCondition=DepositoSucursalFilter.BASECONDITION)

public class StockValorizado {
	@Id
	@Hidden
	@Column(length=96)
	private String id;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre", 
				condition="${activo} = true")
	private ListaPrecio listaPrecio;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private Deposito deposito;
	
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private Producto producto;
	
	@ReadOnly
	private BigDecimal stock;
	
	@ReadOnly
	private BigDecimal stockDisponible;
	
	@ReadOnly
	private BigDecimal precio;
	
	@ReadOnly
	private BigDecimal importe;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ListaPrecio getListaPrecio() {
		return listaPrecio;
	}

	public void setListaPrecio(ListaPrecio listaPrecio) {
		this.listaPrecio = listaPrecio;
	}

	public Deposito getDeposito() {
		return deposito;
	}

	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
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

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public BigDecimal getStockDisponible() {
		return stockDisponible;
	}

	public void setStockDisponible(BigDecimal stockDisponible) {
		this.stockDisponible = stockDisponible;
	}
}
