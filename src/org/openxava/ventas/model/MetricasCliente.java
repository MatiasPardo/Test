package org.openxava.ventas.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;

@Entity

@Table(name="VIEW_METRICASCLIENTE")

@Tab(properties="cliente.codigo, cliente.nombre")

@Views({
	@View(name="Cliente", 
			members="saldoCtaCteMoneda1, saldoPedidos1;" + 
					"saldoCtaCteMoneda2, saldoPedidos2;" + 
					"Moneda2[situacionCrediticia, creditoDisponible];"),
	@View(name="PedidoVenta", 
		members="saldoCtaCteMoneda1, saldoPedidos1;" + 
				"situacionCrediticia"),
	@View(name="Cobranza", 
		members="saldoCtaCteEmpresa1Moneda1, saldoCtaCteEmpresa2Moneda1"),
	@View(name="CuentaCorriente", 
		members="Saldos[saldoCtaCteEmpresa1Moneda1, saldoCtaCteEmpresa2Moneda1, saldoCtaCteMoneda1;" + 
						"saldoCtaCteEmpresa1Moneda2, saldoCtaCteEmpresa2Moneda2, saldoCtaCteMoneda2];" +				
				"SituacionCrediticiaActual[saldoCtaCteMoneda1, saldoPedidos1;" + 
										"saldoCtaCteMoneda2, saldoPedidos2;" + 
										"situacionCrediticia, limiteCredito, creditoDisponible" + 
										"];" + 
				"AnticuacionDeuda[" +
					"Moneda1[saldoCtaCteMoneda1MesActual; saldoCtaCteMoneda1MesAnt1, saldoCtaCteMoneda1MesAnt2, saldoCtaCteMoneda1MesAnt3; saldoCtaCteMoneda1MesAnt4, saldoCtaCteMoneda1MesAnt5, saldoCtaCteMoneda1MesAnt6]" +		
					"Moneda2[saldoCtaCteMoneda2MesActual; saldoCtaCteMoneda2MesAnt1, saldoCtaCteMoneda2MesAnt2, saldoCtaCteMoneda2MesAnt3; saldoCtaCteMoneda2MesAnt4, saldoCtaCteMoneda2MesAnt5, saldoCtaCteMoneda2MesAnt6];" +
				"]"
				
	)	
})

public class MetricasCliente {
	
	@Id @Hidden 
	@Column(length=32)
	private String id;
	
	@ReferenceView("Simple")
	@OneToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="cliente_id", referencedColumnName="id")
	@ReadOnly
	private Cliente cliente;
	
