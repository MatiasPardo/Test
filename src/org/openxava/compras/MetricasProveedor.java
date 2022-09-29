package org.openxava.compras;

import java.math.BigDecimal;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.compras.model.Proveedor;

@Entity

@Table(name="VIEW_METRICASPROVEEDOR")

@Views({
	@View(name="CuentaCorriente", members=
		"saldoCtaCteEmpresa1Moneda1, saldoCtaCteEmpresa2Moneda1, saldoCtaCteMoneda1;" +
		"saldoCtaCteEmpresa1Moneda2, saldoCtaCteEmpresa2Moneda2, saldoCtaCteMoneda2;"  		
	)
})

public class MetricasProveedor {
	
	@Id @Hidden 
	@Column(length=32)
	private String id;
	
	@ReferenceView("Simple")
	@OneToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="proveedor_id", referencedColumnName="id")
	@ReadOnly
	private Proveedor proveedor;
	
	@ReadOnly
	private BigDecimal saldoCtaCteEmpresa1Moneda1;
	
	@ReadOnly
	private BigDecimal saldoCtaCteEmpresa2Moneda1;
	
	@ReadOnly
	private BigDecimal saldoCtaCteMoneda1;
	
	@ReadOnly
	private BigDecimal saldoCtaCteEmpresa1Moneda2;
	
	@ReadOnly
	private BigDecimal saldoCtaCteEmpresa2Moneda2;
	
	@ReadOnly
	private BigDecimal saldoCtaCteMoneda2;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public BigDecimal getSaldoCtaCteEmpresa1Moneda1() {
		return saldoCtaCteEmpresa1Moneda1;
	}

	public void setSaldoCtaCteEmpresa1Moneda1(BigDecimal saldoCtaCteEmpresa1Moneda1) {
		this.saldoCtaCteEmpresa1Moneda1 = saldoCtaCteEmpresa1Moneda1;
	}

	public BigDecimal getSaldoCtaCteEmpresa2Moneda1() {
		return saldoCtaCteEmpresa2Moneda1;
	}

	public void setSaldoCtaCteEmpresa2Moneda1(BigDecimal saldoCtaCteEmpresa2Moneda1) {
		this.saldoCtaCteEmpresa2Moneda1 = saldoCtaCteEmpresa2Moneda1;
	}

	public BigDecimal getSaldoCtaCteMoneda1() {
		return saldoCtaCteMoneda1;
	}

	public void setSaldoCtaCteMoneda1(BigDecimal saldoCtaCteMoneda1) {
		this.saldoCtaCteMoneda1 = saldoCtaCteMoneda1;
	}

	public BigDecimal getSaldoCtaCteEmpresa1Moneda2() {
		return saldoCtaCteEmpresa1Moneda2;
	}

	public void setSaldoCtaCteEmpresa1Moneda2(BigDecimal saldoCtaCteEmpresa1Moneda2) {
		this.saldoCtaCteEmpresa1Moneda2 = saldoCtaCteEmpresa1Moneda2;
	}

	public BigDecimal getSaldoCtaCteEmpresa2Moneda2() {
		return saldoCtaCteEmpresa2Moneda2;
	}

	public void setSaldoCtaCteEmpresa2Moneda2(BigDecimal saldoCtaCteEmpresa2Moneda2) {
		this.saldoCtaCteEmpresa2Moneda2 = saldoCtaCteEmpresa2Moneda2;
	}

	public BigDecimal getSaldoCtaCteMoneda2() {
		return saldoCtaCteMoneda2;
	}

	public void setSaldoCtaCteMoneda2(BigDecimal saldoCtaCteMoneda2) {
		this.saldoCtaCteMoneda2 = saldoCtaCteMoneda2;
	}
}
