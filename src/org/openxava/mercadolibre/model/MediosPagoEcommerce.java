package org.openxava.mercadolibre.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.Required;
import org.openxava.calculators.ZeroBigDecimalCalculator;
import org.openxava.tesoreria.model.Tesoreria;
import org.openxava.tesoreria.model.TipoValorConfiguracion;

@Embeddable

public class MediosPagoEcommerce {
	
	@Required
	@Column(length=50)
	private String medioPago;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private TipoValorConfiguracion tipoValor;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private Tesoreria tesoreria;
		
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	@DefaultValueCalculator(value=ZeroBigDecimalCalculator.class)
	private BigDecimal porcentajeDescuento = BigDecimal.ZERO;
	
	public String getMedioPago() {
		return medioPago;
	}

	public void setMedioPago(String medioPago) {
		this.medioPago = medioPago;
	}

	public TipoValorConfiguracion getTipoValor() {
		return tipoValor;
	}

	public void setTipoValor(TipoValorConfiguracion tipoValor) {
		this.tipoValor = tipoValor;
	}

	public Tesoreria getTesoreria() {
		return tesoreria;
	}

	public void setTesoreria(Tesoreria tesoreria) {
		this.tesoreria = tesoreria;		
	}

	public BigDecimal getPorcentajeDescuento() {
		return porcentajeDescuento == null ? BigDecimal.ZERO : this.porcentajeDescuento;
	}

	public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) {
		if (porcentajeDescuento != null){
			this.porcentajeDescuento = porcentajeDescuento;
		}
	}	
}
