package org.openxava.contabilidad.model;

import java.math.BigDecimal;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Required;

@Embeddable

public class DistribucionCentroCosto {
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre", condition="${distribuye} = 'f'")
	private CentroCostos distribucionCostos;
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	@Required
	private BigDecimal porcentaje;

	public CentroCostos getDistribucionCostos() {
		return distribucionCostos;
	}

	public void setDistribucionCostos(CentroCostos distribucionCostos) {
		this.distribucionCostos = distribucionCostos;
	}

	public BigDecimal getPorcentaje() {
		return porcentaje;
	}

	public void setPorcentaje(BigDecimal porcentaje) {
		this.porcentaje = porcentaje;
	}	
}
