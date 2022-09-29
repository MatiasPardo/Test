package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.impuestos.model.*;

@Embeddable

public class ImpuestoCobranza {
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="codigo, nombre",
			condition="${cobranzas} = 't'")
	private Impuesto impuesto;
	
	@Required
	private BigDecimal importe = BigDecimal.ZERO;
	
	@Required
	@DefaultValueCalculator(CurrentDateCalculator.class)
	private Date fecha = new Date();
	
	@Column(length=25)
	private String numero;
	
	public Impuesto getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(Impuesto impuesto) {
		this.impuesto = impuesto;
	}

	public BigDecimal getImporte() {
		return importe == null ? BigDecimal.ZERO : importe;
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
}
