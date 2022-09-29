package org.openxava.impuestos.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.base.filter.EmpresaFilter;
import org.openxava.base.model.Empresa;

@Entity

@Table(name="VIEW_POSICIONIVAMENSUAL")

@Tab(properties="empresa.codigo, mes, ventas, compras, resultado, ivaVentas, ivaCompras, iva", 
	defaultOrder="${mes} desc",
	filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION)		

@View(members="empresa, mes; ventas, ivaVentas; compras, ivaCompras;" +
			"resultado, iva;")


public class PosicionIvaMensual {
	
	@Hidden
	@Id
	private String id;
	
	@ManyToOne(optional = true, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="codigo")
	@NoCreate @NoModify
	private Empresa empresa;
	
	@ReadOnly	
	private Date mes; 
	
	@ReadOnly
	private BigDecimal ventas;
	
	@ReadOnly
	private BigDecimal compras;
	
	@ReadOnly
	private BigDecimal resultado;
	
	@ReadOnly
	private BigDecimal ivaVentas;
	
	@ReadOnly
	private BigDecimal ivaCompras;
	
	@ReadOnly
	private BigDecimal iva;

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

	public BigDecimal getVentas() {
		return ventas;
	}

	public void setVentas(BigDecimal ventas) {
		this.ventas = ventas;
	}

	public BigDecimal getCompras() {
		return compras;
	}

	public void setCompras(BigDecimal compras) {
		this.compras = compras;
	}

	public BigDecimal getResultado() {
		return resultado;
	}

	public void setResultado(BigDecimal resultado) {
		this.resultado = resultado;
	}

	public BigDecimal getIvaVentas() {
		return ivaVentas;
	}

	public void setIvaVentas(BigDecimal ivaVentas) {
		this.ivaVentas = ivaVentas;
	}

	public BigDecimal getIvaCompras() {
		return ivaCompras;
	}

	public void setIvaCompras(BigDecimal ivaCompras) {
		this.ivaCompras = ivaCompras;
	}

	public BigDecimal getIva() {
		return iva;
	}

	public void setIva(BigDecimal iva) {
		this.iva = iva;
	}
}
