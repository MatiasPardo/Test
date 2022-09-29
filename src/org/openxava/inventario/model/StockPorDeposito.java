package org.openxava.inventario.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.negocio.filter.*;
import org.openxava.ventas.model.*;

@Entity

@Table(name="VIEW_STOCKPORDEPOSITO")

@Tabs({
	@Tab(properties="deposito.nombre, producto.codigo, producto.nombre, disponible, stock, reservado", 
			filter=DepositoSucursalFilter.class,
			baseCondition=DepositoSucursalFilter.BASECONDITION),
	@Tab(name="StockPorSucursal",
		properties="deposito.sucursal.nombre, deposito.nombre, producto.codigo, producto.nombre, disponible, stock, reservado")
})

public class StockPorDeposito {
	@Id
	@Hidden
	@Column(length=64)
	private String id;
	
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
	private BigDecimal reservado;
	
	@ReadOnly
	private BigDecimal disponible;

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

	public BigDecimal getReservado() {
		return reservado;
	}

	public void setReservado(BigDecimal reservado) {
		this.reservado = reservado;
	}

	public BigDecimal getDisponible() {
		return disponible;
	}

	public void setDisponible(BigDecimal disponible) {
		this.disponible = disponible;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
