package org.openxava.impuestos.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;

@Entity

public class AlicuotaImpuesto extends ObjetoEstatico{
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	private Impuesto impuesto; 
	
	@Required
	private PosicionAnteRetencion posicion;
	
	@DefaultValueCalculator(value=BigDecimalCalculator.class, 
			properties={@PropertyValue(name="value", value="0")})
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	private BigDecimal porcentaje;
	
	@DefaultValueCalculator(value=ZeroBigDecimalCalculator.class)
	@Min(value=0, message="No puede menor a 0")
	private BigDecimal minimo;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private TablaEscalasImpuesto escalas;
	
	public PosicionAnteRetencion getPosicion() {
		return posicion;
	}

	public void setPosicion(PosicionAnteRetencion posicion) {
		this.posicion = posicion;
	}

	public Impuesto getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(Impuesto impuesto) {
		this.impuesto = impuesto;
	}

	public BigDecimal getPorcentaje() {
		return porcentaje;
	}

	public void setPorcentaje(BigDecimal porcentaje) {
		this.porcentaje = porcentaje;
	}

	public TablaEscalasImpuesto getEscalas() {
		return escalas;
	}

	public void setEscalas(TablaEscalasImpuesto escalas) {
		this.escalas = escalas;
	}
	
	public BigDecimal getMinimo() {
		return minimo == null ? BigDecimal.ZERO : minimo;
	}

	public void setMinimo(BigDecimal minimo) {
		this.minimo = minimo;
	}
}
