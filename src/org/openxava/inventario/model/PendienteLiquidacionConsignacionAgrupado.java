package org.openxava.inventario.model;

import java.math.BigDecimal;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.Empresa;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.negocio.model.Sucursal;
import org.openxava.ventas.model.Cliente;
import org.openxava.ventas.model.Producto;

@Entity

@Table(name="VIEW_PENDIENTELIQUIDACIONCONSIGNACIONAGRUPADO")

@Tab(
	filter=SucursalEmpresaFilter.class,
	properties="cliente.codigo, cliente.nombre, producto.codigo, producto.nombre, pendienteLiquidacion, paraFacturar, paraDevolver, empresa.codigo, sucursal.codigo",
	baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL )

public class PendienteLiquidacionConsignacionAgrupado {
	
	@Id
	@Hidden
	@Column(length=98)
	private String id;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Empresa empresa;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Sucursal sucursal;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Cliente cliente;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Producto producto;
	
	@ReadOnly
	private BigDecimal pendienteLiquidacion;
	
	@ReadOnly
	private BigDecimal paraFacturar;
	
	@ReadOnly
	private BigDecimal paraDevolver;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public BigDecimal getPendienteLiquidacion() {
		return pendienteLiquidacion;
	}

	public void setPendienteLiquidacion(BigDecimal pendienteLiquidacion) {
		this.pendienteLiquidacion = pendienteLiquidacion;
	}

	public BigDecimal getParaFacturar() {
		return paraFacturar;
	}

	public void setParaFacturar(BigDecimal paraFacturar) {
		this.paraFacturar = paraFacturar;
	}

	public BigDecimal getParaDevolver() {
		return paraDevolver;
	}

	public void setParaDevolver(BigDecimal paraDevolver) {
		this.paraDevolver = paraDevolver;
	}
}
