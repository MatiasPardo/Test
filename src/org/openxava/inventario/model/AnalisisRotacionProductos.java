package org.openxava.inventario.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.ventas.model.Producto;

@Entity

@Table(name="VIEW_ANALISISROTACIONPRODUCTOS")

@View(members="producto;" +
		"Ventas[rotacionVentas6, rotacionVentas5, rotacionVentas4;" +  
				"rotacionVentas3, rotacionVentas2, rotacionVentas1;" +
				"rotacionVentasSemestre, rotacionVentasAnual;" +
				"];" +
		"Stock[stock, rotacionStockSemestre, rotacionStockAnual;];" + 		
		"Precios[rotacionCostoMoneda];"  
	)

@Tab(properties="producto.codigo, producto.nombre," + 
		"rotacionVentasAnual, rotacionVentasSemestre, rotacionVentas6, rotacionVentas5, rotacionVentas4, rotacionVentas3, rotacionVentas2, rotacionVentas1," + 
		"rotacionStockSemestre, rotacionStockAnual, stock, rotacionCostoMoneda")

public class AnalisisRotacionProductos {
	
	@Id
	@Hidden
	@ReadOnly
	@Column(length=32)
	private String id;
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	private Producto producto;
	
	@ReadOnly
	private BigDecimal rotacionVentas6;
	
	@ReadOnly
	private BigDecimal rotacionVentas5;
	
	@ReadOnly
	private BigDecimal rotacionVentas4;
	
	@ReadOnly
	private BigDecimal rotacionVentas3;
	
	@ReadOnly
	private BigDecimal rotacionVentas2;
	
	@ReadOnly
	private BigDecimal rotacionVentas1;
	
	@ReadOnly
	private BigDecimal rotacionVentasSemestre;
	
	@ReadOnly
	private BigDecimal rotacionVentasAnual;
	
	@ReadOnly
	private BigDecimal stock;
	
	@ReadOnly
	private BigDecimal rotacionStockSemestre;
	
	@ReadOnly
	private BigDecimal rotacionStockAnual;
	
	@ReadOnly
	private BigDecimal rotacionCostoMoneda;

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

	public BigDecimal getRotacionVentas6() {
		return rotacionVentas6;
	}

	public void setRotacionVentas6(BigDecimal rotacionVentas6) {
		this.rotacionVentas6 = rotacionVentas6;
	}

	public BigDecimal getRotacionVentas5() {
		return rotacionVentas5;
	}

	public void setRotacionVentas5(BigDecimal rotacionVentas5) {
		this.rotacionVentas5 = rotacionVentas5;
	}

	public BigDecimal getRotacionVentas4() {
		return rotacionVentas4;
	}

	public void setRotacionVentas4(BigDecimal rotacionVentas4) {
		this.rotacionVentas4 = rotacionVentas4;
	}

	public BigDecimal getRotacionVentas3() {
		return rotacionVentas3;
	}

	public void setRotacionVentas3(BigDecimal rotacionVentas3) {
		this.rotacionVentas3 = rotacionVentas3;
	}

	public BigDecimal getRotacionVentas2() {
		return rotacionVentas2;
	}

	public void setRotacionVentas2(BigDecimal rotacionVentas2) {
		this.rotacionVentas2 = rotacionVentas2;
	}

	public BigDecimal getRotacionVentas1() {
		return rotacionVentas1;
	}

	public void setRotacionVentas1(BigDecimal rotacionVentas1) {
		this.rotacionVentas1 = rotacionVentas1;
	}

	public BigDecimal getRotacionVentasSemestre() {
		return rotacionVentasSemestre;
	}

	public void setRotacionVentasSemestre(BigDecimal rotacionVentasSemestre) {
		this.rotacionVentasSemestre = rotacionVentasSemestre;
	}

	public BigDecimal getRotacionVentasAnual() {
		return rotacionVentasAnual;
	}

	public void setRotacionVentasAnual(BigDecimal rotacionVentasAnual) {
		this.rotacionVentasAnual = rotacionVentasAnual;
	}

	public BigDecimal getStock() {
		return stock;
	}

	public void setStock(BigDecimal stock) {
		this.stock = stock;
	}

	public BigDecimal getRotacionCostoMoneda() {
		return rotacionCostoMoneda;
	}

	public void setRotacionCostoMoneda(BigDecimal rotacionCostoMoneda) {
		this.rotacionCostoMoneda = rotacionCostoMoneda;
	}

	public BigDecimal getRotacionStockSemestre() {
		return rotacionStockSemestre;
	}

	public void setRotacionStockSemestre(BigDecimal rotacionStockSemestre) {
		this.rotacionStockSemestre = rotacionStockSemestre;
	}

	public BigDecimal getRotacionStockAnual() {
		return rotacionStockAnual;
	}

	public void setRotacionStockAnual(BigDecimal rotacionStockAnual) {
		this.rotacionStockAnual = rotacionStockAnual;
	}
}
