package org.openxava.tesoreria.model;

import java.math.BigDecimal;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.ventas.model.Producto;

@Embeddable

public class ItemLiquidacionTarjetaCredito {
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@SearchListCondition(value="${compras} = 't'")
	@NoCreate @NoModify
	@ReferenceView("Simple")	
	private Producto concepto;
	
	@Required
	private BigDecimal importe;

	@ReadOnly
	private BigDecimal tasaiva = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal iva;
	
	public Producto getConcepto() {
		return concepto;
	}

	public void setConcepto(Producto concepto) {
		this.concepto = concepto;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public BigDecimal getIva() {
		return iva == null ? BigDecimal.ZERO : this.iva;
	}

	public void setIva(BigDecimal iva) {
		if (iva != null){
			this.iva = iva;
		}
		
	}

	public BigDecimal getTasaiva() {
		return tasaiva == null ? BigDecimal.ZERO : this.tasaiva;
	}

	public void setTasaiva(BigDecimal tasaiva) {
		if (tasaiva != null){
			this.tasaiva = tasaiva;
		}
	}	
}
