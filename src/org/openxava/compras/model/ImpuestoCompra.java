package org.openxava.compras.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.impuestos.model.*;

@Embeddable

public class ImpuestoCompra {
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@SearchListCondition("${compras} = 't'")
	private Impuesto impuesto;
	
	@Required
	private BigDecimal importe = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal importe1 = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal importe2 = BigDecimal.ZERO;
	
	private BigDecimal alicuota = BigDecimal.ZERO;

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

	public BigDecimal getAlicuota() {
		return alicuota;
	}

	public void setAlicuota(BigDecimal alicuota) {
		this.alicuota = alicuota;
	}

	public BigDecimal getImporte1() {
		return importe1;
	}

	public void setImporte1(BigDecimal importe1) {
		this.importe1 = importe1;
	}

	public BigDecimal getImporte2() {
		return importe2;
	}

	public void setImporte2(BigDecimal importe2) {
		this.importe2 = importe2;
	}
}
