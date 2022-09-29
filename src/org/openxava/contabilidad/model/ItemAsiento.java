package org.openxava.contabilidad.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.calculators.*;
import org.openxava.contabilidad.validators.ItemAsientoValidator;
import org.openxava.util.*;
import org.openxava.base.filter.EmpresaFilter;


@Entity

@Tabs({
	@Tab(
		properties="asiento.numero, asiento.libroDiario, asiento.fecha, asiento.detalle, asiento.observaciones, cuenta.codigo, cuenta.nombre, debe, haber, asiento.fechaCreacion",
		defaultOrder="${fechaCreacion} desc", 
		baseCondition="e.asiento.estado = 1 and " + EmpresaFilter.CONDITION_ITEMS1 + "asiento" + EmpresaFilter.CONDITION_ITEMS2 + "asiento" + EmpresaFilter.CONDITION_ITEMS3,
		filter=EmpresaFilter.class),
	@Tab(name="ImportacionCSV", 
		properties="asiento.fecha, asiento.numero, asiento.detalle, asiento.observaciones, asiento.tipoAsiento.codigo, cuenta.codigo, debe, haber, detalle, centroCostos.codigo, unidadNegocio.codigo", 
		baseCondition="e.asiento.estado = 1 and " + EmpresaFilter.CONDITION_ITEMS1 + "asiento" + EmpresaFilter.CONDITION_ITEMS2 + "asiento" + EmpresaFilter.CONDITION_ITEMS3,		
		filter=EmpresaFilter.class, 
		defaultOrder="${asiento.fecha} desc, ${asiento.numero} desc")
})


@Views({
	@View(members="asiento; cuenta; debe, haber; detalle; centroCostos; unidadNegocio;"),
	@View(name="Mayor", 
		members="asiento"),
	@View(name="ReimputarCentroCostos", 
		members="cuenta, debe, haber; detalle; centroCostos; unidadNegocio")
})


@EntityValidators({
	@EntityValidator(value=ItemAsientoValidator.class, 
			properties= {
					@PropertyValue(name="transaccion", from="asiento"), 
					@PropertyValue(name="cuenta"),
					@PropertyValue(name="centroCostos")
				}
	)
})

public class ItemAsiento extends ItemTransaccion{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView(value="Simple", notForViews="Mayor")
	private Asiento asiento;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate
	@NoModify
	@ReadOnly(forViews="ReimputarCentroCostos")
	private CuentaContable cuenta;
	
	
	@Min(value=0, message="No puede ser negativo")
	@DefaultValueCalculator(
			value=ImporteDebeCalculator.class,
			properties={@PropertyValue(name="haber", from="haber")}
			)
	private BigDecimal debe = BigDecimal.ZERO;
	
	@Min(value=0, message="No puede ser negativo")
	@DefaultValueCalculator(
			value=ImporteHaberCalculator.class,
			properties={@PropertyValue(name="debe", from="debe")}
			)
	private BigDecimal haber = BigDecimal.ZERO;
		
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
		
	public Asiento getAsiento() {
		return asiento;
	}

	public void setAsiento(Asiento asiento) {
		this.asiento = asiento;
	}

