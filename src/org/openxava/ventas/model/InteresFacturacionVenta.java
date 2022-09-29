package org.openxava.ventas.model;

import java.math.BigDecimal;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.Required;

@Embeddable

public class InteresFacturacionVenta {
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre", condition="${ventas} = 't'")
	private CondicionVenta condicionVenta;
	
	@Min(value=0, message="No puede menor a 0")
	@Required
	private BigDecimal importe = BigDecimal.ZERO;

	public CondicionVenta getCondicionVenta() {
		return condicionVenta;
	}

	public void setCondicionVenta(CondicionVenta condicionVenta) {
		this.condicionVenta = condicionVenta;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		if (importe != null){
			this.importe = importe;
		}
		else{
			this.importe = BigDecimal.ZERO;
		}
	}	
}
