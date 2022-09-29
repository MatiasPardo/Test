package org.openxava.contabilidad.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.contabilidad.calculators.*;

@Entity

@Tab(
	properties="asiento.numero, asiento.fecha, asiento.empresa.nombre, asiento.detalle, cuenta.codigo, cuenta.nombre, debe, haber, asiento.fechaCreacion",
	defaultOrder="${asiento.numero} desc")

public class ItemAsientoConsolidado{

	@Id @Hidden
	@Column(length=32)
	private String id;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView(value="Simple")
	private AsientoConsolidado asiento;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate
	@NoModify
	@ReadOnly
	private CuentaContable cuenta;
	
	@Min(value=0, message="No puede ser negativo")
	@DefaultValueCalculator(
			value=ImporteDebeCalculator.class,
			properties={@PropertyValue(name="haber", from="haber")}
			)
	@ReadOnly
	private BigDecimal debe = BigDecimal.ZERO;
	
	@Min(value=0, message="No puede ser negativo")
	@DefaultValueCalculator(
			value=ImporteHaberCalculator.class,
			properties={@PropertyValue(name="debe", from="debe")}
			)
	@ReadOnly
	private BigDecimal haber = BigDecimal.ZERO;
	
	public AsientoConsolidado getAsiento() {
		return asiento;
	}

	public void setAsiento(AsientoConsolidado asiento) {
		this.asiento = asiento;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