	public CuentaContable getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaContable cuenta) {
		this.cuenta = cuenta;
	}

	public BigDecimal getDebe() {
		return this.debe == null? BigDecimal.ZERO:this.debe;
	}

	public void setDebe(BigDecimal debe) {
		if (debe == null){
			this.debe = BigDecimal.ZERO;
		}
		else{
			this.debe = debe;
		}
	}

	public BigDecimal getHaber() {
		return this.haber == null? BigDecimal.ZERO:this.haber;
	}

	public void setHaber(BigDecimal haber) {
		if (haber == null){
			this.haber = BigDecimal.ZERO;
		}
		else{
			this.haber = haber;
		}
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

	@Override
	public Boolean soloLectura() {
		boolean readonly = super.soloLectura();
		if (!readonly){
			if (this.asiento != null){
				readonly = this.asiento.soloLectura();
			}
		}
		return readonly;
	}

	public boolean fusionarA(ItemAsiento itemAsiento) {
		boolean fusionado = false;
		if (itemAsiento.getCuenta().equals(this.getCuenta()) && 
			Is.equal(itemAsiento.getCentroCostos(), this.getCentroCostos()) &&
			Is.equal(itemAsiento.getUnidadNegocio(), this.getUnidadNegocio()) &&
			Is.equalAsStringIgnoreCase(itemAsiento.getDetalle(), this.getDetalle())){
			
			if (itemAsiento.getDebe().compareTo(BigDecimal.ZERO) > 0){
				if (this.getDebe().compareTo(BigDecimal.ZERO) > 0){
					itemAsiento.setDebe(itemAsiento.getDebe().add(this.getDebe()));
					fusionado = true;
				}
			}
			else if (this.getHaber().compareTo(BigDecimal.ZERO) > 0){
				itemAsiento.setHaber(itemAsiento.getHaber().add(this.getHaber()));
				fusionado = true;
			}			
		}
		return fusionado;
	}

	@Override
	public Transaccion transaccion() {
		return this.getAsiento();
	}

	@Override
	public void recalcular() {	
	}
	
	public void invertirPaseContable() {
		if (this.getDebe().compareTo(BigDecimal.ZERO) != 0){
			this.setHaber(this.getDebe());
			this.setDebe(BigDecimal.ZERO);
		}
		else{
			this.setDebe(this.getHaber());
			this.setHaber(BigDecimal.ZERO);
		}
		
	}

	public boolean distribuyePorCentroCostos() {
		if ((this.getCentroCostos() != null) && (this.getCentroCostos().getDistribuye())){
			return true;
		}
		else{
			return false;
		}
	}

	public void distribuir(BigDecimal porcentaje) {
		if (this.getDebe().compareTo(BigDecimal.ZERO) != 0){
			if (porcentaje.compareTo(new BigDecimal(100)) != 0){
				this.setDebe(this.getDebe().multiply(porcentaje).divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN));
			}
		}
		
		if (this.getHaber().compareTo(BigDecimal.ZERO) != 0){
			if (porcentaje.compareTo(new BigDecimal(100)) != 0){
				this.setHaber(this.getHaber().multiply(porcentaje).divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN));
			}
		}
	}

	public BigDecimal importeAsiento() {
		return this.getDebe().subtract(this.getHaber());
	}

	public void asignarImporteAsiento(BigDecimal importe){
		int compare = importe.compareTo(BigDecimal.ZERO);
		if (compare > 0){
			this.setDebe(importe);
			this.setHaber(BigDecimal.ZERO);
		}
		else if (compare < 0){
			this.setDebe(BigDecimal.ZERO);
			this.setHaber(importe.abs());
		}
		else{
			this.setDebe(BigDecimal.ZERO);
			this.setHaber(BigDecimal.ZERO);
		}
	}
	
	public void ajustarImporteAsiento(BigDecimal importe) {
		if (importe.compareTo(BigDecimal.ZERO) != 0){
			int compare = this.importeAsiento().compareTo(BigDecimal.ZERO);
			if (compare > 0){
				// Saldo deudor (debe)
				if (importe.compareTo(BigDecimal.ZERO) > 0){
					this.setDebe(this.getDebe().add(importe));
				}
				else{
					BigDecimal importeAbs = importe.abs();
					if (this.getHaber().compareTo(BigDecimal.ZERO) > 0){
						// como hay haber, se suma ahí
						this.setHaber(this.getHaber().add(importeAbs));
					}
					else{
						this.setDebe(this.getDebe().subtract(importeAbs));
						if (this.getDebe().compareTo(BigDecimal.ZERO) < 0){
							// se invierte
							this.setHaber(this.getHaber().add(this.getDebe().abs()));
							this.setDebe(BigDecimal.ZERO);
						}
					}
				}
			}
			else if (compare < 0){
				// saldo acreedor (haber)
				
				BigDecimal importeAbs = importe.abs();
				if (importe.compareTo(BigDecimal.ZERO) < 0){
					this.setHaber(this.getHaber().add(importeAbs));
				}
				else{
					if (this.getDebe().compareTo(BigDecimal.ZERO) > 0){
						// como hay debe, se suma ahí
						this.setDebe(this.getDebe().add(importeAbs));
					}
					else{
						this.setHaber(this.getHaber().subtract(importeAbs));
						if (this.getHaber().compareTo(BigDecimal.ZERO) < 0){
							// se invierte
							this.setDebe(this.getDebe().add(this.getHaber().abs()));
							this.setHaber(BigDecimal.ZERO);
						}
					}
				}
			}
		}
	}

	public void redondear() {
		if (this.getDebe().compareTo(BigDecimal.ZERO) != 0){
			this.setDebe(this.getDebe().setScale(2, RoundingMode.HALF_EVEN));
		}
		if (this.getHaber().compareTo(BigDecimal.ZERO) != 0){
			this.setHaber(this.getHaber().setScale(2, RoundingMode.HALF_EVEN));
		}
	}
}