private BigDecimal saldoCtaCteMoneda1;
	
	private BigDecimal saldoPedidos1;
	
	private BigDecimal saldoCtaCteMoneda2;
	
	private BigDecimal saldoPedidos2;
	
	private BigDecimal situacionCrediticia;
	
	private BigDecimal limiteCredito;
	
	private BigDecimal creditoDisponible;
	
	private BigDecimal saldoCtaCteEmpresa1Moneda1;
	
	private BigDecimal saldoCtaCteEmpresa2Moneda1;
	
	private BigDecimal saldoCtaCteEmpresa1Moneda2;
	
	private BigDecimal saldoCtaCteEmpresa2Moneda2;
	
	private BigDecimal saldoCtaCteMoneda1MesActual;
	
	private BigDecimal saldoCtaCteMoneda1MesAnt1;
	
	private BigDecimal saldoCtaCteMoneda1MesAnt2;
	
	private BigDecimal saldoCtaCteMoneda1MesAnt3; 
	
	private BigDecimal saldoCtaCteMoneda1MesAnt4;
	
	private BigDecimal saldoCtaCteMoneda1MesAnt5;
	
	private BigDecimal saldoCtaCteMoneda1MesAnt6;		
	
	private BigDecimal saldoCtaCteMoneda2MesActual; 
	
	private BigDecimal saldoCtaCteMoneda2MesAnt1;
	
	private BigDecimal saldoCtaCteMoneda2MesAnt2;
	
	private BigDecimal saldoCtaCteMoneda2MesAnt3; 
	
	private BigDecimal saldoCtaCteMoneda2MesAnt4;
	
	private BigDecimal saldoCtaCteMoneda2MesAnt5;
	
	private BigDecimal saldoCtaCteMoneda2MesAnt6;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public BigDecimal getSaldoCtaCteMoneda1() {
		return saldoCtaCteMoneda1;
	}

	public void setSaldoCtaCteMoneda1(BigDecimal saldoCtaCteMoneda1) {
		this.saldoCtaCteMoneda1 = saldoCtaCteMoneda1;
	}

	public BigDecimal getSaldoPedidos1() {
		return saldoPedidos1;
	}

	public void setSaldoPedidos1(BigDecimal saldoPedidos1) {
		this.saldoPedidos1 = saldoPedidos1;
	}

	public BigDecimal getSaldoCtaCteMoneda2() {
		return saldoCtaCteMoneda2;
	}

	public void setSaldoCtaCteMoneda2(BigDecimal saldoCtaCteMoneda2) {
		this.saldoCtaCteMoneda2 = saldoCtaCteMoneda2;
	}

	public BigDecimal getSaldoPedidos2() {
		return saldoPedidos2;
	}

	public void setSaldoPedidos2(BigDecimal saldoPedidos2) {
		this.saldoPedidos2 = saldoPedidos2;
	}

	public BigDecimal getSituacionCrediticia() {
		return situacionCrediticia;
	}

	public void setSituacionCrediticia(BigDecimal situacionCrediticia) {
		this.situacionCrediticia = situacionCrediticia;
	}

	public BigDecimal getLimiteCredito() {
		return limiteCredito;
	}

	public void setLimiteCredito(BigDecimal limiteCredito) {
		this.limiteCredito = limiteCredito;
	}

	public BigDecimal getCreditoDisponible() {
		return creditoDisponible;
	}

	public void setCreditoDisponible(BigDecimal creditoDisponible) {
		this.creditoDisponible = creditoDisponible;
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

	public BigDecimal getSaldoCtaCteMoneda1MesActual() {
		return saldoCtaCteMoneda1MesActual;
	}

	public void setSaldoCtaCteMoneda1MesActual(BigDecimal saldoCtaCteMoneda1MesActual) {
		this.saldoCtaCteMoneda1MesActual = saldoCtaCteMoneda1MesActual;
	}

	public BigDecimal getSaldoCtaCteMoneda1MesAnt1() {
		return saldoCtaCteMoneda1MesAnt1;
	}

	public void setSaldoCtaCteMoneda1MesAnt1(BigDecimal saldoCtaCteMoneda1MesAnt1) {
		this.saldoCtaCteMoneda1MesAnt1 = saldoCtaCteMoneda1MesAnt1;
	}

	public BigDecimal getSaldoCtaCteMoneda1MesAnt2() {
		return saldoCtaCteMoneda1MesAnt2;
	}

	public void setSaldoCtaCteMoneda1MesAnt2(BigDecimal saldoCtaCteMoneda1MesAnt2) {
		this.saldoCtaCteMoneda1MesAnt2 = saldoCtaCteMoneda1MesAnt2;
	}

	public BigDecimal getSaldoCtaCteMoneda1MesAnt3() {
		return saldoCtaCteMoneda1MesAnt3;
	}

	public void setSaldoCtaCteMoneda1MesAnt3(BigDecimal saldoCtaCteMoneda1MesAnt3) {
		this.saldoCtaCteMoneda1MesAnt3 = saldoCtaCteMoneda1MesAnt3;
	}

	public BigDecimal getSaldoCtaCteMoneda1MesAnt4() {
		return saldoCtaCteMoneda1MesAnt4;
	}

	public void setSaldoCtaCteMoneda1MesAnt4(BigDecimal saldoCtaCteMoneda1MesAnt4) {
		this.saldoCtaCteMoneda1MesAnt4 = saldoCtaCteMoneda1MesAnt4;
	}

	public BigDecimal getSaldoCtaCteMoneda1MesAnt5() {
		return saldoCtaCteMoneda1MesAnt5;
	}

	public void setSaldoCtaCteMoneda1MesAnt5(BigDecimal saldoCtaCteMoneda1MesAnt5) {
		this.saldoCtaCteMoneda1MesAnt5 = saldoCtaCteMoneda1MesAnt5;
	}

	public BigDecimal getSaldoCtaCteMoneda1MesAnt6() {
		return saldoCtaCteMoneda1MesAnt6;
	}

	public void setSaldoCtaCteMoneda1MesAnt6(BigDecimal saldoCtaCteMoneda1MesAnt6) {
		this.saldoCtaCteMoneda1MesAnt6 = saldoCtaCteMoneda1MesAnt6;
	}

	public BigDecimal getSaldoCtaCteMoneda2MesActual() {
		return saldoCtaCteMoneda2MesActual;
	}

	public void setSaldoCtaCteMoneda2MesActual(BigDecimal saldoCtaCteMoneda2MesActual) {
		this.saldoCtaCteMoneda2MesActual = saldoCtaCteMoneda2MesActual;
	}

	public BigDecimal getSaldoCtaCteMoneda2MesAnt1() {
		return saldoCtaCteMoneda2MesAnt1;
	}

	public void setSaldoCtaCteMoneda2MesAnt1(BigDecimal saldoCtaCteMoneda2MesAnt1) {
		this.saldoCtaCteMoneda2MesAnt1 = saldoCtaCteMoneda2MesAnt1;
	}

	public BigDecimal getSaldoCtaCteMoneda2MesAnt2() {
		return saldoCtaCteMoneda2MesAnt2;
	}

	public void setSaldoCtaCteMoneda2MesAnt2(BigDecimal saldoCtaCteMoneda2MesAnt2) {
		this.saldoCtaCteMoneda2MesAnt2 = saldoCtaCteMoneda2MesAnt2;
	}

	public BigDecimal getSaldoCtaCteMoneda2MesAnt3() {
		return saldoCtaCteMoneda2MesAnt3;
	}

	public void setSaldoCtaCteMoneda2MesAnt3(BigDecimal saldoCtaCteMoneda2MesAnt3) {
		this.saldoCtaCteMoneda2MesAnt3 = saldoCtaCteMoneda2MesAnt3;
	}

	public BigDecimal getSaldoCtaCteMoneda2MesAnt4() {
		return saldoCtaCteMoneda2MesAnt4;
	}

	public void setSaldoCtaCteMoneda2MesAnt4(BigDecimal saldoCtaCteMoneda2MesAnt4) {
		this.saldoCtaCteMoneda2MesAnt4 = saldoCtaCteMoneda2MesAnt4;
	}

	public BigDecimal getSaldoCtaCteMoneda2MesAnt5() {
		return saldoCtaCteMoneda2MesAnt5;
	}

	public void setSaldoCtaCteMoneda2MesAnt5(BigDecimal saldoCtaCteMoneda2MesAnt5) {
		this.saldoCtaCteMoneda2MesAnt5 = saldoCtaCteMoneda2MesAnt5;
	}

	public BigDecimal getSaldoCtaCteMoneda2MesAnt6() {
		return saldoCtaCteMoneda2MesAnt6;
	}

	public void setSaldoCtaCteMoneda2MesAnt6(BigDecimal saldoCtaCteMoneda2MesAnt6) {
		this.saldoCtaCteMoneda2MesAnt6 = saldoCtaCteMoneda2MesAnt6;
	}
}
