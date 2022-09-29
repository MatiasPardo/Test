package org.openxava.contabilidad.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.calculators.*;

@Entity

public class ItemAsientoPlantilla extends ObjetoNegocio{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private AsientoPlantilla asientoPlantilla;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate
	@NoModify
	private CuentaContable cuenta;
	
	
	@Min(value=0, message="No puede ser negativo")
	@DefaultValueCalculator(
			value=ImporteDebeCalculator.class,
			properties={@PropertyValue(name="haber", from="haber")}
			)
	private BigDecimal debe;
	
	@Min(value=0, message="No puede ser negativo")
	@DefaultValueCalculator(
			value=ImporteHaberCalculator.class,
			properties={@PropertyValue(name="debe", from="debe")}
			)
	private BigDecimal haber;
		
	@Column(length=100)
	private String detalle;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate
	@NoModify
	private CentroCostos centroCostos;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate
	@NoModify
	private UnidadNegocio unidadNegocio;

	public AsientoPlantilla getAsientoPlantilla() {
		return asientoPlantilla;
	}

	public void setAsientoPlantilla(AsientoPlantilla asientoPlantilla) {
		this.asientoPlantilla = asientoPlantilla;
	}

	public CuentaContable getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaContable cuenta) {
		this.cuenta = cuenta;
	}

	public BigDecimal getDebe() {
		return debe;
	}

	public void setDebe(BigDecimal debe) {
		this.debe = debe;
	}

	public BigDecimal getHaber() {
		return haber;
	}

	public void setHaber(BigDecimal haber) {
		this.haber = haber;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public CentroCostos getCentroCostos() {
		return centroCostos;
	}

	public void setCentroCostos(CentroCostos centroCostos) {
		this.centroCostos = centroCostos;
	}

	public UnidadNegocio getUnidadNegocio() {
		return unidadNegocio;
	}

	public void setUnidadNegocio(UnidadNegocio unidadNegocio) {
		this.unidadNegocio = unidadNegocio;
	}
}
