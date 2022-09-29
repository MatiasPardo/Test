package org.openxava.ventas.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;

@Entity

@Table(name="VIEW_VENTASPORMESITEMS")

@Tab(properties="empresa.codigo, mes, cliente.codigo, producto.codigo, producto.nombre, cantidad, subtotal1, subtotal2", 
	defaultOrder="${mes} desc",
	filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION
)

public class VentasMensualesItems {

	// el id cliente + id producto + periodo (yyyymm) + nro empresa (dos digitos)
	@Id
	@Hidden
	@Column(length=72)
	private String id;
	
	@ManyToOne(optional = true, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="codigo")
	@NoCreate @NoModify
	private Empresa empresa;
	
	@ReadOnly	
	private Date mes;
	
	@ManyToOne(optional = true, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private Cliente cliente;
	
	@ManyToOne(optional = true, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private Producto producto;
	
	@ReadOnly
	private BigDecimal subtotal1;
	
	@ReadOnly
	private BigDecimal subtotal2;
	
	@ReadOnly
	private BigDecimal cantidad;
	
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

	public Date getMes() {
		return mes;
	}
	
	public void setMes(Date mes) {
		this.mes = mes;
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

	public BigDecimal getSubtotal1() {
		return subtotal1;
	}

	public void setSubtotal1(BigDecimal subtotal1) {
		this.subtotal1 = subtotal1;
	}

	public BigDecimal getSubtotal2() {
		return subtotal2;
	}

	public void setSubtotal2(BigDecimal subtotal2) {
		this.subtotal2 = subtotal2;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}	
}
