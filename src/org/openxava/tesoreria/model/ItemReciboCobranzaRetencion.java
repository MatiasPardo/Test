package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.impuestos.model.*;

@Entity

@Tabs({
	@Tab(name="RetencionesConfirmadas",
		baseCondition="${reciboCobranza.estado} in (1)",
		defaultOrder="${fecha} desc",
		properties="fecha, impuesto.codigo, impuesto.nombre, numero, importe, reciboCobranza.numero, reciboCobranza.cliente.codigo, reciboCobranza.cliente.nombre"),
	@Tab(defaultOrder="${fecha} desc",
		properties="fecha, impuesto.codigo, impuesto.nombre, numero, importe, reciboCobranza.estado, reciboCobranza.numero, reciboCobranza.cliente.codigo, reciboCobranza.cliente.nombre")
})

public class ItemReciboCobranzaRetencion extends ItemTransaccion{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	private ReciboCobranza reciboCobranza;
		
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@SearchListCondition("${cobranzas} = 't'")
	private Impuesto impuesto;
	
	@Required
	private BigDecimal importe = BigDecimal.ZERO;
	
	@Required
	@DefaultValueCalculator(CurrentDateCalculator.class)
	private Date fecha = new Date();
	
	@Column(length=25)
	private String numero;

	public ReciboCobranza getReciboCobranza() {
		return reciboCobranza;
	}

	public void setReciboCobranza(ReciboCobranza reciboCobranza) {
		this.reciboCobranza = reciboCobranza;
	}

	public Impuesto getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(Impuesto impuesto) {
		this.impuesto = impuesto;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	@Override
	public Transaccion transaccion() {		
		return this.getReciboCobranza();
	}

	@Override
	public void recalcular() {		
	}
}
